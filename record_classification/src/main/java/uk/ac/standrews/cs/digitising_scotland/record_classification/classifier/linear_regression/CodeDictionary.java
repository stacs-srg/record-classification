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
package uk.ac.standrews.cs.digitising_scotland.record_classification.classifier.linear_regression;

import old.record_classification_old.datastructures.code.CodeNotValidException;
import old.record_classification_old.tools.ReaderWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class contains a collection of all of the possible valid codes that can be used in training or classification.
 * If a code is not in this class then it is assumed that the code is not correct, ie a possible typo or mistaken entry.
 *
 * @author jkc25, frjd2
 */
public class CodeDictionary implements Iterable<String> {

    /**
     * The Constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeDictionary.class);

    // TODO replace with Map<String, String> or Set<Code>?

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

        try (BufferedReader br = ReaderWriterFactory.createBufferedReader(codeDictionaryFile)) {
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
     * @throws CodeNotValidException the code not valid exception
     */
    public String getCode(final String codeAsString) throws CodeNotValidException {

        if (!validCodes.contains(codeAsString)) {
            LOGGER.error(codeAsString + " is not a valid code", new CodeNotValidException(codeAsString + " is not a valid code"));
            throw new CodeNotValidException(codeAsString + " is not a valid code");

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
