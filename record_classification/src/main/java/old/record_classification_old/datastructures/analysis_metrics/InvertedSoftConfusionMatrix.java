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
package old.record_classification_old.datastructures.analysis_metrics;

import java.util.Set;

import old.record_classification_old.datastructures.bucket.Bucket;
import old.record_classification_old.datastructures.classification.Classification;
import old.record_classification_old.datastructures.code.Code;
import old.record_classification_old.datastructures.vectors.CodeIndexer;

/**
 * Exists to count the number of predicted codes that are too specific. i.e. A1234
 * predicted when the true gold standard code was A123. False positive, false negative
 * and true negative getters all throw unsupported operation exceptions because they
 * are meaningless given this correctness function.
 * Created by fraserdunlop on 03/07/2014 at 10:09.
 */
public class InvertedSoftConfusionMatrix extends AbstractConfusionMatrix {

    /**
     * Instantiates a new inverted soft confusion matrix.
     *
     * @param bucket the bucket
     */
    public InvertedSoftConfusionMatrix(final Bucket bucket, final CodeIndexer codeIndex) {

        super(bucket, codeIndex);
    }

    /**
     * True posotive and false negative.
     *
     * @param setCodeTriples the set code triples
     * @param goldStandardTriples the gold standard triples
     */
    protected void truePosAndFalseNeg(final Set<Classification> setCodeTriples, final Set<Classification> goldStandardTriples) {

        for (Classification goldStandardCode : goldStandardTriples) {
            final Code code = goldStandardCode.getCode();
            if (hasDescendants(code, setCodeTriples)) {
                truePositive[index.getID(code)]++;
            }
        }
    }

    /**
     * Total and false positive.
     *
     * @param setCodeTriples the set code triples
     * @param goldStandardTriples the gold standard triples
     */
    protected void totalAndFalsePos(final Set<Classification> setCodeTriples, final Set<Classification> goldStandardTriples) {

        for (Classification predictedCode : setCodeTriples) {
            final Code code = predictedCode.getCode();
            totalPredictions[index.getID(code)]++;
        }
    }

    /**
     * Checks for descendants.
     *
     * @param code the code
     * @param setCodeTriples the set code triples
     * @return true, if successful
     */
    private boolean hasDescendants(final Code code, final Set<Classification> setCodeTriples) {

        for (Classification codeTriple : setCodeTriples) {
            if (codeTriple.getCode().isDescendant(code)) { return true; }
        }
        return false;
    }

    /**
     * Unsupported operation! Throws exception!.
     *
     * @return the false positive
     */
    @Override
    public double[] getFalsePositive() {

        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation! Throws exception!.
     *
     * @return the false negative
     */
    @Override
    public double[] getFalseNegative() {

        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation! Throws exception!.
     *
     * @return the true negative
     */
    @Override
    public double[] getTrueNegative() {

        throw new UnsupportedOperationException();
    }
}