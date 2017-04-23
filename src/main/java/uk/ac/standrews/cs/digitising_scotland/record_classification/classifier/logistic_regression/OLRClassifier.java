/*
 * Copyright 2012-2017 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module record-classification.
 *
 * record-classification is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * record-classification is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with record-classification. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.logistic_regression;

import org.apache.mahout.classifier.*;
import org.apache.mahout.classifier.sgd.*;
import org.apache.mahout.math.*;
import org.apache.mahout.math.Vector;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.logging.*;
import java.util.stream.*;

/**
 * @author Masih Hajararab Derkani
 */
public class OLRClassifier extends SingleClassifier implements Externalizable {

    /** The default number of folds in cross-fold learner. **/
    public static final int DEFAULT_FOLDS = 4;

    /** Short description of this classifier. **/
    public static final String DESCRIPTION = "Classifies using Mahout Online Logistic Regression Classifier and CrossFold learner.";

    /** The default number of iterations over the training records. **/
    public static final int DEFAULT_ITERATIONS_OVER_TRAINING_DATA = 30;

    private static final long serialVersionUID = 5972187130211865595L;
    // why intercept is used: http://statistiksoftware.blogspot.nl/2013/01/why-we-need-intercept.html
    private static final int INTERCEPT_OFFSET = 1;
    private static final int INTERCEPT_VECTOR_INDEX = 0;
    private static final int INTERCEPT_INITIAL_VALUE = 1;

    private static final Logger LOGGER = Logger.getLogger(OLRClassifier.class.getName());

    private final AtomicInteger next_token_index = new AtomicInteger(INTERCEPT_OFFSET);
    private final AtomicInteger next_classification_index = new AtomicInteger();
    private final AtomicLong next_training_record_id = new AtomicLong();
    private final ConcurrentHashMap<String, Integer> classification_to_index;
    private final ConcurrentHashMap<Integer, String> index_to_classification;
    private final ConcurrentHashMap<String, Integer> token_to_index;
    private final int folds;
    private final int training_iteration;

    private transient Optional<Random> random;
    private transient CrossFoldLearner model;

    public OLRClassifier() {

        this(DEFAULT_FOLDS, DEFAULT_ITERATIONS_OVER_TRAINING_DATA);
    }

    public OLRClassifier(int folds, int training_iteration) {

        this(folds, training_iteration, Optional.empty());
    }

    public OLRClassifier(int folds, int training_iteration, Optional<Random> random) {

        this.folds = folds;
        this.training_iteration = training_iteration;
        this.random = random;

        index_to_classification = new ConcurrentHashMap<>();
        classification_to_index = new ConcurrentHashMap<>();
        token_to_index = new ConcurrentHashMap<>();
    }

    @Override
    protected Classification doClassify(final String unclassified) {

        final Classification classification;

        if (!isTrained()) {
            classification = Classification.UNCLASSIFIED;
        }
        else {

            final TokenList tokens = new TokenList(unclassified);
            final Vector vector = toFeatureVector(tokens);
            final Vector classification_probability_vector = model.classifyFull(vector);
            final int most_probable_classification_index = classification_probability_vector.maxValueIndex();

            if (index_to_classification.containsKey(most_probable_classification_index)) {
                final String classification_code = index_to_classification.get(most_probable_classification_index);
                final double probability = classification_probability_vector.get(most_probable_classification_index);
                classification = new Classification(classification_code, tokens, probability, null);
            }
            else {
                classification = Classification.UNCLASSIFIED;
            }
        }
        return classification;
    }

    protected boolean isTrained() { return model != null; }

    @Override
    public void trainModel(final Bucket training_records) {

        requireUntrainedModel();
        index(training_records);
        train(toOnlineTrainingRecords(training_records));
    }

    protected List<OnlineTrainingRecord> toOnlineTrainingRecords(final Bucket training_records) {

        return training_records.parallelStream().map(this::toOnlineTrainingRecord).collect(Collectors.toList());
    }

    protected void train(final List<OnlineTrainingRecord> training_records) {

        model = initModel();

        final int training_records_size = training_records.size();
        final int total_training_steps = training_records_size * training_iteration;

        resetTrainingProgressIndicator(total_training_steps);

        final List<ForkJoinTask<?>> training_iterations = new ArrayList<>();
        final ForkJoinPool pool = ForkJoinPool.commonPool();
        try {
            for (int i = 0; i < training_iteration; i++) {

                final ForkJoinTask<?> training_iteration = pool.submit(() -> {
                    shuffle(training_records).parallelStream().forEach(record -> train(model, record));
                });
                training_iterations.add(training_iteration);
            }
        }
        finally {
            model.close();
        }

        awaitCompletion(training_iterations);
    }

    private CrossFoldLearner initModel() {

        final PriorFunction regularisation = new L2();
        final int categories = countCategories();
        final int features = countFeatures();
        return new CrossFoldLearner(folds, categories, features, regularisation);
    }

    private List<OnlineTrainingRecord> shuffle(final List<OnlineTrainingRecord> olr_training_records) {

        List<OnlineTrainingRecord> shuffled = new ArrayList<>(olr_training_records);
        Collections.shuffle(shuffled, getRandom());

        return shuffled;
    }

    protected void train(final OnlineLearner model, final OnlineTrainingRecord record) {

        model.train(record.id, record.code, record.feature_vector);
        progressTrainingStep();
    }

    protected Random getRandom() {

        return random.isPresent() ? random.get() : ThreadLocalRandom.current();
    }

    private void awaitCompletion(final List<ForkJoinTask<?>> training_iterations) {

        for (ForkJoinTask<?> iteration : training_iterations) {
            try {
                iteration.get();
            }
            catch (InterruptedException error) {
                cancel(training_iterations);
                throw new RuntimeException("interrupted while awaiting training completion", error);
            }
            catch (ExecutionException error) {
                cancel(training_iterations);
                throw new RuntimeException("error while training", error.getCause());
            }
        }
    }

    private void cancel(final List<ForkJoinTask<?>> training_iterations) {

        training_iterations.forEach(iteration -> {
            iteration.cancel(true);
        });
    }

    protected void requireUntrainedModel() {

        if (isTrained()) {
            throw new UnsupportedOperationException("already trained, further training upon existing trained model is not implemented yet");
        }
    }

    protected void index(final Bucket training_records) {

        LOGGER.info(() -> String.format("indexing %d training records as part of olr classifier training...", training_records.size()));
        training_records.parallelStream().map(Record::getClassification).forEach(classification -> {

            final String original_code = classification.getCode();
            indexClassificationCode(original_code);

            final TokenList tokens = classification.getTokenList();
            tokens.forEach(this::indexToken);
        });
    }

    private Integer indexClassificationCode(String code) {

        return classification_to_index.computeIfAbsent(code, key -> {
            final int value = next_classification_index.getAndIncrement();

            final String other_code = index_to_classification.putIfAbsent(value, key);

            if (other_code != null) {
                throw new IllegalStateException(String.format("inconsistent index for codes %s and %s", other_code, code));
            }

            return value;
        });
    }

    @Override
    protected void clearModel() {

        model = null;
        token_to_index.clear();
        classification_to_index.clear();
        index_to_classification.clear();
        next_token_index.set(INTERCEPT_OFFSET);
        next_classification_index.set(0);
        next_training_record_id.set(0);
    }

    protected Vector toFeatureVector(final TokenList tokens) {

        final Vector vector = new RandomAccessSparseVector(countFeatures());
        setIntercept(vector);
        tokens.forEach(token -> {
            if (isTokenIndexed(token)) {
                final Integer index = getIndexToken(token);
                vector.incrementQuick(index, 1);
            }
        });
        return vector;
    }

    int countFeatures() {return countUniqueTokens() + INTERCEPT_OFFSET;}

    protected int countUniqueTokens() {return token_to_index.size();}

    private void setIntercept(final Vector vector) {vector.setQuick(INTERCEPT_VECTOR_INDEX, INTERCEPT_INITIAL_VALUE);}

    private boolean isTokenIndexed(final String token) {

        return token_to_index.containsKey(token);
    }

    private Integer getIndexToken(String token) {

        return token_to_index.get(token);
    }

    protected int countCategories() {return classification_to_index.size();}

    private Integer indexToken(String token) {

        return token_to_index.computeIfAbsent(token, key -> next_token_index.getAndIncrement());
    }

    private OnlineTrainingRecord toOnlineTrainingRecord(Record record) {

        final Classification classification = record.getClassification();
        final TokenList tokens = classification.getTokenList();
        final String original_code = classification.getCode();

        final int code_index = classification_to_index.get(original_code);
        final Vector feature_vector = toFeatureVector(tokens);
        return new OnlineTrainingRecord(code_index, feature_vector);
    }

    @Override
    public String getDescription() {

        return DESCRIPTION;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {

        out.writeObject(classification_to_index);
        out.writeObject(index_to_classification);
        out.writeObject(token_to_index);

        final boolean trained = model != null;
        out.writeBoolean(trained);
        if (trained) {
            model.write(out);
        }
        final boolean random_set = random.isPresent();
        out.writeBoolean(random_set);
        if (random_set) {
            out.writeObject(random.get());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {

        final Map<String, Integer> classification_to_index = (Map<String, Integer>) in.readObject();
        this.classification_to_index.putAll(classification_to_index);

        final Map<Integer, String> index_to_classification = (Map<Integer, String>) in.readObject();
        this.index_to_classification.putAll(index_to_classification);

        final Map<String, Integer> token_to_index = (Map<String, Integer>) in.readObject();
        this.token_to_index.putAll(token_to_index);

        final boolean trained = in.readBoolean();
        if (trained) {
            model = new CrossFoldLearner();
            model.readFields(in);
        }
        final boolean random_set = in.readBoolean();
        random = random_set ? Optional.of((Random) in.readObject()) : Optional.empty();
    }

    private class OnlineTrainingRecord {

        protected final long id;
        protected final int code;
        protected final Vector feature_vector;

        private OnlineTrainingRecord(int code, Vector feature_vector) {

            this.code = code;
            this.feature_vector = feature_vector;
            id = next_training_record_id.getAndIncrement();
        }
    }
}
