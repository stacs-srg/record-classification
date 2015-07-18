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
package uk.ac.standrews.cs.digitising_scotland.record_classification.process.cli;

import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.lang3.SerializationUtils;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.processes.generic.ClassificationContext;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.processes.generic.Step;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.serialization.json.ProcessObjectMapper;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.steps.LoadGoldStandardFromFileStep;
import uk.ac.standrews.cs.util.dataset.DataSet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Captures the common functionality among the command-line interface commands.
 *
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
abstract class Command implements Callable<Void>, Step {

    private static final String SERIALIZED_CONTEXT_NAME = "context";

    private static final String CHARSET_DESCRIPTION = "The data file charset";
    private static final String DELIMITER_DESCRIPTION = "The data file delimiter character";
    private static final String SERIALIZATION_FORMAT_DESCRIPTION = "Format for serialized context files";

    private static final long serialVersionUID = -2176702491500665712L;

    private static final String DEFAULT_PROCESS_NAME = "classification_process";
    private static final String JSON_SUFFIX = "json";
    private static final String JSON_COMPRESSED_SUFFIX = "json.gz";
    private static final String SERIALIZED_SUFFIX = "serialized";

    @Parameter(names = {"-n", "--name"}, description = "The name of the classification process.")
    protected String name = DEFAULT_PROCESS_NAME;

    @Parameter(names = {"-ch", "--charset"}, description = CHARSET_DESCRIPTION)
    protected Charsets charset = LoadGoldStandardFromFileStep.DEFAULT_CHARSET;

    @Parameter(names = {"-d", "--delimiter"}, description = DELIMITER_DESCRIPTION)
    protected char delimiter = LoadGoldStandardFromFileStep.DEFAULT_DELIMITER;

    @Parameter(names = {"-f", "--format"}, description = SERIALIZATION_FORMAT_DESCRIPTION)
    protected SerializationFormat serialization_format = SerializationFormat.JSON;

    private enum SerializationFormat {
        JAVA_SERIALIZATION, JSON, COMPRESSED_JSON
    }

    private ObjectMapper json_mapper = new ProcessObjectMapper();

    @Override
    public Void call() throws Exception {

        final ClassificationContext context = loadContext();
        perform(context);
        persistContext(context);

        return null;
    }

    protected ClassificationContext loadContext() throws IOException {

        return serialization_format == SerializationFormat.JAVA_SERIALIZATION ? loadContextFromSerializedFile() : loadContextFromJSONFile();
    }

    protected void persistContext(ClassificationContext context) throws IOException {

        if (serialization_format == SerializationFormat.JAVA_SERIALIZATION) {
            persistContextToSerializedFile(context);
        } else {
            persistContextToJSONFile(context);
        }
    }

    protected CSVFormat getDataFormat(char delimiter) {

        return DataSet.DEFAULT_CSV_FORMAT.withDelimiter(delimiter);
    }

    private ClassificationContext loadContextFromJSONFile() throws IOException {

        InputStream input_stream = Files.newInputStream(getSerializedContextPath());
        if (serialization_format == SerializationFormat.COMPRESSED_JSON) {
            input_stream = new GZIPInputStream(input_stream);
        }

        return json_mapper.readValue(input_stream, new TypeReference<ClassificationContext>() {
        });
    }

    private ClassificationContext loadContextFromSerializedFile() throws IOException {

        Path serialized_classification_process_path = getSerializedContextPath();

        if (!Files.isRegularFile(serialized_classification_process_path)) {

            throw new IOException("No suitable classification process file found; expected a file named " + serialized_classification_process_path + " at the current working directory.");
        }

        final byte[] process_bytes = Files.readAllBytes(serialized_classification_process_path);
        return (ClassificationContext) SerializationUtils.deserialize(process_bytes);
    }

    private void persistContextToJSONFile(final ClassificationContext context) throws IOException {

        OutputStream output_stream = Files.newOutputStream(getSerializedContextPath());
        if (serialization_format == SerializationFormat.COMPRESSED_JSON) {
            output_stream = new GZIPOutputStream(output_stream);
        }

        json_mapper.writeValue(output_stream, context);
    }

    private void persistContextToSerializedFile(ClassificationContext context) throws IOException {

        final byte[] process_bytes = SerializationUtils.serialize(context);
        Files.write(getSerializedContextPath(), process_bytes);
    }

    protected void persistDataSet(Path destination, final DataSet dataset) throws IOException {

        try (final BufferedWriter out = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
            dataset.print(out);
        }
    }

    protected Path getSerializedContextPath() {

        return Paths.get(name).resolve(SERIALIZED_CONTEXT_NAME + "." + getSerializedContextSuffix());
    }

    private String getSerializedContextSuffix() {

        switch (serialization_format) {
            case JAVA_SERIALIZATION:
                return SERIALIZED_SUFFIX;
            case JSON:
                return JSON_SUFFIX;
            default:
                return JSON_COMPRESSED_SUFFIX;
        }
    }
}
