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
import java.util.stream.*;

/**
 * Removes spaces from the begining/end of the classification codes.
 *
 * @author Masih Hajiarab Derkani
 */
public class TrimClassificationCodesCleaner implements Cleaner {

    private static final long serialVersionUID = 2086569954577088946L;

    @Override
    public List<Bucket> apply(final List<Bucket> buckets) {

        return buckets.stream().map(this::apply).collect(Collectors.toList());
    }

    public Bucket apply(Bucket bucket) {

        final Bucket cleaned = new Bucket();
        StreamSupport.stream(bucket.spliterator(), false).map(this::apply).forEach(cleaned::add);
        return cleaned;
    }

    public Record apply(Record record) {

        final Classification cleaned_classification = apply(record.getClassification());
        return new Record(record.getId(), record.getData(), record.getOriginalData(), cleaned_classification);
    }

    public Classification apply(Classification classification) {

        final Classification cleaned_classification;
        if (classification != null && !classification.isUnclassified()) {

            final String trimmed_code = trim(classification.getCode());

            cleaned_classification = new Classification(trimmed_code, classification.getTokenList(), classification.getConfidence(), classification.getDetail());
        }
        else {
            cleaned_classification = classification;
        }

        return cleaned_classification;
    }

    private String trim(String code) {

        return code != null ? code.trim() : null;
    }
}
