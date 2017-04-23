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
package uk.ac.standrews.cs.digitising_scotland.record_classification.cleaning;

/**
 * Removes punctuation characters from text.
 *
 * @author Graham Kirby
 */
public class PunctuationCleaner implements TextCleaner {

    private static final long serialVersionUID = 6585175415850529041L;
    private static final char SPACE = ' ';
    private static final char UNDERSCORE = '_';

    @Override
    public String cleanData(String data) {

        final StringBuilder builder = new StringBuilder();

        for (char c : data.toCharArray()) {

            if (Character.isLetterOrDigit(c) || isAllowablePunctuation(c)) {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    private boolean isAllowablePunctuation(char c) {

        return c == SPACE || c == UNDERSCORE;
    }
}
