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

import org.junit.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.util.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.dataset.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.serialization.*;
import uk.ac.standrews.cs.util.tools.*;

import java.nio.file.*;

import static org.junit.Assert.*;

/**
 * @author Masih Hajiarab Derkani
 */
public class ConfigurationTest {

    private Configuration expected;
    private Path home;

    @Before
    public void setUp() throws Exception {

        expected = new Configuration();
        home = expected.getHome();
    }

    @After
    public void tearDown() throws Exception {

        if (Files.isDirectory(home)) {
            FileManipulation.deleteDirectory(home);
        }
    }

    @Test
    public void testSerializationAndDeserialization() throws Exception {

        if (!Files.isDirectory(home)) {
            Files.createDirectory(home);
        }

        final Bucket unseen_records = TestDataSets.CASE_5_EVALUATION.get(0).getBucket();
        expected.setUnseenRecords(unseen_records);

        final Bucket gold_standard_records = TestDataSets.CASE_5_TRAINING.get(0).getBucket();
        expected.setGoldStandardRecords(gold_standard_records, 0.8);

        expected.setClassifierSupplier(ClassifierSupplier.EXACT_MATCH);
        expected.setClassifierSerializationFormat(SerializationFormat.JAVA_SERIALIZATION);
        expected.setDefaultCharsetSupplier(CharsetSupplier.UTF_16);
        expected.setDefaultLogLevelSupplier(LogLevelSupplier.INFO);
        expected.setSeed(42L);
        expected.setDefaultCsvFormatSupplier(CsvFormatSupplier.RFC4180_PIPE_SEPARATED);
        expected.setDefaultTrainingRatio(0.7);
        expected.setDefaultInternalTrainingRatio(0.1);

        expected.persist();

        final Configuration actual = Configuration.load();

        assertEquals(expected.getUnseenRecords(), actual.getUnseenRecords());
        assertEquals(expected.getGoldStandardRecords(), actual.getGoldStandardRecords());
        assertEquals(expected.getTrainingRecords(), actual.getTrainingRecords());
        assertEquals(expected.getEvaluationRecords(), actual.getEvaluationRecords());
        assertEquals(expected.getClassifierSupplier(), actual.getClassifierSupplier());
        assertEquals(expected.getClassifierSerializationFormat(), actual.getClassifierSerializationFormat());
        assertEquals(expected.getDefaultCharsetSupplier(), actual.getDefaultCharsetSupplier());
        assertEquals(expected.getDefaultLogLevelSupplier(), actual.getDefaultLogLevelSupplier());
        assertEquals(expected.getSeed(), actual.getSeed());
        assertEquals(expected.getDefaultCsvFormatSupplier(), actual.getDefaultCsvFormatSupplier());
        assertEquals(expected.getDefaultTrainingRatio(), actual.getDefaultTrainingRatio(), Validators.DELTA);
        assertEquals(expected.getDefaultInternalTrainingRatio(), actual.getDefaultInternalTrainingRatio(), Validators.DELTA);
        assertEquals(expected.getClassifierOptional(), actual.getClassifierOptional());
        assertEquals(expected.getLogLevel(), actual.getLogLevel());
        assertEquals(expected.getTrainingRecordsOptional(), actual.getTrainingRecordsOptional());
        assertEquals(expected.getEvaluationRecordsOptional(), actual.getEvaluationRecordsOptional());
        assertEquals(expected.getUnseenRecordsOptional(), actual.getUnseenRecordsOptional());
        assertEquals(expected.getUnseenRecordsOptional(), actual.getUnseenRecordsOptional());
    }

    @Test
    public void testDefaultValuesAreSetInNewInstance() throws Exception {

        final Configuration new_instance = new Configuration();

        assertEquals(new_instance.getDefaultCharsetSupplier(), Configuration.DEFAULT_CHARSET_SUPPLIER);
        assertEquals(new_instance.getDefaultCsvFormatSupplier(), Configuration.DEFAULT_CSV_FORMAT_SUPPLIER);
        assertEquals(new_instance.getDefaultDelimiter(), Configuration.DEFAULT_DELIMITER);
        assertEquals(new_instance.getDefaultTrainingRatio(), Configuration.DEFAULT_TRAINING_RATIO, Validators.DELTA);
        assertEquals(new_instance.getDefaultInternalTrainingRatio(), Configuration.DEFAULT_INTERNAL_TRAINING_RATIO, Validators.DELTA);
        assertEquals(new_instance.getDefaultLogLevelSupplier(), Configuration.DEFAULT_LOG_LEVEL_SUPPLIER);
    }
}
