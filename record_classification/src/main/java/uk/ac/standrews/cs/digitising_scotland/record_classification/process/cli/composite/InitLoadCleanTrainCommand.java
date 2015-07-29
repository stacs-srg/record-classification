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
package uk.ac.standrews.cs.digitising_scotland.record_classification.process.cli.composite;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.PathConverter;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.ClassifierSupplier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.CleanerSupplier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.cli.commands.CleanGoldStandardCommand;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.cli.commands.InitCommand;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.cli.commands.LoadGoldStandardCommand;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.cli.commands.TrainCommand;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.processes.generic.ClassificationContext;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.serialization.SerializationFormat;

import java.nio.file.Path;
import java.util.List;

/**
 * Composite command that initialises, loads gold standard, cleans and trains.
 *
 * Example command line invocation:
 *
 * <code>
 *   java uk.ac.standrews.cs.digitising_scotland.record_classification.process.cli.Launcher
 *   init_load_clean_train
 *   -g cambridge.csv
 *   -g hisco.csv
 *   -p trained_hisco_classifier
 *   -c EXACT_MATCH_PLUS_VOTING_ENSEMBLE
 *   -r 1.0
 *   -f JSON_COMPRESSED
 *   -cl COMBINED
 * </code>
 *
 * Or via Maven:
 *
 *   mvn exec:java -q -Dexec.cleanupDaemonThreads=false
 *   -Dexec.mainClass="uk.ac.standrews.cs.digitising_scotland.record_classification.process.cli.Launcher" -e
 *   -Dexec.args="init_load_clean_train -g cambridge.csv -g hisco.csv -p trained_hisco_classifier
 *   -c EXACT_MATCH_PLUS_VOTING_ENSEMBLE -r 1.0 -f JSON_COMPRESSED -cl COMBINED"
 *
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
@Parameters(commandNames = InitLoadCleanTrainCommand.NAME, commandDescription = "Initialise process, load and clean training data, train classifier")
public class InitLoadCleanTrainCommand extends Command {

    /**
     * The name of this command
     */
    public static final String NAME = "init_load_clean_train";
    private static final long serialVersionUID = 8026292848547343006L;

    @Parameter(required = true, names = {InitCommand.CLASSIFIER_FLAG_SHORT, InitCommand.CLASSIFIER_FLAG_LONG}, description = InitCommand.CLASSIFIER_DESCRIPTION)
    private ClassifierSupplier classifier_supplier;

    @Parameter(required = true, names = {LoadGoldStandardCommand.GOLD_STANDARD_FLAG_SHORT, LoadGoldStandardCommand.GOLD_STANDARD_FLAG_LONG}, description = LoadGoldStandardCommand.GOLD_STANDARD_DESCRIPTION, converter = PathConverter.class)
    private List<Path> gold_standards;

    @Parameter(required = true, names = {TrainCommand.TRAINING_RATIO_FLAG_SHORT, TrainCommand.TRAINING_RATIO_FLAG_LONG}, description = TrainCommand.TRAINING_RATIO_DESCRIPTION)
    private Double training_ratio;

    @Parameter(required = true, names = {CleanGoldStandardCommand.CLEAN_FLAG_SHORT, CleanGoldStandardCommand.CLEAN_FLAG_LONG}, description = CleanGoldStandardCommand.CLEAN_DESCRIPTION)
    private List<CleanerSupplier> cleaners;

    @Override
    public Void call() throws Exception {

        initLoadCleanTrain(classifier_supplier, gold_standards, charsets, delimiters, training_ratio, serialization_format, name, process_directory, cleaners);

        return null;
    }

    public static void initLoadCleanTrain(ClassifierSupplier classifier_supplier, List<Path> gold_standard, List<CharsetSupplier> charsets, List<String> delimiters, Double training_ratio, SerializationFormat serialization_format, String process_name, Path process_directory, List<CleanerSupplier> cleaners) throws Exception {

        InitCommand.init(classifier_supplier, serialization_format, process_name, process_directory);

        LoadGoldStandardCommand.loadGoldStandard(gold_standard, charsets, delimiters, serialization_format, process_name, process_directory);

        CleanGoldStandardCommand.cleanGoldStandard(serialization_format, process_name, process_directory, cleaners);

        TrainCommand.train(training_ratio, serialization_format, process_name, process_directory);
    }

    @Override
    public void perform(ClassificationContext context) {
    }

 }
