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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning;

import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.TokenList;

import java.util.*;

/**
 * Provides cleaners that deal with inconsistent classification within a bucket.
 *
 * @author Graham Kirby
 */
public enum ConsistentClassificationCleaner implements Cleaner {

    /**
     * Finds any sets of records containing the same data but different classifications,
     * and returns a new bucket omitting all such records.
     */
    REMOVE {
        @Override
        public List<Bucket> apply(List<Bucket> buckets) {

            final Set<String> inconsistently_classified_data = getInconsistentlyClassifiedData(buckets);
            final List<Bucket> cleaned_buckets = new ArrayList<>();

            for (Bucket bucket : buckets) {

                final Bucket cleaned_bucket = new Bucket();
                for (Record record : bucket) {

                    final String data = record.getData();

                    if (!inconsistently_classified_data.contains(data)) {
                        cleaned_bucket.add(record);
                    }
                }
                cleaned_buckets.add(cleaned_bucket);
            }

            return cleaned_buckets;
        }
    },

    /**
     * Finds any sets of records containing the same data but different classifications,
     * and returns a new bucket in which all such records are reclassified to the most
     * popular classification.
     */
    CORRECT {
        @Override
        public List<Bucket> apply(List<Bucket> buckets) {

            // A map from data string to a map containing all the different classifications for that
            // data, and their frequencies.
            Map<String, Map<String, Integer>> alternative_classifications = getAlternativeClassificationsForDataStrings(buckets);

            List<Bucket> cleaned_buckets = new ArrayList<>();
            for (Bucket bucket : buckets) {
                Bucket cleaned_bucket = new Bucket();

                for (Record record : bucket) {

                    Map<String, Integer> alternative_classifications_for_record = alternative_classifications.get(record.getData());
                    String most_popular_classification = getMostPopularClassification(alternative_classifications_for_record);

                    if (record.getClassification().getCode().equals(most_popular_classification)) {
                        cleaned_bucket.add(record);
                    }
                    else {
                        cleaned_bucket.add(makeCorrectedRecord(record, most_popular_classification));
                    }
                }
                cleaned_buckets.add(cleaned_bucket);
            }

            return cleaned_buckets;
        }
    };

    protected static Set<String> getInconsistentlyClassifiedData(List<Bucket> buckets) {

        final Set<String> inconsistently_coded_data = new HashSet<>();
        final Map<String, String> classifications_encountered = new HashMap<>();

        for (Bucket bucket : buckets) {

            for (Record record : bucket) {

                final Classification classification = record.getClassification();

                if (classification != null) {
                    final String data = record.getData();
                    final String code = classification.getCode();

                    if (classifications_encountered.containsKey(data)) {
                        if (!code.equals(classifications_encountered.get(data))) {
                            inconsistently_coded_data.add(data);
                        }
                    }
                    else {
                        classifications_encountered.put(data, code);
                    }
                }
            }
        }
        return inconsistently_coded_data;
    }

    private static Map<String, Map<String, Integer>> getAlternativeClassificationsForDataStrings(List<Bucket> buckets) {

        final Map<String, Map<String, Integer>> alternative_classifications = new HashMap<>();

        for (Bucket bucket : buckets) {
            for (Record record : bucket) {

                final String data = record.getData();
                if (!alternative_classifications.containsKey(data)) {
                    alternative_classifications.put(data, new HashMap<>());
                }

                final Map<String, Integer> classifications_for_this_data = alternative_classifications.get(data);

                String code = record.getClassification().getCode();
                if (!classifications_for_this_data.containsKey(code)) {
                    classifications_for_this_data.put(code, 0);
                }

                classifications_for_this_data.put(code, classifications_for_this_data.get(code) + 1);
            }
        }

        return alternative_classifications;
    }

    private static String getMostPopularClassification(Map<String, Integer> alternative_classifications) {

        String most_popular = "";
        int highest_count = 0;

        for (Map.Entry<String, Integer> entry : alternative_classifications.entrySet()) {

            int count = entry.getValue();
            if (count > highest_count) {
                highest_count = count;
                most_popular = entry.getKey();
            }
        }

        return most_popular;
    }

    private static Record makeCorrectedRecord(Record record, String most_popular_code) {

        final String data = record.getData();
        final Classification classification = record.getClassification();

        return new Record(record.getId(), data, record.getOriginalData(), new Classification(most_popular_code, new TokenList(data), classification.getConfidence(), classification.getDetail()));
    }
}
