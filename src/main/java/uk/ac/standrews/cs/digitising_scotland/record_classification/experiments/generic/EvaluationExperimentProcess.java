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

import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.Cleaner;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.ClassificationProcess;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.steps.CleanGoldStandardStep;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.steps.EvaluateClassifierStep;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.steps.LoadGoldStandardRecordsByRatioStep;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.steps.TrainClassifierStep;

import java.nio.file.Path;
import java.util.List;

public class EvaluationExperimentProcess extends ClassificationProcess {

    private List<Path> gold_standard_files;
    private List<Double> training_ratios;
    private List<Cleaner> cleaners;
    private double internal_training_ratio = Experiment.DEFAULT_TRAINING_RATIO;

    public EvaluationExperimentProcess setGoldStandardFiles(List<Path> gold_standard_files) {

        this.gold_standard_files = gold_standard_files;
        return this;
    }

    public EvaluationExperimentProcess setTrainingRatios(List<Double> training_ratios) {

        this.training_ratios = training_ratios;
        return this;
    }

    public EvaluationExperimentProcess setCleaners(List<Cleaner> cleaners) {

        this.cleaners = cleaners;
        return this;
    }

    public EvaluationExperimentProcess setInternalTrainingRatio(double internal_training_ratio) {

        this.internal_training_ratio = internal_training_ratio;
        return this;
    }

    public void configureSteps() {

        for (int gold_standard_file_number = 0; gold_standard_file_number < gold_standard_files.size(); gold_standard_file_number++) {

            Path gold_standard_file = gold_standard_files.get(gold_standard_file_number);

            double training_ratio = training_ratios.get(gold_standard_file_number);
            addStep(new LoadGoldStandardRecordsByRatioStep(gold_standard_file, training_ratio));
        }

        if (cleaners != null) {
            for (Cleaner cleaner : cleaners) {
                addStep(new CleanGoldStandardStep(cleaner));
            }
        }

        addStep(new TrainClassifierStep(internal_training_ratio));
        addStep(new EvaluateClassifierStep());
    }
}
