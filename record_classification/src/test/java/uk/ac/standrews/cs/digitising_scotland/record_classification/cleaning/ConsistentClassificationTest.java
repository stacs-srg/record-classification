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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.AbstractMetricsTest;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConsistentClassificationTest extends AbstractMetricsTest {

    private Bucket consistent_bucket;
    private Bucket inconsistent_bucket;

    @Before
    public void setup() {

        consistent_bucket = new Bucket();
        inconsistent_bucket = new Bucket();

        consistent_bucket.add(haddock_correct, osprey_incorrect, makeRecord("haddock", "fish"));
        inconsistent_bucket.add(haddock_correct, haddock_incorrect, osprey_incorrect);
    }

    @Test
    public void removalCleanerWithConsistentBucket() {

        Bucket cleaned_bucket = ConsistentClassificationCleaner.REMOVE.apply(consistent_bucket);

        // The result should be the same as the input bucket.

        assertEquals(cleaned_bucket, consistent_bucket);
    }

    @Test
    public void removalCleanerWithInconsistentBucket() {

        // There are two records containing 'haddock', one classified as 'fish' and
        // one as 'mammal', and one record containing 'osprey'.

        Bucket cleaned_bucket = ConsistentClassificationCleaner.REMOVE.apply(inconsistent_bucket);

        // The result should be that the two 'haddock' records are removed, and the
        // 'osprey' record remains.

        assertEquals(1, cleaned_bucket.size());
        assertTrue(cleaned_bucket.contains(osprey_incorrect));
    }

    @Test
    public void consistentCleanerWithConsistentBucket() {

        Bucket cleaned_bucket = ConsistentClassificationCleaner.CORRECT.apply(consistent_bucket);

        // The result should be the same as the input bucket.

        assertEquals(cleaned_bucket, consistent_bucket);
    }

    @Test
    public void correctionCleanerWithInconsistentBucket() {

        inconsistent_bucket.add(makeRecord("haddock", "mammal"));

        // There are now three records for 'haddock', one classified as 'fish' and two
        // as 'mammal'.

        Bucket cleaned = ConsistentClassificationCleaner.CORRECT.apply(inconsistent_bucket);

        // The number of records should remain the same.

        assertEquals(inconsistent_bucket.size(), cleaned.size());

        // All records containing 'haddock' should be corrected to 'mammal' since that
        // is the most popular classification.

        for (Record record : cleaned) {

            if (record.getData().equals("haddock")) {
                assertTrue(record.getClassification().getCode().equals("mammal"));
            }
        }
    }

    @Test
    public void checker() throws Exception {

        assertTrue(new ConsistentCodingChecker().test(consistent_bucket));
        assertFalse(new ConsistentCodingChecker().test(inconsistent_bucket));
    }
}