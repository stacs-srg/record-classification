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
package uk.ac.standrews.cs.digitising_scotland.record_classification.experiments.specific;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.PathConverter;
import uk.ac.standrews.cs.classification_schemes.hisco.HiscoGroup;
import uk.ac.standrews.cs.classification_schemes.hisco.HiscoScheme;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.ConfusionMatrix;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.Metrics;
import uk.ac.standrews.cs.digitising_scotland.record_classification.analysis.StrictConfusionMatrix;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.Classifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.ClassifierSupplier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.string_similarity.SimilarityMetric;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.string_similarity.StringSimilaritySupplier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.InputFileFormatException;
import uk.ac.standrews.cs.digitising_scotland.record_classification.experiments.generic.Experiment;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.TokenList;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.ClassificationContext;
import uk.ac.standrews.cs.utilities.Logging;
import uk.ac.standrews.cs.utilities.dataset.DataSet;
import uk.ac.standrews.cs.utilities.tables.ConfidenceIntervals;
import uk.ac.standrews.cs.utilities.tables.Means;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Masih Hajiarab Derkani
 */
public class HiscoClassificationWithPerGroupAnalysis extends Experiment {

    private static final List<String> THREE_COLUMN_DATASET = Arrays.asList("id", "title", "code");
    private static final StringSimilaritySupplier RECORD_SIMILARITY_METRIC = StringSimilaritySupplier.JACCARD;
    private static final int CODE_INDEX = 2;
    private static final int LABEL_INDEX = 1;
    private static final int MAX_CODING_SCHEME_LENGTH = 5;
    private static final HiscoScheme HISCO_SCHEME = new HiscoScheme();
    public static final List<String> CLASSIFICATION_DETAIL_COLUMN_LABELS = Arrays.asList("ID", "DATA", "CODE", "CONFIDENCE", "DETAILS");
    private final Map<String, String> code_label_lookup = new HashMap<>();
    @Parameter(names = {"-c", "--classifierSupplier"}, description = "The classifier to use for experiment.", required = true)
    private ClassifierSupplier classifier_supplier;
    @Parameter(names = {"-u", "--unseenData"}, description = "Path to unseen data to be classified.", converter = PathConverter.class)
    private Path unseen_data_path;
    @Parameter(names = {"-o", "--classifiedRecordsOutput"}, description = "Path to which to persist the classified unseen data.", converter = PathConverter.class)
    private Path classified_unseen_data_path;
    @Parameter(names = {"-oe", "--classifiedEvaluationRecordsOutput"}, description = "Path to which to persist the classified evaluation data.", converter = PathConverter.class)
    private Path classified_evaluation_data_path;
    @Parameter(names = {"-h", "--codingLookupCSV"}, description = "Path to coding scheme description lookup; a 3 column csv: id, label and code", converter = PathConverter.class)
    private Path code_label_lookup_path;

    protected HiscoClassificationWithPerGroupAnalysis(final String[] args) throws IOException, InputFileFormatException {

        super(args);
    }

    public static void main(String[] args) throws Exception {

        final HiscoClassificationWithPerGroupAnalysis experiment = new HiscoClassificationWithPerGroupAnalysis(args);
        experiment.call();
    }

    private static DataSet getRecordsWithMatchingCodePrefix(final DataSet dataSet, final String prefix, final int code_column_index) {

        final DataSet matching_code_prefix = new DataSet(dataSet.getColumnLabels());

        dataSet.getRecords().stream().filter(record -> record.get(code_column_index).startsWith(prefix)).forEach(matching_code_prefix::addRow);

        return matching_code_prefix;
    }

    private static Stream<HiscoGroup> getHiscoMajorGroupCodes() {

        return HISCO_SCHEME.getMajorGroups();
    }

    private static Stream<HiscoGroup> getHiscoMinorGroupCodes() {

        return HISCO_SCHEME.getMinorGroups();
    }

    @Override
    public Void call() throws Exception {

        Logging.setLoggingLevel(verbosity);

        final List<ClassifierResults> results = runExperiment();

        populateCodeLabelLookup();

        if (classified_evaluation_data_path != null) {
            persistClassifiedEvaluationRecords(results);
            persistClassificationMetricsPerGroup(results, getHiscoMajorGroupCodes(), "major");
            persistClassificationMetricsPerGroup(results, getHiscoMinorGroupCodes(), "minor");

            persistClassificationDetails(results);
            persistInconsistentlyCodedRecordsAcrossTwoOrMoreRepetitions(results);
        }

        if (unseen_data_path != null) {
            classifyUnseenRecords(results);
        }

        printSummarisedResults(results);

        return null; //void callable
    }

    private void persistInconsistentlyCodedRecordsAcrossTwoOrMoreRepetitions(final List<ClassifierResults> results) {

        System.out.println("Persisting inconsistently coded evaluation records across two or more repetitions...");

        final List<List<Record>> evaluation_records_appearing_twice_or_more = getEvaluationRecordsAcrossRepetitions(results, 2);

        final DataSet inconsistent_dataset = new DataSet(Arrays.asList("DATA", "OUTPUT_CODES", "GOLD_STANDARD_CODE", "REPETITION_APPEARANCE_COUNT", "CORRECT_COUNT", "INCORRECT_COUNT", "UNIQUE_CODE_COUNT"));
        final Bucket gold_standard = getGoldStandardRecords(results.get(0).getContexts().get(0));

        for (List<Record> records : evaluation_records_appearing_twice_or_more) {

            if (records != null) {

                final String record_original_data = records.get(0).getOriginalData(); //Original data across all records should be the same; if not it's a bug.
                final List<String> output_codes = records.stream().map(record -> record.getClassification().getCode()).collect(Collectors.toList());
                final String output_codes_joined = String.join(",", output_codes);
                final String gold_standard_code = gold_standard.stream().filter(gold_standard_record -> gold_standard_record.getOriginalData().equals(record_original_data)).map(record1 -> record1.getClassification().getCode()).findFirst().orElse("NOT_FOUND");
                final long correct_code_count = output_codes.stream().filter(code -> code.equals(gold_standard_code)).count();
                final int repetition_count = output_codes.size();

                inconsistent_dataset.addRow(record_original_data, output_codes_joined, gold_standard_code, String.valueOf(repetition_count), String.valueOf(correct_code_count), String.valueOf(repetition_count - correct_code_count), String.valueOf(new HashSet<>(output_codes).size()));
            }
        }

        System.out.printf("\tdetected %d inconsistent codes across two or more repetitions%n", inconsistent_dataset.getRecords().size());

        final Path path = Paths.get(classified_evaluation_data_path.toString() + ".inconsistent_code_across_reps.csv");
        try {
            persistDataSetToPath(path, inconsistent_dataset);
            System.out.println("Done persisting inconsistently coded evaluation records across repetitions.\n");
        }
        catch (IOException e) {
            throw new RuntimeException("failed to persist inconsistently coded evaluation records across repetitions", e);
        }
    }

    private List<List<Record>> getEvaluationRecordsAcrossRepetitions(final List<ClassifierResults> results, int min_occurance_count) {

        final List<Map<String, Record>> mapStream = results.get(0).getContexts().stream().map(this::getClassifiedEvaluationRecordsOriginalDataMap).collect(Collectors.toList());

        final Set<String> unique_datas = new HashSet<>();
        for (Map<String, Record> map : mapStream) {
            unique_datas.addAll(map.keySet());
        }

        return unique_datas.stream().filter(data -> {
            // filter ids that are present across maps
            int occurance_count = 0;
            for (Map<String, Record> map : mapStream) {
                if (map.containsKey(data)) {
                    occurance_count++;
                }
            }
            return occurance_count >= min_occurance_count;
        }).map(data -> {

            // map to records
            List<Record> records = new ArrayList<>();

            for (Map<String, Record> map : mapStream) {
                if (map.containsKey(data)) {
                    records.add(map.get(data));
                }
            }
            return records;
        }).collect(Collectors.toList());
    }

    private boolean isInconsistentlyCoded(List<Record> records) {
        // filter records with inconsistent coding
        String code_sofar = null;

        for (Record record : records) {
            final String record_code = record.getClassification().getCode();
            if (code_sofar == null) {
                code_sofar = record_code;
            }
            else if (!code_sofar.equals(record_code)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Record> getClassifiedEvaluationRecordsOriginalDataMap(final ClassificationContext context) {

        return context.getConfusionMatrix().getClassifiedRecords().stream().collect(Collectors.toMap(Record::getOriginalData, Function.identity()));
    }

    private void persistClassificationDetails(final List<ClassifierResults> results) {

        System.out.println("Persisting classification details of classified evaluation records...");
        final AtomicInteger repetition_count = new AtomicInteger();
        results.get(0).getContexts().forEach(context -> persistClassificationDetails(context, Paths.get(classified_evaluation_data_path.toString() + ".classifier_details_" + repetition_count.getAndIncrement() + ".csv")));

        System.out.println("Done persisting classification details of classified evaluation records.\n");
    }

    private void persistClassificationDetails(final ClassificationContext context, Path output) {

        final Bucket classified_evaluation_records = context.getConfusionMatrix().getClassifiedRecords();
        final DataSet details = classified_evaluation_records.toDataSet(CLASSIFICATION_DETAIL_COLUMN_LABELS);

        try {
            persistDataSetToPath(output, details);
        }
        catch (IOException e) {
            throw new RuntimeException("failed to persist classification details at " + output, e);
        }
    }

    @Override
    protected List<Supplier<Classifier>> getClassifierFactories() throws IOException, InputFileFormatException {

        return Collections.singletonList(classifier_supplier);
    }

    private void persistClassificationMetricsPerGroup(final List<ClassifierResults> results, Stream<HiscoGroup> groups, final String output_suffix) {

        System.out.printf("Persisting classification metrics per HISCO %s group for classified evaluation records...%n", output_suffix);

        final DataSet per_group_metrics = new DataSet(Arrays.asList("HISCO_GROUP_CODE", "HISCO_GROUP_TITLE", "MACRO-PRECISION", "MACRO-RECALL", "MACRO-F1", "MICRO-PRECISION/RECALL", "TRAINING_RECORDS_COUNT", "TRAINING_RECORDS_TOKEN_LIST_SIZE", "TRAINING_RECORDS_MEAN_JACCARD_SIMILARITY"));

        groups.forEach(group -> {

            final String group_code = group.getNumericalCode();

            final Stream<Metrics> group_classification_metrics = getClassificationMetricsGroupByCodePrefix(results, group_code);

            final List<List<Double>> metrics_values = group_classification_metrics.map(metric -> Arrays.asList(metric.getMacroAveragePrecision(), metric.getMacroAverageRecall(), metric.getMacroAverageF1(), metric.getMicroAveragePrecision())).collect(Collectors.toList());

            final List<Double> means = new Means(metrics_values).getResults();
            final List<Double> intervals = new ConfidenceIntervals(metrics_values).getResults();

            final List<String> row = new ArrayList<>();
            row.add(group_code);
            row.add(group.getTitle());
            for (int i = 0; i < means.size(); i++) {
                row.add(formatMeanAndInterval(means.get(i), intervals.get(i)));
            }

            final List<List<Record>> trainingRecordsForCodePrefix_per_repetition = getTrainingRecordsForCodePrefix(results, group_code);

            final List<Double> training_records_count_in_group = trainingRecordsForCodePrefix_per_repetition.stream().map(List::size).map(value -> (double) value).collect(Collectors.toList());
            row.add(formatMeanAndInterval(training_records_count_in_group));

            final List<Double> training_token_length = trainingRecordsForCodePrefix_per_repetition.stream().map(this::getAverageDataTokenListSize).collect(Collectors.toList());
            row.add(formatMeanAndInterval(training_token_length));

            final List<Double> mean_jaccard_data_distances = trainingRecordsForCodePrefix_per_repetition.stream().map(records -> getAverageSimilarityAcrossRecords(RECORD_SIMILARITY_METRIC.get(), records)).collect(Collectors.toList());
            row.add(formatMeanAndInterval(mean_jaccard_data_distances));

            per_group_metrics.addRow(row);
        });

        final Path destination = Paths.get(classified_evaluation_data_path.toString() + ".per_hisco_group_" + output_suffix + ".csv");
        try {
            persistDataSetToPath(destination, per_group_metrics);
            System.out.printf("Done persisting classification metrics per HISCO %s group for classified evaluation records.%n%n", output_suffix);
        }
        catch (IOException e) {
            throw new RuntimeException("failed to persist classification metrics per group: " + output_suffix, e);
        }
    }

    private double getAverageDataTokenListSize(List<Record> records) {

        return getDataTokenListSize(records).stream().mapToDouble(value -> value).average().orElseGet(() -> Double.NaN);
    }

    private List<Double> getDataTokenListSize(List<Record> records) {

        return records.stream().map(Record::getData).map(TokenList::new).map(tokens -> (double) tokens.size()).collect(Collectors.toList());
    }

    private String formatMeanAndInterval(List<Double> values) {

        final double confidence_interval = values.size() > 1 ? ConfidenceIntervals.calculateConfidenceInterval(values) : Double.NaN;
        return formatMeanAndInterval(Means.calculateMean(values), confidence_interval);
    }

    private String formatMeanAndInterval(double mean, double interval) {

        return String.format("%.2f ± %.2f", mean, interval);
    }

    private List<Double> getSimilarityAcrossRecords(SimilarityMetric metric, List<Record> records) {

        final int records_size = records.size();

        final List<Double> similarities = new ArrayList<>();

        // assuming similarity metric is a symmetric operation
        for (int outer_index = 0; outer_index < records_size; outer_index++) {

            final Record one = records.get(outer_index);
            for (int inner_index = outer_index + 1; inner_index < records_size; inner_index++) {

                final Record another = records.get(inner_index);
                similarities.add((double) metric.getSimilarity(one.getData(), another.getData()));
            }
        }

        return similarities;
    }

    private double getAverageSimilarityAcrossRecords(SimilarityMetric metric, List<Record> records) {

        return Means.calculateMean(getSimilarityAcrossRecords(metric, records));
    }

    private Stream<Metrics> getClassificationMetricsGroupByCodePrefix(final List<ClassifierResults> results_list, final String code_prefix) {

        return results_list.get(0).getContexts().stream().map(context -> {

            final DataSet gold_standard_records = getGoldStandardRecordsDataSet(context);
            final DataSet evaluation_classified_records = getClassifiedEvaluationRecords(context);

            final Set<String> gold_standard_record_ids_in_group = getIdOfRecordsWithMatchingCodePrefix(code_prefix, gold_standard_records);
            final DataSet evaluation_classified_records_in_group = getSubsetById(evaluation_classified_records, gold_standard_record_ids_in_group);

            final StrictConfusionMatrix matrix = new StrictConfusionMatrix(evaluation_classified_records_in_group, gold_standard_records);

            return new Metrics(matrix);
        });
    }

    private DataSet getSubsetById(final DataSet evaluation_classified_records, final Set<String> gold_standard_record_ids_in_group) {

        final DataSet evaluation_classified_records_in_group = new DataSet(THREE_COLUMN_DATASET);
        evaluation_classified_records.getRecords().stream().filter(record -> gold_standard_record_ids_in_group.contains(getRecordId(record))).forEach(evaluation_classified_records_in_group::addRow);
        return evaluation_classified_records_in_group;
    }

    private String getRecordId(final List<String> record) {return record.get(0);}

    private Set<String> getIdOfRecordsWithMatchingCodePrefix(final String major_group_code, final DataSet gold_standard_records) {return getRecordsWithMatchingCodePrefix(gold_standard_records, major_group_code, 2).getRecords().stream().map(this::getRecordId).collect(Collectors.toSet());}

    private DataSet getClassifiedEvaluationRecords(final ClassificationContext context) {return context.getConfusionMatrix().getClassifiedRecords().toDataSet2(THREE_COLUMN_DATASET);}

    private DataSet getGoldStandardRecordsDataSet(final ClassificationContext context) {

        return getGoldStandardRecords(context).toDataSet2(THREE_COLUMN_DATASET);
    }

    private Bucket getGoldStandardRecords(final ClassificationContext context) {

        final Bucket training_records = context.getTrainingRecords();
        final Bucket evaluation_records = context.getEvaluationRecords();
        return training_records.union(evaluation_records);
    }

    private List<List<Record>> getTrainingRecordsForCodePrefix(final List<ClassifierResults> results_list, final String code_prefix) {

        return results_list.get(0).getContexts().stream().map(context -> getTrainingRecordsForCodePrefix(context, code_prefix)).collect(Collectors.toList());
    }

    private List<Record> getTrainingRecordsForCodePrefix(final ClassificationContext context, final String code_prefix) {

        return context.getTrainingRecords().stream().filter(record -> record.getClassification().getCode().startsWith(code_prefix)).collect(Collectors.toList());
    }

    private void persistClassifiedEvaluationRecords(final List<ClassifierResults> results) throws IOException {

        System.out.println("Persisting classified evaluation records...");
        final AtomicInteger repetition_count = new AtomicInteger();
        results.get(0).getContexts().forEach(context -> {
            try {
                persistClassifiedEvaluationRecords(context, Paths.get(classified_evaluation_data_path.toString() + ".all_rep_" + repetition_count.getAndIncrement() + ".csv"));
                System.out.println("Done persisting classified evaluation records.");
            }
            catch (IOException e) {
                throw new RuntimeException("failed to persist classified evaluation records", e);
            }
        });
    }

    private void persistClassifiedEvaluationRecords(ClassificationContext context, Path destination) throws IOException {

        final Bucket evaluation_records = context.getEvaluationRecords();
        final ConfusionMatrix confusion_matrix = context.getConfusionMatrix();
        final Metrics classification_metrics = context.getClassificationMetrics();
        final Map<String, Double> per_class_precision = classification_metrics.getPerClassPrecision();
        final Map<String, Double> per_class_recall = classification_metrics.getPerClassRecall();
        final Map<String, Double> per_class_f1 = classification_metrics.getPerClassF1();
        final Map<String, Double> per_class_accuracy = classification_metrics.getPerClassAccuracy();
        final Bucket evaluation_classified_records = confusion_matrix.getClassifiedRecords();
        final Stream<Record> evaluation_records_stream = StreamSupport.stream(evaluation_records.spliterator(), false);
        final Map<Integer, Record> evaluation_records_map = evaluation_records_stream.collect(Collectors.toMap(Record::getId, record -> record));

        final DataSet evaluation_output = new DataSet(
                        Arrays.asList("ID", "RAW_DATA", "GOLD_STANDARD_CODE", "GOLD_STANDARD_SCHEME_LABEL", "OUTPUT_CODE", "OUTPUT_CODE_SCHEME_LABEL", "ANCESTOR_DISTANCE", "CONFIDENCE", "OUTPUT_CODE_PRECISION", "OUTPUT_CODE_RECALL", "OUTPUT_CODE_F1", "OUTPUT_CODE_ACCURACY",
                                      "GOLD_STANDARD_CODE_PRECISION", "GOLD_STANDARD_CODE_RECALL", "GOLD_STANDARD_CODE_F1", "GOLD_STANDARD_CODE_ACCURACY"
                        ));
        for (Record record : evaluation_classified_records) {

            final int id = record.getId();
            final Classification classification = record.getClassification();

            final Record gold_standard_record = evaluation_records_map.get(id);
            final Classification gold_standard_classification = gold_standard_record.getClassification();

            final String id_string = String.valueOf(id);
            final String raw_data = record.getOriginalData();
            final String gold_standard_code = gold_standard_classification.getCode();
            final String gold_standard_scheme_label = getCodingSchemeLable(gold_standard_code);
            final String output_code = classification.getCode();
            final String output_code_scheme_label = getCodingSchemeLable(output_code);
            final String ancestor_distance = String.valueOf(MAX_CODING_SCHEME_LENGTH - longestMatchingPrefixLength(gold_standard_code, output_code));
            final String confidence = formatToTwoDecimalPlaces(classification.getConfidence());

            final String output_precision = formatToTwoDecimalPlaces(per_class_precision.get(output_code));
            final String output_recall = formatToTwoDecimalPlaces(per_class_recall.get(output_code));
            final String output_f1 = formatToTwoDecimalPlaces(per_class_f1.get(output_code));
            final String output_accuracy = formatToTwoDecimalPlaces(per_class_accuracy.get(output_code));

            final String gold_precision = formatToTwoDecimalPlaces(per_class_precision.get(gold_standard_code));
            final String gold_recall = formatToTwoDecimalPlaces(per_class_recall.get(gold_standard_code));
            final String gold_f1 = formatToTwoDecimalPlaces(per_class_f1.get(gold_standard_code));
            final String gold_accuracy = formatToTwoDecimalPlaces(per_class_accuracy.get(gold_standard_code));

            evaluation_output.addRow(id_string, raw_data, gold_standard_code, gold_standard_scheme_label, output_code, output_code_scheme_label, ancestor_distance, confidence, output_precision, output_recall, output_f1, output_accuracy,
                                     gold_precision, gold_recall, gold_f1, gold_accuracy
            );
        }

        persistDataSetToPath(destination, evaluation_output);
    }

    private String formatToTwoDecimalPlaces(final double value) {return String.format("%.2f", value);}

    private String getCodingSchemeLable(final String code) {return code_label_lookup.getOrDefault(code, "NON_ROOT");}

    public int longestMatchingPrefixLength(String one, String another) {

        return longestMatchingPrefix(one, another).length();
    }

    public String longestMatchingPrefix(String one, String another) {

        final int min_length = Math.min(one.length(), another.length());
        for (int i = 0; i < min_length; i++) {
            if (one.charAt(i) != another.charAt(i)) {
                return one.substring(0, i);
            }
        }
        return one.substring(0, min_length);
    }

    private void persistDataSetToPath(Path destination, final DataSet classified_unseen_data_set) throws IOException {

        try (final BufferedWriter out = Files.newBufferedWriter(destination)) {
            classified_unseen_data_set.print(out);
        }
    }

    private void populateCodeLabelLookup() throws IOException {

        if (code_label_lookup_path != null) {

            final InputStream stream = Files.newInputStream(code_label_lookup_path);
            final DataSet code_label_dataset = new DataSet(stream);

            for (List<String> cells : code_label_dataset.getRecords()) {
                code_label_lookup.put(cells.get(CODE_INDEX), cells.get(LABEL_INDEX));
            }
        }
    }

    private void classifyUnseenRecords(final List<ClassifierResults> results) throws IOException {

        System.out.println("Classifying unseen records...");
        //TODO embed this into the steps of the last classification process.
        final ClassifierResults last_results = getLast(results);
        final Classifier classifier = last_results.getContexts().get(0).getClassifier();
        final InputStream stream = Files.newInputStream(unseen_data_path);
        final DataSet unseen_data_set = new DataSet(stream);
        final Bucket unseen_data_bucket = new Bucket(unseen_data_set);
        final Bucket classified_unseen_data = classifier.classify(unseen_data_bucket);
        final List<String> classified_dataset_labels = Arrays.asList("ID", "RAW_DATA", "OUTPUT_CODE", "OUTPUT_CODE_SCHEME_LABEL", "CONFIDENCE");
        final DataSet classified_unseen_data_set = new DataSet(classified_dataset_labels);

        for (Record record : classified_unseen_data) {

            final Classification classification = record.getClassification();

            final String id = String.valueOf(record.getId());
            final String raw_data = record.getOriginalData();
            final String output_code = classification.getCode();
            final String output_code_scheme_label = getCodingSchemeLable(output_code);
            final String confidence = formatToTwoDecimalPlaces(classification.getConfidence());

            classified_unseen_data_set.addRow(id, raw_data, output_code, output_code_scheme_label, confidence);
        }

        if (classified_unseen_data_path != null) {
            persistDataSetToPath(classified_unseen_data_path, classified_unseen_data_set);
            System.out.println("\t persisted classified unseen records");
        }
        else {
            System.out.println();
            System.out.println("no destination is specified to persist classified unseen data; printing data into stdout instead.");
            System.out.println();
            System.out.println();
            classified_unseen_data_set.print(System.out);
            System.out.println();
            System.out.println();
        }

        System.out.println("Done classifying unseen records.\n");
    }

    private ClassifierResults getLast(final List<ClassifierResults> results) {

        return results != null && !results.isEmpty() ? results.get(results.size() - 1) : null;
    }
}
