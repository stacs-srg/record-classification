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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.logistic_regression.legacy;

import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

/**
 * Distributes training vectors across {@link OLRPool}s in a cross fold manner. Allows concurrent training
 * of the {@link OLRPool}s and provides a classify method that averages the classifications given by each pool.
 *
 * @author Fraser Dunlop
 * @author Jamie Carson
 */
class OLRCrossFold implements Serializable {

    private static final long serialVersionUID = -749333540672669562L;

    /**
     * The Logger.
     */
    private static transient final Logger LOGGER = LoggerFactory.getLogger(OLRCrossFold.class);

    /**
     * The OLRPool models.
     */
    private List<OLRPool> models = new ArrayList<>();

    /**
     * The number of cross folds.
     */
    private static final int FOLDS = 4;

    /**
     * The classifier.
     */
    private OLR classifier;

    /**
     * Needed for JSON deserialization.
     */
    public OLRCrossFold() {}

    /**
     * Constructs an OLRCrossFold object with the given trainingVectors.
     *
     * @param trainingVectorList training vectors to use when training/validating each fold.
     */
    public OLRCrossFold(final List<NamedVector> trainingVectorList, int dictionary_size, int code_map_size) {

        ArrayList<NamedVector>[][] trainingVectors = init(trainingVectorList);
        for (int i = 0; i < FOLDS + 1; i++) {
            OLRPool model = new OLRPool(trainingVectors[i][0], trainingVectors[i][1], dictionary_size, code_map_size);
            models.add(model);
        }
    }

    /**
     * Constructs an OLRCrossFold object with the given trainingVectors.
     *
     * @param trainingVectorList training vectors to use when training/validating each fold.
     * @param betaMatrix         betaMatrix this matrix contains the betas and will be propagated down to the lowest OLR object.
     */
    public OLRCrossFold(final List<NamedVector> trainingVectorList, final Matrix betaMatrix) {

        ArrayList<NamedVector>[][] trainingVectors = init(trainingVectorList);
        for (int i = 0; i < FOLDS + 1; i++) {
            OLRPool model = new OLRPool(betaMatrix, trainingVectors[i][0], trainingVectors[i][1]);
            models.add(model);
        }
    }

    /**
     * Trains all the OLR models contained in this OLRCrossfold.
     */
    protected void train() {

        try {
            ExecutorService executorService = Executors.newFixedThreadPool(FOLDS);
            Collection<Future<?>> futures = new LinkedList<>();

            for (OLRPool model : models) {
                futures.add(executorService.submit(model));
            }

            Utils.handlePotentialErrors(futures);
            executorService.shutdown();
            final int timeout = 365;
            executorService.awaitTermination(timeout, TimeUnit.DAYS);

            prepareClassifier();

        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Returns the averaged beta matrix for this OLRCrossfold. If the OLRCrossfold has not been trained than an empty matrix will be returned.
     *
     * @return the averaged beta matrix for this OLRCrossfold, or an empty matrix if no training has been done.
     */
    protected Matrix averageBetaMatrix() {

        return classifier.getBeta().getMatrix();
    }

    /**
     * Classifies a vector.
     *
     * @param instance vector
     * @return vector encoding probability distribution over output classes
     */
    protected Vector classifyFull(final Vector instance) {

        return classifier.classifyFull(instance);
    }

    /**
     * Prepares the averaged OLR classifier for use by finding the top performing models and averaging their beta matrices.
     */
    private void prepareClassifier() {

        List<OLRShuffled> survivors = getSurvivors();
        Matrix classifierMatrix = getClassifierMatrix(survivors);
        classifier = new OLR(classifierMatrix);
    }

    /**
     * Gets the survivors for each of the {@link OLRShuffled} models.
     *
     * @return the survivors
     */
    private List<OLRShuffled> getSurvivors() {

        List<OLRShuffled> survivors = new ArrayList<>();
        for (OLRPool model : models) {
            survivors.addAll(model.survivors());
        }
        return survivors;
    }

    /**
     * Gets the classifier matrix.
     *
     * @param survivors the survivors
     * @return the classifier matrix
     */
    private Matrix getClassifierMatrix(final List<OLRShuffled> survivors) {

        Stack<Matrix> matrices = new Stack<>();
        for (OLRShuffled model : survivors) {
            matrices.add(model.beta());
        }
        Matrix classifierMatrix = matrices.pop();
        while (!matrices.empty()) {
            classifierMatrix = classifierMatrix.plus(matrices.pop());
        }
        classifierMatrix = classifierMatrix.divide(survivors.size());
        return classifierMatrix;
    }

    private ArrayList<NamedVector>[][] init(final List<NamedVector> trainingVectorList) {

        return CrossFoldFactory.make(trainingVectorList, FOLDS);
    }
}
