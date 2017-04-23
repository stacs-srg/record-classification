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

import org.junit.Test;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;

import java.util.Map;
import java.util.concurrent.atomic.*;

import static org.junit.Assert.assertEquals;

public class ConfusionMatrixTest extends AbstractMetricsTest {

    @Test
    public void emptyBucketsYieldNoClassifications() throws Exception {

        initMatrix();

        assertEquals(0, matrix.getNumberOfClassifications());
    }

    @Test(expected = UnknownDataException.class)
    public void classificationWithNoGoldStandardThrowsException() throws Exception {

        classified_records.add(haddock_correct);

        initMatrix();
    }

    @Test(expected = UnclassifiedGoldStandardRecordException.class)
    public void unclassifiedGoldStandardRecordThrowsException() throws Exception {

        gold_standard_records.add(unicorn_unclassified);

        initMatrix();
    }

    @Test(expected = UnknownClassificationException.class)
    public void classificationToCodeNotInGoldStandardThrowsException() throws Exception {

        classified_records.add(haddock_incorrect);
        gold_standard_records.add(haddock_gold_standard);

        initMatrix();
    }

    @Test
    public void singleClassificationCountedCorrectly() throws Exception {

        classified_records.add(haddock_correct);
        gold_standard_records.add(haddock_gold_standard);

        initMatrix();

        assertEquals(1, matrix.getNumberOfClassifications());
    }

    @Test(expected = RuntimeException.class)
    public void inconsistentCodingThrowsException() throws Exception {

        classified_records.add(haddock_correct, haddock_incorrect);
        gold_standard_records.add(haddock_correct, haddock_incorrect);

        initMatrix();
    }

    @Test(expected = UnknownDataException.class)
    public void unknownClassificationThrowsException() throws Exception {

        classified_records.add(haddock_correct, osprey_incorrect);
        gold_standard_records.add(haddock_gold_standard, cow_gold_standard);

        initMatrix();
    }

    @Test
    public void perCodeClassificationsCountedCorrectly() throws Exception {

        initFullRecords();
        initMatrix();

        Map<String, AtomicInteger> classification_counts = matrix.getClassificationCounts();

        assertEquals(getNumberOfCodes(), classification_counts.size());

        assertEquals(2, classification_counts.get("fish").get());
        assertEquals(2, classification_counts.get("mammal").get());
        assertEquals(2, classification_counts.get("bird").get());
        assertEquals(0, classification_counts.get("mythical").get());
    }

    @Test
    public void truePositivesCountedCorrectly() throws Exception {

        initFullRecords();
        initMatrix();

        Map<String, AtomicInteger> true_positive_counts = matrix.getTruePositiveCounts();

        assertEquals(getNumberOfCodes(), true_positive_counts.size());

        assertEquals(1, true_positive_counts.get("fish").get());
        assertEquals(1, true_positive_counts.get("mammal").get());
        assertEquals(2, true_positive_counts.get("bird").get());
        assertEquals(0, true_positive_counts.get("mythical").get());
    }

    @Test
    public void trueNegativesCountedCorrectly() throws Exception {

        initFullRecords();
        initMatrix();

        Map<String, AtomicInteger> true_negative_counts = matrix.getTrueNegativeCounts();

        assertEquals(getNumberOfCodes(), true_negative_counts.size());

        assertEquals(5, true_negative_counts.get("fish").get());
        assertEquals(4, true_negative_counts.get("mammal").get());
        assertEquals(4, true_negative_counts.get("bird").get());
        assertEquals(6, true_negative_counts.get("mythical").get());
    }

    @Test
    public void falsePositivesCountedCorrectly() throws Exception {

        initFullRecords();
        initMatrix();

        Map<String, AtomicInteger> false_positive_counts = matrix.getFalsePositiveCounts();

        assertEquals(getNumberOfCodes(), false_positive_counts.size());

        assertEquals(1, false_positive_counts.get("fish").get());
        assertEquals(1, false_positive_counts.get("mammal").get());
        assertEquals(0, false_positive_counts.get("bird").get());
        assertEquals(0, false_positive_counts.get("mythical").get());
        assertEquals(1, false_positive_counts.get(Classification.UNCLASSIFIED.getCode()).get());
    }

    @Test
    public void falseNegativesCountedCorrectly() throws Exception {

        initFullRecords();
        initMatrix();

        Map<String, AtomicInteger> false_negative_counts = matrix.getFalseNegativeCounts();

        assertEquals(getNumberOfCodes(), false_negative_counts.size());

        assertEquals(0, false_negative_counts.get("fish").get());
        assertEquals(1, false_negative_counts.get("mammal").get());
        assertEquals(1, false_negative_counts.get("bird").get());
        assertEquals(1, false_negative_counts.get("mythical").get());
    }
}
