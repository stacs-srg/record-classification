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
package uk.ac.standrews.cs.digitising_scotland.population_model.version3.lifetable;

import uk.ac.standrews.cs.digitising_scotland.population_model.config.PopulationProperties;
import uk.ac.standrews.cs.digitising_scotland.population_model.version3.Person;
import uk.ac.standrews.cs.digitising_scotland.util.DateManipulation;
import uk.ac.standrews.cs.nds.util.ErrorHandling;
import uk.ac.standrews.cs.util.tools.FileManipulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by tsd4 on 11/11/2015.
 */
public class LifeTableCatalogue {

    private TreeMap<Integer, LifeTable> catalogue = new TreeMap<Integer, LifeTable>();

    private String line;
    private static final String TAB = "\t";
    private static final String COMMENT_INDICATOR = "%";
    private int epochYear;

    public LifeTableCatalogue(int epochYear, String catalogueKey) {

        this.epochYear = epochYear;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(PopulationProperties.getProperties().getProperty(catalogueKey)), FileManipulation.FILE_CHARSET))) {

            while ((line = reader.readLine()) != null) {

                if (line.startsWith(COMMENT_INDICATOR)) {
                    continue;
                }

                String[] lC = line.split(TAB);

                if(lC.length == 2) {

                    if(catalogue.size() == 0) {
                        addTable(new LifeTable(this.epochYear, lC[1]));
                    }

                    addTable(new LifeTable(Integer.valueOf(lC[0]), lC[1]));

                } else {
                    System.err.println("LifeTable data " + catalogueKey + " line too short");
                }

            }

        } catch (NumberFormatException e) {
            ErrorHandling.exceptionError(e, "Could not process line:" + line);
            e.printStackTrace();
        } catch (IOException e) {
            ErrorHandling.exceptionError(e, "IO Exception");
            e.printStackTrace();
        }

    }

    public void addTable(LifeTable table) {
        catalogue.put(table.getYear(), table);
    }


    public boolean toDieByNQX(Person p, int currentDay) {
        return catalogue.get(catalogue.floorKey(DateManipulation.daysToYear(currentDay))).toDieByNQX(p, currentDay);
    }

    public TreeMap<Integer,LifeTable> getCloneOfTreeMap() {
        return (TreeMap<Integer, LifeTable>) catalogue.clone();
    }
}