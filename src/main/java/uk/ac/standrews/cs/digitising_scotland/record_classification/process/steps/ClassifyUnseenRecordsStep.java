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
package uk.ac.standrews.cs.digitising_scotland.record_classification.process.steps;

import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.ClassificationContext;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.Step;

/**
 * Classifies unseen records and stores the results in a classification process {@link ClassificationContext context}.
 *
 * @author Masih Hajiarab Derkani
 */
public class ClassifyUnseenRecordsStep implements Step {

    private static final long serialVersionUID = 292143932733171808L;

    /** Instantiates a new unseen record classification step. */
    public ClassifyUnseenRecordsStep() { }

    @Override
    public void perform(final ClassificationContext context) {

        final Classifier classifier = context.getClassifier();
        final Bucket classified_unseen_records = classifier.classify(context.getUnseenRecords());

        context.setClassifiedUnseenRecords(classified_unseen_records);
    }
}
