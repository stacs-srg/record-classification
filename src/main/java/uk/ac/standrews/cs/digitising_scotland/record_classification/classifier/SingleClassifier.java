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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier;

import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.Metrics;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.StrictConfusionMatrix;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.util.Validators;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.utilities.tables.ConfidenceIntervals;
import uk.ac.standrews.cs.utilities.tables.Means;

import java.util.*;
import java.util.logging.Logger;

public abstract class SingleClassifier extends Classifier {

    private static final long serialVersionUID = -5145594329971828792L;
    private static final Logger LOGGER = Logger.getLogger(SingleClassifier.class.getName());

    private static final int INTERNAL_EVALUATION_REPETITIONS = 3;
    private Map<String, Double> confidence_map = new HashMap<>();

    public final void trainAndEvaluate(final Bucket bucket, final double internal_training_ratio, final Random random) {

        final List<Map<String, Double>> confidence_maps = new ArrayList<>();

        LOGGER.info(() -> String.format("training and internally evaluating the %s with internal training ratio of %.2f...", getName(), internal_training_ratio));

        final int total_repetitions = Math.abs(1.0 - internal_training_ratio) <= Validators.DELTA ? 1 : INTERNAL_EVALUATION_REPETITIONS;

        for (int repetition = 0; repetition < total_repetitions; repetition++) {

            // On the last iteration, this leaves the model trained.
            final int natural_repetition_count = repetition + 1;

            LOGGER.info(() -> String.format("performing repetition %d of %d...", natural_repetition_count, total_repetitions));
            confidence_maps.add(singleTrainAndEvaluate(bucket, internal_training_ratio, random));
            LOGGER.info(() -> String.format("finished repetition %d of %d.", natural_repetition_count, total_repetitions));
        }
        LOGGER.info(() -> String.format("done training and internally evaluating the %s.", getName()));
        confidence_map = lowerConfidenceIntervalBoundaries(confidence_maps);
    }

    public final Classification classify(String data) {

        Classification classification = doClassify(data);
        setConfidence(classification, true);
        return classification;
    }

    protected abstract Classification doClassify(String data);

    public Bucket classify(final Bucket bucket) {

        return classify(bucket, true);
    }

    protected void setConfidence(Classification classification, boolean set_confidence) {

        if (set_confidence)
            classification.setConfidence(getConfidence(classification.getCode()));
    }

    protected double getConfidence(String code) {

        if (confidence_map.containsKey(code)) {

            double confidence = confidence_map.get(code);
            return (Double.isNaN(confidence)) ? 0.0 : confidence;

        }
        else {
            return 0.0;
        }
    }

    private Map<String, Double> lowerConfidenceIntervalBoundaries(List<Map<String, Double>> confidence_maps) {

        // NB two distinct uses of 'confidence' here...

        Map<String, Double> confidence_map = new HashMap<>();
        Set<String> all_classes = new HashSet<>();

        for (Map<String, Double> map : confidence_maps) {
            all_classes.addAll(map.keySet());
        }

        for (String class_name : all_classes) {

            List<Double> confidence_values_for_class = findConfidenceValuesForClass(class_name, confidence_maps);
            confidence_map.put(class_name, calculateLowerConfidenceIntervalBoundary(confidence_values_for_class));
        }

        return confidence_map;
    }

    private List<Double> findConfidenceValuesForClass(String class_name, List<Map<String, Double>> confidence_maps) {

        List<Double> values = new ArrayList<>();

        for (Map<String, Double> map : confidence_maps) {
            values.add(map.containsKey(class_name) ? map.get(class_name) : 0.0);
        }

        return values;
    }

    private Double calculateLowerConfidenceIntervalBoundary(List<Double> values) {

        return values.size() == 1 ? values.get(0) : Math.max(0.0, Means.calculateMean(values) - ConfidenceIntervals.calculateConfidenceInterval(values));
    }

    private Map<String, Double> singleTrainAndEvaluate(Bucket bucket, double internal_training_ratio, Random random) {

        final Bucket real_training_records = bucket.randomSubset(random, internal_training_ratio);
        final Bucket internal_evaluation_records = bucket.difference(real_training_records);

        clearModel();

        LOGGER.info(() -> String.format("training the %s on %d records...", getName(), real_training_records.size()));
        trainModel(real_training_records);
        LOGGER.info(() -> String.format("done training the %s.", getName()));

        LOGGER.info(() -> String.format("internally evaluating the %s on %d records...", getName(), internal_evaluation_records.size()));
        final Metrics classification_metrics = evaluate(bucket, internal_evaluation_records);
        LOGGER.info(() -> String.format("done internally evaluating the %s.", getName()));

        return classification_metrics.getPerClassF1();
    }

    private Metrics evaluate(final Bucket bucket, final Bucket internal_evaluation_records) {

        final Bucket classified_records = classify(internal_evaluation_records, false);
        final StrictConfusionMatrix confusion_matrix = new StrictConfusionMatrix(classified_records, bucket);
        return new Metrics(confusion_matrix);
    }

    public abstract void trainModel(final Bucket bucket);

    protected abstract void clearModel();

    @Override
    public String toString() {

        return getName();
    }

    @Override
    public String getName() {

        return getClass().getSimpleName();
    }
}
