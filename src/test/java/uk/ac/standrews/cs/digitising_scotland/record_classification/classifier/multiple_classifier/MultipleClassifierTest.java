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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.multiple_classifier;

import org.junit.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.exact_match.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author masih
 */
public class MultipleClassifierTest {

    //TODO improve multiple classification test
    @Test
    public void testClassify() throws Exception {

        final ExactMatchClassifier core_classifier = new ExactMatchClassifier();
        final MultipleClassifier multipleClassifier = new MultipleClassifier(core_classifier, 1, new EnglishStopWordCleaner().andThen(new PunctuationCleaner()).andThen(new LowerCaseCleaner()));

        final Bucket training_bucket = new Bucket();

        final Classification sweet = new Classification("sweet", new TokenList("sweet"), 1, "sweet mother of toast");
        final Classification savory = new Classification("savory", new TokenList("savory"), 1, "savory mother of mustard");
        final Classification salty = new Classification("salty", new TokenList("salty"), 1, "salty mother of sea");

        training_bucket.add(new Record(1, "strawberry jam", sweet));
        training_bucket.add(new Record(2, "fig jam", sweet));
        training_bucket.add(new Record(3, "beef steak", savory));
        training_bucket.add(new Record(4, "pork belly", savory));
        training_bucket.add(new Record(6, "beef", savory));
        training_bucket.add(new Record(7, "jerky", salty));
        training_bucket.add(new Record(8, "sea", salty));
        training_bucket.add(new Record(9, "placid", sweet));
        training_bucket.add(new Record(10, "fiery", savory));
        training_bucket.add(new Record(11, "turquoise", sweet));

        core_classifier.trainModel(training_bucket);

        final List<Classification> sweet_savory = Arrays.asList(savory, sweet);
        final List<Classification> sweet_salty = Arrays.asList(salty, sweet);
        final List<Classification> all_classifications = Arrays.asList(savory, sweet, salty);

        assertFullyJoint(sweet_savory, multipleClassifier.classify("beef jam steak fig"));
        assertFullyJoint(sweet_savory, multipleClassifier.classify("belly jam pork fig"));
        assertFullyJoint(sweet_savory, multipleClassifier.classify("strawberry belly jam pork fig jam"));
        assertFullyJoint(sweet_savory, multipleClassifier.classify("beef strawberry steak belly jam pork fig jam"));
        assertFullyJoint(sweet_salty, multipleClassifier.classify("jam jerky fig"));
        assertFullyJoint(sweet_salty, multipleClassifier.classify("strawberry jerky jam"));
        assertFullyJoint(sweet_salty, multipleClassifier.classify("jam strawberry fig jerky jam"));
        assertFullyJoint(all_classifications, multipleClassifier.classify("beef strawberry steak jam fig jerky jam beef"));
        assertFullyJoint(all_classifications, multipleClassifier.classify("pork the fig in the jam at the sea belly"));

        //TODO Takes too long; to be added to slow running tests with additional memory for JVM
        // assertFullyJoint(all_classifications, multipleClassifier.classify("sea, the fluid turquoise, soul captive of its beauty; so placid; so fiery."));
    }

    private <T> void assertFullyJoint(final List<T> expected, List<T> actual) {

        assertTrue(expected.size() == actual.size() && expected.containsAll(actual) && actual.containsAll(expected));
    }
}
