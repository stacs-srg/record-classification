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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier;

import uk.ac.shef.wit.simmetrics.similaritymetrics.InterfaceStringMetric;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;

import java.util.Map;
import java.util.Objects;

/**
 * Classifies records based on the {@link InterfaceStringMetric similarity} of the training data to unseen data.
 * This class is not thread-safe.
 *
 * @author masih
 */
public class StringSimilarityClassifier extends ExactMatchClassifier {

    private final InterfaceStringMetric similarity_metric;

    /**
     * Constructs a new instance of String similarity classifier.
     *
     * @param similarity_metric the metric by which to calculate similarity between training and unseen data
     * @throws NullPointerException if the given similarity metric is {@code null}.
     */
    public StringSimilarityClassifier(InterfaceStringMetric similarity_metric) {

        Objects.requireNonNull(similarity_metric);
        this.similarity_metric = similarity_metric;
    }

    @Override
    public Classification classify(final String data) {
        
        float highest_similarity_found = 0;
        Classification classification = null;

        for (Map.Entry<String, Classification> known_entry : known_classifications.entrySet()) {

            final float known_to_data_similarity = similarity_metric.getSimilarity(known_entry.getKey(), data);
            if (known_to_data_similarity > highest_similarity_found) {
                classification = known_entry.getValue();
                highest_similarity_found = known_to_data_similarity;
            }
        }

        return classification;
    }
}