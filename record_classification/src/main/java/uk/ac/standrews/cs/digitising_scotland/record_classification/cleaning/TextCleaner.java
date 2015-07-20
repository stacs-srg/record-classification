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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning;

import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.TokenList;

/**
 * Cleans a {@link Bucket bucket}.
 *
 * @author Graham Kirby
 */
public interface TextCleaner extends Cleaner {

    String cleanData(final String data);

    default Bucket apply(final Bucket bucket) {

        final Bucket cleaned_bucket = new Bucket();
        for (Record record : bucket) {

            cleaned_bucket.add(cleanRecord(record));
        }
        return cleaned_bucket;
    }

    default Record cleanRecord(final Record record) {

        final int id = record.getId();
        final String data = record.getData();
        final String original_data = record.getOriginalData();
        final Classification classification = record.getClassification();

        final String cleaned_data = cleanData(data);
        final Classification cleaned_classification = cleanClassification(cleaned_data, classification);

        return new Record(id, cleaned_data, original_data, cleaned_classification);
    }

    default Classification cleanClassification(final String cleaned_data, final Classification old_classification) {

        final Classification cleaned_classification;
        if (old_classification.equals(Classification.UNCLASSIFIED)) {
            cleaned_classification = old_classification;
        } else {
            final String code = old_classification.getCode();
            final TokenList tokens = new TokenList(cleaned_data);
            final double confidence = old_classification.getConfidence();

            cleaned_classification = new Classification(code, tokens, confidence);
        }
        return cleaned_classification;
    }
}