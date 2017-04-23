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
package uk.ac.standrews.cs.digitising_scotland.record_classification.analysis;

import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.Checker;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.ConsistentCodingChecker;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning.UnclassifiedChecker;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.UnclassifiedGoldStandardRecordException;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.UnknownClassificationException;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.UnknownDataException;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;
import uk.ac.standrews.cs.utilities.Formatting;
import uk.ac.standrews.cs.utilities.Logging;
import uk.ac.standrews.cs.utilities.LoggingLevel;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * General implementation of confusion matrix representing the effectiveness of a classification process.
 * The details of whether a classification result is considered to be correct is devolved to subclasses.
 *
 * @author Fraser Dunlop
 * @author Graham Kirby
 */
public abstract class ConfusionMatrix implements Serializable {

    private static final long serialVersionUID = -4742093776785376822L;

    private static final Logger LOGGER = Logger.getLogger(ConfusionMatrix.class.getName());
    private static final UnclassifiedChecker HAS_UNCLASSIFIED_RECORDS = new UnclassifiedChecker();
    private static final Predicate<List<Bucket>> HAS_INCONSISTENTLY_CODED_RECORDS = new ConsistentCodingChecker().negate();

    private final ConcurrentHashMap<String, AtomicInteger> classification_counts;
    private final ConcurrentHashMap<String, AtomicInteger> true_positive_counts;
    private final ConcurrentHashMap<String, AtomicInteger> false_positive_counts;
    private final ConcurrentHashMap<String, AtomicInteger> true_negative_counts;
    private final ConcurrentHashMap<String, AtomicInteger> false_negative_counts;
    private final ConcurrentHashMap<String, String> gold_standard_data_to_code_map;

    private Bucket classified_records;

    private Bucket gold_standard_records;
    private transient DataSet classified_records_data_set;

    private transient DataSet gold_standard_records_data_set;
    private int number_of_records;
    private int total_number_of_classifications = 0;
    private int total_number_of_gold_standard_classifications = 0;
    private int total_number_of_records_with_correct_number_of_classifications = 0;

    /**
     * Creates a confusion matrix representing the effectiveness of a classification process.
     *
     * @param classified_records the records that have been classified
     * @param gold_standard_records the gold standard records against which the classified records should be checked
     * @throws UnknownClassificationException if a code in the classified records does not appear in the gold standard records
     * @throws UnknownDataException if a record in the classified records contains data that does not appear in the gold standard records
     * @throws UnclassifiedGoldStandardRecordException if a record in the gold standard records is not classified
     */
    ConfusionMatrix(final Bucket classified_records, final Bucket gold_standard_records) {

        this(classified_records, gold_standard_records, null);
    }

    /**
     * Creates a confusion matrix representing the effectiveness of a classification process.
     *
     * @param classified_records the records that have been classified
     * @param gold_standard_records the gold standard records against which the classified records should be checked
     * @param gold_standard_checker custom checking of gold standard records
     * @throws UnknownClassificationException if a code in the classified records does not appear in the gold standard records
     * @throws UnknownDataException if a record in the classified records contains data that does not appear in the gold standard records
     * @throws UnclassifiedGoldStandardRecordException if a record in the gold standard records is not classified
     */
    ConfusionMatrix(final Bucket classified_records, final Bucket gold_standard_records, final Checker gold_standard_checker) {

        this.classified_records = classified_records;
        this.gold_standard_records = gold_standard_records;

        classification_counts = new ConcurrentHashMap<>();
        true_positive_counts = new ConcurrentHashMap<>();
        true_negative_counts = new ConcurrentHashMap<>();
        false_positive_counts = new ConcurrentHashMap<>();
        false_negative_counts = new ConcurrentHashMap<>();
        gold_standard_data_to_code_map = new ConcurrentHashMap<>();

        checkGoldStandardDataIsClassifiedAndIsConsistent();
        checkClassifiedDataIsInGoldStandard();
        checkClassifiedToValidCodes();

        if (gold_standard_checker != null && !gold_standard_checker.test(gold_standard_records)) {
            throw new RuntimeException("check failed: " + gold_standard_checker.getClass().getSimpleName());
        }

        calculateCounts();
    }

    /**
     * Creates a confusion matrix representing the effectiveness of a classification process.
     * This version considers multiple classifications.
     *
     * @param classified_records the records that have been classified
     * @param gold_standard_records the gold standard records against which the classified records should be checked
     * @throws UnknownClassificationException if a code in the classified records does not appear in the gold standard records
     * @throws UnknownDataException if a record in the classified records contains data that does not appear in the gold standard records
     * @throws UnclassifiedGoldStandardRecordException if a record in the gold standard records is not classified
     */
    public ConfusionMatrix(DataSet classified_records, DataSet gold_standard_records) {

        this.classified_records_data_set = classified_records;
        this.gold_standard_records_data_set = gold_standard_records;

        classification_counts = new ConcurrentHashMap<>();
        true_positive_counts = new ConcurrentHashMap<>();
        true_negative_counts = new ConcurrentHashMap<>();
        false_positive_counts = new ConcurrentHashMap<>();
        false_negative_counts = new ConcurrentHashMap<>();
        gold_standard_data_to_code_map = new ConcurrentHashMap<>();
    }

    protected void initMultipleClassification() {

        calculateCounts(classified_records_data_set, gold_standard_records_data_set);
    }

    /**
     * Checks whether a classification result is considered to be correct.
     *
     * @param asserted_code the code asserted by the classifier
     * @param real_code the real code as defined in the gold standard records
     * @return true if the asserted code is considered to be correct
     */
    protected abstract boolean classificationsMatch(String asserted_code, String real_code);

    /**
     * Allows for only part of a code to be considered in the confusion matrix, to allow for hierarchical classification systems.
     *
     * @param code the code
     * @return the part of the code to be used in the confusion matrix
     */
    protected String significantPartOfCode(String code) {

        return code;
    }

    /**
     * Returns a map from classification class to the number of true positives for that class.
     * That is, the number of records that were classified as that class, and really were of that class.
     *
     * @return the map
     */
    public Map<String, AtomicInteger> getTruePositiveCounts() {

        return true_positive_counts;
    }

    /**
     * Returns a map from classification class to the number of false positives for that class.
     * That is, the number of records that were classified as that class, but were not of that class.
     *
     * @return the map
     */
    public Map<String, AtomicInteger> getFalsePositiveCounts() {

        return false_positive_counts;
    }

    /**
     * Returns a map from classification class to the number of false negatives for that class.
     * That is, the number of records that were not classified as that class, but actually were of that class.
     *
     * @return the map
     */
    public Map<String, AtomicInteger> getFalseNegativeCounts() {

        return false_negative_counts;
    }

    /**
     * Returns a map from classification class to the number of true negatives for that class.
     * That is, the number of records that were not classified as that class, and really were not of that class.
     *
     * @return the map
     */
    public Map<String, AtomicInteger> getTrueNegativeCounts() {

        return true_negative_counts;
    }

    /**
     * Returns the total number of classifications.
     *
     * @return the number of classifications
     */
    public int getNumberOfClassifications() {

        return classified_records.size();
    }

    /**
     * Returns the total number of true positives.
     *
     * @return the number of true positives
     */
    public int getNumberOfTruePositives() {

        return sum(true_positive_counts);
    }

    private int sum(Map<String, AtomicInteger> counts) {

        int sum = 0;
        for (AtomicInteger i : counts.values()) {
            sum += i.get();
        }
        return sum;
    }

    /**
     * Returns the total number of false positives.
     *
     * @return the number of false positives
     */
    public int getNumberOfFalsePositives() {

        return sum(false_positive_counts);
    }

    /**
     * Returns the total number of true negatives.
     *
     * @return the number of true negatives
     */
    public int getNumberOfTrueNegatives() {

        // Don't count unclassified decisions in true negatives.

        return sum(true_negative_counts) - true_negative_counts.get(Classification.UNCLASSIFIED.getCode()).get();
    }

    /**
     * Returns the total number of false negatives.
     *
     * @return the number of false negatives
     */
    public int getNumberOfFalseNegatives() {

        return sum(false_negative_counts);
    }

    /**
     * Returns the total number of classes present in the classified data.
     *
     * @return the number of classifications
     */
    public int getNumberOfClasses() {

        return getClassificationCounts().size();
    }

    /**
     * Returns a map from classification class to the number of records classified as that class.
     *
     * @return the map
     */
    public Map<String, AtomicInteger> getClassificationCounts() {

        return classification_counts;
    }

    /**
     * Checks whether all records in the gold standard records are classified and classified consistently.
     *
     * @throws UnclassifiedGoldStandardRecordException if they are not
     */
    private void checkGoldStandardDataIsClassifiedAndIsConsistent() {

        if (HAS_UNCLASSIFIED_RECORDS.or(HAS_INCONSISTENTLY_CODED_RECORDS).test(Collections.singletonList(gold_standard_records))) {
            throw new UnclassifiedGoldStandardRecordException();
        }
    }

    /**
     * Checks whether all data in the classified records appears in the gold standard records.
     *
     * @throws UnknownDataException if it does not
     */
    private void checkClassifiedDataIsInGoldStandard() {

        Set<String> known_data = new HashSet<>();

        for (Record record : gold_standard_records) {

            known_data.add(record.getData());
        }

        for (Record record : classified_records) {

            String data = record.getData();
            if (!known_data.contains(data))
                throw new UnknownDataException("data: " + data + " is not in the gold standard data");
        }
    }

    /**
     * Checks whether all codes in the classified records appears in the gold standard records.
     *
     * @throws UnknownClassificationException if they do not
     */
    private void checkClassifiedToValidCodes() {

        Set<String> valid_codes = new HashSet<>();
        valid_codes.add(Classification.UNCLASSIFIED.getCode());

        for (Record record : gold_standard_records) {

            valid_codes.add(record.getClassification().getCode());
        }

        for (Record record : classified_records) {

            Classification classification = record.getClassification();

            if (!valid_codes.contains(classification.getCode())) {
                throw new UnknownClassificationException("unknown code: " + classification.getCode());
            }
        }
    }

    /**
     * Gets the classified records based on which this matrix is constructed.
     *
     * @return the classified records based on which this matrix is constructed
     */
    public Bucket getClassifiedRecords() {

        return classified_records;
    }

    /**
     * Calculates the true/false positive/negative counts for the bucket.
     */
    private void calculateCounts() {

        initCounts();
        updateCounts();
    }

    private void calculateCounts(DataSet classified_records, DataSet gold_standard_records) {

        for (List<String> record : gold_standard_records.getRecords()) {
            initCounts(record);
        }
        initCounts(Classification.UNCLASSIFIED.getCode());

        for (List<String> record : classified_records.getRecords()) {

            updateCountsForRecord(record, gold_standard_records);
        }

        number_of_records = classified_records.getRecords().size();
    }

    private void initCounts() {

        gold_standard_records.parallelStream().forEach(record -> {

            final String code = record.getClassification().getCode();
            updateSearchIndex(record);
            initCounts(code);
        });
        initCounts(Classification.UNCLASSIFIED.getCode());
    }

    private void updateSearchIndex(final Record record) {

        final String code = record.getClassification().getCode();
        final String data = record.getData();
        gold_standard_data_to_code_map.putIfAbsent(data, code);
    }

    private void initCounts(String code) {

        initCount(code, classification_counts);
        initCount(code, true_positive_counts);
        initCount(code, true_negative_counts);
        initCount(code, false_positive_counts);
        initCount(code, false_negative_counts);
    }

    private void initCounts(List<String> record) {

        for (int i = 2; i < record.size(); i++) {
            String code = record.get(i);
            if (code.length() > 0) {
                initCounts(code);
            }
        }
    }

    private void updateCounts() throws UnknownDataException {

        classified_records.parallelStream().forEach(record -> {

            Classification classification = record.getClassification();

            String asserted_code = classification.getCode();
            String real_code = findGoldStandardCode(record.getData());

            updateCountsForRecord(asserted_code, real_code);

            Logging.output(LoggingLevel.VERBOSE, record.getOriginalData() + "\t" + real_code + "\t" + classification.getCode() + "\t" + Formatting.format(classification.getConfidence(), 2) + "\t" + classification.getDetail());
        });
    }

    private void updateCountsForRecord(List<String> classified_record, DataSet gold_standard_records) {

        List<String> classifier_codes = extractCodes(classified_record);
        List<String> gold_standard_codes = findGoldStandardCodes(classified_record, gold_standard_records);

        for (String possible_code : classification_counts.keySet()) {

            if (classificationsMatch(possible_code, classifier_codes)) {

                incrementCount(possible_code, classification_counts);

                if (classificationsMatch(possible_code, gold_standard_codes)) {

                    incrementCount(possible_code, true_positive_counts);

                }
                else {

                    incrementCount(possible_code, false_positive_counts);

                }
            }
            else {

                if (classificationsMatch(possible_code, gold_standard_codes)) {

                    incrementCount(possible_code, false_negative_counts);

                }
                else {

                    incrementCount(possible_code, true_negative_counts);
                }
            }
        }

        int number_of_classifications = classifier_codes.size();
        if (classifier_codes.contains(Classification.UNCLASSIFIED.getCode())) {
            number_of_classifications--;
        }

        total_number_of_classifications += number_of_classifications;

        total_number_of_gold_standard_classifications += gold_standard_codes.size();

        int numberOfUniqueClassifierCodes = numberOfUniqueClassificationsAtHierarchyLevel(classifier_codes);
        int numberOfUniqueGoldStandardCodes = numberOfUniqueClassificationsAtHierarchyLevel(gold_standard_codes);
        if (numberOfUniqueClassifierCodes == numberOfUniqueGoldStandardCodes) {

            total_number_of_records_with_correct_number_of_classifications++;
        }
    }

    private int numberOfUniqueClassificationsAtHierarchyLevel(List<String> codes) {

        Set<String> unique_codes = new HashSet<>();
        for (String code : codes) {
            if (!code.equals(Classification.UNCLASSIFIED.getCode())) {
                unique_codes.add(significantPartOfCode(code));
            }
        }
        return unique_codes.size();
    }

    protected boolean classificationsMatch(String code, List<String> codes) {

        for (String other_code : codes) {
            if (classificationsMatch(other_code, code)) {
                return true;
            }
        }

        return false;
    }

    private List<String> findGoldStandardCodes(List<String> classified_record, DataSet gold_standard_records) {

        String data = classified_record.get(1);
        for (List<String> gold_standard_record : gold_standard_records.getRecords()) {
            if (gold_standard_record.get(1).equals(data))
                return extractCodes(gold_standard_record);
        }
        return new ArrayList<>();
    }

    private List<String> extractCodes(List<String> classified_record) {

        List<String> codes = new ArrayList<>();
        boolean found_code = false;
        for (String code : classified_record.subList(2, classified_record.size())) {
            if (code.length() > 0) {
                codes.add(code);
                found_code = true;
            }
        }
        if (!found_code)
            codes.add(Classification.UNCLASSIFIED.getCode());
        return codes;
    }

    private void updateCountsForRecord(String asserted_code, String real_code) throws UnknownDataException {

        incrementCount(asserted_code, classification_counts);

        for (String this_code : classification_counts.keySet()) {

            if (truePositive(this_code, asserted_code, real_code)) {
                incrementCount(this_code, true_positive_counts);
            }

            if (trueNegative(this_code, asserted_code, real_code)) {
                incrementCount(this_code, true_negative_counts);
            }

            if (falsePositive(this_code, asserted_code, real_code)) {
                incrementCount(this_code, false_positive_counts);
            }

            if (falseNegative(this_code, asserted_code, real_code)) {
                incrementCount(this_code, false_negative_counts);
            }
        }
    }

    private String findGoldStandardCode(String data) throws UnknownDataException {

        if (gold_standard_data_to_code_map.containsKey(data)) {
            return gold_standard_data_to_code_map.get(data);
        }

        throw new UnknownDataException("couldn't find gold standard code for data: " + data);
    }

    private boolean truePositive(String this_code, String asserted_code, String real_code) {

        // True positive for this code if the record should have been classified as this, and it was.
        return classificationsMatch(this_code, real_code) && classificationsMatch(asserted_code, real_code);
    }

    private boolean trueNegative(String this_code, String asserted_code, String real_code) {

        // True negative for this code if the record shouldn't have been classified as this, and it wasn't.
        return !classificationsMatch(this_code, real_code) && !classificationsMatch(asserted_code, this_code);
    }

    private boolean falsePositive(String this_code, String asserted_code, String real_code) {

        // False positive for this code if the record shouldn't have been classified as this, but it was.
        return !classificationsMatch(this_code, real_code) && classificationsMatch(asserted_code, this_code);
    }

    private boolean falseNegative(String this_code, String asserted_code, String real_code) {

        // False negative for this code if the record should have been classified as this, but it wasn't.
        return classificationsMatch(real_code, this_code) && !classificationsMatch(asserted_code, this_code);
    }

    private void initCount(String code, ConcurrentHashMap<String, AtomicInteger> counts) {

        String truncated_code = makeCodeKey(code);
        counts.computeIfAbsent(truncated_code, key -> new AtomicInteger(0));
    }

    private String makeCodeKey(String code) {

        return code.equals(Classification.UNCLASSIFIED.getCode()) ? code : significantPartOfCode(code);
    }

    private void incrementCount(String code, ConcurrentHashMap<String, AtomicInteger> counts) {

        String truncated_code = makeCodeKey(code);

        counts.compute(truncated_code, (key, value) -> {

            if (value == null) {
                LOGGER.warning(String.format("uninitialised code: %s", truncated_code));
                value = new AtomicInteger(0);
            }
            value.incrementAndGet();
            return value;
        });
    }

    public double averageClassificationsPerRecord() {

        return ((double) total_number_of_classifications) / number_of_records;
    }

    public double actualAverageClassificationsPerRecord() {

        return ((double) total_number_of_gold_standard_classifications) / number_of_records;
    }

    public double proportionOfRecordsWithCorrectNumberOfClassifications() {

        return ((double) total_number_of_records_with_correct_number_of_classifications) / number_of_records;
    }
}
