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
package uk.ac.standrews.cs.digitising_scotland.record_classification.experiments;

import org.junit.Test;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.Classifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.ClassifierSupplier;

import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class ExactMatchClassificationProcessTest extends AbstractClassificationProcessTest {

    protected Supplier<Classifier> getClassifierSupplier() {

        return ClassifierSupplier.EXACT_MATCH;
    }

    @Test
    public void checkPrecision() throws Exception {

        // Macro-precision must be 100% since every positive classification decision must be correct.
        // Micro-precision probably won't be 100%, since unclassified decisions are considered as false positives
        // for 'unclassified', and included in the calculation.

        assertEquals(1.0, metrics.getMacroAveragePrecision(), DELTA);
    }

    @Test
    public void checkTruePositivesAndFalseNegatives() throws Exception {

        // Every decision by exact match must be correct (true positive) or absent (false negative).
        assertEquals(matrix.getNumberOfClassifications(), matrix.getNumberOfTruePositives() + matrix.getNumberOfFalseNegatives());
    }

    @Test
    public void checkTrueNegatives() throws Exception {

        // Every class but one accumulates a true negative on each classification.
        // Need to subtract another 1 from the number of classes to account for 'unclassified'.
        assertEquals((matrix.getNumberOfClasses() - 2) * matrix.getNumberOfClassifications(), matrix.getNumberOfTrueNegatives());
    }
}
