package uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.AnalysisMetrics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.records.Record;
import uk.ac.standrews.cs.digitising_scotland.record_classification.resolver.CodeTriple;
import uk.ac.standrews.cs.digitising_scotland.tools.Utils;

/**
 * Class representing the statistics about a bucket of Records.
 *
 * @author jkc25
 */
public class ListAccuracyMetrics {

    /**
     * The unique records.
     */
    private int uniqueRecords;

    /**
     * The total aggregated records.
     */
    private int totalAggregatedRecords;

    /**
     * The average confidence.
     */
    private double averageConfidence;

    /**
     * The coded from lookup.
     */
    private int numConfidenceOfOne;

    /**
     * The coded by machine learning.
     */
    private int numConfidenceNotOne;

    /** The coded exact match. */
    private int codedExactMatch;

    /**
     * The micro precision.
     */
    private double microPrecision;

    /**
     * The micro recall.
     */
    private double microRecall;

    /**
     * The macro precision.
     */
    private double macroPrecision;

    /**
     * The macro recall.
     */
    private double macroRecall;

    /** The prop gold predicted. */
    private double propGoldPredicted;

    /** The unclassified. */
    private int unclassified;

    /** The single classification. */
    private int singleClassification;

    /** The two classifications. */
    private int twoClassifications;

    /** The more than two classifications. */
    private int moreThanTwoClassifications;

    /** The prop wrongly predicted. */
    private double propWronglyPredicted;

    /** The number of codes not coded. */
    private int[] numberOfCodesNotCoded;

    /** The over under predicion matrix. */
    private int[][] overUnderPredicionMatrix;

    /**
     * Instantiates a new list accuracy metrics.
     *
     * @param listOfRecrods the list of recrods
     */
    public ListAccuracyMetrics(final List<Record> listOfRecrods) {

        this(new Bucket(listOfRecrods));
    }

    /**
     * Instantiates a new list accuracy metrics.
     *
     * @param bucket the bucket of records
     */
    public ListAccuracyMetrics(final Bucket bucket) {

        uniqueRecords = calculateUniqueRecords(bucket);
        totalAggregatedRecords = bucket.size();
        averageConfidence = calculateAverageConfidence(bucket);
        numConfidenceOfOne = calculateNumConfidenceOfOne(bucket);
        numConfidenceNotOne = calculateNumConfidenceNotOne(bucket);
        propGoldPredicted = calculatePropGoldStandardCorrectlyPredicted(bucket);
        codedExactMatch = calculateExactMatch(bucket);
        numberOfCodesNotCoded = calculateBreakDownOfMatches(bucket);
        countNumClassifications(bucket);
        int maxCodes = calculateMaxCodes(bucket);
        overUnderPredicionMatrix = calculateOverPredictionMatrix(bucket, maxCodes);
    }

    /**
     * Calculate over prediction matrix.
     *
     * @param bucket the bucket
     * @param maxCodes the max codes
     * @return the int[][]
     */
    private int[][] calculateOverPredictionMatrix(final Bucket bucket, final int maxCodes) {

        overUnderPredicionMatrix = new int[maxCodes + 1][maxCodes + 1];
        for (Record record : bucket) {
            int goldStandardSize = record.getGoldStandardClassificationSet().size();
            int actualSize = record.getCodeTriples().size();
            overUnderPredicionMatrix[actualSize][goldStandardSize]++;
        }
        return overUnderPredicionMatrix;
    }

    /**
     * Calculate max codes.
     *
     * @param bucket the bucket
     * @return the int
     */
    private int calculateMaxCodes(final Bucket bucket) {

        int maxCodes = 0;
        for (Record record : bucket) {

            if (record.getGoldStandardClassificationSet().size() > maxCodes) {
                maxCodes = record.getGoldStandardClassificationSet().size();
            }
            if (record.getCodeTriples().size() > maxCodes) {
                maxCodes = record.getCodeTriples().size();
            }
        }

        return maxCodes;
    }

    /**
     * Calculate exact match.
     *
     * @param bucket the bucket
     * @return the int
     */
    private int[] calculateBreakDownOfMatches(final Bucket bucket) {

        int[] missedCodesCounter = new int[16];
        for (int i = 0; i < missedCodesCounter.length; i++) {
            missedCodesCounter[i] = 0;
        }

        for (Record record : bucket) {

            Set<CodeTriple> goldStandrdSet = record.getGoldStandardClassificationSet();
            final Set<CodeTriple> codedTriples = record.getCodeTriples();
            int missedCodeCount = 0;
            for (CodeTriple goldTriple : goldStandrdSet) {

                int matchCount = 0;

                for (CodeTriple classification : codedTriples) {
                    if (goldTriple.getCode() == classification.getCode()) {
                        matchCount++;
                    }
                }
                if (matchCount == 0) {
                    missedCodeCount++;
                }
            }

            missedCodesCounter[missedCodeCount]++;
        }

        return missedCodesCounter;
    }

    /**
     * Calculate exact match.
     *
     * @param bucket the bucket
     * @return the int
     */
    private int calculateExactMatch(final Bucket bucket) {

        int exactMatch = 0;

        for (Record record : bucket) {
            final Iterator<CodeTriple> iterator = record.getCodeTriples().iterator();
            double totalConfidence = 0;
            while (iterator.hasNext()) {
                CodeTriple codeTriple = (CodeTriple) iterator.next();
                totalConfidence += codeTriple.getConfidence();
            }

            if ((totalConfidence / (double) record.getCodeTriples().size()) % 2 == 0) {
                exactMatch++;
            }
        }

        return exactMatch;
    }

    /**
     * Prints the statistics generated with pretty formatting.
     */
    public void prettyPrint() {

        System.out.println("Unique records: " + uniqueRecords);
        System.out.println("Total aggregated: " + totalAggregatedRecords);
        System.out.println("Average confidence: " + averageConfidence);
        System.out.println("Total number of classifications: " + (numConfidenceNotOne + numConfidenceOfOne));
        System.out.println("Number of classifications with confidence of 1: " + numConfidenceOfOne);
        System.out.println("Number of classifications with confidence < 1: " + numConfidenceNotOne);
        System.out.println("Proportion of gold standard codes predicted: " + propGoldPredicted);
        System.out.println("Proportion of incorrect gold standard codes predicted: " + propWronglyPredicted);
        System.out.println("Number unclassified: " + unclassified);
        System.out.println("Number coded by exact match: " + codedExactMatch);
        System.out.println("Singly classified: " + singleClassification);
        System.out.println("Doubly classified: " + twoClassifications);
        System.out.println("Multiply classified: " + moreThanTwoClassifications);
        printNumberOfCodesMissed();
        printMatrix("Over/Under Prediction Matrix", overUnderPredicionMatrix);
    }

    /**
     * Prints a matrix.
     *
     * @param message the message to add to the top of the matrix
     * @param matrix the matrix to print
     */
    private String printMatrix(final String message, final int[][] matrix) {

        StringBuilder sb = new StringBuilder();

        sb.append(message + "\n");
        sb.append("   ");
        sb.append("\n" + getMatrixAsString(matrix, "\t", true));
        System.out.println(sb.toString());
        return sb.toString();
    }

    /**
     * Prints a matrix.
     *
     * @param matrix the matrix to print
     */
    public String getMatrixAsString(final int[][] matrix, final String delimiter, final boolean printLabels) {

        StringBuilder sb = new StringBuilder();

        if (printLabels) {
            for (int i = 0; i <= matrix.length; i++) {
                sb.append(i + delimiter);
            }
            sb.append("\n");
        }

        for (int i = 0; i < matrix.length; i++) {
            if (printLabels) {
                sb.append(i + delimiter);
            }

            for (int j = 0; j < matrix.length; j++) {
                sb.append(matrix[i][j]);

                if (j < matrix.length - 1) {
                    sb.append(delimiter);
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Prints the statistics generated with pretty formatting.
     *
     * @param pathToExperiemntFolder the path to experiemnt folder
     * @param pathToGraph the path to graph
     */
    public void generateMarkDownSummary(final String pathToExperiemntFolder, final String pathToGraph) {

        StringBuilder sb = new StringBuilder();
        sb.append("#Classification Report    \n" + "##Summary    \n");
        sb.append("Unique records: " + uniqueRecords + "   \n");
        sb.append("Total aggregated: " + totalAggregatedRecords + "   \n");
        sb.append("Average confidence: " + averageConfidence + "    \n");
        sb.append("Total number of classifications: " + (numConfidenceNotOne + numConfidenceOfOne) + "   \n");
        sb.append("Number of classifications with confidence of 1: " + numConfidenceOfOne + "    \n");
        sb.append("Number of classifications with confidence < 1: " + numConfidenceNotOne + "   \n");
        sb.append("Proportion of gold standard codes predicted: " + propGoldPredicted + "    \n");
        sb.append("Proportion of incorrect gold standard codes predicted: " + propWronglyPredicted + "    \n");
        sb.append("Number unclassified: " + unclassified + "    \n");
        sb.append("Number coded by exact match: " + codedExactMatch + "    \n");
        sb.append("Singly classified: " + singleClassification + "    \n");
        sb.append("Doubly classified: " + twoClassifications + "   \n");
        sb.append("Multiply classified: " + moreThanTwoClassifications + "    \n");
        sb.append(printNumberOfCodesMissed());
        sb.append("Over/Under Matrix    \n");
        sb.append(getMatrixAsString(overUnderPredicionMatrix, "\t", true));
        sb.append("    \n\n");
        sb.append("##Graphs    \n");
        sb.append("![Graph Matrix][" + pathToGraph + "]     \n");
        sb.append("   \n\n");
        sb.append("[" + pathToGraph + "]: " + pathToGraph + ".png \"Graph Matrix\"    \n");
        //sb.append("![Graph](graph.png)");
        Utils.writeToFile(sb.toString(), pathToExperiemntFolder + "/Reports/summary.md", true);
        Utils.writeToFile(getMatrixAsString(overUnderPredicionMatrix, ",", false), pathToExperiemntFolder + "/Data/classificationCountMatrix.csv");

    }

    /**
     * Prints the number of codes missed.
     *
     * @return the string
     */
    private String printNumberOfCodesMissed() {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfCodesNotCoded.length; i++) {
            if (i == 0) {
                sb.append("Number of records coded exactly: " + numberOfCodesNotCoded[i] + "    \n");
            }
            else {
                sb.append("Number of records with " + i + " missed: " + numberOfCodesNotCoded[i] + "    \n");

            }
        }
        sb.append("    ");
        return sb.toString();
    }

    /**
     * Count num classifications.
     *
     * @param bucket the bucket
     */
    private void countNumClassifications(final Bucket bucket) {

        unclassified = 0;
        singleClassification = 0;
        twoClassifications = 0;
        moreThanTwoClassifications = 0;
        for (Record record : bucket) {
            Set<CodeTriple> setCodeTriples = record.getCodeTriples();
            int size = setCodeTriples.size();
            if (size < 1) {
                unclassified++;
            }
            else if (size == 1) {
                singleClassification++;
            }
            else if (size == 2) {
                twoClassifications++;
            }
            else if (size > 2) {
                moreThanTwoClassifications++;
            }
        }
    }

    /**
     * Calculate prop gold standard correctly predicted.
     *
     * @param bucket the bucket
     * @return the double
     */
    private double calculatePropGoldStandardCorrectlyPredicted(final Bucket bucket) {

        double propGoldPredicted = 0.;

        for (Record record : bucket) {
            Set<CodeTriple> setCodeTriples = record.getCodeTriples();
            Set<CodeTriple> goldStandardTriples = record.getGoldStandardClassificationSet();
            if (goldStandardTriples.size() < 1) {
                break;
            }
            int count = 0;
            for (CodeTriple goldTriple : goldStandardTriples) {
                for (CodeTriple classification : setCodeTriples) {
                    if (goldTriple.getCode() == classification.getCode()) {
                        count++;
                    }
                }
            }

            propGoldPredicted += count / (double) goldStandardTriples.size();
        }
        return propGoldPredicted / bucket.size();
    }

    /**
     * Calculate num confidence of one.
     *
     * @param bucket the bucket
     * @return the int
     */
    private int calculateNumConfidenceOfOne(final Bucket bucket) {

        int totallookup = 0;

        for (Record record : bucket) {
            Set<CodeTriple> setCodeTriples = record.getCodeTriples();
            for (CodeTriple classification : setCodeTriples) {
                if (classification.getConfidence() == 1) {
                    totallookup++;
                }

            }
        }

        return totallookup;
    }

    /**
     * Calculate num confidence not one.
     *
     * @param bucket the bucket
     * @return the int
     */
    private int calculateNumConfidenceNotOne(final Bucket bucket) {

        int totalMi = 0;

        for (Record record : bucket) {
            Set<CodeTriple> setCodeTriples = record.getCodeTriples();
            for (CodeTriple classification : setCodeTriples) {
                if (classification.getConfidence() != 1) {
                    totalMi++;
                }

            }
        }

        return totalMi;
    }

    /**
     * Calculate average confidence.
     *
     * @param bucket the bucket
     * @return the double
     */
    private double calculateAverageConfidence(final Bucket bucket) {

        double totalConfidence = 0;
        double totalMeasurments = 0;

        for (Record record : bucket) {
            Set<CodeTriple> setCodeTriples = record.getCodeTriples();
            for (CodeTriple codeTriple : setCodeTriples) {
                totalConfidence += codeTriple.getConfidence();
                totalMeasurments++;

            }
        }

        return totalConfidence / totalMeasurments;
    }

    /**
     * Calculate unique records.
     *
     * @param bucket the bucket
     * @return the int
     */
    private int calculateUniqueRecords(final Bucket bucket) {

        HashMap<Record, Integer> tempMap = new HashMap<>();
        for (Record record : bucket) {
            tempMap.put(record, 0);
        }

        return tempMap.size();
    }

    /**
     * Instantiates a new list accuracy metrics.
     */
    public ListAccuracyMetrics() {

        // TODO Auto-generated constructor stub
    }

    /**
     * Gets the unique records.
     *
     * @return the unique records
     */
    public int getUniqueRecords() {

        return uniqueRecords;
    }

    /**
     * Sets the unique records.
     *
     * @param uniqueRecords the new unique records
     */
    public void setUniqueRecords(final int uniqueRecords) {

        this.uniqueRecords = uniqueRecords;
    }

    /**
     * Gets the total aggregated records.
     *
     * @return the total aggregated records
     */
    public int getTotalAggregatedRecords() {

        return totalAggregatedRecords;
    }

    /**
     * Sets the total aggregated records.
     *
     * @param totalAggregatedRecords the new total aggregated records
     */
    public void setTotalAggregatedRecords(final int totalAggregatedRecords) {

        this.totalAggregatedRecords = totalAggregatedRecords;
    }

    /**
     * Gets the average confidence.
     *
     * @return the average confidence
     */
    public double getAverageConfidence() {

        return averageConfidence;
    }

    /**
     * Sets the average confidence.
     *
     * @param averageConfidence the new average confidence
     */
    public void setAverageConfidence(final int averageConfidence) {

        this.averageConfidence = averageConfidence;
    }

    /**
     * Gets the coded from lookup.
     *
     * @return the coded from lookup
     */
    public int getNumConfidenceOfOne() {

        return numConfidenceOfOne;
    }

    /**
     * Sets the coded from lookup.
     *
     * @param numConfidenceOfOne the new coded from lookup
     */
    public void setNumConfidenceOfOne(final int numConfidenceOfOne) {

        this.numConfidenceOfOne = numConfidenceOfOne;
    }

    /**
     * Gets the coded by machine learning.
     *
     * @return the coded by machine learning
     */
    public int getNumConfidenceNotOne() {

        return numConfidenceNotOne;
    }

    /**
     * Sets the coded by machine learning.
     *
     * @param numConfidenceNotOne the new coded by machine learning
     */
    public void setNumConfidenceNotOne(final int numConfidenceNotOne) {

        this.numConfidenceNotOne = numConfidenceNotOne;
    }

    /**
     * Gets the micro precision.
     *
     * @return the micro precision
     */
    public double getMicroPrecision() {

        return microPrecision;
    }

    /**
     * Sets the micro precision.
     *
     * @param microPrecision the new micro precision
     */
    public void setMicroPrecision(final double microPrecision) {

        this.microPrecision = microPrecision;
    }

    /**
     * Gets the micro recall.
     *
     * @return the micro recall
     */
    public double getMicroRecall() {

        return microRecall;
    }

    /**
     * Sets the micro recall.
     *
     * @param microRecall the new micro recall
     */
    public void setMicroRecall(final double microRecall) {

        this.microRecall = microRecall;
    }

    /**
     * Gets the macro precision.
     *
     * @return the macro precision
     */
    public double getMacroPrecision() {

        return macroPrecision;
    }

    /**
     * Sets the macro precision.
     *
     * @param macroPrecision the new macro precision
     */
    public void setMacroPrecision(final double macroPrecision) {

        this.macroPrecision = macroPrecision;
    }

    /**
     * Gets the macro recall.
     *
     * @return the macro recall
     */
    public double getMacroRecall() {

        return macroRecall;
    }

    /**
     * Sets the macro recall.
     *
     * @param macroRecall the new macro recall
     */
    public void setMacroRecall(final double macroRecall) {

        this.macroRecall = macroRecall;
    }

    /**
     * Gets the coded by sub string match.
     *
     * @return the coded by sub string match
     */
    public int getCodedBySubStringMatch() {

        return codedExactMatch;
    }

    /**
     * Sets the coded by sub string match.
     *
     * @param codedBySubStringMatch the new coded by sub string match
     */
    public void setCodedBySubStringMatch(final int codedBySubStringMatch) {

        this.codedExactMatch = codedBySubStringMatch;
    }
}
