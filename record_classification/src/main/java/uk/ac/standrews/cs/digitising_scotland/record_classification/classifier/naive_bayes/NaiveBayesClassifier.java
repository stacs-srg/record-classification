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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.naive_bayes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.SingleClassifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.TokenList;
import uk.ac.standrews.cs.util.tools.FileManipulation;
import weka.classifiers.bayes.NaiveBayesMultinomialText;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

public class NaiveBayesClassifier extends SingleClassifier {

    @JsonIgnore
    private NaiveBayesMultinomialText naive_bayes;
    private Instances structure;

    public Instances getStructure() {
        return structure;
    }

    public void setStructure(Instances structure) {
        this.structure = structure;
    }

    public NaiveBayesClassifier() {

//        naive_bayes = new NaiveBayesMultinomialText();
    }

    @Override
    public void clearModel() {

        naive_bayes = null;
        structure = null;
    }

    public static void main(String[] args) {

        try {

            File f = new File("/Users/graham/Desktop/cambridge_short.arff");

            ArffLoader loader = new ArffLoader();
            loader.setFile(f);

            Instances structure = loader.getStructure();
            structure.setClassIndex(structure.numAttributes() - 1);


            NaiveBayesMultinomialText naive_bayes = new NaiveBayesMultinomialText();
            naive_bayes.buildClassifier(structure);
            Instance current;
            while ((current = loader.getNextInstance(structure)) != null) {
                naive_bayes.updateClassifier(current);
            }

            Instance test_instance = new DenseInstance(2);
            test_instance.setDataset(structure);
            test_instance.setValue(0, "gentleman");
            test_instance.setClassValue(naive_bayes.classifyInstance(test_instance));
            System.out.println(test_instance.stringValue(1));

            test_instance = new DenseInstance(2);
            test_instance.setDataset(structure);
            test_instance.setValue(0, "fish");
            test_instance.setClassValue(naive_bayes.classifyInstance(test_instance));
            System.out.println(test_instance.stringValue(1));

            test_instance = new DenseInstance(2);
            test_instance.setDataset(structure);
            test_instance.setValue(0, "chemist");
            test_instance.setClassValue(naive_bayes.classifyInstance(test_instance));
            System.out.println(test_instance.stringValue(1));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void trainModel(final Bucket bucket) {

        // TODO need special case to deal with only having one class in the training set - falls over.

        try {
            ArffLoader loader = makeArffLoader(bucket);

            structure = loader.getStructure();


            constructClassifierIfNecessary();


            Instance current;
            while ((current = loader.getNextInstance(structure)) != null) {
                naive_bayes.updateClassifier(current);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Classification doClassify(final String data) {


        if (modelIsUntrained()) return Classification.UNCLASSIFIED;

        try {

            constructClassifierIfNecessary();

            // Create instance with attributes for data and class (latter to be filled in by classifier).
            Instance instance = new DenseInstance(2);

            // Instance needs to know about the attribute structure.
            instance.setDataset(structure);

            // Fill in the data attribute.
            instance.setValue(0, data);

            // Run the classifier.
            double class_index = naive_bayes.classifyInstance(instance);

            // Get the name of the resulting class.
            instance.setClassValue(class_index);
            String class_name = instance.stringValue(1);

            return new Classification(class_name, new TokenList(data), 0.0, null);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void constructClassifierIfNecessary() throws Exception {

        structure.setClassIndex(structure.numAttributes() - 1);

        if (naive_bayes == null) {
            naive_bayes = new NaiveBayesMultinomialText();
            naive_bayes.buildClassifier(structure);
        }
    }

    @Override
    public String getDescription() {

        return "Classifies using Naive Bayes";
    }

    private ArffLoader makeArffLoader(Bucket bucket) throws IOException {

        ArffLoader loader = new ArffLoader();
        loader.setFile(makeArffFile(bucket));
        return loader;
    }

    private File makeArffFile(Bucket bucket) throws IOException {

        File f = Files.createTempFile("naive_bayes_", ".arff").toFile();
        writeBucketToArffFile(bucket, f);
        return f;
    }

    private void writeBucketToArffFile(Bucket bucket, File f) throws IOException {

        try (PrintStream print_stream = new PrintStream(f, FileManipulation.FILE_CHARSET.name())) {

            print_stream.println("@relation bucket\n");

            Set<String> class_names = new HashSet<>();

            for (Record record : bucket) {
                class_names.add(record.getClassification().getCode());
            }

            print_stream.println("@attribute att1 string");

            print_stream.print("@attribute att2 {");
            boolean first = true;
            for (String class_name : class_names) {
                if (!first) {
                    print_stream.print(",");
                }
                first = false;
                print_stream.print(class_name);
            }
            print_stream.println("}");

            print_stream.println();
            print_stream.println("@data");

            for (Record record : bucket) {

                print_stream.print("'" + record.getData() + "',");
                print_stream.println(record.getClassification().getCode());
            }
        }
    }

    private boolean modelIsUntrained() {

        return structure == null;
    }
}
