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
import uk.ac.standrews.cs.digitising_scotland.record_classification.dataset.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.util.*;
import java.util.stream.*;

import static org.junit.Assert.*;

/**
 * @author Masih Hajiarab Derkani
 */
public class UnclassifiedCheckerTest {

    protected static final List<Bucket> TEST_BUCKETS_WITH_CLASSIFICATION = TestDataSets.ALL_TRAINING_DATASETS.stream().map(TestDataSet::getBucket).collect(Collectors.toList());
    protected static final List<Bucket> TEST_BUCKETS_WITHOUT_CLASSIFICATION = TEST_BUCKETS_WITH_CLASSIFICATION.stream().map(Bucket::stripRecordClassifications).collect(Collectors.toList());
    private UnclassifiedChecker checker;

    @Before
    public void setUp() throws Exception {

        checker = new UnclassifiedChecker();
    }

    @Test
    public void checkFailsIfRecordsAreClassified() throws Exception {

        final Bucket first_bucket_with_classification = TEST_BUCKETS_WITH_CLASSIFICATION.get(0);
        final Bucket[] buckets_with_classification_as_array = TEST_BUCKETS_WITH_CLASSIFICATION.toArray(new Bucket[TEST_BUCKETS_WITH_CLASSIFICATION.size()]);
        assertFalse(checkWithListOfBuckets(TEST_BUCKETS_WITH_CLASSIFICATION));
        assertFalse(checkWithSingleBucket(first_bucket_with_classification));
        assertFalse(checkWithOneOrMoreBuckets(first_bucket_with_classification, buckets_with_classification_as_array));
    }

    @Test
    public void checkPassesIfRecordsAreUnclassified() throws Exception {

        final Bucket first_bucket_without_classification = TEST_BUCKETS_WITHOUT_CLASSIFICATION.get(0);
        final Bucket[] buckets_without_classification_as_array = TEST_BUCKETS_WITHOUT_CLASSIFICATION.toArray(new Bucket[TEST_BUCKETS_WITHOUT_CLASSIFICATION.size()]);
        assertTrue(checkWithListOfBuckets(TEST_BUCKETS_WITHOUT_CLASSIFICATION));
        assertTrue(checkWithSingleBucket(first_bucket_without_classification));
        assertTrue(checkWithOneOrMoreBuckets(first_bucket_without_classification, buckets_without_classification_as_array));
    }

    private boolean checkWithOneOrMoreBuckets(final Bucket first, final Bucket[] rest) {return checker.test(first, rest);}

    private boolean checkWithListOfBuckets(final List<Bucket> buckets) {return checker.test(buckets);}

    private boolean checkWithSingleBucket(final Bucket bucket) {return checker.test(bucket);}
}
