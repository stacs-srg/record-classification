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
package old.record_classification_old.datastructures.classification;

import old.record_classification_old.datastructures.code.Code;
import old.record_classification_old.datastructures.tokens.TokenSet;

import java.io.Serializable;

/**
 * This class represents a classification, either gold standard or from a classifier.
 * A classification consists of the following:
 * The class contains 3 variables, the {@link old.record_classification_old.datastructures.code.Code}, the {@link TokenSet} that relates to the code and finally
 * the confidence of that code.
 * <br><br>
 * @author jkc25, frjd2
 */
public class Classification implements Comparable<Classification>, Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 7683621012309471383L;

    /** The code of the classification. */
    private final Code code;

    /** The token set representing the string that relates to the code. */
    private final TokenSet tokenSet;

    /** The confidence of the classification. */
    private final Double confidence;

    /**
     * Instantiates a new code triple.
     *
     * @param code the code
     * @param tokenSet the token set
     * @param confidence the confidence
     */
    public Classification(final Code code, final TokenSet tokenSet, final Double confidence) {

        this.code = code;
        this.tokenSet = tokenSet;
        this.confidence = confidence;
    }

    /**
     * Gets the code.
     *
     * @return the code
     */
    public Code getCode() {

        return code;
    }

    /**
     * Gets the token set.
     *
     * @return the token set
     */
    public TokenSet getTokenSet() {

        return tokenSet;
    }

    /**
     * Gets the confidence.
     *
     * @return the confidence
     */
    public Double getConfidence() {

        return confidence;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        result = prime * result + ((confidence == null) ? 0 : confidence.hashCode());
        result = prime * result + ((tokenSet == null) ? 0 : tokenSet.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        Classification other = (Classification) obj;
        if (code == null) {
            if (other.code != null) { return false; }
        }
        else if (!code.equals(other.code)) { return false; }
        if (confidence == null) {
            if (other.confidence != null) { return false; }
        }
        else if (!confidence.equals(other.confidence)) { return false; }
        if (tokenSet == null) {
            if (other.tokenSet != null) { return false; }
        }
        else if (!tokenSet.equals(other.tokenSet)) { return false; }
        return true;
    }

    @Override
    public String toString() {

        return "CodeTriple [code=" + code + ", tokenSet=" + tokenSet + ", confidence=" + confidence + "]";
    }

    @Override
    public int compareTo(Classification o) {

        Double c = Math.abs(confidence);
        if (c > o.confidence) return 1;
        if (c.equals(o.confidence))
            return 0;
        else return -1;
    }
}