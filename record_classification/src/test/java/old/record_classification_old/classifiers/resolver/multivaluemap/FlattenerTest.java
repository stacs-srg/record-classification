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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import old.record_classification_old.classifiers.resolver.generic.Flattener;
import old.record_classification_old.classifiers.resolver.generic.MultiValueMap;
import old.record_classification_old.datastructures.classification.Classification;
import old.record_classification_old.datastructures.code.Code;
import old.record_classification_old.datastructures.code.CodeNotValidException;

import java.io.IOException;

/**
 *
 * Created by fraserdunlop on 07/10/2014 at 12:22.
 */
@Ignore
public class FlattenerTest {

    private Flattener flattener = new Flattener();
    private MultiValueMapTestHelper mvmHelper;

    @Before
    public void setup() throws IOException, CodeNotValidException {

        mvmHelper = new MultiValueMapTestHelper();
        mvmHelper.addMockEntryToMatrix("brown dog", "2100", 0.5);
        mvmHelper.addMockEntryToMatrix("white dog", "2100", 0.85);
        mvmHelper.addMockEntryToMatrix("brown dog", "2200", 0.81);
        mvmHelper.addMockEntryToMatrix("white dog", "2200", 0.87);
    }

    @Test
    public void testFlattener() throws IOException, ClassNotFoundException {

        MultiValueMap<Code, Classification> map = mvmHelper.getMap();
        Assert.assertEquals(4, map.complexity());
        Assert.assertEquals(2, map.size());
        MultiValueMap<Code, Classification> map2 = flattener.moveAllIntoKey(map, map.iterator().next());
        Assert.assertEquals(4, map2.complexity());
        Assert.assertEquals(1, map2.size());
    }
}
