/*
 * | ______________________________________________________________________________________________ | Understanding
 * Scotland's People (USP) project. | | The aim of the project is to produce a linked pedigree for all publicly | |
 * available Scottish birth/death/marriage records from 1855 to the present day. | | | | Digitization of the records is
 * being carried out by the ESRC-funded Digitising | | Scotland project, run by University of St Andrews and National
 * Records of Scotland. | | | | The project is led by Chris Dibben at the Longitudinal Studies Centre at St Andrews. | |
 * The other project members are Lee Williamson (also at the Longitudinal Studies Centre) | | Graham Kirby, Alan Dearle
 * and Jamie Carson at the School of Computer Science at St Andrews; | | and Eilidh Garret and Alice Reid at the
 * Department of Geography at Cambridge. | | | |
 * ______________________________________________________________________________________________
 */
package uk.ac.standrews.cs.usp.tools.analysis;

/**
 * Provides methods to calculate metrics such as true and false negatives and positives on a result set.
 * 
 * @author jkc25
 */
public class AccuracyMetrics {

    /** The Constant ONETHOUSANDD. */
    private static final double ONETHOUSANDD = 1000.0d;

    /** The Constant ONETHOUSAND. */
    private static final double ONETHOUSAND = 1000;

    /** The classification. */
    private String classification;

    /** The precision. */
    private double precision = -1;

    /** The recall. */
    private double recall = -1;

    /** The accuracy. */
    private double accuracy = -1;

    /** The error. */
    private double error = -1.0;

    /** The f1. */
    private double f1 = -1;

    /** The t p. */
    private double tP = -1;

    /** The f p. */
    private double fP = -1;

    /** The f n. */
    private double fN = -1;

    /** The t n. */
    private double tN = -1;

    /** The n. */
    private double n = -1;

    /** The content uniquness. */
    private double contentUniquness = -1;

    /**
     * Returns the true positive.
     * 
     * @return true positive.
     */
    public double getTP() {

        return tP;
    }

    /**
     * Sets the true positive.
     * 
     * @param tP
     *            true positive.
     */
    public void setTP(final double tP) {

        this.tP = tP;
    }

    /**
     * Gets the false positive.
     * 
     * @return false positive.
     */
    public double getFP() {

        return fP;
    }

    /**
     * Sets the false positive.
     * 
     * @param fP
     *            false positive.
     */
    public void setFP(final double fP) {

        this.fP = fP;
    }

    /**
     * Gets the false negative.
     * 
     * @return false negative.
     */
    public double getFN() {

        return fN;
    }

    /**
     * Sets the false negative.
     * 
     * @param fN
     *            false negative.
     */
    public void setFN(final double fN) {

        this.fN = fN;
    }

    /**
     * Gets the true negative.
     * 
     * @return true negative.
     */
    public double getTN() {

        return tN;
    }

    /**
     * Sets the true negative value.
     * 
     * @param tN
     *            value of true negative.
     */
    public void setTN(final double tN) {

        this.tN = tN;
    }

    /**
     * Returns the value of n. n is defined as n = tP + fP + tN + fN.
     * 
     * @return value of n.
     */
    public double getN() {

        n = tP + fP + tN + fN;
        return n;
    }

    /**
     * Sets the value of n. n is defined as n = tP + fP + tN + fN.
     * 
     * @param n
     *            value of n.
     */
    public void setN(final double n) {

        this.n = n;
    }

    /**
     * Returns the classification.
     * 
     * @return classification.
     */
    public String getClassification() {

        return classification;
    }

    /**
     * Returns the precision.
     * 
     * @return the precision.
     */
    public double getPrecision() {

        if (precision < 0) {
            precision = tP / (tP + fP);
        }
        if ((tP + fP) == 0) {
            precision = 0;
        }
        return precision;
    }

    /**
     * Returns the recall.
     * 
     * @return recall value.
     */
    public double getRecall() {

        if (recall < 0) {
            recall = tP / (tP + fN);
        }
        return recall;
    }

    /**
     * Returns the accuracy.
     * 
     * @return accuracy value.
     */
    public double getAccuracy() {

        n = tP + fP + tN + fN;

        if (accuracy < 0) {
            accuracy = (tP + tN) / n;
        }
        return accuracy;
    }

    /**
     * Gets the error.
     * 
     * @return the error value.
     */
    public double getError() {

        if (n == -1) {
            n = tP + fP + tN + fN;
        }
        if (error < 0) {
            error = (fP + fN) / n;
        }
        return error;
    }

    /**
     * Returns the F1 value, defined as f1 = (2 * recall * precision) / (recall + precision).
     * 
     * @return the value of F1.
     */
    public double getF1() {

        f1 = (2 * recall * precision) / (recall + precision);
        return f1;
    }

    /**
     * Creates a new AccuracyMetrics object.
     * 
     * @param classification
     *            the classification we want to know the details for.
     */
    public AccuracyMetrics(final String classification) {

        this.classification = classification;
    }

    /**
     * Overridden toString method to print out the accuracy metrics.
     * 
     * @return String printable string.
     */
    public String toString() {

        String details = " True Positive: " + Math.round(getTP() * ONETHOUSAND) / ONETHOUSANDD + " False Positive: " + Math.round(getFP() * ONETHOUSAND) / ONETHOUSANDD + " False Negative: " + Math.round(getFN() * ONETHOUSAND) / ONETHOUSANDD + " True Negative: " + Math.round(getTN() * ONETHOUSAND)
                        / ONETHOUSANDD + " Precision: " + Math.round(getPrecision() * ONETHOUSAND) / ONETHOUSANDD + " Recall: " + Math.round(getRecall() * ONETHOUSAND) / ONETHOUSANDD + " Accuracy:  " + Math.round(getAccuracy() * ONETHOUSAND) / ONETHOUSANDD + " F1: "
                        + Math.round(getF1() * ONETHOUSAND) / ONETHOUSANDD + " Error: " + Math.round(getError() * ONETHOUSAND) / ONETHOUSANDD + " Total: " + n;
        return details;
    }

    /**
     * Gets the content uniqueness.
     *
     * @return the content uniqueness
     */
    public double getContentUniqueness() {

        return contentUniquness;
    }

    /**
     * Sets the content uniqueness.
     *
     * @param contentUniquness the new content uniquness
     */
    public void setContentUniquness(final double contentUniquness) {

        this.contentUniquness = contentUniquness;
    }

}
