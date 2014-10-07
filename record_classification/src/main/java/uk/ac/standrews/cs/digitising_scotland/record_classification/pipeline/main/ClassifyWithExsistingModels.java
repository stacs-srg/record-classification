package uk.ac.standrews.cs.digitising_scotland.record_classification.pipeline.main;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.bucket.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.bucket.BucketUtils;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.code.CodeDictionary;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.records.Record;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.vectors.VectorFactory;
import uk.ac.standrews.cs.digitising_scotland.record_classification.pipeline.BucketGenerator;
import uk.ac.standrews.cs.digitising_scotland.record_classification.pipeline.ClassifierPipeline;
import uk.ac.standrews.cs.digitising_scotland.record_classification.pipeline.ClassifierTrainer;
import uk.ac.standrews.cs.digitising_scotland.record_classification.pipeline.ExactMatchPipeline;
import uk.ac.standrews.cs.digitising_scotland.record_classification.pipeline.IPipeline;
import uk.ac.standrews.cs.digitising_scotland.record_classification.pipeline.PipelineUtils;
import uk.ac.standrews.cs.digitising_scotland.tools.Timer;
import uk.ac.standrews.cs.digitising_scotland.tools.configuration.MachineLearningConfiguration;

/**
 * This class integrates the training of machine learning models and the
 * classification of records using those models. The classification process is
 * as follows: <br>
 * <br>
 * The gold standard training file is read in from the command line and a
 * {@link Bucket} of {@link Record}s are created from this file. A
 * {@link VectorFactory} is then created to manage the creation of vectors for
 * these records. The vectorFactory also manages the mapping of vectors IDs to
 * words, ie the vector dictionary. <br>
 * <br>
 * An {@link AbstractClassifier} is then created from the training bucket and
 * the model(s) are trained and saved to disk. <br>
 * <br>
 * The records to be classified are held in a file with the correct format as
 * specified by NRS. One record per line. This class initiates the reading of
 * these records. These are stored as {@link Record} objects inside a
 * {@link Bucket}. <br>
 * <br>
 * After the records have been created and stored in a bucket, classification
 * can begin. This is carried out by the {@link BucketClassifier} class which in
 * turn implements the {@link ClassifierPipeline}. Please see this
 * class for implementation details. <br>
 * <br>
 * Some initial metrics are then printed to the console and classified records
 * are written to file (target/NRSData.txt).
 * 
 * @author jkc25, frjd2
 */
public final class ClassifyWithExsistingModels {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassifyWithExsistingModels.class);

    /**
     * Entry method for training and classifying a batch of records into
     * multiple codes.
     * 
     * @param args
     *            <file1> training file <file2> file to classify
     * @throws Exception
     *             If exception occurs
     */
    public static void main(final String[] args) throws Exception {

        ClassifyWithExsistingModels instance = new ClassifyWithExsistingModels();
        instance.run(args);

    }

    public void run(final String[] args) throws Exception {

        Timer timer = PipelineUtils.initAndStartTimer();

        String experimentalFolderName = PipelineUtils.setupExperimentalFolders("Experiments");

        File goldStandard = parseGoldStandard(args);
        String modelLocation = parseModelLocation(args);
        boolean multipleClassifications = parseMultipleClassifications(args);

        File codeDictionaryFile = new File(MachineLearningConfiguration.getDefaultProperties().getProperty("codeDictionaryFile"));
        CodeDictionary codeDictionary = new CodeDictionary(codeDictionaryFile);
        BucketGenerator generator = new BucketGenerator(codeDictionary);
        Bucket allRecords = generator.generateTrainingBucket(goldStandard);

        PipelineUtils.printStatusUpdate();

        ClassifierTrainer trainer = PipelineUtils.getExistingModels(modelLocation, allRecords, experimentalFolderName);

        IPipeline exactMatchPipeline = new ExactMatchPipeline(trainer.getExactMatchClassifier());
        IPipeline machineLearningClassifier = new ClassifierPipeline(trainer.getOlrClassifier(), allRecords);

        Bucket notExactMatched = exactMatchPipeline.classify(allRecords, multipleClassifications);
        Bucket notMachineLearned = machineLearningClassifier.classify(notExactMatched, multipleClassifications);

        LOGGER.info("Exact Matched Bucket Size: " + exactMatchPipeline.getClassified().size());
        LOGGER.info("Machine Learned Bucket Size: " + machineLearningClassifier.getClassified().size());
        LOGGER.info("Not Classifed Bucket Size: " + notMachineLearned.size());

        Bucket allClassifed = BucketUtils.getUnion(exactMatchPipeline.getClassified(), machineLearningClassifier.getClassified());

        PipelineUtils.writeRecords(allClassifed, experimentalFolderName, "MachineLearning");

        PipelineUtils.generateAndPrintStatistics(allClassifed, trainer.getVectorFactory().getCodeIndexer(), experimentalFolderName, "MachineLearning");

        timer.stop();
    }

    private boolean parseMultipleClassifications(final String[] args) {

        if (args.length > 3) {
            System.err.println("usage: $" + ClassifyWithExsistingModels.class.getSimpleName() + "    <goldStandardDataFile>    <trainingRatio(optional)>    <output multiple classificatiosn");
        }
        else {
            if (args[2].equals("1")) { return true; }
        }
        return false;

    }

    private File parseGoldStandard(final String[] args) {

        File goldStandard = null;
        if (args.length > 3) {
            System.err.println("usage: $" + ClassifyWithExsistingModels.class.getSimpleName() + "    <goldStandardDataFile>    <trainingRatio(optional)>");
        }
        else {
            goldStandard = new File(args[0]);
            PipelineUtils.exitIfDoesNotExist(goldStandard);
        }
        return goldStandard;

    }

    private String parseModelLocation(final String[] args) {

        String modelLocation = null;
        if (args.length > 3) {
            System.err.println("usage: $" + ClassifyWithExsistingModels.class.getSimpleName() + "    <goldStandardDataFile>    <trainingRatio(optional)>");
        }
        else {
            modelLocation = args[1];
            PipelineUtils.exitIfDoesNotExist(new File(modelLocation));
        }
        return modelLocation;
    }
}
