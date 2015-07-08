/*
 * Copyright 2015 Digitising Scotland project:
 * <http://digitisingscotland.cs.st-andrews.ac.uk/>
 *
 * This file is part of the module record_classification.
 *
 * record_classification is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * record_classification is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with record_classification. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.digitising_scotland.record_classification.analysis;

import org.apache.commons.csv.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;
import uk.ac.standrews.cs.util.dataset.*;

import java.io.*;
import java.time.*;
import java.util.*;

import static uk.ac.standrews.cs.util.tools.Formatting.*;

/**
 * Collection of metrics measuring the effectiveness of classification.
 *
 * @author Graham Kirby
 * @author Masih Hajiarab Derkani
 * @see {@code http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-text-classification-1.html#17469}
 */
public class ClassificationMetrics implements Serializable {

    /**
     * Labels used in conversion of metrics to {@link DataSet dataset}.
     */
    public static final List<String> DATASET_LABELS = Arrays.asList("macro-precision", "macro-recall", "macro-accuracy", "macro-F1", "micro-precision", "micro-recall", "micro-accuracy", "micro-F1", "training time (m)", "evaluation classification time (m)");
    public static final List<Boolean> COLUMNS_AS_PERCENTAGES = Arrays.asList(true, true, true, true, true, true, true, true, false, false);

    private static final long serialVersionUID = -214187549269797059L;
    public static final double ONE_MINUTE_IN_SECONDS = 60.0;

    private final ConfusionMatrix confusion_matrix;
    private final Duration training_time;
    private final Duration evaluation_classification_time;

    /**
     * instantiates a new classification metrics from the given confusion matrix.
     *
     * @param confusion_matrix the matrix from which to instantiate a new classification metrics
     */
    public ClassificationMetrics(ConfusionMatrix confusion_matrix) {

        this(confusion_matrix, null, null);
    }

    /**
     * instantiates a new classification metrics from the given confusion matrix.
     *
     * @param confusion_matrix the matrix from which to instantiate a new classification metrics
     */
    public ClassificationMetrics(ConfusionMatrix confusion_matrix, final Duration training_time, final Duration evaluation_classification_time) {

        this.confusion_matrix = confusion_matrix;
        this.training_time = training_time;
        this.evaluation_classification_time = evaluation_classification_time;
    }

    public DataSet toDataSet() {

        final DataSet dataset = new DataSet(DATASET_LABELS);
        final List<String> row = toDataSetRow(this);
        dataset.addRow(row);

        return dataset;
    }

    public List<String> getValues() {

        return toDataSetRow(this);
    }

    /**
     * Converts a collection of metrics into {@link DataSet dataset} with labels set to {@link #DATASET_LABELS}.
     *
     * @param metricses the collection of metrics to convert to dataset
     * @return the metrics as dataset.
     */
    public static DataSet toDataSet(final Collection<ClassificationMetrics> metricses) {

        final DataSet dataset = new DataSet(DATASET_LABELS);
        populateDataSet(metricses, dataset);
        return dataset;
    }

    private static void populateDataSet(final Collection<ClassificationMetrics> metricses, final DataSet dataset) {

        for (ClassificationMetrics metrics : metricses) {

            final List<String> row = toDataSetRow(metrics);
            dataset.addRow(row);
        }
    }

    private static List<String> toDataSetRow(final ClassificationMetrics metrics) {

        final String macro_precision = String.valueOf(metrics.getMacroAveragePrecision());
        final String macro_recall = String.valueOf(metrics.getMacroAverageRecall());
        final String macro_accuracy = String.valueOf(metrics.getMacroAverageAccuracy());
        final String macro_f1 = String.valueOf(metrics.getMacroAverageF1());
        final String micro_precision = String.valueOf(metrics.getMicroAveragePrecision());
        final String micro_recall = String.valueOf(metrics.getMicroAverageRecall());
        final String micro_accuracy = String.valueOf(metrics.getMicroAverageAccuracy());
        final String micro_f1 = String.valueOf(metrics.getMicroAverageF1());
        final String training_time = String.valueOf(metrics.getTrainingTime().getSeconds() / ONE_MINUTE_IN_SECONDS);
        final String evaluation_classification_time = String.valueOf(metrics.getEvaluationClassificationTime().getSeconds() / ONE_MINUTE_IN_SECONDS);

        return Arrays.asList(macro_precision, macro_recall, macro_accuracy, macro_f1, micro_precision, micro_recall, micro_accuracy, micro_f1, training_time, evaluation_classification_time);
    }

    /**
     * Converts a collection of metrics into {@link DataSet dataset} with labels set to {@link #DATASET_LABELS}.
     *
     * @param metricses the collection of metrics to convert to dataset
     * @param format    the format of the dataset
     * @return the metrics as dataset.
     */
    public static DataSet toDataSet(final Collection<ClassificationMetrics> metricses, CSVFormat format) {

        final DataSet dataset = new DataSet(DATASET_LABELS);
        dataset.setOutputFormat(format);
        populateDataSet(metricses, dataset);
        return dataset;
    }

    private void printMetrics(String label, Map<String, Double> metrics) {

        System.out.println("\n" + label + ":");

        for (Map.Entry<String, Double> entry : metrics.entrySet()) {
            printMetric(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Returns a map from classification class to precision.
     *
     * @return the map
     */
    public Map<String, Double> getPerClassPrecision() {

        final Map<String, Double> precision_map = new HashMap<>();

        Map<String, Integer> true_positive_counts = confusion_matrix.getTruePositiveCounts();
        Map<String, Integer> false_positive_counts = confusion_matrix.getFalsePositiveCounts();

        for (String code : getCodes()) {

            precision_map.put(code, calculatePrecision(true_positive_counts.get(code), false_positive_counts.get(code)));
        }

        return precision_map;
    }

    /**
     * Returns a map from classification class to recall.
     *
     * @return the map
     */
    public Map<String, Double> getPerClassRecall() {

        final Map<String, Double> recall_map = new HashMap<>();

        Map<String, Integer> true_positive_counts = confusion_matrix.getTruePositiveCounts();
        Map<String, Integer> false_negative_counts = confusion_matrix.getFalseNegativeCounts();

        for (String code : getCodes()) {

            recall_map.put(code, calculateRecall(true_positive_counts.get(code), false_negative_counts.get(code)));
        }

        return recall_map;
    }

    /**
     * Returns a map from classification class to accuracy.
     *
     * @return the map
     */
    public Map<String, Double> getPerClassAccuracy() {

        final Map<String, Double> accuracy_map = new HashMap<>();

        Map<String, Integer> true_positive_counts = confusion_matrix.getTruePositiveCounts();
        Map<String, Integer> true_negative_counts = confusion_matrix.getTrueNegativeCounts();
        Map<String, Integer> false_positive_counts = confusion_matrix.getFalsePositiveCounts();
        Map<String, Integer> false_negative_counts = confusion_matrix.getFalseNegativeCounts();

        for (String code : getCodes()) {

            accuracy_map.put(code, calculateAccuracy(true_positive_counts.get(code), true_negative_counts.get(code), false_positive_counts.get(code), false_negative_counts.get(code)));
        }

        return accuracy_map;
    }

    /**
     * Returns a map from classification class to F1 measure.
     *
     * @return the map
     */
    public Map<String, Double> getPerClassF1() {

        final Map<String, Double> f1_map = new HashMap<>();

        Map<String, Integer> true_positive_counts = confusion_matrix.getTruePositiveCounts();
        Map<String, Integer> false_positive_counts = confusion_matrix.getFalsePositiveCounts();
        Map<String, Integer> false_negative_counts = confusion_matrix.getFalseNegativeCounts();

        for (String code : getCodes()) {

            f1_map.put(code, calculateF1(true_positive_counts.get(code), false_positive_counts.get(code), false_negative_counts.get(code)));
        }

        return f1_map;
    }

    /**
     * Returns the macro-averaged precision over all classes.
     *
     * @return the macro-averaged precision
     */
    public double getMacroAveragePrecision() {

        return getMacroAverage(getPerClassPrecision());
    }

    /**
     * Returns the macro-averaged recall over all classes.
     *
     * @return the macro-averaged recall
     */
    public double getMacroAverageRecall() {

        return getMacroAverage(getPerClassRecall());
    }

    /**
     * Returns the macro-averaged accuracy over all classes.
     *
     * @return the macro-averaged accuracy
     */
    public double getMacroAverageAccuracy() {

        return getMacroAverage(getPerClassAccuracy());
    }

    /**
     * Returns the macro-averaged F1 measure over all classes.
     *
     * @return the macro-averaged F1 measure
     */
    public double getMacroAverageF1() {

        return getMacroAverage(getPerClassF1());
    }

    /**
     * Returns the micro-averaged precision over all classes.
     *
     * @return the micro-averaged precision
     */
    public double getMicroAveragePrecision() {

        // Micro average is total TP / (total TP + total FP).
        int total_true_positives = confusion_matrix.getNumberOfTruePositives();
        int total_false_positives = confusion_matrix.getNumberOfFalsePositives();

        return calculatePrecision(total_true_positives, total_false_positives);
    }

    /**
     * Returns the micro-averaged recall over all classes.
     *
     * @return the micro-averaged recall
     */
    public double getMicroAverageRecall() {

        // Micro average is total TP / (total TP + total FN).
        int total_true_positives = confusion_matrix.getNumberOfTruePositives();
        int total_false_negatives = confusion_matrix.getNumberOfFalseNegatives();

        return calculateRecall(total_true_positives, total_false_negatives);
    }

    /**
     * Returns the micro-averaged accuracy over all classes.
     *
     * @return the micro-averaged accuracy
     */
    public double getMicroAverageAccuracy() {

        // Micro average is (total TP + total TN) / (total TP + total TN + total FP + total FN).
        int total_true_positives = confusion_matrix.getNumberOfTruePositives();
        int total_true_negatives = confusion_matrix.getNumberOfTrueNegatives();
        int total_false_positives = confusion_matrix.getNumberOfFalsePositives();
        int total_false_negatives = confusion_matrix.getNumberOfFalseNegatives();

        return calculateAccuracy(total_true_positives, total_true_negatives, total_false_positives, total_false_negatives);
    }

    /**
     * Returns the micro-averaged F1 measure over all classes.
     *
     * @return the micro-averaged F1 measure
     */
    public double getMicroAverageF1() {

        // Micro average is (2 * total TP) / (2 * total TP + total FP + total FN).
        int total_true_positives = confusion_matrix.getNumberOfTruePositives();
        int total_false_positives = confusion_matrix.getNumberOfFalsePositives();
        int total_false_negatives = confusion_matrix.getNumberOfFalseNegatives();

        return calculateF1(total_true_positives, total_false_positives, total_false_negatives);
    }

    /**
     * Gets the time it took to train the classifier.
     *
     * @return the time it took to train the classifier, or {@code null} if the classifier is not specified
     */
    public Duration getTrainingTime() {

        return training_time;
    }

    /**
     * Gets the time it took to classify the evaluation records by the classifier.
     *
     * @return the time it took to classify the evaluation records by the classifier, or {@code null} if the classifier is not specified
     */
    public Duration getEvaluationClassificationTime() {

        return evaluation_classification_time;
    }

    /**
     * Prints out the metrics at a specified level of detail.
     *
     * @param info_level the detail level
     */
    public void printMetrics(InfoLevel info_level) {

        if (info_level == InfoLevel.VERBOSE) {

            printMetric("total TPs", confusion_matrix.getNumberOfTruePositives());
            printMetric("total FPs", confusion_matrix.getNumberOfFalsePositives());
            printMetric("total TNs", confusion_matrix.getNumberOfTrueNegatives());
            printMetric("total FNs", confusion_matrix.getNumberOfFalseNegatives());
            System.out.println();

            printMetrics("precision", getPerClassPrecision());
            printMetrics("recall", getPerClassRecall());
            printMetrics("accuracy", getPerClassAccuracy());
            printMetrics("F1", getPerClassF1());
            System.out.println();
        }

        if (info_level != InfoLevel.NONE) {

            printMetric("macro-average precision    ", getMacroAveragePrecision());
            printMetric("micro-average precision    ", getMicroAveragePrecision());
            printMetric("macro-average recall       ", getMacroAverageRecall());
            printMetric("micro-average recall       ", getMicroAverageRecall());
            printMetric("macro-average accuracy     ", getMacroAverageAccuracy());
            printMetric("micro-average accuracy     ", getMicroAverageAccuracy());
            printMetric("macro-average F1           ", getMacroAverageF1());
            printMetric("micro-average F1           ", getMicroAverageF1());
        }
    }

    private double getMacroAverage(Map<String, Double> values) {

        // Macro average is mean of the individual per-class values.
        double total = 0;
        int count = 0;

        for (Map.Entry<String, Double> entry : values.entrySet()) {

            String code = entry.getKey();
            Double value = entry.getValue();

            if (!value.isNaN() && !code.equals(Classification.UNCLASSIFIED.getCode())) {
                total += value;
                count++;
            }
        }

        return total / count;
    }

    private Set<String> getCodes() {

        return confusion_matrix.getClassificationCounts().keySet();
    }

    private double calculatePrecision(int true_positives, int false_positives) {

        // Precision is TP / (TP + FP).

        return (double) true_positives / (true_positives + false_positives);
    }

    private double calculateRecall(int true_positives, int false_negatives) {

        // Interpret recall as 1 if there are no cases.

        return (double) true_positives / (true_positives + false_negatives);
    }

    private double calculateAccuracy(int true_positives, int true_negatives, int false_positives, int false_negatives) {

        int total_cases = true_positives + true_negatives + false_positives + false_negatives;
        return (double) (true_positives + true_negatives) / total_cases;
    }

    private double calculateF1(int true_positives, int false_positives, int false_negatives) {

        return (double) (true_positives * 2) / (true_positives * 2 + false_positives + false_negatives);
    }
}
