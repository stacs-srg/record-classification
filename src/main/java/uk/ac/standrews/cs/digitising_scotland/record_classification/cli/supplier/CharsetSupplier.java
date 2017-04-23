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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cli.supplier;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * Predefined suppliers of {@link Charset}.
 *
 * @author Graham Kirby
 * @author Masih Hajiarab Derkani
 */
public enum CharsetSupplier implements Supplier<Charset> {

    SYSTEM_DEFAULT(Charset.defaultCharset()),
    ISO_8859_1(StandardCharsets.ISO_8859_1),
    UTF_8(StandardCharsets.UTF_8),
    UTF_16BE(StandardCharsets.UTF_16BE),
    UTF_16LE(StandardCharsets.UTF_16LE),
    UTF_16(StandardCharsets.UTF_16),
    US_ASCII(StandardCharsets.US_ASCII);

    @SuppressWarnings("NonSerializableFieldInSerializableClass")
    private final Charset charset;

    CharsetSupplier(Charset charset) {

        this.charset = charset;
    }

    @Override
    public Charset get() {
        return charset;
    }
}
