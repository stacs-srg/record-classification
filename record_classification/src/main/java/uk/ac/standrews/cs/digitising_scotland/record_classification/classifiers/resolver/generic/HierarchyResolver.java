/*
 * Copyright 2014 Digitising Scotland project:
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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifiers.resolver.generic;

import uk.ac.standrews.cs.digitising_scotland.record_classification.classifiers.resolver.Interfaces.AncestorAble;
import java.io.IOException;
import java.util.*;

/**
 * Resolves hierarchies in the keys of a MultiValueMap. Moves ancestor key contents
 * into decendent key lists. Keys must implement Ancestorable<K> interface.
 * Created by fraserdunlop on 06/10/2014 at 10:00.
 */
public class HierarchyResolver<K extends AncestorAble<K>, V> {

    /**
     * Moves ancestor key contents to decendent key lists.
     * @param map MultiValueMap
     * @return new MultiValueMap with hierarchies in keys resolved
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public  MultiValueMap<K, V> moveAncestorsToDescendantKeys(final MultiValueMap<K, V> map) throws IOException, ClassNotFoundException {
        MultiValueMap<K, V> clone = map.deepClone();
        for (K key : map)
            moveAncestorsIntoKey(map, clone, key);
        return clone;
    }

    /**
     * Iterates over keys in map checking if they are ancestors of K decendentKey. If they are
     * then their values are migrated to the List associated with decendentKey (in clone) and
     * the ancestor keys removed (from clone).
     * @param map original MultiValueMap
     * @param clone clone of map which is edited
     * @param decendentKey the decendent key
     */
    private void moveAncestorsIntoKey(MultiValueMap<K, V> map, MultiValueMap<K, V> clone, K decendentKey) {
        for(K ancestor : getAncestors(decendentKey, clone.keySet())) {
                clone.get(decendentKey).addAll(map.get(ancestor));
                clone.remove(ancestor);
        }
    }

    /**
     * Returns the set of ancestors of K k contained in Set<K> keys.
     * @param k the key
     * @param keys the keys
     * @return the ancestors of k.
     */
    private Set<K> getAncestors(final K k, final Set<K> keys) {
        Set<K> ancestors = new HashSet<>();
        for (K key : keys) {
            if (k.isAncestor(key)) { ancestors.add(key); }
        }
        return ancestors;
    }

}
