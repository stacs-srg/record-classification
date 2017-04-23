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
package uk.ac.standrews.cs.digitising_scotland.record_classification.process;

import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a classification process that consists of a list of steps and produces a {@link Bucket bucket}  of classified records.
 *
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
public class ClassificationProcess implements Serializable {

    private static final long serialVersionUID = -3086230162106640193L;

    private final List<Step> steps;

    /**
     * Instantiates a new classification process.
     */
    public ClassificationProcess() {

        steps = new ArrayList<>();
    }

    /**
     * Adds a step to the steps to be performed by this process.
     *
     * @param step the step to be performed in the classification process.
     */
    public void addStep(Step step) {

        steps.add(step);
    }

    /**
     * Sequentially performs the steps in this classification process.
     *
     * @return the classified records, or {@code null} if no records were classified
     * @throws Exception if an error while performing the process steps
     */
    public Bucket call(ClassificationContext context) throws Exception {

        for (Step step : steps) {
            step.perform(context);
        }

        return context.getClassifiedUnseenRecords();
    }
}
