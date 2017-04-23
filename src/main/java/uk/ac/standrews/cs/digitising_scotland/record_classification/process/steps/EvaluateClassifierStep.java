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

import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.Metrics;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.StrictConfusionMatrix;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.ClassificationContext;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.Step;
import uk.ac.standrews.cs.utilities.Formatting;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static uk.ac.standrews.cs.utilities.Logging.output;
import static uk.ac.standrews.cs.utilities.LoggingLevel.LONG_SUMMARY;

/**
 * Evaluates a classifier in the context of a classification process.
 * Stores the result of evaluation by setting the {@link ClassificationContext#getConfusionMatrix() confusion matrix}
 * and {@link ClassificationContext#getClassificationMetrics() classification metrix} of the context.
 *
 * @author Graham Kirby
 * @author Masih Hajiarab Derkani
 */
public class EvaluateClassifierStep implements Step {

    private static final long serialVersionUID = -2181763269734136008L;

    public EvaluateClassifierStep() {
    }

    @Override
    public void perform(final ClassificationContext context) {

        final Bucket training_records = context.getTrainingRecords();
        final Bucket unique_evaluation_records = context.getUniqueEvaluationRecords();
        final Bucket gold_standard_records = training_records.union(unique_evaluation_records);

        final Instant start = Instant.now();
        final Bucket classified_records = context.getClassifier().classify(unique_evaluation_records.stripRecordClassifications());
        context.setEvaluationClassificationTime(Duration.between(start, Instant.now()));

        final StrictConfusionMatrix confusion_matrix = new StrictConfusionMatrix(classified_records, gold_standard_records);
        final Metrics classification_metrics = new Metrics(confusion_matrix);

        context.setConfusionMatrix(confusion_matrix);
        context.setClassificationMetrics(classification_metrics);

        final Set<String> unique_training_strings = extractRecordsData(context.getTrainingRecords().makeUniqueDataRecords());
        final Set<String> unique_evaluation_strings = extractRecordsData(unique_evaluation_records);

        final int training_records_size = context.getTrainingRecords().size();
        final int number_of_evaluation_strings_not_in_training_set = countEvaluationStringsNotInTrainingSet(unique_training_strings, unique_evaluation_strings);

        output(LONG_SUMMARY, "\n----------------------------------\n");
        output(LONG_SUMMARY, "classifier: " + context.getClassifier().getName());
        output(LONG_SUMMARY, "");
        output(LONG_SUMMARY, "total records              : %s%n", Formatting.format(training_records_size + unique_evaluation_records.size()));
        output(LONG_SUMMARY, "records used for training  : %s (%s unique)%n", Formatting.format(training_records_size), Formatting.format(unique_training_strings.size()));
        output(LONG_SUMMARY, "records used for evaluation: %s (%s unique, %s not in training set)%n", Formatting.format(context.getEvaluationRecords().size()), Formatting.format(unique_evaluation_records.size()), Formatting.format(number_of_evaluation_strings_not_in_training_set));
        output(LONG_SUMMARY, "");

        context.getClassificationMetrics().printMetrics();

        output(LONG_SUMMARY, "");
        output(LONG_SUMMARY, "training time              : %s%n", Formatting.format(context.getTrainingTime()));
        output(LONG_SUMMARY, "classification time        : %s%n", Formatting.format(context.getEvaluationClassificationTime()));

        output(LONG_SUMMARY, "----------------------------------");
    }

    private static Set<String> extractRecordsData(final Bucket bucket) {

        final Set<String> strings = new HashSet<>();

        for (Record record : bucket) {
            strings.add(record.getData());
        }

        return strings;
    }

    private static int countEvaluationStringsNotInTrainingSet(final Set<String> unique_training, final Set<String> unique_evaluation) {

        int count = 0;
        for (String evaluation_string : unique_evaluation) {
            if (!unique_training.contains(evaluation_string))
                count++;
        }
        return count;
    }
}
