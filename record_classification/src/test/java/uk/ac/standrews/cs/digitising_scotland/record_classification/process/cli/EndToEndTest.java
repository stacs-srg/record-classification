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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.Classifiers;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.ConsistentCodingChecker;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.serialization.SerializationFormat;
import uk.ac.standrews.cs.util.dataset.DataSet;
import uk.ac.standrews.cs.util.tools.FileManipulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class EndToEndTest extends EndToEndCommon {


    @Parameterized.Parameters(name = "{0}, {1}")
    public static Collection<Object[]> generateData() {

        List<Classifiers> classifiers = Arrays.asList(
                Classifiers.EXACT_MATCH,
                Classifiers.OLR,
                Classifiers.EXACT_MATCH_PLUS_VOTING_ENSEMBLE);

        List<SerializationFormat> serialization_formats = Arrays.asList(
                SerializationFormat.JSON,
                SerializationFormat.COMPRESSED_JSON,
                SerializationFormat.JAVA_SERIALIZATION);

        return allCombinations(classifiers, serialization_formats);
    }

    public EndToEndTest(Classifiers classifier_supplier, SerializationFormat serialization_format) {

        this.classifier_supplier = classifier_supplier;
        this.serialization_format = serialization_format;

        input_gold_standard_file = FileManipulation.getResourcePath(EndToEndTest.class, GOLD_STANDARD_FILE_NAME);

        input_unseen_data_file = FileManipulation.getResourcePath(EndToEndTest.class, EVALUATION_FILE_NAME);
    }

    @Test
    public void endToEndOLRProcess() throws Exception {

        checkInitialisationLoadingAndTraining();
        checkClassification();
    }

    private static Collection<Object[]> allCombinations(List<Classifiers> classifier_suppliers, List<SerializationFormat> serialization_formats) {

        List<Object[]> result = new ArrayList<>();

        for (Classifiers classifier_supplier : classifier_suppliers) {
            for (SerializationFormat serialization_format : serialization_formats) {
                result.add(new Object[]{classifier_supplier, serialization_format});
            }
        }

        return result;
    }

    private void checkInitialisationLoadingAndTraining() throws Exception {

        initLoadTrain();
        assertTrainedModelFileExists();
    }

    private void assertTrainedModelFileExists() {

        assertFileExists(output_trained_model_file);
    }

    private void checkClassification() throws Exception {

        final Path classified_file = loadCleanClassify();

        assertFileExists(classified_file);
        assertSameNumberOfRecords(classified_file, input_gold_standard_file);
        assertRecordsContainExpectedContent(classified_file);
        assertRecordsConsistentlyClassified(classified_file);
    }

    private void assertFileExists(Path path) {

        assertTrue(Files.exists(path));
    }

    private void assertSameNumberOfRecords(Path csv_file_1, Path csv_file_2) throws IOException {

        final DataSet data_set_1 = new DataSet(csv_file_1);
        final DataSet data_set_2 = new DataSet(csv_file_2);

        assertEquals(data_set_1.getRecords().size(), data_set_2.getRecords().size());
    }

    private void assertRecordsContainExpectedContent(Path classified_csv_file) throws IOException {

        final DataSet data_set = new DataSet(classified_csv_file);

        for (List<String> record : data_set.getRecords()) {

            assertFirstElementIsNumber(record);

            // Exact match classifier doesn't classify unknown data.
            if (classifier_supplier != Classifiers.EXACT_MATCH) {
                assertRecordIsClassified(record);
            }
        }
    }

    private void assertRecordsConsistentlyClassified(Path classified_csv_file) throws IOException {

        final Bucket bucket = new Bucket(new DataSet(classified_csv_file));

        assertTrue(new ConsistentCodingChecker().test(bucket));
    }

    private void assertRecordIsClassified(List<String> record) {

        String classification = record.get(2);
        assertNotNull(classification);
        assertNotEquals("", classification);
        assertNotEquals("null", classification);
        assertNotEquals(Classification.UNCLASSIFIED.getCode(), classification);
    }

    private void assertFirstElementIsNumber(List<String> record) {

        //noinspection ResultOfMethodCallIgnored
        Integer.parseInt(record.get(0));
    }
}
