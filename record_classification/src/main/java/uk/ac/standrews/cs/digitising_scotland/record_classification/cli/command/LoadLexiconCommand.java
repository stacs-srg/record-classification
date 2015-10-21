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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cli.command;

import com.beust.jcommander.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.*;

import java.util.logging.*;

/**
 * Command to load dictionary of words or stop words.
 * The loaded lexicon can later be used via the clean command.
 *
 * @author Masih Hajiarab Derkani
 */
@Parameters(commandNames = LoadLexiconCommand.NAME, commandDescription = "Load lexicon")
public class LoadLexiconCommand extends Command {

    /** The name of this command. */
    public static final String NAME = "lexicon";

    /** The short name of the option that specifies the type of lexicon to be loaded. **/
    public static final String OPTION_TYPE_SHORT = "-t";

    /** The long name of the option that specifies the type of lexicon to be loaded. **/
    public static final String OPTION_TYPE_LONG = "--type";

    private static final Logger LOGGER = Logger.getLogger(LoadLexiconCommand.class.getName());

    private final LoadCommand load_command;

    @Parameter(required = true, names = {OPTION_TYPE_SHORT, OPTION_TYPE_LONG}, description = "The type of the lexicon to be loaded.")
    private LexiconType type;

    public LoadLexiconCommand(final LoadCommand load_command) {

        super(load_command.launcher);
        this.load_command = load_command;
    }

    @Override
    public void run() {

        switch (type) {
            case DICTIONARY:
                loadDictionary();
                break;
            case STOP_WORDS:
                loadStopWords();
                break;
            default:
                LOGGER.fine(() -> String.format("unimplemented lexicon tyoe: %s", type));
                throw new UnsupportedOperationException("loading lexicon of type " + type + " is not implemented.");
        }
    }

    private void loadDictionary() {

        LOGGER.finest("loading dictionary...");
        //TODO implement
    }

    private void loadStopWords() {

        LOGGER.finest("loading stop words...");
        //TODO implement
    }

    /** The types of lexicons that can be loaded by this command. **/
    public enum LexiconType {
        /** Dictionary of words to be used by spell checker via the clean command. **/
        DICTIONARY,

        /** Stop words to be used by {@link CleanerSupplier#ENGLISH_STOP_WORDS stop words cleaner} via the clean command. **/
        STOP_WORDS
    }
}
