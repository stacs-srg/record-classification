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
package uk.ac.standrews.cs.digitising_scotland.record_classification.process;

import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.Metrics;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.ConfusionMatrix;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.Classifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;

/**
 * Captures the shared knowledge among the {@link Step steps} of a {@link ClassificationProcess
 * classification process}.
 *
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
public class ClassificationContext implements Serializable {

    private static final long serialVersionUID = -6389479358148790573L;

    private Random random;
    private Classifier classifier;
    private Bucket training_records;
    private Bucket evaluation_records;
    private Bucket unseen_records;
    private Bucket classified_evaluation_records;
    private Bucket classified_unseen_records;
    private ConfusionMatrix confusion_matrix;
    private Metrics classification_metrics;

    private Duration training_time;
    private Duration evaluation_classification_time;

    public ClassificationContext() {

        this(null, new Random());
    }

    /**
     * Instantiates a new classification context.
     *
     * @param random the random number generator
     */
    public ClassificationContext(Classifier classifier, Random random) {

        setClassifier(classifier);
        setRandom(random);
    }

    /**
     * Adds the given records to the bucket of records that are to be classified.
     *
     * @param unseen_records the records to be added
     */
    public void addUnseenRecords(Iterable<Record> unseen_records) {

        final Bucket records = getUnseenRecords();
        unseen_records.forEach(records::add);
    }

    /**
     * Gets the records that are to be classified.
     *
     * @return the records that are to be classified.
     */
    public Bucket getUnseenRecords() {

        if (!isUnseenRecordsSet()) {
            resetUnseenRecords();
        }
        return unseen_records;
    }

    protected boolean isUnseenRecordsSet() {return unseen_records != null;}

    public void resetUnseenRecords() {

        this.unseen_records = new Bucket();
    }

    /**
     * Sets the given records to the bucket of records that are to be classified.
     *
     * @param unseen_records the records to be added
     */
    public void setUnseenRecords(final Bucket unseen_records) {

        resetUnseenRecords();
        this.unseen_records.add(unseen_records);
    }

    /**
     * Gets the evaluation records with unique data in this context.
     *
     * @return the evaluation records with unique data, or {@code null} if not set
     */
    public Bucket getUniqueEvaluationRecords() {

        final Optional<Bucket> evaluation_optional = getEvaluationRecordsOptional();
        return evaluation_optional.isPresent() ? evaluation_optional.get().makeUniqueDataRecords() : null;
    }

    public Optional<Bucket> getEvaluationRecordsOptional() {

        return Optional.ofNullable(getEvaluationRecords());
    }

    /**
     * Gets the evaluation records in this context, including duplicate data records.
     *
     * @return the evaluation records including duplicate data records, or {@code null} if not set
     */
    public Bucket getEvaluationRecords() {

        if (!isEvaluationRecordsSet()) {
            resetEvaluationRecords();
        }
        return evaluation_records;
    }

    protected boolean isEvaluationRecordsSet() {return evaluation_records != null;}

    public void resetEvaluationRecords() {

        // Do allow multiple data sets with potentially clashing ids to be added to evaluation records.
        this.evaluation_records = newBucketWithClashingIdAllowed();
    }

    private Bucket newBucketWithClashingIdAllowed() {

        return new Bucket(true);
    }

    public void setEvaluationRecords(Bucket evaluation_records) {

        resetEvaluationRecords();
        this.evaluation_records.add(evaluation_records);
    }

    public Optional<Bucket> getClassifiedEvaluationRecordsOptional() {

        return Optional.ofNullable(getClassifiedEvaluationRecords());
    }

    public Bucket getClassifiedEvaluationRecords() {

        if (!isClassifiedEvaluationRecordsSet()) {
            resetClassifiedEvaluationRecords();
        }

        return classified_evaluation_records;
    }

    protected boolean isClassifiedEvaluationRecordsSet() {return classified_evaluation_records != null;}

    private void resetClassifiedEvaluationRecords() {

        this.classified_evaluation_records = new Bucket();
    }

    public void setClassifiedEvaluationRecords(final Bucket classified_evaluation_records) {

        resetClassifiedEvaluationRecords();
        this.classified_evaluation_records.add(classified_evaluation_records);
    }

    public Optional<Bucket> getUnseenRecordsOptional() {

        return Optional.ofNullable(getUnseenRecords());
    }

    public Optional<Bucket> getClassifiedUnseenRecordsOptional() {

        return Optional.ofNullable(getClassifiedUnseenRecords());
    }

    /**
     * Gets the records that are classified by the classifier.
     *
     * @return the records that are classified by the classifier.
     */
    public Bucket getClassifiedUnseenRecords() {

        if (!isClassifiedUnseenRecordsSet()) {
            resetClassifiedUnseenRecords();
        }

        return classified_unseen_records;
    }

    protected boolean isClassifiedUnseenRecordsSet() {return classified_unseen_records != null;}

    private void resetClassifiedUnseenRecords() {

        this.classified_unseen_records = new Bucket();
    }

    /**
     * Sets the given records to the bucket of records that have been classified.
     *
     * @param classified_unseen_records the records to be set
     */
    public void setClassifiedUnseenRecords(final Bucket classified_unseen_records) {

        resetClassifiedUnseenRecords();
        this.classified_unseen_records.add(classified_unseen_records);
    }

    protected Optional<Bucket> getGoldStandardRecordsOptional() {

        return Optional.ofNullable(getGoldStandardRecords());
    }

    public Bucket getGoldStandardRecords() {

        final Optional<Bucket> training_optional = getTrainingRecordsOptional();
        final Optional<Bucket> evaluation_optional = getEvaluationRecordsOptional();

        final boolean training_records_is_present = training_optional.isPresent();
        final boolean evaluation_records_is_present = evaluation_optional.isPresent();

        if (training_records_is_present && evaluation_records_is_present) {
            return training_optional.get().union(evaluation_optional.get());
        }
        else if (training_records_is_present) {
            return training_optional.get();
        }
        else if (evaluation_records_is_present) {
            return evaluation_optional.get();
        }
        else {
            return null;
        }
    }

    public Optional<Bucket> getTrainingRecordsOptional() {

        return Optional.ofNullable(getTrainingRecords());
    }

    /**
     * Gets the records that are used to train the classifier of this context.
     *
     * @return the records that are used to train the classifier of this context, or {@code null} if
     * the records are not set.
     */
    public Bucket getTrainingRecords() {

        if (!isTrainingRecordsSet()) {
            resetTrainingRecords();
        }

        return training_records;
    }

    public boolean isTrainingRecordsSet() {return training_records != null;}

    public void resetTrainingRecords() {

        // Do allow multiple data sets with potentially clashing ids to be added to training records.
        this.training_records = newBucketWithClashingIdAllowed();
    }

    public void setTrainingRecords(Bucket training_records) {

        resetTrainingRecords();
        this.training_records.add(training_records);
    }

    public void setGoldStandardRecords(Bucket gold_standard, double training_ratio) {

        final Bucket training_records = gold_standard.randomSubset(getRandom(), training_ratio);
        final Bucket evaluation_records = gold_standard.difference(training_records);

        setTrainingRecords(training_records);
        setEvaluationRecords(evaluation_records);
    }

    /**
     * Gets the random number generator.
     *
     * @return the random number generator.
     */
    public Random getRandom() {

        return random;
    }

    protected void setRandom(final Random random) {

        this.random = random;
    }

    public void addGoldStandardRecords(Bucket gold_standard, double training_ratio) {

        final Bucket training_records = gold_standard.randomSubset(getRandom(), training_ratio);
        final Bucket evaluation_records = gold_standard.difference(training_records);

        addTrainingRecords(training_records);
        addEvaluationRecords(evaluation_records);
    }

    /**
     * Adds the given records to the bucket of records that are used to train the classifier of this
     * context.
     *
     * @param training_records the records to be added
     */
    public void addTrainingRecords(Iterable<Record> training_records) {

        final Bucket records = getTrainingRecords();
        training_records.forEach(records::add);
    }

    /**
     * Adds the given records to the bucket for evaluating the classifier.
     *
     * @param evaluation_records the records to be added
     */
    public void addEvaluationRecords(final Iterable<Record> evaluation_records) {

        final Bucket records = getEvaluationRecords();
        evaluation_records.forEach(records::add);
    }

    public Optional<Classifier> getClassifierOptional() {

        return Optional.ofNullable(getClassifier());
    }

    /**
     * Gets classifier in this context.
     *
     * @return the classifier in this context
     */
    public Classifier getClassifier() {

        return classifier;
    }

    protected boolean isClassifierSet() {return classifier != null;}

    protected void setClassifier(final Classifier classifier) {

        this.classifier = classifier;
    }

    public Optional<Metrics> getClassificationMetricsOptional() {

        return Optional.ofNullable(getClassificationMetrics());
    }

    /**
     * Gets the classification metrics of this context.
     *
     * @return the classification metrics of this context, or {@code null}
     */
    public Metrics getClassificationMetrics() {

        return classification_metrics;
    }

    /**
     * Sets the classification metrics of this context.
     *
     * @param classification_metrics the classification metrics to set
     */
    public void setClassificationMetrics(final Metrics classification_metrics) {

        this.classification_metrics = classification_metrics;
    }

    public Optional<ConfusionMatrix> getConfusionMatrixOptional() {

        return Optional.ofNullable(getConfusionMatrix());
    }

    /**
     * Gets the confusion matrix of this context.
     *
     * @return the confusion matrix of this context, or {@code null} if no confusion matrix is set
     */
    public ConfusionMatrix getConfusionMatrix() {

        return confusion_matrix;
    }

    /**
     * Sets the confusion matrix of this context.
     *
     * @param confusion_matrix the confusion matrix to set
     */
    public void setConfusionMatrix(ConfusionMatrix confusion_matrix) {

        this.confusion_matrix = confusion_matrix;
    }

    protected boolean isConfusionMatrixSet() {

        return confusion_matrix != null;
    }

    protected boolean isClassificationMetricsSet() {

        return classification_metrics != null;
    }

    /**
     * Gets the time it took to train the classifier in this context.
     *
     * @return the time it took to train the classifier in this context, or {@code null} if the
     * classifier is not trained
     */
    public Duration getTrainingTime() {

        return training_time;
    }

    /**
     * Sets the time it took to train the classifier in this context.
     *
     * @param training_time the time it took to train the classifier in this context.
     */
    public void setTrainingTime(Duration training_time) {

        this.training_time = training_time;
    }

    /**
     * Gets the time it took to classify the evaluation records by the classifier in this context.
     *
     * @return the time it took to classify the evaluation records by the classifier in this
     * context, or {@code null} if the classifier is not evaluated
     */
    public Duration getEvaluationClassificationTime() {

        return evaluation_classification_time;
    }

    /**
     * Sets the time it took to classify the evaluation records by the classifier in this context.
     *
     * @param evaluation_classification_time the time it took to classify the evaluation records by
     * the classifier in this context
     */
    public void setEvaluationClassificationTime(Duration evaluation_classification_time) {

        this.evaluation_classification_time = evaluation_classification_time;
    }
}
