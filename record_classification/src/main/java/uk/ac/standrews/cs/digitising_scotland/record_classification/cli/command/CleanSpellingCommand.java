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
import org.apache.lucene.analysis.util.*;
import org.apache.lucene.search.spell.*;
import org.apache.lucene.search.spell.Dictionary;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.string_similarity.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * @author Masih Hajiarab Derkani
 */
@Parameters(commandNames = CleanSpellingCommand.NAME, resourceBundle = Configuration.RESOURCE_BUNDLE_NAME, commandDescriptionKey = "command.clean.spelling.description")
public class CleanSpellingCommand extends CleanStopWordsCommand {

    /** The name of this command. */
    public static final String NAME = "spelling";

    /** The short name of the option that specifies the accuracy threshold of the spelling correction. **/
    public static final String OPTION_ACCURACY_THRESHOLD_SHORT = "-a";

    /** The long name of the option that specifies the accuracy threshold of the spelling correction. **/
    public static final String OPTION_ACCURACY_THRESHOLD_LONG = "--accuracyThreshold";

    /** The short name of the option that specifies the accuracy threshold of the spelling correction. **/
    public static final String OPTION_DISTANCE_FUNCTION_SHORT = "-d";

    /** The long name of the option that specifies the accuracy threshold of the spelling correction. **/
    public static final String OPTION_DISTANCE_FUNCTION_LONG = "--distanceFunction";

    /** The default accuracy threshold of the spelling correction. **/
    public static final float DEFAULT_ACCURACY_THRESHOLD = 0.5f;

    @Parameter(names = {OPTION_ACCURACY_THRESHOLD_SHORT, OPTION_ACCURACY_THRESHOLD_LONG}, descriptionKey = "command.clean.spelling.accuracy_threshold.description", validateValueWith = Validators.BetweenZeroToOneInclusive.class)
    private float accuracy_threshold = DEFAULT_ACCURACY_THRESHOLD;

    @Parameter(names = {OPTION_DISTANCE_FUNCTION_SHORT, OPTION_DISTANCE_FUNCTION_LONG}, descriptionKey = "command.clean.spelling.distance.description")
    private StringDistanceSupplier string_distance_supplier = StringDistanceSupplier.JARO_WINKLER;

    /**
     * Instantiates this command for the given launcher.
     *
     * @param launcher the launcher to which this command belongs.
     */
    public CleanSpellingCommand(final Launcher launcher) {

        super(launcher, NAME);
    }

    @Override
    public void run() {

        final Cleaner cleaner = getCleaner();
        final Configuration configuration = launcher.getConfiguration();

        CleanCommand.cleanUnseenRecords(cleaner, configuration, logger);
        CleanCommand.cleanGoldStandardRecords(cleaner, configuration, logger);
    }

    protected Cleaner getCleaner() {

        try {
            final Dictionary dictionary = LoadDictionary();
            return new SuggestiveCleaner(dictionary, string_distance_supplier.get(), accuracy_threshold);
        }
        catch (final IOException cause) {
            throw new IOError(cause);
        }
    }

    private Dictionary LoadDictionary() throws IOException {

        return new PlainTextDictionary(Files.newBufferedReader(getSource(), getCharset()));
    }
}
