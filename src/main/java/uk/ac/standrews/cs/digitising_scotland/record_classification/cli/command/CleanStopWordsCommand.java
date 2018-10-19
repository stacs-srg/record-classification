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
import org.apache.lucene.analysis.CharArraySet;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.Cleaner;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.EnglishStopWordCleaner;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Configuration;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Launcher;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.CharsetSupplier;

import java.io.IOError;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
