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
 * Confusion matrix using exact matching on codes.
 *
 * @author Fraser Dunlop
 * @author Graham Kirby
 */
public class StrictConfusionMatrix extends ConfusionMatrix {

    private static final long serialVersionUID = 983226994385506070L;

    public StrictConfusionMatrix(final Bucket classified_records, final Bucket gold_standard_records) {

        super(classified_records, gold_standard_records);
    }

    public StrictConfusionMatrix(final Bucket classified_records, final Bucket gold_standard_records, Checker checker) {

        super(classified_records, gold_standard_records, checker);
    }

    public StrictConfusionMatrix(DataSet classified_records, DataSet gold_standard_records) {

        super(classified_records, gold_standard_records);
        initMultipleClassification();
    }

    @Override
    protected boolean classificationsMatch(String asserted_code, String real_code) {

        return asserted_code.equals(real_code);
    }

    @Override
    public String toString() {

        return getClass().getSimpleName();
    }
}
