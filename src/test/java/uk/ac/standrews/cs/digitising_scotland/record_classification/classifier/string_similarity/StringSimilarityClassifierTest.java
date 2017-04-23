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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.Classifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.ClassifierSupplier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.ClassifierTest;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.SingleClassifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;

import java.util.*;
import java.util.function.Supplier;
import java.util.logging.*;

import static org.junit.Assert.assertEquals;

/***
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
@RunWith(Parameterized.class)
public class StringSimilarityClassifierTest extends ClassifierTest {

    private static final Map<String, String> SIMILARITY_MAP = new HashMap<>();

    static {
        SIMILARITY_MAP.put("trial", "trail");
        SIMILARITY_MAP.put("house", "mouse");
        SIMILARITY_MAP.put("thought", "through");
        SIMILARITY_MAP.put("quick brown fish", "quick brown fox");
        SIMILARITY_MAP.put("lazy dogs", "lazy dog");
        SIMILARITY_MAP.put("lazy \"dogs\"", "lazy dog");
    }

    private Bucket test_bucket;
    private Supplier<Classifier> factory;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> generateData() {

        final List<Object[]> result = new ArrayList<>();

        result.add(new Object[]{ClassifierSupplier.STRING_SIMILARITY_LEVENSHTEIN});
        result.add(new Object[]{ClassifierSupplier.STRING_SIMILARITY_JARO_WINKLER});
        result.add(new Object[]{ClassifierSupplier.STRING_SIMILARITY_JACCARD});
        result.add(new Object[]{ClassifierSupplier.STRING_SIMILARITY_DICE});

        return result;
    }

    public StringSimilarityClassifierTest(Supplier<Classifier> factory) {

        this.factory = factory;
        test_bucket = new Bucket(TEST_RECORDS);
    }

    @Test
    public void trainedClassifierReturnsCodeOfSimilarValueForIndividualRecord() {

        StringSimilarityClassifier classifier = newClassifier();

        classifier.trainModel(training_bucket);

        for (String value : TEST_VALUES) {

            assertClassifiedSimilarly(value, classifier.classify(value));
        }
    }

    @Test
    public void trainedClassifierReturnsCodeOfSimilarValueForBucket() {

        StringSimilarityClassifier classifier = newClassifier();

        classifier.trainModel(training_bucket);

        final Bucket classified_bucket = classifier.classify(test_bucket);

        for (String value : TEST_VALUES) {

            assertClassifiedSimilarly(value, getClassificationFromBucket(value, classified_bucket));
        }
    }

    private void assertClassifiedSimilarly(String test_value, Classification test_classification) {

        final String most_similar_data_in_training_set = SIMILARITY_MAP.get(test_value);
        final Classification classification_in_training_set = getClassificationFromBucket(most_similar_data_in_training_set, training_bucket);

        //noinspection ConstantConditions
        assertEquals(classification_in_training_set.getCode(), test_classification.getCode());
    }

    private Classification getClassificationFromBucket(String value, Bucket bucket) {

        for (Record record : bucket) {
            if (record.getData().equals(value)) {
                return record.getClassification();
            }
        }
        return null;
    }

    @Override
    protected StringSimilarityClassifier newClassifier() {

        return (StringSimilarityClassifier) factory.get();
    }
}
