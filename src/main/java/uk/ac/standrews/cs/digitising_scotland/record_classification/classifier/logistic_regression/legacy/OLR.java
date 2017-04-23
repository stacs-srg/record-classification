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

import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.math.function.Functions;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An online logistic regression model.
 */
class OLR implements Serializable {

    private static final long serialVersionUID = 4157757308558382483L;

    /**
     * The minimum permitted value for the log likelihood.
     */
    private static final double LOGLIK_MINIMUM = -100.0;

    /**
     * The learning rate.
     */
    private static final double LEARNING_RATE = 100;

    /**
     * The decay rates for per term annealing - allows each feature its own learning.
     */
    private static final double ANNEALING_RATE = 0.999;

    private int number_of_categories;

    private SerializableDenseMatrix beta;

    private int[] updateCounts;

    private volatile double runningLogLikelihood;

    private volatile AtomicInteger numLogLikelihoodSumUpdates;

    private volatile AtomicLong numTrained;

    public OLR() {
    }

    public OLR(int dictionary_size, int code_map_size) {

        init(new SerializableDenseMatrix(code_map_size - 1, dictionary_size), dictionary_size, code_map_size);
    }

    public OLR(final Matrix matrix) {

        init(new SerializableDenseMatrix(matrix.clone()), matrix.numCols(), matrix.numRows() + 1);
    }

    /**
     * Gets the beta.
     *
     * @return the beta
     */
    protected SerializableDenseMatrix getBeta() {

        return beta;
    }

    /**
     * Classifies an instance vector and returns a result vector.
     *
     * @param instance the instance vector to classify
     * @return the result vector
     */
    protected Vector classifyFull(final Vector instance) {

        Vector r = new DenseVector(number_of_categories);
        r.viewPart(1, number_of_categories - 1).assign(classify(instance));
        r.setQuick(0, 1.0 - r.zSum());
        return r;
    }

    /**
     * Trains an OLR model on a instance vector with a known 'actual' output class.
     *
     * @param instance feature vector
     */
    protected void train(final NamedVector instance) {

        numTrained.getAndIncrement();
        updateModelParameters(instance);
        updateCounts(instance);
    }

    private void init(SerializableDenseMatrix beta, int number_of_features, int number_of_categories) {

        this.beta = beta;

        runningLogLikelihood = 0.;
        numLogLikelihoodSumUpdates = new AtomicInteger(1);
        numTrained = new AtomicLong(0);

        this.number_of_categories = number_of_categories;

        updateCounts = new int[number_of_features];
    }

    private void updateLogLikelihoodSum(final int actual, final Vector classification) {

        double thisloglik;

        if (actual > 0) {
            final double classificationGet = classification.get(actual - 1);
            final double mathLog = Math.log(classificationGet);
            thisloglik = Math.max(LOGLIK_MINIMUM, mathLog);
        } else {
            thisloglik = Math.max(LOGLIK_MINIMUM, Math.log1p(-classification.zSum()));
        }

        if (numLogLikelihoodSumUpdates.get() != 0) {
            runningLogLikelihood += (thisloglik - runningLogLikelihood) / numLogLikelihoodSumUpdates.get();
        } else {
            runningLogLikelihood = thisloglik;
        }

        numLogLikelihoodSumUpdates.getAndIncrement();
    }

    private void updateModelParameters(final NamedVector instance) {

        Vector gradient = calcGradient(instance);
        for (int category = 0; category < number_of_categories - 1; category++) {
            updateBetaCategory(instance, gradient, category);
        }
    }

    private Vector calcGradient(final NamedVector instance) {

        int actual = Integer.parseInt(instance.getName());

        // what does the current model say?
        Vector v = classify(instance);
        updateLogLikelihoodSum(actual, v);
        Vector r = v.like();

        if (actual != 0) {
            r.setQuick(actual - 1, 1);
        }
        r.assign(v, Functions.MINUS);

        return r;
    }

    private void updateBetaCategory(final Vector instance, final Vector gradient, final int category) {

        double gradientBase = gradient.get(category);
        Iterable<Element> element = instance.nonZeroes();
        for (Element nonZeroFeature : element) {
            updateCategoryAtNonZeroFeature(category, gradientBase, nonZeroFeature);
        }
    }

    private void updateCategoryAtNonZeroFeature(final int category, final double gradientBase, final Element featureElement) {

        final double aSmallNumber = 0.000001;
        int feature = featureElement.index();

        if (gradientBase > aSmallNumber || gradientBase < -aSmallNumber) {
            updateCoefficient(category, feature, featureElement, gradientBase);
        }
    }

    private void updateCoefficient(final int category, final int feature, final Element featureElement, final double gradientBase) {

        double newValue = beta.getQuick(category, feature) + gradientBase * getLearningRate(feature) * featureElement.get();
        beta.setQuick(category, feature, newValue);
    }

    private double getLearningRate(final int feature) {

        return LEARNING_RATE * Math.pow(ANNEALING_RATE, updateCounts[feature]);
    }

    /**
     * Returns n-1 probabilities, one for each category but the 0-th.  The probability of the 0-th
     * category is 1 - sum(this result).
     *
     * @param instance A vector of features to be classified.
     * @return A vector of probabilities, one for each of the first n-1 categories.
     */
    private Vector classify(final Vector instance) {

        return link(beta.times(instance));
    }

    private Vector link(final Vector v) {

        double max = v.maxValue();
        if (max >= 40) {
            // if max > 40, we subtract the large offset first
            // the size of the max means that 1+sum(exp(v)) = sum(exp(v)) to within round-off
            v.assign(Functions.minus(max)).assign(Functions.EXP);
            return v.divide(v.norm(1));
        } else {
            v.assign(Functions.EXP);
            return v.divide(1 + v.norm(1));
        }
    }

    private void updateCounts(final Vector instance) {

        Iterable<Element> instanceFeatures = instance.nonZeroes();

        for (Element feature : instanceFeatures) {
            updateCounts[feature.index()]++;
        }
    }
}
