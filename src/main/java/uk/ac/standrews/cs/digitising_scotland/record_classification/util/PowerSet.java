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

import java.util.*;

/**
 * Heavily based on {@code http://stackoverflow.com/questions/1670862/obtaining-a-powerset-of-a-set-in-java/3078491#3078491}
 */
public class PowerSet<T> implements Iterator<List<T>>, Iterable<List<T>> {

    private final List<T> input;
    private final BitSet bitset;
    private final int input_size;

    public PowerSet(Collection<T> input) {

        this.input = new ArrayList<>(input);
        input_size = input.size();
        bitset = new BitSet(input_size + 1);
    }

    @Override
    public boolean hasNext() {

        return !bitset.get(input_size);
    }

    @Override
    public List<T> next() {

        final List<T> next = getNext();
        incrementBitSet();
        return next;
    }

    private List<T> getNext() {

        final List<T> next = new ArrayList<>();
        for (int i = 0; i < input_size; i++) {
            if (bitset.get(i)) {
                next.add(input.get(i));
            }
        }
        return next;
    }

    protected void incrementBitSet() {

        for (int i = 0; i < bitset.size(); i++) {
            if (!bitset.get(i)) {
                bitset.set(i);
                break;
            }
            else
                bitset.clear(i);
        }
    }

    @Override
    public void remove() {

        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<List<T>> iterator() {

        return this;
    }
}
