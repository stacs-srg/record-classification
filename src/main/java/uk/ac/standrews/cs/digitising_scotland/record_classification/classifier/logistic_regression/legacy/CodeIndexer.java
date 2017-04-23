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

import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Record;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents all the outputs codes that are being used in the OLR models.
 * As the size of the OLR models depend on the number of output classes it was deemed undesirable to construct models
 * that contained codes that are never seen in the training data. To avoid this, this class should hold all the output classes
 * that are actually used.
 *
 * @author jkc25, frjd2
 */
final class CodeIndexer implements Serializable {

    private static final long serialVersionUID = 3073583599428985116L;

    /**
     * Maps UID's to codes.
     */
    private Map<Integer, String> idToCodeMap = new HashMap<>();

    /**
     * Maps code to their UIDs.
     */
    private Map<String, Integer> codeToIDMap = new HashMap<>();

    /**
     * The current max id.
     */
    private int currentMaxID;


    /**
     * Required for JSON deserialization.
     */
    public CodeIndexer() {}

    /**
     * Instantiates a new CodeIndexer with all the codes from the supplied bucket added to the index.
     *
     * @param records The bucket to add the codes from
     */
    public CodeIndexer(final Iterable<Record> records) {

        addGoldStandardCodes(records);
    }

    /**
     * Adds gold standard codes from each record to the {@link CodeIndexer}.
     *
     * @param records records with gold standard codes
     */
    public void addGoldStandardCodes(final Iterable<Record> records) {

        for (Record record : records) {
            putCodeInMap(record.getClassification().getCode());
        }
    }

    protected int codeMapSize() {

        return idToCodeMap.size();
    }

    /**
     * Returns the code that this id is mapped to.
     *
     * @param id associated with mapped code
     * @return Code associated with this id
     */
    public String getCode(final Integer id) {

        return idToCodeMap.get(id);
    }

    /**
     * Returns the ID that this code is mapped to.
     *
     * @param code associated with mapped id
     * @return ID associated with this code
     */
    public Integer getID(final String code) {

        return codeToIDMap.get(code);
    }

    /**
     * Returns the total number of output classes based on the size of the code map.
     *
     * @return the number of output classes in the codeMap.
     */
    public int getNumberOfOutputClasses() {

        return codeToIDMap.size();
    }

    /**
     * Puts a code in the map after checking that it's valid by using the {@link CodeDictionary}.
     *
     * @param code the code to add to the map
     */
    private void putCodeInMap(final String code) {

        if (!codeToIDMap.containsKey(code)) {
            createCodeAndAddToMaps(code);
        }
    }

    private void createCodeAndAddToMaps(final String code) {

        idToCodeMap.put(currentMaxID, code);
        codeToIDMap.put(code, currentMaxID);
        currentMaxID++;
    }

    @Override
    public String toString() {

        return "CodeIndexer [idToCodeMap=" + idToCodeMap + ", codeToIDMap=" + codeToIDMap + ", currentMaxID=" + currentMaxID + "]";
    }

}
