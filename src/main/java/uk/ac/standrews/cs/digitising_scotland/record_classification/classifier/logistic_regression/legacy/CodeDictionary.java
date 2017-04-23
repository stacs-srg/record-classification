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

import org.slf4j.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.exceptions.*;

import java.io.*;
import java.util.*;

/**
 * This class contains a collection of all of the possible valid codes that can be used in training or classification.
 * If a code is not in this class then it is assumed that the code is not correct, ie a possible typo or mistaken entry.
 *
 * @author jkc25, frjd2
 */
class CodeDictionary implements Iterable<String> {

    /**
     * The Constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeDictionary.class);

    // TODO review whether this class is necessary.

    /**
     * Map of codes strings to code descriptions.
     */
    private Set<String> validCodes;

    /**
     * Instantiates a new CodeDictionary.
     *
     * @param codeDictionaryFile the code dictionary file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public CodeDictionary(final File codeDictionaryFile) throws IOException {

        validCodes = new HashSet<>();

        try (BufferedReader br = Utils.createBufferedReader(codeDictionaryFile)) {
            String line;
            while ((line = br.readLine()) != null) {
                parseLineAndAddToMap(line);
            }
        }
    }

    /**
     * Parses the line and add to map.
     *
     * @param line the line
     */
    private void parseLineAndAddToMap(final String line) {

        String[] splitLine = line.split("\t");
        if (splitLine.length == 2) {
            String codeFromFile = splitLine[0].trim();
            String descriptionFromFile = splitLine[1].trim();
            createCodeAndAddToMap(codeFromFile, descriptionFromFile);
        } else if (splitLine.length == 1) {
            String codeFromFile = splitLine[0].trim();
            String descriptionFromFile = "No Description";
            createCodeAndAddToMap(codeFromFile, descriptionFromFile);
        }
    }

    /**
     * Creates the code and add to map.
     *
     * @param codeFromFile        the code from file
     * @param descriptionFromFile the description from file
     */
    private void createCodeAndAddToMap(final String codeFromFile, final String descriptionFromFile) {

        validCodes.add(codeFromFile);
    }

    /**
     * Gets the code object associated with the string representation.
     *
     * @param codeAsString the code as string
     * @return the code object
     */
    public String getCode(final String codeAsString) throws UnknownClassificationException {

        if (!validCodes.contains(codeAsString)) {
            throw new UnknownClassificationException(codeAsString + " is not a valid code");

        }
        return codeAsString;
    }

    /**
     * Gets the total number of codes in the dictionary.
     *
     * @return the total number of codes
     */
    public int getTotalNumberOfCodes() {

        return validCodes.size();
    }

    /**
     * Returns an iterator over the validCode map.
     *
     * @return A set of String, Code entries
     */
    public Iterator<String> iterator() {

        return validCodes.iterator();
    }

    public boolean isValid(final String code) {

        return validCodes.contains(code);
    }
}
