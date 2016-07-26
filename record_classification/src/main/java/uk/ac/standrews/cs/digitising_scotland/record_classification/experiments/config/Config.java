/*
 * record_classification - Automatic record attribute classification.
 * Copyright © 2014-2016 Digitising Scotland project
 * (http://digitisingscotland.cs.st-andrews.ac.uk/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.digitising_scotland.record_classification.experiments.config;

import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.*;

public class Config {

    private static boolean clean_up_files_after_tests = true;
    private static double internal_training_ratio = Configuration.DEFAULT_INTERNAL_TRAINING_RATIO;

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

    public static double getInternalTrainingRatio() {

        return internal_training_ratio;
    }

    public static void setInternalTrainingRatio(double internal_training_ratio) {

        Config.internal_training_ratio = internal_training_ratio;
    }
}
