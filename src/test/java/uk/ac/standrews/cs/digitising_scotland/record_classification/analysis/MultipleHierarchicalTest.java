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

public class MultipleHierarchicalTest {

    private static final String CLASSIFIED_FILE_NAME = "example_classified.csv";
    private static final String GOLD_STANDARD_FILE_NAME = "example_gold_standard.csv";

    private static final double DELTA = 0.001;

    private DataSet classified_records_csv;
    private DataSet gold_standard_records_csv;

    private ConfusionMatrix matrix;

    @Before
    public void setUp() throws Exception {

        try (final InputStream stream = FileManipulation.getInputStreamForResource(MultipleHierarchicalTest.class, CLASSIFIED_FILE_NAME)) {

            classified_records_csv = new DataSet(stream, ',');
        }
        try (final InputStream stream = FileManipulation.getInputStreamForResource(MultipleHierarchicalTest.class, GOLD_STANDARD_FILE_NAME)) {

            gold_standard_records_csv = new DataSet(stream, ',');
        }
    }

    @Test
    public void perCodeClassificationsCountedCorrectlyOneUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(5, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> classification_counts = matrix.getClassificationCounts();

        assertCount(0, "A12.6", classification_counts);
        assertCount(0, "A12.7", classification_counts);
        assertCount(1, "A34.5", classification_counts);
        assertCount(0, "A72.5", classification_counts);
        assertCount(0, "B21.4", classification_counts);
        assertCount(0, "B61.4", classification_counts);
        assertCount(1, "C11.1", classification_counts);
        assertCount(0, "C49.7", classification_counts);
        assertCount(1, "D11.2", classification_counts);
        assertCount(1, "D55.1", classification_counts);
        assertCount(2, "E34.6", classification_counts);
        assertCount(0, "E52.2", classification_counts);
        assertCount(0, "J21.0", classification_counts);
        assertCount(0, "L54.8", classification_counts);
        assertCount(0, "X00.0", classification_counts);
    }

    @Test
    public void truePositivesCountedCorrectlyOneUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(5, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> true_positive_counts = matrix.getTruePositiveCounts();

        assertCount(0, "A12.6", true_positive_counts);
        assertCount(0, "A12.7", true_positive_counts);
        assertCount(1, "A34.5", true_positive_counts);
        assertCount(0, "A72.5", true_positive_counts);
        assertCount(0, "B21.4", true_positive_counts);
        assertCount(0, "B61.4", true_positive_counts);
        assertCount(1, "C11.1", true_positive_counts);
        assertCount(0, "C49.7", true_positive_counts);
        assertCount(1, "D11.2", true_positive_counts);
        assertCount(1, "D55.1", true_positive_counts);
        assertCount(1, "E34.6", true_positive_counts);
        assertCount(0, "E52.2", true_positive_counts);
        assertCount(0, "J21.0", true_positive_counts);
        assertCount(0, "L54.8", true_positive_counts);
        assertCount(0, "X00.0", true_positive_counts);
    }

    @Test
    public void trueNegativesCountedCorrectlyOneUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(5, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> true_negative_counts = matrix.getTrueNegativeCounts();

        assertCount(6, "A12.6", true_negative_counts);
        assertCount(6, "A12.7", true_negative_counts);
        assertCount(6, "A34.5", true_negative_counts);
        assertCount(6, "A72.5", true_negative_counts);
        assertCount(6, "B21.4", true_negative_counts);
        assertCount(7, "B61.4", true_negative_counts);
        assertCount(6, "C11.1", true_negative_counts);
        assertCount(6, "C49.7", true_negative_counts);
        assertCount(6, "D11.2", true_negative_counts);
        assertCount(6, "D55.1", true_negative_counts);
        assertCount(5, "E34.6", true_negative_counts);
        assertCount(6, "E52.2", true_negative_counts);
        assertCount(6, "J21.0", true_negative_counts);
        assertCount(6, "L54.8", true_negative_counts);
        assertCount(6, "X00.0", true_negative_counts);
    }

    @Test
    public void falsePositivesCountedCorrectlyOneUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(5, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> false_positive_counts = matrix.getFalsePositiveCounts();

        assertCount(0, "A12.6", false_positive_counts);
        assertCount(0, "A12.7", false_positive_counts);
        assertCount(0, "A34.5", false_positive_counts);
        assertCount(0, "A72.5", false_positive_counts);
        assertCount(0, "B21.4", false_positive_counts);
        assertCount(0, "B61.4", false_positive_counts);
        assertCount(0, "C11.1", false_positive_counts);
        assertCount(0, "C49.7", false_positive_counts);
        assertCount(0, "D11.2", false_positive_counts);
        assertCount(0, "D55.1", false_positive_counts);
        assertCount(1, "E34.6", false_positive_counts);
        assertCount(0, "E52.2", false_positive_counts);
        assertCount(0, "J21.0", false_positive_counts);
        assertCount(0, "L54.8", false_positive_counts);
        assertCount(0, "X00.0", false_positive_counts);
        assertCount(1, Classification.UNCLASSIFIED.getCode(), false_positive_counts);
    }

    @Test
    public void falseNegativesCountedCorrectlyOneUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(5, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> false_negative_counts = matrix.getFalseNegativeCounts();

        assertCount(1, "A12.6", false_negative_counts);
        assertCount(1, "A12.7", false_negative_counts);
        assertCount(0, "A34.5", false_negative_counts);
        assertCount(1, "A72.5", false_negative_counts);
        assertCount(1, "B21.4", false_negative_counts);
        assertCount(0, "B61.4", false_negative_counts);
        assertCount(0, "C11.1", false_negative_counts);
        assertCount(1, "C49.7", false_negative_counts);
        assertCount(0, "D11.2", false_negative_counts);
        assertCount(0, "D55.1", false_negative_counts);
        assertCount(0, "E34.6", false_negative_counts);
        assertCount(1, "E52.2", false_negative_counts);
        assertCount(1, "J21.0", false_negative_counts);
        assertCount(1, "L54.8", false_negative_counts);
        assertCount(1, "X00.0", false_negative_counts);
    }

    @Test
    public void otherStatsCalculatedCorrectlyOneUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(5, classified_records_csv, gold_standard_records_csv);

        assertEquals(2.0 / 7.0, matrix.proportionOfRecordsWithCorrectNumberOfClassifications(), DELTA);
    }

    @Test
    public void perCodeClassificationsCountedCorrectlyTwoUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(3, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> classification_counts = matrix.getClassificationCounts();

        assertCount(0, "A12", classification_counts);
        assertCount(1, "A34", classification_counts);
        assertCount(0, "A72", classification_counts);
        assertCount(0, "B21", classification_counts);
        assertCount(0, "B61", classification_counts);
        assertCount(1, "C11", classification_counts);
        assertCount(0, "C49", classification_counts);
        assertCount(1, "D11", classification_counts);
        assertCount(1, "D55", classification_counts);
        assertCount(2, "E34", classification_counts);
        assertCount(0, "E52", classification_counts);
        assertCount(1, "J21", classification_counts);
        assertCount(0, "L54", classification_counts);
        assertCount(0, "X00", classification_counts);
    }

    @Test
    public void truePositivesCountedCorrectlyTwoUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(3, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> true_positive_counts = matrix.getTruePositiveCounts();

        assertCount(0, "A12", true_positive_counts);
        assertCount(1, "A34", true_positive_counts);
        assertCount(0, "A72", true_positive_counts);
        assertCount(0, "B21", true_positive_counts);
        assertCount(0, "B61", true_positive_counts);
        assertCount(1, "C11", true_positive_counts);
        assertCount(0, "C49", true_positive_counts);
        assertCount(1, "D11", true_positive_counts);
        assertCount(1, "D55", true_positive_counts);
        assertCount(1, "E34", true_positive_counts);
        assertCount(0, "E52", true_positive_counts);
        assertCount(1, "J21", true_positive_counts);
        assertCount(0, "L54", true_positive_counts);
        assertCount(0, "X00", true_positive_counts);
    }

    @Test
    public void trueNegativesCountedCorrectlyTwoUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(3, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> true_negative_counts = matrix.getTrueNegativeCounts();

        assertCount(5, "A12", true_negative_counts);
        assertCount(6, "A34", true_negative_counts);
        assertCount(6, "A72", true_negative_counts);
        assertCount(6, "B21", true_negative_counts);
        assertCount(7, "B61", true_negative_counts);
        assertCount(6, "C11", true_negative_counts);
        assertCount(6, "C49", true_negative_counts);
        assertCount(6, "D11", true_negative_counts);
        assertCount(6, "D55", true_negative_counts);
        assertCount(5, "E34", true_negative_counts);
        assertCount(6, "E52", true_negative_counts);
        assertCount(6, "J21", true_negative_counts);
        assertCount(6, "L54", true_negative_counts);
        assertCount(6, "X00", true_negative_counts);
    }

    @Test
    public void falsePositivesCountedCorrectlyTwoUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(3, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> false_positive_counts = matrix.getFalsePositiveCounts();

        assertCount(0, "A12", false_positive_counts);
        assertCount(0, "A34", false_positive_counts);
        assertCount(0, "A72", false_positive_counts);
        assertCount(0, "B21", false_positive_counts);
        assertCount(0, "B61", false_positive_counts);
        assertCount(0, "C11", false_positive_counts);
        assertCount(0, "C49", false_positive_counts);
        assertCount(0, "D11", false_positive_counts);
        assertCount(0, "D55", false_positive_counts);
        assertCount(1, "E34", false_positive_counts);
        assertCount(0, "E52", false_positive_counts);
        assertCount(0, "J21", false_positive_counts);
        assertCount(0, "L54", false_positive_counts);
        assertCount(0, "X00", false_positive_counts);
        assertCount(1, Classification.UNCLASSIFIED.getCode(), false_positive_counts);
    }

    @Test
    public void falseNegativesCountedCorrectlyTwoUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(3, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> false_negative_counts = matrix.getFalseNegativeCounts();

        assertCount(2, "A12", false_negative_counts);
        assertCount(0, "A34", false_negative_counts);
        assertCount(1, "A72", false_negative_counts);
        assertCount(1, "B21", false_negative_counts);
        assertCount(0, "B61", false_negative_counts);
        assertCount(0, "C11", false_negative_counts);
        assertCount(1, "C49", false_negative_counts);
        assertCount(0, "D11", false_negative_counts);
        assertCount(0, "D55", false_negative_counts);
        assertCount(0, "E34", false_negative_counts);
        assertCount(1, "E52", false_negative_counts);
        assertCount(0, "J21", false_negative_counts);
        assertCount(1, "L54", false_negative_counts);
        assertCount(1, "X00", false_negative_counts);
    }

    @Test
    public void otherStatsCalculatedCorrectlyTwoUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(3, classified_records_csv, gold_standard_records_csv);

        assertEquals(2.0 / 7.0, matrix.proportionOfRecordsWithCorrectNumberOfClassifications(), DELTA);
    }

    @Test
    public void perCodeClassificationsCountedCorrectlyThreeUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(2, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> classification_counts = matrix.getClassificationCounts();

        assertCount(1, "A1", classification_counts);
        assertCount(1, "A3", classification_counts);
        assertCount(0, "A7", classification_counts);
        assertCount(0, "B2", classification_counts);
        assertCount(0, "B6", classification_counts);
        assertCount(1, "C1", classification_counts);
        assertCount(0, "C4", classification_counts);
        assertCount(1, "D1", classification_counts);
        assertCount(1, "D5", classification_counts);
        assertCount(2, "E3", classification_counts);
        assertCount(1, "E5", classification_counts);
        assertCount(1, "J2", classification_counts);
        assertCount(0, "L5", classification_counts);
        assertCount(0, "X0", classification_counts);
    }

    @Test
    public void truePositivesCountedCorrectlyThreeUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(2, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> true_positive_counts = matrix.getTruePositiveCounts();

        assertCount(1, "A1", true_positive_counts);
        assertCount(1, "A3", true_positive_counts);
        assertCount(0, "A7", true_positive_counts);
        assertCount(0, "B2", true_positive_counts);
        assertCount(0, "B6", true_positive_counts);
        assertCount(1, "C1", true_positive_counts);
        assertCount(0, "C4", true_positive_counts);
        assertCount(1, "D1", true_positive_counts);
        assertCount(1, "D5", true_positive_counts);
        assertCount(1, "E3", true_positive_counts);
        assertCount(1, "E5", true_positive_counts);
        assertCount(1, "J2", true_positive_counts);
        assertCount(0, "L5", true_positive_counts);
        assertCount(0, "X0", true_positive_counts);
    }

    @Test
    public void trueNegativesCountedCorrectlyThreeUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(2, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> true_negative_counts = matrix.getTrueNegativeCounts();

        assertCount(5, "A1", true_negative_counts);
        assertCount(6, "A3", true_negative_counts);
        assertCount(6, "A7", true_negative_counts);
        assertCount(6, "B2", true_negative_counts);
        assertCount(7, "B6", true_negative_counts);
        assertCount(6, "C1", true_negative_counts);
        assertCount(6, "C4", true_negative_counts);
        assertCount(6, "D1", true_negative_counts);
        assertCount(6, "D5", true_negative_counts);
        assertCount(5, "E3", true_negative_counts);
        assertCount(6, "E5", true_negative_counts);
        assertCount(6, "J2", true_negative_counts);
        assertCount(6, "L5", true_negative_counts);
        assertCount(6, "X0", true_negative_counts);
    }

    @Test
    public void falsePositivesCountedCorrectlyThreeUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(2, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> false_positive_counts = matrix.getFalsePositiveCounts();

        assertCount(0, "A1", false_positive_counts);
        assertCount(0, "A3", false_positive_counts);
        assertCount(0, "A7", false_positive_counts);
        assertCount(0, "B2", false_positive_counts);
        assertCount(0, "B6", false_positive_counts);
        assertCount(0, "C1", false_positive_counts);
        assertCount(0, "C4", false_positive_counts);
        assertCount(0, "D1", false_positive_counts);
        assertCount(0, "D5", false_positive_counts);
        assertCount(1, "E3", false_positive_counts);
        assertCount(0, "E5", false_positive_counts);
        assertCount(0, "J2", false_positive_counts);
        assertCount(0, "L5", false_positive_counts);
        assertCount(0, "X0", false_positive_counts);
        assertCount(1, Classification.UNCLASSIFIED.getCode(), false_positive_counts);
    }

    @Test
    public void falseNegativesCountedCorrectlyThreeUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(2, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> false_negative_counts = matrix.getFalseNegativeCounts();

        assertCount(1, "A1", false_negative_counts);
        assertCount(0, "A3", false_negative_counts);
        assertCount(1, "A7", false_negative_counts);
        assertCount(1, "B2", false_negative_counts);
        assertCount(0, "B6", false_negative_counts);
        assertCount(0, "C1", false_negative_counts);
        assertCount(1, "C4", false_negative_counts);
        assertCount(0, "D1", false_negative_counts);
        assertCount(0, "D5", false_negative_counts);
        assertCount(0, "E3", false_negative_counts);
        assertCount(0, "E5", false_negative_counts);
        assertCount(0, "J2", false_negative_counts);
        assertCount(1, "L5", false_negative_counts);
        assertCount(1, "X0", false_negative_counts);
    }

    @Test
    public void otherStatsCalculatedCorrectlyThreeUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(2, classified_records_csv, gold_standard_records_csv);

        assertEquals(2.0 / 7.0, matrix.proportionOfRecordsWithCorrectNumberOfClassifications(), DELTA);
    }

    @Test
    public void perCodeClassificationsCountedCorrectlyFourUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(1, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> classification_counts = matrix.getClassificationCounts();

        assertCount(2, "A", classification_counts);
        assertCount(0, "B", classification_counts);
        assertCount(2, "C", classification_counts);
        assertCount(3, "D", classification_counts);
        assertCount(2, "E", classification_counts);
        assertCount(1, "J", classification_counts);
        assertCount(0, "L", classification_counts);
        assertCount(0, "X", classification_counts);
    }

    @Test
    public void truePositivesCountedCorrectlyFourUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(1, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> true_positive_counts = matrix.getTruePositiveCounts();

        assertCount(2, "A", true_positive_counts);
        assertCount(0, "B", true_positive_counts);
        assertCount(2, "C", true_positive_counts);
        assertCount(2, "D", true_positive_counts);
        assertCount(2, "E", true_positive_counts);
        assertCount(1, "J", true_positive_counts);
        assertCount(0, "L", true_positive_counts);
        assertCount(0, "X", true_positive_counts);
    }

    @Test
    public void trueNegativesCountedCorrectlyFourUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(1, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> true_negative_counts = matrix.getTrueNegativeCounts();

        assertCount(4, "A", true_negative_counts);
        assertCount(6, "B", true_negative_counts);
        assertCount(5, "C", true_negative_counts);
        assertCount(4, "D", true_negative_counts);
        assertCount(5, "E", true_negative_counts);
        assertCount(6, "J", true_negative_counts);
        assertCount(6, "L", true_negative_counts);
        assertCount(6, "X", true_negative_counts);
    }

    @Test
    public void falsePositivesCountedCorrectlyFourUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(1, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> false_positive_counts = matrix.getFalsePositiveCounts();

        assertCount(0, "A", false_positive_counts);
        assertCount(0, "B", false_positive_counts);
        assertCount(0, "C", false_positive_counts);
        assertCount(1, "D", false_positive_counts);
        assertCount(0, "E", false_positive_counts);
        assertCount(0, "J", false_positive_counts);
        assertCount(0, "L", false_positive_counts);
        assertCount(0, "X", false_positive_counts);
        assertCount(1, Classification.UNCLASSIFIED.getCode(), false_positive_counts);
    }

    @Test
    public void falseNegativesCountedCorrectlyFourUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(1, classified_records_csv, gold_standard_records_csv);

        final Map<String, AtomicInteger> false_negative_counts = matrix.getFalseNegativeCounts();

        assertCount(1, "A", false_negative_counts);
        assertCount(1, "B", false_negative_counts);
        assertCount(0, "C", false_negative_counts);
        assertCount(0, "D", false_negative_counts);
        assertCount(0, "E", false_negative_counts);
        assertCount(0, "J", false_negative_counts);
        assertCount(1, "L", false_negative_counts);
        assertCount(1, "X", false_negative_counts);
    }

    @Test
    public void otherStatsCalculatedCorrectlyFourUpInHierarchy() {

        matrix = new MatchingPrefixConfusionMatrix(1, classified_records_csv, gold_standard_records_csv);

        assertEquals(3.0 / 7.0, matrix.proportionOfRecordsWithCorrectNumberOfClassifications(), DELTA);
    }

    private void assertCount(final int count, final String code, final Map<String, AtomicInteger> counts) {

        if (counts.containsKey(code)) {
            assertEquals(count, counts.get(code).get());
        }
    }
}
