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

import org.simmetrics.StringMetric;
import org.simmetrics.metrics.DiceSimilarity;
import org.simmetrics.metrics.JaccardSimilarity;
import org.simmetrics.metrics.JaroWinkler;
import org.simmetrics.metrics.Levenshtein;

import java.util.function.Supplier;

/**
 * The metrics by which to measure similarity between a pair of strings.
 *
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public enum StringSimilaritySupplier implements Supplier<SimilarityMetric> {

    /** @see JaccardSimilarity **/
    JACCARD(() -> new StringSimilarityMetricWrapper(new JaccardSimilarity<>())),

    /** @see Levenshtein **/
    LEVENSHTEIN(() -> new StringSimilarityMetricWrapper(new Levenshtein())),

    /** @see JaroWinkler **/
    JARO_WINKLER(() -> new StringSimilarityMetricWrapper(new JaroWinkler())),

    /** @see DiceSimilarity **/
    DICE(() -> new StringSimilarityMetricWrapper(new DiceSimilarity<>()));

    private Supplier<SimilarityMetric> supplier;

    StringSimilaritySupplier(Supplier<SimilarityMetric> supplier) {

        this.supplier = supplier;
    }

    public SimilarityMetric get() {

        return supplier.get();
    }
}
