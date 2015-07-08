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
package uk.ac.standrews.cs.digitising_scotland.record_classification.process.experiments;


import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.AbstractMetricsTest;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.ClassificationMetrics;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.ConfusionMatrix;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.Classifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.ConsistentCodingCleaner;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.InfoLevel;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.ClassificationProcessWithContext;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.steps.AddTrainingAndEvaluationRecordsByRatio;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.steps.EvaluateClassifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.steps.TrainClassifier;
import uk.ac.standrews.cs.util.tools.FileManipulation;

import java.io.InputStreamReader;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class AbstractClassificationProcessTest extends AbstractMetricsTest {

    public static final String CODED_DATA_1K_FILE_NAME = "coded_data_1K.csv";
    public static final long SEED = 34234234234L;

    protected abstract Classifier getClassifier();

    protected ConfusionMatrix matrix;
    protected ClassificationMetrics metrics;

    @Before
    public void setup() throws Exception {

        InputStreamReader inputStreamReaderForResource = FileManipulation.getInputStreamReaderForResource(AbstractClassificationProcessTest.class, CODED_DATA_1K_FILE_NAME);
        final Bucket gold_standard = new Bucket(inputStreamReaderForResource);

        final ClassificationProcessWithContext process = new ClassificationProcessWithContext(getClassifier(), new Random(SEED));

        process.addStep(new AddTrainingAndEvaluationRecordsByRatio(gold_standard, 0.8, ConsistentCodingCleaner.CORRECT));
        process.addStep(new TrainClassifier());
        process.addStep(new EvaluateClassifier(InfoLevel.NONE));
        process.call();

        metrics = process.getClassificationMetrics();
        matrix = process.getConfusionMatrix();
    }

    @Test
    public void checkF1BetweenPrecisionAndRecall() throws Exception {

        checkMicroF1BetweenPrecisionAndRecall();
        checkMacroF1BetweenPrecisionAndRecall();
    }

    @Test
    public void checkMicroPrecisionRecallAndF1AreSame() throws Exception {

        assertEquals(metrics.getMicroAveragePrecision(), metrics.getMicroAverageRecall(), DELTA);
        assertEquals(metrics.getMicroAveragePrecision(), metrics.getMicroAverageF1(), DELTA);
    }

    private void checkMicroF1BetweenPrecisionAndRecall() {

        double micro_precision = metrics.getMicroAveragePrecision();
        double micro_recall = metrics.getMicroAverageRecall();
        double micro_f1 = metrics.getMicroAverageF1();

        assertBetween(micro_precision, micro_recall, micro_f1);
    }

    private void checkMacroF1BetweenPrecisionAndRecall() {

        Map<String, Double> per_class_precision = metrics.getPerClassPrecision();
        Map<String, Double> per_class_recall = metrics.getPerClassRecall();
        Map<String, Double> per_class_f1 = metrics.getPerClassF1();

        for (String code : per_class_precision.keySet()) {

            double precision = per_class_precision.get(code);
            double recall = per_class_recall.get(code);
            double f1 = per_class_f1.get(code);

            assertBetween(precision, recall, f1);
        }
    }

    private void assertBetween(Double d1, Double d2, Double d3) {

        if (!d1.isNaN() && !d2.isNaN() && !d3.isNaN()) {
            assertTrue((d1 <= d3 && d3 <= d2) || (d2 <= d3 && d3 <= d1));
        }
    }
}
