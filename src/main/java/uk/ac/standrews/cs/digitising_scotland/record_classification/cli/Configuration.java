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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cli;

import com.beust.jcommander.*;
import com.fasterxml.jackson.databind.*;
import org.apache.commons.csv.*;
import org.apache.commons.io.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.command.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.serialization.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.serialization.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.stream.*;

/**
 * The Command Line Interface configuration.
 *
 * @author Masih Hajiarab Derkani
 */
public class Configuration extends ClassificationContext {

    // TODO feature: add parent config loading from user.home if exists.
    // TODO why does this extend ClassificationContext?

    /** Name of record classification CLI program. */
    public static final String PROGRAM_NAME = "classli";

    /** The name of the folder that contains the persisted state of this program. */
    public static final String HOME_NAME = "." + Configuration.PROGRAM_NAME;

    /** The name of the resource bundle containing the command line interface messages. */
    public static final String RESOURCE_BUNDLE_NAME = "uk.ac.standrews.cs.digitising_scotland.record_classification.cli.CLIMessages";

    /** Format of the {@link Record records} stored in a CSV file by the command line interface. */
    public static final CSVFormat RECORD_CSV_FORMAT = CSVFormat.RFC4180.withHeader("ID", "DATA", "ORIGINAL_DATA", "CODE", "CONFIDENCE", "DETAIL");

    /** The character encoding by which the state of the command line interface is persisted. */
    public static final Charset RESOURCE_CHARSET = StandardCharsets.UTF_8;

    /** */
    public static final CharsetSupplier DEFAULT_CHARSET_SUPPLIER = CharsetSupplier.SYSTEM_DEFAULT;
    public static final SerializationFormat DEFAULT_CLASSIFIER_SERIALIZATION_FORMAT = SerializationFormat.JAVA_SERIALIZATION;

    /** The default working directory of the command line interface. */
    public static final Path DEFAULT_WORKING_DIRECTORY = Paths.get(System.getProperty("user.dir"));

    /** The default ratio of records to be used for training of the classifier. **/
    public static final double DEFAULT_TRAINING_RATIO = 0.8;

    /** The default ratio of records to be used for internal training of the classifier. **/
    public static final double DEFAULT_INTERNAL_TRAINING_RATIO = DEFAULT_TRAINING_RATIO;

    protected static final CsvFormatSupplier DEFAULT_CSV_FORMAT_SUPPLIER = CsvFormatSupplier.DEFAULT;
    protected static final Character DEFAULT_DELIMITER = DEFAULT_CSV_FORMAT_SUPPLIER.get().getDelimiter();
    protected static final LogLevelSupplier DEFAULT_LOG_LEVEL_SUPPLIER = LogLevelSupplier.INFO;

    private static final long serialVersionUID = 5386411103557347275L;
    private static final String CONFIG_FILE_NAME = "config.json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

    static {
        MAPPER.registerModule(new ClassliModule());
    }

    private CharsetSupplier default_charset_supplier = DEFAULT_CHARSET_SUPPLIER;
    private Character default_delimiter = DEFAULT_DELIMITER;
    private double default_training_ratio = DEFAULT_TRAINING_RATIO;
    private double default_internal_training_ratio = DEFAULT_INTERNAL_TRAINING_RATIO;
    private CsvFormatSupplier default_csv_format_supplier = DEFAULT_CSV_FORMAT_SUPPLIER;
    private Long seed;
    private ClassifierSupplier classifier_supplier;
    private SerializationFormat classifier_serialization_format = DEFAULT_CLASSIFIER_SERIALIZATION_FORMAT;
    private LogLevelSupplier log_level = DEFAULT_LOG_LEVEL_SUPPLIER;
    private LogLevelSupplier internal_log_level = DEFAULT_LOG_LEVEL_SUPPLIER;

    private transient FileHandler internal_log_handler;
    private transient Path working_directory = DEFAULT_WORKING_DIRECTORY;

    // Loaders of lazily loaded field values
    private transient Supplier<Classifier> classifier_loader;
    private transient Supplier<Bucket> training_records_loader;
    private transient Supplier<Bucket> evaluation_records_loader;
    private transient Supplier<Bucket> unseen_records_loader;
    private transient Supplier<Bucket> classified_evaluation_records_loader;
    private transient Supplier<Bucket> classified_unseen_records_loader;
    private transient Supplier<ConfusionMatrix> confusion_matrix_loader;
    private transient Supplier<Metrics> classification_metrics_loader;

    public Configuration() {

        this(DEFAULT_WORKING_DIRECTORY);
    }

    public Configuration(Path working_directory) {

        setWorkingDirectory(working_directory);
    }

    public void init() throws IOException {

        final Path internal_log_home = getInternalLogsHome();

        InitCommand.assureDirectoryExists(getHome());
        InitCommand.assureDirectoryExists(internal_log_home);

        if (internal_log_handler != null) {
            internal_log_handler.close();
            Logger.getGlobal().removeHandler(internal_log_handler);
        }
        internal_log_handler = new FileHandler(internal_log_home.resolve("classli_%u.log").toString(), 50000, 1, false);
        internal_log_handler.setEncoding(StandardCharsets.UTF_8.name());
        internal_log_handler.setLevel(internal_log_level.get());
        Logger.getGlobal().addHandler(internal_log_handler);

        clearLazyLoaders();
    }

    private void clearLazyLoaders() {

        classifier_loader = null;
        training_records_loader = null;
        evaluation_records_loader = null;
        unseen_records_loader = null;
        classified_evaluation_records_loader = null;
        classified_unseen_records_loader = null;
        confusion_matrix_loader = null;
        classification_metrics_loader = null;
    }

    public Path getInternalLogsHome() {

        return getHome().resolve("logs");
    }

    public static Configuration load() throws IOException {

        return load(DEFAULT_WORKING_DIRECTORY);
    }

    public static Configuration load(Path working_directory) {

        final Path home = getHome(working_directory);
        final Path config_file = getConfigurationFile(home);

        try (final BufferedReader in = Files.newBufferedReader(config_file)) {
            final Configuration configuration = MAPPER.readValue(in, Configuration.class);
            configuration.setWorkingDirectory(working_directory);
            return configuration;
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load configuration from" + home + ", cause: " + e.getMessage(), e);
        }
    }

    private static Path getHome(final Path working_directory) {

        return working_directory.resolve(HOME_NAME);
    }

    private static Path getConfigurationFile(final Path home) {

        return home.resolve(CONFIG_FILE_NAME);
    }

    public static void persistBucketAsCSV(Bucket bucket, Path destination, CSVFormat format, Charset charset) throws IOException {

        if (bucket != null) {

            try (final BufferedWriter out = Files.newBufferedWriter(destination, charset)) {

                final CSVPrinter printer = format.print(out);

                for (Record record : bucket) {

                    final Classification classification = record.getClassification();

                    printer.print(record.getId());
                    printer.print(record.getData());
                    printer.print(record.getOriginalData());
                    printer.print(classification.getCode());
                    printer.print(classification.getConfidence());
                    printer.print(classification.getDetail());
                    printer.println();
                }
            }
        }
    }

    public static Bucket loadBucket(final Path source) throws IOException {

        final Bucket bucket = new Bucket();

        try (final BufferedReader in = Files.newBufferedReader(source, RESOURCE_CHARSET)) {
            final CSVParser parser = RECORD_CSV_FORMAT.withSkipHeaderRecord().parse(in);
            StreamSupport.stream(parser.spliterator(), false).map(Configuration::toRecord).forEach(bucket::add);
        }
        return bucket;
    }

    private static Record toRecord(final CSVRecord csv_record) {

        final int id = Integer.parseInt(csv_record.get(0));
        final String label = csv_record.get(1);
        final String label_original = csv_record.get(2);
        final String code = csv_record.get(3);
        final double confidence = Double.parseDouble(csv_record.get(4));
        final String details = csv_record.get(5);

        final Classification classification = new Classification(code, new TokenList(label), confidence, details);

        return new Record(id, label, label_original, classification);
    }

    static boolean exists(final Path working_directory) {

        return Files.isRegularFile(Configuration.getConfigurationFile(getHome(working_directory)));
    }

    public void setClassificationMetricsLazyLoader(final Supplier<Metrics> classification_metrics_loader) {

        this.classification_metrics_loader = classification_metrics_loader;
    }

    public void setClassifiedEvaluationRecordsLazyLoader(final Supplier<Bucket> classified_evaluation_records_loader) {

        this.classified_evaluation_records_loader = classified_evaluation_records_loader;
    }

    public void setClassifiedUnseenRecordsLazyLoader(final Supplier<Bucket> classified_unseen_records_loader) {

        this.classified_unseen_records_loader = classified_unseen_records_loader;
    }

    public void setClassifierLazyLoader(final Supplier<Classifier> classifier_loader) {

        this.classifier_loader = classifier_loader;
    }

    public void setTrainingRecordsLazyLoader(final Supplier<Bucket> training_records_loader) {

        this.training_records_loader = training_records_loader;
    }

    public void setUnseenRecordsLazyLoader(final Supplier<Bucket> unseen_records_loader) {

        this.unseen_records_loader = unseen_records_loader;
    }

    public void setEvaluationRecordsLazyLoader(final Supplier<Bucket> evaluation_records_loader) {

        this.evaluation_records_loader = evaluation_records_loader;
    }

    public void setConfusionMatrixLazyLoader(final Supplier<ConfusionMatrix> confusion_matrix_loader) {

        this.confusion_matrix_loader = confusion_matrix_loader;
    }

    @Override
    public Bucket getUnseenRecords() {

        loadLazily(unseen_records_loader, this::setUnseenRecords, isUnseenRecordsSet(), "unseen records");
        return super.getUnseenRecords();
    }

    @Override
    public Bucket getEvaluationRecords() {

        loadLazily(evaluation_records_loader, this::setEvaluationRecords, isEvaluationRecordsSet(), "evaluation records");
        return super.getEvaluationRecords();
    }

    @Override
    public Bucket getClassifiedEvaluationRecords() {

        loadLazily(classified_evaluation_records_loader, this::setClassifiedEvaluationRecords, isClassifiedEvaluationRecordsSet(), "classified evaluation records");
        return super.getClassifiedEvaluationRecords();
    }

    @Override
    public Bucket getClassifiedUnseenRecords() {

        loadLazily(classified_unseen_records_loader, this::setClassifiedUnseenRecords, isClassifiedUnseenRecordsSet(), "classified unseen records");
        return super.getClassifiedUnseenRecords();
    }

    @Override
    public Bucket getTrainingRecords() {

        loadLazily(training_records_loader, this::setTrainingRecords, isTrainingRecordsSet(), "training records");
        return super.getTrainingRecords();
    }

    @Override
    public Classifier getClassifier() {

        loadLazily(classifier_loader != null ? classifier_loader : classifier_supplier, this::setClassifier, isClassifierSet(), "classifier");
        return super.getClassifier();
    }

    private <Value> void loadLazily(Supplier<Value> loader, Consumer<Value> setter, boolean already_set, String parameter_name) {

        if (!already_set && loader != null) {
            LOGGER.info(() -> String.format("loading %s...", parameter_name));
            setter.accept(loader.get());
        }
    }

    @Override
    public void setClassifier(final Classifier classifier) {

        super.setClassifier(classifier);
    }

    @Override
    public Metrics getClassificationMetrics() {

        loadLazily(classification_metrics_loader, this::setClassificationMetrics, isClassificationMetricsSet(), "classification metrics");

        return super.getClassificationMetrics();
    }

    @Override
    public ConfusionMatrix getConfusionMatrix() {

        loadLazily(confusion_matrix_loader, this::setConfusionMatrix, isConfusionMatrixSet(), "confusion matrix");
        return super.getConfusionMatrix();
    }

    public Path getWorkingDirectory() {

        return working_directory;
    }

    public void setWorkingDirectory(final Path working_directory) {

        Objects.requireNonNull(working_directory);

        final Path new_home = getHome(working_directory);
        final Path current_home = getHome();
        if (!Files.isDirectory(new_home)) {

            if (Files.isDirectory(current_home)) {
                try {
                    FileUtils.copyDirectory(current_home.toFile(), new_home.toFile());
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        this.working_directory = working_directory;

    }

    public Path getEvaluationRecordsPath() {

        return getHome().resolve("evaluation.csv");
    }

    public Path getHome() {

        return getHome(working_directory);
    }

    public SerializationFormat getClassifierSerializationFormat() {

        return classifier_serialization_format;
    }

    public void setClassifierSerializationFormat(final SerializationFormat serialization_format) {

        this.classifier_serialization_format = serialization_format;
    }

    public CsvFormatSupplier getDefaultCsvFormatSupplier() {

        return default_csv_format_supplier;
    }

    public void setDefaultCsvFormatSupplier(final CsvFormatSupplier default_csv_format_supplier) {

        this.default_csv_format_supplier = default_csv_format_supplier;
    }

    public CharsetSupplier getDefaultCharsetSupplier() {

        return default_charset_supplier;
    }

    public void setDefaultCharsetSupplier(final CharsetSupplier default_charset_supplier) {

        this.default_charset_supplier = default_charset_supplier;
    }

    public Character getDefaultDelimiter() {

        return default_delimiter;
    }

    public void setDefaultDelimiter(final Character default_delimiter) {

        this.default_delimiter = default_delimiter;
    }

    public Classifier requireClassifier() {

        return getClassifierOptional().orElseThrow(() -> getMissingParameterException("classifier"));
    }

    private ParameterException getMissingParameterException(final String parameter_name) {

        return new ParameterException(String.format("%s required but not set; please specify %s.", parameter_name, parameter_name));
    }

    public Bucket requireGoldStandardRecords() {

        return getGoldStandardRecordsOptional().orElseThrow(() -> getMissingParameterException("gold standard records"));
    }

    public Bucket requireEvaluationRecords() {

        return getEvaluationRecordsOptional().orElseThrow(() -> getMissingParameterException("evaluation records"));
    }

    public Bucket requireTrainingRecords() {

        return getTrainingRecordsOptional().orElseThrow(() -> getMissingParameterException("training records"));
    }

    public Bucket requireUnseenRecords() {

        return getUnseenRecordsOptional().orElseThrow(() -> getMissingParameterException("unseen records"));
    }

    public ClassifierSupplier getClassifierSupplier() {

        return classifier_supplier;
    }

    public void setClassifierSupplier(final ClassifierSupplier classifier_supplier) {

        this.classifier_supplier = classifier_supplier;
    }

    public void persist() throws IOException {

        try (final OutputStream out = Files.newOutputStream(getConfigurationFile(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            MAPPER.writerWithDefaultPrettyPrinter().writeValue(out, this);
            out.flush();
        }
    }

    public Path getConfigurationFile() {

        return getConfigurationFile(getHome());
    }

    public double getDefaultInternalTrainingRatio() {

        return default_internal_training_ratio;
    }

    public void setDefaultInternalTrainingRatio(final double default_internal_training_ratio) {

        this.default_internal_training_ratio = default_internal_training_ratio;
    }

    public double getDefaultTrainingRatio() {

        return default_training_ratio;
    }

    public void setDefaultTrainingRatio(final double default_training_ratio) {

        this.default_training_ratio = default_training_ratio;
    }

    public Long getSeed() {

        return seed;
    }

    public void setSeed(final Long seed) {

        this.seed = seed;
        resetRandom();
    }

    private void resetRandom() {

        if (isSeeded()) {
            setRandom(new Random(seed));
        }
    }

    private boolean isSeeded() {

        return seed != null;
    }

    public LogLevelSupplier getLogLevel() {

        return log_level;
    }

    public void setVerbosity(final LogLevelSupplier log_level) {

        this.log_level = log_level;
        setRootLoggerLevelByHandler(ConsoleHandler.class, log_level.get());
    }

    public void setInternalVerbosity(final LogLevelSupplier internal_log_level) {

        this.internal_log_level = internal_log_level;
        if (internal_log_handler != null) {
            internal_log_handler.setLevel(internal_log_level.get());
        }
    }

    private static void setRootLoggerLevelByHandler(Class<? extends Handler> handler_type, Level level) {

        final Handler[] handlers = Logger.getGlobal().getHandlers();
        for (Handler handler : handlers) {
            if (handler_type.isAssignableFrom(handler.getClass())) {
                handler.setLevel(level);
            }
        }
    }

    public LogLevelSupplier getInternalLogLevel() {

        return internal_log_level;
    }
}
