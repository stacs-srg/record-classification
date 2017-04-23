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
package uk.ac.standrews.cs.digitising_scotland.record_classification.util;

import org.apache.commons.collections4.iterators.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author masih
 */
public final class Combinations {

    private Combinations() {

        throw new UnsupportedOperationException();
    }

    public static <T> List<List<T>> permutations(Collection<T> input) {

        return permutations(new ArrayList<>(input), 0);
    }

    private static <T> List<List<T>> permutations(List<T> input, int start) {

        List<List<T>> permutations = new ArrayList<>();

        if (start == input.size()) {
            permutations.add(input);
        }
        else {
            for (int i = start; i < input.size(); i++) {
                swap(input, start, i);
                permutations.addAll(permutations(input, start + 1));
                swap(input, start, i);
            }
        }

        return permutations;
    }

    private static <T> void swap(final List<T> a, final int start, final int i) {

        final T temp = a.get(start);
        a.set(start, a.get(i));
        a.set(i, temp);
    }

    public static <T> List<List<T>> powerset(Collection<T> input) {

        List<List<T>> powerset = new ArrayList<>();
        powerset.add(new ArrayList<>());

        for (T item : input) {
            final List<List<T>> new_combination = new ArrayList<>();

            for (List<T> subset : powerset) {
                new_combination.add(subset);
                final List<T> new_sub_combination = new ArrayList<T>(subset);
                new_sub_combination.add(item);
                new_combination.add(new_sub_combination);
            }

            powerset = new_combination;
        }

        return powerset;
    }

    public static <T> Stream<List<T>> powerSetStream(Collection<T> input) {

        final Iterable<List<T>> lists = new PowerSet<>(input);
        return StreamSupport.stream(lists.spliterator(), false);
    }

    public static <T> CombinationGenerator<T> powerSetGenerator() {

        return Combinations::powerSetStream;
    }

    public static <T> Stream<List<T>> permutationsStream(Collection<T> input) {

        final PermutationIterator<T> permuatations = new PermutationIterator<>(input);
        return toStream(permuatations);
    }

    public static <T> CombinationGenerator<T> permutationsGenerator() {

        return Combinations::permutationsStream;
    }

    public static <T> List<List<T>> all(Collection<T> input) {

        return allStream(input).collect(Collectors.toList());
    }

    public static <T> Stream<List<T>> allStream(Collection<T> input) {

        final Iterator<List<T>> powerset = new PowerSet<>(input);

        final Iterator<List<T>> all = new Iterator<List<T>>() {

            PermutationIterator<T> permutations;

            @Override
            public boolean hasNext() {

                return permutationsHasNext() || powerset.hasNext();
            }

            private boolean permutationsHasNext() {return permutations != null && permutations.hasNext();}

            @Override
            public List<T> next() {

                final List<T> next;

                if (permutationsHasNext()) {
                    next = permutations.next();
                }
                else {

                    final List<T> powerset_next = powerset.next();
                    if (powerset_next.size() > 1) {
                        permutations = new PermutationIterator<>(powerset_next);
                        next = permutations.next();
                    }
                    else {
                        next = powerset_next;
                    }
                }

                return next;
            }
        };

        final Iterable<List<T>> iterable = () -> all;
        return StreamSupport.stream(iterable.spliterator(), false);

    }

    public static <T> CombinationGenerator<T> allGenerator() {

        return Combinations::allStream;
    }

    public static <T> CombinationGenerator<T> generatorWithTruncatedInput(int max_input_length, CombinationGenerator<T> core_generator) {

        return input -> core_generator.apply(input.size() > max_input_length ? subCollection(0, max_input_length, input) : input);
    }

    private static <T> Collection<T> subCollection(final int from, final int to, final Collection<T> input) {

        return new ArrayList<>(input).subList(from, to);
    }

    public static <T> CombinationGenerator<T> concatenateGenerators(CombinationGenerator<T> first, CombinationGenerator<T> second) {

        return input -> Stream.concat(first.apply(input), second.apply(input));
    }

    static <T> Stream<T> toStream(Iterator<T> iterator) {

        Iterable<T> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
