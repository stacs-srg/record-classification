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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.string_similarity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class StringSimilarityMetricWrapperTest {

    private static final double DELTA = 0.001;

    private static String s1 = "good night";
    private static String s2 = "gute nacht";
    private static String s3 = "fish";

    private static Set<String> s1_bigrams = makeSet("go", "oo", "od", "ni", "ig", "gh", "ht");
    private static Set<String> s2_bigrams = makeSet("gu", "ut", "te", "na", "ac", "ch", "ht");
    private static Set<String> s3_bigrams = makeSet("fi", "is", "sh");

    private static Collection<String> union_s1_s2 = Arrays.asList("go", "oo", "od", "ni", "ig", "gh", "ht", "gu", "ut", "te", "na", "ac", "ch");
    private static Collection<String> intersection_s1_s2 = Arrays.asList("ht");

    SimilarityMetric metric;
    double expected_similarity_for_identical_strings;
    double expected_similarity_for_similar_strings;
    double expected_similarity_for_dissimilar_strings;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> generateData() {

        List<Object[]> parameters = new ArrayList<>();

        // See https://en.wikipedia.org/wiki/Sørensen–Dice_coefficient
        parameters.add(new Object[]{StringSimilaritySupplier.DICE.get(), 1.0, 2.0 * intersection_s1_s2.size() / (s1_bigrams.size() + s2_bigrams.size()), 0.0});

        // See https://en.wikipedia.org/wiki/Jaccard_index
        parameters.add(new Object[]{StringSimilaritySupplier.JACCARD.get(), 1.0, (double) intersection_s1_s2.size() / union_s1_s2.size(), 0.0});

        // See https://en.wikipedia.org/wiki/Jaro–Winkler_distance
        parameters.add(new Object[]{StringSimilaritySupplier.JARO_WINKLER.get(), 1.0, 0.7, 0.0});

        // See https://en.wikipedia.org/wiki/Levenshtein_distance
        parameters.add(new Object[]{StringSimilaritySupplier.LEVENSHTEIN.get(), 1.0, 0.5, 0.2});

        return parameters;
    }

    public StringSimilarityMetricWrapperTest(SimilarityMetric metric, double expected_similarity_for_identical_strings, double expected_similarity_for_similar_strings, double expected_similarity_for_dissimilar_strings) {

        this.metric = metric;
        this.expected_similarity_for_identical_strings = expected_similarity_for_identical_strings;
        this.expected_similarity_for_similar_strings = expected_similarity_for_similar_strings;
        this.expected_similarity_for_dissimilar_strings = expected_similarity_for_dissimilar_strings;
    }

    @Test
    public void checkBigrams() {

        assertEquals(s1_bigrams, SetMetricAdapter.getBigrams(s1));
        assertEquals(s2_bigrams, SetMetricAdapter.getBigrams(s2));
        assertEquals(s3_bigrams, SetMetricAdapter.getBigrams(s3));
    }

    @Test
    public void checkSimilarityOfIdenticalStrings() {

        assertEquals(expected_similarity_for_identical_strings, metric.getSimilarity(s1, s1), DELTA);
        assertEquals(expected_similarity_for_identical_strings, metric.getSimilarity(s2, s2), DELTA);
        assertEquals(expected_similarity_for_identical_strings, metric.getSimilarity(s3, s3), DELTA);
    }

    @Test
    public void checkSimilarityOfDissimilarStrings() {

        assertEquals(expected_similarity_for_dissimilar_strings, metric.getSimilarity(s1, s3), DELTA);
    }

    @Test
    public void checkSimilarityOfSimilarStrings() {

        assertEquals(expected_similarity_for_similar_strings, metric.getSimilarity(s1, s2), DELTA);
        assertEquals(expected_similarity_for_similar_strings, metric.getSimilarity(s2, s1), DELTA);
    }

    private static Set<String> makeSet(String... strings) {

        return new HashSet<>(Arrays.asList(strings));
    }
}
