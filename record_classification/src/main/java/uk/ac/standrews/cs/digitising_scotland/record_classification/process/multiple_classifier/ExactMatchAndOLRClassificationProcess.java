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
package uk.ac.standrews.cs.digitising_scotland.record_classification.process.multiple_classifier;

import uk.ac.standrews.cs.digitising_scotland.record_classification.interfaces.ClassificationProcess;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.InfoLevel;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.single_classifier.ExactMatchClassificationProcess;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.single_classifier.OLRClassificationProcess;

import java.util.Arrays;
import java.util.List;

public class ExactMatchAndOLRClassificationProcess extends AbstractMultipleClassificationProcess {

    public static void main(final String[] args)  {

        try {
            ExactMatchAndOLRClassificationProcess classification_process = new ExactMatchAndOLRClassificationProcess();

            classification_process.process(args);
        }
        catch (Exception e) {
            System.out.println("problem in classification process: " + e.getMessage());
        }
    }

    protected List<ClassificationProcess> getClassificationProcesses(String[] args) throws Exception {

        ClassificationProcess process1 = new ExactMatchClassificationProcess(args);
        ClassificationProcess process2 = new OLRClassificationProcess(args);

        return Arrays.asList(process1, process2);
    }

    @Override
    public InfoLevel getInfoLevel() {

        return InfoLevel.LONG_SUMMARY;
    }
}