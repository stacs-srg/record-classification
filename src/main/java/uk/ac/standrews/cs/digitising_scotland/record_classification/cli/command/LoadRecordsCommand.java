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

import com.beust.jcommander.*;
import org.apache.commons.csv.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.util.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;

/**
 * Captures common functionality in loading tabular data from file in form of {@link Record records}.
 *
 * @author Masih Hajiarab Derkani
 */
@Parameters(resourceBundle = Configuration.RESOURCE_BUNDLE_NAME)
abstract class LoadRecordsCommand extends Command {

    /** The short name of the option that specifies the delimiter character in the tabular resource file to be loaded. **/
    public static final String OPTION_DELIMITER_SHORT = "-d";

    /** The long name of the option that specifies the delimiter character in the tabular resource file to be loaded. **/
    public static final String OPTION_DELIMITER_LONG = "--delimiter";

    /** The short name of the option that specifies the {@link CSVFormat format} of the tabular resource file to be loaded. **/
    public static final String OPTIONS_FORMAT_SHORT = "-f";

    /** The long name of the option that specifies the {@link CSVFormat format} of the tabular resource file to be loaded. **/
    public static final String OPTIONS_FORMAT_LONG = "--format";

    /** The short name of the option that specifies whether to skip the header record in the tabular resource file to be loaded. **/
    public static final String OPTION_SKIP_HEADER_SHORT = "-h";

    /** The long name of the option that specifies whether to skip the header record in the tabular resource file to be loaded. **/
    public static final String OPTION_SKIP_HEADER_LONG = "--skip_header";

    /** The short name of the option that specifies the index of the column that contains the ID associated to each row, starting from {@code 0}. **/
    public static final String OPTION_ID_COLUMN_INDEX_SHORT = "-ii";

    /** The long name of the option that specifies the index of the column that contains the ID associated to each row, starting from {@code 0}. **/
    public static final String OPTION_ID_COLUMN_INDEX_LONG = "--id_column_index";

    /** The short name of the option that specifies the index of the column that contains the label associated to each row, starting from {@code 0}. **/
    public static final String OPTION_LABEL_COLUMN_INDEX_SHORT = "-li";

    /** The long name of the option that specifies the index of the column that contains the label associated to each row, starting from {@code 0}. **/
    public static final String OPTION_LABEL_COLUMN_INDEX_LONG = "--label_column_index";

    /** The default index of the column that contains the ID associated to each row. **/
    public static final int DEFAULT_ID_COLUMN_INDEX = 0;

    /** The default index of the column that contains the label associated to each row. **/
    public static final int DEFAULT_LABEL_COLUMN_INDEX = 1;

    protected final LoadCommand load_command;

    @Parameter(names = {OPTION_DELIMITER_SHORT, OPTION_DELIMITER_LONG}, descriptionKey = "command.load_records.delimiter.description", converter = Converters.CharacterConverter.class)
    private Character delimiter = configuration.getDefaultDelimiter();

    @Parameter(names = {OPTIONS_FORMAT_SHORT, OPTIONS_FORMAT_LONG}, descriptionKey = "command.load_records.format.description")
    private CsvFormatSupplier csv_format_supplier = configuration.getDefaultCsvFormatSupplier();

    @Parameter(names = {OPTION_SKIP_HEADER_SHORT, OPTION_SKIP_HEADER_LONG}, descriptionKey = "command.load_records.skip_header.description")
    private boolean skip_header_record = false;

    @Parameter(names = {OPTION_ID_COLUMN_INDEX_SHORT, OPTION_ID_COLUMN_INDEX_LONG}, descriptionKey = "command.load_records.id_column_index.description", validateValueWith = Validators.AtLeastZero.class)
    private Integer id_column_index = DEFAULT_ID_COLUMN_INDEX;

    @Parameter(names = {OPTION_LABEL_COLUMN_INDEX_SHORT, OPTION_LABEL_COLUMN_INDEX_LONG}, descriptionKey = "command.load_records.label_column_index.description", validateValueWith = Validators.AtLeastZero.class)
    private Integer label_column_index = DEFAULT_LABEL_COLUMN_INDEX;

    public static abstract class Builder extends LoadCommand.Builder {

        private Character delimiter;
        private CsvFormatSupplier csv_format;
        private boolean skip_header_record;
        private Integer id_column_index;
        private Integer label_column_index;

        public void setDelimiter(Character delimiter) {

            this.delimiter = delimiter;
        }

        public void setFormat(CsvFormatSupplier csv_format) {

            this.csv_format = csv_format;
        }

        public void setSkipHeader() {

            this.skip_header_record = true;
        }

        public void setSkipHeader(boolean skip_header) {

            this.skip_header_record = skip_header;
        }

        public void setIdColumnIndex(Integer id_column_index) {

            this.id_column_index = id_column_index;
        }

        public void setLabelColumnIndex(Integer label_column_index) {

            this.label_column_index = label_column_index;
        }

        @Override
        protected void populateSubCommandArguments() {

            addArgument(getSubCommandName());

            if (delimiter != null) {
                addArgument(OPTION_DELIMITER_SHORT);
                addArgument(String.valueOf(delimiter));
            }

            if (csv_format != null) {
                addArgument(OPTIONS_FORMAT_SHORT);
                addArgument(csv_format.name());
            }

            if (skip_header_record) {
                addArgument(OPTION_SKIP_HEADER_SHORT);
            }
            if (id_column_index != null) {
                addArgument(OPTION_ID_COLUMN_INDEX_SHORT);
                addArgument(String.valueOf(id_column_index));
            }
            if (label_column_index != null) {
                addArgument(OPTION_LABEL_COLUMN_INDEX_SHORT);
                addArgument(String.valueOf(label_column_index));
            }
        }

        protected abstract String getSubCommandName();
    }

    /**
     * Instantiates this command as a sub command of the given load command.
     *
     * @param load_command the load command to which this command belongs.
     */
    LoadRecordsCommand(LoadCommand load_command, String name) {

        super(load_command.launcher, name);
        this.load_command = load_command;
    }

    @Override
    public void run() {

        process(readRecords());
    }

    /**
     * Processes loaded records.
     *
     * @param records the records to be processed
     */
    protected abstract void process(final List<Record> records);

    private List<Record> readRecords() {

        final CSVFormat format = getCsvFormat();
        final Path source = load_command.getSource();
        final Charset charset = load_command.getCharset();

        logger.finest(() -> String.format("loading records from %s, with charset %s, with format %s", source, charset, format));

        try (final BufferedReader in = Files.newBufferedReader(source, charset)) {

            final CSVParser parser = format.parse(in);
            return StreamSupport.stream(parser.spliterator(), true).map(this::toRecord).collect(Collectors.toList());
        }
        catch (RuntimeException e) {
            logger.log(Level.SEVERE, String.format("Failure while reading a record from file %s: check CSV format at specified line.", source), e);
            throw e;
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Failure while loading records.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the {@link CSVFormat format} of the tabular resource file.
     * The may be format specified via {@value #OPTIONS_FORMAT_SHORT} or {@value #OPTIONS_FORMAT_LONG} options.
     *
     * @return the {@link CSVFormat format} of the tabular resource file
     */
    public CSVFormat getCsvFormat() {

        final CSVFormat format = csv_format_supplier.get().withDelimiter(delimiter);
        return skip_header_record ? format.withHeader() : format;
    }

    /**
     * Converts a {@link CSVRecord tabular data record} into a {@link Record}.
     *
     * @param record the tabular data record to be converted
     * @return the converted record
     */
    protected abstract Record toRecord(final CSVRecord record);

    /**
     * Gets the label value from a {@link CSVRecord tabular data record} based on its column index.
     * The label column index may be specified via {@value #OPTION_LABEL_COLUMN_INDEX_SHORT} or {@value #OPTION_LABEL_COLUMN_INDEX_LONG} options.
     * If unspecified, the default index of {@value #DEFAULT_LABEL_COLUMN_INDEX} is used.
     *
     * @param record the record from which to extract label
     * @return the record label
     */
    protected String getLabel(final CSVRecord record) {

        return record.get(label_column_index);
    }

    /**
     * Gets the ID value from a {@link CSVRecord tabular data record} based on its column index.
     * The ID column index may be specified via {@value #OPTION_ID_COLUMN_INDEX_SHORT} or {@value #OPTION_ID_COLUMN_INDEX_LONG} options.
     * If unspecified, the default index of {@value #DEFAULT_ID_COLUMN_INDEX} is used.
     *
     * @param record the record from which to extract ID
     * @return the record ID
     */
    protected Integer getId(final CSVRecord record) {

        return Integer.parseInt(record.get(id_column_index));
    }
}
