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

import com.beust.jcommander.Parameters;
import org.apache.commons.csv.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.util.*;

/**
 * Command to load unseen records from a file.
 *
 * @author Masih Hajiarab Derkani
 */
@Parameters(commandNames = LoadUnseenRecordsCommand.NAME, resourceBundle = Configuration.RESOURCE_BUNDLE_NAME, commandDescriptionKey = "command.load.unseen.description")
public class LoadUnseenRecordsCommand extends LoadRecordsCommand {

    /** The name of this command. */
    public static final String NAME = "unseen";

    /**
     * Instantiates this command as a sub command of the given load command.
     *
     * @param load_command the load command to which this command belongs.
     */
    public LoadUnseenRecordsCommand(LoadCommand load_command) { this(load_command, NAME); }

    protected LoadUnseenRecordsCommand(final LoadCommand load_command, final String name) { super(load_command, name); }

    @Override
    protected void process(final List<Record> records) {

        if (load_command.isOverrideExistingEnabled()) {
            configuration.resetUnseenRecords();
        }

        configuration.addUnseenRecords(records);
    }

    @Override
    protected Record toRecord(final CSVRecord record) {

        logger.finest(() -> String.format("Loading record number %d, at character position %d", record.getRecordNumber(), record.getCharacterPosition()));

        final Integer id = getId(record);
        final String label = getLabel(record);

        return new Record(id, label);
    }

    public static class Builder extends LoadRecordsCommand.Builder {

        @Override
        protected String getSubCommandName() {

            return NAME;
        }
    }
}
