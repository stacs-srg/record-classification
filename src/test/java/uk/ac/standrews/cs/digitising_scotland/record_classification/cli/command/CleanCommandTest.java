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
import org.junit.rules.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.dataset.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.nio.file.*;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Graham Kirby
 * @author Masih Hajiarab Derkani
 */
public class CleanCommandTest extends CommandTest {

    private static final List<String> SUFFIXES = Arrays.asList("ing", "s");
    private static final List<String> PUNCTUATION_CHARACTERS = Arrays.asList(".", ",", "'", "\"", "!", "-", "+", "@", "|", "<", ">", "%", "&", "*", "(", ")", "/", "\\");

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private Path source;

    @Override
    @Before
    public void setUp() throws Exception {

        super.setUp();
        source = temp.newFile().toPath();
        TestDataSets.UNCLEAN_TRAINING.copy(source);
    }

    @Test
    public void goldStandardCleaned() throws Exception {

        initForcefully();
        loadGoldStandard();
        cleanUsingCombinedCleaner();

        assertRecordsAreCleaned(configuration.requireGoldStandardRecords());
    }

    protected void loadGoldStandard() throws Exception {

        final LoadGoldStandardRecordsCommand.Builder builder = new LoadGoldStandardRecordsCommand.Builder();
        builder.setDelimiter(',');
        builder.setSkipHeader();
        builder.setSource(source);
        builder.run(launcher);
    }

    protected void cleanUsingCombinedCleaner() throws Exception {

        final CleanCommand.Builder builder = new CleanCommand.Builder();
        builder.addCleaners(CleanerSupplier.COMBINED);
        builder.run(launcher);
    }

    private void assertRecordsAreCleaned(Bucket bucket) {

        for (Record record : bucket) {
            assertClean(record);
        }
    }

    private void assertClean(Record record) {

        String data = record.getData();
        String classification_tokens = record.getClassification().getTokenList().toString();

        assertClean(data);
        assertClean(classification_tokens);
    }

    private void assertClean(String s) {

        assertNoPunctuation(s);

        for (String token : new TokenList(s)) {
            assertNotInStopWords(token);
            assertNoSuffix(token);
        }
    }

    private void assertNoPunctuation(String s) {

        for (String punctuation_character : PUNCTUATION_CHARACTERS) {
            assertFalse(s.contains(punctuation_character));
        }
    }

    private void assertNotInStopWords(String token) {

        for (Object o : EnglishStopWordCleaner.DEFAULT_STOP_WORDS) {

            String stop_word = String.valueOf((char[]) o);
            assertNotEquals(token, stop_word);
        }
    }

    private void assertNoSuffix(String token) {

        for (String suffix : SUFFIXES) {

            assertFalse(token.endsWith(suffix));
        }
    }

    @Test
    public void unseenCleaned() throws Exception {

        initForcefully();
        loadGoldStandard();
        cleanUsingCombinedCleaner();

        assertRecordsAreCleaned(configuration.requireUnseenRecords());
    }
}
