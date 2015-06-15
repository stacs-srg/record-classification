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
package old.record_classification_old.classifiers.olr.serializabledensematrix;

import old.record_classification_old.classifiers.olr.SerializableDenseMatrix;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * Created by fraserdunlop on 10/10/2014 at 13:33.
 */
@RunWith(Parameterized.class)
@Ignore
public class SerializableDenseMatrixTest {

    private int numRows;
    private int numCols;

    public SerializableDenseMatrixTest(final int numRows, final int numCols) {

        this.numRows = numRows;
        this.numCols = numCols;
    }

    @Parameterized.Parameters
    public static Collection<Integer[]> params() {

        return Arrays.asList(new Integer[][]{{1, 2}, {10, 8}, {45, 34}, {7, 7},});
    }

    @Test
    public void test() throws IOException, ClassNotFoundException {

        double[][] values = generateValues(numRows, numCols);
        SerializableDenseMatrix matrix = new SerializableDenseMatrix(values);
        FileOutputStream fileOutputStream = new FileOutputStream("target/SerializableDenseMatrix.ser");
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(matrix);
        FileInputStream fileInputStream = new FileInputStream("target/SerializableDenseMatrix.ser");
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        SerializableDenseMatrix matrix1 = (SerializableDenseMatrix) objectInputStream.readObject();
        assertSame(matrix, matrix1);

    }

    private void assertSame(final SerializableDenseMatrix matrix, final SerializableDenseMatrix matrix1) {

        Assert.assertEquals(matrix.numCols(), matrix1.numCols());
        Assert.assertEquals(matrix.numRows(), matrix1.numRows());
        for (int i = 0; i < matrix.numRows(); i++) {
            for (int j = 0; j < matrix.numCols(); j++)
                Assert.assertEquals(matrix.get(i, j), matrix1.get(i, j), 0.0001);
        }
    }

    private double[][] generateValues(final int numRows, final int numCols) {

        double[][] array = new double[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++)
                array[i][j] = (i + Math.random()) * (j + Math.random());
        }
        return array;
    }
}