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
import org.junit.runner.*;
import org.junit.runners.*;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Masih Hajirab Derkani
 */
@RunWith(Parameterized.class)
public class ConvertersTest {

    private static final Object[][] GIVEN_ONE_EXPECT_OTHER = {

                    {"", null},

                    {"a", 'a'},

                    {"å", 'å'},

                    {"*", '*'},

                    {"-", '-'},

                    {"\"", '"'},

                    {"'", '\''},

                    {"!", '!'},

                    {"\t", '\t'},

                    {"\n", '\n'},

                    {"a string longer than a character should map to its first character", 'a'},

                    {"get this", 'g'}

    };

    private final String given;
    private final Character expect;
    private Converters.CharacterConverter character_converter;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {

        return Arrays.asList(GIVEN_ONE_EXPECT_OTHER);
    }

    public ConvertersTest(String given, Character expect) {

        this.given = given;
        this.expect = expect;
        character_converter = new Converters.CharacterConverter();
    }

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void givenInputAssertExpected() throws Exception {

        assertEquals(expect, character_converter.convert(given));
    }
}
