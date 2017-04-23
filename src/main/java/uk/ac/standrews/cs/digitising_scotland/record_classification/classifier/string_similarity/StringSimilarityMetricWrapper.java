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

import org.simmetrics.*;
import org.simmetrics.metrics.*;
import weka.*;

import java.io.Serializable;

/**
 * The metrics by which to measure similarity between a pair of strings.
 *
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
public class StringSimilarityMetricWrapper implements SimilarityMetric {

    private static final long serialVersionUID = -7590079815946529116L;

    // This is transient to help with JSON serialization.
    private transient StringMetric metric;
    private String name;

    /**
     * Needed for JSON deserialization.
     */
    StringSimilarityMetricWrapper() {

    }

    StringSimilarityMetricWrapper(final SetMetric<String> set_metric) {

        this(new SetMetricAdapter(set_metric));
    }

    StringSimilarityMetricWrapper(StringMetric metric) {

        this.metric = metric;
        if (SetMetricAdapter.class.isAssignableFrom(metric.getClass())) {
            name = ((SetMetricAdapter) metric).getSetMetric().getClass().getName();
        }
        else {
            name = metric.getClass().getName();
        }
    }

    /**
     * Calculates the similarity between a given pair of strings.
     *
     * @param one the first string
     * @param other the second string
     * @return the similarity between the inclusive range of {@code 0} (no similarity) and {@code 1} (exact match)
     */
    public float getSimilarity(String one, String other) {

        loadMetricIfNecessary();
        return metric.compare(one, other);
    }

    private void loadMetricIfNecessary() {

        if (metric == null) {
            try {
                final Class<?> metric_type = Class.forName(name);
                Object metric_instance = metric_type.newInstance();

                if (SetMetric.class.isAssignableFrom(metric_type)) {
                    //noinspection unchecked
                    metric_instance = new SetMetricAdapter((SetMetric<String>) metric_instance);
                }

                metric = (StringMetric) metric_instance;
            }
            catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException("failed to load metric", e);
            }
        }
    }

    public String getDescription() {

        loadMetricIfNecessary();
        return metric.toString();
    }

    @Override
    public boolean equals(Object obj) {

        return obj instanceof StringSimilarityMetricWrapper && ((StringSimilarityMetricWrapper) obj).getName().equals(name);
    }

    public String getName() {

        return name;
    }

    public String toString() {

        return getName();
    }
}
