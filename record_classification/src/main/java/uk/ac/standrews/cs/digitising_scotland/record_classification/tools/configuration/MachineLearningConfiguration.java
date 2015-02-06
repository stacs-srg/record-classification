/*
 * Copyright 2014 Digitising Scotland project:
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
package uk.ac.standrews.cs.digitising_scotland.record_classification.tools.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Machine learning parameters are held in this class.
 * The default machine learning properties file is held in target/classes/machineLearning.default.properties
 * @author jkc25
 *
 */
public class MachineLearningConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MachineLearningConfiguration.class);

    private static Properties defaultProperties = populateDefaults();

    /**
     * Returns the {@link Properties} containing the default machine learning configuration data.
     * @return machineLearningProperties
     */
    public static Properties getDefaultProperties() {

        return defaultProperties;
    }

    /**
     * Extends the default properties with a custom properties file.
     * 
     * @param customPropertiesFile String location of custom properties file
     * @return {@link Properties} file with custom settings backed by the default properties
     */
    public Properties extendDefaultProperties(final String customPropertiesFile) {

        Properties machineLearningProperties = new Properties(defaultProperties);

        try {
            ClassLoader classLoader = MachineLearningConfiguration.class.getClassLoader();
            InputStream resourceAsStream = classLoader.getResourceAsStream(customPropertiesFile);
            machineLearningProperties.load(resourceAsStream);
        }
        catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return machineLearningProperties;
    }

    /**
     * Reads the default properties file.
     * @return default properties
     */
    private static Properties populateDefaults() {

        Properties defaultProperties = new Properties();
        String machineLearningDefault = "machineLearning.default.properties";

        try {
            ClassLoader classLoader = MachineLearningConfiguration.class.getClassLoader();
            InputStream resourceAsStream = classLoader.getResourceAsStream(machineLearningDefault);
            defaultProperties.load(resourceAsStream);
        }
        catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return defaultProperties;
    }

    /**
     * Reads the default properties file.
     * @return default properties
     */
    public static Properties loadProperties(File pathToProperties) {

        Properties newProperties = new Properties();

        try {
            ClassLoader classLoader = MachineLearningConfiguration.class.getClassLoader();
            final String absolutePath = pathToProperties.getAbsolutePath();
            System.out.println(absolutePath);
            InputStream resourceAsStream = new FileInputStream(pathToProperties);
            newProperties.load(resourceAsStream);
            defaultProperties = newProperties;
        }
        catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return newProperties;
    }

}