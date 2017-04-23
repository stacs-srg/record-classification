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

import org.apache.commons.csv.*;
import org.apache.commons.io.*;
import org.junit.*;
import org.junit.rules.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.util.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.dataset.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;

import static java.util.logging.Logger.getLogger;

/**
 * Captures common functionality among {@link Command command} test classes.
 *
 * @author Masih Hajiarab Derkani
 */
public abstract class CommandTest {

    public static final long TEST_SEED = 142L;
    protected Launcher launcher;
    protected Configuration configuration;
    protected Path working_directory;

    private static final Logger LOGGER = getLogger(CommandTest.class.getName());
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {

        launcher = new Launcher();
        configuration = launcher.getConfiguration();
        working_directory = temporary.newFolder().toPath();
        configuration.setWorkingDirectory(working_directory);
    }

    protected void run(Object... args) throws Exception {

        final List<String> arguments = Arrays.asList(args).stream().map(String::valueOf).collect(Collectors.toList());
        LOGGER.info(() -> String.format("running: %s", Arguments.joinWithSpace(arguments)));
        launcher.parse(arguments.toArray(new String[arguments.size()]));
        launcher.run();
    }

    protected void init() throws Exception { new InitCommand.Builder().run(launcher); }

    protected void initForcefully() throws Exception {

        final InitCommand.Builder builder = new InitCommand.Builder();
        builder.setForce(true);
        builder.run(launcher);
    }

    protected void setVerbosity(final LogLevelSupplier supplier) throws Exception {

        final SetCommand.Builder builder = new SetCommand.Builder();
        builder.setVerbosity(supplier);
        builder.run(launcher);
    }

    protected void setSeed() throws Exception {

        final SetCommand.Builder builder = new SetCommand.Builder();
        builder.setSeed(TEST_SEED);
        builder.run(launcher);
    }

    protected void setClassifier(ClassifierSupplier classifier_supplier) throws Exception {

        final SetCommand.Builder builder = new SetCommand.Builder();
        builder.setClassifier(classifier_supplier);
        builder.run(launcher);
    }

    protected void loadGoldStandards(List<TestDataSet> gold_standards, final CharsetSupplier charset, final CSVFormat format, double training_ratio) throws Exception {

        for (TestDataSet gold_standard : gold_standards) {
            loadGoldStandard(gold_standard, charset, format, training_ratio);
        }
    }

    protected void loadUnseens(List<TestDataSet> unseens, final CharsetSupplier charset, final CSVFormat format) throws Exception {

        for (TestDataSet unseen : unseens) {
            loadUnseen(unseen, charset, format);
        }
    }

    protected void evaluate() throws Exception { new EvaluateCommand.Builder().run(launcher); }

    protected void evaluate(final Path output) throws Exception {

        final EvaluateCommand.Builder builder = new EvaluateCommand.Builder();
        builder.setOutput(output);
        builder.run(launcher);
    }

    protected void train() throws Exception {

        final TrainCommand.Builder builder = new TrainCommand.Builder();
        builder.setInternalTrainingRatio(1.0);
        builder.run(launcher);
    }

    protected void clean(CleanerSupplier cleaner) throws Exception {

        final CleanCommand.Builder builder = new CleanCommand.Builder();
        builder.addCleaners(cleaner);
        builder.run(launcher);
    }

    protected Path classify() throws Exception {

        final Path output_path = temporary.newFile().toPath();
        final ClassifyCommand.Builder builder = new ClassifyCommand.Builder();
        builder.setOutputPath(output_path);
        builder.run(launcher);

        return output_path;
    }

    protected void loadGoldStandards(final List<TestDataSet> records, double training_ratio) throws Exception {

        loadGoldStandards(records, TestDataSet.DEFAULT_CHARSET, TestDataSet.DEFAULT_CSV_FORMAT, training_ratio);
    }

    protected void loadGoldStandards(final List<TestDataSet> records) throws Exception {

        loadGoldStandards(records, TestDataSet.DEFAULT_CHARSET, TestDataSet.DEFAULT_CSV_FORMAT, 1.0);
    }

    protected void loadGoldStandard(final TestDataSet records, final CharsetSupplier charset, final CSVFormat format, final double training_ratio) throws Exception {

        final LoadGoldStandardRecordsCommand.Builder gold_standard_builder = new LoadGoldStandardRecordsCommand.Builder();
        gold_standard_builder.setTrainingRatio(training_ratio);
        gold_standard_builder.setClassColumnIndex(records.class_column_index);
        load(gold_standard_builder, records, charset, format);
    }

    protected void loadUnseens(final List<TestDataSet> records) throws Exception {

        loadUnseens(records, TestDataSet.DEFAULT_CHARSET, TestDataSet.DEFAULT_CSV_FORMAT);
    }

    protected void loadUnseen(final TestDataSet records, final CharsetSupplier charset, final CSVFormat format) throws Exception {

        load(new LoadUnseenRecordsCommand.Builder(), records, charset, format);
    }

    protected void load(LoadRecordsCommand.Builder builder, TestDataSet records, final CharsetSupplier charset, final CSVFormat format) throws Exception {

        final Path source = getTestCopy(records, charset, format);
        builder.setIdColumnIndex(records.id_column_index);
        builder.setLabelColumnIndex(records.label_column_index);
        builder.setDelimiter(format.getDelimiter());
        builder.setSkipHeader(format.getSkipHeaderRecord());
        builder.setSource(source);
        builder.setSourceCharset(charset);
        builder.run(launcher);
    }

    protected Path getTestCopy(final TestDataSet records, final CharsetSupplier charset, final CSVFormat format) {

        try {
            final Path destination = temporary.newFile().toPath();
            records.copy(destination, charset.get(), format);
            return destination;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void tearDown() throws Exception {

        LogManager.getLogManager().reset();
    }
}
