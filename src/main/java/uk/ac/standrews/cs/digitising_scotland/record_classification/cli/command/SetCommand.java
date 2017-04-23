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
import com.beust.jcommander.converters.*;
import org.apache.commons.csv.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.util.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.serialization.*;

import java.nio.file.*;
import java.util.function.*;

/**
 * Sets variables in the Command-line Interface {@link Configuration configuration}.
 *
 * @author Masih Hajiarab Derkani
 */
@Parameters(commandNames = SetCommand.NAME, resourceBundle = Configuration.RESOURCE_BUNDLE_NAME, commandDescriptionKey = "command.set.description")
public class SetCommand extends Command {

    /** The name of this command. */
    public static final String NAME = "set";

    /** The short name of the option that specifies the {@link ClassifierSupplier classifier}. */
    public static final String OPTION_CLASSIFIER_SHORT = "-c";

    /** The long name of the option that specifies the {@link ClassifierSupplier classifier}. */
    public static final String OPTION_CLASSIFIER_LONG = "--classifier";

    /** The short name of the option that specifies the seed of configuration's random number generator. **/
    public static final String OPTION_RANDOM_SEED_SHORT = "-r";

    /** The short name of the option that specifies the seed of configuration's random number generator. **/
    public static final String OPTION_RANDOM_SEED_LONG = "--randomSeed";

    /** The short name of the option that specifies the default {@link CharsetSupplier charset} . **/
    public static final String OPTION_CHARSET_SHORT = "-ch";

    /** The Long name of the option that specifies the default {@link CharsetSupplier charset} . **/
    public static final String OPTION_CHARSET_LONG = "--charset";

    /** The short name of the option that specifies the default delimiter in input/output tabular data files. **/
    public static final String OPTION_DELIMITER_SHORT = "-d";

    /** The long name of the option that specifies the default delimiter in input/output tabular data files. **/
    public static final String OPTION_DELIMITER_LONG = "--delimiter";

    /** The short name of the option that specifies the {@link SerializationFormat format} in which to persist the state of this program. **/
    public static final String OPTION_SERIALIZATION_FORMAT_SHORT = "-s";

    /** The long name of the option that specifies the {@link SerializationFormat format} in which to persist the state of this program. **/
    public static final String OPTION_SERIALIZATION_FORMAT_LONG = "--serializationFormat";

    /** The short name of the option that specifies the ratio of the gold standard records to be used for training the classifier. **/
    public static final String OPTION_TRAINING_RATIO_SHORT = "-t";

    /** The short name of the option that specifies the ratio of the gold standard records to be used for training the classifier. **/
    public static final String OPTION_TRAINING_RATIO_LONG = "--trainingRatio";

    /** The short name of the option that specifies the internal training ratio of the classifier. **/
    public static final String OPTION_INTERNAL_TRAINING_RATIO_SHORT = "-it";

    /** The long name of the option that specifies the internal training ratio of the classifier. **/
    public static final String OPTION_INTERNAL_TRAINING_RATIO_LONG = "--internalTrainingRecordRatio";

    /** The short name of the option that specifies the {@link CSVFormat format} of the input/output tabular data files. **/
    public static final String OPTIONS_FORMAT_SHORT = "-f";

    /** The long name of the option that specifies the {@link CSVFormat format} of the input/output tabular data files. **/
    public static final String OPTIONS_FORMAT_LONG = "--format";

    /** The short name of the option that specifies the level of verbosity of the command line interface. **/
    public static final String OPTION_VERBOSITY_SHORT = "-v";

    /** The long name of the option that specifies the level of verbosity of the command line interface. **/
    public static final String OPTION_VERBOSITY_LONG = "--verbosity";

    /** The short name of the option that specifies the level of verbosity of the internal logging mechanism. **/
    public static final String OPTION_INTERNAL_VERBOSITY_SHORT = "-iv";

    /** The long name of the option that specifies the level of verbosity of the internal logging mechanism. **/
    public static final String OPTION_INTERNAL_VERBOSITY_LONG = "--internalVerbosity";

    /** The long name of the option that specifies the path to the working directory. **/
    public static final String OPTION_WORKING_DIRECTORY_SHORT = "-w";

    /** The long name of the option that specifies the path to the working directory. **/
    public static final String OPTION_WORKING_DIRECTORY_LONG = "--workingDirectory";

    @Parameter(names = {OPTION_CLASSIFIER_SHORT, OPTION_CLASSIFIER_LONG}, descriptionKey = "command.set.classifier.description")
    private ClassifierSupplier classifier_supplier;

    @Parameter(names = {OPTION_RANDOM_SEED_SHORT, OPTION_RANDOM_SEED_LONG}, descriptionKey = "command.set.seed.description")
    private Long seed;

    @Parameter(names = {OPTION_CHARSET_SHORT, OPTION_CHARSET_LONG}, descriptionKey = "command.set.default_charset.description")
    private CharsetSupplier charset_supplier;

    @Parameter(names = {OPTION_DELIMITER_SHORT, OPTION_DELIMITER_LONG}, descriptionKey = "command.set.default_delimiter.description", converter = Converters.CharacterConverter.class)
    private Character delimiter;

    @Parameter(names = {OPTION_SERIALIZATION_FORMAT_SHORT, OPTION_SERIALIZATION_FORMAT_LONG}, descriptionKey = "command.set.serialization_format.description")
    private SerializationFormat serialization_format;

    @Parameter(names = {OPTION_TRAINING_RATIO_SHORT, OPTION_TRAINING_RATIO_LONG}, descriptionKey = "command.set.default_training_ratio.description")
    private Double training_ratio;

    @Parameter(names = {OPTION_INTERNAL_TRAINING_RATIO_SHORT, OPTION_INTERNAL_TRAINING_RATIO_LONG}, descriptionKey = "command.set.default_internal_training_ratio.description")
    private Double internal_training_ratio;

    @Parameter(names = {OPTIONS_FORMAT_SHORT, OPTIONS_FORMAT_LONG}, descriptionKey = "command.set.default_csv_format.description")
    private CsvFormatSupplier csv_format;

    @Parameter(names = {OPTION_VERBOSITY_SHORT, OPTION_VERBOSITY_LONG}, descriptionKey = "command.set.verbosity.description")
    private LogLevelSupplier verbosity;

    @Parameter(names = {OPTION_INTERNAL_VERBOSITY_SHORT, OPTION_INTERNAL_VERBOSITY_LONG}, descriptionKey = "command.set.internal_verbosity.description")
    private LogLevelSupplier internal_verbosity;

    @Parameter(names = {OPTION_WORKING_DIRECTORY_SHORT, OPTION_WORKING_DIRECTORY_LONG}, descriptionKey = "command.set.working_directory.description", converter = PathConverter.class)
    private Path working_directory;

    /**
     * Instantiates this command for the given launcher.
     *
     * @param launcher the launcher to which this format belongs.
     */
    public SetCommand(final Launcher launcher) { super(launcher, NAME); }

    @Override
    public void run() {

        boolean set_at_least_once = set("classifier", classifier_supplier, configuration::setClassifierSupplier);
        set_at_least_once |= set("seed", seed, configuration::setSeed);
        set_at_least_once |= set("default charset", charset_supplier, configuration::setDefaultCharsetSupplier);
        set_at_least_once |= set("default delimiter", delimiter, configuration::setDefaultDelimiter);
        set_at_least_once |= set("default training ratio", training_ratio, configuration::setDefaultTrainingRatio);
        set_at_least_once |= set("default internal training ratio", internal_training_ratio, configuration::setDefaultInternalTrainingRatio);
        set_at_least_once |= set("default csv format", csv_format, configuration::setDefaultCsvFormatSupplier);
        set_at_least_once |= set("classifier serialization format", serialization_format, configuration::setClassifierSerializationFormat);
        set_at_least_once |= set("verbosity level", verbosity, configuration::setVerbosity);
        set_at_least_once |= set("internal verbosity level", internal_verbosity, configuration::setInternalVerbosity);
        set_at_least_once |= set("working directory", working_directory, configuration::setWorkingDirectory);

        if (!set_at_least_once) {
            throw new ParameterException("Please specify at lease one variable to be set.");
        }
    }

    private <Value> boolean set(String value_name, Value value, Consumer<Value> setter) {

        final boolean settable = value != null;
        if (settable) {
            setter.accept(value);
            logger.info(() -> String.format("The %s is set to %s", value_name, value));
        }
        return settable;
    }

    public static class Builder extends Command.Builder {

        private ClassifierSupplier classifier_supplier;
        private Long seed;
        private CharsetSupplier charset_supplier;
        private Character delimiter;
        private SerializationFormat serialization_format;
        private Double training_ratio;
        private Double internal_training_ratio;
        private CsvFormatSupplier csv_format;
        private LogLevelSupplier verbosity;
        private LogLevelSupplier internal_verbosity;
        private Path working_directory;

        public void setClassifier(final ClassifierSupplier classifier_supplier) {

            this.classifier_supplier = classifier_supplier;
        }

        public void setSeed(final Long seed) {

            this.seed = seed;
        }

        public void setDefaultCharset(final CharsetSupplier charset_supplier) {

            this.charset_supplier = charset_supplier;
        }

        public void setDefaultDelimiter(final Character delimiter) {

            this.delimiter = delimiter;
        }

        public void setClassifierSerializationFormat(final SerializationFormat serialization_format) {

            this.serialization_format = serialization_format;
        }

        public void setDefaultTrainingRatio(final Double training_ratio) {

            this.training_ratio = training_ratio;
        }

        public void setDefaultInternalTrainingRatio(final Double internal_training_ratio) {

            this.internal_training_ratio = internal_training_ratio;
        }

        public void setDefaultCsvFormat(final CsvFormatSupplier csv_format) {

            this.csv_format = csv_format;
        }

        public void setVerbosity(final LogLevelSupplier verbosity) {

            this.verbosity = verbosity;
        }

        public void setInternalVerbosity(final LogLevelSupplier internal_verbosity) {

            this.internal_verbosity = internal_verbosity;
        }

        public void setWorkingDirectory(final Path working_directory) {

            this.working_directory = working_directory;
        }

        @Override
        protected void populateArguments() {

            if (classifier_supplier != null) {
                addArgument(OPTION_CLASSIFIER_SHORT);
                addArgument(classifier_supplier);
            }
            if (seed != null) {
                addArgument(OPTION_RANDOM_SEED_SHORT);
                addArgument(seed);
            }
            if (charset_supplier != null) {
                addArgument(OPTION_CHARSET_SHORT);
                addArgument(charset_supplier);
            }
            if (delimiter != null) {
                addArgument(OPTION_DELIMITER_SHORT);
                addArgument(delimiter);
            }
            if (serialization_format != null) {
                addArgument(OPTION_SERIALIZATION_FORMAT_SHORT);
                addArgument(serialization_format);
            }
            if (training_ratio != null) {
                addArgument(OPTION_TRAINING_RATIO_SHORT);
                addArgument(training_ratio);
            }
            if (internal_training_ratio != null) {
                addArgument(OPTION_INTERNAL_TRAINING_RATIO_SHORT);
                addArgument(internal_training_ratio);
            }
            if (csv_format != null) {
                addArgument(OPTIONS_FORMAT_SHORT);
                addArgument(csv_format);
            }
            if (verbosity != null) {
                addArgument(OPTION_VERBOSITY_SHORT);
                addArgument(verbosity);
            }
            if (internal_verbosity != null) {
                addArgument(OPTION_INTERNAL_VERBOSITY_SHORT);
                addArgument(internal_verbosity);
            }
            if (working_directory != null) {
                addArgument(OPTION_WORKING_DIRECTORY_SHORT);
                addArgument(working_directory);
            }

            if (isArgumentsEmpty()) {
                throw new ParameterException("at least one parameter must be set");
            }

            addArgument(0, NAME);
        }
    }
}
