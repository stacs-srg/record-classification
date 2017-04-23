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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.naive_bayes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.SingleClassifier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.TokenList;
import uk.ac.standrews.cs.utilities.FileManipulation;
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

    private static final long serialVersionUID = 5651409456204017931L;
    private static final int NUMBER_OF_ITERATIONS_OVER_DATA_DURING_TRAINING = 3;

    // With JSON serialisation, don't serialise NB object itself; its state is reconstructed from the instances.
    @JsonIgnore
    private NaiveBayesMultinomialText naive_bayes;
    private Instances instances;
    private String single_class;

    @Override
    public void clearModel() {

        naive_bayes = null;
        instances = null;
        single_class = null;
    }

    @Override
    public void trainModel(final Bucket training_records) {

        resetTrainingProgressIndicator(training_records.size() * NUMBER_OF_ITERATIONS_OVER_DATA_DURING_TRAINING);

        // Naive Bayes implementation doesn't work if there's only one class.
        if (countClasses(training_records) == 1) {

            single_class = training_records.getFirstRecord().getClassification().getCode();

        }
        else {

            try {
                // Get training data into form required by Weka by writing it out to an ARFF format file, and loading it in again.
                ArffLoader loader = makeArffLoader(training_records);

                // Load the model structure.
                instances = loader.getStructure();

                // Create the classifier instance.
                constructClassifierIfNecessary();

                // Update the classifier state with the training data.
                updateClassifier(loader);

            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Classification doClassify(final String data) {

        if (modelIsUntrained())
            return Classification.UNCLASSIFIED;

        if (onlyOneClass())
            return new Classification(single_class, new TokenList(data), 1.0, null);

        try {

            // The classifier instance may not exist if this object has recently been deserialized from JSON representation.
            constructClassifierIfNecessary();

            // Create instance with attributes for data and class (latter to be filled in by classifier).
            Instance instance = new DenseInstance(2);

            // Instance needs to know about the attribute structure.
            instance.setDataset(instances);

            // Fill in the data attribute.
            instance.setValue(0, data);

            // Run the classifier.
            double class_index = naive_bayes.classifyInstance(instance);

            // Get the name of the resulting class.
            instance.setClassValue(class_index);
            String class_name = instance.stringValue(1);

            return new Classification(class_name, new TokenList(data), 0.0, null);

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDescription() {

        return "Classifies using Naive Bayes";
    }

    private void constructClassifierIfNecessary() throws Exception {

        instances.setClassIndex(instances.numAttributes() - 1);

        if (naive_bayes == null) {

            naive_bayes = new NaiveBayesMultinomialText();
            naive_bayes.buildClassifier(instances);
        }
    }

    private void updateClassifier(ArffLoader loader) throws Exception {

        Instance current;
        while ((current = loader.getNextInstance(instances)) != null) {
            naive_bayes.updateClassifier(current);
            progressTrainingStep();
        }
    }

    private ArffLoader makeArffLoader(Bucket bucket) throws IOException {

        File f = Files.createTempFile("naive_bayes_", ".arff").toFile();

        ArffLoader loader = new ArffLoader();
        writeBucketToArffFile(bucket, f);
        loader.setFile(f);
        return loader;
    }

    private void writeBucketToArffFile(Bucket bucket, File f) throws IOException {

        try (PrintStream print_stream = new PrintStream(f, FileManipulation.FILE_CHARSET.name())) {

            print_stream.println("@relation bucket\n");

            Set<String> class_names = new HashSet<>();

            for (Record record : bucket) {
                class_names.add(record.getClassification().getCode());
                progressTrainingStep();
            }

            print_stream.println("@attribute data string");

            print_stream.print("@attribute class {");
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

    private int countClasses(Bucket bucket) {

        Set<String> classes = new HashSet<>();

        for (Record record : bucket) {
            classes.add(record.getClassification().getCode());
            progressTrainingStep();
        }

        return classes.size();
    }

    private boolean modelIsUntrained() {

        return instances == null && single_class == null;
    }

    private boolean onlyOneClass() {

        return single_class != null;
    }
}
