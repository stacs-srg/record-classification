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
package uk.ac.standrews.cs.digitising_scotland.record_classification.experiments.generic;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.PathConverter;
import com.beust.jcommander.Parameter;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.Metrics;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.Classifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.Cleaner;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.CleanerSupplier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Launcher;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.command.SetCommand;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.util.Validators;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.InputFileFormatException;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.ClassificationContext;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.ClassificationProcess;
import uk.ac.standrews.cs.utilities.Logging;
import uk.ac.standrews.cs.utilities.LoggingLevel;
import uk.ac.standrews.cs.utilities.dataset.DataSet;
import uk.ac.standrews.cs.utilities.tables.TableGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Graham Kirby
 * @author Masih Hajiarab Derkani
 */
public abstract class Experiment implements Callable<Void> {

    static {
        Launcher.setCLISystemProperties();
    }

    private static final long SEED = 3249870987977239578L;
    private static final String FIRST_COLUMN_HEADING = "classifier";

    private static final String DEFAULT_GOLD_STANDARD_PATH = "src/test/resources/uk/ac/standrews/cs/digitising_scotland/record_classification/experiments/AbstractClassificationProcessTest/coded_data_1K.csv";
    public static final double DEFAULT_TRAINING_RATIO = 0.8;
    private static final int DEFAULT_REPETITIONS = 2;
    private static final LoggingLevel DEFAULT_VERBOSITY = LoggingLevel.LONG_SUMMARY;

    private static final List<Boolean> COLUMNS_AS_PERCENTAGES = Arrays.asList(true, true, true, true, false, false);
    private static final double ONE_MINUTE_IN_SECONDS = 60.0;

    private static final String DESCRIPTION_GOLD_STANDARD = "Path to a file containing the three column gold standard.";
    private static final String DESCRIPTION_REPETITION = "The number of repetitions.";
    private static final String DESCRIPTION_VERBOSITY = "The level of output verbosity.";
    private static final String DESCRIPTION_RATIO = "The ratio of gold standard records to be used for training. The value must be between 0.0 to 1.0 (inclusive).";

    private static final String INCONSISTENT_PARAM_NUMBERS_ERROR_MESSAGE = "the number of gold standard files must be equal to the number of training ratios";
    private static final String TABLE_CAPTION_STRING = "\naggregate classifier performance (%d repetition%s):\n";
    private static final String TRAINING_TIME_HEADER = "training time (m)";
    private static final String EVALUATION_TIME_HEADER = "evaluation time (m)";

    private final JCommander commander;
    private Collection<ClassificationProcess> processes;

    // TODO collect all JCommander flag definitions into one class and rationalise.

    @Parameter(names = {"-v", "--verbosity"}, description = DESCRIPTION_VERBOSITY)
    protected LoggingLevel verbosity = DEFAULT_VERBOSITY;

    @Parameter(names = {"-r", "--repetitionCount"}, description = DESCRIPTION_REPETITION)
    protected int repetitions = DEFAULT_REPETITIONS;

    @Parameter(names = {"-g", "--goldStandard"}, description = DESCRIPTION_GOLD_STANDARD, listConverter = PathConverter.class)
    protected List<Path> gold_standard_files = Collections.singletonList(Paths.get(DEFAULT_GOLD_STANDARD_PATH));

    @Parameter(names = {"-t", "--trainingRecordRatio"}, description = DESCRIPTION_RATIO)
    protected List<Double> training_ratios = Collections.singletonList(DEFAULT_TRAINING_RATIO);

    @Parameter(names = {SetCommand.OPTION_INTERNAL_TRAINING_RATIO_SHORT, SetCommand.OPTION_INTERNAL_TRAINING_RATIO_LONG}, description = "the ratio of training records to be used for internal training", validateValueWith = Validators.BetweenZeroToOneInclusive.class)
    protected double internal_training_ratio = DEFAULT_TRAINING_RATIO;

    public static final List<Cleaner> CLEANERS = Collections.singletonList(CleanerSupplier.COMBINED.get());

    protected Experiment() throws IOException, InputFileFormatException {

        commander = new JCommander(this);
    }

    protected Experiment(final String[] args) throws IOException, InputFileFormatException {

        this();

        try {
            parse(args);

        }
        catch (ParameterException e) {
            exitWithErrorMessage(e.getMessage());
        }
    }

    @Override
    public Void call() throws Exception {

        Logging.setLoggingLevel(verbosity);

        final List<ClassifierResults> results = runExperiment();
        printSummarisedResults(results);

        return null; //void callable
    }

    public void setRepetitions(final int repetitions) {

        this.repetitions = repetitions;
    }

    public void setGoldStandardFiles(final List<Path> gold_standard_files) {

        this.gold_standard_files = gold_standard_files;
    }

    public void setTrainingRatios(final List<Double> training_ratios) {

        this.training_ratios = training_ratios;
    }

    public List<ClassifierResults> runExperiment() throws Exception {

        final List<Supplier<Classifier>> classifier_factories = getClassifierFactories();

        final Map<Supplier<Classifier>, ClassificationProcess> process_map = makeProcessMap(classifier_factories);
        final Map<Supplier<Classifier>, ClassifierResults> result_map = makeResultsMap(classifier_factories);
        final Map<Supplier<Classifier>, Random> random_map = makeRandomMap(classifier_factories);

        // Loop nesting is this way round to avoid performing all repetitions of a given process consecutively, given
        // that timing information is being recorded.
        for (int repetition_index = 0; repetition_index < repetitions; repetition_index++) {

            for (Supplier<Classifier> factory : classifier_factories) {

                final ClassificationProcess process = process_map.get(factory);
                final ClassifierResults result = result_map.get(factory);
                final Random random = random_map.get(factory);

                final Classifier classifier = factory.get();
                final ClassificationContext context = new ClassificationContext(classifier, random);

                runRepetition(process, context, repetition_index);

                result.data_set.addRow(getResultValuesWithTimings(context));
                result.contexts.add(context);
            }
        }

        processes = process_map.values();

        return new ArrayList<>(result_map.values());
    }

    protected void runRepetition(final ClassificationProcess process, final ClassificationContext context, final int repetition_index) throws Exception {

        process.call(context);
    }

    public Collection<ClassificationProcess> getProcesses() {

        return processes;
    }

    protected abstract List<Supplier<Classifier>> getClassifierFactories() throws IOException, InputFileFormatException;

    protected ClassificationProcess makeClassificationProcess(final Supplier<Classifier> factory) {

        final EvaluationExperimentProcess process = new EvaluationExperimentProcess();

        process.setGoldStandardFiles(gold_standard_files);
        process.setTrainingRatios(training_ratios);
        process.setCleaners(CLEANERS);
        process.setInternalTrainingRatio(internal_training_ratio);

        process.configureSteps();

        return process;
    }

    private List<String> getResultValuesWithTimings(final ClassificationContext context) {

        final Metrics classificationMetrics = context.getClassificationMetrics();
        final List<String> values = classificationMetrics.getValues();

        values.add(String.valueOf(context.getTrainingTime().getSeconds() / ONE_MINUTE_IN_SECONDS));
        values.add(String.valueOf(context.getEvaluationClassificationTime().getSeconds() / ONE_MINUTE_IN_SECONDS));

        return values;
    }

    private Map<Supplier<Classifier>, Random> makeRandomMap(final List<Supplier<Classifier>> factories) {

        return factories.stream().collect(Collectors.toMap(Function.identity(), this::makeRandom));
    }

    private Map<Supplier<Classifier>, ClassifierResults> makeResultsMap(final List<Supplier<Classifier>> factories) {

        return factories.stream().collect(Collectors.toMap(Function.identity(), this::makeClassifierResults));
    }

    private Map<Supplier<Classifier>, ClassificationProcess> makeProcessMap(final List<Supplier<Classifier>> factories) {

        return factories.stream().collect(Collectors.toMap(Function.identity(), this::makeClassificationProcess));
    }

    private Random makeRandom(final Supplier<Classifier> factory) {

        return new Random(SEED);
    }

    private ClassifierResults makeClassifierResults(final Supplier<Classifier> factory) {

        return new ClassifierResults(factory.get().getName(), new DataSet(getDataSetLabelsWithTimingColumns()));
    }

    private List<String> getDataSetLabelsWithTimingColumns() {

        final List<String> labels = new ArrayList<>(Metrics.DATASET_LABELS);
        labels.add(TRAINING_TIME_HEADER);
        labels.add(EVALUATION_TIME_HEADER);
        return labels;
    }

    private void parse(final String[] args) {

        commander.parse(args);

        if (gold_standard_files.size() != training_ratios.size()) {
            throw new ParameterException(INCONSISTENT_PARAM_NUMBERS_ERROR_MESSAGE);
        }
    }

    private void exitWithErrorMessage(final String error_message) {

        System.err.println(error_message);
        commander.usage();
        System.exit(1);
    }

    protected void printSummarisedResults(final List<ClassifierResults> results) throws IOException {

        final String table_caption = String.format(TABLE_CAPTION_STRING, repetitions, getPluralitySuffix(repetitions));
        final TableGenerator table_generator = new TableGenerator(getDataSets(results), FIRST_COLUMN_HEADING, getNames(results), COLUMNS_AS_PERCENTAGES);

        printDateStamp();
        printExperimentInputs();
        System.out.println(table_caption);
        table_generator.getTable().print(System.out);
    }

    private static String getPluralitySuffix(final int repetitions) {

        return repetitions > 1 ? "s" : "";
    }

    private List<String> getNames(final List<ClassifierResults> results) {

        return results.stream().map(result -> result.name).collect(Collectors.toList());
    }

    private List<DataSet> getDataSets(final List<ClassifierResults> results) {

        return results.stream().map(result -> result.data_set).collect(Collectors.toList());
    }

    private void printDateStamp() {

        System.out.println("experiment run at: " + new Date());
        System.out.println();
    }

    private void printExperimentInputs() {

        System.out.println("internal training ratio: " + internal_training_ratio);
        System.out.println("gold standard files (training ratios): ");
        System.out.println();

        for (int i = 0; i < gold_standard_files.size(); i++) {

            System.out.println(gold_standard_files.get(i).getFileName() + " (" + training_ratios.get(i) + ")");
        }
    }

    public class ClassifierResults {

        final String name;
        final DataSet data_set;
        final List<ClassificationContext> contexts;

        public ClassifierResults(String name, DataSet data_set) {

            this.name = name;
            this.data_set = data_set;
            contexts = new ArrayList<>();
        }

        public List<ClassificationContext> getContexts() {

            return contexts;
        }
    }
}
