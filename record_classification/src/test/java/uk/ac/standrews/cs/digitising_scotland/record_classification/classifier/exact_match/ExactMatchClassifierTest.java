/*
 * record_classification - Automatic record attribute classification.
 * Copyright © 2014-2016 Digitising Scotland project
 * (http://digitisingscotland.cs.st-andrews.ac.uk/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.exact_match;

import org.junit.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.*;

import java.util.function.*;

import static org.junit.Assert.*;

/**
 * @author masih
 */
public class ExactMatchClassifierTest extends ClassifierTest {

    @Test
    public void exactMatchClassificationHasConfidenceOfOne() {

        SingleClassifier classifier = newClassifier();

        classifier.trainModel(training_bucket);

        assertEquals(1.0, classifier.classify("trail").getConfidence(), DELTA);
        assertEquals(1.0, classifier.classify("through").getConfidence(), DELTA);
    }

    @Test
    public void exactMatchUnclassifiedHasConfidenceOfZero() {

        SingleClassifier classifier = newClassifier();
        classifier.trainModel(training_bucket);
        assertEquals(0.0, classifier.classify("never seen before string").getConfidence(), DELTA);
    }

    @Override
    protected ExactMatchClassifier newClassifier() {

        return new ExactMatchClassifier();
    }
}
