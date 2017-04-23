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

import org.junit.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.util.*;

import static org.junit.Assert.*;
import static uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.naive_bayes.NaiveBayesClassifierTest.*;

/**
 * @author masih
 */
public class TrimClassificationCodesCleanerTest {

    private TrimClassificationCodesCleaner cleaner;

    @Before
    public void setUp() throws Exception {

        cleaner = new TrimClassificationCodesCleaner();
    }

    @Test
    public void whiteSpaceCharactersAtPrefixOrSuffixOfClassificationsAreRemoved() throws Exception {

        final Bucket cleaned = cleaner.apply(GOLD_STANDARD_WITH_SPACE_IN_CODE_PREFIX_SUFFIX);
        cleaned.stream().map(Record::getClassification).map(Classification::getCode).forEach(code -> assertTrue(isTrimmed(code)));
    }

    @Test
    public void unclassifiedRecordsAreReturnedAsTheyAre() throws Exception {

        final Bucket unclassified_bucket = GOLD_STANDARD_WITH_SPACE_IN_CODE_PREFIX_SUFFIX.stripRecordClassifications();
        final Bucket cleaned_unclassified_bucket = cleaner.apply(unclassified_bucket);

        final List<Record> expected_records = unclassified_bucket.getRecordsList();
        final List<Record> actual_records = cleaned_unclassified_bucket.getRecordsList();
        for (int i = 0; i < expected_records.size(); i++) {

            final Classification expected = expected_records.get(i).getClassification();
            final Classification actual = actual_records.get(i).getClassification();

            assertEquals(expected, actual);
        }

    }

    private boolean isTrimmed(final String code) {return code.trim().equals(code);}
}
