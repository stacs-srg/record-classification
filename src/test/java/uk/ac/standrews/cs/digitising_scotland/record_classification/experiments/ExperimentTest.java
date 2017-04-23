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
package uk.ac.standrews.cs.digitising_scotland.record_classification.experiments;

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.digitising_scotland.record_classification.experiments.generic.Experiment;
import uk.ac.standrews.cs.digitising_scotland.record_classification.experiments.specific.ExactMatchAndStringSimilarityExperiment;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.ClassificationContext;
import uk.ac.standrews.cs.utilities.FileManipulation;
import uk.ac.standrews.cs.utilities.Logging;
import uk.ac.standrews.cs.utilities.LoggingLevel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ExperimentTest {

    public static final String CODED_DATA_1K_FILE_NAME = "coded_data_1K.csv";
    public static final int NUMBER_OF_REPETITIONS = 5;

    private Experiment experiment;
    private List<Experiment.ClassifierResults> experiment_results;

    @Before
    public void setup() throws Exception {

        Logging.setLoggingLevel(LoggingLevel.NONE);

        experiment = new ExactMatchAndStringSimilarityExperiment();

        Path path = FileManipulation.getResourcePath(AbstractClassificationProcessTest.class, CODED_DATA_1K_FILE_NAME);

        experiment.setGoldStandardFiles(Arrays.asList(path));
        experiment.setRepetitions(NUMBER_OF_REPETITIONS);

        experiment_results = experiment.runExperiment();
    }

    @Test
    public void resultsContainDataForCorrectNumberOfClassifiers() throws Exception {

        assertEquals(experiment_results.size(), experiment.getProcesses().size());
    }

    @Test
    public void numbersOfTrainingRecordsInEachRepetitionAreSameAcrossExperiments() throws Exception {

        List<List<Integer>> training_bucket_sizes = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_REPETITIONS; i++) {
            training_bucket_sizes.add(new ArrayList<>());
        }

        for (Experiment.ClassifierResults result : experiment_results) {

            int repetition_number = 0;
            for (ClassificationContext context : result.getContexts()) {

                training_bucket_sizes.get(repetition_number++).add(context.getTrainingRecords().size());
            }
        }

        training_bucket_sizes.forEach(this::assertAllSame);
    }

    private void assertAllSame(List<Integer> numbers) {

        Integer first = numbers.get(0);
        numbers.stream().filter(i -> !Objects.equals(i, first)).forEach(i -> fail());
    }
}
