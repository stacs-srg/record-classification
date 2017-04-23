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

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

/**
 * Utility class used to validate command-line parameters.
 *
 * @author Masih Hajiarab Derkani
 */
public final class Validators {

    public static final double DELTA = 0.001;

    private Validators() { throw new UnsupportedOperationException(); }

    private static void throwParameterMustBeAtLeast(final String parameter_name, Number least) {throw new ParameterException("The value  of parameter " + parameter_name + " must be at least " + least);}

    public static boolean isBetweenZeroToOneInclusive(double value) {

        // Use DELTA to avoid rounding-error-prone exact comparison of doubles.
        return value > -DELTA && value < 1.0 + DELTA;
    }

    public static class AtLeastZero implements IValueValidator<Integer> {

        @Override
        public void validate(final String name, final Integer value) throws ParameterException {

            if (value < 0) {
                throwParameterMustBeAtLeast(name, 0);
            }
        }
    }

    public static class AtLeastOne implements IValueValidator<Integer> {

        @Override
        public void validate(final String name, final Integer value) throws ParameterException {

            if (value < 1) {
                throwParameterMustBeAtLeast(name, 1);
            }
        }
    }

    public static class BetweenZeroToOneInclusive implements IValueValidator<Number> {

        @Override
        public void validate(final String name, final Number value) throws ParameterException {

            if (!isBetweenZeroToOneInclusive(value.doubleValue())) {
                throw new ParameterException(String.format("The value of parameter %s must be between 0.0 and 1.0 inclusive", name));
            }
        }
    }
}
