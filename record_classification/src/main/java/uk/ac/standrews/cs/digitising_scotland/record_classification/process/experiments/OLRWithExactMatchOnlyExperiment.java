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

import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.OLRWithExactMatchClassifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.ClassificationProcess;

import java.io.*;
import java.util.List;

public class OLRWithExactMatchOnlyExperiment extends Experiment {

    protected OLRWithExactMatchOnlyExperiment(final String[] args) {

        super(args);
    }

    public static void main(final String[] args) throws Exception {

        final OLRWithExactMatchOnlyExperiment experiment = new OLRWithExactMatchOnlyExperiment(args);
        experiment.call();
    }

    @Override
    protected List<ClassificationProcess> getClassificationProcesses() throws IOException, InputFileFormatException {

        return initClassificationProcesses(new OLRWithExactMatchClassifier());
    }
}
