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
package uk.ac.standrews.cs.digitising_scotland.record_classification_cleaned.pipeline;

import uk.ac.standrews.cs.digitising_scotland.record_classification_cleaned.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClassificationMetrics {

    // http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-text-classification-1.html#17469

    private ConfusionMatrix confusion_matrix;

    public ClassificationMetrics(ConfusionMatrix confusion_matrix) {

        this.confusion_matrix = confusion_matrix;
    }

    public void printMetrics() throws InvalidCodeException, InconsistentCodingException, UnknownDataException, UnclassifiedGoldStandardRecordException {

        printMetrics("precision", getPerClassPrecision());
        printMetrics("recall", getPerClassRecall());
        printMetrics("accuracy", getPerClassAccuracy());
        printMetrics("F1", getPerClassF1());

        printMetric("macro-average precision", getMacroAveragePrecision());
        printMetric("micro-average precision", getMicroAveragePrecision());
        printMetric("macro-average recall", getMacroAverageRecall());
        printMetric("micro-average recall", getMicroAverageRecall());
        printMetric("macro-average accuracy", getMacroAverageAccuracy());
        printMetric("micro-average accuracy", getMicroAverageAccuracy());
        printMetric("macro-average F1", getMacroAverageF1());
        printMetric("micro-average F1", getMicroAverageF1());
    }

    private void printMetrics(String label, Map<String, Double> metrics) {

        System.out.println(label + ":");

        for (Map.Entry<String, Double> entry : metrics.entrySet()) {
            printMetric("code: " + entry.getKey(), entry.getValue());
        }
    }

    private void printMetric(String label, double metric) {

        System.out.println(label + " value: " + metric);
    }

    public Map<String, Double> getPerClassPrecision() {

        final Map<String, Double> precision_map = new HashMap<>();

        Map<String, Integer> true_positive_counts = confusion_matrix.getTruePositiveCounts();
        Map<String, Integer> false_positive_counts = confusion_matrix.getFalsePositiveCounts();

        for (String code : getCodes()) {

            precision_map.put(code, calculatePrecision(true_positive_counts.get(code), false_positive_counts.get(code)));
        }

        return precision_map;
    }

    public Map<String, Double> getPerClassRecall() {

        final Map<String, Double> recall_map = new HashMap<>();

        Map<String, Integer> true_positive_counts = confusion_matrix.getTruePositiveCounts();
        Map<String, Integer> false_negative_counts = confusion_matrix.getFalseNegativeCounts();

        for (String code : getCodes()) {

            recall_map.put(code, calculateRecall(true_positive_counts.get(code), false_negative_counts.get(code)));
        }

        return recall_map;
    }

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

    public double getMacroAveragePrecision() {

        return getMacroAverage(getPerClassPrecision());
    }

    public double getMacroAverageRecall() {

        return getMacroAverage(getPerClassRecall());
    }

    public double getMacroAverageAccuracy() {

        return getMacroAverage(getPerClassAccuracy());
    }

    public double getMacroAverageF1() {

        return getMacroAverage(getPerClassF1());
    }

    public double getMicroAveragePrecision() {

        // Micro average is total TP / (total TP + total FP).
        int total_true_positives = getSumOfTruePositives();
        int total_false_positives = getSumOfFalsePositives();

        return calculatePrecision(total_true_positives, total_false_positives);
    }

    public double getMicroAverageRecall() {

        // Micro average is total TP / (total TP + total FN).
        int total_true_positives = getSumOfTruePositives();
        int total_false_negatives = getSumOfFalseNegatives();

        return calculateRecall(total_true_positives, total_false_negatives);
    }

    public double getMicroAverageAccuracy() {

        // Micro average is (total TP + total TN) / (total TP + total TN + total FP + total FN).
        int total_true_positives = getSumOfTruePositives();
        int total_true_negatives = getSumOfTrueNegatives();
        int total_false_positives = getSumOfFalsePositives();
        int total_false_negatives = getSumOfFalseNegatives();

        return calculateAccuracy(total_true_positives, total_true_negatives, total_false_positives, total_false_negatives);
    }

    public double getMicroAverageF1() {

        // Micro average is (2 * total TP) / (2 * total TP + total FP + total FN).
        int total_true_positives = getSumOfTruePositives();
        int total_false_positives = getSumOfFalsePositives();
        int total_false_negatives = getSumOfFalseNegatives();

        return calculateF1(total_true_positives, total_false_positives, total_false_negatives);
    }

    private double getMacroAverage(Map<String, Double> values) {

        // Macro average is mean of the individual per-class values.
        double total = 0;

        for (double value : values.values()) {
            total += value;
        }

        return total / values.size();
    }

    private int getSumOfTruePositives() {

        return sum(confusion_matrix.getTruePositiveCounts());
    }

    private int getSumOfTrueNegatives() {

        return sum(confusion_matrix.getTrueNegativeCounts());
    }

    private int getSumOfFalsePositives() {

        return sum(confusion_matrix.getFalsePositiveCounts());
    }

    private int getSumOfFalseNegatives() {

        return sum(confusion_matrix.getFalseNegativeCounts());
    }

    private int sum(Map<String, Integer> counts) {

        int total = 0;
        for (int count : counts.values()) total += count;
        return total;
    }

    private Set<String> getCodes() {

        return confusion_matrix.getClassificationCounts().keySet();
    }

    private double calculatePrecision(int true_positives, int false_positives) {

        // Precision is TP / (TP + FP).
        // Interpret precision as 1 if there are no positives.

        int total_positives = true_positives + false_positives;
        return total_positives > 0 ? (double) true_positives / total_positives : 1.0;
    }

    private double calculateRecall(int true_positives, int false_negatives) {

        // Interpret recall as 1 if there are no cases.

        int total_cases_of_this_class = true_positives + false_negatives;
        return total_cases_of_this_class > 0 ? (double) true_positives / total_cases_of_this_class : 1.0;
    }

    private double calculateAccuracy(int true_positives, int true_negatives, int false_positives, int false_negatives) {

        int total_cases = true_positives + true_negatives + false_positives + false_negatives;
        return (double) (true_positives + true_negatives) / total_cases;
    }

    private double calculateF1(int true_positives, int false_positives, int false_negatives) {

        // Interpret F1 as 1 if there are no cases.

        int denominator = true_positives * 2 + false_positives + false_negatives;
        return denominator > 0 ? (double) (true_positives * 2) / denominator : 1.0;
    }
}
