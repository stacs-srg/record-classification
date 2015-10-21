/*
 * Copyright 2015 Digitising Scotland project:
 * <http://digitisingscotland.cs.st-andrews.ac.uk/>
 *
 * This file is part of the module record_classification.
 *
 * record_classification is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * record_classification is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with record_classification. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.digitising_scotland.record_classification.cli;

import com.beust.jcommander.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import org.apache.commons.csv.*;
import org.apache.commons.io.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.serialization.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * The Command Line Interface configuration.
 *
 * @author Masih Hajiarab Derkani
 */
public class Configuration {

    //TODO add parent config loading from user.home if exists.

    /** Name of record classification CLI program. */
    public static final String PROGRAM_NAME = "classi";

    /** The name of the folder that contains the persisted state of this program. */
    public static final Path CLI_HOME = Paths.get(Configuration.PROGRAM_NAME);
    public static final Path CONFIGURATION_FILE = CLI_HOME.resolve("config.json");
    public static final Path GOLD_STANDARD_HOME = CLI_HOME.resolve("gold_standard");
    public static final Path UNSEEN_HOME = CLI_HOME.resolve("unseen");
    public static final Path DICTIONARY_HOME = CLI_HOME.resolve("dictionary");
    public static final Path STOP_WORDS_HOME = CLI_HOME.resolve("stop_words");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
//        MAPPER.setVisibilityChecker(MAPPER.getSerializationConfig().getDefaultVisibilityChecker().
//                        withGetterVisibility(JsonAutoDetect.Visibility.NONE).
//                        withSetterVisibility(JsonAutoDetect.Visibility.NONE));
//        MAPPER.setVisibilityChecker(MAPPER.getDeserializationConfig().getDefaultVisibilityChecker().
//                        withGetterVisibility(JsonAutoDetect.Visibility.NONE).
//                        withSetterVisibility(JsonAutoDetect.Visibility.NONE));
    }

    private static final CsvFormatSupplier DEFAULT_CSV_FORMAT_SUPPLIER = CsvFormatSupplier.DEFAULT;
    private static final char DEFAULT_DELIMITER = DEFAULT_CSV_FORMAT_SUPPLIER.get().getDelimiter();
    public static final CharsetSupplier DEFAULT_CHARSET_SUPPLIER = CharsetSupplier.SYSTEM_DEFAULT;

    public static final double DEFAULT_TRAINING_RATIO = 0.8;
    /** The default ratio of records to be used for internal training of the classifier. **/
    public static final double DEFAULT_INTERNAL_TRAINING_RATIO = DEFAULT_TRAINING_RATIO;

    private CharsetSupplier default_charset_supplier = DEFAULT_CHARSET_SUPPLIER;
    private Character default_delimiter = DEFAULT_DELIMITER;
    private Long seed;
    private boolean proceed_on_error;
    private Path error_log;
    private Path info_log;
    private Classifier classifier;
    private ClassifierSupplier classifier_supplier;
    private SerializationFormat serialization_format;
    private CsvFormatSupplier default_csv_format_supplier = DEFAULT_CSV_FORMAT_SUPPLIER;
    private List<GoldStandard> gold_standards;
    private List<Unseen> unseens;
    private List<Dictionary> dictionaries;
    private List<StopWords> stop_words;
    @JsonIgnore
    private Random random;
    private double default_training_ratio = DEFAULT_TRAINING_RATIO;
    private double default_internal_training_ratio = DEFAULT_INTERNAL_TRAINING_RATIO;

    public Configuration() {

        gold_standards = new ArrayList<>();
        unseens = new ArrayList<>();
        dictionaries = new ArrayList<>();
        stop_words = new ArrayList<>();

        initRandom();
    }

    private void initRandom() {random = seed == null ? new Random() : new Random(seed);}

    static Configuration load() throws IOException {

        return MAPPER.readValue(Files.newBufferedReader(CONFIGURATION_FILE), Configuration.class);
    }

    public void setSeed(final Long seed) {

        this.seed = seed;
        initRandom();
    }

    public SerializationFormat getSerializationFormat() {

        return serialization_format;
    }

    public void setSerializationFormat(final SerializationFormat serialization_format) {

        this.serialization_format = serialization_format;
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

    public void addUnseen(final Unseen unseen) {

        unseens.add(unseen);
    }

    public Optional<Classifier> getClassifier() {

        return classifier == null ? Optional.empty() : Optional.of(classifier);
    }

    public Classifier requireClassifier() {

        return getClassifier().orElseThrow(() -> new ParameterException("The classifier is required and not set; please specify a classifier."));
    }

    public void addGoldStandard(final GoldStandard gold_standard) {

        gold_standards.add(gold_standard);

    }

    public Optional<Bucket> getGoldStandardRecords() {

        final Optional<Bucket> training_records = getTrainingRecords();
        final Optional<Bucket> evaluation_records = getEvaluationRecords();

        if (!training_records.isPresent() && !evaluation_records.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(training_records.orElse(new Bucket()).union(evaluation_records.orElse(new Bucket())));
    }

    public Bucket requireGoldStandardRecords() {

        return getGoldStandardRecords().orElseThrow(() -> new ParameterException("No gold standard record is present; please load some gold standard records."));
    }

    public Bucket requireEvaluationRecords() {

        return getEvaluationRecords().orElseThrow(() -> new ParameterException("No evaluation record is present; please load some evaluation records."));
    }

    public Bucket requireTrainingRecords() {

        return getTrainingRecords().orElseThrow(() -> new ParameterException("No training record is present; please load some training records."));
    }

    public Bucket requireUnseenRecords() {

        return getUnseenRecords().orElseThrow(() -> new ParameterException("No unseen record is present; please load some unseen records."));
    }

    public List<Bucket> requireUnseenRecordsList() {

        final List<Bucket> unseen_records_list = getUnseenRecordsList();

        if (unseen_records_list.isEmpty()) {
            new ParameterException("No unseen record is present; please load some unseen records.");
        }

        return unseen_records_list;
    }

    @JsonIgnore
    public Optional<Bucket> getTrainingRecords() {

        return getGoldStandards().stream().map(Configuration.GoldStandard::getTrainingRecords).reduce(Bucket::union);
    }

    @JsonIgnore
    public List<GoldStandard> getGoldStandards() {

        return gold_standards;
    }

    @JsonIgnore
    public Optional<Bucket> getEvaluationRecords() {

        return getGoldStandards().stream().map(Configuration.GoldStandard::getEvaluationRecords).reduce(Bucket::union);
    }

    public List<Unseen> getUnseens() {

        return unseens;
    }

    @JsonIgnore
    public ClassifierSupplier getClassifierSupplier() {

        return classifier_supplier;
    }

    public void setClassifierSupplier(final ClassifierSupplier classifier_supplier) {

        this.classifier_supplier = classifier_supplier;
        classifier = classifier_supplier.get();
    }

    public Random getRandom() {

        return random;
    }

    public void persist() throws IOException {

        MAPPER.writeValue(Files.newOutputStream(CONFIGURATION_FILE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), this);
    }

    public double getDefaultInternalTrainingRatio() {

        return default_internal_training_ratio;
    }

    public void setDefaultInternalTrainingRatio(final double default_internal_training_ratio) {

        this.default_internal_training_ratio = default_internal_training_ratio;
    }

    public void setDefaultTrainingRatio(final double default_training_ratio) {

        this.default_training_ratio = default_training_ratio;
    }

    public double getDefaultTrainingRatio() {

        return default_training_ratio;
    }

    public Optional<Bucket> getUnseenRecords() {

        return getUnseens().stream().map(Configuration.Unseen::toBucket).reduce(Bucket::union);
    }

    public List<Bucket> getUnseenRecordsList() {

        return getUnseens().stream().map(Configuration.Unseen::toBucket).collect(Collectors.toList());
    }

    abstract static class Resource {

        public static final Charset CHARSET = StandardCharsets.UTF_8;

        private final String name;

        public Resource(final String name) {

            this.name = name;
        }

        public String getName() {

            return name;
        }

        protected Path getPath() {

            return getHome().resolve(name);
        }

        protected abstract Path getHome();
    }

    public static class Dictionary extends Resource {

        public Dictionary(final String name) {

            super(name);
        }

        @Override
        protected Path getHome() {

            return DICTIONARY_HOME;
        }

        public void load(final Path source, final Charset charset) throws IOException {

            try (final BufferedReader in = Files.newBufferedReader(source, charset);
                 final BufferedWriter out = Files.newBufferedWriter(getPath(), CHARSET);
            ) {
                IOUtils.copy(in, out);
            }
        }
    }

    public static class StopWords extends Dictionary {

        public StopWords(final String name) {

            super(name);
        }

        @Override
        protected Path getHome() {

            return STOP_WORDS_HOME;
        }
    }

    public static class Unseen extends Resource {

        public static CSVFormat CSV_FORMAT = CSVFormat.RFC4180.withHeader("ID", "LABEL");

        private Bucket bucket;

        public Unseen(final String name) {

            super(name);
        }

        public synchronized Bucket toBucket() {

            if (bucket == null) {

                bucket = new Bucket();
                try (final BufferedReader in = Files.newBufferedReader(getPath(), CHARSET)) {

                    final CSVParser parser = getCsvFormat().parse(in);
                    for (final CSVRecord csv_record : parser) {
                        final Record record = toRecord(csv_record);
                        bucket.add(record);
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return bucket;
        }

        @Override
        protected Path getHome() {

            return UNSEEN_HOME;
        }

        public CSVFormat getCsvFormat() {

            return CSV_FORMAT;
        }

        protected Record toRecord(CSVRecord csv_record) {

            final int id = Integer.parseInt(csv_record.get(0));
            final String label = csv_record.get(1);

            return new Record(id, label);
        }

        public synchronized void add(final Stream<Record> records) {

            if (bucket == null) {
                bucket = new Bucket();
            }

            bucket.add(records.collect(Collectors.toList()));
        }

        public void setBucket(Bucket bucket) {

            this.bucket = bucket;
        }

    }

    public static class GoldStandard extends Unseen {

        public static final CSVFormat CSV_FORMAT = CSVFormat.RFC4180.withHeader("ID", "LABEL", "CLASS");
        private double training_ratio;

        public GoldStandard(final String name, final double training_ratio) {

            super(name);
            this.training_ratio = training_ratio;
        }

        public double getTrainingRatio() {

            return training_ratio;
        }

        @Override
        protected Path getHome() {

            return GOLD_STANDARD_HOME;
        }

        @Override
        public CSVFormat getCsvFormat() {

            return CSV_FORMAT;
        }

        @Override
        protected Record toRecord(final CSVRecord csv_record) {

            final int id = Integer.parseInt(csv_record.get(0));
            final String label = csv_record.get(1);
            final String clazz = csv_record.get(2);

            return new Record(id, label, new Classification(clazz, new TokenList(label), 0.0, null));
        }

        public Bucket getTrainingRecords() {
            //TODO implement
            return null;
        }

        public Bucket getEvaluationRecords() {
            //TODO implement
            return null;
        }
    }
}
