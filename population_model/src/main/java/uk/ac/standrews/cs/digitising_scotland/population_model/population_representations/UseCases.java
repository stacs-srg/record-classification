/*
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
package uk.ac.standrews.cs.digitising_scotland.population_model.population_representations;

import uk.ac.standrews.cs.digitising_scotland.population_model.population_representations.adapted_db.ExportPopulationToDB;


public class UseCases {

	public static LinkedPopulation generateNuclearFamilyUseCase() {

		LinkedPopulation population = new LinkedPopulation("Nuclear Family Use Case");
		population.addPerson(new LinkedPerson(0, "alpha", "Family", 'M'));
		population.addPerson(new LinkedPerson(1, "beta", "Family", 'M'));
		population.addPerson(new LinkedPerson(2, "gamma", "Family", 'M'));
		population.addPerson(new LinkedPerson(3, "delta", "Family", 'M'));
		population.addPerson(new LinkedPerson(4, "epsilon", "Family", 'F'));
		population.addPerson(new LinkedPerson(5, "zeta", "Not", 'M'));
		population.addPerson(new LinkedPerson(6, "eta", "Not", 'F'));
		population.addPerson(new LinkedPerson(7, "theta", "Not", 'F'));
		population.addPerson(new LinkedPerson(8, "iota", "Not", 'F'));

		Evidence[] records = new Evidence[9];
		for(int i = 0; i < 9; i++)
			records[i] = new Evidence(i);

		population.addPartnership(new LinkedPartnership(0, "alef"));
		population.addPartnership(new LinkedPartnership(1, "bet"));
		population.addPartnership(new LinkedPartnership(2, "gimel"));

		population.getPartnershipByRef("alef").setChildLink(population.findPersonByFirstName("alpha"), new Evidence[]{records[2]});
		population.getPartnershipByRef("bet").setChildLink(population.findPersonByFirstName("beta"), new Evidence[]{records[1]});
		population.getPartnershipByRef("gimel").setChildLink(population.findPersonByFirstName("gamma"), new Evidence[]{records[0]});

		population.getPartnershipByRef("alef").addPossibleFatherLink(population.findPersonByFirstName("delta"), new Evidence[]{records[2], records[6]}, 0.8f);
		population.getPartnershipByRef("alef").addPossibleFatherLink(population.findPersonByFirstName("zeta"), new Evidence[]{records[2], records[7]}, 0.6f);

		population.getPartnershipByRef("alef").addPossibleMotherLink(population.findPersonByFirstName("iota"), new Evidence[]{records[2], records[5]}, 0.3f);
		population.getPartnershipByRef("alef").addPossibleMotherLink(population.findPersonByFirstName("theta"), new Evidence[]{records[2], records[4]}, 0.4f);
		population.getPartnershipByRef("alef").addPossibleMotherLink(population.findPersonByFirstName("epsilon"), new Evidence[]{records[2], records[3]}, 0.9f);

		population.getPartnershipByRef("bet").addPossibleFatherLink(population.findPersonByFirstName("delta"), new Evidence[]{records[1], records[6]}, 0.7f);

		population.getPartnershipByRef("bet").addPossibleMotherLink(population.findPersonByFirstName("epsilon"), new Evidence[]{records[1], records[3]}, 0.7f);
		population.getPartnershipByRef("bet").addPossibleMotherLink(population.findPersonByFirstName("eta"), new Evidence[]{records[1], records[8]}, 0.5f);

		population.getPartnershipByRef("gimel").addPossibleFatherLink(population.findPersonByFirstName("delta"), new Evidence[]{records[0], records[6]}, 0.9f);
		population.getPartnershipByRef("gimel").addPossibleFatherLink(population.findPersonByFirstName("zeta"), new Evidence[]{records[0], records[7]}, 0.5f);

		population.getPartnershipByRef("gimel").addPossibleMotherLink(population.findPersonByFirstName("epsilon"), new Evidence[]{records[0], records[3]}, 0.6f);

		return population;
	}

	public static LinkedPopulation nonCrossOverMultiGenerationUseCase() {

		LinkedPopulation population = new LinkedPopulation("Nuclear Family Use Case");
		population.addPerson(new LinkedPerson(0, "a", "Name", 'M'));
		population.addPerson(new LinkedPerson(1, "b", "Name", 'M'));
		population.addPerson(new LinkedPerson(2, "c", "Name", 'F'));
		population.addPerson(new LinkedPerson(3, "d", "Name", 'F'));
		population.addPerson(new LinkedPerson(4, "e", "Name", 'M'));
		population.addPerson(new LinkedPerson(5, "f", "Name", 'M'));
		population.addPerson(new LinkedPerson(6, "g", "Name", 'F'));
		population.addPerson(new LinkedPerson(7, "h", "Name", 'F'));
		population.addPerson(new LinkedPerson(8, "i", "Name", 'M'));
		population.addPerson(new LinkedPerson(9, "j", "Name", 'F'));


		Evidence[] records = new Evidence[9];
		for(int i = 0; i < 9; i++)
			records[i] = new Evidence(i);

		population.addPartnership(new LinkedPartnership(0, "alpha"));
		population.addPartnership(new LinkedPartnership(1, "beta"));
		population.addPartnership(new LinkedPartnership(2, "gamma"));

		population.getPartnershipByRef("alpha").setChildLink(population.findPersonByFirstName("a"), new Evidence[]{records[0]});
		population.getPartnershipByRef("beta").setChildLink(population.findPersonByFirstName("g"), new Evidence[]{records[6]});
		population.getPartnershipByRef("gamma").setChildLink(population.findPersonByFirstName("c"), new Evidence[]{records[2]});

		population.getPartnershipByRef("alpha").addPossibleFatherLink(population.findPersonByFirstName("b"), new Evidence[]{records[0], records[1]}, 0.8f);

		population.getPartnershipByRef("alpha").addPossibleMotherLink(population.findPersonByFirstName("c"), new Evidence[]{records[0], records[2]}, 0.6f);
		population.getPartnershipByRef("alpha").addPossibleMotherLink(population.findPersonByFirstName("g"), new Evidence[]{records[0], records[6]}, 0.6f);

		population.getPartnershipByRef("beta").addPossibleFatherLink(population.findPersonByFirstName("e"), new Evidence[]{records[2], records[5]}, 0.7f);
		population.getPartnershipByRef("beta").addPossibleFatherLink(population.findPersonByFirstName("f"), new Evidence[]{records[2], records[4]}, 0.8f);

		population.getPartnershipByRef("beta").addPossibleMotherLink(population.findPersonByFirstName("d"), new Evidence[]{records[3], records[2]}, 0.6f);

		population.getPartnershipByRef("gamma").addPossibleFatherLink(population.findPersonByFirstName("i"), new Evidence[]{records[6], records[7]}, 0.8f);

		population.getPartnershipByRef("gamma").addPossibleMotherLink(population.findPersonByFirstName("j"), new Evidence[]{records[6], records[9]}, 0.6f);
		population.getPartnershipByRef("gamma").addPossibleMotherLink(population.findPersonByFirstName("h"), new Evidence[]{records[6], records[8]}, 0.4f);

		return population;		

	}

	public static LinkedPopulation crossOverMultiGenerationUseCase() {

		LinkedPopulation population = new LinkedPopulation("Nuclear Family Use Case");
		population.addPerson(new LinkedPerson(0, "a", "Name", 'M'));
		population.addPerson(new LinkedPerson(1, "b", "Name", 'M'));
		population.addPerson(new LinkedPerson(2, "c", "Name", 'F'));
		population.addPerson(new LinkedPerson(3, "d", "Name", 'F'));
		population.addPerson(new LinkedPerson(4, "e", "Name", 'M'));
		population.addPerson(new LinkedPerson(5, "f", "Name", 'M'));
		population.addPerson(new LinkedPerson(6, "g", "Name", 'F'));
		population.addPerson(new LinkedPerson(7, "h", "Name", 'F'));
		population.addPerson(new LinkedPerson(8, "i", "Name", 'M'));
		population.addPerson(new LinkedPerson(9, "j", "Name", 'F'));


		Evidence[] records = new Evidence[9];
		for(int i = 0; i < 9; i++)
			records[i] = new Evidence(i);

		population.addPartnership(new LinkedPartnership(0, "alpha"));
		population.addPartnership(new LinkedPartnership(1, "beta"));
		population.addPartnership(new LinkedPartnership(2, "gamma"));

		population.getPartnershipByRef("alpha").setChildLink(population.findPersonByFirstName("a"), new Evidence[]{records[0]});
		population.getPartnershipByRef("beta").setChildLink(population.findPersonByFirstName("g"), new Evidence[]{records[6]});
		population.getPartnershipByRef("gamma").setChildLink(population.findPersonByFirstName("c"), new Evidence[]{records[2]});

		population.getPartnershipByRef("alpha").addPossibleFatherLink(population.findPersonByFirstName("b"), new Evidence[]{records[0], records[1]}, 0.8f);

		population.getPartnershipByRef("alpha").addPossibleMotherLink(population.findPersonByFirstName("c"), new Evidence[]{records[0], records[2]}, 0.6f);
		population.getPartnershipByRef("alpha").addPossibleMotherLink(population.findPersonByFirstName("g"), new Evidence[]{records[0], records[6]}, 0.6f);

		population.getPartnershipByRef("beta").addPossibleFatherLink(population.findPersonByFirstName("e"), new Evidence[]{records[2], records[5]}, 0.7f);
		population.getPartnershipByRef("beta").addPossibleFatherLink(population.findPersonByFirstName("f"), new Evidence[]{records[2], records[4]}, 0.8f);

		population.getPartnershipByRef("beta").addPossibleMotherLink(population.findPersonByFirstName("d"), new Evidence[]{records[3], records[2]}, 0.6f);

		population.getPartnershipByRef("gamma").addPossibleFatherLink(population.findPersonByFirstName("i"), new Evidence[]{records[6], records[7]}, 0.8f);
		population.getPartnershipByRef("gamma").addPossibleFatherLink(population.findPersonByFirstName("e"), new Evidence[]{records[6], records[5]}, 0.9f);

		population.getPartnershipByRef("gamma").addPossibleMotherLink(population.findPersonByFirstName("j"), new Evidence[]{records[6], records[9]}, 0.6f);
		population.getPartnershipByRef("gamma").addPossibleMotherLink(population.findPersonByFirstName("h"), new Evidence[]{records[6], records[8]}, 0.4f);
		population.getPartnershipByRef("gamma").addPossibleMotherLink(population.findPersonByFirstName("d"), new Evidence[]{records[6], records[3]}, 0.8f);

		return population;		

	}

	public static LinkedPopulation singleBestFitUseCase() {

		LinkedPopulation population = new LinkedPopulation("Nuclear Family Use Case");
		population.addPerson(new LinkedPerson(0, "a", "Name", 'M'));
		population.addPerson(new LinkedPerson(1, "b", "Name", 'M'));
		population.addPerson(new LinkedPerson(2, "c", "Name", 'F'));
		population.addPerson(new LinkedPerson(3, "d", "Name", 'M'));
		population.addPerson(new LinkedPerson(4, "e", "Name", 'F'));
		population.addPerson(new LinkedPerson(5, "f", "Name", 'F'));
		population.addPerson(new LinkedPerson(6, "g", "Name", 'M'));
		population.addPerson(new LinkedPerson(7, "h", "Name", 'M'));
		population.addPerson(new LinkedPerson(8, "i", "Name", 'F'));
		population.addPerson(new LinkedPerson(9, "j", "Name", 'M'));
		population.addPerson(new LinkedPerson(10, "k", "Name", 'F'));
		population.addPerson(new LinkedPerson(11, "l", "Name", 'M'));
		population.addPerson(new LinkedPerson(12, "m", "Name", 'M'));
		population.addPerson(new LinkedPerson(13, "n", "Name", 'F'));
		population.addPerson(new LinkedPerson(14, "o", "Name", 'F'));


		Evidence[] records = new Evidence[14];
		for(int i = 0; i < 14; i++)
			records[i] = new Evidence(i);

		population.addPartnership(new LinkedPartnership(0, "alpha"));
		population.addPartnership(new LinkedPartnership(1, "beta"));
		population.addPartnership(new LinkedPartnership(2, "gamma"));
		population.addPartnership(new LinkedPartnership(3, "delta"));
		population.addPartnership(new LinkedPartnership(4, "epsilon"));
		population.addPartnership(new LinkedPartnership(5, "zeta"));
		population.addPartnership(new LinkedPartnership(6, "eta"));

		population.getPartnershipByRef("alpha").setChildLink(population.findPersonByFirstName("a"), new Evidence[]{records[0]});
		population.getPartnershipByRef("beta").setChildLink(population.findPersonByFirstName("b"), new Evidence[]{records[1]});
		population.getPartnershipByRef("gamma").setChildLink(population.findPersonByFirstName("c"), new Evidence[]{records[2]});
		population.getPartnershipByRef("delta").setChildLink(population.findPersonByFirstName("g"), new Evidence[]{records[3]});
		population.getPartnershipByRef("epsilon").setChildLink(population.findPersonByFirstName("f"), new Evidence[]{records[4]});
		population.getPartnershipByRef("zeta").setChildLink(population.findPersonByFirstName("d"), new Evidence[]{records[5]});
		population.getPartnershipByRef("eta").setChildLink(population.findPersonByFirstName("e"), new Evidence[]{records[6]});

		population.getPartnershipByRef("alpha").addPossibleFatherLink(population.findPersonByFirstName("b"), new Evidence[]{records[0], records[1]}, 0.9f);
		population.getPartnershipByRef("alpha").addPossibleMotherLink(population.findPersonByFirstName("c"), new Evidence[]{records[0], records[2]}, 0.9f);
		
		population.getPartnershipByRef("beta").addPossibleFatherLink(population.findPersonByFirstName("g"), new Evidence[]{records[3], records[1]}, 0.9f);
		population.getPartnershipByRef("beta").addPossibleMotherLink(population.findPersonByFirstName("f"), new Evidence[]{records[1], records[4]}, 0.9f);
		
		population.getPartnershipByRef("gamma").addPossibleFatherLink(population.findPersonByFirstName("d"), new Evidence[]{records[2], records[5]}, 0.9f);
		population.getPartnershipByRef("gamma").addPossibleMotherLink(population.findPersonByFirstName("e"), new Evidence[]{records[6], records[2]}, 0.9f);
		
		population.getPartnershipByRef("delta").addPossibleFatherLink(population.findPersonByFirstName("m"), new Evidence[]{records[3], records[7]}, 0.9f);
		population.getPartnershipByRef("delta").addPossibleMotherLink(population.findPersonByFirstName("n"), new Evidence[]{records[3], records[8]}, 0.9f);
		
		population.getPartnershipByRef("epsilon").addPossibleFatherLink(population.findPersonByFirstName("l"), new Evidence[]{records[4], records[9]}, 0.9f);
		population.getPartnershipByRef("epsilon").addPossibleMotherLink(population.findPersonByFirstName("o"), new Evidence[]{records[4], records[10]}, 0.9f);
		
		population.getPartnershipByRef("zeta").addPossibleFatherLink(population.findPersonByFirstName("h"), new Evidence[]{records[5], records[11]}, 0.9f);
		population.getPartnershipByRef("zeta").addPossibleMotherLink(population.findPersonByFirstName("i"), new Evidence[]{records[5], records[12]}, 0.9f);
		
		population.getPartnershipByRef("eta").addPossibleFatherLink(population.findPersonByFirstName("j"), new Evidence[]{records[6], records[13]}, 0.9f);
		population.getPartnershipByRef("eta").addPossibleMotherLink(population.findPersonByFirstName("k"), new Evidence[]{records[6], records[14]}, 0.9f);
		
		return population;
		
	}
	
	public static LinkedPopulation maleLineUseCase() {

		return new LinkedPopulation("desc");
		
	}
	
	public static LinkedPopulation cousinsUseCase() {

		return new LinkedPopulation("desc");
		
	}
	
	



	public static void main(String[] args) {
		LinkedPopulation pop = generateNuclearFamilyUseCase();
		try {
			new ExportPopulationToDB(pop);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}