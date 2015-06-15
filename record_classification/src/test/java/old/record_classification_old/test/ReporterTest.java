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
package old.record_classification_old.test;

import old.record_classification_old.tools.analysis.Reporter;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the the Reporter class is working as expected.
 * @author jkc25
 *
 */
@Ignore
public class ReporterTest {

    /**
     * Tests that the singleton nature of the class works as expected.
     */
    @Test
    public void test() {

        Reporter r = Reporter.getInstance();
        Reporter d = Reporter.getInstance();
        assertEquals(r, d);
    }

}