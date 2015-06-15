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
package old.record_classification_old.classifiers.olr;

import old.record_classification_old.datastructures.code.Code;
import old.record_classification_old.datastructures.code.CodeDictionary;
import old.record_classification_old.datastructures.code.CodeNotValidException;
import old.record_classification_old.datastructures.vectors.CodeIndexer;
import old.record_classification_old.datastructures.vectors.VectorFactory;
import old.record_classification_old.tools.configuration.MachineLearningConfiguration;
import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.Vector;
import org.junit.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;


//FIXME
@Ignore
public class OLRShuffledTest {

    /** The properties. */
    private Properties properties = MachineLearningConfiguration.getDefaultProperties();

    /** The vector factory. */
    private VectorFactory vectorFactory;

    /** The training vector list. */
    private ArrayList<NamedVector> trainingVectorList = new ArrayList<NamedVector>();

    /** The model. */
    private OLRShuffled model;

    private CodeDictionary cd;
    private CodeIndexer index;

    /**
     * Setup.
     *
     * @throws Exception the exception
     */
    @Before
    public void setup() throws Exception, CodeNotValidException {

        String codeDictionary = getClass().getResource("/CodeFactoryOLRTestFile.txt").getFile();

        cd = new CodeDictionary(new File(codeDictionary));
        index = new CodeIndexer(cd);
        vectorFactory = new VectorFactory();
        properties.setProperty("numCategories", "5");
        trainingVectorList = generateTrainingVectors();
        model = new OLRShuffled(properties, trainingVectorList);
        model.run();
    }

    @After
    public void tearDown() {

        if (!new File("target/testOLRShuffledWrite.txt").delete()) {
            System.err.println("Could not clean up all resources.");
        }
    }

    /**
     * Test classify.
     *
     * @throws Exception the exception
     */
    @Test
    public void testClassify() throws Exception {

        BufferedReader br = getBufferedReaderOfCodeDictionaryFile();
        String line;
        while ((line = br.readLine()) != null) {
            testClassifyWithCodeAsDescription(model, line);
        }
    }

    /**
     * Test default properties constructor.
     */
    @Test
    public void testDefaultPropertiesConstructor() {

        OLRShuffled olr = new OLRShuffled(trainingVectorList);
        olr.run();
    }

    /**
     * Test write.
     *
     * @throws Exception the exception
     */
    @Test
    public void testWrite() throws Exception {

        String filename = "target/testOLRShuffledWrite.txt";
        FileOutputStream fileOutputStream = new FileOutputStream(filename);
        ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
        outputStream.writeObject(model);

        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(filename));

        OLRShuffled olrShuffled = (OLRShuffled) inputStream.readObject();

        //OLRShuffled olrShuffled = OLRShuffled.deSerializeModel(filename);

        BufferedReader br = getBufferedReaderOfCodeDictionaryFile();
        String line;
        while ((line = br.readLine()) != null) {
            testClassifyWithCodeAsDescription(olrShuffled, line);
        }

        outputStream.close();
        inputStream.close();
    }

    //TODO test throwing this error when trying to classify
    //    @Test(expected=UnsupportedOperationException.class)
    //    public void testWriteAndTrainFail() throws Exception {
    //        model.serializeModel("target/testOLRShuffledWrite.txt");
    //        OLRShuffled olrShuffled = OLRShuffled.deSerializeModel("target/testOLRShuffledWrite.txt");
    //
    //        BufferedReader br = getBufferedReaderOfCodeDictionaryFile();
    //        String line;
    //        while ((line = br.readLine()) != null) {
    //            testClassifyWithCodeAsDescription(olrShuffled, line);
    //        }
    //
    //        olrShuffled.
    //    }

    /**
     * Test classify with code as description.
     *
     * @param model the model
     * @param line the line
     */
    private void testClassifyWithCodeAsDescription(final OLRShuffled model, final String line) {

        String codeFromFile = getCodeFromLine(line);
        Vector testVector = vectorFactory.createVectorFromString(codeFromFile);
        int id = getCodeID(codeFromFile);
        int classification = getClassification(model, testVector);
        Assert.assertEquals(id, classification);
    }

    /**
     * Gets the code id.
     *
     * @param codeFromFile the code from file
     * @return the code id
     */
    private int getCodeID(final String codeFromFile) {

        try {
            return index.getID(cd.getCode(codeFromFile));
        }
        catch (CodeNotValidException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Gets the classification.
     *
     * @param model the model
     * @param testVector the test vector
     * @return the classification
     */
    private int getClassification(final OLRShuffled model, final Vector testVector) {

        return model.classifyFull(testVector).maxValueIndex();
    }

    /**
     * Gets the code from line.
     *
     * @param line the line
     * @return the code from line
     */
    private String getCodeFromLine(final String line) {

        String[] splitLine = line.split("\t");
        return splitLine[0].trim();
    }

    /**
     * Gets the buffered reader of code dictionary file.
     *
     * @return the buffered reader of code dictionary file
     * @throws FileNotFoundException the file not found exception
     */
    private BufferedReader getBufferedReaderOfCodeDictionaryFile() throws FileNotFoundException {

        File file = new File(getClass().getResource("/CodeFactoryOLRTestFile.txt").getFile());
        return new BufferedReader(new FileReader(file));
    }

    /**
     * Generate training vectors.
     *
     * @return the array list
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws CodeNotValidException 
     */
    private ArrayList<NamedVector> generateTrainingVectors() throws IOException, CodeNotValidException {

        populateDictionary();
        BufferedReader br = getBufferedReaderOfCodeDictionaryFile();
        String line;
        ArrayList<NamedVector> trainingVectorList = new ArrayList<NamedVector>();
        while ((line = br.readLine()) != null) {
            trainingVectorList.add(createTrainingVector(line));
        }
        return trainingVectorList;
    }

    /**
     * Populate dictionary.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void populateDictionary() throws IOException {

        BufferedReader br = getBufferedReaderOfCodeDictionaryFile();
        String line;
        while ((line = br.readLine()) != null) {
            vectorFactory.updateDictionary(line.split("\t")[1]);
        }

    }

    /**
     * Creates the training vector.
     *
     * @param line the line
     * @return the named vector
     * @throws CodeNotValidException 
     */
    private NamedVector createTrainingVector(final String line) throws CodeNotValidException {

        String[] splitLine = line.split("\t");
        String codeFromFile = splitLine[0].trim();
        String descriptionFromFile = splitLine[1].trim();
        final Code code = cd.getCode(codeFromFile);
        int id = index.getID(code);
        return vectorFactory.createNamedVectorFromString(descriptionFromFile, String.valueOf(id));
    }

}