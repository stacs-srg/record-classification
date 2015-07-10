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
package uk.ac.standrews.cs.digitising_scotland.record_classification.process.steps;

import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.processes.generic.ClassificationContext;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.processes.generic.Step;

import java.io.*;
import java.nio.file.*;

/**
 * Loads gold standard records from a file into a classification process {@link ClassificationContext context}.
 *
 * @author Masih Hajiarab Derkani
 */
public class LoadGoldStandardFromFileStep implements Step {

    private static final long serialVersionUID = 7742825393693404041L;
    private final Path path;

    /**
     * Instantiates a new step which loads a gold standard CSV file into a classification process {@link ClassificationContext context}.
     *
     * @param path the file to the CSV file
     */
    public LoadGoldStandardFromFileStep(Path path) {

        this.path = path;
    }

    @Override
    public void perform(final ClassificationContext context)  {

        try (final BufferedReader reader = Files.newBufferedReader(path, Step.DEFAULT_CHARSET)) {

            context.getGoldStandardRecords().add(new Bucket(reader));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    private void writeObject(ObjectOutputStream out) throws IOException {
//
//        out.defaultWriteObject();
//    }
//
//    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
//
//        in.defaultReadObject();
//    }
}
