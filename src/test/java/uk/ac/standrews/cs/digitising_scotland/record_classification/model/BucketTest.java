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
package uk.ac.standrews.cs.digitising_scotland.record_classification.model;

import org.junit.Test;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.DuplicateRecordIdException;

public class BucketTest {

    private static final Record[] RECORDS_WITH_DUPLICATE_IDS = {

            new Record(1, "abc", new Classification("class1", new TokenList("abc"), 1.0, null)),
            new Record(2, "def", new Classification("class2", new TokenList("def"), 1.0, null)),
            new Record(2, "ghi", new Classification("class3", new TokenList("ghi"), 1.0, null)),
            new Record(3, "bcd", new Classification("class4", new TokenList("bcd"), 1.0, null)),
            new Record(4, "efg", new Classification("class5", new TokenList("efg"), 1.0, null))
    };

    @Test(expected = DuplicateRecordIdException.class)
    public void duplicateRecordIdsDetected() {

        new Bucket(RECORDS_WITH_DUPLICATE_IDS);
    }
}
