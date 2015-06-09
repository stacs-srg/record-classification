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
package old.record_classification_old.datastructures.bucket;

import old.record_classification_old.datastructures.records.RecordFactory;
import old.record_classification_old.exceptions.InputFormatException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * The Class BucketUtilsTest.
 */
@Ignore
public class BucketUtilsTest {

    /** The bucket a. */
    private static Bucket bucketA;

    /** The bucket b. */
    private static Bucket bucketB;

    /** The bucket c. */
    private static Bucket bucketC;

    /**
     * Sets the up.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InputFormatException the input format exception
     */
    @Before
    public void setUp() throws IOException, InputFormatException {

        bucketC = new Bucket();

        File inputFileA = new File(getClass().getResource("/BucketUtilsTestFile1.txt").getFile());
        File inputFileB = new File(getClass().getResource("/BucketUtilsTestFile2.txt").getFile());

        bucketA = new Bucket(RecordFactory.makeUnCodedRecordsFromFile(inputFileA));
        bucketB = new Bucket(RecordFactory.makeUnCodedRecordsFromFile(inputFileB));

        bucketC.addCollectionOfRecords(bucketA);
        bucketC.remove(bucketA.iterator().next());
    }

    /**
     * Test compliment.
     */
    @Test
    public void testCompliment() {

        Bucket compliment = BucketUtils.getComplement(bucketA, bucketC);
        int expectedSize = 1;
        Assert.assertEquals(expectedSize, compliment.size());
    }

    /**
     * Test intersection.
     */
    @Test
    public void testIntersection() {

        Bucket intersection = BucketUtils.getIntersection(bucketA, bucketC);
        int expectedSize = bucketA.size() - 1;
        Assert.assertEquals(expectedSize, intersection.size());
    }

    /**
     * Test union.
     */
    @Test
    public void testUnion() {

        Bucket union = BucketUtils.getUnion(bucketA, bucketB);
        int expectedSize = bucketA.size() + bucketB.size();
        Assert.assertEquals(expectedSize, union.size());
    }

}
