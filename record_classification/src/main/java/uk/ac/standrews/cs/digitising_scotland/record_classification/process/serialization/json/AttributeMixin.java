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
package uk.ac.standrews.cs.digitising_scotland.record_classification.process.serialization.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import weka.core.ProtectedProperties;

public abstract class AttributeMixin {

    // This is used to give the same effect as adding the @JsonIgnore attribute
    // to methods of Capabilities, which can't be done as it's a 3rd party class.

    @JsonIgnore
    public abstract ProtectedProperties getMetaData();

    @JsonIgnore
    public abstract boolean isRegular();

    @JsonIgnore
    public abstract boolean isAveragable();

    @JsonIgnore
    public abstract double getLowerNumericBound();


    @JsonIgnore
    public abstract double getUpperNumericBound();

    @JsonIgnore
    public abstract String getRevision();

    @JsonIgnore
    public abstract String getDateFormat();

//    @JsonIgnore
//    public abstract boolean isNominal();
//
//    @JsonIgnore
//    public abstract boolean isNumeric();
//
//    @JsonIgnore
//    public abstract boolean isRelationValued();
//
//    @JsonIgnore
//    public abstract boolean isString();
//
//    @JsonIgnore
//    public abstract boolean isDate();
}
