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
package uk.ac.standrews.cs.digitising_scotland.record_classification.analysis;

import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.Checker;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

/**
 * @author Masih Hajiarab Derkani
 */
public class MatchingPrefixConfusionMatrix extends ConfusionMatrix {

    private static final long serialVersionUID = 7477797660952003199L;
    private final int matching_prefix_length;

    public MatchingPrefixConfusionMatrix(int matching_prefix_length, final Bucket classified_records, final Bucket gold_standard_records, Checker checker) {

        super(classified_records, gold_standard_records, checker);
        this.matching_prefix_length = matching_prefix_length;
    }

    public MatchingPrefixConfusionMatrix(int matching_prefix_length, DataSet classified_records, DataSet gold_standard_records) {

        super(classified_records, gold_standard_records);
        this.matching_prefix_length = matching_prefix_length;
        initMultipleClassification();
    }

    @Override
    protected boolean classificationsMatch(String asserted_code, String real_code) {

        final String prefix = real_code.length() > matching_prefix_length ? real_code.substring(0, matching_prefix_length) : real_code;
        return !asserted_code.isEmpty() && !real_code.isEmpty() && asserted_code.startsWith(prefix);
    }

    @Override
    protected String significantPartOfCode(String code) {

        return code.substring(0, matching_prefix_length);
    }

    @Override
    public String toString() {

        return String.format("%s [ matching_prefix_length: %d ]", getClass().getSimpleName(), matching_prefix_length);
    }
}
