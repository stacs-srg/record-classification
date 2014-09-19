package uk.ac.standrews.cs.digitising_scotland.record_classification.classifiers.lookup;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.bucket.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.code.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.records.Record;
import uk.ac.standrews.cs.digitising_scotland.record_classification.datastructures.records.RecordFactory;

/**
 * Test class to test {@link ExactMatchClassifier}.
 * @author jkc25
 *
 */
public class ExactMatchClassifierTest {

    /** The training bucket. */
    private Bucket trainingBucket;

    /** The testing bucket. */
    private Bucket testingBucket;

    /** The exact match classifier. */
    private ExactMatchClassifier exactMatchClassifier;


    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {

        trainingBucket = getTrainingBucket();
        testingBucket = createTestingBucket();
        train();

    }

    @After
    public void tearDown() {

        FileUtils.deleteQuietly(new File("target/lookupTable.ser"));
        FileUtils.deleteQuietly(new File("target/exactmatchlookuptable.ser"));

    }

    /**
     * Test train.
     *
     * @throws Exception the exception
     */
    @Test
    public void testTrain() throws Exception {

        train();
        Assert.assertNotNull(exactMatchClassifier.toString());

    }

    /**
     * Test classify record.
     *
     * @throws Exception the exception
     */
    @Test
    public void testClassifyRecord() throws Exception {

        ExactMatchClassifier exactMatchClassifer = train();
        for (Record record : testingBucket) {
            Record classifiedRecord = exactMatchClassifer.classify(record);
            Set<Classification> codeTriples = classifiedRecord.getCodeTriples();
            if (!codeTriples.isEmpty()) {
                for (Classification codeTriple : codeTriples) {
                    Assert.assertEquals(codeTriple.getConfidence(), 1.0, 0.01);

                }
            }
        }

    }

    /**
     * Test classify bucket.
     *
     * @throws Exception the exception
     */
    @Test
    public void testClassifyBucket() throws Exception {

        ExactMatchClassifier exactMatchClassifer = train();
        Bucket classifiedBucket = exactMatchClassifer.classify(testingBucket);

        for (Record record : classifiedBucket) {
            Set<Classification> codeTriples = record.getCodeTriples();
            if (!codeTriples.isEmpty()) {
                for (Classification codeTriple : codeTriples) {
                    Assert.assertEquals(codeTriple.getConfidence(), 1.0, 0.01);

                }
            }
        }
    }

    /**
     * Train.
     *
     * @return the exact match classifier
     * @throws Exception the exception
     */
    private ExactMatchClassifier train() throws Exception {

        exactMatchClassifier = new ExactMatchClassifier();
        exactMatchClassifier.train(trainingBucket);
        return exactMatchClassifier;
    }

    /**
     * Creates the testing bucket.
     *
     * @return the bucket
     * @throws Exception the exception
     */
    private Bucket createTestingBucket() throws Exception {

        Bucket trainingBucket;
        File inputFileTraining = new File(getClass().getResource("/occupationTestFormatPipeTesting.txt").getFile());
        List<Record> listOfRecordsTraining = RecordFactory.makeUnCodedRecordsFromFile(inputFileTraining);
        trainingBucket = new Bucket(listOfRecordsTraining);

        return trainingBucket;
    }

    /**
     * Gets the training bucket.
     *
     * @return the training bucket
     * @throws Exception the exception
     */
    public Bucket getTrainingBucket() throws Exception {

        Bucket trainingBucket;
        File inputFileTraining = new File(getClass().getResource("/occupationTestFormatPipe.txt").getFile());
        List<Record> listOfRecordsTraining = RecordFactory.makeUnCodedRecordsFromFile(inputFileTraining);
        trainingBucket = new Bucket(listOfRecordsTraining);

        return trainingBucket;
    }

    /**
     * Serialization write test.
     *
     * @throws Exception the exception
     */
    @Test
    public void serializationWriteTest() throws Exception {

        exactMatchClassifier = train();
        exactMatchClassifier.writeModel("target/exactmatchlookuptable");
        Assert.assertTrue(new File("target/exactmatchlookuptable.ser").exists());
    }

    /**
     * Serialization read test.
     *
     * @throws Exception the exception
     */
    @Test
    public void serializationReadTest() throws Exception {

        exactMatchClassifier = train();
        exactMatchClassifier.writeModel("target/exactmatchlookuptable");
        Assert.assertTrue(new File("target/exactmatchlookuptable.ser").exists());
        ExactMatchClassifier newMatcher = new ExactMatchClassifier();
        newMatcher.readModel("target/exactmatchlookuptable");
        Assert.assertEquals(exactMatchClassifier, newMatcher);
    }

}
