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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.composite;


import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.Classifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.exact_match.ExactMatchClassifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;

public class ClassifierPlusExactMatchClassifier implements Classifier {

    private Classifier classifier;
    private ExactMatchClassifier exact_match_classifier;

    /**
     * Needed for JSON deserialization.
     */
    @SuppressWarnings("unused")
    public ClassifierPlusExactMatchClassifier() {
    }

    public ClassifierPlusExactMatchClassifier(Classifier classifier) {

        this.classifier = classifier;
        exact_match_classifier = new ExactMatchClassifier();
    }

    @Override
    public void train(final Bucket bucket) {

        classifier.train(bucket);
        exact_match_classifier.train(bucket);
    }

    @Override
    public Classification classify(String data) {

        Classification result = exact_match_classifier.classify(data);

        if (result != Classification.UNCLASSIFIED) {
            return result;
        } else {
            return classifier.classify(data);
        }
    }

    @Override
    public String getName() {

        return getClass().getSimpleName() + "[" + classifier.getName() + "]";
    }

    @Override
    public String getDescription() {

        return "Classifies using exact match first, then if no match using " + classifier.getName() + " classifier";
    }

    public String toString() {

        return getName();
    }
}