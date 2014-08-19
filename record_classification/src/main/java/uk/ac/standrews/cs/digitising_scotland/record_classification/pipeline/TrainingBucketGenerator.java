package uk.ac.standrews.cs.digitising_scotland.record_classification.pipeline;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.standrews.cs.digitising_scotland.record_classification.datareaders.AbstractFormatConverter;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datareaders.LongFormatConverter;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.bucket.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.records.Record;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.records.RecordFactory;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.InputFormatException;

public class TrainingBucketGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrainingBucketGenerator.class);
    private static AbstractFormatConverter formatConverter = new LongFormatConverter();

    public Bucket generate(final File trainingFile) throws IOException, InputFormatException {

        LOGGER.info("********** Generating Training Bucket **********");

        Bucket tempBucketForCodeMapCreation = createBucketOfRecords(trainingFile);

        PipelineUtils.generateActualCodeMappings(tempBucketForCodeMapCreation);

        return createBucketOfRecords(trainingFile);

    }

    private static Bucket createBucketOfRecords(final File training) throws IOException, InputFormatException {

        Bucket bucket = new Bucket();
        Iterable<Record> records;
        boolean longFormat = PipelineUtils.checkFileType(training);

        if (longFormat) {
            records = formatConverter.convert(training);
        }
        else {
            records = RecordFactory.makeCodedRecordsFromFile(training);
        }

        bucket.addCollectionOfRecords(records);

        return bucket;
    }

}
