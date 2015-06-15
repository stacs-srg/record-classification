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
package old.record_classification_old.classifiers.resolver.multivaluemap;

import old.record_classification_old.classifiers.resolver.generic.BelowThresholdRemover;
import old.record_classification_old.classifiers.resolver.generic.MultiValueMap;
import old.record_classification_old.datastructures.classification.Classification;
import old.record_classification_old.datastructures.code.Code;
import old.record_classification_old.datastructures.code.CodeNotValidException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * Testing BelowThresholdRemover with Codes and Classifications.
 * Created by fraserdunlop on 07/10/2014 at 11:53.
 */
@Ignore
public class BelowThresholdRemoverTest {

    private BelowThresholdRemover belowThresholdRemover = new BelowThresholdRemover(0.7);
    private MultiValueMapTestHelper mvmHelper;

    @Before
    public void setup() throws IOException, CodeNotValidException {

        mvmHelper = new MultiValueMapTestHelper();
        mvmHelper.addMockEntryToMatrix("brown dog", "2100", 0.5);
        mvmHelper.addMockEntryToMatrix("white dog", "2100", 0.85);
        mvmHelper.addMockEntryToMatrix("brown dog", "2200", 0.81);
        mvmHelper.addMockEntryToMatrix("white dog", "2200", 0.87);
    }

    /**
     * Chop below confidence test.
     */
    @Test
    public void removeBelowThresholdTest() throws IOException, ClassNotFoundException {

        MultiValueMap<Code, Classification> map = mvmHelper.getMap();
        Assert.assertEquals(4, map.complexity());
        MultiValueMap<Code, Classification> matrix2 = belowThresholdRemover.removeBelowThreshold(map);
        Assert.assertEquals(2, matrix2.complexity());
    }
}