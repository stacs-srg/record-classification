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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.ensemble;

import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.Classifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.SingleClassifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;

import java.io.Serializable;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;

/**
 * Classifies data with the aid of a collection of classifiers and chooses between classifications using a given {@link ResolutionStrategy}.
 *
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
public class EnsembleClassifier extends Classifier {

    private static final long serialVersionUID = 6432371860423757296L;
    private static final Logger LOGGER = Logger.getLogger(EnsembleClassifier.class.getName());

    private List<SingleClassifier> classifiers;
    private StringSimilarityGroupWithSharedState group;
    private ResolutionStrategy resolution_strategy;

    /**
     * Needed for JSON deserialization.
     */
    public EnsembleClassifier() {

    }

    /**
     * Instantiates a new ensemble classifier.
     *
     * @param classifiers the classifiers
     * @param resolution_strategy the strategy by which to decide a single classification between multiple classifications
     */
    public EnsembleClassifier(Collection<SingleClassifier> classifiers, ResolutionStrategy resolution_strategy) {

        this(classifiers, null, resolution_strategy);
    }

    public EnsembleClassifier(Collection<SingleClassifier> classifiers, StringSimilarityGroupWithSharedState group, ResolutionStrategy resolution_strategy) {

        this.classifiers = new ArrayList<>(classifiers);
        this.group = group;
        this.resolution_strategy = resolution_strategy;
    }

    @Override
    public void trainAndEvaluate(final Bucket bucket, double internal_training_ratio, final Random random) {

        final int classifiers_count = classifiers.size();
        resetTrainingProgressIndicator(classifiers_count);

        LOGGER.info(() -> String.format("training %d classifiers in the %s...", classifiers_count, getName()));

        classifiers.parallelStream().forEach(classifier -> {
            classifier.trainAndEvaluate(bucket, internal_training_ratio, random);
            progressTrainingStep();
        });

        if (group != null) {
            group.trainAll(bucket);
        }
        LOGGER.info(() -> String.format("done training the %s.", getName()));
    }

    @Override
    public Classification classify(String data) {

        final Collection<SingleClassifier> all_classifiers = classifiers.stream().collect(Collectors.toList());

        if (group != null) {
            all_classifiers.addAll(group.getClassifiers().stream().collect(Collectors.toList()));
        }

        final Map<SingleClassifier, Classification> candidate_classifications = new HashMap<>();

        for (SingleClassifier classifier : all_classifiers) {
            final Classification classification = classifier.classify(data);
            candidate_classifications.put(classifier, classification);
        }

        return resolution_strategy.resolve(candidate_classifications);
    }

    @Override
    public String getName() {

        return getClass().getSimpleName() + "[" + concatenateClassifierNames() + "]";
    }

    public String toString() {

        return getName();
    }

    private String concatenateClassifierNames() {

        String result = "";
        for (Classifier classifier : classifiers) {
            if (result.length() > 0)
                result += ",";
            result += classifier.getName();
        }
        if (group != null) {
            for (Classifier classifier : group.getClassifiers()) {
                if (result.length() > 0)
                    result += ",";
                result += classifier.getName();
            }
        }
        return result;
    }

    @Override
    public String getDescription() {

        return "Classifies using a collection of classifiers";
    }

    @Override
    public boolean equals(final Object other) {

        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        final EnsembleClassifier that = (EnsembleClassifier) other;
        return Objects.equals(classifiers, that.classifiers) && Objects.equals(resolution_strategy, that.resolution_strategy);
    }

    @Override
    public int hashCode() {

        return Objects.hash(classifiers, resolution_strategy);
    }

    /**
     * Captures the strategy by which to resolve a single classification from multiple classifications.
     */
    public interface ResolutionStrategy extends Serializable {

        /**
         * Resolves a single classification from classifications produced by different classifiers.
         *
         * @param candidate_classifications the candidate classifications
         * @return a single classification.
         */
        Classification resolve(Map<SingleClassifier, Classification> candidate_classifications);
    }
}
