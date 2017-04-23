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

import com.beust.jcommander.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.util.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.time.*;

import static uk.ac.standrews.cs.digitising_scotland.record_classification.cli.command.SetCommand.OPTION_INTERNAL_TRAINING_RATIO_LONG;
import static uk.ac.standrews.cs.digitising_scotland.record_classification.cli.command.SetCommand.OPTION_INTERNAL_TRAINING_RATIO_SHORT;

/**
 * The train command of classification process command line interface.
 *
 * @author Masih Hajiarab Derkani
 */
@Parameters(commandNames = TrainCommand.NAME, resourceBundle = Configuration.RESOURCE_BUNDLE_NAME, commandDescriptionKey = "command.train.description")
public class TrainCommand extends Command {

    /** The name of this command. */
    public static final String NAME = "train";

    @Parameter(names = {OPTION_INTERNAL_TRAINING_RATIO_SHORT, OPTION_INTERNAL_TRAINING_RATIO_LONG},
               descriptionKey = "command.train.internal_training_ratio.description",
               validateValueWith = Validators.BetweenZeroToOneInclusive.class)
    private Double internal_training_ratio = configuration.getDefaultInternalTrainingRatio();

    /**
     * Instantiates this command for the given launcher.
     *
     * @param launcher the launcher to which this command belongs.
     */
    public TrainCommand(final Launcher launcher) { super(launcher, NAME); }

    @Override
    public void run() {

        final Configuration configuration = launcher.getConfiguration();
        final Classifier classifier = configuration.requireClassifier();
        final Bucket training_records = configuration.requireTrainingRecords();

        logger.info(() -> String.format("training classifier %s...", configuration.getClassifierSupplier()));

        final Instant start = Instant.now();
        classifier.trainAndEvaluate(training_records, internal_training_ratio, configuration.getRandom());
        final Duration training_time = Duration.between(start, Instant.now());

        configuration.setTrainingTime(training_time);

        logger.info(() -> String.format("trained the classifier on %d records in %s", training_records.size(), formatDuration(training_time)));
    }

    public static class Builder extends Command.Builder {

        private Double internal_training_ratio;

        public void setInternalTrainingRatio(double ratio) {

            this.internal_training_ratio = ratio;
        }

        @Override
        protected void populateArguments() {

            addArgument(NAME);
            if (internal_training_ratio != null) {
                addArgument(OPTION_INTERNAL_TRAINING_RATIO_SHORT);
                addArgument(String.valueOf(internal_training_ratio));
            }
        }
    }
}
