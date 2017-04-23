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

import java.io.*;

/**
 * A data record.
 *
 * @author Graham Kirby
 * @author Masih Hajiarab Derkani
 */
public class Record implements Comparable<Record>, Serializable {

    private static final long serialVersionUID = 5810954671977163993L;

    private int id;
    private String data;
    private String original_data;
    private Classification classification;

    private int hash_code;

    /**
     * Required for JSON deserialization.
     */
    public Record() {

    }

    /**
     * Instantiates an {@link Classification#UNCLASSIFIED unclassified} record.
     *
     * @param id the unique record identifier
     * @param data the record data
     */
    public Record(final int id, final String data) {

        this(id, data, Classification.UNCLASSIFIED);
    }

    public Record(final int id, final String data, final String original_data) {

        this(id, data, original_data, Classification.UNCLASSIFIED);
    }

    /**
     * Instantiates a record classified by the given classification.
     *
     * @param id the unique record identifier
     * @param data the record data
     * @param classification the classification of this record
     * @throws NullPointerException if the given classification is {@code null}
     */
    public Record(final int id, final String data, final Classification classification) {

        this(id, data, data, classification);
    }

    public Record(final int id, final String data, final String original_data, final Classification classification) {

        this.id = id;
        this.data = data;
        this.original_data = original_data;
        this.classification = classification;

        // Previously used Objects.hash(id, data, classification) but that gave clashes.
        hash_code = id;
    }

    public int getId() {

        return id;
    }

    /**
     * Gets the data of this record.
     *
     * @return the data of this record
     */
    public String getData() {

        return data;
    }

    public String getOriginalData() {

        return original_data;
    }

    /**
     * Gets the classification of this record.
     *
     * @return the classification of this record.
     */
    public Classification getClassification() {

        return classification;
    }

    @Override
    public boolean equals(Object other) {

        return other instanceof Record && hash_code == ((Record) other).hash_code;
    }

    @Override
    public int hashCode() {

        return hash_code;
    }

    @Override
    public String toString() {

        return "Record [id=" + id + ", data=" + data + ", orig_data=" + original_data + ", classification=" + classification + "]";
    }

    @Override
    public int compareTo(Record other) {

        return hash_code - other.hash_code;
    }
}
