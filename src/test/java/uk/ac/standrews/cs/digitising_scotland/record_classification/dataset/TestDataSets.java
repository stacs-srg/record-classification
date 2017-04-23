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
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.*;

import java.nio.charset.*;
import java.util.*;

/**
 * @author Masih Hajiarab Derkani
 */
public final class TestDataSets {

    public static final CSVFormat NRS_SAMPLE_CSV_FORMAT = CsvFormatSupplier.RFC4180_PIPE_SEPARATED.get().withIgnoreEmptyLines().withHeader();

    public static final TestDataSet UNCLEAN_TRAINING = getTestDataSet("cleaning/test_training_data.csv");
    public static final List<TestDataSet> CASE_1_TRAINING = Collections.singletonList(getTestDataSet("case1/test_training_data.csv", StandardCharsets.US_ASCII));

    public static final List<TestDataSet> CASE_1_EVALUATION = Collections.singletonList(getEvaluationTestDataSet("case1/test_evaluation_UTF8_unix.csv"));

    public static final List<TestDataSet> CASE_2_TRAINING = Collections.singletonList(getTestDataSet("case2/test_training_UTF8_unix.csv", StandardCharsets.US_ASCII));

    public static final List<TestDataSet> CASE_2_EVALUATION = Collections.singletonList(getEvaluationTestDataSet("case2/test_evaluation_UTF8_windows.txt", NRS_SAMPLE_CSV_FORMAT));

    public static final List<TestDataSet> CASE_3_TRAINING = Arrays.asList(getTestDataSet("case3/gold_standard1.csv", StandardCharsets.US_ASCII), getTestDataSet("case3/gold_standard2.csv", StandardCharsets.US_ASCII));

    public static final List<TestDataSet> CASE_3_EVALUATION = Collections.singletonList(getEvaluationTestDataSet("case3/unseen_data.csv"));

    public static final List<TestDataSet> CASE_4_TRAINING = Arrays.asList(getTestDataSet("case4/gold_standard1.csv", StandardCharsets.US_ASCII), getTestDataSet("case4/gold_standard2.csv", StandardCharsets.US_ASCII));

    public static final List<TestDataSet> CASE_4_EVALUATION = Collections.singletonList(getEvaluationTestDataSet("case4/unseen_data.csv"));

    public static final List<TestDataSet> CASE_5_TRAINING = Arrays
                    .asList(getTestDataSet("case5/test_training_ascii_unix.csv", StandardCharsets.US_ASCII), getTestDataSet("case5/test_training_iso_latin1_unix.csv", StandardCharsets.ISO_8859_1), getTestDataSet("case5/test_training_UTF16_unix.csv", StandardCharsets.UTF_16),
                                    getTestDataSet("case5/test_training_windows_windows.csv", StandardCharsets.ISO_8859_1));

    public static final List<TestDataSet> CASE_5_EVALUATION = Collections.singletonList(getEvaluationTestDataSet("case5/test_evaluation_ascii_windows.csv", StandardCharsets.US_ASCII));

    public static final List<TestDataSet> ALL_TRAINING_DATASETS = new ArrayList<>();

    static {
        ALL_TRAINING_DATASETS.addAll(CASE_1_TRAINING);
        ALL_TRAINING_DATASETS.addAll(CASE_2_TRAINING);
        ALL_TRAINING_DATASETS.addAll(CASE_3_TRAINING);
        ALL_TRAINING_DATASETS.addAll(CASE_4_TRAINING);
        ALL_TRAINING_DATASETS.addAll(CASE_5_TRAINING);
    }

    private TestDataSets() {

        throw new UnsupportedOperationException();
    }

    private static TestDataSet getTestDataSet(String resource_name) {

        return new TestDataSet(TestDataSet.class, resource_name);
    }

    private static TestDataSet getTestDataSet(String resource_name, final Charset charset) {

        return new TestDataSet(TestDataSets.class, resource_name, charset, TestDataSet.DEFAULT_CSV_FORMAT);
    }

    private static TestDataSet getEvaluationTestDataSet(String resource_name) {

        return getEvaluationTestDataSet(resource_name, TestDataSet.DEFAULT_CSV_FORMAT);
    }

    private static TestDataSet getEvaluationTestDataSet(String resource_name, Charset charset) {

        return new TestDataSet(0, 1, null, TestDataSets.class, resource_name, charset, TestDataSet.DEFAULT_CSV_FORMAT);
    }

    private static TestDataSet getEvaluationTestDataSet(String resource_name, CSVFormat format) {

        return new TestDataSet(0, 1, null, TestDataSets.class, resource_name, StandardCharsets.UTF_8, format);
    }
}
