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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cli.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.PathConverter;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.ConfusionMatrix;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.Metrics;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.StrictConfusionMatrix;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.Classifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Configuration;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Launcher;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.utilities.tables.ConfidenceIntervals;
import uk.ac.standrews.cs.utilities.tables.Means;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static uk.ac.standrews.cs.digitising_scotland.record_classification.cli.Configuration.*;

/**
 * @author Masih Hajiarab Derkani
 */
@Parameters(commandNames = EvaluateCommand.NAME, resourceBundle = Configuration.RESOURCE_BUNDLE_NAME, commandDescriptionKey = "command.evaluate.description")
public class EvaluateCommand extends Command {

    /** The name of this command. */
    public static final String NAME = "evaluate";

    /** The short name of the option that specifies the path in which to store the classified evaluation records. **/
    public static final String OPTION_OUTPUT_RECORDS_PATH_SHORT = "-o";

    /** The long name of the option that specifies the path in which to store the classified evaluation records. **/
    public static final String OPTION_OUTPUT_RECORDS_PATH_LONG = "--output";

    @Parameter(names = {OPTION_OUTPUT_RECORDS_PATH_SHORT, OPTION_OUTPUT_RECORDS_PATH_LONG}, description = "command.evaluate.output.description", converter = PathConverter.class)
    private Path classified_evaluation_records;

    public EvaluateCommand(final Launcher launcher) {

        super(launcher, NAME);
    }

    static void logClassificationMetrics(Logger logger, final List<Metrics> classification_metrics) {

        logger.info(() -> String.format("%-30s %s", "Macro Average Accuracy:", getFormattedMeanAndConfidenceInterval(classification_metrics, Metrics::getMacroAverageAccuracy)));
        logger.info(() -> String.format("%-30s %s", "Macro Average F1:", getFormattedMeanAndConfidenceInterval(classification_metrics, Metrics::getMacroAverageF1)));
        logger.info(() -> String.format("%-30s %s", "Macro Average Precision:", getFormattedMeanAndConfidenceInterval(classification_metrics, Metrics::getMacroAveragePrecision)));
        logger.info(() -> String.format("%-30s %s", "Macro Average Recall:", getFormattedMeanAndConfidenceInterval(classification_metrics, Metrics::getMacroAverageRecall)));
        logger.info(() -> String.format("%-30s %s", "Micro Average Accuracy:", getFormattedMeanAndConfidenceInterval(classification_metrics, Metrics::getMicroAverageAccuracy)));
        logger.info(() -> String.format("%-30s %s", "Micro Average F1:", getFormattedMeanAndConfidenceInterval(classification_metrics, Metrics::getMicroAverageF1)));
        logger.info(() -> String.format("%-30s %s", "Micro Average Precision:", getFormattedMeanAndConfidenceInterval(classification_metrics, Metrics::getMicroAveragePrecision)));
        logger.info(() -> String.format("%-30s %s", "Micro Average Recall:", getFormattedMeanAndConfidenceInterval(classification_metrics, Metrics::getMicroAverageRecall)));
    }

    static void logConfusionMatrix(Logger logger, final List<ConfusionMatrix> confusion_matrix) {

        logger.info(() -> String.format("%-30s %s", "Number Of Classes:", getFormattedMeanAndConfidenceInterval(confusion_matrix, ConfusionMatrix::getNumberOfClasses)));
        logger.info(() -> String.format("%-30s %s", "Number Of Classifications:", getFormattedMeanAndConfidenceInterval(confusion_matrix, ConfusionMatrix::getNumberOfClassifications)));
        logger.info(() -> String.format("%-30s %s", "Number Of True Positives:", getFormattedMeanAndConfidenceInterval(confusion_matrix, ConfusionMatrix::getNumberOfTruePositives)));
        logger.info(() -> String.format("%-30s %s", "Number Of True Negatives:", getFormattedMeanAndConfidenceInterval(confusion_matrix, ConfusionMatrix::getNumberOfTrueNegatives)));
        logger.info(() -> String.format("%-30s %s", "Number Of False Negatives:", getFormattedMeanAndConfidenceInterval(confusion_matrix, ConfusionMatrix::getNumberOfFalseNegatives)));
        logger.info(() -> String.format("%-30s %s", "Number Of False Positives:", getFormattedMeanAndConfidenceInterval(confusion_matrix, ConfusionMatrix::getNumberOfFalsePositives)));
    }

    private static <Value> String getFormattedMeanAndConfidenceInterval(List<Value> values, Function<Value, ? extends Number> getter) {

        final List<Double> doubles = values.stream().map(getter).map(Number::doubleValue).collect(Collectors.toList());
        return formatMeanAndInterval(doubles);
    }

    private static String formatMeanAndInterval(List<Double> values) {

        if (values.size() == 1) {
            return String.format("%.2f", values.get(0));
        }
        else {
            return formatMeanAndInterval(Means.calculateMean(values), ConfidenceIntervals.calculateConfidenceInterval(values));
        }
    }

    private static String formatMeanAndInterval(double mean, double interval) {

        return String.format("%.2f ± %.2f", mean, interval);
    }

    @Override
    public void run() {

        final Bucket unique_evaluation_records = configuration.requireEvaluationRecords().makeUniqueDataRecords();
        final Bucket gold_standard_records = configuration.requireGoldStandardRecords();
        final Bucket evaluation_records_stripped = unique_evaluation_records.stripRecordClassifications();
        final Classifier classifier = configuration.requireClassifier();

        final Instant start = Instant.now();
        final Bucket classified_evaluation_records = classifier.classify(evaluation_records_stripped);
        final Duration evaluation_classification_time = Duration.between(start, Instant.now());

        logger.info(() -> String.format("classified %d evaluation records in %s", evaluation_records_stripped.size(), evaluation_classification_time));

        final ConfusionMatrix confusion_matrix = new StrictConfusionMatrix(classified_evaluation_records, gold_standard_records);
        final Metrics classification_metrics = new Metrics(confusion_matrix);

        configuration.setClassifiedEvaluationRecords(classified_evaluation_records);
        configuration.setEvaluationClassificationTime(evaluation_classification_time);
        configuration.setConfusionMatrix(confusion_matrix);
        configuration.setClassificationMetrics(classification_metrics);

        logConfusionMatrix(logger, Collections.singletonList(confusion_matrix));
        logClassificationMetrics(logger, Collections.singletonList(classification_metrics));

        if (isOutputClassifiedRecordsPathSet()) {
            persistClassifiedEvaluationRecords(classified_evaluation_records);
        }

        //TODO export matrix as json?
        //TODO export metrics as json?
    }

    private boolean isOutputClassifiedRecordsPathSet() {

        return classified_evaluation_records != null;
    }

    private void persistClassifiedEvaluationRecords(final Bucket classified_records) {

        final Path destination = resolveRelativeToWorkingDirectory(classified_evaluation_records);
        try {
            persistBucketAsCSV(classified_records, destination, RECORD_CSV_FORMAT, RESOURCE_CHARSET);
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Failure while exporting classified evaluation records: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static class Builder extends Command.Builder {

        private Path classified_evaluation_records;

        public void setOutput(Path classified_evaluation_records) {

            this.classified_evaluation_records = classified_evaluation_records;
        }

        @Override
        protected void populateArguments() {

            addArgument(NAME);
            if (classified_evaluation_records != null) {
                addArgument(OPTION_OUTPUT_RECORDS_PATH_SHORT);
                addArgument(classified_evaluation_records);
            }
        }
    }
}
