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
package uk.ac.standrews.cs.digitising_scotland.record_classification.data_cleaning;

import com.google.common.collect.Multiset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.Pair;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.bucket.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.code.CodeNotValidException;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.records.Record;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.InputFormatException;

import java.io.IOException;
import java.util.*;

/**
 * Reads a {@link Bucket} and performs data cleaning such as spelling correction and feature selection on the descriptions in each {@link Record}.
 * OriginalData.description is not changed, instead the cleanedDescripion field is populated.
 *
 * @author jkc25, frjd2
 */
public class LevenshteinCleaner extends AbstractDataCleaner {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LevenshteinCleaner.class);

    /**
     * The Constant SIMILARITY.
     */
    private double similarity = 0.85;

    /** The Constant METRIC. */
    private static final AbstractStringMetric METRIC = new Levenshtein();

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InputFormatException the input format exception
     * @throws CodeNotValidException 
     */
    public static void main(final String... args) throws IOException, InputFormatException, CodeNotValidException {

        LevenshteinCleaner cleaner = new LevenshteinCleaner();
        cleaner.setSimilarity(args);
        cleaner.runOnFile(args);
    }

    /**
     * Sets the similarity.
     *
     * @param args the new similarity
     */
    private void setSimilarity(final String... args) {

        try {
            similarity = Double.parseDouble(args[3]);
            LOGGER.info("SIMILARITY set to " + similarity);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            LOGGER.info("No SIMILARITY argument. Default is " + similarity);
        }
    }

    /**
     * Corrects a token to the most similar higher occurrence term.
     *
     * @param token the token to correct
     * @return the highest similarity match
     */
    @Override
    public String correct(final String token) {

        List<Pair<String, Float>> possibleMatches = getPossibleMatches(token, getWordMultiset());
        sortPossibleMatches(possibleMatches);
        String bestMatch = getBestMatch(token, possibleMatches);
        printDebugInfo(token, bestMatch);
        return bestMatch;
    }

    /**
     * Prints the debug info.
     *
     * @param token the token
     * @param bestMatch the best match
     */
    private void printDebugInfo(final String token, final String bestMatch) {

        if (!token.equals(bestMatch)) {
            LOGGER.info("Original token: " + token + " Corrected token: " + bestMatch);
        }
    }

    /**
     * Gets the possible matches.
     *
     * @param token  the token
     * @param wordMultiset the word multiset
     * @return the possible matches
     */
    //TODO test
    private List<Pair<String, Float>> getPossibleMatches(final String token, final Multiset<String> wordMultiset) {

        List<Pair<String, Float>> possibleMatches = new ArrayList<>();
        Set<String> allWords = wordMultiset.elementSet();
        for (String string : allWords) {
            if (wordMultiset.count(string) > wordMultiset.count(token)) {
                float result = METRIC.getSimilarity(string, token);
                if (result > similarity) {
                    possibleMatches.add(new Pair<>(string, result));
                }
            }
        }
        return possibleMatches;
    }

    /**
     * Gets the best match.
     *
     * @param token           the token
     * @param possibleMatches the possible matches
     * @return the best match
     */
    //TODO test
    private static String getBestMatch(final String token, final List<Pair<String, Float>> possibleMatches) {

        String bestMatch;
        if (!possibleMatches.isEmpty()) {
            Pair<String, Float> stringFloatPair = possibleMatches.get(possibleMatches.size() - 1);
            bestMatch = stringFloatPair.getLeft();
        }
        else {
            bestMatch = token;
        }
        return bestMatch;
    }

    /**
     * Sort possible matches.
     *
     * @param possibleMatches the possible matches
     */
    //TODO test
    private static void sortPossibleMatches(final List<Pair<String, Float>> possibleMatches) {

        Comparator<Pair<String, Float>> c = new Comparator<Pair<String, Float>>() {

            @Override
            public int compare(final Pair<String, Float> o1, final Pair<String, Float> o2) {

                return o1.getRight() < o2.getRight() ? -1 : o1.getRight() > o2.getRight() ? 1 : 0;
            }
        };

        Collections.sort(possibleMatches, c);
    }
}