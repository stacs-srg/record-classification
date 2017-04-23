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

import com.google.common.collect.Lists;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.Vector;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Trains a number of models on the training vectors provided. These models are then used to make predictions
 * on the testing vectors and ranked from best to worst. Only the best models are kept and used for prediction.
 *
 * @author Fraser Dunlop
 */
class OLRPool implements Runnable, Serializable {

    private static final long serialVersionUID = -7098039612837520093L;

    private static final int POOL_SIZE = 4;
    private static final int NUMBER_OF_SURVIVORS = 2;

    private List<OLRShuffled> models = Lists.newArrayList();
    private transient List<NamedVector> testingVectorList = Lists.newArrayList();

    /**
     * Needed for JSON deserialization.
     */
    public OLRPool() {
    }

    /**
     * Constructor.
     *
     * @param internalTrainingVectorList internal training vector list
     * @param testingVectorList          internal testing vector list
     */
    public OLRPool(final List<NamedVector> internalTrainingVectorList, final List<NamedVector> testingVectorList, int dictionary_size, int code_map_size) {

        this.testingVectorList = testingVectorList;

        for (int i = 0; i < POOL_SIZE; i++) {
            final List<NamedVector> trainingVectorList = new ArrayList<>(internalTrainingVectorList);
            OLRShuffled model = new OLRShuffled(trainingVectorList, dictionary_size, code_map_size);
            models.add(model);
        }
    }

    public OLRPool(final Matrix betaMatrix, final ArrayList<NamedVector> internalTrainingVectorList, final ArrayList<NamedVector> testingVectorList) {

        this.testingVectorList = testingVectorList;

        for (int i = 0; i < POOL_SIZE; i++) {
            final List<NamedVector> trainingVectorList = new ArrayList<>(internalTrainingVectorList);
            OLRShuffled model = new OLRShuffled(betaMatrix, trainingVectorList);
            models.add(model);
        }
    }

    /**
     * Trains, tests, packages, sorts.
     */
    @Override
    public void run() {

        try {

            ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);
            Collection<Future<?>> futures = new LinkedList<>();

            for (OLRShuffled model : models) {
                futures.add(executorService.submit(model));
            }

            Utils.handlePotentialErrors(futures);
            executorService.shutdown();
            executorService.awaitTermination(365, TimeUnit.DAYS);

        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Get the survivors from the model pool.
     */
    public List<OLRShuffled> survivors() {

        return survivors(testAndPackageModels());
    }

    private double getProportionTestingVectorsCorrectlyClassified(final OLRShuffled model) {

        int countCorrect = 0;
        for (NamedVector vector : testingVectorList) {
            Vector classificationVector = model.classifyFull(vector);
            int classification = classificationVector.maxValueIndex();
            if (Integer.parseInt(vector.getName()) == classification) {
                countCorrect++;
            }
        }
        return ((double) countCorrect) / ((double) testingVectorList.size());
    }

    private ArrayList<ModelDoublePair> testAndPackageModels() {

        ArrayList<ModelDoublePair> modelPairs = new ArrayList<>();
        for (OLRShuffled model : models) {
            double proportionCorrect = getProportionTestingVectorsCorrectlyClassified(model);
            modelPairs.add(new ModelDoublePair(model, proportionCorrect));
        }
        return modelPairs;
    }

    private List<OLRShuffled> survivors(final List<ModelDoublePair> modelPairs) {

        ArrayList<OLRShuffled> survivors = new ArrayList<>();
        Collections.sort(modelPairs);
        for (int i = modelPairs.size() - 1; i >= modelPairs.size() - NUMBER_OF_SURVIVORS; i--) {
            survivors.add(modelPairs.get(i).getModel());
        }
        return survivors;
    }
}
