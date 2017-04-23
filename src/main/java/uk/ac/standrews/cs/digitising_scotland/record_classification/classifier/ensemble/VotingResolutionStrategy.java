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

import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.SingleClassifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.utilities.Formatting;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VotingResolutionStrategy implements EnsembleClassifier.ResolutionStrategy, Serializable {

    /**
     * Needed for JSON deserialization.
     */
    public VotingResolutionStrategy() {
    }

    @Override
    public Classification resolve(Map<SingleClassifier, Classification> candidate_classifications) {

        Set<Set<Classification>> classifications_with_same_codes = partitionClassificationsWithSameCodes(candidate_classifications);

        Set<Set<Classification>> classifications_with_most_popular_code = getClassificationsWithMostPopularCode(classifications_with_same_codes);

        Map<Set<Classification>, Double> confidence_averages = getConfidenceAverages(classifications_with_most_popular_code);

        String detail = constructDetailString(candidate_classifications);

        return classificationWithHighestConfidenceAverage(confidence_averages, detail);
    }

    private Set<Set<Classification>> partitionClassificationsWithSameCodes(Map<SingleClassifier, Classification> candidate_classifications) {

        Set<Set<Classification>> classification_sets = new HashSet<>();

        for (Classification classification : candidate_classifications.values()) {

            if (!classification.isUnclassified()) {

                Set<Classification> other_classifications_with_this_code = findOtherClassificationsWithThisCode(classification_sets, classification.getCode());

                if (other_classifications_with_this_code == null) {
                    other_classifications_with_this_code = new HashSet<>();
                    classification_sets.add(other_classifications_with_this_code);
                }

                other_classifications_with_this_code.add(classification);
            }
        }

        return classification_sets;
    }

    private Set<Classification> findOtherClassificationsWithThisCode(Set<Set<Classification>> classification_sets, String code) {

        for (Set<Classification> other_classifications : classification_sets) {

            for (Classification classification : other_classifications) {

                if (classification.getCode().equals(code)) {
                    return other_classifications;
                }
            }
        }
        return null;
    }

    private Set<Set<Classification>> getClassificationsWithMostPopularCode(Set<Set<Classification>> classifications_with_same_codes) {

        int max_size = 0;

        for (Set<Classification> classifications : classifications_with_same_codes) {

            if (classifications.size() > max_size) {
                max_size = classifications.size();
            }
        }

        Set<Set<Classification>> classifications_with_popular_code = new HashSet<>();

        for (Set<Classification> classifications : classifications_with_same_codes) {

            if (classifications.size() == max_size) {
                classifications_with_popular_code.add(classifications);
            }
        }

        return classifications_with_popular_code;
    }

    private Map<Set<Classification>, Double> getConfidenceAverages(Set<Set<Classification>> classifications_with_most_popular_code) {

        Map<Set<Classification>, Double> confidence_averages = new HashMap<>();

        for (Set<Classification> classifications : classifications_with_most_popular_code) {
            confidence_averages.put(classifications, combinedConfidence(classifications));
        }

        return confidence_averages;
    }

    private Double combinedConfidence(Set<Classification> classifications) {

        // If any of the individual confidence values is 1.0, then return 1.0 overall.
        // Assume that this will only occur with exact match classifier.

        double sum = 0.0;
        for (Classification classification : classifications) {

            final double confidence = classification.getConfidence();
            if (confidence > 0.999) return 1.0;
            sum += confidence;
        }
        return sum / classifications.size();
    }

    private String constructDetailString(Map<SingleClassifier, Classification> candidate_classifications) {

        StringBuilder result = new StringBuilder();

        for (Map.Entry<SingleClassifier, Classification> entry : candidate_classifications.entrySet()) {

            if (result.length() > 0) result.append("\t");

            result.append(entry.getKey().getName());
            result.append("\t");
            result.append(entry.getValue().getCode());
            result.append("\t");
            result.append(Formatting.format(entry.getValue().getConfidence(),2));
        }

        return result.toString();
    }

    private Classification classificationWithHighestConfidenceAverage(Map<Set<Classification>, Double> confidence_averages, String detail) {

        double highest_confidence = 0.0;
        Set<Classification> classifications = null;

        for (Map.Entry<Set<Classification>, Double> entry : confidence_averages.entrySet()) {

            if (entry.getValue() >= highest_confidence) {
                highest_confidence = entry.getValue();
                classifications = entry.getKey();
            }
        }

        if (classifications != null) {
            //noinspection LoopStatementThatDoesntLoop
            for (Classification classification : classifications) {
                return classification.makeClone(combinedConfidence(classifications), detail);
            }
        }
        return Classification.UNCLASSIFIED;
    }
}
