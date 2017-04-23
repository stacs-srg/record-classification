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
package uk.ac.standrews.cs.digitising_scotland.record_classification.util;


import uk.ac.standrews.cs.utilities.FileManipulation;
import uk.ac.standrews.cs.utilities.dataset.DataSet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AnonymiseText {

    private static final boolean RANDOMISE = false;
    private static final boolean RESET_IDS = true;

    private static final Map<String, String> anonymised_strings = new HashMap<>();
    private static final Pattern COMPILE = Pattern.compile("\\d");

    public static void main(final String[] args) throws IOException {

        try (
                InputStreamReader inputStreamReader = FileManipulation.getInputStreamReader(Paths.get(args[0]));
                OutputStreamWriter out = FileManipulation.getOutputStreamWriter(Paths.get(args[1]))) {

            final DataSet source_data = new DataSet(inputStreamReader, ',');
            final DataSet anonymised_data = new DataSet(Arrays.asList("id", "data", "code"));

            for (final List<String> record : source_data.getRecords()) {

                @SuppressWarnings("UnusedAssignment")
                String id = record.get(0);
                final String data = record.get(1);
                final String code = record.get(2);

                if (RESET_IDS) id = String.valueOf(id_counter++);

                anonymised_data.addRow(Arrays.asList(id, anonymise(data), anonymise(code)));
            }

            anonymised_data.print(out);
        }
    }

    private static String anonymise(final String s) {

        return anonymised_strings.computeIfAbsent(s, k -> makeAnonymousString());
    }

    private static final SecureRandom random = new SecureRandom();
    private static int string_counter = 1;
    private static int id_counter = 1;

    private static String makeAnonymousString() {

        if (RANDOMISE) {

            return COMPILE.matcher(new BigInteger(130, random).toString(32)).replaceAll(" ");
        } else {
            return "string_" + string_counter++;
        }
    }
}
