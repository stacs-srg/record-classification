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
package uk.ac.standrews.cs.digitising_scotland.record_classification.analysis;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.utilities.FileManipulation;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class MultipleClassifierConfusionMatrixTest {

    private static final String CLASSIFIED_FILE_NAME = "example_classified.csv";
    private static final String GOLD_STANDARD_FILE_NAME = "example_gold_standard.csv";

    private static final double DELTA = 0.001;

    private StrictConfusionMatrix matrix;

    @Before
    public void setUp() throws Exception {

        try (final InputStream stream1 = FileManipulation.getInputStreamForResource(MultipleClassifierConfusionMatrixTest.class, CLASSIFIED_FILE_NAME);
             final InputStream stream2 = FileManipulation.getInputStreamForResource(MultipleClassifierConfusionMatrixTest.class, GOLD_STANDARD_FILE_NAME)) {

            final DataSet classified_records_csv = new DataSet(stream1, ',');
            final DataSet gold_standard_records_csv = new DataSet(stream2, ',');

            matrix = new StrictConfusionMatrix(classified_records_csv, gold_standard_records_csv);
        }
    }

    @Test
    public void perCodeClassificationsCountedCorrectly() {

        final Map<String, AtomicInteger> classification_counts = matrix.getClassificationCounts();

        assertCount(2, "fish", classification_counts);
        assertCount(2, "mammal", classification_counts);
        assertCount(2, "bird", classification_counts);
        assertCount(0, "mythical", classification_counts);
        assertCount(1, "tasty", classification_counts);
        assertCount(2, "big", classification_counts);
        assertCount(2, "small", classification_counts);
        assertCount(1, "predator", classification_counts);
    }

    @Test
    public void truePositivesCountedCorrectly() {

        final Map<String, AtomicInteger> true_positive_counts = matrix.getTruePositiveCounts();

        assertCount(1, "fish", true_positive_counts);
        assertCount(1, "mammal", true_positive_counts);
        assertCount(2, "bird", true_positive_counts);
        assertCount(0, "mythical", true_positive_counts);
        assertCount(0, "tasty", true_positive_counts);
        assertCount(1, "big", true_positive_counts);
        assertCount(1, "small", true_positive_counts);
        assertCount(1, "predator", true_positive_counts);
    }

    @Test
    public void trueNegativesCountedCorrectly() {

        final Map<String, AtomicInteger> true_negative_counts = matrix.getTrueNegativeCounts();

        assertCount(5, "fish", true_negative_counts);
        assertCount(4, "mammal", true_negative_counts);
        assertCount(4, "bird", true_negative_counts);
        assertCount(6, "mythical", true_negative_counts);
        assertCount(6, "tasty", true_negative_counts);
        assertCount(3, "big", true_negative_counts);
        assertCount(3, "small", true_negative_counts);
        assertCount(6, "predator", true_negative_counts);
    }

    @Test
    public void falsePositivesCountedCorrectly() {

        final Map<String, AtomicInteger> false_positive_counts = matrix.getFalsePositiveCounts();

        assertCount(1, "fish", false_positive_counts);
        assertCount(1, "mammal", false_positive_counts);
        assertCount(0, "bird", false_positive_counts);
        assertCount(0, "mythical", false_positive_counts);
        assertCount(1, "tasty", false_positive_counts);
        assertCount(1, "big", false_positive_counts);
        assertCount(1, "small", false_positive_counts);
        assertCount(0, "predator", false_positive_counts);

        assertCount(1, Classification.UNCLASSIFIED.getCode(), false_positive_counts);
    }

    @Test
    public void falseNegativesCountedCorrectly() {

        final Map<String, AtomicInteger> false_negative_counts = matrix.getFalseNegativeCounts();

        assertCount(0, "fish", false_negative_counts);
        assertCount(1, "mammal", false_negative_counts);
        assertCount(1, "bird", false_negative_counts);
        assertCount(1, "mythical", false_negative_counts);
        assertCount(0, "tasty", false_negative_counts);
        assertCount(2, "big", false_negative_counts);
        assertCount(2, "small", false_negative_counts);
        assertCount(0, "predator", false_negative_counts);
    }

    @Test
    public void otherStatsCalculatedCorrectly() {

        assertEquals(average(3, 1, 2, 2, 2, 2, 0), matrix.averageClassificationsPerRecord(), DELTA);
        assertEquals(average(2, 2, 2, 2, 3, 2, 2, 1), matrix.actualAverageClassificationsPerRecord(), DELTA);
        assertEquals(3.0 / 7.0, matrix.proportionOfRecordsWithCorrectNumberOfClassifications(), DELTA);
    }

    private static void assertCount(final int count, final String code, final Map<String, AtomicInteger> counts) {

        if (counts.containsKey(code)) {
            assertEquals(count, counts.get(code).get());
        }
    }

    private static double average(final double... numbers) {

        double sum = 0.0;
        for (final double n : numbers) {
            sum += n;
        }
        return sum / numbers.length;
    }
}
