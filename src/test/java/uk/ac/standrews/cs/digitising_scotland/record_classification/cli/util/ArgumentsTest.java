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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cli.util;

import org.junit.*;
import org.junit.rules.*;
import uk.ac.standrews.cs.digitising_scotland.record_classification.dataset.*;

import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import static org.junit.Assert.*;

/**
 * @author masih
 */
public class ArgumentsTest {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();
    
    @Test
    public void testCommentRecognition() throws Exception {
        
        assertTrue(Arguments.isComment("# some comment"));
        assertTrue(Arguments.isComment("         # some comment"));
        assertTrue(Arguments.isComment("\t# some comment"));
        assertFalse(Arguments.isComment("not some comment"));
        assertFalse(Arguments.isComment("\tnot some comment"));
        assertFalse(Arguments.isComment("    not some comment"));
    }

    @Test
    public void testCommandParse() throws Exception {

        final Path commands = temp.newFile().toPath();
        final TestResource commands_with_comment = new TestResource(Arguments.class, "test_batch_with_comments.txt");
        commands_with_comment.copy(commands, StandardCharsets.UTF_8);

        final TestResource commands_without_comment = new TestResource(Arguments.class, "test_batch_parsed_one_argument_per_line.txt");
        final List<String> expected = commands_without_comment.readLines();
        
        final List<String[]> actual = Arguments.parseBatchCommandFile(commands, StandardCharsets.UTF_8).collect(Collectors.toList());

        int index = 0;
        for (String[] actual_arguments : actual) {
            for (String actual_argument : actual_arguments) {
                final String expected_argument = expected.get(index++);
                assertEquals(expected_argument, actual_argument);
            }
        }
    }
}
