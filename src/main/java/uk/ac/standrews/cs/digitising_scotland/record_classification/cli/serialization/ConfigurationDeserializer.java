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

import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.serialization.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static uk.ac.standrews.cs.digitising_scotland.record_classification.cli.serialization.ConfigurationSerializer.*;

/**
 * @author Masih Hajirab Derkani
 */
public class ConfigurationDeserializer extends JsonDeserializer<Configuration> {

    @Override
    public Configuration deserialize(final JsonParser in, final DeserializationContext context) throws IOException {

        Configuration configuration = new Configuration();
        expectCurrent(in, JsonToken.START_OBJECT);
        expectNext(in, JsonToken.FIELD_NAME);

        String field_name = in.getCurrentName();
        while (field_name != null) {

            switch (field_name) {
                case DEFAULT_CHARSET_SUPPLIER:
                    expectNext(in, JsonToken.VALUE_STRING);
                    configuration.setDefaultCharsetSupplier(in.readValueAs(CharsetSupplier.class));
                    break;
                case DEFAULT_DELIMITER:
                    expectNext(in, JsonToken.VALUE_STRING);
                    configuration.setDefaultDelimiter(in.readValueAs(Character.TYPE));
                    break;
                case DEFAULT_TRAINING_RATIO:
                    expectNext(in, JsonToken.VALUE_NUMBER_FLOAT);
                    configuration.setDefaultTrainingRatio(in.readValueAs(Double.TYPE));
                    break;
                case DEFAULT_INTERNAL_TRAINING_RATIO:
                    expectNext(in, JsonToken.VALUE_NUMBER_FLOAT);
                    configuration.setDefaultInternalTrainingRatio(in.readValueAs(Double.TYPE));
                    break;
                case DEFAULT_CSV_FORMAT_SUPPLIER:
                    expectNext(in, JsonToken.VALUE_STRING);
                    configuration.setDefaultCsvFormatSupplier(in.readValueAs(CsvFormatSupplier.class));
                    break;
                case SEED:
                    expectNext(in, JsonToken.VALUE_NULL, JsonToken.VALUE_NUMBER_INT);
                    configuration.setSeed(in.readValueAs(Long.class));
                    break;
                case CLASSIFIER_SUPPLIER:
                    expectNext(in, JsonToken.VALUE_NULL, JsonToken.VALUE_STRING);
                    configuration.setClassifierSupplier(in.readValueAs(ClassifierSupplier.class));
                    break;
                case CLASSIFIER_SERIALIZATION_FORMAT:
                    expectNext(in, JsonToken.VALUE_STRING);
                    configuration.setClassifierSerializationFormat(in.readValueAs(SerializationFormat.class));
                    break;
                case VERBOSITY:
                    expectNext(in, JsonToken.VALUE_STRING);
                    configuration.setVerbosity(in.readValueAs(LogLevelSupplier.class));
                    break;
                case INTERNAL_VERBOSITY:
                    expectNext(in, JsonToken.VALUE_STRING);
                    configuration.setInternalVerbosity(in.readValueAs(LogLevelSupplier.class));
                    break;
                default:
                    throw new JsonParseException("unknown configuration parameter", in.getCurrentLocation());
            }
            expectNext(in, JsonToken.FIELD_NAME, JsonToken.END_OBJECT);
            field_name = in.getCurrentName();
        }

        readClassifierLazily(configuration);
        readGoldStandardRecordsLazily(configuration);
        readUnseenRecordsLazily(configuration);
        readClassifiedEvaluationRecordsLazily(configuration);
        readClassifiedUnseenRecordsLazily(configuration);
        readConfusionMatrixLazily(configuration);
        readClassificationMetricsLazily(configuration);

        return configuration;
    }

    private void expectCurrent(final JsonParser in, final JsonToken expected) throws JsonParseException {

        final JsonToken actual = in.getCurrentToken();
        expectToken(in, actual, expected);
    }

    private void expectNext(final JsonParser in, JsonToken... expected) throws IOException {

        final JsonToken actual = in.nextToken();
        expectToken(in, actual, expected);
    }

    private void expectToken(final JsonParser in, final JsonToken actual, final JsonToken... expected) throws JsonParseException {

        if (!Arrays.stream(expected).filter(actual::equals).findAny().isPresent()) {
            throw new JsonParseException(String.format("expected %s, found %s", Arrays.toString(expected), actual), in.getCurrentLocation());
        }
    }

    private void readUnseenRecordsLazily(Configuration configuration) throws IOException {

        configuration.setUnseenRecordsLazyLoader(() -> loadBucketIfPresent(getUnseenRecordsPath(configuration)));
    }

    private Bucket loadBucketIfPresent(Path source) {

        try {
            return Files.isRegularFile(source) ? Configuration.loadBucket(source) : null;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readGoldStandardRecordsLazily(Configuration configuration) throws IOException {

        readTrainingRecordsLazily(configuration);
        readEvaluationRecordsLazily(configuration);
    }

    private void readTrainingRecordsLazily(Configuration configuration) throws IOException {

        configuration.setTrainingRecordsLazyLoader(() -> loadBucketIfPresent(getTrainingRecordsPath(configuration)));
    }

    private void readEvaluationRecordsLazily(Configuration configuration) throws IOException {

        configuration.setEvaluationRecordsLazyLoader(() -> loadBucketIfPresent(configuration.getEvaluationRecordsPath()));
    }

    private void readClassifiedUnseenRecordsLazily(Configuration configuration) throws IOException {

        configuration.setClassifiedUnseenRecordsLazyLoader(() -> loadBucketIfPresent(getClassifiedUnseenRecordsPath(configuration)));
    }

    private void readClassifiedEvaluationRecordsLazily(Configuration configuration) throws IOException {

        configuration.setClassifiedEvaluationRecordsLazyLoader(() -> loadBucketIfPresent(getClassifiedEvaluationRecordsPath(configuration)));
    }

    private void readClassifierLazily(final Configuration configuration) throws IOException {

        final SerializationFormat format = configuration.getClassifierSerializationFormat();
        final Path source = getSerializedClassifierPath(configuration, format);

        if (Files.isRegularFile(source)) {
            configuration.setClassifierLazyLoader(() -> loadSerializedIfPresent(source, Classifier.class, format));
        }
    }

    private <Type> Type loadSerializedIfPresent(Path source, Class<Type> type, SerializationFormat format) {

        try {
            return Files.isRegularFile(source) ? Serialization.load(source, type, format) : null;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readConfusionMatrixLazily(final Configuration configuration) throws IOException {

        configuration.setConfusionMatrixLazyLoader(() -> loadObjectIfPresent(getConfusionMatrixPath(configuration), ConfusionMatrix.class));
    }

    private <Type> Type loadObjectIfPresent(Path source, Class<Type> type) {

        return loadSerializedIfPresent(source, type, SerializationFormat.JAVA_SERIALIZATION);
    }

    private void readClassificationMetricsLazily(final Configuration configuration) throws IOException {

        configuration.setClassificationMetricsLazyLoader(() -> loadObjectIfPresent(getClassificationMetricsPath(configuration), Metrics.class));
    }
}
