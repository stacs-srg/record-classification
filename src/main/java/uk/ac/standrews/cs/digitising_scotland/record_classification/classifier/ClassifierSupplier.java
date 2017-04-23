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

import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.ensemble.StringSimilarityGroupWithSharedState;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.ensemble.EnsembleVotingClassifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.exact_match.ExactMatchClassifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.logistic_regression.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.naive_bayes.NaiveBayesClassifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.string_similarity.StringSimilarityClassifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.string_similarity.StringSimilaritySupplier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import static uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.string_similarity.StringSimilaritySupplier.*;

/**
 * Classifiers that are accessible via the command-line interface.
 *
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
public enum ClassifierSupplier implements Supplier<Classifier> {

    EXACT_MATCH(ExactMatchClassifier::new),

    STRING_SIMILARITY_LEVENSHTEIN(ClassifierSupplier::makeLevenshteinClassifier),
    STRING_SIMILARITY_JARO_WINKLER(ClassifierSupplier::makeJaroWinklerClassifier),
    STRING_SIMILARITY_JACCARD(ClassifierSupplier::makeJaccardClassifier),
    STRING_SIMILARITY_DICE(ClassifierSupplier::makeDiceClassifier),

    OLR(OLRClassifier::new),
    NAIVE_BAYES(NaiveBayesClassifier::new),

    VOTING_ENSEMBLE_EXACT_ML_SIMILARITY(ClassifierSupplier::makeVotingEnsembleClassifierWithMLAndStringSimilarity),
    VOTING_ENSEMBLE_EXACT_SIMILARITY(ClassifierSupplier::makeVotingEnsembleClassifierWithStringSimilarity);

    private Supplier<Classifier> supplier;

    ClassifierSupplier(Supplier<Classifier> supplier) {

        this.supplier = supplier;
    }

    public Classifier get() {

        return supplier.get();
    }

    public boolean isExactMatch() {

        return this == EXACT_MATCH;
    }

    public boolean isEnsemble() {

        return this == VOTING_ENSEMBLE_EXACT_ML_SIMILARITY || this == VOTING_ENSEMBLE_EXACT_SIMILARITY;
    }

    private static StringSimilarityClassifier makeDiceClassifier() {

        return new StringSimilarityClassifier(DICE.get());
    }

    private static StringSimilarityClassifier makeJaccardClassifier() {

        return new StringSimilarityClassifier(JACCARD.get());
    }

    private static StringSimilarityClassifier makeJaroWinklerClassifier() {

        return new StringSimilarityClassifier(JARO_WINKLER.get());
    }

    private static StringSimilarityClassifier makeLevenshteinClassifier() {

        return new StringSimilarityClassifier(LEVENSHTEIN.get());
    }

    private static EnsembleVotingClassifier makeVotingEnsembleClassifierWithMLAndStringSimilarity() {

        return new EnsembleVotingClassifier(Arrays.asList((SingleClassifier) EXACT_MATCH.get(), (SingleClassifier) OLR.get(), (SingleClassifier) NAIVE_BAYES.get()),
                                            new StringSimilarityGroupWithSharedState(Arrays.asList(makeDiceClassifier(), makeJaccardClassifier(), makeJaroWinklerClassifier(), makeLevenshteinClassifier())));
    }

    private static EnsembleVotingClassifier makeVotingEnsembleClassifierWithStringSimilarity() {

        return new EnsembleVotingClassifier(Collections.singletonList((SingleClassifier) EXACT_MATCH.get()), new StringSimilarityGroupWithSharedState(Arrays.asList(makeDiceClassifier(), makeJaccardClassifier(), makeJaroWinklerClassifier(), makeLevenshteinClassifier())));
    }
}
