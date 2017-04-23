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

import java.util.*;

/**
 * Tests {@link PorterStemCleaner}.
 *
 * @author Masih Hajiarab Derkani
 */
public class PorterStemCleanerTest extends TextCleanerTest {

    public PorterStemCleanerTest() {

        super(new PorterStemCleaner(), new HashMap<String, String>() {

            {
                put("driving", "drive");
                put("cats driving cars at the pub", "cat drive car at the pub");
                put("classification", "classif");
                put("this", "thi");
                put("this and that and the other stuffing shoots the shingles off the roofs", "thi and that and the other stuf shoot the shingl off the roof");
                put("john's car", "john' car");
                put("ASSISTANT - BAKER'S SHOP", "assist baker' shop");
            }
        });
    }
}
