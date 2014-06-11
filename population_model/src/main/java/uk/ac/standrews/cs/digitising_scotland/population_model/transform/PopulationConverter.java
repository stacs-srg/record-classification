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
package uk.ac.standrews.cs.digitising_scotland.population_model.transform;

import uk.ac.standrews.cs.digitising_scotland.population_model.model.IPartnership;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.IPerson;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.IPopulation;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.IPopulationWriter;
import uk.ac.standrews.cs.digitising_scotland.util.ProgressIndicator;

/**
 * Created by graham on 11/06/2014.
 */
public class PopulationConverter {

    private final IPopulation population;
    private final IPopulationWriter writer;

    private final ProgressIndicator progress_indicator;


    public PopulationConverter(final IPopulation population, final IPopulationWriter writer, final ProgressIndicator progress_indicator) {

        this.population = population;
        this.writer = writer;
        this.progress_indicator = progress_indicator;

        initialiseProgressIndicator();
    }

    public PopulationConverter(final IPopulation population, final IPopulationWriter writer) {

        this(population, writer, null);
    }

    public void convert() throws Exception {

        for (Object member : population.getPopulation()) {

            if (member instanceof IPerson) {

                writer.recordIndividual((IPerson) member);
                progressStep();
            }
            else {
                if (member instanceof IPartnership) {

                    writer.recordPartnership((IPartnership) member);
                    progressStep();
                }
                else {
                    throw new RuntimeException("unexpected population member type");
                }
            }
        }
    }

    private void initialiseProgressIndicator() {

        if (progress_indicator != null) {
            progress_indicator.setTotalSteps(population.size() * 2);
        }
    }

    private void progressStep() {

        if (progress_indicator != null) {
            progress_indicator.progressStep();
        }
    }
}
