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
package uk.ac.standrews.cs.digitising_scotland.record_classification.dataset;

import org.apache.commons.io.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

/**
 * @author masih
 */
public class TestResource {

    public static final CharsetSupplier DEFAULT_CHARSET = CharsetSupplier.UTF_8;
    protected final Supplier<Reader> resource_reader;

    public TestResource(Class<?> loader, String resource_name) {

        this(loader, resource_name, DEFAULT_CHARSET.get());
    }

    public TestResource(Class<?> loader, String resource_name, Charset charset) {

        this(toReaderSupplier(loader, resource_name, charset));
    }

    public TestResource(final Supplier<Reader> resource_reader) {

        this.resource_reader = resource_reader;

    }

    public void copy(Path destination) {

        copy(destination, DEFAULT_CHARSET.get());
    }

    public static Supplier<Reader> toReaderSupplier(final Class<?> loader, final String resource_name, Charset charset) {

        return () -> new BufferedReader(new InputStreamReader(loader.getResourceAsStream(resource_name), charset));
    }
    
    public List<String> readLines() throws IOException {

        return IOUtils.readLines(resource_reader.get());
    }

    public void copy(Path destination, Charset charset) {

        try {
            try (
                            final Reader in = resource_reader.get();
                            final BufferedWriter out = Files.newBufferedWriter(destination, charset)
            ) {

                IOUtils.copy(in, out);
                out.flush();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
