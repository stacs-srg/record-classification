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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier;

import org.apache.lucene.search.spell.*;
import org.simmetrics.metrics.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.string_similarity.*;

import java.util.function.*;

/**
 * Predefined metrics by which to measure distance between a pair of strings.
 *
 * @author Masih Hajiarab Derkani
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public enum StringDistanceSupplier implements Supplier<StringDistance> {

    /** @see NGramDistance(int) **/
    N_GRAMS_2(() -> new NGramDistance(2)),

    /** @see NGramDistance(int) **/
    N_GRAMS_3(() -> new NGramDistance(3)),

    /** @see NGramDistance(int) **/
    N_GRAMS_4(() -> new NGramDistance(4)),

    /** @see NGramDistance(int) **/
    N_GRAMS_5(() -> new NGramDistance(5)),

    /** @see NGramDistance(int) **/
    N_GRAMS_6(() -> new NGramDistance(6)),

    /** @see NGramDistance(int) **/
    N_GRAMS_7(() -> new NGramDistance(7)),

    /** @see LevensteinDistance **/
    LEVENSTEIN(LevensteinDistance::new),

    /** @see LuceneLevenshteinDistance **/
    DAMERAU_LEVENSHTEIN(LuceneLevenshteinDistance::new),

    /** @see JaroWinklerDistance **/
    JARO_WINKLER(JaroWinklerDistance::new);

    private Supplier<StringDistance> supplier;

    StringDistanceSupplier(Supplier<StringDistance> supplier) {

        this.supplier = supplier;
    }

    public StringDistance get() {

        return supplier.get();
    }
}
