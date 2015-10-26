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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning;

import org.apache.lucene.search.spell.*;
import uk.ac.standrews.cs.lexicon.*;
import uk.ac.standrews.cs.lexicon.dictionary.*;
import uk.ac.standrews.cs.lexicon.dictionary.Dictionary;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.Supplier;

/**
 * Predefined enumeration of {@link Cleaner cleaners}.
 *
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
public enum CleanerSupplier implements Supplier<Cleaner> {

    /** Removes punctuation characters. **/
    PUNCTUATION(PunctuationCleaner::new),

    /** Converts text to lower case. **/
    LOWER_CASE(LowerCaseCleaner::new),

    /** Removes English stop words. **/
    ENGLISH_STOP_WORDS(EnglishStopWordCleaner::new),

    /** Performs stemming using Porter algorithm. **/
    PORTER_STEM(PorterStemCleaner::new),

    /** Corrects the classification of any inconsistently classified records to the most popular. **/
    CONSISTENT_CLASSIFICATION_CLEANER_CORRECT(() -> ConsistentClassificationCleaner.CORRECT),

    /** Removes any inconsistently classified records. **/
    CONSISTENT_CLASSIFICATION_CLEANER_REMOVE(() -> ConsistentClassificationCleaner.REMOVE),

    /** Removes white-space characters fom the beginning/end of classification codes. **/
    TRIM_CLASSIFICATION_CODE(TrimClassificationCodesCleaner::new),

    /** Applies all available text cleaners and corrects inconsistent classifications. **/
    COMBINED(() -> new CompositeCleaner(Arrays.asList(PUNCTUATION.get(), LOWER_CASE.get(), ENGLISH_STOP_WORDS.get(), PORTER_STEM.get(), CONSISTENT_CLASSIFICATION_CLEANER_CORRECT.get(), TRIM_CLASSIFICATION_CODE.get())));

    @SuppressWarnings("NonSerializableFieldInSerializableClass")
    private final Supplier<Cleaner> supplier;

    CleanerSupplier(Supplier<Cleaner> supplier) {

        this.supplier = supplier;
    }

    /**
     * Supplies the cleaner.
     *
     * @return the cleaner
     */
    public Cleaner get() {

        return supplier.get();
    }
}
