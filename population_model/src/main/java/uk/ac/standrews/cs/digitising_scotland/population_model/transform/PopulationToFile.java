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

import uk.ac.standrews.cs.digitising_scotland.population_model.generation.distributions.FemaleFirstNameDistribution;
import uk.ac.standrews.cs.digitising_scotland.population_model.generation.distributions.InconsistentWeightException;
import uk.ac.standrews.cs.digitising_scotland.population_model.generation.distributions.MaleFirstNameDistribution;
import uk.ac.standrews.cs.digitising_scotland.population_model.generation.distributions.SurnameDistribution;
import uk.ac.standrews.cs.digitising_scotland.population_model.generation.util.RandomFactory;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.CompactPartnership;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.CompactPerson;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.CompactPopulation;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.IPerson;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.Person;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.PersonFactory;
import uk.ac.standrews.cs.digitising_scotland.util.ArrayIterator;
import uk.ac.standrews.cs.digitising_scotland.util.FileManipulation;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Writes a representation of the population to file in some external format - specialised by subclasses.
 * 
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 * @author Alan Dearle (alan.dearle@st-andrews.ac.uk)
 */
public abstract class PopulationToFile {

    private static final int NUMBER_OF_DIGITS_IN_ID = 8;

    protected final CompactPopulation population;
    private final String path_string;
    private final PersonFactory person_factory;
    private final MaleFirstNameDistribution male_first_name_distribution;
    private final FemaleFirstNameDistribution female_first_name_distribution;
    private final SurnameDistribution surname_distribution;

    protected final Set<CompactPartnership> processed_partnerships;
    protected final NumberFormat formatter;

    protected abstract void outputHeader(PrintWriter writer);
    protected abstract void outputIndividual(PrintWriter writer, int index, CompactPerson compact_person, Person person);
    protected abstract void outputFamilies(PrintWriter writer);
    protected abstract void outputTrailer(PrintWriter writer);

    /**
     * Initialises the exporter. This includes potentially expensive scanning of the population graph.
     * 
     * @param population the population
     * @param path_string the path for the output file
     * @throws IOException if the file does not exist and cannot be created
     */
    public PopulationToFile(final CompactPopulation population, final String path_string) throws IOException, InconsistentWeightException {

        this.population = population;
        this.path_string = path_string;

        processed_partnerships = new HashSet<>();
        person_factory = new PersonFactory();

        Random random = RandomFactory.getRandom();

        male_first_name_distribution = new MaleFirstNameDistribution(random);
        female_first_name_distribution = new FemaleFirstNameDistribution(random);
        surname_distribution = new SurnameDistribution(random);

        formatter = NumberFormat.getInstance();
        formatter.setMinimumIntegerDigits(NUMBER_OF_DIGITS_IN_ID);
        formatter.setGroupingUsed(false);
    }

    /**
     * Exports representation of the population to file.
     */
    public final synchronized void export() throws IOException {

        Path path = Paths.get(path_string);
        FileManipulation.createParentDirectoryIfDoesNotExist(path);

        try (final PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path, FileManipulation.FILE_CHARSET))) {

            outputHeader(writer);
            outputIndividuals(writer);
            outputFamilies(writer);
            outputTrailer(writer);
        }
    }

    protected void outputIndividuals(final PrintWriter writer) {

        final int index = 0;
        Iterator people = new ArrayIterator(population.getPeopleArray());

        while(people.hasNext()) {
            CompactPerson p = (CompactPerson)people.next();
            if (!p.isMarked()) { // Don't put out people we have already processed

                final String currSurname = surname_distribution.getSample(); // everyone in this tree will have the same surname

                final List<CompactPerson> personsToCheck = new ArrayList<CompactPerson>(); // a list of people to output - represents a family tree rooted at perspm p

                personsToCheck.add(p); // add the next person from the whole population

                do {

                    final CompactPerson next = personsToCheck.remove(0); // get the first person off the toCheck list

                    // output the next full person

                    final Person fp = person_factory.createPerson(next);
                    next.setMarked(true); // remember that we have processed this person.
                    fp.setSurname(currSurname);
                    fp.setFirstName(fp.getSex() == IPerson.FEMALE ? female_first_name_distribution.getSample() : male_first_name_distribution.getSample());

                    outputIndividual(writer, index, next, fp);

                    // record their children for processing future

                    if (next.isMale() && next.getPartnerships() != null) {
                        for (final CompactPartnership partnership : next.getPartnerships()) {

                            for (final int child : partnership.getChildren()) {
                                personsToCheck.add(population.getPerson(child));
                            }
                        }
                    }
                }
                while (!personsToCheck.isEmpty());
            }
        }
    }

    protected String padId(final int index) {

        return formatter.format(index);
    }
}
