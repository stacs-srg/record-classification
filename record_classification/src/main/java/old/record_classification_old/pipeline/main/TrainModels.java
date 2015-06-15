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
package old.record_classification_old.pipeline.main;

import java.io.File;

import old.record_classification_old.classifiers.lookup.ExactMatchClassifier;
import old.record_classification_old.classifiers.olr.OLRClassifier;
import old.record_classification_old.datastructures.bucket.Bucket;
import old.record_classification_old.datastructures.code.CodeDictionary;
import old.record_classification_old.datastructures.code.CodeNotValidException;
import old.record_classification_old.pipeline.BucketGenerator;
import old.record_classification_old.pipeline.PipelineUtils;
import old.record_classification_old.tools.Timer;
import old.record_classification_old.tools.configuration.MachineLearningConfiguration;

/**
 * Trains a model and a lookup table from the supplied gold standard data file.
 * The models are then written to disk.
 * @author jkc25
 *
 */
public class TrainModels {

    /**
     * Entry method for training a model on a batch of records.
     * 
     * @param args
     *            <file1> training file
     * @throws Exception
     *             If exception occurs
     */
    public static void main(final String[] args) throws Exception, CodeNotValidException {

        TrainModels instance = new TrainModels();
        instance.run(args);

    }

    public void run(final String[] args) throws Exception, CodeNotValidException {

        Timer timer = PipelineUtils.initAndStartTimer();

        String experimentalFolderName = PipelineUtils.setupExperimentalFolders("Experiments");

        File goldStandard = parseGoldStandFile(args);

        File codeDictionaryFile = new File(MachineLearningConfiguration.getDefaultProperties().getProperty("codeDictionaryFile"));
        CodeDictionary codeDictionary = new CodeDictionary(codeDictionaryFile);

        BucketGenerator generator = new BucketGenerator(codeDictionary);
        Bucket allRecords = generator.generateTrainingBucket(goldStandard);

        PipelineUtils.printStatusUpdate();

        ExactMatchClassifier exactMatchClassifier = new ExactMatchClassifier();
        exactMatchClassifier.setModelFileName(experimentalFolderName + "/Models/lookupTable");
        exactMatchClassifier.train(allRecords);

        OLRClassifier.setModelPath(experimentalFolderName + "/Models/olrModel");
        OLRClassifier olrClassifier = new OLRClassifier();
        olrClassifier.train(allRecords);
        timer.stop();
    }

    private File parseGoldStandFile(final String[] args) {

        File goldStandard = null;
        if (args.length > 2) {
            System.err.println("usage: $" + TrainClassifyOneFile.class.getSimpleName() + "    <goldStandardDataFile>    <modelLocation(optional)>");
        }
        if (args.length < 2) {
            System.err.println("usage: $" + TrainClassifyOneFile.class.getSimpleName() + "    <goldStandardDataFile>    <modelLocation(optional)>");
        }
        else {
            goldStandard = new File(args[0]);
            PipelineUtils.exitIfDoesNotExist(goldStandard);
            File modelLocation = new File(args[1]);
            PipelineUtils.exitIfDoesNotExist(modelLocation);

        }
        return goldStandard;
    }

}