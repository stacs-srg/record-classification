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
package uk.ac.standrews.cs.digitising_scotland.record_classification.process.v2;

import org.apache.commons.lang3.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Represents a classification process that consists of a list of steps and produces a {@link Bucket bucket}  of classified records.
 *
 * @author Masih Hajiarab Derkani
 */
public class ClassificationProcess implements Callable<Bucket>, Serializable {

    private static final long serialVersionUID = -3086230162106640193L;

    private final Context context;
    private final List<Step> steps;

    /**
     * Instantiates a new classification process with an empty context.
     */
    public ClassificationProcess() {

        this(new Context());
    }

    /**
     * Instantiates a new classification process.
     *
     * @param context the context in which to classify
     */
    public ClassificationProcess(final Context context) {

        this.context = context;
        steps = new ArrayList<>();
    }

    /**
     * Adds a step to the steps to be performed by this process.
     *
     * @param step the step to be performed in the classification process.
     * @return this classification process to accommodate chaining of step additions.
     */
    public ClassificationProcess addStep(Step step) {

        steps.add(step);
        return this;
    }

    /**
     * Sequentially performs the steps in this classification process.
     *
     * @return the classified records, or {@code null} if no records were classified
     * @throws Exception if an error while performing the process steps
     */
    @Override
    public Bucket call() throws Exception {

        for (Step step : steps) {
            step.perform(context);
        }

        return context.getClassifiedUnseenRecords();
    }

    /**
     * Performs this classification process {@code n} times where {@code n} is the given number of repetitions.
     * The classification process is performed on this process and {@code n-1} of its copies.
     * The copying of this process is done by serialization/deserialization of this process.
     *
     * @param repetitions the number of times to repeat this classification process.
     * @return the list of performed classification processes
     * @throws IllegalArgumentException if the given number of repetitions is less than {@code 1}
     * @throws Exception if an error occurs during the execution of the classification processes
     */
    public List<ClassificationProcess> repeat(int repetitions) throws Exception {

        if (repetitions < 1) {
            throw new IllegalArgumentException("the number of repetitions must be at least one");
        }

        final List<ClassificationProcess> performed_processes = new ArrayList<>();
        call();
        performed_processes.add(this);

        final byte[] serialized_process = SerializationUtils.serialize(this);
        for (int i = 0; i < repetitions - 1; i++) {
            final ClassificationProcess process_copy = (ClassificationProcess) SerializationUtils.deserialize(serialized_process);
            process_copy.call();
            performed_processes.add(process_copy);
        }

        return Collections.unmodifiableList(performed_processes);
    }

    /**
     * Gets the context of this classification process.
     *
     * @return the context of this classification process
     */
    public Context getContext() {

        return context;
    }
}