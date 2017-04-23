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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.logistic_regression.legacy;

import org.apache.mahout.math.NamedVector;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.Vector;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Classification;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.TokenList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Fraser Dunlop
 */
class VectorFactory implements Serializable {

    private static final long serialVersionUID = 5369887941319861994L;

    private CodeIndexer index;

    private SimpleVectorEncoder vectorEncoder;

    /**
     * Needed for JSON deserialization.
     */
    public VectorFactory() {
    }

    public VectorFactory(final Bucket bucket) {

        index = new CodeIndexer(bucket);
        vectorEncoder = new SimpleVectorEncoder();
        updateDictionary(bucket);
    }

    /**
     * Updates the dictionary with the tokens for all the records in the given bucket.
     *
     * @param bucket the bucket
     */
    protected void updateDictionary(final Bucket bucket) {

        for (Record record : bucket) {

            record.getClassification().getTokenList().forEach(vectorEncoder::updateDictionary);
        }
    }

    protected int numberOfDistinctTokens() {

        return vectorEncoder.getDictionarySize();
    }

    protected int numberOfDistinctClassifications() {

        return index.codeMapSize();
    }

    /**
     * Creates a {@link NamedVector} from the cleaned description of a {@link Record}.
     * If a gold standard coding exists this will be used for the name of the vector.
     * If no gold standard then "noGoldStandard" will be used.
     * A List<NamedVector> is returned as the record may have more than one line in the original description.
     *
     * @param record Record to generate vector for.
     * @return List<NamedVector> List of {@link NamedVector} for this record
     */
    protected List<NamedVector> generateVectorsFromRecord(final Record record) {

        List<NamedVector> vectors = new ArrayList<>();

        if (record.getClassification() != Classification.UNCLASSIFIED) {
            vectors.addAll(createNamedVectorsWithGoldStandardCodes(record));
        } else {
            vectors.addAll(createUnNamedVectorsFromDescription(new TokenList(record.getData())));
        }
        return vectors;
    }

    protected NamedVector createNamedVectorFromString(final TokenList token_list, final String name) {

        Vector vector = createVectorFromString(token_list);
        return new NamedVector(vector, name);
    }

    protected CodeIndexer getCodeIndexer() {

        return index;
    }

    private Collection<? extends NamedVector> createUnNamedVectorsFromDescription(final TokenList description) {

        List<NamedVector> vectorList = new ArrayList<>();

        vectorList.add(new NamedVector(createVectorFromString(description), "noGoldStandard"));

        return vectorList;
    }

    private List<NamedVector> createNamedVectorsWithGoldStandardCodes(final Record record) {

        List<NamedVector> vectors = new ArrayList<>();

        Classification codeTriple = record.getClassification();
        Integer id = index.getID(codeTriple.getCode());
        vectors.add(createNamedVectorFromString(codeTriple.getTokenList(), id.toString()));

        return vectors;
    }

    private Vector createVectorFromString(final TokenList description) {

        Vector vector = new RandomAccessSparseVector(numberOfDistinctTokens());
        addFeaturesToVector(vector, description);
        return vector;
    }

    private void addFeaturesToVector(final Vector vector, final TokenList token_list) {

        for (String token : token_list) {
            vectorEncoder.addToVector(token, vector);
        }
    }
}
