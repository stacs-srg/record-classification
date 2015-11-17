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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cli;

import com.beust.jcommander.*;
import com.beust.jcommander.converters.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.command.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

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

    /** The short name of the option that specifies the level of verbosity of the command line interface **/
    public static final String OPTION_VERBOSITY_SHORT = "-v";

    /** The long name of the option that specifies the level of verbosity of the command line interface **/
    public static final String OPTION_VERBOSITY_LONG = "--verbosity";

    /** The long name of the option that specifies the path to the working directory. **/
    public static final String OPTION_WORKING_DIRECTORY_SHORT = "-w";

    /** The long name of the option that specifies the path to the working directory. **/
    public static final String OPTION_WORKING_DIRECTORY_LONG = "--workingDirectory";

    private static final Logger LOGGER = CLILogManager.CLILogger.getLogger(Launcher.class.getName());
    private static final Pattern COMMAND_LINE_ARGUMENT_PATTERN = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("^ *#");

    private JCommander commander;
    private final Configuration configuration;

    @Parameter(names = {OPTION_HELP_SHORT, OPTION_HELP_LONG}, descriptionKey = "launcher.usage.description", help = true)
    private boolean help;

    @Parameter(names = {OPTION_COMMANDS_SHORT, OPTION_COMMANDS_LONG}, descriptionKey = "launcher.commands.description", converter = PathConverter.class)
    private Path commands;

    @Parameter(names = {OPTION_VERBOSITY_SHORT, OPTION_VERBOSITY_LONG}, descriptionKey = "launcher.verbosity.description")
    private LogLevelSupplier log_level_supplier;

    @Parameter(names = {OPTION_WORKING_DIRECTORY_SHORT, OPTION_WORKING_DIRECTORY_LONG}, descriptionKey = "launcher.working_directory.description", converter = PathConverter.class)
    private Path working_directory = Configuration.DEFAULT_WORKING_DIRECTORY;

    //TODO implement interactive mode
    //TODO //@Parameter(names = {"-i", "--interactive"}, description = "Interactive mode; allows multiple command execution.")
    //TODO //private boolean interactive;

    //TODO help command: prints usage, describes individual commands, parameters to the commands, etc.;e.g. help classifier STRING_SIMILARITY
    //TODO status command: prints the current state of the classification process, such as set variables, etc.
    //TODO think whether to have experiment command: does the repetition and joint analysis
    //TODO think whether to have reset command: to be used by experiment command; e.g. reset [classifier, random, gold_standard, unseen, lexicon]
    //TODO think whether to have report command: answer a set of predefined queries about the current state.
    //TODO think whether to have do command: exposes general utilities for one-off execution, clean, unique, split, remove_duplicates, word_frequency_analysis, sort.
    //TODO import/export command: import/export pre-trained classifier.
    //TODO update usage to display description of enums using a custom annotation
    //TODO move exception messages into a resource bundle? this is useful for possible future internationalization of CLI.
    //TODO JScience: floating point accuracy.
    //FIXME Javadoc
    //FIXME website: the what, the why, the how, table of commands and their description.
    //FIXME integration testing
    //TODO javascript command generator?

    static {
        System.setProperty("java.awt.headless", "true");
    }

    public Launcher() throws IOException {

        configuration = loadContext(working_directory);
        log_level_supplier = configuration.getDefaultLogLevelSupplier();

    }

    private Configuration loadContext(final Path working_directory) throws IOException {

        return Configuration.exists(working_directory) ? Configuration.load(working_directory) : new Configuration(working_directory);
    }

    public static void main(String[] args) {

        try {
            final Launcher launcher = new Launcher();
            launcher.parse(args);
            launcher.handle();
        }
        catch (FileAlreadyExistsException error) {
            LOGGER.log(Level.SEVERE, String.format("file '%s' already exists.", error.getFile()), error);
            exitWithError(error);
        }
        catch (NoSuchFileException error) {
            LOGGER.log(Level.SEVERE, String.format("file '%s' not found.", error.getFile()), error);
            exitWithError(error);
        }
        catch (Exception error) {
            LOGGER.log(Level.SEVERE, error.getMessage(), error);
            exitWithError(error);
        }

        //TODO expand user-friendly messages per exceptions
        //TODO think about CLI-specific exceptions.
    }

    private static void exitWithError(final Throwable error) {

        //TODO introduce error coding.
        //TODO set exit value based on error code.
//        throw error;
        System.exit(1);
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

    public void handle() throws Exception {

        configuration.setLogLevel(log_level_supplier.get());

        try {
            if (help) {
                commander.usage();
            }
            else if (isBatchModeEnabled()) {
                handleCommands();
            }
            else {
                handleCommand();
            }
        }
        finally {
            persistContext();
        }
    }

    public boolean isBatchModeEnabled() {return commands != null;}

    private void handleCommands() throws Exception {

        final List<String> command_lines = Files.readAllLines(configuration.getWorkingDirectory().resolve(commands), configuration.getDefaultCharsetSupplier().get());

        for (String command_line : command_lines) {
            final Optional<String[]> arguments = toCommandLineArguments(command_line);
            if (arguments.isPresent()) {
                parse(arguments.get());
                handleCommand();
            }
        }
    }

    private Optional<String[]> toCommandLineArguments(final String command_line) {

        return command_line.trim().isEmpty() || isComment(command_line) ? Optional.empty() : Optional.of(parseCommandLine(command_line));
    }

    private String[] parseCommandLine(final String command_line) {

        final List<String> arguments = new ArrayList<>();
        final Matcher matcher = COMMAND_LINE_ARGUMENT_PATTERN.matcher(command_line);
        while (matcher.find()) {
            if (matcher.group(1) != null) { // Add double-quoted string without the quotes
                arguments.add(matcher.group(1));
            }
            else if (matcher.group(2) != null) { // Add single-quoted string without the quotes
                arguments.add(matcher.group(2));
            }
            else { // Add unquoted word
                arguments.add(matcher.group());
            }
        }
        return arguments.toArray(new String[arguments.size()]);
    }

    private boolean isComment(final String command_line) {

        return COMMENT_PATTERN.matcher(command_line).find();
    }

    private void handleCommand() throws Exception {

        final String command_name = commander.getParsedCommand();

        requireCommand(command_name);

        final JCommander command_commander = commander.getCommands().get(command_name);
        final Command command = (Command) command_commander.getObjects().get(0);

        command.run();
    }

    private void persistContext() throws IOException {

        if (Files.isDirectory(configuration.getHome())) {
            configuration.persist();
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
