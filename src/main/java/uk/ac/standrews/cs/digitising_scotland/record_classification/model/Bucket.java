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
package uk.ac.standrews.cs.digitising_scotland.record_classification.model;

import com.google.common.collect.Iterables;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.util.Validators;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.DuplicateRecordIdException;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.InputFileFormatException;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Bucket implements Iterable<Record>, Serializable {

    private static final long serialVersionUID = 7216381249689825103L;

    public static final String FORMAT_ERROR_MESSAGE = "record should contain id, data and optional code and confidence";

    private final TreeSet<Record> records;
    private boolean auto_allocate_ids;
    private int next_id = 1;

    /** Instantiates a new empty bucket. */
    public Bucket() {

        this(false);
    }

    public Bucket(boolean auto_allocate_ids) {

        records = new TreeSet<>();
        this.auto_allocate_ids = auto_allocate_ids;
    }

    public Bucket(File records, Charset charset, char delimiter) throws IOException {

        this(Files.newBufferedReader(records.toPath(), charset), delimiter);
    }

    public Bucket(Reader reader, char delimiter) throws IOException {

        this(new DataSet(reader, delimiter));
    }

    public Bucket(DataSet data_set) {

        this();

        boolean first = true;
        for (List<String> record : data_set.getRecords()) {

            try {
                int id = extractId(record);
                String data = extractData(record);
                Classification classification = extractClassification(record, data);

                add(new Record(id, data, classification));

            }
            catch (InputFileFormatException e) {

                // If this is the first row, assume it's a header row and ignore exception.
                if (!first) {
                    throw e;
                }
            }
            finally {
                first = false;
            }
        }
    }

    public Bucket(Collection<Record> records) {

        this();
        add(records);
    }

    public Bucket(Record... records) {

        this(Arrays.asList(records));
    }

    public boolean isEmpty() {

        return records.isEmpty();
    }

    public List<Bucket> split(int ways, Random random) {

        if (ways < 1) {
            throw new IllegalArgumentException("the number of splits must be at least 1");
        }

        final List<Bucket> splits = new ArrayList<>(ways);
        for (int i = 0; i < ways; i++) {
            splits.add(new Bucket());
        }

        final List<Record> source_records = getRecordsList();
        Collections.shuffle(source_records, random);
        final Iterator<Bucket> splits_iterator = Iterables.cycle(splits).iterator();
        for (Record record : source_records) {
            splits_iterator.next().add(record);
        }

        return splits;
    }

    /**
     * Adds the given records to this bucket.
     *
     * @param records the records to add
     */
    public final void add(final Record... records) {

        add(Arrays.asList(records));
    }

    public final void add(final Bucket bucket) {

        add(bucket.records);
    }

    public final void add(final Collection<Record> records) {

        int original_size = this.records.size();

        if (auto_allocate_ids) {
            this.records.addAll(reallocateIds(records));
        }
        else {
            this.records.addAll(records);
        }

        int final_size = this.records.size();

        if (final_size != original_size + records.size()) {
            throw new DuplicateRecordIdException();
        }
    }

    public Stream<Record> stream() {

        return StreamSupport.stream(spliterator(), false);
    }

    public Stream<Record> parallelStream() {

        return StreamSupport.stream(spliterator(), true);
    }

    public DataSet toDataSet(List<String> column_labels) {

        final DataSet dataset = new DataSet(column_labels);

        for (Record record : records) {

            final String column_0 = String.valueOf(record.getId());
            final String column_1 = record.getOriginalData();
            final String column_2 = record.getClassification().getCode();
            final String column_3 = String.valueOf(record.getClassification().getConfidence());
            final String column_4 = record.getClassification().getDetail();

            dataset.addRow(column_0, column_1, column_2, column_3, column_4);
        }
        return dataset;
    }

    public DataSet toDataSet2(List<String> column_labels) {

        final DataSet dataset = new DataSet(column_labels);

        for (Record record : records) {

            final String column_0 = String.valueOf(record.getId());
            final String column_1 = record.getOriginalData();
            final String column_2 = record.getClassification().getCode();

            dataset.addRow(column_0, column_1, column_2);
        }
        return dataset;
    }

    public Record getFirstRecord() {

        return records.isEmpty() ? null : records.first();
    }

    public List<Record> getRecordsList() {

        return new CopyOnWriteArrayList<>(records);
    }

    public Optional<Record> findRecordById(int id) {

        return records.stream().filter(record -> record.getId() == id).findFirst();
    }

    @Override
    public Iterator<Record> iterator() {

        return records.iterator();
    }

    @Override
    public int hashCode() {

        return Objects.hash(records);
    }

    @Override
    public boolean equals(final Object other) {

        return this == other || other instanceof Bucket && Objects.equals(records, ((Bucket) other).records);
    }

    @Override
    public String toString() {

        return "Bucket [records=" + records + ", size=" + size() + "]";
    }

    /**
     * Returns the number of records in the bucket.
     *
     * @return the number of records
     */
    public int size() {

        return records.size();
    }

    public Bucket union(final Bucket other) {

        final Bucket combined = new Bucket(true);

        combined.add(this);
        combined.add(other);

        return combined;
    }

    /**
     * Constructs a new bucket containing this bucket's records that do not exist in the given bucket.
     *
     * @param other the other bucket
     * @return a new bucket containing this records that are present in this bucket and not present in the other bucket
     */
    public Bucket difference(final Bucket other) {

        final Bucket difference = new Bucket();

        for (Record record : this) {
            if (!other.contains(record)) {
                difference.add(record);
            }
        }

        return difference;
    }

    /**
     * Checks whether the specified record is in this bucket.
     *
     * @param record the record to check.
     * @return true if the record is a member of this bucket
     */
    public boolean contains(final Record record) {

        return records.contains(record);
    }

    public boolean containsData(final String data) {

        for (Record record : this) {
            if (record.getData().equals(data))
                return true;
        }

        return false;
    }

    /**
     * Constructs a new bucket containing records of this bucket without classification.
     *
     * @return a new bucket containing records of this bucket without classification
     * @see Record#Record(int, String, String)
     */
    public Bucket stripRecordClassifications() {

        Bucket unclassified_bucket = new Bucket();

        for (Record record : this) {
            unclassified_bucket.add(new Record(record.getId(), record.getData(), record.getOriginalData()));
        }

        return unclassified_bucket;
    }

    /**
     * Constructs a new bucket containing the records with unique data in this bucket.
     *
     * @return a new bucket containing the records with unique data in this bucket
     */
    public Bucket makeUniqueDataRecords() {

        Map<String, Record> unique_data_records = new HashMap<>();

        for (Record record : this) {
            unique_data_records.put(record.getOriginalData(), record);
        }

        final Bucket unique_bucket = new Bucket();
        unique_bucket.add(unique_data_records.values());

        return unique_bucket;
    }

    /**
     * Constructs a new bucket containing a randomly selected subset of records present in this bucket based on a given selection probability.
     *
     * @param random the random number generator
     * @param selection_probability the probability of a record being selected expressed within inclusive range of {@code 0.0}  to {@code 1.0}
     * @return a new bucket containing a randomly selected subset of this bucket's records
     */
    public Bucket randomSubset(final Random random, double selection_probability) {

        final Bucket subset = new Bucket();

        if (selection_probability > 1.0 - Validators.DELTA) {

            subset.add(records);
        }
        else if (selection_probability > Validators.DELTA) {

            final Bucket not_selected = new Bucket();
            final int expected_subset_size = (int) (size() * selection_probability);

            for (Record record : this) {
                if (subset.size() < expected_subset_size && random.nextDouble() < selection_probability) {
                    subset.add(record);
                }
                else {
                    not_selected.add(record);
                }
            }

            // Add further records as necessary to make up to required size.
            for (Record record : not_selected) {
                if (subset.size() < expected_subset_size) {
                    subset.add(record);
                }
            }
        }

        return subset;
    }

    private int extractId(List<String> record) {

        if (record.size() < 1) {
            throw new InputFileFormatException(FORMAT_ERROR_MESSAGE);
        }

        final String id_string = record.get(0);
        try {
            return Integer.parseInt(id_string);

        }
        catch (NumberFormatException e) {
            throw new InputFileFormatException("invalid numerical id: " + id_string);
        }
    }

    private String extractData(List<String> record) {

        if (record.size() < 2) {
            throw new InputFileFormatException(FORMAT_ERROR_MESSAGE);
        }

        return record.get(1);
    }

    private Classification extractClassification(List<String> record, String data) {

        if (record.size() < 3) {
            return Classification.UNCLASSIFIED;
        }

        String code = record.get(2);
        double confidence = extractConfidence(record);
        String detail = extractDetail(record);

        return code.isEmpty() ? Classification.UNCLASSIFIED : new Classification(code, new TokenList(data), confidence, detail);
    }

    private double extractConfidence(List<String> record) {

        if (record.size() < 4) {
            return 0.0;
        }

        final String confidence_string = record.get(3);
        try {
            return Double.parseDouble(confidence_string);

        }
        catch (NumberFormatException e) {
            throw new InputFileFormatException("invalid numerical confidence: " + confidence_string);
        }
    }

    private String extractDetail(List<String> record) {

        return record.size() < 5 ? null : record.get(4);
    }

    private Collection<Record> reallocateIds(Collection<Record> records) {

        return records.stream().map(record -> new Record(next_id++, record.getData(), record.getOriginalData(), record.getClassification())).collect(Collectors.toList());
    }

}
