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

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.dataset.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author masih
 */
@RunWith(Parameterized.class)
public class CleanStopWordsCommandTest extends CommandTest {

    public static final TestResource STOP_WORDS = new TestResource(TestResource.class, "stop_words/stop_words.txt");
    public static final List<TestDataSet> GS_WITH_STOP_WORDS = Collections.singletonList(new TestDataSet(TestDataSet.class, "stop_words/gold_standard_with_stop_words.csv"));
    public static final List<TestDataSet> GS_WITHOUT_STOP_WORDS = Collections.singletonList(new TestDataSet(TestDataSet.class, "stop_words/gold_standard_without_stop_words.csv"));
    private final List<TestDataSet> gold_standards;
    private final List<TestDataSet> unseens;
    private List<TestDataSet> expected_gold_standards;
    private List<TestDataSet> expected_unseens;
    private TestResource stop_words;
    private final CharsetSupplier stop_words_charset;
    private boolean case_sensitive;

    @Parameterized.Parameters(name = "{index} {5}")
    public static Collection<Object[]> data() {

        final List<Object[]> parameters = new ArrayList<>();
        for (CharsetSupplier charset_supplier : CharsetSupplier.values()) {
            parameters.add(new Object[]{GS_WITH_STOP_WORDS, GS_WITH_STOP_WORDS, GS_WITHOUT_STOP_WORDS, GS_WITHOUT_STOP_WORDS, STOP_WORDS, charset_supplier, false});
        }
        return parameters;
    }

    public CleanStopWordsCommandTest(List<TestDataSet> gold_standards, List<TestDataSet> unseens, List<TestDataSet> expected_gold_standards, List<TestDataSet> expected_unseens, TestResource stop_words, CharsetSupplier stop_words_charset, boolean case_sensitive) throws IOException {

        this.gold_standards = gold_standards;
        this.unseens = unseens;
        this.expected_gold_standards = expected_gold_standards;
        this.expected_unseens = expected_unseens;
        this.stop_words = stop_words;
        this.stop_words_charset = stop_words_charset;
        this.case_sensitive = case_sensitive;
    }

    @Override
    @Before
    public void setUp() throws Exception {

        super.setUp();

        initForcefully();
        setVerbosity(LogLevelSupplier.OFF);
        loadGoldStandards(gold_standards);
        loadUnseens(unseens);
    }

    @Test
    public void test() throws Exception {

        clean();
        assertExpected();
    }

    private void assertExpected() throws IOException {

        final Configuration configuration = Configuration.load(working_directory);
        assertExpectedGoldStandard(configuration);
        assertExpectedUnseen(configuration);
    }

    private void assertExpectedGoldStandard(final Configuration configuration) throws IOException {

        final Bucket actual = configuration.getGoldStandardRecords();
        final Bucket expected = expected_gold_standards.stream().map(TestDataSet::getBucket).reduce(Bucket::union).get();

        assertExpected(actual, expected);
    }

    private void assertExpectedUnseen(final Configuration configuration) {

        final Bucket actual = configuration.getUnseenRecordsOptional().get();
        final Bucket expected = expected_unseens.stream().map(TestDataSet::getBucket).reduce(Bucket::union).get();

        assertExpected(actual, expected);
    }

    private void assertExpected(final Bucket actual, final Bucket expected) {

        actual.stream().forEach(actual_record -> {
            final Record expected_record = expected.findRecordById(actual_record.getId()).get();

            assertEquals(expected_record.getData(), actual_record.getData());
        });
    }

    private void clean() throws Exception {

        final Path copy = temporary.newFile().toPath();
        stop_words.copy(copy, stop_words_charset.get());

        final CleanStopWordsCommand.Builder builder = getBuilder();
        builder.setSourceCharset(stop_words_charset);
        builder.setSource(copy);
        builder.setCaseSensitive(case_sensitive);
        builder.run(launcher);
    }

    protected CleanStopWordsCommand.Builder getBuilder() {return new CleanStopWordsCommand.Builder();}
}
