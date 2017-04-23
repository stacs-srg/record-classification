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

import org.junit.Test;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.dataset.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Masih Hajiarab Derkani
 */
public class EvaluateCommandTest extends CommandTest {

    @Test(expected = RuntimeException.class)
    public void testEvaluationFailureWithNoGoldStandard() throws Exception {

        init();
        evaluate();
    }

    @Test(expected = RuntimeException.class)
    public void testEvaluationFailureWithNoEvaluationRecords() throws Exception {

        init();
        setClassifier(ClassifierSupplier.EXACT_MATCH);
        loadGoldStandards(TestDataSets.CASE_1_TRAINING, 1.0);
        evaluate();
    }

    @Test
    public void testEvaluationWithOutput() throws Exception {

        initForcefully();
        setSeed();
        setClassifier(ClassifierSupplier.EXACT_MATCH);
        loadGoldStandards(TestDataSets.CASE_1_TRAINING, 0.8);
        clean(CleanerSupplier.COMBINED);
        final Path output = temporary.newFile().toPath();
        evaluate(output);

        assertEvaluationResultsIsSet();
        assertEvaluationOutputIsCorrect(output);
    }

    private void assertEvaluationResultsIsSet() {

        assertNotNull(configuration.getConfusionMatrix());
        assertNotNull(configuration.getClassificationMetrics());
        assertNotNull(configuration.getEvaluationClassificationTime());
    }

    private void assertEvaluationOutputIsCorrect(final Path output) throws IOException {

        assertRegularFile(output);

        final Bucket records = Configuration.loadBucket(output);
        assertConsistentClassification(records);
    }

    private void assertRegularFile(final Path output) {assertTrue(Files.isRegularFile(output));}

    private void assertConsistentClassification(final Bucket records) {assertTrue(new ConsistentCodingChecker().test(Collections.singletonList(records)));}
}
