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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.logistic_regression;

import org.junit.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.logistic_regression.legacy.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.dataset.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Masih Hajiarab Derkani
 */
public class OLRClassifierTest extends ClassifierTest {

    private static final int SEED = 42;

    @Test(expected = UnsupportedOperationException.class)
    public void trainingAlreadyTrainedClassifierFails() throws Exception {

        final OLRClassifier classifier = newClassifier();

        assertFalse(classifier.isTrained());
        classifier.trainModel(training_bucket);
        assertTrue(classifier.isTrained());
        classifier.trainModel(training_bucket); //expected exception
    }

    @Test
    public void mahoutOLRClassifierOutperformsLegacyOLRClassifier() throws Exception {

        TestDataSets.ALL_TRAINING_DATASETS.stream().map(TestDataSet::getBucket).forEach(this::assertMahoutOutperformsLegacy);
    }

    private void assertMahoutOutperformsLegacy(final Bucket gold_standard) {

        
        final Bucket cleaned_gold_standard = CleanerSupplier.CONSISTENT_CLASSIFICATION_CLEANER_CORRECT.get().apply(gold_standard);
        final Bucket training = cleaned_gold_standard.randomSubset(new Random(SEED), 0.8);
        final Bucket evaluation = cleaned_gold_standard.difference(training).stripRecordClassifications();

        final LegacyOLRClassifier legacy = new LegacyOLRClassifier();
        legacy.trainModel(training);
        final StrictConfusionMatrix legacy_matrix = new StrictConfusionMatrix(legacy.classify(evaluation), cleaned_gold_standard);
        final Metrics legacy_metrics = new Metrics(legacy_matrix);

        final OLRClassifier mahout = newClassifier();
        mahout.trainModel(training);
        final StrictConfusionMatrix mahout_matrix = new StrictConfusionMatrix(mahout.classify(evaluation), cleaned_gold_standard);
        final Metrics mahout_metrics = new Metrics(mahout_matrix);

        assertTrue(legacy_metrics.getMacroAverageAccuracy() <= mahout_metrics.getMacroAverageAccuracy());
        assertTrue(legacy_metrics.getMacroAverageF1() <= mahout_metrics.getMacroAverageF1());
        assertTrue(legacy_metrics.getMacroAveragePrecision() <= mahout_metrics.getMacroAveragePrecision());
        assertTrue(legacy_metrics.getMacroAverageRecall() <= mahout_metrics.getMacroAverageRecall());
        assertTrue(legacy_metrics.getMicroAverageAccuracy() <= mahout_metrics.getMicroAverageAccuracy());
        assertTrue(legacy_metrics.getMicroAverageF1() <= mahout_metrics.getMicroAverageF1());
        assertTrue(legacy_metrics.getMicroAveragePrecision() <= mahout_metrics.getMicroAveragePrecision());
        assertTrue(legacy_metrics.getMicroAverageRecall() <= mahout_metrics.getMicroAverageRecall());
    }

    @Override
    protected OLRClassifier newClassifier() {

        return new OLRClassifier();
    }
}
