/**
 * Copyright 2014 Digitising Scotland project:
 * <http://digitisingscotland.cs.st-andrews.ac.uk/>
 *
 * This file is part of the module population_model.
 *
 * population_model is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * population_model is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with population_model. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.digitising_scotland.population_model.model.gedcom;

import org.gedcom4j.parser.GedcomParser;
import org.junit.Test;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.IPopulation;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.IPopulationWriter;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.PopulationConverter;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.in_memory.CompactPopulation;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.in_memory.CompactPopulationAdapter;
import uk.ac.standrews.cs.digitising_scotland.population_model.transform.AbstractExporterTest;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Tests for GEDCOM validity using the gedcom4j parser.
 * See also online validator: http://ged-inline.elasticbeanstalk.com/validate
 */
public class GEDCOMValidityTest {

    @Test
    public void gedcomIsValid() throws Exception {

        final String path = Paths.get(AbstractExporterTest.TEST_DIRECTORY_PATH_STRING, "gedcom", "_test.ged").toString();

        final IPopulation population = new CompactPopulationAdapter(new CompactPopulation(1000));
        final IPopulationWriter population_writer = new PopulationToGEDCOM(path);

        try (PopulationConverter converter = new PopulationConverter(population, population_writer)) {
            converter.convert();
        }

        final GedcomParser parser = new GedcomParser();
        parser.load(path);

        assertEquals(0, parser.errors.size());
        assertEquals(0, parser.warnings.size());
    }
}