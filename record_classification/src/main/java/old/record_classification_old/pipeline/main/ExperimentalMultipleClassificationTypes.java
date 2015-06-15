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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.DiceSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import old.record_classification_old.classifiers.closestmatchmap.ClosestMatchMap;
import old.record_classification_old.classifiers.closestmatchmap.SimilarityMetric;
import old.record_classification_old.classifiers.closestmatchmap.SimilarityMetricFromSimmetricFactory;
import old.record_classification_old.classifiers.closestmatchmap.StringSimilarityClassifier;
import old.record_classification_old.classifiers.lookup.ExactMatchClassifier;
import old.record_classification_old.classifiers.resolver.LengthWeightedLossFunction;
import old.record_classification_old.datastructures.Pair;
import old.record_classification_old.datastructures.analysis_metrics.CodeMetrics;
import old.record_classification_old.datastructures.analysis_metrics.ListAccuracyMetrics;
import old.record_classification_old.datastructures.analysis_metrics.StrictConfusionMatrix;
import old.record_classification_old.datastructures.bucket.Bucket;
import old.record_classification_old.datastructures.bucket.BucketFilter;
import old.record_classification_old.datastructures.bucket.BucketUtils;
import old.record_classification_old.datastructures.classification.Classification;
import old.record_classification_old.datastructures.code.Code;
import old.record_classification_old.datastructures.code.CodeDictionary;
import old.record_classification_old.datastructures.code.CodeNotValidException;
import old.record_classification_old.datastructures.records.Record;
import old.record_classification_old.datastructures.tokens.TokenSet;
import old.record_classification_old.datastructures.vectors.CodeIndexer;
import old.record_classification_old.pipeline.BucketGenerator;
import old.record_classification_old.pipeline.ClassifierPipeline;
import old.record_classification_old.pipeline.ExactMatchPipeline;
import old.record_classification_old.pipeline.IPipeline;
import old.record_classification_old.pipeline.PipelineUtils;
import old.record_classification_old.writers.DataClerkingWriter;
import old.record_classification_old.writers.FileComparisonWriter;
import old.record_classification_old.writers.MetricsWriter;
import old.record_classification_old.tools.Timer;
import old.record_classification_old.tools.configuration.MachineLearningConfiguration;

import com.google.common.io.Files;

public final class ExperimentalMultipleClassificationTypes {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentalMultipleClassificationTypes.class);
    private static double DEFAULT_TRAINING_RATIO = 0.8;

    /**
     * Entry method for training and classifying a batch of records into
     * multiple codes.
     * 
     * @param args
     *            <file1> training file <file2> file to classify
     * @throws Exception
     *             If exception occurs
     */
    public static void main(final String[] args) throws Exception, CodeNotValidException {

        ExperimentalMultipleClassificationTypes instance = new ExperimentalMultipleClassificationTypes();
        instance.run(args);
    }

    public void run(final String[] args) throws Exception, CodeNotValidException {

        String experimentalFolderName;
        File goldStandard;
        Bucket trainingBucket;
        Bucket predictionBucket1;

        Timer timer = PipelineUtils.initAndStartTimer();

        experimentalFolderName = PipelineUtils.setupExperimentalFolders("Experiments");

        goldStandard = parseGoldStandFile(args);
        double trainingRatio = parseTrainingPct(args);
        boolean multipleClassifications = parseMultipleClassifications(args);
        parseProperties(args);

        File codeDictionaryFile = new File(MachineLearningConfiguration.getDefaultProperties().getProperty("codeDictionaryFile"));
        CodeDictionary codeDictionary = new CodeDictionary(codeDictionaryFile);

        BucketGenerator generator = new BucketGenerator(codeDictionary);
        Bucket allRecords = generator.generateTrainingBucket(goldStandard);

        Bucket[] trainingPredicition = randomlyAssignToTrainingAndPrediction(allRecords, trainingRatio);
        trainingBucket = trainingPredicition[0];

        PipelineUtils.printStatusUpdate();

        CodeIndexer codeIndex = new CodeIndexer(allRecords);

        Map<String, Classification> map = getMap(trainingBucket);

        AbstractStringMetric simMetric = new JaccardSimilarity();
        String identifier = "Jaccard";
        predictionBucket1 = trainingPredicition[1];
        classifyAndWrite(experimentalFolderName, trainingBucket, predictionBucket1, multipleClassifications, allRecords, codeIndex, map, simMetric, identifier);

        //        simMetric = new JaroWinkler();
        //        identifier = "JaroWinkler";
        //        Bucket predictionBucket2 = copyOf(trainingPredicition[1]);
        //        classifyAndWrite(experimentalFolderName, trainingBucket, predictionBucket2, multipleClassifications, allRecords, codeIndex, map, simMetric, identifier);

        simMetric = new DiceSimilarity();
        identifier = "DiceSimilarity";
        Bucket predictionBucket3 = copyOf(trainingPredicition[1]);
        classifyAndWrite(experimentalFolderName, trainingBucket, predictionBucket3, multipleClassifications, allRecords, codeIndex, map, simMetric, identifier);

        simMetric = new Levenshtein();
        identifier = "Levenshtein";
        Bucket predictionBucket4 = copyOf(trainingPredicition[1]);
        classifyAndWrite(experimentalFolderName, trainingBucket, predictionBucket4, multipleClassifications, allRecords, codeIndex, map, simMetric, identifier);

        //        simMetric = new NeedlemanWunch();
        //        identifier = "NeedlemanWunch";
        //        Bucket predictionBucket5 = copyOf(trainingPredicition[1]);
        //        classifyAndWrite(experimentalFolderName, trainingBucket, predictionBucket5, multipleClassifications, allRecords, codeIndex, map, simMetric, identifier);
        //
        //        simMetric = new SmithWaterman();
        //        identifier = "SmithWaterman";
        //        Bucket predictionBucket6 = copyOf(trainingPredicition[1]);
        //        classifyAndWrite(experimentalFolderName, trainingBucket, predictionBucket6, multipleClassifications, allRecords, codeIndex, map, simMetric, identifier);
        //
        //        simMetric = new ChapmanMatchingSoundex();
        //        identifier = "ChapmanMatchingSoundex";
        //        Bucket predictionBucket7 = copyOf(trainingPredicition[1]);
        //        classifyAndWrite(experimentalFolderName, trainingBucket, predictionBucket7, multipleClassifications, allRecords, codeIndex, map, simMetric, identifier);

        timer.stop();

    }

    private Bucket copyOf(final Bucket bucket) {

        Bucket newBucket = new Bucket();
        for (Record record : bucket) {
            newBucket.addRecordToBucket(record.copyOfOriginalRecord(record));
        }
        return newBucket;
    }

    private void classifyAndWrite(final String experimentalFolderName, final Bucket trainingBucket, final Bucket predictionBucket, final boolean multipleClassifications, final Bucket allRecords, final CodeIndexer codeIndex, final Map<String, Classification> map,
                    final AbstractStringMetric simMetric, final String identifier) throws Exception {

        ExactMatchClassifier exactMatchClassifier = new ExactMatchClassifier();
        exactMatchClassifier.setModelFileName(experimentalFolderName + "/Models/lookupTable");
        exactMatchClassifier.train(trainingBucket);
        StringSimilarityClassifier similarityClassifier = trainStringSimilarityClassifier(map, simMetric);

        IPipeline exactMatchPipeline = new ExactMatchPipeline(exactMatchClassifier);
        IPipeline stringSimPipeline = new ClassifierPipeline(similarityClassifier, trainingBucket, new LengthWeightedLossFunction(), multipleClassifications, true);

        Bucket notExactMatched = exactMatchPipeline.classify(predictionBucket);
        Bucket notStringSim = stringSimPipeline.classify(notExactMatched);
        Bucket stringSimClassified = stringSimPipeline.getSuccessfullyClassified();
        Bucket exactMatchClassified = exactMatchPipeline.getSuccessfullyClassified();

        Bucket allClassifed = BucketUtils.getUnion(exactMatchClassified, stringSimClassified);
        Bucket allOutputRecords = BucketUtils.getUnion(exactMatchClassified, notStringSim);

        LOGGER.info("Exact Matched Bucket Size: " + exactMatchClassified.size());
        LOGGER.info("Similarity Metric Bucket Size: " + stringSimClassified.size());

        writeRecords(experimentalFolderName, identifier, allOutputRecords);

        writeComparisonFile(experimentalFolderName, identifier, allOutputRecords);

        final Bucket uniqueRecordsOnly = BucketFilter.uniqueRecordsOnly(allOutputRecords);
        printAllStats(experimentalFolderName, identifier, codeIndex, allOutputRecords, uniqueRecordsOnly);
        printAllStats(experimentalFolderName, identifier, codeIndex, stringSimClassified, BucketFilter.uniqueRecordsOnly(stringSimClassified));

    }

    public StringSimilarityClassifier trainStringSimilarityClassifier(final Map<String, Classification> map, final AbstractStringMetric simMetric) {

        SimilarityMetricFromSimmetricFactory factory = new SimilarityMetricFromSimmetricFactory();
        SimilarityMetric<String> metric = factory.create(simMetric);
        ClosestMatchMap<String, Classification> closestMatchMap = new ClosestMatchMap<>(metric, map);
        return new StringSimilarityClassifier(closestMatchMap);
    }

    private void printAllStats(final String experimentalFolderName, final String identifier, final CodeIndexer codeIndex, final Bucket allClassifed, final Bucket uniqueRecordsOnly) throws IOException {

        CodeMetrics codeMetrics = new CodeMetrics(new StrictConfusionMatrix(allClassifed, codeIndex), codeIndex);
        ListAccuracyMetrics accuracyMetrics = new ListAccuracyMetrics(allClassifed, codeMetrics);
        MetricsWriter metricsWriter = new MetricsWriter(accuracyMetrics, experimentalFolderName, codeIndex);
        metricsWriter.write(identifier, "allClassified");
        accuracyMetrics.prettyPrint("All Records");

        LOGGER.info("Unique Only");
        LOGGER.info("Unique Only  Bucket Size: " + uniqueRecordsOnly.size());

        CodeMetrics codeMetrics1 = new CodeMetrics(new StrictConfusionMatrix(uniqueRecordsOnly, codeIndex), codeIndex);
        accuracyMetrics = new ListAccuracyMetrics(uniqueRecordsOnly, codeMetrics1);
        accuracyMetrics.prettyPrint("Unique Only");
        metricsWriter = new MetricsWriter(accuracyMetrics, experimentalFolderName, codeIndex);
        metricsWriter.write(identifier, "unique records");
        accuracyMetrics.prettyPrint("Unique Records");
    }

    private void writeRecords(final String experimentalFolderName, final String identifier, final Bucket allClassifed) throws IOException {

        final String nrsReportPath = "/Data/" + identifier + "/NRSData.txt";
        final File outputPath = new File(experimentalFolderName + nrsReportPath);
        Files.createParentDirs(outputPath);
        final DataClerkingWriter writer = new DataClerkingWriter(outputPath);

        for (final Record record : allClassifed) {
            writer.write(record);
        }
        writer.close();
    }

    private void writeComparisonFile(final String experimentalFolderName, final String identifier, final Bucket allClassifed) throws IOException, FileNotFoundException, UnsupportedEncodingException {

        final String comparisonReportPath = "/Data/" + identifier + "/comaprison.txt";
        final File outputPath2 = new File(experimentalFolderName + comparisonReportPath);
        Files.createParentDirs(outputPath2);

        final FileComparisonWriter comparisonWriter = new FileComparisonWriter(outputPath2, "\t");
        for (final Record record : allClassifed) {
            comparisonWriter.write(record);
        }
        comparisonWriter.close();
    }

    private Map<String, Classification> getMap(final Bucket bucket) {

        return convert(prePopulateCache(bucket));

    }

    private Map<String, Classification> convert(final Map<TokenSet, Pair<Code, Double>> map) {

        Map<String, Classification> newMap = new HashMap<String, Classification>();
        for (TokenSet key : map.keySet()) {
            newMap.put(key.toString(), new Classification(map.get(key).getLeft(), key, map.get(key).getRight()));
        }
        return newMap;
    }

    private static File parseGoldStandFile(final String[] args) {

        File goldStandard = null;
        if (args.length > 4) {
            System.err.println("usage: $" + ExperimentalMultipleClassificationTypes.class.getSimpleName() + "    <goldStandardDataFile>    <trainingRatio(optional)>  <output multiple classificatiosn> <properties file>");
        }
        else {
            goldStandard = new File(args[0]);
            PipelineUtils.exitIfDoesNotExist(goldStandard);

        }
        return goldStandard;
    }

    private boolean parseMultipleClassifications(final String[] args) {

        if (args.length > 4) {
            System.err.println("usage: $" + ClassifyWithExistingModels.class.getSimpleName() + "    <goldStandardDataFile>    <trainingRatio(optional)>    <output multiple classificatiosn> <properties file>");
        }
        else {
            if (args[2].equals("1")) { return true; }
        }
        return false;

    }

    private static double parseTrainingPct(final String[] args) {

        double trainingRatio = DEFAULT_TRAINING_RATIO;
        if (args.length > 1) {
            double userRatio = Double.valueOf(args[1]);
            if (userRatio > 0 && userRatio < 1) {
                trainingRatio = userRatio;
            }
            else {
                System.err.println("trainingRatio must be between 0 and 1. Exiting.");
                System.exit(1);
            }
        }
        return trainingRatio;
    }

    public File parseProperties(String[] args) {

        File properties = null;

        if (args.length > 3) {
            properties = new File(args[3]);

            if (properties.exists()) {
                MachineLearningConfiguration.loadProperties(properties);
                System.out.println(MachineLearningConfiguration.getDefaultProperties().getProperty("codeDictionaryFile"));
            }
            else {
                LOGGER.error("Supplied properties file does not exsist. Using system defaults");

            }
        }
        return properties;
    }

    private Bucket[] randomlyAssignToTrainingAndPrediction(final Bucket bucket, final double trainingRatio) {

        Bucket[] buckets = initBuckets();

        for (Record record : bucket) {
            if (Math.random() < trainingRatio) {
                buckets[0].addRecordToBucket(record);
            }
            else {
                buckets[1].addRecordToBucket(record);
            }
        }
        return buckets;
    }

    private Bucket[] initBuckets() {

        Bucket[] buckets = new Bucket[2];
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new Bucket();
        }
        return buckets;
    }

    private Map<TokenSet, Pair<Code, Double>> prePopulateCache(final Bucket cacheBucket) {

        Map<TokenSet, Pair<Code, Double>> cache = new HashMap<TokenSet, Pair<Code, Double>>();

        for (Record record : cacheBucket) {
            List<Classification> singles = getSinglyCodedTriples(record);
            cache = addAll(singles, cache);
        }
        return cache;
    }

    /**
     * Gets the singly coded triples, that is codeTriples that have only one coding.
     *
     * @param record the record to get single triples from
     * @return the singly coded triples
     */
    protected List<Classification> getSinglyCodedTriples(final Record record) {

        List<Classification> singles = new ArrayList<>();

        final Set<Classification> goldStandardClassificationSet = record.getGoldStandardClassificationSet();
        for (Classification codeTriple1 : goldStandardClassificationSet) {
            int count = 0;
            for (Classification codeTriple2 : goldStandardClassificationSet) {
                if (codeTriple1.getTokenSet().equals(codeTriple2.getTokenSet())) {
                    count++;
                }
            }
            if (count == 1) {
                singles.add(codeTriple1);
            }
        }

        return singles;
    }

    /**
     * Add all CodeTriples to the cache.
     * @param setOfCodeTriples Set to add
     * @return 
     */
    public Map<TokenSet, Pair<Code, Double>> addAll(final List<Classification> setOfCodeTriples, final Map<TokenSet, Pair<Code, Double>> cache) {

        for (Classification codeTriple : setOfCodeTriples) {
            cache.put(codeTriple.getTokenSet(), new Pair<Code, Double>(codeTriple.getCode(), codeTriple.getConfidence()));
        }
        return cache;
    }
}