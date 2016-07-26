/*
 * record_classification - Automatic record attribute classification.
 * Copyright © 2012-2016 Digitising Scotland project
 * (http://digitisingscotland.cs.st-andrews.ac.uk/record_classification/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.digitising_scotland.record_classification.experiments;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.ClassifierSupplier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.UnknownDataException;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.ClassificationContext;
import uk.ac.standrews.cs.digitising_scotland.record_classification.experiments.generic.EvaluationExperimentProcess;
import uk.ac.standrews.cs.util.tools.FileManipulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ClassificationProcessInternalTest {

    public static final String TRAINING_DATA_1_FILE_NAME = "training_set1.csv";
    public static final String TRAINING_DATA_2_FILE_NAME = "training_set2.csv";
    public static final String TRAINING_DATA_3_FILE_NAME = "training_set3.csv";

    public static final long SEED = 234252345;
    private static final double DELTA = 0.001;

    private List<Path> gold_standard_files;
    List<Double> training_ratios;

    public ClassificationProcessInternalTest(List<Path> gold_standard_files, List<Double> training_ratios) {

        this.gold_standard_files = gold_standard_files;
        this.training_ratios = training_ratios;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> generateData() {

        Path path1 = FileManipulation.getResourcePath(ClassificationProcessInternalTest.class, TRAINING_DATA_1_FILE_NAME);
        Path path2 = FileManipulation.getResourcePath(ClassificationProcessInternalTest.class, TRAINING_DATA_2_FILE_NAME);
        Path path3 = FileManipulation.getResourcePath(ClassificationProcessInternalTest.class, TRAINING_DATA_3_FILE_NAME);

        List<Object[]> result = new ArrayList<>();

        result.add(new Object[]{Arrays.asList(path1, path2), Arrays.asList(1.0, 0.5)});
        result.add(new Object[]{Arrays.asList(path1, path2, path3), Arrays.asList(1.0, 1.0, 1.0)});
        result.add(new Object[]{Arrays.asList(path1, path2, path3), Arrays.asList(1.0, 0.5, 0.0)});
        result.add(new Object[]{Arrays.asList(path1, path2, path3), Arrays.asList(1.0, 1.0, 0.5)});

        return result;
    }

    @Test
    public void multipleGoldStandardFiles() throws Exception {

        final EvaluationExperimentProcess process = new EvaluationExperimentProcess();

        process.setGoldStandardFiles(gold_standard_files);
        process.setTrainingRatios(training_ratios);

        process.configureSteps();

        ClassificationContext context = new ClassificationContext(ClassifierSupplier.EXACT_MATCH.get(), new Random(SEED));

        try {
            process.call(context);
        }
        catch (UnknownDataException e) {
            // Don't care about evaluation data not being in the gold standard.
        }

        int training_records_size = context.getTrainingRecords().size();
        int evaluation_records_size = context.getEvaluationRecords().size();

        assertEquals(totalGoldStandardRecordsSize(), training_records_size + evaluation_records_size);
        assertEquals(totalEvaluationRecordsSize(), evaluation_records_size);

        for (int i = 0; i < gold_standard_files.size(); i++) {

            Path path = gold_standard_files.get(i);
            Double ratio = training_ratios.get(i);

            assertEquals(ratio, proportionOfRecordsInBucket(path, context.getTrainingRecords()), DELTA);
            double actual = proportionOfRecordsInBucket(path, context.getEvaluationRecords());
            assertEquals(1.0 - ratio, actual, DELTA);
        }
    }

    private double proportionOfRecordsInBucket(Path gold_standard_path, Bucket bucket) throws IOException {

        double number_of_records = 0.0;
        double number_of_records_in_training_bucket = 0.0;

        try (BufferedReader reader = Files.newBufferedReader(gold_standard_path, FileManipulation.FILE_CHARSET)) {

            for (Record r : new Bucket(reader, ',')) {

                number_of_records++;
                if (bucket.containsData(r.getData())) number_of_records_in_training_bucket++;
            }
        }

        return number_of_records_in_training_bucket / number_of_records;
    }

    private int totalGoldStandardRecordsSize() throws IOException {

        int total = 0;
        for (Path path : gold_standard_files) {
            total += FileManipulation.countLinesInFile(path) - 1;
        }
        return total;
    }

    private int totalEvaluationRecordsSize() throws IOException {

        int total = 0;
        for (int i = 0; i < gold_standard_files.size(); i++) {
            total += (FileManipulation.countLinesInFile(gold_standard_files.get(i)) - 1) * (1.0 - training_ratios.get(i));
        }
        return total;
    }
}
