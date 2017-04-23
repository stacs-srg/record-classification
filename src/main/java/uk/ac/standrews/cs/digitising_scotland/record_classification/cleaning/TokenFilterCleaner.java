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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import uk.ac.standrews.cs.digitising_scotland.record_classification.model.TokenList;

/**
 * An abstract cleaner that uses {@link TokenFilter} to clean records.
 *
 * @author Masih Hajiarab Derkani
 * @author Graham Kirby
 */
public abstract class TokenFilterCleaner implements TextCleaner {

    private static final long serialVersionUID = -7341612194148263930L;
    private static final CharSequence SPACE = " ";

    public String cleanData(final String data) {

        final TokenList cleaned_data_tokens = new TokenList(data, this::getTokenFilter);
        return String.join(SPACE, cleaned_data_tokens);
    }

    protected abstract TokenFilter getTokenFilter(final TokenStream stream);
}
