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

import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.util.*;
import java.util.logging.*;

/**
 * Checks whether any records are present that are unclassified.
 * A record is considered to be unclassified if its {@link Record#getClassification() classification} is {@code null} or is {@link Classification#isUnclassified() unclassified} as defined by the {@link Classification} class.
 * The check for unclassified records is performed in parallel.
 *
 * @author Masih Hajiarab Derkani
 */
public class UnclassifiedChecker implements Checker {

    private static final long serialVersionUID = 2465719719128993717L;
    private static final Logger LOGGER = Logger.getLogger(UnclassifiedChecker.class.getName());

    @Override
    public boolean test(final List<Bucket> buckets) {

        return buckets.parallelStream().anyMatch(this::test);
    }

    public boolean test(final Bucket bucket) {

        return bucket.parallelStream().anyMatch(this::isUnclassified);
    }

    protected boolean isUnclassified(final Record record) {

        final Classification classification = record.getClassification();
        boolean unclassified = classification == null || classification.isUnclassified();
        if (unclassified) {
            LOGGER.info(() -> "Unclassified record detected: " + record);
        }
        return unclassified;
    }
}
