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

import java.util.*;
import java.util.function.*;

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
    COMBINED(() -> PUNCTUATION.get().andThen(LOWER_CASE.get()).andThen(ENGLISH_STOP_WORDS.get()).andThen(PORTER_STEM.get()).andThen(CONSISTENT_CLASSIFICATION_CLEANER_CORRECT.get()).andThen(TRIM_CLASSIFICATION_CODE.get()));

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
