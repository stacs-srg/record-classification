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

import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

/**
 * Checks a list of {@link Bucket bucket}s.
 * This operates on a list because some checking may need to consider multiple buckets, e.g. checking that all
 * training and evaluation records use consistent classification.
 *
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
public interface Checker extends Predicate<List<Bucket>>, Serializable {

    default boolean test(Bucket first, final Bucket... rest) {

        final List<Bucket> buckets = new ArrayList<>();
        buckets.add(first);
        if (rest != null) {
            Collections.addAll(buckets, rest);
        }

        return test(buckets);
    }
}
