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
package uk.ac.standrews.cs.digitising_scotland.record_classification.dataset;

import org.apache.commons.csv.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * @author Masih Hajiarab Derkani
 */
public class TestDataSet extends TestResource {

    public static final int DEFAULT_ID_COLUMN_INDEX = 0;
    public static final int DEFAULT_DATA_COLUMN_INDEX = 1;
    public static final int DEFAULT_CLASS_COLUMN_INDEX = 2;
    public static final CSVFormat DEFAULT_CSV_FORMAT = CSVFormat.DEFAULT.withHeader();

    public final Integer id_column_index;
    public final Integer label_column_index;
    public final Integer class_column_index;

    public final CSVFormat format;

    public TestDataSet(Supplier<Reader> resource_reader) {

        this(resource_reader, DEFAULT_CSV_FORMAT);
    }

    public TestDataSet(Supplier<Reader> resource_reader, CSVFormat format) {

        this(DEFAULT_ID_COLUMN_INDEX, DEFAULT_DATA_COLUMN_INDEX, DEFAULT_CLASS_COLUMN_INDEX, resource_reader, format);
    }

    public TestDataSet(final Integer id_column_index, final Integer label_column_index, final Integer class_column_index, Supplier<Reader> resource_reader, CSVFormat format) {

        super(resource_reader);

        this.id_column_index = id_column_index;
        this.label_column_index = label_column_index;
        this.class_column_index = class_column_index;
        this.format = format;
    }

    public TestDataSet(Class<?> loader, String resource_name) {

        this(loader, resource_name, DEFAULT_CSV_FORMAT);
    }

    public TestDataSet(Class<?> loader, String resource_name, CSVFormat format) {

        this(loader, resource_name, DEFAULT_CHARSET.get(), format);
    }

    public TestDataSet(Class<?> loader, String resource_name, Charset charset, CSVFormat format) {

        this(toReaderSupplier(loader, resource_name, charset), format);

    }

    public TestDataSet(final Integer id_column_index, final Integer label_column_index, final Integer class_column_index, Class<?> loader, String resource_name, Charset charset, CSVFormat format) {

        this(id_column_index, label_column_index, class_column_index, toReaderSupplier(loader, resource_name, charset), format);
    }

    public long getRecordsCount() {

        try {
            return getCSVRecordStream(resource_reader.get()).count();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Stream<CSVRecord> getCSVRecordStream(final Reader in) throws IOException {return StreamSupport.stream(format.parse(in).spliterator(), false);}

    public Bucket getBucket()  {

        Bucket bucket = new Bucket();

        try (final Reader in = resource_reader.get()) {
            getCSVRecordStream(in).map(this::toRecord).forEach(bucket::add);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        return bucket;
    }

    private Record toRecord(CSVRecord csv_record) {

        final int id = Integer.parseInt(csv_record.get(id_column_index));
        final String data = csv_record.get(label_column_index);

        final Classification classification;
        if (class_column_index != null) {
            final String code = csv_record.get(class_column_index);
            classification = new Classification(code, new TokenList(data), 0.0, null);
        }
        else {
            classification = Classification.UNCLASSIFIED;
        }
        return new Record(id, data, data, classification);
    }

    @Override
    public void copy(Path destination) {

        copy(destination, DEFAULT_CHARSET.get(), DEFAULT_CSV_FORMAT);
    }

    @Override
    public void copy(final Path destination, final Charset charset) {

        copy(destination, charset, DEFAULT_CSV_FORMAT);
    }

    public void copy(Path destination, Charset charset, CSVFormat format) {

        try {
            try (
                            final Reader in = resource_reader.get();
                            final BufferedWriter out = Files.newBufferedWriter(destination, charset)
            ) {

                final CSVPrinter printer = format.print(out);
                getCSVRecordStream(in).forEach(record -> print(printer, record));
                out.flush();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void print(final CSVPrinter printer, final CSVRecord record) {

        try {
            printer.printRecord(record);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
