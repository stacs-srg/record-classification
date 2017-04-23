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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.ensemble;

import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.string_similarity.StringSimilarityClassifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class StringSimilarityGroupWithSharedState implements Serializable {

    private static final long serialVersionUID = 2905598266409106509L;
    private List<StringSimilarityClassifier> classifiers;

    /**
     * Required for JSON deserialization.
     */
    public StringSimilarityGroupWithSharedState() {
    }

    public StringSimilarityGroupWithSharedState(List<StringSimilarityClassifier> classifiers) {

        this.classifiers = classifiers;
    }

    public List<StringSimilarityClassifier> getClassifiers() {

        return classifiers;
    }

    public void trainAll(Bucket bucket) {

        final StringSimilarityClassifier first_classifier = classifiers.get(0);
        first_classifier.trainModel(bucket);

        setOtherClassifierStatesToFirst();
    }

    // TODO check whether needed.
    public void recoverFromSerialization() {

        setOtherClassifierStatesToFirst();
    }

    public void prepareForSerialization() {

        deleteOtherClassifierStates();
    }

    private void setOtherClassifierStatesToFirst() {

        Map<String, Classification> state_of_first_classifier = classifiers.get(0).readState();

        for (int i = 1; i < classifiers.size(); i++) {

            classifiers.get(i).writeState(state_of_first_classifier);
        }
    }

    private void deleteOtherClassifierStates() {

        for (int i = 1; i < classifiers.size(); i++) {

            classifiers.get(i).writeState(null);
        }
    }
}
