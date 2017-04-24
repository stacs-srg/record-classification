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

import org.apache.commons.csv.CSVFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.ClassifierSupplier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.CleanerSupplier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.ConsistentCodingChecker;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Configuration;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.CharsetSupplier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.CsvFormatSupplier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.LogLevelSupplier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.dataset.TestDataSet;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.*;
import static uk.ac.standrews.cs.digitising_scotland.record_classification.dataset.TestDataSets.*;

/**
 * @author Graham Kirby
 * @author Masih Hajiarab Derkani
 */
@RunWith(Parameterized.class)
public class ClassifyCommandTest extends CommandTest {

    public static final List<ClassifierSupplier> CLASSIFIERS = Arrays.asList(ClassifierSupplier.EXACT_MATCH, ClassifierSupplier.STRING_SIMILARITY_JARO_WINKLER, ClassifierSupplier.OLR, ClassifierSupplier.NAIVE_BAYES, ClassifierSupplier.VOTING_ENSEMBLE_EXACT_ML_SIMILARITY);
    private ClassifierSupplier classifier_supplier;
    private List<TestDataSet> gold_standards;
    private List<TestDataSet> unseens;
    private CharsetSupplier charset;
    private CSVFormat format;

    @Parameterized.Parameters(name = "{index} {0} {3}")
    public static Collection<Object[]> data() {

        final List<Object[]> parameters = new ArrayList<>();
        for (ClassifierSupplier classifier_supplier : CLASSIFIERS) {
            for (CharsetSupplier charset_supplier : CharsetSupplier.values()) {
                for (CsvFormatSupplier format_supplier : CsvFormatSupplier.values()) {
                    parameters.add(new Object[]{classifier_supplier, CASE_2_TRAINING, CASE_2_EVALUATION, charset_supplier, format_supplier.get()});
                    parameters.add(new Object[]{classifier_supplier, CASE_4_TRAINING, CASE_4_EVALUATION, charset_supplier, format_supplier.get()});
                }
            }
        }
        return parameters;
    }

    public ClassifyCommandTest(ClassifierSupplier classifier_supplier, List<TestDataSet> gold_standards, List<TestDataSet> unseens, CharsetSupplier charset, CSVFormat format) throws IOException {

        this.classifier_supplier = classifier_supplier;
        this.gold_standards = gold_standards;
        this.unseens = unseens;
        this.charset = charset;
        this.format = format;
    }

    @Test
    public void testClassification() throws Exception {

        initForcefully();
        setVerbosity(LogLevelSupplier.OFF);
        setClassifier(classifier_supplier);
        loadGoldStandards(gold_standards, charset, format, 1.0);
        loadUnseens(unseens, charset, format);
        clean(CleanerSupplier.COMBINED);
        train();
        final Path classified_output = classify();

        checkClassification(classified_output);
    }

    private void checkClassification(Path classified_file) throws Exception {

        assertFileExists(classified_file);
        assertSameNumberOfRecords(classified_file, unseens);
        assertRecordsContainExpectedContent(classified_file);
        assertRecordsConsistentlyClassified(classified_file);
    }

    private void assertFileExists(Path path) {

        assertTrue(Files.exists(path));
    }

    private void assertSameNumberOfRecords(Path csv_file_1, List<TestDataSet> records) throws IOException {

        final long expected_count = records.stream().mapToLong(TestDataSet::getRecordsCount).sum();
        final DataSet data_set_1 = new DataSet(csv_file_1);
        assertEquals(expected_count, data_set_1.getRecords().size());
    }

    private void assertRecordsContainExpectedContent(Path classified_csv_file) throws IOException {

        new DataSet(classified_csv_file).getRecords().forEach(this::assertRecordContainsExpectedContent);
    }

    private void assertRecordContainsExpectedContent(List<String> record) {

        assertRecordContainsId(record);

        // Exact match classifier doesn't classify unknown data.
        if (!classifier_supplier.isExactMatch()) {
            assertRecordContainsClassification(record);
            assertRecordContainsConfidence(record);
        }

        if (classifier_supplier.isEnsemble()) {
            assertRecordContainsEnsembleDetail(record);
        }
    }

    private void assertRecordsConsistentlyClassified(Path classified_csv_file) throws IOException {

        final Bucket bucket = Configuration.loadBucket(classified_csv_file);

        assertTrue(new ConsistentCodingChecker().test(Collections.singletonList(bucket)));
    }

    private void assertRecordContainsId(List<String> record) {

        //noinspection ResultOfMethodCallIgnored
        Integer.parseInt(getId(record));
    }

    private void assertRecordContainsClassification(List<String> record) {

        String classification = getClassification(record);
        assertPresent(classification);
        assertNotEquals(Classification.UNCLASSIFIED.getCode(), classification);
    }

    private void assertPresent(String value) {

        assertNotNull(value);
        assertNotEquals("", value);
        assertNotEquals("null", value);
    }

    private void assertRecordContainsConfidence(List<String> record) {

        assertBetween(Double.parseDouble(getConfidence(record)), 0.0, 1.0);
    }

    private void assertRecordContainsEnsembleDetail(List<String> record) {

        assertPresent(getEnsembleDetail(record));
    }

    private void assertBetween(double value, double lower, double higher) {

        assertTrue(value >= lower);
        assertTrue(value <= higher);
    }

    private String getId(List<String> record) {

        return record.get(0);
    }

    private String getClassification(List<String> record) {

        return record.get(3);
    }

    private String getConfidence(List<String> record) {

        return record.size() > 4 ? record.get(4) : "";
    }

    private String getEnsembleDetail(List<String> record) {

        return record.size() > 5 ? record.get(5) : null;
    }
}
