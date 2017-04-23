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
 * Checks whether a list buckets are consistently coded.
 *
 * @author Graham Kirby
 */
public class ConsistentCodingChecker implements Checker {

    private static final long serialVersionUID = 3117180381918812824L;
    private static final Logger LOGGER = Logger.getLogger(ConsistentCodingChecker.class.getName());

    /**
     * @param buckets the buckets to check for consistent coding
     * @return {@code true} if the buckets are consistently coded; {@code false} otherwise.
     */
    @Override
    public boolean test(final List<Bucket> buckets) {

        final Map<String, String> classifications_encountered = new HashMap<>();

        for (Bucket bucket : buckets) {

            for (Record record : bucket) {

                final Classification classification = record.getClassification();

                if (classification != null) {
                    final String data = record.getData();
                    final String code = classification.getCode();

                    if (classifications_encountered.containsKey(data)) {
                        final String other_code = classifications_encountered.get(data);
                        if (!code.equals(other_code)) {

                            LOGGER.info(() -> "inconsistently coded record codes[" + code + "," + other_code + "] record: " + record);
                            return false;
                        }
                    }
                    else {
                        classifications_encountered.put(data, code);
                    }
                }
            }
        }
        return true;
    }
}
