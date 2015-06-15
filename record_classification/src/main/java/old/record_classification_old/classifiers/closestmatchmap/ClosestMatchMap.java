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
package old.record_classification_old.classifiers.closestmatchmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An extension of the Java Collections map interface with a method
 * for getting the value of the closest matching key according to a
 * similarity metric supplied at construction.
 * Created by fraserdunlop on 01/10/2014 at 15:38.
 */
public class ClosestMatchMap<K, V> implements Map<K, V> {

    private final Similaritor<K> similaritor;
    private Map<K, V> map;

    public ClosestMatchMap(final SimilarityMetric<K> metric, final Map<K, V> map) {

        this.map = map;
        this.similaritor = new Similaritor<>(metric);
    }

    /**
     * WARNING: make sure that the Similaritor supplied at construction
     * returns a Comparator which puts the most similar key at the head
     * of the list otherwise this will return the value associated with the
     * least similar key.
     * @param key the key to fetch.
     * @return the value of the key which matches the supplied key most closely.
     */
    public V getClosestMatch(final K key) {

        if (containsKey(key)) { return get(key); }
        return get(getClosestKey(key));
    }

    /**
     *
     * @param key used to construct comparator to sort keys.
     * @return the key at the head of the sorted list of keys.
     */
    public K getClosestKey(final K key) {

        List<K> keyList = new ArrayList<>(keySet());
        try {
            Collections.sort(keyList, similaritor.getComparator(key));
        }
        catch (IllegalArgumentException iae) {
            System.err.println(keyList);
            System.err.println(key);
            System.err.println(keyList.size());
            throw new IllegalArgumentException();
        }

        return keyList.get(0);
    }

    public double getSimilarity(K o1, K o2) {

        return similaritor.getSimilarity(o1, o2);
    }

    @Override
    public int size() {

        return map.size();
    }

    @Override
    public boolean isEmpty() {

        return map.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {

        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {

        return map.containsValue(value);
    }

    @Override
    public V get(final Object key) {

        return map.get(key);
    }

    @Override
    public V put(final K key, final V value) {

        return map.put(key, value);
    }

    @Override
    public V remove(final Object key) {

        return map.remove(key);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {

        map.putAll(m);
    }

    @Override
    public void clear() {

        map.clear();
    }

    @Override
    public Set<K> keySet() {

        return map.keySet();
    }

    @Override
    public Collection<V> values() {

        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {

        return map.entrySet();
    }
}