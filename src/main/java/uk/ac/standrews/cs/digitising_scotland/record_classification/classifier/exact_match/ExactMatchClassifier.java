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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.exact_match;

import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.SingleClassifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;

import java.util.HashMap;
import java.util.Objects;
import java.util.logging.*;

public class ExactMatchClassifier extends SingleClassifier {

    /** The description of this classifier **/
    public static final String DESCRIPTION = "Classifies based on exact match with training data";
    
    private static final long serialVersionUID = 7439350806549465200L;
    private static final Logger LOGGER = Logger.getLogger(ExactMatchClassifier.class.getName());

    private HashMap<String, Classification> known_classifications;

    public ExactMatchClassifier() {

        clearModel();
    }

    @Override
    public void clearModel() {

        known_classifications = new HashMap<>();
    }

    @Override
    public void trainModel(final Bucket training_records) {

        final int training_records_size = training_records.size();
        resetTrainingProgressIndicator(training_records_size);

        for (Record record : training_records) {
            loadRecord(record);
            progressTrainingStep();
        }
    }

    @Override
    public Classification doClassify(final String data) {

        final Classification exact_classification = known_classifications.get(data);
        return exact_classification != null ? exact_classification : Classification.UNCLASSIFIED;
    }

    @Override
    public String getName() {

        return getClass().getSimpleName();
    }

    @Override
    public String getDescription() {

        return DESCRIPTION;
    }

    @Override
    public boolean equals(final Object o) {

        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final ExactMatchClassifier that = (ExactMatchClassifier) o;
        return Objects.equals(known_classifications, that.known_classifications);
    }

    @Override
    public int hashCode() {

        return Objects.hash(known_classifications);
    }

    @Override
    public String toString() {

        return getName();
    }

    @Override
    protected double getConfidence(final String code) {

        return code.equals(Classification.UNCLASSIFIED.getCode()) ? 0.0 : 1.0;
    }

    private void loadRecord(final Record record) {

        known_classifications.put(record.getData(), record.getClassification());
    }
}
