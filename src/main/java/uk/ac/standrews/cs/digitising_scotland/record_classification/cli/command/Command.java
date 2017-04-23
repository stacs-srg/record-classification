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
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.util.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;

/**
 * Represents an operation exposed to the user via the {@link Launcher command-line interface}.
 *
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
public abstract class Command implements Runnable {

    protected final Launcher launcher;
    protected final Configuration configuration;
    protected final Logger logger = Logger.getLogger(getClass().getName());

    private final String name;

    /**
     * Instantiates this command for the given launcher and the name by which it is triggered.
     *
     * @param launcher the launcher to which this command belongs.
     * @param name the name by which this command is triggered via the command line interface
     */
    public Command(final Launcher launcher, final String name) {

        this.launcher = launcher;
        this.name = name;
        configuration = launcher.getConfiguration();
    }

    /**
     * Gets the name by which this command is triggered via the command line interface.
     *
     * @return the name by which this command is triggered via the command line interface
     */
    public final String getCommandName() {

        return name;
    }

    /**
     * Adds a given command to this command as its sub-command.
     *
     * @param sub_command the sub command to add to this command
     * @throws IllegalStateException if this command is not added to a launcher
     */
    public void addSubCommand(Command sub_command) {

        final JCommander sub_commander = launcher.getCommander().getCommands().get(name);
        if (sub_commander != null) {
            sub_commander.addCommand(sub_command);
        }
        else {
            throw new IllegalStateException("command must be added to launcher before adding sub commands");
        }
    }

    /**
     * Gets the sub command of this command if it is specified by the user.
     *
     * @return the optional sub command of this command.
     */
    protected Optional<Command> getSubCommand() {

        final Command command;
        final JCommander sub_commander = getSubCommander();
        final String command_name = sub_commander.getParsedCommand();

        if (command_name != null) {
            final JCommander load_command_commander = sub_commander.getCommands().get(command_name);
            command = (Command) load_command_commander.getObjects().get(0);
        }
        else {
            command = null;
        }

        return Optional.ofNullable(command);
    }

    private JCommander getSubCommander() {

        final JCommander core_commander = launcher.getCommander();
        return core_commander.getCommands().get(name);
    }

    protected Path resolveRelativeToWorkingDirectory(Path path) {

        return configuration.getWorkingDirectory().resolve(path);
    }

    /** Builds command line arguments of this command. */
    protected static abstract class Builder {

        private final List<String> arguments;

        protected Builder() {

            arguments = new ArrayList<>();
        }

        protected void addArgument(int index, Object argument) {

            arguments.add(index, String.valueOf(argument));
        }

        protected void addArgument(Object argument) {

            arguments.add(String.valueOf(argument));
        }

        protected boolean isArgumentsEmpty() {

            return arguments.isEmpty();
        }

        /**
         * Runs this command with the built arguments.
         *
         * @return the launcher on which this built command was executed
         * @throws Exception if an error occurs during execution of this command.
         */
        public Launcher run() throws Exception {

            final String[] arguments = build();
            final Launcher launcher = new Launcher();
            launcher.parse(arguments);
            launcher.run();
            return launcher;
        }

        /**
         * Runs this command with the built arguments.
         *
         * @param launcher the launcher on which to execute this command
         * @throws Exception if an error occurs during execution of this command.
         */
        public void run(Launcher launcher) throws Exception {

            final String[] arguments = build();
            launcher.parse(arguments);
            launcher.run();
        }

        /**
         * Builds the command-line arguments for this command with escaped special characters.
         *
         * @return the command-line arguments of this command with escaped special characters.
         */
        public String[] build() {

            populateArguments();
            populateSubCommandArguments();
            final List<String> escaped_arguments = Arguments.escapeSpecialCharacters(arguments);
            return escaped_arguments.toArray(new String[escaped_arguments.size()]);
        }

        protected abstract void populateArguments();

        protected void populateSubCommandArguments() { }
    }

    public static final class BatchBuilder {

        private final List<Builder> command_builders;

        public BatchBuilder() {

            command_builders = new ArrayList<>();
        }

        public boolean add(Builder command_builder) {

            return command_builders.add(command_builder);
        }

        public List<String[]> build() {

            return command_builders.stream().map(Builder::build).collect(Collectors.toList());
        }

        public void build(File destination, Charset charset) throws IOException {

            build(destination.toPath(), charset);
        }

        public void build(Path destination, Charset charset) throws IOException {

            final Iterable<String> command_lines = () -> build().stream().map(Arguments::joinWithSpace).iterator();
            Files.write(destination, command_lines, charset);
        }
    }

    static String formatDuration(final Duration duration) {

        long s = duration.getSeconds();
        return String.format("%d:%02d:%02d", s/3600, (s%3600)/60, (s%60)) + " (hrs/mins/secs)";
    }
}
