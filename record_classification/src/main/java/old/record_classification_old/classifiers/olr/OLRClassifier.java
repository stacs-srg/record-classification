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
package old.record_classification_old.classifiers.olr;

import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import old.record_classification_old.classifiers.Classifier;
import old.record_classification_old.datastructures.bucket.Bucket;
import old.record_classification_old.datastructures.classification.Classification;
import old.record_classification_old.datastructures.code.Code;
import old.record_classification_old.datastructures.records.Record;
import old.record_classification_old.datastructures.tokens.TokenSet;
import old.record_classification_old.datastructures.vectors.CodeIndexer;
import old.record_classification_old.datastructures.vectors.VectorFactory;
import old.record_classification_old.tools.configuration.MachineLearningConfiguration;
import old.record_classification_old.classifiers.AbstractClassifier;

import java.io.*;
import java.util.*;

/**
 * Provides methods for training and classifying records.
 * This classifier utilises the {@link OLRCrossFold} objects in order to build the best possible models.
 * More information can be found on the project
 *  <a href="http://digitisingscotland.cs.st-andrews.ac.uk/record_classification/algorithms-information.html"> Algorithms Page</a>.
 *
 * @author frjd2, jkc25
 */
public class OLRClassifier extends AbstractClassifier implements Classifier, Serializable {

    private static final long serialVersionUID = -2561454096763303789L;
    private static final Logger LOGGER = LoggerFactory.getLogger(OLRClassifier.class);
    private OLRCrossFold model = null;
    private final Properties properties;
    private VectorFactory vectorFactory;

    /** The Constant MODELPATH. Default is target/olrModelPath, but can be overwritten. */
    private static String modelPath = "target/olrModelPath";

    /**
     * Constructor.
     */
    public OLRClassifier() {

        model = new OLRCrossFold();
        properties = MachineLearningConfiguration.getDefaultProperties();
    }

    /**
     * Constructor.
     *
     * @param customProperties custom properties file
     */
    public OLRClassifier(final String customProperties) {

        MachineLearningConfiguration mlc = new MachineLearningConfiguration();
        properties = mlc.extendDefaultProperties(customProperties);
    }

    /**
     * Trains an OLRCrossfold model on a bucket.
     *
     * @param bucket bucket to train on
     */
    public void train(final Bucket bucket) {

        CodeIndexer index;

        if (vectorFactory == null) {
            index = new CodeIndexer(bucket);
            vectorFactory = new VectorFactory(bucket, index);
            ArrayList<NamedVector> trainingVectorList = getTrainingVectors(bucket);
            Collections.shuffle(trainingVectorList);
            model = new OLRCrossFold(trainingVectorList, properties);
        }
        else {
            int classCountDiff = getNumClassesAdded(bucket);
            int featureCountDiff = getFeatureCountDiff(bucket);
            Matrix matrix = expandModel(featureCountDiff, classCountDiff);
            ArrayList<NamedVector> trainingVectorList = getTrainingVectors(bucket);
            Collections.shuffle(trainingVectorList);
            model = new OLRCrossFold(trainingVectorList, properties, matrix);
        }

        model.train();

        writeModel();
    }

    private ArrayList<NamedVector> getTrainingVectors(final Bucket bucket) {

        ArrayList<NamedVector> trainingVectorList = new ArrayList<>();

        for (Record record : bucket) {
            final List<NamedVector> listOfVectors = vectorFactory.generateVectorsFromRecord(record);
            trainingVectorList.addAll(listOfVectors);
        }
        return trainingVectorList;
    }

    private Matrix expandModel(final int featureCountDiff, final int classCountDiff) {

        return MatrixEnlarger.enlarge(model.getAverageBetaMatrix(), featureCountDiff, classCountDiff);
    }

    private int getFeatureCountDiff(final Bucket bucket) {

        int initNoFeatures = vectorFactory.getNumberOfFeatures();
        vectorFactory.updateDictionary(bucket);
        int newNoFeatures = vectorFactory.getNumberOfFeatures();
        return newNoFeatures - initNoFeatures;
    }

    private int getNumClassesAdded(final Bucket bucket) {

        int initNoClasses = vectorFactory.getCodeIndexer().getNumberOfOutputClasses();
        vectorFactory.getCodeIndexer().addGoldStandardCodes(bucket);
        int newNoClasses = vectorFactory.getCodeIndexer().getNumberOfOutputClasses();
        return newNoClasses - initNoClasses;
    }

    private void writeModel() {

        try {
            serializeModel(modelPath);
        }
        catch (IOException e) {
            LOGGER.error("Could not write model. IOException has occured", e.getCause());
        }
    }

    public Set<Classification> classify(final TokenSet tokenSet) {

        NamedVector vector = vectorFactory.createNamedVectorFromString(tokenSet.toString(), "unknown");
        Vector classifyFull = model.classifyFull(vector);
        int classificationID = classifyFull.maxValueIndex();
        Code code = vectorFactory.getCodeIndexer().getCode(classificationID);
        double confidence = Math.exp(model.logLikelihood(classificationID, vector));

        return makeClassificationSet( new Classification(code, tokenSet, confidence));
    }

    /**
     * Overrides the default path and sets to the path provided.
     * @param modelPath New path to write model to
     */
    public static void setModelPath(final String modelPath) {

        OLRClassifier.modelPath = modelPath;
    }

    /**
     * Returns the {@link VectorFactory} used when training this classifier.
     * @return vectorFactory  the {@link VectorFactory} used when training this classifier.
     */
    public VectorFactory getVectorFactory() {

        return vectorFactory;
    }

    public OLRClassifier getModelFromDefaultLocation() {

        OLRClassifier olr = null;
        try {
            olr = deSerializeModel(modelPath);
            model = olr.model;
            vectorFactory = olr.vectorFactory;
        }
        catch (Exception e) {
            LOGGER.error("Could not get model from default location (" + modelPath + "). IOEception has occured.", e);
        }
        return olr;
    }

    /**
     * Allows serialization of the model to file.
     *
     * @param filename name of file to serialize model to
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void serializeModel(final String filename) throws IOException {

        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
        out.writeObject(this);
        out.close();
    }

    /**
     * Allows de-serialization of a model from a file. The de-serialized model is not trainable.
     *
     * @param filename name of file to de-serialize
     * @return {@link OLRClassifier} that has been read from disk. Does not contain all training vectors so can
     * only be used for classification
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public OLRClassifier deSerializeModel(final String filename) throws IOException, ClassNotFoundException {

        try (ObjectInputStream input_stream = new ObjectInputStream(new FileInputStream(filename))) {

            return (OLRClassifier) input_stream.readObject();
        }
    }
}