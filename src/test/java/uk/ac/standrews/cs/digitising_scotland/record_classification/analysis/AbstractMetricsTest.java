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
package uk.ac.standrews.cs.digitising_scotland.record_classification.analysis;

import org.junit.Before;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.InputFileFormatException;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.TokenList;
import uk.ac.standrews.cs.utilities.FileManipulation;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class AbstractMetricsTest {

    protected static final double DELTA = 0.001;

    private static int next_record_id = 1;

    protected static final Record haddock_correct = makeRecord("haddock", "fish");
    protected static final Record haddock_incorrect = makeRecord("haddock", "mammal");
    protected static final Record osprey_incorrect = makeRecord("osprey", "mammal");
    protected static final Record unicorn_unclassified = makeRecord("unicorn");
    protected static final Record haddock_gold_standard = makeRecord("haddock", "fish");
    protected static final Record cow_gold_standard = makeRecord("cow", "mammal");

    protected Bucket classified_records;
    protected Bucket gold_standard_records;
    protected StrictConfusionMatrix matrix;

    private static final String CLASSIFIED_FILE_NAME = "example_classified.csv";
    private static final String GOLD_STANDARD_FILE_NAME = "example_gold_standard.csv";

    DataSet classified_records_csv;
    DataSet gold_standard_records_csv;

    @Before
    public void setUp() throws Exception {

        classified_records = new Bucket();
        gold_standard_records = new Bucket();

        try (InputStreamReader reader = FileManipulation.getInputStreamReaderForResource(AbstractMetricsTest.class, CLASSIFIED_FILE_NAME)) {

            classified_records_csv = new DataSet(reader, ',');
        }
        try (InputStreamReader reader = FileManipulation.getInputStreamReaderForResource(AbstractMetricsTest.class, GOLD_STANDARD_FILE_NAME)) {

            gold_standard_records_csv = new DataSet(reader, ',');
        }
    }

    protected void initMatrix() throws Exception {

        matrix = new StrictConfusionMatrix(classified_records, gold_standard_records);
    }

    protected void initFullRecords() throws InputFileFormatException {

        classified_records = new Bucket(classified_records_csv);
        gold_standard_records = new Bucket(gold_standard_records_csv);
    }

    protected int getNumberOfCodes() {

        Set<String> valid_codes = new HashSet<>();

        for (Record record : gold_standard_records) {

            valid_codes.add(record.getClassification().getCode());
        }
        valid_codes.add(Classification.UNCLASSIFIED.getCode());
        return valid_codes.size();
    }

    protected static Record makeRecord(String data, Classification classification) {

        return new Record(next_record_id++, data, classification);
    }

    protected static Record makeRecord(String data, String code) {

        return makeRecord(data, new Classification(code, new TokenList(data), 1.0, null));
    }

    protected static Record makeRecord(String data) {

        return makeRecord(data, Classification.UNCLASSIFIED);
    }
}
