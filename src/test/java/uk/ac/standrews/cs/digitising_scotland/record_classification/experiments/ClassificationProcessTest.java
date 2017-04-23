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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.Classifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.ClassifierSupplier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@RunWith(Parameterized.class)
public class ClassificationProcessTest extends AbstractClassificationProcessTest {

    private final Supplier<Classifier> classifier_supplier;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> generateData() {

        List<Object[]> result = new ArrayList<>();

        result.add(new Object[]{ClassifierSupplier.STRING_SIMILARITY_DICE});
        result.add(new Object[]{ClassifierSupplier.STRING_SIMILARITY_JACCARD});
        result.add(new Object[]{ClassifierSupplier.OLR});
        result.add(new Object[]{ClassifierSupplier.NAIVE_BAYES});
        result.add(new Object[]{ClassifierSupplier.VOTING_ENSEMBLE_EXACT_ML_SIMILARITY});
        result.add(new Object[]{ClassifierSupplier.VOTING_ENSEMBLE_EXACT_SIMILARITY});

        return result;
    }

    public ClassificationProcessTest(Supplier<Classifier> classifier_supplier) {

        this.classifier_supplier = classifier_supplier;
    }

    protected Supplier<Classifier> getClassifierSupplier() {

        return classifier_supplier;
    }

    /**
     * This just checks that the process of classification and evaluation runs without errors.
     *
     * @throws Exception
     */
    @Test
    public void checkProcessRunsWithoutErrors() throws Exception {
    }
}
