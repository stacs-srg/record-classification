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

import static org.junit.Assert.*;

/**
 * @author Masih Hajiarab Derkani
 */
public class ValidatorsTest {

    @Test
    public void testBetweenZeroToOneInclusive() throws Exception {

        assertTrue(Validators.isBetweenZeroToOneInclusive(0.2));
        assertTrue(Validators.isBetweenZeroToOneInclusive(0.0));
        assertTrue(Validators.isBetweenZeroToOneInclusive(1.0));
        assertTrue(Validators.isBetweenZeroToOneInclusive(0.0 - Validators.DELTA / 2));
        assertTrue(Validators.isBetweenZeroToOneInclusive(1.0 + Validators.DELTA / 2));
        assertFalse(Validators.isBetweenZeroToOneInclusive(0.0 - Validators.DELTA));
        assertFalse(Validators.isBetweenZeroToOneInclusive(1.0 + Validators.DELTA));
    }
}
