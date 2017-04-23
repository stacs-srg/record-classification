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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier;

import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;
import uk.ac.standrews.cs.utilities.GenericProgressIndicator;
import uk.ac.standrews.cs.utilities.ProgressIndicator;

import java.io.Serializable;
import java.util.Random;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Basic classifier implementation.
 *
 * @author Graham Kirby
 * @author Masih Hajiarab Derkani
 */
public abstract class Classifier implements Serializable {

    private static final long serialVersionUID = 7015118610311481144L;
    private static final Logger LOGGER = Logger.getLogger(Classifier.class.getName());
    private static final Consumer<Double> DEFAULT_PROGRESS_HANDLER = progress -> LOGGER.info(Math.round(progress * 100) + "%");
    private static final int DEFAULT_NUMBER_OF_PROGRESS_UPDATES = 20;

    private transient ProgressIndicator classification_progress_indicator;
    private transient ProgressIndicator training_progress_indicator;
    private transient Consumer<Double> classification_progress_handler = DEFAULT_PROGRESS_HANDLER;
    private transient Consumer<Double> training_progress_handler = DEFAULT_PROGRESS_HANDLER;

    public void setClassificationProgressHandler(final Consumer<Double> progress_handler) {

        this.classification_progress_handler = progress_handler;
    }

    protected void resetTrainingProgressIndicator(int total) {

        resetTrainingProgressIndicator(total, DEFAULT_NUMBER_OF_PROGRESS_UPDATES);
    }

    protected void resetTrainingProgressIndicator(int total_steps, int updates_count) {

        training_progress_indicator = initProgressIndicator(total_steps, training_progress_handler, updates_count);
    }

    private ProgressIndicator initProgressIndicator(int total_steps, Consumer<Double> progress_handler, int updates_count) {

        final ProgressIndicator progress_indicator = new GenericProgressIndicator(updates_count, progress_handler);
        progress_indicator.setTotalSteps(total_steps);
        return progress_indicator;
    }

    public void setTrainingProgressHandler(final Consumer<Double> progress_handler) {

        this.training_progress_handler = progress_handler;
    }

    /**
     * Trains the classifier on the given gold standard records, and performs internal evaluation.
     *
     * @param bucket the training data
     * @param internal_training_ratio the ratio of gold standard records to be used for training as opposed to internal evaluation
     * @param random a random number generator to use in selecting the records to use for internal evaluation
     */
    public abstract void trainAndEvaluate(final Bucket bucket, final double internal_training_ratio, final Random random);

    /**
     * Classifies a bucket of data items.
     * If a record in the given bucket cannot be classified, its classification is set to {@link Classification#UNCLASSIFIED}.
     *
     * @param bucket the data to be classified
     * @return a new bucket containing the classified data
     */
    public Bucket classify(final Bucket bucket) {

        return classify(bucket, false);
    }

    protected Bucket classify(final Bucket unclassified, boolean set_confidence) {

        final Bucket classified = new Bucket();
        final int unclassified_records_size = unclassified.size();

        resetClassificationProgressIndicator(unclassified_records_size);
        LOGGER.info(() -> String.format("classifying %d records...", unclassified_records_size));
        for (Record record : unclassified) {

            final String data = record.getData();

            Classification classification = classify(data);
            setConfidence(classification, set_confidence);
            classified.add(new Record(record.getId(), data, record.getOriginalData(), classification));

            progressClassificationStep();
        }
        LOGGER.info(() -> "done.");

        return classified;
    }

    protected void resetClassificationProgressIndicator(int total) {

        resetClassificationProgressIndicator(total, DEFAULT_NUMBER_OF_PROGRESS_UPDATES);
    }

    protected void resetClassificationProgressIndicator(int total_steps, int updates_count) {

        classification_progress_indicator = initProgressIndicator(total_steps, classification_progress_handler, updates_count);
    }

    /**
     * Classifies a single data item.
     *
     * @param data the data to be classified
     * @return the resulting classification, or {@link Classification#UNCLASSIFIED} if the data cannot be classified
     */
    public abstract Classification classify(String data);

    protected void setConfidence(Classification classification, boolean set_confidence) {

        // do nothing; this is to allow extending classes to customise confidence measure of a classification.
    }

    protected void progressClassificationStep() {

        if (classification_progress_indicator != null) {
            classification_progress_indicator.progressStep();
        }
    }

    protected void progressTrainingStep() {

        if (training_progress_indicator != null) {
            training_progress_indicator.progressStep();
        }
    }

    public abstract String getName();

    public abstract String getDescription();
}
