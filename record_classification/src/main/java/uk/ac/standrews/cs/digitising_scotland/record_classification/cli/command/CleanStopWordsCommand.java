/*
 * Copyright 2016 Digitising Scotland project:
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
import org.apache.lucene.analysis.util.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import static uk.ac.standrews.cs.digitising_scotland.record_classification.cli.command.LoadCommand.OPTION_CHARSET_SHORT;
import static uk.ac.standrews.cs.digitising_scotland.record_classification.cli.command.LoadCommand.OPTION_SOURCE_SHORT;

/**
 * Cleans a user-specified set of stop words from the loaded gold standard and unseen records.
 *
 * @author Masih Hajiarab Derkani
 */
@Parameters(commandNames = CleanStopWordsCommand.NAME, resourceBundle = Configuration.RESOURCE_BUNDLE_NAME, commandDescriptionKey = "command.clean.stop_words.description")
public class CleanStopWordsCommand extends Command {

    /** The name of this command. */
    public static final String NAME = "stop_words";

    /** The short name of the option that specifies whether the stop words are case sensitive. **/
    public static final String OPTION_CASE_SENSITIVE_SHORT = "-cs";

    /** The long name of the option that specifies whether the stop words are case sensitive. **/
    public static final String OPTION_CASE_SENSITIVE_LONG = "--caseSensitive";

    @Parameter(required = true, names = {OPTION_SOURCE_SHORT, LoadCommand.OPTION_SOURCE_LONG}, descriptionKey = "command.clean.stop_words.source.description", converter = PathConverter.class)
    private Path source;

    @Parameter(names = {LoadCommand.OPTION_CHARSET_SHORT, LoadCommand.OPTION_CHARSET_LONG}, descriptionKey = "command.clean.stop_words.charset.description")
    private CharsetSupplier charset_supplier = configuration.getDefaultCharsetSupplier();

    @Parameter(names = {OPTION_CASE_SENSITIVE_SHORT, OPTION_CASE_SENSITIVE_LONG}, descriptionKey = "command.clean.stop_words.case_sensitive.description")
    private boolean case_sensitive = false;

    public static class Builder extends Command.Builder {

        private Path source;
        private CharsetSupplier charset_supplier;
        private Boolean case_sensitive;

        public void setSource(Path source) {

            this.source = source;
        }

        public void setSourceCharset(CharsetSupplier charset_supplier) {

            this.charset_supplier = charset_supplier;
        }

        public void setCaseSensitive(boolean case_sensitive) {

            this.case_sensitive = case_sensitive;
        }

        @Override
        protected void populateArguments() {

            addArgument(CleanCommand.NAME);
        }

        protected String getSubCommandName() {

            return NAME;
        }

        @Override
        protected void populateSubCommandArguments() {

            Objects.requireNonNull(source);

            addArgument(getSubCommandName());

            addArgument(OPTION_SOURCE_SHORT);
            addArgument(source);

            if (charset_supplier != null) {
                addArgument(OPTION_CHARSET_SHORT);
                addArgument(charset_supplier.name());
            }

            if (case_sensitive != null) {
                addArgument(OPTION_CASE_SENSITIVE_SHORT);
            }
        }
    }

    /**
     * Instantiates this command for the given launcher.
     *
     * @param launcher the launcher to which this command belongs.
     */
    public CleanStopWordsCommand(final Launcher launcher) {

        this(launcher, NAME);
    }

    protected CleanStopWordsCommand(final Launcher launcher, final String name) {

        super(launcher, name);
    }

    @Override
    public void run() {

        final Cleaner cleaner = getCleaner();

        CleanCommand.cleanUnseenRecords(cleaner, configuration, logger);
        CleanCommand.cleanGoldStandardRecords(cleaner, configuration, logger);
    }

    protected Cleaner getCleaner() {

        final List<String> words = readWords();
        final CharArraySet stop_words = new CharArraySet(words, case_sensitive);
        return new EnglishStopWordCleaner(stop_words);
    }

    private List<String> readWords() {

        try {
            return Files.lines(getSourceRelativeToWorkingDirectory(), getCharset()).collect(Collectors.toList());
        }
        catch (IOException e) {
            throw new IOError(e);
        }
    }

    protected Path getSourceRelativeToWorkingDirectory() {return resolveRelativeToWorkingDirectory(source);}

    protected Charset getCharset() {

        return charset_supplier.get();
    }
}
