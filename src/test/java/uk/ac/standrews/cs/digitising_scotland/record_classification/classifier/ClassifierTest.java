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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier;

import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.TokenList;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.serialization.*;

import java.nio.file.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.*;

import static org.junit.Assert.assertEquals;

/***
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
public abstract class ClassifierTest {

    protected static final double DELTA = 0.999;

    protected static final String[] TEST_VALUES = new String[]{"trial", "house", "thought", "quick brown fish", "lazy dogs", "lazy \"dogs\""};

    protected static final Record[] TRAINING_RECORDS = new Record[]{new Record(1, "trail", new Classification("class1", new TokenList("trail"), 1.0, null)), new Record(2, "mouse", new Classification("class2", new TokenList("mouse"), 1.0, null)),
                                                                    new Record(3, "through", new Classification("class3", new TokenList("through"), 1.0, null)), new Record(4, "quick brown fox", new Classification("class4", new TokenList("quick brown fox"), 1.0, null)),
                                                                    new Record(5, "lazy dog", new Classification("class4", new TokenList("lazy dog"), 1.0, null)), new Record(6, "lazy \"cat\"", new Classification("class4", new TokenList("lazy cat"), 1.0, null))};

    protected static final Record[] TEST_RECORDS = new Record[]{new Record(1, TEST_VALUES[0]), new Record(2, TEST_VALUES[1]), new Record(3, TEST_VALUES[2]), new Record(4, TEST_VALUES[3]), new Record(5, TEST_VALUES[4]), new Record(6, TEST_VALUES[5])};

    protected Bucket training_bucket;

    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    public ClassifierTest() {

        training_bucket = new Bucket(TRAINING_RECORDS);

        Logger.getLogger(Classifier.class.getName()).setLevel(Level.OFF);
    }

    @Test
    public void untrainedClassifierReturnsUnclassified() {

        Classifier classifier = newClassifier();

        for (String value : TEST_VALUES) {
            assertEquals(Classification.UNCLASSIFIED, classifier.classify(value));
        }
    }

    @Test
    public void testSerialization() throws Exception {

        Logger.getLogger(Classifier.class.getName()).setLevel(Level.OFF);

        final Classifier classifier = newClassifier();
        trainOnTrainingRecords(classifier);

        final Bucket classified = classifyTestRecords(classifier);

        final Path file = temporary.newFile().toPath();

        Serialization.persist(file, classifier, SerializationFormat.JAVA_SERIALIZATION);
        final Classifier deserialised_classifier = Serialization.load(file, Classifier.class, SerializationFormat.JAVA_SERIALIZATION);

        final Bucket actual = classifyTestRecords(deserialised_classifier);
        assertEquals(classified, actual);
    }

    protected abstract Classifier newClassifier();

    protected static Bucket classifyTestRecords(final Classifier classifier) {return classifier.classify(new Bucket(TEST_RECORDS));}

    public static void trainOnTrainingRecords(final Classifier classifier) {classifier.trainAndEvaluate(new Bucket(TRAINING_RECORDS), 0.8, new Random(42));}
}
