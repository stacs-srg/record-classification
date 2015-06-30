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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.linear_regression;

import old.record_classification_old.data_readers.AbstractFormatConverter;
import old.record_classification_old.data_readers.LongFormatConverter;
import old.record_classification_old.datastructures.code.CodeNotValidException;
import old.record_classification_old.exceptions.InputFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.InputFileFormatException;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.util.tools.FileManipulation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;


public class BucketGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BucketGenerator.class);

    /** The Abstract Format converter. This is set to be an instance of {@link LongFormatConverter} by default.
     *  This can be changed as and when is necessary in the future.
     */
    private static AbstractFormatConverter formatConverter = new LongFormatConverter();

    private CodeDictionary codeDictionary;

    public BucketGenerator(final CodeDictionary codeDictionary) {

        this.codeDictionary = codeDictionary;
    }

    /**
     * Generates a bucket of training records (with gold standard codes) from the given training file.
     * The file should be either in the short NRS format or in the format the matches the {@link AbstractFormatConverter}
     * specified in the class. Set to {@link LongFormatConverter} as  default.
     *
     * @param trainingFile the training file to generate the records and train the models from
     * @return the bucket that will be populated
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InputFormatException the input format exception
     * @throws CodeNotValidException
     */
    public Bucket generateTrainingBucket(final File trainingFile) throws IOException, InputFormatException, CodeNotValidException, InputFileFormatException {

        // TODO check - used to also pass codeDictionary to Bucket constructor.
        return new Bucket(FileManipulation.getInputStreamReader(Paths.get(trainingFile.getAbsolutePath())));
    }

//    /**
//     * Creates the prediction bucket from the given text file. This method currently expects the data to be in the form of the
//     * pilot data. The source code should be updated if this changes.
//     *
//     * @param prediction file containing the records to be classified, one per line.
//     * @return the bucket containing records to be classified
//     */
//    public Bucket createPredictionBucket(final File prediction) throws CodeNotValidException, IOException, InputFormatException {
//
//        return createPredictionBucket(prediction, new PilotDataFormatConverter());
//    }
//
//    /**
//     * Creates the prediction bucket from the given text file. This method takes an {@link AbstractFormatConverter} and
//     * uses this to perform the record creation.
//     *
//     * @param prediction the file containing the prediction records
//     * @param formatConverter the format converter to create records with
//     * @return the bucket
//     */
//    public Bucket createPredictionBucket(final File prediction, final AbstractFormatConverter formatConverter) throws CodeNotValidException, IOException, InputFormatException {
//
//        return new Bucket(formatConverter.convert(prediction, codeDictionary));
//    }
}