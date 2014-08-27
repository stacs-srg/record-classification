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
package uk.ac.standrews.cs.digitising_scotland.population_model.organic.logger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import uk.ac.standrews.cs.digitising_scotland.population_model.organic.OrganicPartnership;

public class LoggingControl {

    public static TemporalIntegerLogger numberOfChildrenFromAffairsDistributionLogger;
    public static TemporalIntegerLogger numberOfChildrenFromCohabitationDistributionLogger;
    public static TemporalIntegerLogger numberOfChildrenFromCohabThenMarriageDistributionLogger;
    public static TemporalIntegerLogger numberOfChildrenFromMarriagesDistributionLogger;

    public static void setUpLogger() {
        LoggingControl.numberOfChildrenFromAffairsDistributionLogger = new TemporalIntegerLogger(OrganicPartnership.getTemporalAffairNumberOfChildrenDistribution(), "ChildrenNumberOfAffairs", "Number of Children Distribution - Affairs", "Number of Children");
        LoggingControl.numberOfChildrenFromCohabitationDistributionLogger = new TemporalIntegerLogger(OrganicPartnership.getTemporalChildrenNumberOfInCohabDistribution(), "ChildrenNumberOfCohab", "Number of Children Distribution - Cohabitation", "Number of Children");
        LoggingControl.numberOfChildrenFromCohabThenMarriageDistributionLogger = new TemporalIntegerLogger(OrganicPartnership.getTemporalChildrenNumberOfInCohabThenMarriageDistribution(), "ChildrenNumberOfCohabTheMarriage", "Number of Children Distribution - Cohabitation Then Marriage", "Number of Children");
        LoggingControl.numberOfChildrenFromMarriagesDistributionLogger = new TemporalIntegerLogger(OrganicPartnership.getTemporalChildrenNumberOfInMarriageDistribution(), "ChildrenNumberOfMarriage", "Number of Children Distribution - Marriage", "Number of Children");
    }

    private static void output() {
        
        LoggingControl.numberOfChildrenFromMarriagesDistributionLogger.outputToGnuPlotFormat();
        LoggingControl.numberOfChildrenFromCohabitationDistributionLogger.outputToGnuPlotFormat();
        LoggingControl.numberOfChildrenFromCohabThenMarriageDistributionLogger.outputToGnuPlotFormat();
        LoggingControl.numberOfChildrenFromAffairsDistributionLogger.outputToGnuPlotFormat();
    }

    public static void createGnuPlotOutputFilesAndScript() {
        output();
        PrintWriter writer;
        try {
            String filePath = "src/main/resources/output/gnu/log_output_script.p";
            writer = new PrintWriter(filePath, "UTF-8");
            writer.println("# This file is called log_output_script.p");
            
            writer.println("set terminal pdf");
            writer.println("set output 'output.pdf'");
            numberOfChildrenFromAffairsDistributionLogger.generateGnuPlotScriptLines(writer);
            numberOfChildrenFromCohabitationDistributionLogger.generateGnuPlotScriptLines(writer);
            numberOfChildrenFromCohabThenMarriageDistributionLogger.generateGnuPlotScriptLines(writer);
            numberOfChildrenFromMarriagesDistributionLogger.generateGnuPlotScriptLines(writer);
            writer.println("set terminal png");
            
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
        }
    }

}
