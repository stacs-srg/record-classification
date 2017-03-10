/*
 * Copyright 2015 Digitising Scotland project:
 * <http://digitisingscotland.cs.st-andrews.ac.uk/>
 *
 * This file is part of the module util.
 *
 * util is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * util is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with util. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.digitising_scotland.util.MTree;

import org.junit.Before;
import org.junit.Test;
import org.simmetrics.metrics.Levenshtein;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by al on 27/01/2017.
 */
public class MTreeStringEditDistanceTest {

    MTree<String> t;

    @Before
    public void setUp() throws Exception {

        t = new MTree<>(new EditDistance2());
    }

    /**
     * add a single point to the tree
     */
    @Test
    public void add_one() {

        String s = "hello mum";
        t.add(s);
        assertEquals(1, t.size());
        assertTrue(t.contains(s));
    }

    /**
     * add some words and find the closest
     */
    @Test
    public void check_nearest() {

        String[] words = new String[]{"girl", "boy", "fish", "flash", "shed", "crash", "hill", "moon"};

        for (String word : words) {
            t.add(word);
        }
        assertEquals("crash", t.nearestNeighbour("brash").value);
        assertEquals("boy", t.nearestNeighbour("toy").value);
        assertEquals("hill", t.nearestNeighbour("sill").value);
        assertEquals("hill", t.nearestNeighbour("hole").value);
        assertEquals("moon", t.nearestNeighbour("soon").value);
        assertEquals("fish", t.nearestNeighbour("fist").value);
        assertEquals("shed", t.nearestNeighbour("shod").value);
    }


    public class EditDistance2 implements Distance<String> {

        @Override
        public float distance(String s1, String s2) {

            Levenshtein levenshtein = new Levenshtein();

         //   System.out.println("Distance between( " + s1 + "," + s2 + " ) is " + levenshtein.distance(s1, s2));

            return levenshtein.distance(s1, s2);
        }
    }

    public class EditDistance implements Distance<String> {

        public float distance(String s1, String s2) {

         //   System.out.println("Distance between( " + s1 + "," + s2 + " ) is " + Ldistance(s1, s2));

            return (float) Ldistance(s1, s2);
        }

        /**
         * Code from https://github.com/tdebatty/java-string-similarity/blob/master/src/main/java/info/debatty/java/stringsimilarity/Levenshtein.java
         * The Levenshtein distance, or edit distance, between two words is the
         * minimum number of single-character edits (insertions, deletions or
         * substitutions) required to change one word into the other.
         *
         * http://en.wikipedia.org/wiki/Levenshtein_distance
         *
         * It is always at least the difference of the sizes of the two strings.
         * It is at most the length of the longer string.
         * It is zero if and only if the strings are equal.
         * If the strings are the same size, the Hamming distance is an upper bound
         * on the Levenshtein distance.
         * The Levenshtein distance verifies the triangle inequality (the distance
         * between two strings is no greater than the sum Levenshtein distances from
         * a third string).
         *
         * Implementation uses dynamic programming (Wagner–Fischer algorithm), with
         * only 2 rows of data. The space requirement is thus O(m) and the algorithm
         * runs in O(mn).
         *
         * @param s1 The first string to compare.
         * @param s2 The second string to compare.
         * @return The computed Levenshtein distance.
         * @throws NullPointerException if s1 or s2 is null.
         */
        public final double Ldistance(final String s1, final String s2) {
            if (s1 == null) {
                throw new NullPointerException("s1 must not be null");
            }

            if (s2 == null) {
                throw new NullPointerException("s2 must not be null");
            }

            if (s1.equals(s2)) {
                return 0;
            }

            if (s1.length() == 0) {
                return s2.length();
            }

            if (s2.length() == 0) {
                return s1.length();
            }

            // create two work vectors of integer distances
            int[] v0 = new int[s2.length() + 1];
            int[] v1 = new int[s2.length() + 1];
            int[] vtemp;

            // initialize v0 (the previous row of distances)
            // this row is A[0][i]: edit distance for an empty s
            // the distance is just the number of characters to delete from t
            for (int i = 0; i < v0.length; i++) {
                v0[i] = i;
            }

            for (int i = 0; i < s1.length(); i++) {
                // calculate v1 (current row distances) from the previous row v0
                // first element of v1 is A[i+1][0]
                //   edit distance is delete (i+1) chars from s to match empty t
                v1[0] = i + 1;

                // use formula to fill in the rest of the row
                for (int j = 0; j < s2.length(); j++) {
                    int cost = 1;
                    if (s1.charAt(i) == s2.charAt(j)) {
                        cost = 0;
                    }
                    v1[j + 1] = Math.min(
                            v1[j] + 1,              // Cost of insertion
                            Math.min(
                                    v0[j + 1] + 1,  // Cost of remove
                                    v0[j] + cost)); // Cost of substitution
                }

                // copy v1 (current row) to v0 (previous row) for next iteration
                //System.arraycopy(v1, 0, v0, 0, v0.length);

                // Flip references to current and previous row
                vtemp = v0;
                v0 = v1;
                v1 = vtemp;

            }

            return v0[s2.length()];
        }

    }
}