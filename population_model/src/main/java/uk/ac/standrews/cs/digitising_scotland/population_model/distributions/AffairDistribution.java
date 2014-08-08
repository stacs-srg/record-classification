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
package uk.ac.standrews.cs.digitising_scotland.population_model.distributions;

import java.util.Random;

import uk.ac.standrews.cs.digitising_scotland.population_model.organic.OrganicPartnership;
import uk.ac.standrews.cs.digitising_scotland.population_model.organic.OrganicPerson;
import uk.ac.standrews.cs.digitising_scotland.population_model.organic.OrganicPopulation;

public class AffairDistribution extends NormalDistribution {
	
	public static AffairDistribution AffairDistributionFactory(OrganicPartnership partnership, Random random) {
		double midPoint = (partnership.getTimeline().getEndDate() - partnership.getTimeline().getStartDay())/2;
    	double mean = partnership.getTimeline().getStartDay() + midPoint;
    	if (OrganicPopulation.DEBUG) {
    		System.out.println("Midpoint: " + midPoint);
    		System.out.println("Mean: " + mean);
    	}
		try {
			return new AffairDistribution(mean, midPoint/4, random);
		} catch (NegativeDeviationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private AffairDistribution(double mean, double standard_deviation, Random random) throws NegativeDeviationException {
		super(mean, standard_deviation, random);
	}
	
	public int getIntSample() {
		int temp = super.getSample().intValue();
		return temp;
	}
	
}
