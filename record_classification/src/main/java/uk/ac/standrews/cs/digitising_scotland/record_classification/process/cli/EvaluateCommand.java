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
package uk.ac.standrews.cs.digitising_scotland.record_classification.process.cli;

import com.beust.jcommander.*;
import com.beust.jcommander.converters.*;
import org.apache.commons.csv.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.steps.*;
import uk.ac.standrews.cs.util.dataset.*;

import java.io.*;
import java.util.*;

/**
 * @author Masih Hajiarab Derkani
 */
@Parameters(commandNames = EvaluateCommand.NAME, commandDescription = "Evaluate classifier")
class EvaluateCommand extends Command {

    /** The name of this command */
    public static final String NAME = "evaluate";
    private static final long serialVersionUID = 4285779171774505978L;

    @Parameter(required = true, names = {"-o", "--output"}, description = "Path to the place to persist the evaluation results.", converter = FileConverter.class)
    private File destination;

    @Parameter(names = {"-r", "--repetitionCount"}, description = "The number of repetitions.", validateValueWith = Validators.AtLeastOne.class)
    private int repetition_count = 1;

    @Parameter(names = {"-d", "--delimiter"}, description = "The delimiter character of the output results.")
    private char delimiter = '|';

    @Override
    public void perform(final ClassificationContext context) throws Exception {

        List<ClassificationMetrics> results = new ArrayList<>();
        for (int i = 0; i < repetition_count; i++) {

            new EvaluateClassifier().perform(context);
            results.add(context.getClassificationMetrics());
        }

        final CSVFormat format = CSVFormat.newFormat(delimiter);
        final DataSet dataset = ClassificationMetrics.toDataSet(results, format);
        persistDataSet(destination.toPath(), dataset);
    }
}
