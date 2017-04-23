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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.multiple_classifier;

import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.Classifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.TextCleaner;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.TokenList;
import uk.ac.standrews.cs.digitising_scotland.record_classification.util.CombinationGenerator;
import uk.ac.standrews.cs.digitising_scotland.record_classification.util.Combinations;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * Classifies a given string value to a list of {@link Classification classifications} sorted by their fitness in ascending order (i.e. the last classification in the list is the fittest).
 *
 * @author Masih Hajiarab Derkani
 */
public class MultipleClassifier {

    public static final CombinationGenerator<String> ALL_COMBINATION_GENERATOR = Combinations.allGenerator();
    public static final BiPredicate<Classification, Classification> NOT_EQUAL_ONE_ANOTHER = (one, another) -> !one.equals(another);

    private static final List<Classification> UNCLASSIFIED_CLASSIFICATION_LIST = Collections.singletonList(Classification.UNCLASSIFIED);
    private static final CandidateClassificationListFitnessComparator CANDIDATE_CLASSIFICATION_LIST_FITNESS_COMPARATOR = new CandidateClassificationListFitnessComparator();
    private static final CharSequence TOKEN_JOIN_DELIMITER = " ";
    private static final TextCleaner AS_IS = data -> data;

    private final Classifier core_classifier;
    private final double classification_confidence_threshold;
    private final TextCleaner pre_classification_data_cleaner;
    private final BiPredicate<Classification, Classification> distinct_classification_checker;
    private final CombinationGenerator<String> token_combination_generator;

    public MultipleClassifier(Classifier core_classifier, double classification_confidence_threshold) {

        this(core_classifier, classification_confidence_threshold, AS_IS, NOT_EQUAL_ONE_ANOTHER, ALL_COMBINATION_GENERATOR);
    }

    public MultipleClassifier(Classifier core_classifier, double classification_confidence_threshold, TextCleaner pre_classification_data_cleaner) {

        this(core_classifier, classification_confidence_threshold, pre_classification_data_cleaner, NOT_EQUAL_ONE_ANOTHER, ALL_COMBINATION_GENERATOR);
    }

    public MultipleClassifier(Classifier core_classifier, double classification_confidence_threshold, TextCleaner pre_classification_data_cleaner, BiPredicate<Classification, Classification> distinct_classification_checker, CombinationGenerator<String> token_combination_generator) {

        if (classification_confidence_threshold < 0 || classification_confidence_threshold > 1) {
            throw new IllegalArgumentException("confidence threshold must be within inclusive range of 0.0 to 1.0");
        }

        this.core_classifier = core_classifier;
        this.classification_confidence_threshold = classification_confidence_threshold;
        this.pre_classification_data_cleaner = pre_classification_data_cleaner;
        this.distinct_classification_checker = distinct_classification_checker;
        this.token_combination_generator = token_combination_generator;
    }

    public List<Classification> classify(String data) {

        final String cleaned_data = cleanData(data);
        final TokenList cleaned_data_tokens = new TokenList(cleaned_data);
        final List<CandidateClassification> candidate_classifications = token_combination_generator.apply(cleaned_data_tokens).map(this::classify).filter(this::isAcceptable).collect(Collectors.toList());
        final Optional<List<CandidateClassification>> fittest_combination = Combinations.powerSetStream(candidate_classifications).filter(this::isValidCombination).max(CANDIDATE_CLASSIFICATION_LIST_FITNESS_COMPARATOR);
        return fittest_combination.isPresent() ? toClassificationList(fittest_combination.get()) : UNCLASSIFIED_CLASSIFICATION_LIST;
    }

    private String cleanData(final String data) {return pre_classification_data_cleaner.cleanData(data);}

    private CandidateClassification classify(final List<String> token_combination) {

        final String data_element = String.join(TOKEN_JOIN_DELIMITER, token_combination);
        final Classification classification = core_classifier.classify(data_element);
        return new CandidateClassification(token_combination, classification);
    }

    private boolean isAcceptable(final CandidateClassification candidate) {

        final Classification classification = candidate.getClassification();
        return !classification.isUnclassified() && satisfiesConfidenceThreshold(classification);
    }

    private boolean satisfiesConfidenceThreshold(final Classification classification) {return classification.getConfidence() >= classification_confidence_threshold;}

    private List<Classification> toClassificationList(final List<CandidateClassification> candidate_classifications) {

        return candidate_classifications.stream().sorted().map(CandidateClassification::getClassification).collect(Collectors.toList());
    }

    private boolean isValidCombination(List<CandidateClassification> combination) {

        final int combination_size = combination.size();

        // We do not need to loop n^2 times, since isCombinableWith is a symmetric operation.
        for (int outer_index = 0; outer_index < combination_size; outer_index++) {

            final CandidateClassification one = combination.get(outer_index);
            for (int inner_index = outer_index + 1; inner_index < combination_size; inner_index++) {

                final CandidateClassification another = combination.get(inner_index);
                if (!one.isCombinableWith(another)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static class CandidateClassificationListFitnessComparator implements Comparator<List<CandidateClassification>>, Serializable {

        private static final long serialVersionUID = 4539055505067693762L;

        @Override
        public int compare(final List<CandidateClassification> one, final List<CandidateClassification> another) {

            return Double.compare(collectiveFitness(one), collectiveFitness(another));
        }

        private double collectiveFitness(final List<CandidateClassification> candidate_combination) {

            return candidate_combination.stream().mapToDouble(CandidateClassification::fitness).sum();
        }
    }

    private class CandidateClassification implements Comparable<CandidateClassification> {

        private final int hashcode;
        private final double fitness;
        private List<String> tokens;
        private Classification classification;

        CandidateClassification(final List<String> tokens, final Classification classification) {

            this.tokens = tokens;
            this.classification = classification;
            hashcode = Objects.hash(tokens, classification);
            fitness = tokens.size() * classification.getConfidence();
        }

        /**
         * Checks whether this candidate can be combined with the other candidate.
         * This operation is symmetric, i.e. {@code a.isCombinableWith(b) == b.isCombinableWith(a)}.
         *
         * @param other the other candidate
         * @return whether this candidate can occur in combination with the other candidate.
         */
        boolean isCombinableWith(CandidateClassification other) {

            return isTokensDisjoint(other) && isClassificationDistinct(other.classification);
        }

        private boolean isClassificationDistinct(final Classification other) {

            return distinct_classification_checker.test(classification, other);
        }

        private boolean isTokensDisjoint(final CandidateClassification other) {return Collections.disjoint(tokens, other.tokens);}

        /**
         * Gets the goodness measure of this candidate.
         * The higher the fitness value the fitter candidate.
         *
         * @return the goodness measure of this candidate, where higher is better.
         */
        private double fitness() {

            return fitness;
        }

        private Classification getClassification() {

            return classification;
        }

        @Override
        public int hashCode() {

            return hashcode;
        }

        @Override
        public boolean equals(final Object other) {

            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            final CandidateClassification that = (CandidateClassification) other;
            return Objects.equals(tokens, that.tokens) && Objects.equals(classification, that.classification);
        }

        @Override
        public int compareTo(final CandidateClassification other) {

            return Double.compare(fitness(), other.fitness());
        }
    }
}
