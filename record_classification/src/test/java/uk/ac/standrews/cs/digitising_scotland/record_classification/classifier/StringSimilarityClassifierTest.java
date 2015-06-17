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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier;

import old.record_classification_old.classifiers.closestmatchmap.CarsonSimilarity;
import old.record_classification_old.datastructures.tokens.TokenSet;
import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Tests {@link StringSimilarityClassifier}.
 *
 * @author Masih Hajiarab Derkani
 */
public class StringSimilarityClassifierTest {

    private static final String RECORD_DATA = "record";
    private static final String SIMILAR_RECORD_DATA = "recrod";
    private static final String DISSIMILAR_RECORD_DATA = "fish";
    private static final Record TRAINING_RECORD = new Record(1, RECORD_DATA, new Classification("media", new TokenSet(), 1.0));

    private Bucket training_records;
    private StringSimilarityClassifier classifier;

    @Before
    public void setUp() throws Exception {

        classifier = new StringSimilarityClassifier(new CarsonSimilarity<>());
        training_records = new Bucket();
        training_records.add(TRAINING_RECORD);
    }

    @Test
    public void testTrainAndClassify() throws Exception {

        assertEquals(Classification.UNCLASSIFIED, classifier.classify(RECORD_DATA));
        assertEquals(Classification.UNCLASSIFIED, classifier.classify(SIMILAR_RECORD_DATA));
        classifier.train(training_records);
        assertNotEquals(Classification.UNCLASSIFIED, classifier.classify(RECORD_DATA));
        assertNotEquals(Classification.UNCLASSIFIED, classifier.classify(SIMILAR_RECORD_DATA));
        assertEquals(Classification.UNCLASSIFIED, classifier.classify(DISSIMILAR_RECORD_DATA));
    }
}
