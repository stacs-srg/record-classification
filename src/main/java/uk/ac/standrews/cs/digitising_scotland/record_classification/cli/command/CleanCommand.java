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
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.util.*;
import java.util.function.*;
import java.util.logging.*;

import static java.util.logging.Logger.getLogger;

/**
 * Cleans load gold standard and unseen records.
 *
 * @author Masih Hajiarab Derkani
 */
@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
@Parameters(commandNames = CleanCommand.NAME, resourceBundle = Configuration.RESOURCE_BUNDLE_NAME, commandDescriptionKey = "command.clean.description")
public class CleanCommand extends Command {

    /** The name of this command. */
    public static final String NAME = "clean";

    /** The short name of the command that specifies the cleaners by which to clean loaded gold standard and/or unseen records. **/
    public static final String OPTION_CLEANER_SHORT = "-c";

    /** The long name of the command that specifies the cleaners by which to clean loaded gold standard and/or unseen records. **/
    public static final String OPTION_CLEANER_LONG = "--cleaner";

    @Parameter(names = {OPTION_CLEANER_SHORT, OPTION_CLEANER_LONG}, descriptionKey = "command.clean.cleaner.description", variableArity = true)
    private List<CleanerSupplier> cleaner_suppliers;

    public static class Builder extends Command.Builder {

        private List<CleanerSupplier> cleaners = new ArrayList<>();

        void addCleaners(CleanerSupplier... cleaners) {

            Collections.addAll(this.cleaners, cleaners);
        }

        @Override
        protected void populateArguments() {

            requireAtLeastOneCleaner();

            addArgument(NAME);
            addArgument(OPTION_CLEANER_SHORT);
            cleaners.forEach(cleaner -> addArgument(cleaner.name()));
        }

        private void requireAtLeastOneCleaner() {

            if (cleaners.isEmpty()) {
                throw new ParameterException("at least one cleaner must be added");
            }
        }
    }

    /**
     * Instantiates the clean command for the given launcher.
     *
     * @param launcher the launcher to which this command belongs
     */
    public CleanCommand(final Launcher launcher) { super(launcher, NAME); }

    @Override
    public void run() {

        final boolean cleaners_specified = cleaner_suppliers != null;

        if (cleaners_specified) {
            cleanWithPreDefinedCleaner();
        }

        final Optional<Command> sub_command = getSubCommand();

        if (sub_command.isPresent()) {
            logger.fine(() -> "Detected sub command " + sub_command);
            sub_command.get().run();
        }
        else if (!cleaners_specified) {
            logger.severe(() -> "No predefined cleaner or sub command detected to execute.");
            throw new ParameterException("Please specify a predefined cleaner or a sub command.");
        }
    }

    private void cleanWithPreDefinedCleaner() {

        final Cleaner cleaner = getCombinedCleaner();

        cleanGoldStandardRecords(cleaner, configuration, logger);
        cleanUnseenRecords(cleaner, configuration, logger);
    }

    static void cleanUnseenRecords(final Cleaner cleaner, final Configuration configuration, Logger logger) {

        clean(cleaner, "unseen", configuration::getUnseenRecordsOptional, configuration::setUnseenRecords, logger);
    }

    static void cleanGoldStandardRecords(final Cleaner cleaner, final Configuration configuration, Logger logger) {

        final Optional<Bucket> eval_records_optional = configuration.getEvaluationRecordsOptional();
        final Optional<Bucket> train_records_optional = configuration.getTrainingRecordsOptional();

        if (eval_records_optional.isPresent() && train_records_optional.isPresent()) {
            logger.info(() -> "cleaning training and evaluation records...");
            final List<Bucket> cleaned = cleaner.apply(Arrays.asList(eval_records_optional.get(), train_records_optional.get()));
            configuration.setEvaluationRecords(cleaned.get(0));
            configuration.setTrainingRecords(cleaned.get(1));
        }
        else {

            cleanEvaluationRecords(cleaner, configuration, logger);
            cleanTrainingRecords(cleaner, configuration, logger);
        }
    }

    private static void cleanEvaluationRecords(final Cleaner cleaner, final Configuration configuration, Logger logger) {

        clean(cleaner, "evaluation", configuration::getEvaluationRecordsOptional, configuration::setEvaluationRecords, logger);
    }

    private static void cleanTrainingRecords(final Cleaner cleaner, final Configuration configuration, Logger logger) {

        clean(cleaner, "training", configuration::getTrainingRecordsOptional, configuration::setTrainingRecords, logger);
    }

    private static void clean(final Cleaner cleaner, String name, Supplier<Optional<Bucket>> unclean_getter, Consumer<Bucket> cleaned_setter, Logger logger) {

        final Optional<Bucket> unclean = unclean_getter.get();
        if (unclean.isPresent()) {
            logger.info(() -> "cleaning " + name + " records...");
            final Bucket cleaned = cleaner.apply(unclean.get());
            cleaned_setter.accept(cleaned);
        }
        else {
            logger.info(() -> "skipped cleaning of " + name + "; no records are loaded to clean.");
        }
    }

    private Cleaner getCombinedCleaner() {

        Cleaner combined = null;

        for (CleanerSupplier supplier : cleaner_suppliers) {

            final Cleaner cleaner = supplier.get();

            if (combined == null) {
                combined = cleaner;
            }
            combined = combined.andThen(cleaner);
        }

        if (combined == null) throw new ParameterException("no cleaner specified");

        return combined;

        //return this.cleaner_suppliers.stream().map(Supplier::get).reduce(Cleaner::andThen).orElseThrow(() -> new ParameterException("no cleaner is specified"));
    }
}
