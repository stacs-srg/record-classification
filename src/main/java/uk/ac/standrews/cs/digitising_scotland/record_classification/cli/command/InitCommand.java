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
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Configuration;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Launcher;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.ConfigurationDirectoryAlreadyExistsException;
import uk.ac.standrews.cs.utilities.FileManipulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Initialisation command of the classification process.
 *
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
@Parameters(commandNames = InitCommand.NAME, resourceBundle = Configuration.RESOURCE_BUNDLE_NAME, commandDescriptionKey = "command.init.description")
public class InitCommand extends Command {

    /** The name of this command. */
    public static final String NAME = "init";

    /** The short option name which forces any existing configuration to be replaced upon initialisation. **/
    public static final String OPTION_FORCE_SHORT = "-f";

    /** The long option name which forces any existing configuration to be replaced upon initialisation. **/
    public static final String OPTION_FORCE_LONG = "--force";

    @Parameter(names = {OPTION_FORCE_SHORT, OPTION_FORCE_LONG}, descriptionKey = "command.init.force.description")
    private boolean replace_existing;

    public InitCommand(final Launcher launcher) { super(launcher, NAME); }

    @Override
    public void run() {

        final Path home = configuration.getHome();
        try {

            checkDirectoryExistence(home, replace_existing);
            configuration.init();
        }
        catch (IOException e) {
            throw new RuntimeException("failed to create configuration folder", e);
        }
    }

    private static void checkDirectoryExistence(Path directory, boolean delete_if_exists) throws IOException {

        if (Files.isDirectory(directory)) {

            if (delete_if_exists) {
                FileManipulation.deleteDirectory(directory);
            }
            else {
                throw new ConfigurationDirectoryAlreadyExistsException(directory);
            }
        }
    }

    public static void assureDirectoryExists(final Path directory) throws IOException {

        if (!Files.isDirectory(directory)) {

            final Path directories = Files.createDirectories(directory);
            if (!Files.isDirectory(directories)) {
                throw new IOException(directory.toString());
            }
        }
    }

    public static class Builder extends Command.Builder {

        private boolean force;

        void setForce(boolean force) {

            this.force = force;
        }

        @Override
        protected void populateArguments() {

            addArgument(NAME);
            if (force) {
                addArgument(OPTION_FORCE_SHORT);
            }
        }
    }
}
