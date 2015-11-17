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
package uk.ac.standrews.cs.digitising_scotland.population_model.version3;


import uk.ac.standrews.cs.digitising_scotland.population_model.distributions.general.UniformIntegerDistribution;
import uk.ac.standrews.cs.digitising_scotland.population_model.distributions.temporal.ITemporalPopulationInfo;
import uk.ac.standrews.cs.digitising_scotland.population_model.distributions.temporal.TemporalIntegerDistribution;
import uk.ac.standrews.cs.digitising_scotland.population_model.version3.lifetable.LifeTableCatalogue;
import uk.ac.standrews.cs.digitising_scotland.population_model.version3.lifetable.analysis.LifeTableCatalogueShadow;
import uk.ac.standrews.cs.digitising_scotland.population_model.version3.lifetable.analysis.LifeTableShadow;
import uk.ac.standrews.cs.digitising_scotland.util.DateManipulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by tsd4 on 10/11/2015.
 */
public class Population implements ITemporalPopulationInfo {

    public static void main(String[] args) {
        Population pop = new Population();
        pop.initLifeTables();
        pop.generateSeedPopulation(500000);
        pop.runSimulation();
        System.out.println(pop.people.size());

        LifeTableCatalogueShadow ltcs = new LifeTableCatalogueShadow(pop.lifeTables, START_YEAR, END_YEAR);

        for (Double d : ltcs.analyse(pop.people)) {
            System.out.print(d + " ");
        }

    }

    private List<Person> people = new ArrayList<Person>();
    private List<Person> deadPeople = new ArrayList<Person>();

    private static final int EPOCH_YEAR = 1600;
    private static final int DAYS_PER_YEAR = 365;
    private static final int SEX_RATIO = 100;

    private static final int START_YEAR = 1775;
    private static final int END_YEAR = 1825;

    private int startYearInDays = DateManipulation.dateToDays(START_YEAR,0,0);
    private int endYearInDays = DateManipulation.dateToDays(END_YEAR,0,0);

    private int currentDay = startYearInDays;

    private int timeStep = 365;


    private Random random = new Random();

    LifeTableCatalogue lifeTables;



    public void runSimulation() {

        UniformIntegerDistribution dayInPeriod = new UniformIntegerDistribution(0, timeStep, random);

        while (currentDay < endYearInDays) {

            for(int i = 0; i < people.size(); i++) {
                Person p = people.get(i);
                if(lifeTables.toDieByNQX(p, currentDay)) {
                    p.die(currentDay);
                    deadPeople.add(p);
                    people.remove(p);
                    people.add(new Person(currentDay, true));
                }
            }


            currentDay += timeStep;
        }

        people.addAll(deadPeople);

    }

    public void generateSeedPopulation(int seedSize) {

        TemporalIntegerDistribution seedAgeForMalesDistribution = new TemporalIntegerDistribution(this, "seed_age_for_males_distribution_data_filename", random, false);
        TemporalIntegerDistribution seedAgeForFemalesDistribution = new TemporalIntegerDistribution(this, "seed_age_for_females_distribution_data_filename", random, false);

        ArrayList<Person> seedPop = new ArrayList<Person>();

        for(int i = 0; i < seedSize; i++) {
            if (random.nextBoolean()) {
                int age = seedAgeForMalesDistribution.getSample();
                seedPop.add(new Person(currentDay - age, true));
            } else {
                int age = seedAgeForFemalesDistribution.getSample();
                seedPop.add(new Person(currentDay - age, false));
            }
        }

        people = seedPop;

    }

    public void initLifeTables() {
        lifeTables = new LifeTableCatalogue(EPOCH_YEAR, "lifetable_catalogue");
    }


    public int getEpochYear() {
        return EPOCH_YEAR;
    }

    public int getDaysPerYear() {
        return DAYS_PER_YEAR;
    }

    @Override
    public void setMaximumNumberOfChildrenInFamily(int maximum) {
        // Here to allow the use of temporal distributions for the time being
    }
}