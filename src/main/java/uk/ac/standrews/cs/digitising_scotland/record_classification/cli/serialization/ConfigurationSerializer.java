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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cli.serialization;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.serialization.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Configuration.RECORD_CSV_FORMAT;
import static uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Configuration.RESOURCE_CHARSET;
import static uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Configuration.persistBucketAsCSV;
import static uk.ac.standrews.cs.digitising_scotland.record_classification.cli.command.InitCommand.assureDirectoryExists;

/**
 * @author Masih Hajiarab Derkani
 */
class ConfigurationSerializer extends JsonSerializer<Configuration> {

    protected static final String DEFAULT_CHARSET_SUPPLIER = "default_charset_supplier";
    protected static final String DEFAULT_DELIMITER = "default_delimiter";
    protected static final String SEED = "seed";
    protected static final String CLASSIFIER_SUPPLIER = "classifier_supplier";
    protected static final String CLASSIFIER_SERIALIZATION_FORMAT = "classifier_serialization_format";
    protected static final String DEFAULT_CSV_FORMAT_SUPPLIER = "default_csv_format_supplier";
    protected static final String DEFAULT_TRAINING_RATIO = "default_training_ratio";
    protected static final String DEFAULT_INTERNAL_TRAINING_RATIO = "default_internal_training_ratio";
    protected static final String VERBOSITY = "verbosity";
    protected static final String INTERNAL_VERBOSITY = "internal_verbosity";
    protected static final String SERIALIZED_CLASSIFIER_FILE_NAME_PREFIX = "classifier";

    @Override
    public void serialize(final Configuration configuration, final JsonGenerator out, final SerializerProvider serializers) throws IOException {

        out.writeStartObject();

        out.writeObjectField(DEFAULT_CHARSET_SUPPLIER, configuration.getDefaultCharsetSupplier());
        out.writeObjectField(DEFAULT_DELIMITER, configuration.getDefaultDelimiter());
        out.writeNumberField(DEFAULT_TRAINING_RATIO, configuration.getDefaultTrainingRatio());
        out.writeNumberField(DEFAULT_INTERNAL_TRAINING_RATIO, configuration.getDefaultInternalTrainingRatio());
        out.writeObjectField(DEFAULT_CSV_FORMAT_SUPPLIER, configuration.getDefaultCsvFormatSupplier());
        out.writeObjectField(SEED, configuration.getSeed());
        out.writeObjectField(CLASSIFIER_SUPPLIER, configuration.getClassifierSupplier());
        out.writeObjectField(CLASSIFIER_SERIALIZATION_FORMAT, configuration.getClassifierSerializationFormat());
        out.writeObjectField(VERBOSITY, configuration.getLogLevel());
        out.writeObjectField(INTERNAL_VERBOSITY, configuration.getInternalLogLevel());

        writeClassifier(configuration);
        writeGoldStandardRecords(configuration);
        writeUnseenRecords(configuration);
        writeClassifiedEvaluationRecords(configuration);
        writeClassifiedUnseenRecords(configuration);
        writeConfusionMatrix(configuration);
        writeClassificationMetrics(configuration);

        out.writeEndObject();
    }

    private void writeUnseenRecords(Configuration configuration) throws IOException {

        persistBucketIfPresent(configuration.getUnseenRecordsOptional(), getUnseenRecordsPath(configuration));
    }

    private void writeGoldStandardRecords(final Configuration configuration) throws IOException {

        writeTrainingRecords(configuration);
        writeEvaluationRecords(configuration);
    }

    private void writeClassifiedUnseenRecords(Configuration configuration) throws IOException {

        persistBucketIfPresent(configuration.getClassifiedUnseenRecordsOptional(), getClassifiedUnseenRecordsPath(configuration));
    }

    private void writeClassifiedEvaluationRecords(final Configuration configuration) throws IOException {

        persistBucketIfPresent(configuration.getClassifiedEvaluationRecordsOptional(), getClassifiedEvaluationRecordsPath(configuration));
    }

    private void writeClassifier(final Configuration configuration) throws IOException {

        final Optional<Classifier> classifier = configuration.getClassifierOptional();
        if (classifier.isPresent()) {

            final SerializationFormat format = configuration.getClassifierSerializationFormat();
            final Path destination = getSerializedClassifierPath(configuration, format);
            Serialization.persist(destination, classifier.get(), format);
        }
    }

    private void writeConfusionMatrix(final Configuration configuration) throws IOException {

        persistObjectIfPresent(configuration.getConfusionMatrixOptional(), getConfusionMatrixPath(configuration));
    }

    private void writeClassificationMetrics(final Configuration configuration) throws IOException {

        persistObjectIfPresent(configuration.getClassificationMetricsOptional(), getClassificationMetricsPath(configuration));
    }

    private void persistBucketIfPresent(final Optional<Bucket> bucket, final Path destination) throws IOException {

        if (bucket.isPresent()) {
            assureDirectoryExists(destination.getParent());
            persistBucketAsCSV(bucket.get(), destination, RECORD_CSV_FORMAT, RESOURCE_CHARSET);
        }
    }

    private void writeTrainingRecords(final Configuration configuration) throws IOException {

        persistBucketIfPresent(configuration.getTrainingRecordsOptional(), getTrainingRecordsPath(configuration));
    }

    private void writeEvaluationRecords(final Configuration configuration) throws IOException {

        persistBucketIfPresent(configuration.getEvaluationRecordsOptional(), configuration.getEvaluationRecordsPath());
    }

    static Path getSerializedClassifierPath(Configuration configuration, SerializationFormat format) {

        switch (format) {
            case JAVA_SERIALIZATION:
                return configuration.getHome().resolve(SERIALIZED_CLASSIFIER_FILE_NAME_PREFIX + ".object");
            case JSON:
                return configuration.getHome().resolve(SERIALIZED_CLASSIFIER_FILE_NAME_PREFIX + ".json");
            case JSON_COMPRESSED:
                return configuration.getHome().resolve(SERIALIZED_CLASSIFIER_FILE_NAME_PREFIX + ".object");
            default:
                throw new RuntimeException("unsupported classifier serialization format: " + format);
        }
    }

    static Path getTrainingRecordsPath(Configuration configuration) {

        return configuration.getHome().resolve("training.csv");
    }

    static Path getUnseenRecordsPath(Configuration configuration) {

        return configuration.getHome().resolve("unseen.csv");
    }

    static Path getClassifiedUnseenRecordsPath(Configuration configuration) {

        return configuration.getHome().resolve("classified_unseen.csv");
    }

    static Path getClassifiedEvaluationRecordsPath(Configuration configuration) {

        return configuration.getHome().resolve("classified_evaluation.csv");
    }

    static Path getConfusionMatrixPath(Configuration configuration) {

        return configuration.getHome().resolve("confusion_matrix.object");
    }

    static Path getClassificationMetricsPath(Configuration configuration) {

        return configuration.getHome().resolve("classification_metrics.object");
    }

    private void persistObjectIfPresent(final Optional<?> object, Path destination) throws IOException {

        if (object.isPresent()) {
            Serialization.persist(destination, object.get(), SerializationFormat.JAVA_SERIALIZATION);
        }
    }
}
