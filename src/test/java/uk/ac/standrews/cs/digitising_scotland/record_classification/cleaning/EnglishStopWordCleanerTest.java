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

import org.apache.lucene.analysis.util.*;
import org.junit.runner.*;
import org.junit.runners.*;

import java.util.*;

/**
 * Tests {@link EnglishStopWordCleaner}.
 *
 * @author Masih Hajiarab Derkani
 */
@RunWith(Parameterized.class)
public class EnglishStopWordCleanerTest extends TextCleanerTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {

        final EnglishStopWordCleaner case_sensitive_cleaner = new EnglishStopWordCleaner();
        final HashMap<String, String> case_sensitive_test_values = new HashMap<String, String>() {

            {
                put("this and that and the", "");
                put("this and the fish", "fish");
                put("a fish and the tank", "fish tank");
                put("stop the words", "stop words");
            }
        };

        final EnglishStopWordCleaner case_insensitive_cleaner = new EnglishStopWordCleaner(true);
        final HashMap<String, String> case_insensitive_test_values = new HashMap<String, String>() {

            {
                put("thiS AND that and the", "");
                put("tHIs aND tHe fish", "fish");
                put("A fish And thE tank", "fish tank");
                put("stop tHE words", "stop words");
            }
        };

        final EnglishStopWordCleaner custom_case_sensitive_cleaner = new EnglishStopWordCleaner(new CharArraySet(Arrays.asList("unpaid", "unknown", "fish"), false));
        final HashMap<String, String> custom_cleaning_test_values = new HashMap<String, String>() {

            {
                put("this and the fish", "this and the");
                put("an unknown fish and the unpaid tank", "an and the tank");
                put("stop the unpaid unknown fish words", "stop the words");
            }
        };

        return new ArrayList<Object[]>() {

            {
                add(new Object[]{case_sensitive_cleaner, case_sensitive_test_values});
                add(new Object[]{case_insensitive_cleaner, case_insensitive_test_values});
                add(new Object[]{custom_case_sensitive_cleaner, custom_cleaning_test_values});
            }
        };
    }

    public EnglishStopWordCleanerTest(EnglishStopWordCleaner cleaner, HashMap<String, String> given_expect) {

        super(cleaner, given_expect);
    }
}
