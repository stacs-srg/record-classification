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

import org.junit.Test;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.*;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ConfusionMatrixTest extends AbstractMetricsTest {

    @Test
    public void emptyBucketsYieldNoClassifications() throws InvalidCodeException, InconsistentCodingException, UnknownDataException, UnclassifiedGoldStandardRecordException {

        initMatrix();

        assertEquals(0, matrix.getTotalNumberOfClassifications());
    }

    @Test(expected = UnknownDataException.class)
    public void classificationWithNoGoldStandardThrowsException() throws InvalidCodeException, InconsistentCodingException, UnknownDataException, UnclassifiedGoldStandardRecordException {

        classified_records.add(haddock_correct);

        initMatrix();
    }

    @Test(expected = UnclassifiedGoldStandardRecordException.class)
    public void unclassifiedGoldStandardRecordThrowsException() throws InvalidCodeException, InconsistentCodingException, UnknownDataException, UnclassifiedGoldStandardRecordException {

        gold_standard_records.add(unicorn_unclassified);

        initMatrix();
    }

    @Test(expected = InvalidCodeException.class)
    public void classificationToCodeNotInGoldStandardThrowsException() throws InvalidCodeException, InconsistentCodingException, UnknownDataException, UnclassifiedGoldStandardRecordException {

        classified_records.add(haddock_incorrect);
        gold_standard_records.add(haddock_gold_standard);

        initMatrix();
    }

    @Test
    public void singleClassificationCountedCorrectly() throws InvalidCodeException, InconsistentCodingException, UnknownDataException, UnclassifiedGoldStandardRecordException {

        classified_records.add(haddock_correct);
        gold_standard_records.add(haddock_gold_standard);

        initMatrix();

        assertEquals(1, matrix.getTotalNumberOfClassifications());
    }

    @Test(expected = InconsistentCodingException.class)
    public void inconsistentCodingThrowsException() throws InconsistentCodingException, InvalidCodeException, UnknownDataException, UnclassifiedGoldStandardRecordException {

        classified_records.add(haddock_correct, haddock_incorrect);
        gold_standard_records.add(haddock_correct, haddock_incorrect);

        initMatrix();
    }

    @Test(expected = UnknownDataException.class)
    public void unknownClassificationThrowsException() throws UnknownDataException, InvalidCodeException, InconsistentCodingException, UnclassifiedGoldStandardRecordException {

        classified_records.add(haddock_correct, osprey_incorrect);
        gold_standard_records.add(haddock_gold_standard, cow_gold_standard);

        initMatrix();
    }

    @Test
    public void perCodeClassificationsCountedCorrectly() throws InvalidCodeException, InconsistentCodingException, UnknownDataException, UnclassifiedGoldStandardRecordException, InputFileFormatException {

        initFullRecords();
        initMatrix();

        Map<String, Integer> classification_counts = matrix.getClassificationCounts();

        assertEquals(getNumberOfCodes(), classification_counts.size());

        assertEquals(2, (int) classification_counts.get("fish"));
        assertEquals(2, (int) classification_counts.get("mammal"));
        assertEquals(2, (int) classification_counts.get("bird"));
        assertEquals(0, (int) classification_counts.get("mythical"));
    }

    @Test
    public void truePositivesCountedCorrectly() throws InvalidCodeException, InconsistentCodingException, UnknownDataException, UnclassifiedGoldStandardRecordException, InputFileFormatException {

        initFullRecords();
        initMatrix();

        Map<String, Integer> true_positive_counts = matrix.getTruePositiveCounts();

        assertEquals(getNumberOfCodes(), true_positive_counts.size());

        assertEquals(1, (int) true_positive_counts.get("fish"));
        assertEquals(1, (int) true_positive_counts.get("mammal"));
        assertEquals(2, (int) true_positive_counts.get("bird"));
        assertEquals(0, (int) true_positive_counts.get("mythical"));
    }

    @Test
    public void trueNegativesCountedCorrectly() throws InvalidCodeException, InconsistentCodingException, UnknownDataException, UnclassifiedGoldStandardRecordException, InputFileFormatException {

        initFullRecords();
        initMatrix();

        Map<String, Integer> true_negative_counts = matrix.getTrueNegativeCounts();

        assertEquals(getNumberOfCodes(), true_negative_counts.size());

        assertEquals(4, (int) true_negative_counts.get("fish"));
        assertEquals(3, (int) true_negative_counts.get("mammal"));
        assertEquals(3, (int) true_negative_counts.get("bird"));
        assertEquals(6, (int) true_negative_counts.get("mythical"));
    }

    @Test
    public void falsePositivesCountedCorrectly() throws InvalidCodeException, InconsistentCodingException, UnknownDataException, UnclassifiedGoldStandardRecordException, InputFileFormatException {

        initFullRecords();
        initMatrix();

        Map<String, Integer> false_positive_counts = matrix.getFalsePositiveCounts();

        assertEquals(getNumberOfCodes(), false_positive_counts.size());

        assertEquals(1, (int) false_positive_counts.get("fish"));
        assertEquals(1, (int) false_positive_counts.get("mammal"));
        assertEquals(0, (int) false_positive_counts.get("bird"));
        assertEquals(0, (int) false_positive_counts.get("mythical"));
    }

    @Test
    public void falseNegativesCountedCorrectly() throws InvalidCodeException, InconsistentCodingException, UnknownDataException, UnclassifiedGoldStandardRecordException, InputFileFormatException {

        initFullRecords();
        initMatrix();

        Map<String, Integer> false_negative_counts = matrix.getFalseNegativeCounts();

        assertEquals(getNumberOfCodes(), false_negative_counts.size());

        assertEquals(0, (int) false_negative_counts.get("fish"));
        assertEquals(1, (int) false_negative_counts.get("mammal"));
        assertEquals(1, (int) false_negative_counts.get("bird"));
        assertEquals(0, (int) false_negative_counts.get("mythical"));
    }
}
