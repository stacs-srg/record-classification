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
import org.apache.commons.csv.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.util.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.util.*;

import static uk.ac.standrews.cs.digitising_scotland.record_classification.cli.command.SetCommand.OPTION_TRAINING_RATIO_LONG;
import static uk.ac.standrews.cs.digitising_scotland.record_classification.cli.command.SetCommand.OPTION_TRAINING_RATIO_SHORT;

/**
 * Command to load gold standard data from a file.
 *
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
@Parameters(commandNames = LoadGoldStandardRecordsCommand.NAME, resourceBundle = Configuration.RESOURCE_BUNDLE_NAME, commandDescriptionKey = "command.load.gold_standard.description")
public class LoadGoldStandardRecordsCommand extends LoadUnseenRecordsCommand {

    /** The name of this command. */
    public static final String NAME = "gold_standard";

    /** The short name of the option that specifies the index of the column that contains the classes associated to each row, starting from {@code 0}. **/
    public static final String OPTION_CLASS_COLUMN_INDEX_SHORT = "-ci";

    /** The long name of the option that specifies the index of the column that contains the classes associated to each row, starting from {@code 0}. **/
    public static final String OPTION_CLASS_COLUMN_INDEX_LONG = "--class_column_index";

    /** The default index of the column that contains the classes associated to each row. **/
    public static final int DEFAULT_CLASS_COLUMN_INDEX = 2;

    @Parameter(names = {OPTION_TRAINING_RATIO_SHORT, OPTION_TRAINING_RATIO_LONG},
               descriptionKey = "command.load.gold_standard.training_ratio.description",
               validateValueWith = Validators.BetweenZeroToOneInclusive.class)
    private Double training_ratio = configuration.getDefaultTrainingRatio();

    @Parameter(names = {OPTION_CLASS_COLUMN_INDEX_SHORT, OPTION_CLASS_COLUMN_INDEX_LONG}, descriptionKey = "command.load.gold_standard.class_column_index.description", validateValueWith = Validators.AtLeastZero.class)
    private Integer class_column_index = DEFAULT_CLASS_COLUMN_INDEX;

    /**
     * Instantiates this command as a sub command of the given load command.
     *
     * @param load_command the load command to which this command belongs.
     */
    public LoadGoldStandardRecordsCommand(final LoadCommand load_command) { super(load_command, NAME); }

    @Override
    protected void process(final List<Record> records) {

        final Bucket gold_standard_records = new Bucket(records);

        if (load_command.isOverrideExistingEnabled()) {
            configuration.resetTrainingRecords();
            configuration.resetEvaluationRecords();
        }

        configuration.addGoldStandardRecords(gold_standard_records, getTrainingRatio());
    }

    @Override
    protected Record toRecord(final CSVRecord record) {

        logger.finest(() -> String.format("loading record number %d, at character position %d", record.getRecordNumber(), record.getCharacterPosition()));

        final Integer id = getId(record);
        final String label = getLabel(record);
        final String code = getClass(record);

        return new Record(id, label, new Classification(code, new TokenList(label), 0.0, null));
    }

    private String getClass(final CSVRecord record) {

        return record.get(class_column_index);
    }

    /**
     * Gets the ratio of the gold standard records to be used for training.
     * The ratio must be specified via {@value SetCommand#OPTION_TRAINING_RATIO_SHORT} or {@value SetCommand#OPTION_TRAINING_RATIO_LONG} options
     * as a numerical value within inclusive range of {@code 0.0} to {@code 1.0}.
     *
     * @return the ratio of the gold standard records to be used for training
     */
    public Double getTrainingRatio() {

        return training_ratio;
    }

    public static class Builder extends LoadRecordsCommand.Builder {

        private Double training_ratio;
        private Integer class_column_index;

        public void setTrainingRatio(Double training_ratio) {

            this.training_ratio = training_ratio;
        }

        public void setClassColumnIndex(Integer class_column_index) {

            this.class_column_index = class_column_index;
        }

        @Override
        protected void populateSubCommandArguments() {

            super.populateSubCommandArguments();

            if (training_ratio != null) {
                addArgument(OPTION_TRAINING_RATIO_SHORT);
                addArgument(String.valueOf(training_ratio));
            }

            if (class_column_index != null) {
                addArgument(OPTION_CLASS_COLUMN_INDEX_SHORT);
                addArgument(String.valueOf(class_column_index));
            }
        }

        @Override
        protected String getSubCommandName() {

            return NAME;
        }
    }
}
