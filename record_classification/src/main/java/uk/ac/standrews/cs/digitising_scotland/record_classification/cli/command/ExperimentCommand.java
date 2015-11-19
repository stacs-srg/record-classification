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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cli.command;

import com.beust.jcommander.*;
import com.beust.jcommander.converters.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Validators.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import static uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Launcher.OPTION_COMMANDS_LONG;
import static uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Launcher.OPTION_COMMANDS_SHORT;

/**
 * Runs a batch of commands multiple times in separate contexts within the same JVM and aggregates evaluation results if present.
 *
 * @author Masih Hajiarab Derkani
 */
@Parameters(commandNames = ExperimentCommand.NAME, resourceBundle = Configuration.RESOURCE_BUNDLE_NAME, commandDescriptionKey = "command.experiment.description")
public class ExperimentCommand extends Command {

    /** The name of this command. **/
    public static final String NAME = "experiment";

    /** The default number of repetitions. **/
    public static final int DEFAULT_REPETITION_COUNT = 5;

    /** The prefix of the working directory name for each repetition of an experiment. */
    public static final String REPETITION_WORKING_DIRECTORY_PREFIX = "repetition_";

    /** The short name of the option that specifies the number of times the experiment should be repeated. **/
    public static final String OPTION_REPETITIONS_SHORT = "-r";

    /** The long name of the option that specifies the number of times the experiment should be repeated. **/
    public static final String OPTION_REPETITIONS_LONG = "--repeat";

    /** The number associated to the first repetition. **/
    public static final int FIRST_REPETITION_NUMBER = 1;

    @Parameter(names = {OPTION_REPETITIONS_SHORT, OPTION_REPETITIONS_LONG}, descriptionKey = "command.experiment.repetitions.description", validateValueWith = AtLeastOne.class)
    private int repetitions = DEFAULT_REPETITION_COUNT;

    @Parameter(required = true, names = {OPTION_COMMANDS_SHORT, OPTION_COMMANDS_LONG}, descriptionKey = "launcher.commands.description", converter = PathConverter.class)
    private Path commands;

    /**
     * Instantiates this command for the given launcher and the name by which it is triggered.
     *
     * @param launcher the launcher to which this command belongs.
     */
    public ExperimentCommand(final Launcher launcher) {

        super(launcher, NAME);
    }

    @Override
    public void run() {

        final Path batch_file = getCommandsRelativeToWorkingDirectory();

        final List<Configuration> repetition_configurations = new ArrayList<>();

        for (int repetition = FIRST_REPETITION_NUMBER; repetition <= repetitions; repetition++) {

            final Path repetition_working_directory = getRepetitionWorkingDirectory(repetition);
            final Path batch_file_relative_to_repetition = repetition_working_directory.relativize(batch_file);
            final String[] args = new String[]{Launcher.OPTION_WORKING_DIRECTORY_SHORT, Command.Builder.quote(repetition_working_directory), NAME, OPTION_COMMANDS_SHORT, Command.Builder.quote(batch_file_relative_to_repetition)};

            Launcher.main(args);

            final Configuration repetition_config = Configuration.load(repetition_working_directory);
            repetition_configurations.add(repetition_config);

        }

        logAggregatedConfusionMatrix(repetition_configurations);
        logAggregatedClassificationMetrics(repetition_configurations);
    }

    private Path getCommandsRelativeToWorkingDirectory() {return resolveRelativeToWorkingDirectory(commands);}

    private Path getRepetitionWorkingDirectory(final int repetition) {

        return configuration.getWorkingDirectory().resolve(String.format("%s%d", REPETITION_WORKING_DIRECTORY_PREFIX, repetition));
    }

    private void logAggregatedConfusionMatrix(final List<Configuration> repetition_configurations) {

        final List<ConfusionMatrix> matrices = repetition_configurations.stream().map(Configuration::getConfusionMatrix).filter(Objects::nonNull).collect(Collectors.toList());
        if (!matrices.isEmpty()) {
            EvaluateCommand.logConfusionMatrix(logger, matrices);
        }
    }

    private void logAggregatedClassificationMetrics(final List<Configuration> repetition_configurations) {

        final List<ClassificationMetrics> metrics = repetition_configurations.stream().map(Configuration::getClassificationMetrics).filter(Objects::nonNull).collect(Collectors.toList());
        if (!metrics.isEmpty()) {
            EvaluateCommand.logClassificationMetrics(logger, metrics);
        }
    }

    public static class Builder extends Command.Builder {

        private Integer repetitions;
        private Path commands;

        public void setRepetitions(final int repetitions) {

            this.repetitions = repetitions;
        }

        public void setCommands(final Path commands) {

            this.commands = commands;
        }

        @Override
        protected void populateArguments() {

            Objects.requireNonNull(commands);

            addArgument(NAME);
            addArgument(OPTION_COMMANDS_SHORT);
            addArgument(commands);

            if (repetitions != null) {
                addArgument(OPTION_REPETITIONS_SHORT);
                addArgument(repetitions);
            }
        }
    }
}
