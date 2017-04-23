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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.multiple_classifier;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;
import org.apache.commons.csv.CSVPrinter;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.ConfusionMatrix;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.MatchingPrefixConfusionMatrix;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.Metrics;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.StrictConfusionMatrix;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.Classifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.ClassifierSupplier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.util.CombinationGenerator;
import uk.ac.standrews.cs.digitising_scotland.record_classification.util.Combinations;
import uk.ac.standrews.cs.utilities.Formatting;
import uk.ac.standrews.cs.utilities.Logging;
import uk.ac.standrews.cs.utilities.LoggingLevel;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

import static uk.ac.standrews.cs.digitising_scotland.record_classification.util.Combinations.concatenateGenerators;
import static uk.ac.standrews.cs.digitising_scotland.record_classification.util.Combinations.generatorWithTruncatedInput;

enum TextCleanerSupplier implements Supplier<TextCleaner> {

    STOP_WORD(EnglishStopWordCleaner::new),
    PUNCTUATION(PunctuationCleaner::new),
    LOWER_CASE(LowerCaseCleaner::new),
    STEMMING(PorterStemCleaner::new),
    ALL(() -> new EnglishStopWordCleaner().andThen(new PunctuationCleaner()).andThen(new LowerCaseCleaner()).andThen(new PorterStemCleaner()));

    private final Supplier<TextCleaner> supplier;

    TextCleanerSupplier(final Supplier<TextCleaner> supplier) { this.supplier = supplier; }

    @Override
    public TextCleaner get() { return supplier.get(); }
}

enum TokenCombinationGeneratorSupplier implements Supplier<CombinationGenerator<String>> {
    POWER_SET(Combinations::<String>powerSetGenerator),
    PERMUTATIONS(Combinations::<String>permutationsGenerator),
    ALL(Combinations::<String>allGenerator),

    TRUNCATED_6_ALL(() -> generatorWithTruncatedInput(6, Combinations.<String>allGenerator())),
    TRUNCATED_7_ALL(() -> generatorWithTruncatedInput(7, Combinations.<String>allGenerator())),
    TRUNCATED_8_ALL(() -> generatorWithTruncatedInput(8, Combinations.<String>allGenerator())),
    TRUNCATED_9_ALL(() -> generatorWithTruncatedInput(9, Combinations.<String>allGenerator())),
    TRUNCATED_10_ALL(() -> generatorWithTruncatedInput(10, Combinations.<String>allGenerator())),
    TRUNCATED_11_ALL(() -> generatorWithTruncatedInput(11, Combinations.<String>allGenerator())),
    TRUNCATED_12_ALL(() -> generatorWithTruncatedInput(12, Combinations.<String>allGenerator())),

    TRUNCATED_6_ALL_WITH_POWER_SET(() -> concatenateGenerators(generatorWithTruncatedInput(6, Combinations.<String>allGenerator()), Combinations.<String>powerSetGenerator())),
    TRUNCATED_7_ALL_WITH_POWER_SET(() -> concatenateGenerators(generatorWithTruncatedInput(7, Combinations.<String>allGenerator()), Combinations.<String>powerSetGenerator())),
    TRUNCATED_8_ALL_WITH_POWER_SET(() -> concatenateGenerators(generatorWithTruncatedInput(8, Combinations.<String>allGenerator()), Combinations.<String>powerSetGenerator())),
    TRUNCATED_9_ALL_WITH_POWER_SET(() -> concatenateGenerators(generatorWithTruncatedInput(9, Combinations.<String>allGenerator()), Combinations.<String>powerSetGenerator())),
    TRUNCATED_10_ALL_WITH_POWER_SET(() -> concatenateGenerators(generatorWithTruncatedInput(10, Combinations.<String>allGenerator()), Combinations.<String>powerSetGenerator())),
    TRUNCATED_11_ALL_WITH_POWER_SET(() -> concatenateGenerators(generatorWithTruncatedInput(11, Combinations.<String>allGenerator()), Combinations.<String>powerSetGenerator())),
    TRUNCATED_12_ALL_WITH_POWER_SET(() -> concatenateGenerators(generatorWithTruncatedInput(12, Combinations.<String>allGenerator()), Combinations.<String>powerSetGenerator()));

    private final Supplier<CombinationGenerator<String>> supplier;

    TokenCombinationGeneratorSupplier(final Supplier<CombinationGenerator<String>> supplier) { this.supplier = supplier; }

    @Override
    public CombinationGenerator<String> get() { return supplier.get(); }
}

/**
 * @author Masih Hajiarab Derkani
 */
public class MultipleClassifierExperiment implements Runnable {

    private static final long DEFAULT_RANDOM_SEED = 1413;
    private static final Charset DEFAULT_DESTINATION_CHARSET = StandardCharsets.UTF_8;
    private static final int ID_COLUMN_INDEX = 0;
    private static final int DATA_COLUMN_INDEX = 1;

    private final MultipleClassifier multiple_classifier;
    private final Random random;
    private final DataSet classified_records;
    private final DataSet training;
    private final DataSet gold_standard;
    private final JCommander commander;
    private final Classifier core_classifier;
    private final TextCleaner text_cleaner;
    private final CombinationGenerator<String> token_combination_generator;

    private CSVPrinter csv_printer;

    @Parameter(names = "-t", description = "The data set to be used for training the core classifier.", required = true, converter = FileConverter.class)
    private File training_file;

    @Parameter(names = "-g", description = "The data set containing the ground truth about multiple classifications.", required = true, converter = FileConverter.class)
    private File gold_standard_file;

    @Parameter(names = "-c", description = "The core classifier to use as part of multiple classification.", required = true)
    private ClassifierSupplier core_classifier_supplier;

    @Parameter(names = "-tr", description = "The classification confidence threshold.", required = true)
    private double classification_confidence_threshold;

    @Parameter(names = "-p", description = "The cleaner to use for cleaning data prior to classification.")
    private TextCleanerSupplier text_cleaner_supplier = TextCleanerSupplier.ALL;

    @Parameter(names = "-f", description = "The data token combination generator.", required = true)
    private TokenCombinationGeneratorSupplier token_combination_generator_supplier;

    @Parameter(names = "-s", description = "Random seed")
    private long random_seed = DEFAULT_RANDOM_SEED;

    @Parameter(names = "-d", description = "The path to the file in which to store the classified data.", required = true, converter = FileConverter.class)
    private File destination;

    @Parameter(names = "-m", description = "The max length of matching prefix classification metrics. If zero, no matching prefix metrics are printed.")
    private int max_matching_prefix_metrics_length = 0;

    @Parameter(names = "-v", description = "Logging verbosity.")
    private LoggingLevel verbosity = LoggingLevel.VERBOSE;

    private MultipleClassifierExperiment(String... args) throws IOException {

        commander = new JCommander(this);

        parseCommandLineArguments(args);

        random = new Random(random_seed);
        training = new DataSet(training_file.toPath());
        gold_standard = removeDuplicateData(new DataSet(gold_standard_file.toPath()));
        classified_records = new DataSet(gold_standard.getColumnLabels());
        core_classifier = core_classifier_supplier.get();
        text_cleaner = text_cleaner_supplier.get();
        token_combination_generator = token_combination_generator_supplier.get();
        multiple_classifier = new MultipleClassifier(core_classifier, classification_confidence_threshold, text_cleaner, MultipleClassifier.NOT_EQUAL_ONE_ANOTHER, token_combination_generator);
        Logging.setLoggingLevel(verbosity);
    }

    private DataSet removeDuplicateData(final DataSet source) {

        Logging.output(LoggingLevel.VERBOSE, "Removing duplicate data records from gold standard...");
        final DataSet unique = new DataSet(source.getColumnLabels());
        final Set<String> unique_data = new HashSet<>();

        final List<List<String>> source_records = source.getRecords();
        source_records.forEach(row -> {
            final String data = row.get(1);
            if (!unique_data.contains(data)) {
                unique_data.add(data);
                unique.addRow(row);
            }
        });

        Logging.output(LoggingLevel.VERBOSE, String.format("Removed %d duplicate data records from gold standard", source_records.size() - unique.getRecords().size()));
        return unique;
    }

    private void parseCommandLineArguments(final String[] args) {

        try {
            commander.parse(args);
        }
        catch (ParameterException e) {
            System.err.println(e.getMessage());
            commander.usage();
            System.exit(1);
        }
    }

    public static void main(String... args) throws IOException {

        final MultipleClassifierExperiment experiment = new MultipleClassifierExperiment(args);
        experiment.run();
    }

    @Override
    public void run() {

        logParameters();
        trainCoreClassifier();
        classifyAndPersistResults();
        logClassificationMetrics();
    }

    private void logClassificationMetrics() {

        logStrictClassificationMetrics();
        logMatchingPrefixClassificationMetrics();
    }

    private void logMatchingPrefixClassificationMetrics() {

        if (max_matching_prefix_metrics_length > 0) {
            for (int matching_prefix_length = 1; matching_prefix_length <= max_matching_prefix_metrics_length; matching_prefix_length++) {

                printMetrics(new MatchingPrefixConfusionMatrix(matching_prefix_length, classified_records, gold_standard));
            }
        }
    }

    private void logStrictClassificationMetrics() {

        printMetrics(new StrictConfusionMatrix(classified_records, gold_standard));
    }

    private void printMetrics(final ConfusionMatrix confusion_matrix) {

        Logging.output(LoggingLevel.VERBOSE, String.format("Classification metrics by: %s", confusion_matrix.toString()));
        new Metrics(confusion_matrix).printMetrics();
        Logging.output(LoggingLevel.VERBOSE, "");
    }

    private void logParameters() {

        Logging.output(LoggingLevel.VERBOSE, String.format("Core classifier: %s", core_classifier.getName()));
        Logging.output(LoggingLevel.VERBOSE, String.format("Classification confidence threshold: %f", classification_confidence_threshold));
        Logging.output(LoggingLevel.VERBOSE, String.format("Pre-classification data cleaner: %s", String.valueOf(text_cleaner_supplier)));
        Logging.output(LoggingLevel.VERBOSE, String.format("Training dataset: %s", String.valueOf(training_file)));
        Logging.output(LoggingLevel.VERBOSE, String.format("Gold standard dataset: %s", String.valueOf(gold_standard_file)));
    }

    private void classifyAndPersistResults() {

        final int gold_standard_size = gold_standard.getRecords().size();
        Logging.initialiseProgressIndicator(gold_standard_size);
        Logging.output(LoggingLevel.VERBOSE, String.format("Classifying %d records...", gold_standard_size));

        try {
            initCSVResultPrinter();

            final Instant start = Instant.now();
            for (List<String> cells : gold_standard.getRecords()) {

                final String id = cells.get(ID_COLUMN_INDEX);
                final String data = cells.get(DATA_COLUMN_INDEX);
                final List<Classification> classifications = multiple_classifier.classify(data);
                final List<String> result_row = toDataSetRow(id, data, classifications);
                classified_records.addRow(result_row);
                persistClassificationResult(result_row);
                Logging.progressStep(LoggingLevel.VERBOSE);
            }
            Logging.output(LoggingLevel.VERBOSE, "Done classifying records in " + Formatting.format(Duration.between(start, Instant.now())));
        }
        finally {
            destroyCSVResultPrinter();
        }
    }

    private void destroyCSVResultPrinter() {

        if (csv_printer != null) {
            try {
                csv_printer.flush();
            }
            catch (IOException e) {
                Logging.output(LoggingLevel.LONG_SUMMARY, "failed to flush csv result printer before closing");
            }
            finally {
                closeCSVResultPrinter();
            }
        }
    }

    private void closeCSVResultPrinter() {

        try {
            csv_printer.close();
        }
        catch (IOException e) {
            Logging.output(LoggingLevel.LONG_SUMMARY, "failed to close csv result printer");
        }
    }

    private void persistClassificationResult(List<String> row) {

        try {
            csv_printer.printRecord(row);
            csv_printer.flush();
        }
        catch (IOException e) {
            throw new RuntimeException("failed to write result row " + row, e);
        }
    }

    private void initCSVResultPrinter() {

        try {
            final BufferedWriter out = Files.newBufferedWriter(destination.toPath(), DEFAULT_DESTINATION_CHARSET, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            final List<String> labels = gold_standard.getColumnLabels();
            final String[] header_array = labels.toArray(new String[labels.size()]);
            csv_printer = new CSVPrinter(out, DataSet.DEFAULT_CSV_FORMAT.withHeader(header_array));
        }
        catch (IOException e) {
            throw new RuntimeException("failed to initialise csv result printer.", e);
        }
    }

    private List<String> toDataSetRow(final String id, final String data, final List<Classification> classifications) {

        final List<String> row = new ArrayList<>();
        row.add(id);
        row.add(data);
        classifications.stream().map(Classification::getCode).forEach(row::add);
        return row;
    }

    private void trainCoreClassifier() {

        final Bucket consistent_cleaned_bucket = getTrainingBucket();
        Logging.output(LoggingLevel.VERBOSE, String.format("Training core classifier with %d records...", consistent_cleaned_bucket.size()));
        final Instant start = Instant.now();
        core_classifier.trainAndEvaluate(consistent_cleaned_bucket, 1.0, random);
        Logging.output(LoggingLevel.VERBOSE, "Done training core classifier in " + Formatting.format(Duration.between(start, Instant.now())));
    }

    private Bucket getTrainingBucket() {

        // TODO tidy up cleaning before training.
        Logging.output(LoggingLevel.VERBOSE, String.format("Cleaning %d records prior to training core classifier...", training.getRecords().size()));
        final Instant start = Instant.now();

        final Bucket training_bucket = new Bucket(training);
        final Bucket cleaned_bucket = new Bucket();
        training_bucket.forEach(record -> cleaned_bucket.add(text_cleaner.cleanRecord(record)));
        final Bucket consistent_cleaned_training_bucket = ConsistentClassificationCleaner.CORRECT.apply(Collections.singletonList(cleaned_bucket)).get(0);

        Logging.output(LoggingLevel.VERBOSE, "Done cleaning training records in " + Formatting.format(Duration.between(start, Instant.now())));

        return consistent_cleaned_training_bucket;
    }
}
