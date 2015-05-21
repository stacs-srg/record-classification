package uk.ac.standrews.cs.digitising_scotland.record_classification_cleaned.pipeline;

import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.bucket.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.vectors.CodeIndexer;
import uk.ac.standrews.cs.digitising_scotland.record_classification_cleaned.*;

import java.io.IOException;

public class ClassificationMetrics {

    private Bucket2 classified_evaluation_records;
    private Bucket2 gold_standard_records;

    public ClassificationMetrics(Bucket2 classified_evaluation_records, Bucket2 gold_standard_records) {

        this.classified_evaluation_records = classified_evaluation_records;
        this.gold_standard_records = gold_standard_records;
    }

    public void printMetrics() throws InvalidCodeException, InconsistentCodingException, UnknownDataException {

        System.out.println("number of classified records: " + classified_evaluation_records.size());
        System.out.println("number of gold standard records: " + gold_standard_records.size());

        ConfusionMatrix confusion_matrix = new StrictConfusionMatrix2(classified_evaluation_records, gold_standard_records);
    }

    private void printAllStats(final String experimentalFolderName, final CodeIndexer codeIndex, final Bucket bucket, final String identifier) throws IOException {

//        final Bucket uniqueRecordsOnly = BucketFilter.uniqueRecordsOnly(bucket);
//
//        LOGGER.info("All Records");
//        LOGGER.info("All Records Bucket Size: " + bucket.size());
//        CodeMetrics codeMetrics = new CodeMetrics(new StrictConfusionMatrix(bucket, codeIndex), codeIndex);
//        ListAccuracyMetrics accuracyMetrics = new ListAccuracyMetrics(bucket, codeMetrics);
//        MetricsWriter metricsWriter = new MetricsWriter(accuracyMetrics, experimentalFolderName, codeIndex);
//        metricsWriter.write(identifier, "nonUniqueRecords");
//        accuracyMetrics.prettyPrint("AllRecords");
//
//        LOGGER.info("Unique Only");
//        LOGGER.info("Unique Only  Bucket Size: " + uniqueRecordsOnly.size());
//
//        CodeMetrics codeMetrics1 = new CodeMetrics(new StrictConfusionMatrix(uniqueRecordsOnly, codeIndex), codeIndex);
//        accuracyMetrics = new ListAccuracyMetrics(uniqueRecordsOnly, codeMetrics1);
//        accuracyMetrics.prettyPrint("Unique Only");
//        metricsWriter = new MetricsWriter(accuracyMetrics, experimentalFolderName, codeIndex);
//        metricsWriter.write(identifier, "uniqueRecords");
//        accuracyMetrics.prettyPrint("UniqueRecords");
    }
}
