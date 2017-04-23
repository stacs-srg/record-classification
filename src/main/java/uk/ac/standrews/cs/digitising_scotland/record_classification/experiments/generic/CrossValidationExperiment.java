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
package uk.ac.standrews.cs.digitising_scotland.record_classification.experiments.generic;

import com.beust.jcommander.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.steps.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.stream.*;

/**
 * Evaluates a classifier using cross validation, where the number of repetitions represents the number of cross validations.
 *
 * The gold standard is split into {@code n}  chunks where {@code n}  is equal to the number of repetitions.
 * The classifier is trained on all but one of the chunks, and evaluated on the remaining chunk;
 * this process is repeated for each chunk.
 *
 * @author Masih Hajiarab Derkani
 */
public class CrossValidationExperiment extends Experiment {

    private static final Logger LOGGER = Logger.getLogger(CrossValidationExperiment.class.getName());
    private final Random experiment_random = new Random(6284684);
    private Bucket gold_standard;
    private List<Bucket> gold_standard_splits;

    @Parameter( names = {"-c", "--classifier"}, description = "the classifier")
    protected List<Supplier<Classifier>> classifier_suppliers = Collections.singletonList(ClassifierSupplier.VOTING_ENSEMBLE_EXACT_ML_SIMILARITY);

    protected CrossValidationExperiment(final String[] args) throws IOException, InputFileFormatException {

        super(args);
    }

    public static void main(final String[] args) throws Exception {

        LOGGER.warning("This experiment ignores training ratios since the ratio is dependent on the number of cross validations, i.e. the number of repetitions.");

        final CrossValidationExperiment experiment = new CrossValidationExperiment(args);
        experiment.call();
    }

    @Override
    public List<ClassifierResults> runExperiment() throws Exception {

        loadGoldStandard();
        splitGoldStandard();
        return super.runExperiment();
    }

    private void splitGoldStandard() {

        LOGGER.info(String.format("splitting %d gold standard records in %d ways...", gold_standard.size(), repetitions));

        gold_standard_splits = gold_standard.split(repetitions, experiment_random);

        LOGGER.info("gold standard split sizes are: " + gold_standard_splits.stream().map(Bucket::size).collect(Collectors.toList()));
    }

    private void loadGoldStandard() throws IOException {

        gold_standard = new Bucket();

        for (Path gold_standard_file : gold_standard_files) {

            try (final BufferedReader reader = Files.newBufferedReader(gold_standard_file)) {
                final Bucket records = new Bucket(reader, LoadStep.DEFAULT_DELIMITER.charAt(0));
                gold_standard.add(records);
            }
        }
    }

    @Override
    protected void runRepetition(final ClassificationProcess process, final ClassificationContext context, final int repetition_index) throws Exception {

        addTrainingRecordsByRepetitionIndex(context, repetition_index);
        addEvaluationRecordsByRepetitionIndex(context, repetition_index);

        super.runRepetition(process, context, repetition_index);
    }

    @Override
    protected List<Supplier<Classifier>> getClassifierFactories() throws IOException, InputFileFormatException {

        return classifier_suppliers;
    }

    @Override
    protected ClassificationProcess makeClassificationProcess(final Supplier<Classifier> factory) {

        final ClassificationProcess process = new ClassificationProcess();

        //No loading of gold standard; it is loaded in #runRepetition, 
        // since the selection of training/evaluation records depend on repetition number. 

        for (Cleaner cleaner : CLEANERS) {
            process.addStep(new CleanGoldStandardStep(cleaner));
        }

        process.addStep(new TrainClassifierStep(internal_training_ratio));
        process.addStep(new EvaluateClassifierStep());

        return process;
    }

    private void addEvaluationRecordsByRepetitionIndex(final ClassificationContext context, final int repetition_index) {

        context.addEvaluationRecords(gold_standard_splits.get(repetition_index));
    }

    private void addTrainingRecordsByRepetitionIndex(final ClassificationContext context, final int repetition_index) {

        for (int split_index = 0; split_index < repetitions; split_index++) {
            if (split_index != repetition_index) {
                context.addTrainingRecords(gold_standard_splits.get(split_index));
            }
        }
    }
}
