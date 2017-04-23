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

import java.io.Serializable;

public class Classification implements Serializable {

    public static final String UNCLASSIFIED_CODE = "UNCLASSIFIED";
    public static final Classification UNCLASSIFIED = new Classification(UNCLASSIFIED_CODE, TokenList.UNMODIFIABLE_TOKEN_SET, 0.0, null);

    private static final long serialVersionUID = 7074436345885045033L;

    private String code;
    private TokenList token_list;
    private double confidence;
    private String detail;

    public Classification() {

    }

    public Classification(final String code, final TokenList token_list, final double confidence, final String detail) {

        this.code = code;
        this.token_list = token_list;
        this.confidence = confidence;
        this.detail = detail;
    }

    public String getCode() {

        return code;
    }

    public TokenList getTokenList() {

        return token_list;
    }

    public double getConfidence() {

        return confidence;
    }

    public void setConfidence(double confidence) {

        this.confidence = confidence;
    }

    public String getDetail() {

        return detail;
    }

    @Override
    public String toString() {

        return "Classification [code=" + code + ", token_list=" + token_list + ", confidence=" + confidence + (detail != null ? ", detail=" + detail : "") + "]";
    }

    public Classification makeClone() {

        return makeClone(confidence);
    }

    public Classification makeClone(double confidence) {

        return makeClone(confidence, detail);
    }

    public Classification makeClone(double confidence, String detail) {

        return isUnclassified() ? UNCLASSIFIED : new Classification(code, new TokenList(token_list.toString()), confidence, detail);
    }

    public boolean isUnclassified() {

        return equals(UNCLASSIFIED);
    }
}
