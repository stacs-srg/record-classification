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
package uk.ac.standrews.cs.digitising_scotland.population_model.model.in_memory;

import uk.ac.standrews.cs.digitising_scotland.population_model.distributions.InconsistentWeightException;
import uk.ac.standrews.cs.digitising_scotland.population_model.distributions.NegativeDeviationException;
import uk.ac.standrews.cs.digitising_scotland.population_model.distributions.NegativeWeightException;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.IPopulation;
import uk.ac.standrews.cs.digitising_scotland.util.DateManipulation;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by graham on 07/07/2014.
 */
public class CompactPopulationTestCases {

    protected static int fatherA_id = 1;
    protected static int motherA_id = 2;
    protected static int motherB_id = 3;
    protected static int fatherB_id = 4;
    protected static int child1A_id = 5;
    protected static int child1B_id = 6;
    protected static int child2A_id = 7;
    protected static int child2B_id = 8;

    public static IPopulation[] getTestPopulations() throws IOException, InconsistentWeightException, ParseException, NegativeDeviationException, NegativeWeightException {

        return new IPopulation[]{
                unconnectedPopulation(0),
                unconnectedPopulation(1),
                unconnectedPopulation(3),
                unconnectedPopulation(100),
                populationWithOnePartnership(),
                populationWithThreePartnerships(),
                populationWithTwoFamilies(),
                fullPopulation(1),
                fullPopulation(2),
                fullPopulation(3),
                fullPopulation(10),
                fullPopulation(100),
                fullPopulation(1000),
                fullPopulation(10000)};
    }

    protected static IPopulation makePopulation(int population_size) throws IOException, InconsistentWeightException {

        return new CompactPopulationAdapter(new CompactPopulation(makePeople(population_size), 0, 0));
    }

    protected static CompactPerson[] makePeople(int n) {

        CompactPerson[] result = new CompactPerson[n];
        for (int i = 0; i < n; i++) {
            result[i] = new CompactPerson(0, true);
            result[i].id = i + 1;
        }
        return result;
    }

    private static IPopulation unconnectedPopulation(int size) throws IOException, InconsistentWeightException {

        CompactPerson[] people = makePeople(size);
        IPopulation population = new CompactPopulationAdapter(new CompactPopulation(people, 0, 0));
        population.setDescription("unconnected-population-" + size);

        return population;
    }

    private static IPopulation fullPopulation(int size) throws IOException, InconsistentWeightException, NegativeDeviationException, NegativeWeightException {

        CompactPopulation compact_population = new CompactPopulation(size);
        IPopulation population = new CompactPopulationAdapter(compact_population);
        population.setDescription("full-population-" + size);

        return population;
    }

    private static IPopulation populationWithOnePartnership() throws IOException, InconsistentWeightException {

        CompactPerson[] people = makePeople(3);
        createPartnership(people, 0, 1);

        IPopulation population = new CompactPopulationAdapter(new CompactPopulation(people, 0, 0));
        population.setDescription("population-1-partnership");

        return population;
    }

    private static IPopulation populationWithThreePartnerships() throws IOException, InconsistentWeightException {

        CompactPerson[] people = makePeople(6);

        createPartnership(people, 0, 1);
        createPartnership(people, 2, 3);
        createPartnership(people, 4, 5);

        IPopulation population = new CompactPopulationAdapter(new CompactPopulation(people, 0, 0));
        population.setDescription("population-3-partnerships");

        return population;
    }

    protected static IPopulation populationWithTwoFamilies() throws IOException, InconsistentWeightException, ParseException {

        CompactPerson[] people = makePeopleInTwoFamilies();

        IPopulation population = new CompactPopulationAdapter(new CompactPopulation(people, 0, 0));
        population.setDescription("population-2-families");

        return population;
    }

    private static CompactPartnership createPartnership(CompactPerson[] people, int partner1_index, int partner2_index) {

        people[partner1_index].setMale(false);
        people[partner2_index].setMale(true);

        return new CompactPartnership(people[partner1_index], partner1_index, people[partner2_index], partner2_index, 0);
    }

    private static CompactPerson[] makePeopleInTwoFamilies() throws IOException, InconsistentWeightException, ParseException {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        int fatherA_birth_date = DateManipulation.dateToDays(DateManipulation.parseDate("21/08/1888", formatter));
        int motherA_birth_date = DateManipulation.dateToDays(DateManipulation.parseDate("01/01/1890", formatter));
        int child1A_birth_date = DateManipulation.dateToDays(DateManipulation.parseDate("17/11/1910", formatter));
        int child2A_birth_date = DateManipulation.dateToDays(DateManipulation.parseDate("31/12/1913", formatter));

        int fatherB_birth_date = DateManipulation.dateToDays(DateManipulation.parseDate("21/07/1860", formatter));
        int motherB_birth_date = DateManipulation.dateToDays(DateManipulation.parseDate("23/02/1891", formatter));
        int child1B_birth_date = DateManipulation.dateToDays(DateManipulation.parseDate("21/08/1920", formatter));
        int child2B_birth_date = DateManipulation.dateToDays(DateManipulation.parseDate("21/08/1925", formatter));

        CompactPerson fatherA = new CompactPerson(fatherA_birth_date, true, fatherA_id);
        CompactPerson motherA = new CompactPerson(motherA_birth_date, false, motherA_id);
        CompactPerson motherB = new CompactPerson(motherB_birth_date, false, motherB_id);
        CompactPerson fatherB = new CompactPerson(fatherB_birth_date, true, fatherB_id);
        CompactPerson child1A = new CompactPerson(child1A_birth_date, false, child1A_id);
        CompactPerson child1B = new CompactPerson(child1B_birth_date, true, child1B_id);
        CompactPerson child2A = new CompactPerson(child2A_birth_date, true, child2A_id);
        CompactPerson child2B = new CompactPerson(child2B_birth_date, false, child2B_id);

        CompactPerson[] population = new CompactPerson[]{fatherA, motherA, motherB, fatherB, child1A, child1B, child2A, child2B};

        CompactPartnership partnershipA = new CompactPartnership(0, 1, 0);
        CompactPartnership partnershipB = new CompactPartnership(2, 3, 0);

        List<CompactPartnership> partnershipsFatherA = new ArrayList<>();
        List<CompactPartnership> partnershipsMotherA = new ArrayList<>();
        List<CompactPartnership> partnershipsFatherB = new ArrayList<>();
        List<CompactPartnership> partnershipsMotherB = new ArrayList<>();

        partnershipsFatherA.add(partnershipA);
        partnershipsMotherA.add(partnershipA);
        partnershipsFatherB.add(partnershipB);
        partnershipsMotherB.add(partnershipB);

        fatherA.setPartnerships(partnershipsFatherA);
        motherA.setPartnerships(partnershipsMotherA);
        fatherB.setPartnerships(partnershipsFatherB);
        motherB.setPartnerships(partnershipsMotherB);

        List<Integer> childrenA = new ArrayList<>();
        List<Integer> childrenB = new ArrayList<>();

        childrenA.add(4);
        childrenA.add(6);
        childrenB.add(5);
        childrenB.add(7);

        partnershipA.setChildren(childrenA);
        partnershipB.setChildren(childrenB);

        return population;
    }
}