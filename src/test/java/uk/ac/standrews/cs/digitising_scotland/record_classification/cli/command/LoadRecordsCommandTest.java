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
import org.junit.*;
import org.junit.runners.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.util.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

import static java.util.logging.Logger.getLogger;
import static org.junit.Assert.*;

/**
 * @author Masih Hajiarab Derkani
 */
public abstract class LoadRecordsCommandTest extends CommandTest {

    protected static final String ID = "ID";
    protected static final String LABEL = "LABEL";
    protected static final String CODE = "CODE";
    protected static final Bucket TEST_RECORDS = new Bucket();
    protected static final Map<Integer[], CSVFormat> TEST_FORMATS = new HashMap<>();

    static {
        TEST_RECORDS.add(new Record(1, "fish", new Classification("swim", new TokenList("fish"), 0.0, null)));
        TEST_RECORDS.add(new Record(2, "dog", new Classification("bark", new TokenList("dog"), 0.0, null)));
        TEST_RECORDS.add(new Record(3, "cat", new Classification("purr", new TokenList("cat"), 0.0, null)));
        TEST_RECORDS.add(new Record(4, "shark", new Classification("swim", new TokenList("shark"), 0.0, null)));

        TEST_FORMATS.put(new Integer[]{0, 1, 2}, CSVFormat.DEFAULT);
        TEST_FORMATS.put(new Integer[]{0, 1, 2}, CSVFormat.RFC4180);
        TEST_FORMATS.put(new Integer[]{0, 1, 2}, CSVFormat.EXCEL);
        TEST_FORMATS.put(new Integer[]{0, 1, 2}, CSVFormat.MYSQL);
        TEST_FORMATS.put(new Integer[]{0, 1, 2}, CSVFormat.TDF);
        TEST_FORMATS.put(new Integer[]{0, 1, 2}, CsvFormatSupplier.RFC4180_PIPE_SEPARATED.get());
        TEST_FORMATS.put(new Integer[]{0, 1, 2}, CSVFormat.DEFAULT.withHeader(ID, LABEL, CODE));
        TEST_FORMATS.put(new Integer[]{0, 2, 1}, CSVFormat.DEFAULT.withHeader(ID, CODE, LABEL));
        TEST_FORMATS.put(new Integer[]{2, 1, 0}, CSVFormat.DEFAULT.withHeader(CODE, LABEL, ID));
    }

    private static final Logger LOGGER = getLogger(LoadRecordsCommandTest.class.getName());

    private Path path;
    private CSVFormat format;
    private int id_index;
    private int label_index;
    protected int code_index;
    private Bucket records;

    protected LoadRecordsCommandTest(Bucket records, CSVFormat format, int id_index, int label_index, int code_index) {

        this.records = records;

        this.format = format;
        this.id_index = id_index;
        this.label_index = label_index;
        this.code_index = code_index;
        this.path = printRecordsIntoTempFileWithFormat(records, format, id_index, label_index, code_index);
    }

    protected Bucket getExpectedRecords() {

        return records;
    }

    protected static Path printRecordsIntoTempFileWithFormat(Bucket records, CSVFormat format, int id_index, int label_index, Integer code_index) {

        final Path temp_path = newTempFile();

        try (final BufferedWriter out = Files.newBufferedWriter(temp_path)) {

            final CSVPrinter printer = format.print(out);
            records.stream().forEach(record -> {
                try {

                    final String[] record_csv = new String[3];
                    record_csv[id_index] = String.valueOf(record.getId());
                    record_csv[label_index] = record.getData();
                    if (code_index != null) {
                        record_csv[code_index] = record.getClassification().getCode();
                    }
                    printer.printRecord(record_csv);
                }
                catch (IOException e) { throw new IOError(e); }
            });
        }
        catch (IOException e) { throw new IOError(e); }

        return temp_path;
    }

    private static Path newTempFile() {

        try {
            return Files.createTempFile(null, null);
        }
        catch (IOException e) {
            throw new IOError(e);
        }
    }

    @After
    public void tearDownAfterClass() throws Exception {

        deleteQuietlyIfExists(path);
    }

    @Parameterized.Parameters(name = "{index} - id: {2}, label: {3}, code: {4}")
    public static Collection<Object[]> data() {

        final List<Object[]> arguments = new ArrayList<>();

        TEST_FORMATS.entrySet().forEach(index_format -> {
            final Integer[] indices = index_format.getKey();
            final CSVFormat format = index_format.getValue();
            final Integer id_index = indices[0];
            final Integer label_index = indices[1];
            final Integer code_index = indices[2];
            arguments.add(new Object[]{TEST_RECORDS, format, id_index, label_index, code_index});
        });

        return arguments;
    }

    protected List<Object> getArguments() {

        final List<Object> arguments = new ArrayList<>();

        arguments.add(LoadCommand.NAME);
        arguments.add(LoadCommand.OPTION_SOURCE_SHORT);
        arguments.add(path);
        arguments.add(getSubCommandName());
        arguments.add(LoadRecordsCommand.OPTION_DELIMITER_SHORT);
        arguments.add(Arguments.quote(format.getDelimiter()));
        arguments.add(LoadRecordsCommand.OPTION_ID_COLUMN_INDEX_SHORT);
        arguments.add(id_index);
        arguments.add(LoadRecordsCommand.OPTION_LABEL_COLUMN_INDEX_SHORT);
        arguments.add(label_index);

        if (format.getHeader() != null) {
            arguments.add(LoadRecordsCommand.OPTION_SKIP_HEADER_SHORT);
        }

        return arguments;
    }

    protected abstract String getSubCommandName();

    @Test
    public void testLoad() throws Exception {

        run(getArguments().toArray());

        final Bucket actual_records = getActualRecords();
        assertEquals(getExpectedRecords(), actual_records);
    }

    protected abstract Bucket getActualRecords();

    private static void deleteQuietlyIfExists(final Path path) {

        try {
            Files.deleteIfExists(path);
        }
        catch (IOException e) {
            LOGGER.log(Level.FINE, String.format("IO exception while deleting temp file: %s", path), e);
        }
    }
}
