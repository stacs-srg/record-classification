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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.SingleClassifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.TokenList;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Graham Kirby
 */
@RunWith(Parameterized.class)
public class VotingResolutionStrategyTest {

    private static final double DELTA = 0.001;

    List<Classification> alternative_classifications;
    Classification expected_classification;
    String test_description;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> generateData() {

        return Arrays.asList(single(), twoWithDifferentConfidence(), unclassified1(), unclassified2(), multipleIdenticalClassifications(), three(), five());
    }

    public VotingResolutionStrategyTest(String test_description,
                                        List<Classification> alternative_classifications,
                                        Classification expected_classification) {

        this.test_description = test_description;
        this.alternative_classifications = alternative_classifications;
        this.expected_classification = expected_classification;
    }

    @Test
    public void checkResults() {

        final Map<SingleClassifier, Classification> candidate_classifications = makeClassificationMap(alternative_classifications);

        final Classification actual_classification = new VotingResolutionStrategy().resolve(candidate_classifications);
        assertEquals(expected_classification.getCode(), actual_classification.getCode());
        assertEquals(expected_classification.getConfidence(), actual_classification.getConfidence(), DELTA);
    }

    private static Object[] single() {

        Classification classification_1 = new Classification("abc", new TokenList("abc"), 0.5, null);

        //noinspection ArraysAsListWithZeroOrOneArgument
        return new Object[]{"single", Arrays.asList(classification_1), classification_1};
    }

    private static Object[] twoWithDifferentConfidence() {

        Classification classification_1 = new Classification("abc", new TokenList("abc"), 0.5, null);
        Classification classification_2 = new Classification("def", new TokenList("def"), 0.6, null);

        return new Object[]{"twoWithDifferentConfidence", Arrays.asList(classification_1, classification_2), classification_2};
    }

    private static Object[] unclassified1() {

        Classification classification_1 = new Classification("abc", new TokenList("abc"), 0.0, null);

        return new Object[]{"unclassified1", Arrays.asList(Classification.UNCLASSIFIED, classification_1), classification_1};
    }

    private static Object[] unclassified2() {

        Classification classification_1 = new Classification("abc", new TokenList("abc"), 0.0, null);

        return new Object[]{"unclassified2", Arrays.asList(classification_1, Classification.UNCLASSIFIED), classification_1};
    }

    private static Object[] multipleIdenticalClassifications() {

        Classification classification_1 = new Classification("abc", new TokenList("abc"), 0.6, null);
        Classification classification_2 = new Classification("def", new TokenList("def"), 0.0, null);
        Classification classification_3 = new Classification("def", new TokenList("def"), 0.0, null);

        return new Object[]{"multipleIdenticalClassifications", Arrays.asList(classification_1, classification_2, classification_3), classification_2};
    }

    private static Object[] three() {

        Classification classification_1 = new Classification("abc", new TokenList("abc"), 0.5, null);
        Classification classification_2 = new Classification("def", new TokenList("def"), 0.2, null);
        Classification classification_3 = new Classification("def", new TokenList("def"), 0.1, null);

        Classification expected = new Classification("def", new TokenList("def"), 0.15, null);

        return new Object[]{"three", Arrays.asList(classification_1, classification_2, classification_3), expected};
    }

    private static Object[] five() {

        Classification classification_1 = new Classification("abc", new TokenList("abc"), 0.5, null);
        Classification classification_2 = new Classification("def", new TokenList("def"), 0.4, null);
        Classification classification_3 = new Classification("def", new TokenList("def"), 0.1, null);
        Classification classification_4 = new Classification("ghi", new TokenList("ghi"), 0.3, null);
        Classification classification_5 = new Classification("ghi", new TokenList("ghi"), 0.3, null);

        Classification expected = new Classification("ghi", new TokenList("ghi"), 0.3, null);

        return new Object[]{"five", Arrays.asList(classification_1, classification_2, classification_3, classification_4, classification_5), expected};
    }

    private Map<SingleClassifier, Classification> makeClassificationMap(List<Classification> classifications) {

        Map<SingleClassifier, Classification> result = new HashMap<>();
        for (Classification classification : classifications) {
            result.put(makeDummyClassifier(), classification);
        }
        return result;
    }

    private SingleClassifier makeDummyClassifier() {

        return new SingleClassifier() {

            private static final long serialVersionUID = 5056870270840071301L;

            @Override
            public String getName() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void clearModel() {
            }

            @Override
            public void trainModel(Bucket bucket) {
            }

            @Override
            protected Classification doClassify(String data) {
                return null;
            }
        };
    }
}

