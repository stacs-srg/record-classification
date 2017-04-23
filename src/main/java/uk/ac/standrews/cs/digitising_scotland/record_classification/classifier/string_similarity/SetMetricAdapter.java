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

import org.simmetrics.SetMetric;
import org.simmetrics.StringMetric;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.TokenList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetMetricAdapter implements StringMetric {

    SetMetric<String> set_metric;

    public SetMetricAdapter(SetMetric<String> set_metric) {

        this.set_metric = set_metric;
    }

    public SetMetric<String> getSetMetric() {

        return set_metric;
    }

    @Override
    public float compare(String s1, String s2) {

        Set<String> set1 = getBigrams(s1);
        Set<String> set2 = getBigrams(s2);

        return set_metric.compare(set1, set2);
    }

    public static Set<String> getBigrams(String s) {

        return getBigrams(new TokenList(s));
    }

    private static Set<String> getBigrams(List<String> strings) {

        Set<String> result = new HashSet<>();
        for (String s : strings) {
            result.addAll(getBigramsFromWord(s));
        }
        return result;
    }

    private static Set<String> getBigramsFromWord(String s) {

        Set<String> result = new HashSet<>();
        for (int i = 0; i < s.length() - 1; i++) {
            result.add(s.substring(i, i + 2));
        }
        return result;
    }
}
