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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Jamie Carson
 * @author Graham Kirby
 */
public class MachineLearningConfiguration {




    private static final String DEFAULT_PROPERTIES_FILE_NAME = "machineLearning.default.properties";

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
     * Reads the default properties file.
     * @return default properties
     */
    private static Properties populateDefaults() {

        Properties defaultProperties = new Properties();

        try {
            ClassLoader classLoader = MachineLearningConfiguration.class.getClassLoader();
            InputStream resourceAsStream = classLoader.getResourceAsStream(DEFAULT_PROPERTIES_FILE_NAME);

            defaultProperties.load(resourceAsStream);
        }
        catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return defaultProperties;
    }
}
