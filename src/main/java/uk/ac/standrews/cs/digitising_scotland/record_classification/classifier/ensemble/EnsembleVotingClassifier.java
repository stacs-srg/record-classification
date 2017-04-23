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

import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.SingleClassifier;

import java.util.Collection;

/**
 * @author Graham Kirby
 */
public class EnsembleVotingClassifier extends EnsembleClassifier {

    private static final long serialVersionUID = 6432371860467757296L;

    /**
     * Needed for JSON deserialization.
     */
    public EnsembleVotingClassifier() {
    }

    /**
     * Instantiates a new ensemble classifier.
     *
     * @param classifiers the classifiers
     */
    public EnsembleVotingClassifier(Collection<SingleClassifier> classifiers) {

        super(classifiers, new VotingResolutionStrategy());
    }

    public EnsembleVotingClassifier(Collection<SingleClassifier> classifiers, StringSimilarityGroupWithSharedState group) {

        super(classifiers, group, new VotingResolutionStrategy());
    }
}
