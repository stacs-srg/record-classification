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
import uk.ac.standrews.cs.digitising_scotland.population_model.model.IPartnership;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.IPerson;
import uk.ac.standrews.cs.digitising_scotland.population_model.model.IPopulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created by graham on 10/06/2014.
 * Not thread-safe.
 */
public class CompactPopulationAdapter implements IPopulation {

    private final CompactPopulation population;
    private final CompactPerson[] people;
    private final CompactPersonAdapter compact_person_adapter;
    private final CompactPartnershipAdapter compact_partnership_adapter;

    private boolean consistent_across_iterations;
    private static boolean default_consistent_across_iterations = false;

    private final Map<Integer, IPerson> person_cache;
    private final Map<Integer, IPartnership> partnership_cache;

    private String description;

    public CompactPopulationAdapter(final CompactPopulation population) throws IOException, InconsistentWeightException {

        this.population = population;
        people = population.getPeopleArray();

        compact_person_adapter = new CompactPersonAdapter();
        compact_partnership_adapter = new CompactPartnershipAdapter();

        person_cache = new HashMap<>();
        partnership_cache = new HashMap<>();

        consistent_across_iterations = default_consistent_across_iterations;
        description = super.toString();
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return description;
    }

    public static void setDefaultConsistentAcrossIterations(boolean default_consistent_across_iterations) {
        CompactPopulationAdapter.default_consistent_across_iterations = default_consistent_across_iterations;
    }

    public void setConsistentAcrossIterations(boolean consistent_across_iterations) {
        this.consistent_across_iterations = consistent_across_iterations;
    }

    @Override
    public Iterable<IPerson> getPeople() {

        return new Iterable<IPerson>() {

            @Override
            public Iterator<IPerson> iterator() {

                unmarkAllPeople();
                return new PersonIterator();
            }

            class PersonIterator implements Iterator<IPerson> {

                private int person_index = 0;
                private CompactPerson next_person = null;
                private List<CompactPerson> descendants = new ArrayList<>();

                PersonIterator() {
                    advanceToNext();
                }

                @Override
                public boolean hasNext() {

                    return next_person != null;
                }

                @Override
                public IPerson next() {

                    if (!hasNext()) throw new NoSuchElementException();
                    IPerson result = getFullPerson(next_person);
                    advanceToNext();
                    return result;
                }

                @Override
                public void remove() {

                    throw new UnsupportedOperationException("remove");
                }

                private void advanceToNext() {

                    if (!descendants.isEmpty()) {

                        // Another person available with same surname.
                        next_person = descendants.remove(0);

                    } else {

                        // New person that doesn't inherit surname from father.
                        compact_person_adapter.generateNextSurname();

                        if (person_index >= people.length) {
                            recordNoMorePeople();

                        } else {

                            advanceToNextUnmarkedPerson();
                            checkChildren();
                        }
                    }

                    markPerson(next_person);
                }

                private void advanceToNextUnmarkedPerson() {

                    do {
                        next_person = people[person_index++];
                    }
                    while (person_index < people.length && next_person.isMarked());
                }

                private void checkChildren() {

                    if (reachedEnd()) {
                        recordNoMorePeople();

                    } else {
                        recordChildrenWithSameSurnameIfMale(next_person);
                    }
                }

                private boolean reachedEnd() {

                    return person_index == people.length && next_person.isMarked();
                }

                private void recordNoMorePeople() {

                    next_person = null;
                }

                private void recordChildrenWithSameSurnameIfMale(CompactPerson person) {

                    if (person.isMale()) {

                        List<CompactPartnership> partnerships = person.getPartnerships();
                        if (partnerships != null) {

                            for (final CompactPartnership partnership : partnerships) {

                                List<Integer> children = partnership.getChildren();
                                if (children != null) {

                                    for (final int child_index : children) {

                                        CompactPerson child = population.getPerson(child_index);
                                        descendants.add(child);
                                        recordChildrenWithSameSurnameIfMale(child);
                                    }
                                }
                            }
                        }
                    }
                }

                private void markPerson(CompactPerson person) {

                    if (person != null) {
                        person.setMarked(true);
                    }
                }
            }
        };
    }

    @Override
    public Iterable<IPartnership> getPartnerships() {

        return new Iterable<IPartnership>() {

            @Override
            public Iterator<IPartnership> iterator() {

                unmarkAllPartnerships();
                return new PartnershipIterator();
            }

            class PartnershipIterator implements Iterator<IPartnership> {

                int person_index = 0;
                Iterator<CompactPartnership> partnerships = null;
                CompactPartnership next_partnership = null;

                PartnershipIterator() {
                    advanceToNext();
                }

                @Override
                public boolean hasNext() {

                    return next_partnership != null;
                }

                @Override
                public IPartnership next() {

                    if (!hasNext()) throw new NoSuchElementException();
                    IPartnership result = getFullPartnership(next_partnership);
                    advanceToNext();
                    return result;
                }

                @Override
                public void remove() {

                    throw new UnsupportedOperationException("remove");
                }

                private void advanceToNext() {

                    readFirstPartnerships();
                    readNextUnmarkedPartnership();
                    markPartnership();
                }

                private void readFirstPartnerships() {

                    if (partnerships == null) {
                        partnerships = getPartnerships(0);
                    }
                }

                private void readNextUnmarkedPartnership() {

                    do {
                        readPartnershipsForNextPerson();
                        readNextPartnershipForThisPerson();
                    }
                    while (nextPartnershipIsMarked());
                }

                private boolean nextPartnershipIsMarked() {

                    return next_partnership != null && next_partnership.isMarked();
                }

                private void markPartnership() {

                    if (next_partnership != null) {
                        next_partnership.setMarked(true);
                    }
                }

                private void readPartnershipsForNextPerson() {

                    while (person_index < people.length && !partnerships.hasNext()) {
                        partnerships = getPartnerships(++person_index);
                    }
                }

                private void readNextPartnershipForThisPerson() {

                    next_partnership = partnerships.hasNext() ? partnerships.next() : null;
                }
            }
        };
    }

    @Override
    public IPerson findPerson(final int id) {

        CompactPerson person = population.findPerson(id);
        return getFullPerson(person);
    }

    @Override
    public IPartnership findPartnership(final int id) {

        return getFullPartnership(population.findPartnership(id));
    }

    @Override
    public int getNumberOfPeople() {

        return population.getNumberOfPeople();
    }

    @Override
    public int getNumberOfPartnerships() {

        return population.getNumberOfPartnerships();
    }

    private IPerson getFullPerson(final CompactPerson person) {

        if (person == null) return null;

        if (!consistent_across_iterations) {

            return compact_person_adapter.convertToFullPerson(person);

        } else {

            int id = person.getId();

            if (person_cache.containsKey(id)) {
                return person_cache.get(id);
            }
            else {

                IPerson full_person = compact_person_adapter.convertToFullPerson(person);
                person_cache.put(id, full_person);
                return full_person;
            }
        }
    }

    private IPartnership getFullPartnership(final CompactPartnership partnership) {

        if (partnership == null) return null;

        if (!consistent_across_iterations) {

            return compact_partnership_adapter.convertToFullPartnership(partnership, population);

        } else {

            int id = partnership.getId();

            if (partnership_cache.containsKey(id)) {
                return partnership_cache.get(id);
            }
            else {

                IPartnership full_partnership = compact_partnership_adapter.convertToFullPartnership(partnership, population);
                partnership_cache.put(id, full_partnership);
                return full_partnership;
            }
        }
    }

    private Iterator<CompactPartnership> getPartnerships(final int person_index) {

        List<CompactPartnership> partnerships = person_index < people.length ? people[person_index].getPartnerships() : null;
        return (partnerships == null ? new ArrayList<CompactPartnership>() : partnerships).iterator();
    }

    private void unmarkAllPeople() {

        for (CompactPerson person : people) {
            person.setMarked(false);
        }
    }

    private void unmarkAllPartnerships() {

        for (CompactPerson person : people) {
            List<CompactPartnership> partnerships = person.getPartnerships();
            if (partnerships != null) {
                for (CompactPartnership partnership : partnerships) {
                    partnership.setMarked(false);
                }
            }
        }
    }
}