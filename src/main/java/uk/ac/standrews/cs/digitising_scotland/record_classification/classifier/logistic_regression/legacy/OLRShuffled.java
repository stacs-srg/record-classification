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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Training involves shuffling the training vectors and training the model. This process is repeated for the desired
 * number of repetitions.
 *
 * @author Fraser Dunlop
 */
class OLRShuffled implements Runnable, Serializable {

    private static final long serialVersionUID = -4877057661440167819L;

    private OLR model;
    private static final int NUMBER_OF_REPETITIONS = 10;
    private transient List<NamedVector> trainingVectorList = new ArrayList<>();

    /**
     * Needed for JSON deserialization.
     */
    public OLRShuffled() {
    }

    /**
     * Constructor.
     *
     * @param trainingVectorList the training vector list
     */
    public OLRShuffled(final List<NamedVector> trainingVectorList, int dictionary_size, int code_map_size) {

        this.trainingVectorList = trainingVectorList;
        model = new OLR(dictionary_size, code_map_size);
    }

    /**
     * Constructor.
     *
     * @param betaMatrix         the new beta matrix which is used as a starting point for model training
     * @param trainingVectorList the training vector list
     */
    public OLRShuffled(final Matrix betaMatrix, final List<NamedVector> trainingVectorList) {

        this.trainingVectorList = trainingVectorList;
        this.model = new OLR(betaMatrix);
    }

    @Override
    public void run() {

        for (int rep = 0; rep < NUMBER_OF_REPETITIONS; rep++) {

            trainingVectorList.forEach(model::train);
        }
    }

    /**
     * Classifies a vector.
     *
     * @param instance vector to classify
     * @return vector encoding probability distribution over output classes
     */
    protected Vector classifyFull(final Vector instance) {

        return model.classifyFull(instance);
    }

    /**
     * Gets the beta matrix.
     *
     * @return the beta maxtrix
     */
    protected Matrix beta() {

        return model.getBeta().getMatrix();
    }

}
