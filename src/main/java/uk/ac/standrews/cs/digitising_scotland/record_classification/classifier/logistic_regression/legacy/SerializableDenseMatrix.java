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

import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Fraser Dunlop
 */
class SerializableDenseMatrix implements Serializable {

    private static final long serialVersionUID = -7738028674358816065L;

    private DenseMatrix matrix;

    /**
     * Needed for JSON deserialization.
     */
    public SerializableDenseMatrix(){}

    public SerializableDenseMatrix(final Matrix matrix) {

        this.matrix = new DenseMatrix(asArray(matrix));
    }

    public SerializableDenseMatrix(final int i, final int j) {

        this.matrix = new DenseMatrix(i, j);
    }

    public DenseMatrix getMatrix() {

        return matrix;
    }

    public void setMatrix(DenseMatrix matrix) {

        this.matrix= matrix;
    }

    public static double[][] asArray(final Matrix matrix) {

        double[][] array = new double[matrix.numRows()][matrix.numCols()];
        for (int i = 0; i < matrix.numRows(); i++) {
            for (int j = 0; j < matrix.numCols(); j++)
                array[i][j] = matrix.get(i, j);
        }
        return array;
    }

    public double get(final int i, final int j) {

        return matrix.get(i, j);
    }

    public double getQuick(final int category, final int feature) {

        return matrix.get(category, feature);
    }

    public void setQuick(final int category, final int feature, final double newValue) {

        matrix.setQuick(category, feature, newValue);
    }

    public Vector times(final Vector instance) {

        return matrix.times(instance);
    }

    public void set(final int category, final int feature, final double newValue) {

        matrix.set(category, feature, newValue);
    }

    // Next two methods needed for Java serialization.
    private void readObject(final ObjectInputStream inputStream) throws ClassNotFoundException, IOException {

        matrix = new DenseMatrix((double[][]) inputStream.readObject());
    }

    private void writeObject(final ObjectOutputStream outputStream) throws IOException {

        outputStream.writeObject(asArray(matrix));
    }
}
