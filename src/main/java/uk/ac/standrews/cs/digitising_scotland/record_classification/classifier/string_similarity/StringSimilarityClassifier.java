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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.string_similarity;

import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.SingleClassifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.TokenList;

import java.util.HashMap;
import java.util.Map;

/**
 * Classifies records based on the string similarity of the training data to unseen data.
 * This class is not thread-safe.
 *
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
public class StringSimilarityClassifier extends SingleClassifier {

    private static final long serialVersionUID = -6159276459112698341L;

    private SimilarityMetric similarity_metric;
    private Map<String, Classification> known_classifications;

    /**
     * @param similarity_metric the metric by which to calculate similarity between training and unseen data
     */
    public StringSimilarityClassifier(SimilarityMetric similarity_metric) {

        this.similarity_metric = similarity_metric;
        clearModel();
    }

    /**
     * Needed for JSON deserialization.
     */
    public StringSimilarityClassifier() {

    }

    @Override
    public void clearModel() {

        known_classifications = new HashMap<>();
    }

    @Override
    public void trainModel(Bucket training_records) {

        final int training_records_size = training_records.size();
        resetTrainingProgressIndicator(training_records_size);

        for (Record record : training_records) {
            Classification classification = record.getClassification();
            known_classifications.put(record.getData(), new Classification(classification.getCode(), classification.getTokenList(), 0.0, classification.getDetail()));
            progressTrainingStep();
        }
    }

    @Override
    public Classification doClassify(final String data) {

        float highest_similarity_found = -1;
        Classification classification = null;

        for (Map.Entry<String, Classification> known_entry : known_classifications.entrySet()) {

            final float known_to_data_similarity = similarity_metric.getSimilarity(known_entry.getKey(), data);
            if (known_to_data_similarity > highest_similarity_found) {
                classification = known_entry.getValue();
                highest_similarity_found = known_to_data_similarity;
            }
        }
        return classification == null ? Classification.UNCLASSIFIED : new Classification(classification.getCode(), new TokenList(data), classification.getConfidence(), classification.getDetail());
    }

    @Override
    public String getName() {

        return getClass().getSimpleName() + "[" + similarity_metric.getName() + "]";
    }

    @Override
    public String getDescription() {

        return "Classifies based on similarity of the string to the training data, using " + similarity_metric.getDescription() + " similarity metric";
    }

    public String toString() {

        return getName();
    }

    public Map<String, Classification> readState() {

        return known_classifications;
    }

    public void writeState(Map<String, Classification> known_classifications) {

        this.known_classifications = known_classifications;
    }
}
