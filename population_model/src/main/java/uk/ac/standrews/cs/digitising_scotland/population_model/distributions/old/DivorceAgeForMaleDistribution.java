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
package uk.ac.standrews.cs.digitising_scotland.population_model.distributions.old;

import uk.ac.standrews.cs.digitising_scotland.population_model.distributions.general.Distribution;
import uk.ac.standrews.cs.digitising_scotland.population_model.distributions.general.NegativeWeightException;
import uk.ac.standrews.cs.digitising_scotland.population_model.distributions.general.WeightedIntegerDistribution;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.PopulationLogic;

import java.util.Random;


/**
 * Distribution modelling ages of males at divorce, represented in days.
 *
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class DivorceAgeForMaleDistribution implements Distribution<Integer> {

    /*
     * from numberofdivorcesageatdivorceandmaritalstatusbeforemarriage_tcm77-351699
     * from http://www.ons.gov.uk/ons/rel/vsob1/divorces-in-england-and-wales/2012/rtd-divorces---number-of-divorces-age-at-divorce-and-marital-status-before-marriage.xls
     * Number of divorces, age at divorce and marital status before marriage
     * 
     * Data for the year 1975
     * 
     * Age     per 1000 Divorces
     * 0-4     0
     * 5-9     0
     * 10-14   0
     * 15-19   6
     * 20-24   137
     * 25-29   214
     * 30-34   192
     * 35-39   161
     * 40-44   122
     * 45-49   91
     * 50-54   28
     * 55-59   28
     * 60-64   14 // By interpolation
     * 65-69   6 // By interpolation
     * 70-74   1 // Only here to allow for corner case possibility
     * 75-79   1
     * 80-84   1
     * 85-89   1
     * 90-95   1
     * 95-100  1
     */

    private static final int MINIMUM_AGE_IN_YEARS = 15;
    private static final int MAXIMUM_AGE_IN_YEARS = 100;

    @SuppressWarnings("MagicNumber")
    private static final int[] AGE_DISTRIBUTION_WEIGHTS = new int[]{6, 137, 214, 192, 161, 122, 91, 28, 28, 14, 6, 1, 1, 1, 1, 1, 1};

    private final WeightedIntegerDistribution distribution;

    /**
     * Creates an age at divorce for males distribution.
     *
     * @param random The random number generator to be used.
     */
    public DivorceAgeForMaleDistribution(final Random random) {
        try {
            distribution = new WeightedIntegerDistribution((int) (MINIMUM_AGE_IN_YEARS * PopulationLogic.DAYS_PER_YEAR), (int) (MAXIMUM_AGE_IN_YEARS * PopulationLogic
                    .DAYS_PER_YEAR) - 1, AGE_DISTRIBUTION_WEIGHTS, random);

        } catch (final NegativeWeightException e) {
            throw new RuntimeException("negative weight exception: " + e.getMessage());
        }
    }

    @Override
    public Integer getSample() {
        return distribution.getSample();
    }
}