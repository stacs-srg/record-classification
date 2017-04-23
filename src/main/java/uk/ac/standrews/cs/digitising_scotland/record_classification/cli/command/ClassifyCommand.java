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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.PathConverter;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.Classifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Configuration;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Launcher;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.logging.Level;

import static uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Configuration.persistBucketAsCSV;

/**
 * Classifies unseen records.
 *
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
@Parameters(commandNames = ClassifyCommand.NAME, resourceBundle = Configuration.RESOURCE_BUNDLE_NAME, commandDescriptionKey = "command.classify.description")
public class ClassifyCommand extends Command {

    /** The name of this command. */
    public static final String NAME = "classify";

    /** The short name of the option that specifies the path in which to store the classified records. **/
    public static final String OPTION_OUTPUT_RECORDS_PATH_SHORT = "-o";

    /** The long name of the option that specifies the path in which to store the classified records. **/
    public static final String OPTION_OUTPUT_RECORDS_PATH_LONG = "--output";

    @Parameter(required = true, names = {OPTION_OUTPUT_RECORDS_PATH_SHORT, OPTION_OUTPUT_RECORDS_PATH_LONG}, descriptionKey = "command.classify.output.description", converter = PathConverter.class)
    private Path output_path;

    /**
     * Instantiates this command for the given launcher.
     *
     * @param launcher the launcher to which this format belongs.
     */
    public ClassifyCommand(final Launcher launcher) { super(launcher, NAME); }

    @Override
    public void run() {

        final Classifier classifier = configuration.requireClassifier();

        final Bucket unseen_records = configuration.requireUnseenRecords();
        final Instant start = Instant.now();
        final Bucket classified_unseen_records = classifier.classify(unseen_records);
        final Duration classification_time = Duration.between(start, Instant.now());

        configuration.setClassifiedUnseenRecords(classified_unseen_records);
        logger.info(() -> String.format("classified %d records in %s", classified_unseen_records.size(), formatDuration(classification_time)));

        persistClassifiedUnseenRecords(classified_unseen_records);
    }

    private void persistClassifiedUnseenRecords(final Bucket classified_unseen_records) {

        final Path destination = resolveRelativeToWorkingDirectory(output_path);

        logger.info(() -> String.format("Persisting total of %d classified unseen records into path: %s", classified_unseen_records.size(), destination));
        try {
            persistBucketAsCSV(classified_unseen_records, destination, Configuration.RECORD_CSV_FORMAT, Configuration.RESOURCE_CHARSET);
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "failed to persist classified unseen records: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /** Builds command line arguments of this command. */
    public static class Builder extends Command.Builder {

        private Path output_path;

        /**
         * Sets the path at which to store the classified unseen records.
         *
         * @param output_path the path at which to store the classified unseen records
         */
        public void setOutputPath(Path output_path) {

            Objects.requireNonNull(output_path);
            this.output_path = output_path;
        }

        @Override
        protected void populateArguments() {

            addArgument(NAME);
            addArgument(OPTION_OUTPUT_RECORDS_PATH_SHORT);
            addArgument(output_path);
        }
    }
}
