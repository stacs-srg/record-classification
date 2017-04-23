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
package uk.ac.standrews.cs.digitising_scotland.record_classification.process.steps;

import uk.ac.standrews.cs.digitising_scotland.record_classification.model.Bucket;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.CharsetSupplier;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.ClassificationContext;
import uk.ac.standrews.cs.digitising_scotland.record_classification.process.Step;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads gold standard records from a file into a classification process {@link ClassificationContext context}.
 *
 * @author Masih Hajiarab Derkani
 */
public abstract class LoadStep implements Step {

    private static final long serialVersionUID = 774282123424314041L;

    protected final Path path;
    protected final Charset charset;
    protected final char delimiter;

    public static final CharsetSupplier DEFAULT_CHARSET_SUPPLIER = CharsetSupplier.UTF_8;

    public static final String DEFAULT_DELIMITER = ",";

    /**
     * Instantiates a new step which loads a gold standard CSV file into a classification process {@link ClassificationContext context}.
     *
     * @param path the file to the CSV file
     */
    public LoadStep(Path path) {

        this(path, DEFAULT_CHARSET_SUPPLIER.get(), DEFAULT_DELIMITER);
    }

    public LoadStep(Path path, Charset charset, String delimiter) {

        this.path = path;
        this.charset = charset;
        this.delimiter = delimiter.charAt(0);
    }

    @Override
    public void perform(final ClassificationContext context)  {

        try (final BufferedReader reader = Files.newBufferedReader(path, charset)) {

            clearRecords(context);
            getRecords(context).add(new Bucket(reader, delimiter));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract Bucket getRecords(ClassificationContext context);

    protected abstract void clearRecords(ClassificationContext context);
}
