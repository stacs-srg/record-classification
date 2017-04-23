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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cli;

import com.beust.jcommander.*;
import com.beust.jcommander.converters.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.command.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.util.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.logging.*;

/**
 * Launches the command line interface for a classification process.
 *
 * @author Masih Hajiarab Derkani
 */
@Parameters(resourceBundle = Configuration.RESOURCE_BUNDLE_NAME)
public class Launcher {

    /** The short name of the option to display usage. **/
    public static final String OPTION_HELP_SHORT = "-h";

    /** The long name of the option to display usage. **/
    public static final String OPTION_HELP_LONG = "--help";

    /** The short name of the option that specifies the path to a file containing the batch commands to be executed. **/
    public static final String OPTION_COMMANDS_SHORT = "-c";

    /** The long name of the option that specifies the path to a file containing the batch commands to be executed. **/
    public static final String OPTION_COMMANDS_LONG = "--commands";

    protected static final String JVM_LOGGING_CONFIG_PROPERTY = "java.util.logging.config.file";
    protected static final String JVM_HEADLESS_PROPERTY = "java.awt.headless";

    private static final Logger LOGGER = Logger.getLogger(Launcher.class.getName());

    static {
        setCLISystemProperties();
    }

    private JCommander commander;
    private Configuration configuration;

    @Parameter(names = {OPTION_HELP_SHORT, OPTION_HELP_LONG}, descriptionKey = "launcher.usage.description", help = true)
    private boolean help;
    @Parameter(names = {OPTION_COMMANDS_SHORT, OPTION_COMMANDS_LONG}, descriptionKey = "launcher.commands.description", converter = PathConverter.class)
    private Path commands;

    public Launcher() {

        this(Configuration.DEFAULT_WORKING_DIRECTORY);
    }

    public Launcher(Path working_directory) {

        loadConfiguration(working_directory);
    }

    private void loadConfiguration(final Path working_directory) {

        if (Configuration.exists(working_directory)) {
            try {
                configuration = Configuration.load(working_directory);
            }
            catch (RuntimeException e) {
                LOGGER.severe(e.getMessage());
                LOGGER.warning("ignored existing configuration due to load error.");
            }
        }

        if (configuration == null) {
            configuration = new Configuration(working_directory);
        }
        else {
            configuration.setWorkingDirectory(working_directory);
        }
    }

    public static void setCLISystemProperties() {

        if (System.getProperty(JVM_HEADLESS_PROPERTY) == null) {
            System.setProperty(JVM_HEADLESS_PROPERTY, "true");
        }
        if (System.getProperty(JVM_LOGGING_CONFIG_PROPERTY) == null) {
            try {

                LogManager.getLogManager().readConfiguration(Configuration.class.getResourceAsStream("logging.properties"));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String... args) {

        try {
            final Launcher launcher = new Launcher();

            launcher.parse(args);
            launcher.run();
        }
        catch (RuntimeException error) {

            final Throwable cause = error.getCause();

            if (cause instanceof ConfigurationDirectoryAlreadyExistsException) {
                ConfigurationDirectoryAlreadyExistsException exception = (ConfigurationDirectoryAlreadyExistsException) cause;
                LOGGER.log(Level.SEVERE, String.format("Configuration directory '%s' already exists. Use %s flag to %s command to force deletion of existing directory.", exception.getFile(), InitCommand.OPTION_FORCE_SHORT, InitCommand.NAME), error);
            }

            else if (cause instanceof NoSuchFileException) {
                NoSuchFileException exception = (NoSuchFileException) cause;
                LOGGER.log(Level.SEVERE, String.format("File '%s' not found.", exception.getFile()), error);
            }

            else if (cause instanceof IOException) {
                LOGGER.log(Level.SEVERE, String.format("File format error: %s (see http://digitisingscotland.cs.st-andrews.ac.uk/record_classification/cli/usage/).", cause), error);
            }

            else {
                LOGGER.log(Level.SEVERE, "critical error: " + error.getMessage(), error);
            }

            System.exit(1);
        }
        catch (Exception error) {

            final String message = error.getMessage();

            if (message != null) {
                LOGGER.log(Level.SEVERE, message, error);
            }
            else {
                LOGGER.log(Level.SEVERE, error.getClass().getName(), error);
            }

            System.exit(1);
        }

        //TODO expand user-friendly messages per exceptions
        //TODO think about CLI-specific exceptions.
        //TODO introduce error coding.
        //TODO set exit value based on error code.
    }

    void addCommand(Command command) {

        commander.addCommand(command);
    }

    public void parse(final String... args) throws ParameterException {

        initCommander();
        commander.parse(args);
    }

    private void initCommander() {

        commander = new JCommander(this);
        commander.setProgramName(Configuration.PROGRAM_NAME);

        addCommand(new InitCommand(this));
        addCommand(new SetCommand(this));
        addCommand(new ClassifyCommand(this));
        addCommand(new EvaluateCommand(this));
        addCommand(new TrainCommand(this));
        addCommand(new ExperimentCommand(this));

        final CleanCommand clean_command = new CleanCommand(this);
        addCommand(clean_command);
        clean_command.addSubCommand(new CleanStopWordsCommand(this));
        clean_command.addSubCommand(new CleanSpellingCommand(this));

        final LoadCommand load_command = new LoadCommand(this);
        addCommand(load_command);
        load_command.addSubCommand(new LoadUnseenRecordsCommand(load_command));
        load_command.addSubCommand(new LoadGoldStandardRecordsCommand(load_command));
    }

    public void run() {

        if (help) {
            commander.usage();
        }
        else if (isBatchModeEnabled()) {
            runCommandBatch();
        }
        else {
            runCommand();
        }

        persistConfiguration();
    }

    private boolean isBatchModeEnabled() {

        return commands != null;
    }

    private void runCommandBatch() {

        final Path commands_file = configuration.getWorkingDirectory().resolve(commands);
        final Charset charset = configuration.getDefaultCharsetSupplier().get();
        try {
            Arguments.parseBatchCommandFile(commands_file, charset).forEachOrdered(arguments -> {
                parse(arguments);
                runCommand();
            });
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runCommand() {

        final String command_name = commander.getParsedCommand();

        requireCommand(command_name);

        final JCommander command_commander = commander.getCommands().get(command_name);
        final Command command = (Command) command_commander.getObjects().get(0);

        command.run();
    }

    private void persistConfiguration() {

        if (Files.isDirectory(configuration.getHome())) {
            try {
                configuration.persist();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void requireCommand(final String command) {

        if (command == null) {
            throw new ParameterException("Please specify a command");
        }
    }

    public Configuration getConfiguration() {

        return configuration;
    }

    public JCommander getCommander() {

        return commander;
    }
}
