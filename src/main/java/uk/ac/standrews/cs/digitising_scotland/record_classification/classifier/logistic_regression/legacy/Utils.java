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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.standrews.cs.utilities.FileManipulation;

import java.io.*;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Utility classes related to writing to files and other often used methods.
 *
 * @author jkc25
 * @author fraser dunlop
 */
final class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    /**
     * Handles and exceptions or throwables thrown from threads that are handles by a {@link Future} or {@link ExecutorService}.
     *
     * @param futures Collection of executing futures to handle possible exceptions from.
     * @throws InterruptedException if thread is interrupted
     */
    public static void handlePotentialErrors(final Collection<Future<?>> futures) throws InterruptedException {

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                Throwable rootException = e.getCause();
                if (rootException != null) {
                    LOGGER.error(rootException.toString(), rootException);
                }

            }
        }
    }

    /**
     * Constructs a {@link java.io.BufferedReader} with the default File_Charset specified in {@link FileManipulation}.
     * @param inputFile the file to create the reader for
     * @return BufferedReader for the specified file
     */
    public static BufferedReader createBufferedReader(final File inputFile) {

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile.getAbsolutePath()), FileManipulation.FILE_CHARSET));
        }
        catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return br;

    }
}
