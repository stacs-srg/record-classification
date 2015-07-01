/*
 * Copyright 2015 Digitising Scotland project:
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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier;

import uk.ac.standrews.cs.digitising_scotland.record_classification.interfaces.Classifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractClassifier implements Classifier {

    private static final long serialVersionUID = -4322016472465051488L;

    public Bucket classify(final Bucket bucket) throws Exception {

        Bucket classified = new Bucket();

        for (Record record : bucket) {

            final String data = record.getData();
            Classification classification = classify(data);

            assert classification != null;

            classified.add(new Record(record.getId(), data, classification));
        }

        return classified;
    }

    public static Set<Classification> makeClassificationSet(Classification classification) {

        Set<Classification> result = new HashSet<>();
        result.add(classification);
        return result;
    }
}
