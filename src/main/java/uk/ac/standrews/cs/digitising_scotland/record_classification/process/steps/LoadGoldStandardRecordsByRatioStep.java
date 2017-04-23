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

import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.util.Validators;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.ClassificationContext;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.Step;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Adds a randomly selected ratio of records from a gold standard to the training records, and adds the remaining to the evaluation records of a given {@link ClassificationContext context}.
 *
 * @author Graham Kirby
 * @author Masih Hajiarab Derkani
 */
public class LoadGoldStandardRecordsByRatioStep implements Step {

    private static final long serialVersionUID = 6192497012225048336L;

    private final Path path;
    private final double training_ratio;
    private final Charset charset;
    private final String delimiter;

    /**
     * Instantiates a new step which randomly selects a ratio of records from the gold standard as the training records, and the remaining as the evaluation records in the {@link ClassificationContext context} of a classification process.
     *
     * @param training_ratio the proportion of gold standard records to be used for training
     */
    public LoadGoldStandardRecordsByRatioStep(Path path, double training_ratio) {

        this(path, training_ratio, LoadStep.DEFAULT_CHARSET_SUPPLIER.get(), LoadStep.DEFAULT_DELIMITER);
    }

    public LoadGoldStandardRecordsByRatioStep(Path path, double training_ratio, Charset charset, String delimiter) {

        validateRatio(training_ratio);

        this.path = path;
        this.training_ratio = training_ratio;
        this.charset = charset;
        this.delimiter = delimiter;
    }

    @Override
    public void perform(final ClassificationContext context) {

        final Bucket gold_standard_records = loadRecords();
        context.addGoldStandardRecords(gold_standard_records, training_ratio);
    }

    protected Bucket loadRecords() {

        final Bucket gold_standard_records;
        try (final BufferedReader reader = Files.newBufferedReader(path, charset)) {

            gold_standard_records = new Bucket(reader, delimiter.charAt(0));

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return gold_standard_records;
    }

    private void validateRatio(final double ratio) {

        if (!Validators.isBetweenZeroToOneInclusive(ratio)) {
            throw new IllegalArgumentException("ratio must be within inclusive range of 0.0 to 1.0");
        }
    }
}
