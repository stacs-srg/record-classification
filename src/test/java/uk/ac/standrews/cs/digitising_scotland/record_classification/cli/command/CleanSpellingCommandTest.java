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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cli.command;

import org.junit.runner.*;
import org.junit.runners.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.dataset.*;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Masih Hajirab Derkani
 */
@RunWith(Parameterized.class)
public class CleanSpellingCommandTest extends CleanStopWordsCommandTest {

    public static final TestResource DICTIONARY = new TestResource(TestResource.class, "spelling/dictionary.txt");
    public static final List<TestDataSet> GS_WITH_MISTAKES = Collections.singletonList(new TestDataSet(TestDataSet.class, "spelling/gold_standard_with_spelling_mistakes.csv"));
    public static final List<TestDataSet> GS_WITHOUT_MISTAKES = Collections.singletonList(new TestDataSet(TestDataSet.class, "spelling/gold_standard_without_spelling_mistakes.csv"));

    @Parameterized.Parameters(name = "{index} {5}")
    public static Collection<Object[]> data() {

        final List<Object[]> parameters = new ArrayList<>();
        for (CharsetSupplier charset_supplier : CharsetSupplier.values()) {
            parameters.add(new Object[]{GS_WITH_MISTAKES, GS_WITH_MISTAKES, GS_WITHOUT_MISTAKES, GS_WITHOUT_MISTAKES, DICTIONARY, charset_supplier, false});
        }

        return parameters;
    }

    public CleanSpellingCommandTest(List<TestDataSet> gold_standards, List<TestDataSet> unseens, List<TestDataSet> expected_gold_standards, List<TestDataSet> expected_unseens, TestResource dictionary, CharsetSupplier dictionary_charset, boolean case_sensitive) throws IOException {

        super(gold_standards, unseens, expected_gold_standards, expected_unseens, dictionary, dictionary_charset, case_sensitive);
    }

    @Override
    protected CleanStopWordsCommand.Builder getBuilder() {

        final CleanSpellingCommand.Builder builder = new CleanSpellingCommand.Builder();
        builder.setAccuracyThreshold(0.7f);
        return builder;
    }
}
