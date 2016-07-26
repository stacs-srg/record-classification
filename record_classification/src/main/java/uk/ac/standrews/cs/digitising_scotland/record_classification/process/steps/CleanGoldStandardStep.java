/*
 * record_classification - Automatic record attribute classification.
 * Copyright © 2012-2016 Digitising Scotland project
 * (http://digitisingscotland.cs.st-andrews.ac.uk/record_classification/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.digitising_scotland.record_classification.process.steps;

import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.Cleaner;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.ClassificationContext;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.Step;

import java.util.Arrays;
import java.util.List;

public class CleanGoldStandardStep implements Step {

    private static final long serialVersionUID = -306461499132936070L;
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
