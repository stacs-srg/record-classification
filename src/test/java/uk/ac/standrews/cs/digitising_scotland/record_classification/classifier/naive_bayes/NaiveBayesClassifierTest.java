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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.naive_bayes;

import org.junit.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import static org.junit.Assert.*;

/**
 * @author Masih Hajiarab Derkani
 */
public class NaiveBayesClassifierTest extends ClassifierTest {

    public static final Bucket GOLD_STANDARD_WITH_SPACE_IN_CODE_PREFIX_SUFFIX = new Bucket();

    static {
        GOLD_STANDARD_WITH_SPACE_IN_CODE_PREFIX_SUFFIX.add(new Record(1, "fish", new Classification("swims", new TokenList("fish"), 1.0, null)));
        GOLD_STANDARD_WITH_SPACE_IN_CODE_PREFIX_SUFFIX.add(new Record(2, "fish", new Classification("swims   ", new TokenList("fish"), 1.0, null)));
        GOLD_STANDARD_WITH_SPACE_IN_CODE_PREFIX_SUFFIX.add(new Record(3, "fish", new Classification("   swims", new TokenList("fish"), 1.0, null)));
    }

    @Test(expected = RuntimeException.class)
    public void trainingFailsIfClassificationCodeIsUntrimmed() throws Exception {

        // bug in weka: seems to internally trim codes 
        // fixed by trimming classification codes prior to training

        final NaiveBayesClassifier classifier = newClassifier();
        classifier.trainModel(GOLD_STANDARD_WITH_SPACE_IN_CODE_PREFIX_SUFFIX);
    }

    @Test
    public void trainingSucceedsIfClassificationCodeIsTrimmed() throws Exception {

        // bug in weka: seems to internally trim codes
        // fixed by trimming classification codes prior to training

        final NaiveBayesClassifier classifier = newClassifier();
        final Bucket gold_standard_with_trimmed_code = new TrimClassificationCodesCleaner().apply(GOLD_STANDARD_WITH_SPACE_IN_CODE_PREFIX_SUFFIX);
        classifier.trainModel(gold_standard_with_trimmed_code);
    }

    @Override
    protected NaiveBayesClassifier newClassifier() {

        return new NaiveBayesClassifier();
    }
}
