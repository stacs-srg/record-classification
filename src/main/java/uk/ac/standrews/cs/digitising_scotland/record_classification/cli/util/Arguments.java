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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cli.util;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

/**
 * @author Masih Hajiarab Derkani
 */
public final class Arguments {

    private static final Pattern SPECIAL_CHARACTER = Pattern.compile("[^-_A-Za-z0-9]");
    private static final Pattern COMMAND_LINE_ARGUMENT_PATTERN = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("^\\s*#");
    private static final CharSequence SPACE = " ";

    private Arguments() {

        throw new UnsupportedOperationException();
    }

    public static String joinWithSpace(CharSequence... arguments) {

        return String.join(SPACE, arguments);
    }

    public static String joinWithSpace(Iterable<? extends CharSequence> arguments) {

        return String.join(SPACE, arguments);
    }

    public static String quote(Object value) {

        return quote(String.valueOf(value));
    }

    public static String quote(String value) {

        return hasSpecialCharacter(value) ? String.format("\"%s\"", String.valueOf(value)) : value;
    }

    public static boolean hasSpecialCharacter(final String argument) {

        return SPECIAL_CHARACTER.matcher(argument).find();
    }

    public static List<String> escapeSpecialCharacters(final List<String> arguments) {

        return arguments.stream().map(Arguments::quote).collect(Collectors.toList());
    }

    public static Stream<String[]> parseBatchCommandFile(Path commands, Charset charset) throws IOException {

        return Files.lines(commands, charset).map(Arguments::toCommandLineArguments).filter(Objects::nonNull);
    }

    private static String[] toCommandLineArguments(final String command_line) {

        return isCommandLine(command_line) ? null : parseCommandLine(command_line);
    }

    private static boolean isCommandLine(final String command_line) {

        return command_line.trim().isEmpty() || isComment(command_line);
    }

    private static String[] parseCommandLine(final String command_line) {

        final List<String> arguments = new ArrayList<>();
        final Matcher matcher = COMMAND_LINE_ARGUMENT_PATTERN.matcher(command_line);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // Add double-quoted string without the quotes
                arguments.add(matcher.group(1));
            }
            else if (matcher.group(2) != null) {
                // Add single-quoted string without the quotes
                arguments.add(matcher.group(2));
            }
            else {
                // Add unquoted word
                arguments.add(matcher.group());
            }
        }
        return arguments.toArray(new String[arguments.size()]);
    }

    protected static boolean isComment(final String command_line) {

        return COMMENT_PATTERN.matcher(command_line).find();
    }
}
