package uk.ac.standrews.cs.usp.parser.classifiers;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.standrews.cs.usp.parser.datastructures.Bucket;
import uk.ac.standrews.cs.usp.parser.datastructures.Record;
import uk.ac.standrews.cs.usp.parser.datastructures.RecordFactory;
import uk.ac.standrews.cs.usp.parser.datastructures.TokenSet;
import uk.ac.standrews.cs.usp.parser.datastructures.code.Code;
import uk.ac.standrews.cs.usp.parser.datastructures.code.CodeFactory;
import uk.ac.standrews.cs.usp.parser.datastructures.code.CodeNotValidException;
import uk.ac.standrews.cs.usp.parser.preprocessor.DataCleaning;
import uk.ac.standrews.cs.usp.parser.resolver.CodeTriple;

// TODO: Auto-generated Javadoc
/**
 * The Class ClassifierTestingHelper.
 */
public class ClassifierTestingHelper {

    /** The testing bucket. */
    private Bucket testingBucket;

    /**
     * Instantiates a new classifier testing helper.
     */
    public ClassifierTestingHelper() {

    }

    /**
     * Populates all records in the bucket with the {@link Code} 2100.
     * Use for testing only.
     *
     * @param bucket Bucket to populate
     * @return bucket with gold standard codes
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws CodeNotValidException the code not valid exception
     * @throws URISyntaxException the URI syntax exception
     */
    public Bucket giveBucketTestingOccCodes(final Bucket bucket) throws IOException, CodeNotValidException, URISyntaxException {

        DataCleaning.cleanData(bucket);

        for (Record record : bucket) {
            loadDictionary("/CodeFactoryTestFile.txt");
            record = addGoldStandardCodeToRecord(record, "2200");
        }

        return bucket;
    }

    /**
     * Give bucket testing cod codes.
     *
     * @param bucket the bucket
     * @return the bucket
     * @throws URISyntaxException the URI syntax exception
     */
    public Bucket giveBucketTestingCODCodes(final Bucket bucket) throws URISyntaxException {

        for (Record record : bucket) {
            Set<CodeTriple> codeTriples = new HashSet<CodeTriple>();
            loadDictionary("/CodeFactoryCoDFile.txt");
            addCodeTriplesStandardCodeToRecord(record, "R99");
            record.addAllCodeTriples(codeTriples);
        }

        return bucket;
    }

    /**
     * Give bucket testing hicod codes.
     *
     * @param bucket the bucket
     * @param code 
     * @return the bucket
     * @throws URISyntaxException the URI syntax exception
     */
    public Bucket giveBucketTestingHICODCodes(final Bucket bucket, String code) throws URISyntaxException {

        for (Record record : bucket) {
            Set<CodeTriple> codeTriples = new HashSet<CodeTriple>();
            loadDictionary("/CodeFactoryCoDFile.txt");
            addCodeTriplesStandardCodeToRecord(record, code);
            record.addAllCodeTriples(codeTriples);
        }

        return bucket;
    }

    //====================================================================================================

    /**
     * Load dictionary.
     *
     * @param codeDictionaryFile the code dictionary file
     * @throws URISyntaxException the URI syntax exception
     */
    private void loadDictionary(final String codeDictionaryFile) throws URISyntaxException {

        File file = new File(this.getClass().getResource(codeDictionaryFile).toURI());
        CodeFactory.getInstance().loadDictionary(file);
    }

    /**
     * Adds the gold standard code to classification set.
     *
     * @param record the record
     * @param Set<CodeTriple> the classification set
     * @param goldStandardCode the gold standard code
     */
    private Record addGoldStandardCodeToRecord(final Record record, final String goldStandardCode) {

        Code code = CodeFactory.getInstance().getCode(goldStandardCode);
        CodeTriple c = new CodeTriple(code, new TokenSet(record.getOriginalData().getDescription()), 1.0);
        Set<CodeTriple> set = new HashSet<>();
        set.add(c);
        record.getOriginalData().setGoldStandardClassification(set);
        return record;
    }

    /**
     * Adds the gold standard code to classification set.
     *
     * @param record the record
     * @param Set<CodeTriple> the classification set
     * @param code the gold standard code
     */
    private Record addCodeTriplesStandardCodeToRecord(final Record record, final String codeAsString) {

        Code code = CodeFactory.getInstance().getCode(codeAsString);
        CodeTriple c = new CodeTriple(code, new TokenSet(record.getOriginalData().getDescription()), 1.0);
        Set<CodeTriple> set = new HashSet<>();
        set.add(c);
        record.addCodeTriples(c);
        return record;
    }

    /**
     * Gets the testing bucket.
     *
     * @return the testing bucket
     */
    public Bucket getTestingBucket() {

        return testingBucket;
    }

    /**
     * Sets the testing bucket.
     *
     * @param testingBucket the new testing bucket
     */
    public void setTestingBucket(final Bucket testingBucket) {

        this.testingBucket = testingBucket;
    }

    /**
     * Gets the training bucket.
     *
     * @param fileName the file name
     * @return the training bucket
     * @throws Exception the exception
     */
    public Bucket getTrainingBucket(final String fileName) throws Exception {

        Bucket bucketB;
        File inputFileTraining = new File(getClass().getResource(fileName).getFile());
        List<Record> listOfRecordsTraining = RecordFactory.makeUnCodedRecordsFromFile(inputFileTraining);
        bucketB = new Bucket(listOfRecordsTraining);
        // bucketB = giveBucketTestingOccCodes(bucketB);
        return bucketB;
    }

}
