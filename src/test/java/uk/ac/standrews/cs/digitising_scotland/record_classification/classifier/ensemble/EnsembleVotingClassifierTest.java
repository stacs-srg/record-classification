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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.ensemble;

import org.junit.runner.*;
import org.junit.runners.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.*;

import java.util.*;
import java.util.function.*;

import static org.junit.Assert.*;

/**
 * @author masih
 */
@RunWith(Parameterized.class)
public class EnsembleVotingClassifierTest extends ClassifierTest {

    private Supplier<Classifier> factory;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> generateData() {

        List<Object[]> result = new ArrayList<>();

        result.add(new Object[]{ClassifierSupplier.VOTING_ENSEMBLE_EXACT_ML_SIMILARITY});
        result.add(new Object[]{ClassifierSupplier.VOTING_ENSEMBLE_EXACT_SIMILARITY});

        return result;
    }

    public EnsembleVotingClassifierTest(Supplier<Classifier> factory) {

        this.factory = factory;
    }

    @Override
    protected Classifier newClassifier() {

        return factory.get();
    }
}
