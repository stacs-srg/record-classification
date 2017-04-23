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
package uk.ac.standrews.cs.digitising_scotland.record_classification.model;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.function.Function;

/**
 * @author Fraser Dunlop
 * @author Graham Kirby
 */
public class TokenList extends ArrayList<String> implements Serializable {

    private static final long serialVersionUID = 4771078200991926082L;
    private static final String SPACE = " ";

    public static final TokenList UNMODIFIABLE_TOKEN_SET = new TokenList() {

        @Override
        public boolean add(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    };

    private TokenList() {
    }

    /**
     * Instantiates a new token set.
     *
     * @param string the string
     */
    public TokenList(final String string) {

        this(string, Function.identity());
    }

    public TokenList(final String string, Function<TokenStream, TokenStream> filter) {

        super();

        try {
            final TokenStream token_stream = getTokenizer(string);
            TokenStream filtered_stream = filter.apply(token_stream);

            final CharTermAttribute charTermAttribute = filtered_stream.addAttribute(CharTermAttribute.class);

            filtered_stream.reset();

            while (filtered_stream.incrementToken()) {
                add(charTermAttribute.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();
        for (String token : this) {
            if (!(builder.length() == 0)) builder.append(SPACE);
            builder.append(token);
        }
        return builder.toString();
    }

    private StandardTokenizer getTokenizer(final String data) throws IOException {

        StandardTokenizer tokenizer = new StandardTokenizer();
        tokenizer.setReader(new StringReader(data));

        return tokenizer;
    }
}
