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
package uk.ac.standrews.cs.digitising_scotland.record_classification.process;

import org.junit.Test;
import uk.ac.standrews.cs.digitising_scotland.record_classification.interfaces.ClassificationProcess;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.InfoLevel;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.single_classifier.ExactMatchClassificationProcess;

import java.io.InputStreamReader;

public class ExactMatchClassifierTest extends AbstractClassificationTest {

    @Test
    public void classifyUsingExactMatch() throws Exception {

        // This just tests that the classification process runs without error.

        InputStreamReader occupation_data_path = getInputStreamReaderForResource(AbstractClassificationTest.class, GOLD_STANDARD_DATA_FILE_NAME);

        ClassificationProcess classification_process = new ExactMatchClassificationProcess(occupation_data_path, 0.8);
        classification_process.setInfoLevel(InfoLevel.NONE);
        classification_process.trainClassifyAndEvaluate(3);
    }
}
