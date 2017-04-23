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
public class LowerCaseCleanerTest extends TextCleanerTest {

    public LowerCaseCleanerTest() {

        super(new LowerCaseCleaner(), new HashMap<String, String>() {

            {
                put("TEST", "test");
                put("ReSt", "rest");
                put("yoyO", "yoyo");
                put("googlYEyesRollLiKe...CrazY", "googlyeyesrolllike...crazy");
                put("someThingµostp∑çuli0√YO", "somethingµostp∑çuli0√yo");
            }
        });
    }
}
