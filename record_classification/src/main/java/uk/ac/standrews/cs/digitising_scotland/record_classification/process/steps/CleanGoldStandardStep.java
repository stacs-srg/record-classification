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
package uk.ac.standrews.cs.digitising_scotland.record_classification.process.steps;

import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.Cleaner;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.processes.generic.ClassificationContext;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.processes.generic.Step;

import java.util.Arrays;
import java.util.List;

public class CleanGoldStandardStep implements Step {

    private final Cleaner cleaner;

    public CleanGoldStandardStep(Cleaner cleaner) {

        this.cleaner = cleaner;
    }

    @Override
    public void perform(ClassificationContext context) {

        final List<Bucket> cleaned_records = cleaner.apply(Arrays.asList(context.getTrainingRecords(), context.getEvaluationRecords()));
        context.setTrainingRecords(cleaned_records.get(0));
        context.setEvaluationRecords(cleaned_records.get(1));
    }
}
