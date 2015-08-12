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
package uk.ac.standrews.cs.digitising_scotland.record_classification.process.config;

import uk.ac.standrews.cs.digitising_scotland.util.ProgressIndicator;

public class Config {

    private static boolean clean_up_files_after_tests = true;
    private static boolean include_ensemble_detail_in_classification_output = false;
    private static ProgressIndicator progress_indicator;

    public static boolean cleanUpFilesAfterTests() {

        return clean_up_files_after_tests;
    }

    /**
     * Set to false if there's a need to inspect the serialized context or classified output after the test.
     * @param clean_up_files_after_tests true if temporary files should be deleted
     */
    public static void setCleanUpFilesAfterTests(boolean clean_up_files_after_tests) {

        Config.clean_up_files_after_tests = clean_up_files_after_tests;
    }
}
