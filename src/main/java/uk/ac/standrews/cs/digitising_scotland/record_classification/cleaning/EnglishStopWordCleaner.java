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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 * Removes english stop words from records. By default this class cleans the case-sensitive set of stop words defined in {@link #DEFAULT_STOP_WORDS}.
 *
 * @author Masih Hajiarab Derkani
 */
public class EnglishStopWordCleaner extends TokenFilterCleaner {

    private static final long serialVersionUID = 3018841867887670242L;

    /** The default set of stop words **/
    public static final CharArraySet DEFAULT_STOP_WORDS = EnglishAnalyzer.getDefaultStopSet();

    private final CharArraySet stop_words;

    /**
     * Constructs a new instance of this class with the case-sensitive {@link #DEFAULT_STOP_WORDS default} set of stop words.
     */
    public EnglishStopWordCleaner() {

        stop_words = DEFAULT_STOP_WORDS;
    }

    /**
     * Constructs a new instance of this class with the {@link #DEFAULT_STOP_WORDS default} set of stop words.
     *
     * @param ignore_case weather the ignore the casing of the stop words.
     */
    public EnglishStopWordCleaner(boolean ignore_case) {

        stop_words = new CharArraySet(DEFAULT_STOP_WORDS, ignore_case);
    }

    /**
     * Constructs a new instance of this class with the specified set of stop words.
     *
     * @param stop_words the stop words to clean
     */
    public EnglishStopWordCleaner(CharArraySet stop_words) {

        this.stop_words = stop_words;
    }

    @Override
    protected TokenFilter getTokenFilter(final TokenStream stream) {

        return new StopFilter(stream, stop_words);
    }
}
