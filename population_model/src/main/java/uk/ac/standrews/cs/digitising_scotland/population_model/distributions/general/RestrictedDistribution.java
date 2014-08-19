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
package uk.ac.standrews.cs.digitising_scotland.population_model.distributions.general;

import java.util.ArrayList;

public abstract class RestrictedDistribution<Value> implements Distribution<Value>{


    // Restricted Distribution Helper Values
    protected Double minimumReturnValue = (Double) null;
    protected Double maximumReturnValue = (Double) null;
    
    protected ArrayList<Double> unusedSampleValues = new ArrayList<Double>();
    protected int zeroCount = -1;
    
    public abstract Value getSample(double earliestReturnValue, double latestReturnValue) throws NoPermissableValueException, NotSetUpAtClassInitilisationException;
    
    protected static boolean inRange(final double d, final double earliestReturnValue, final double latestReturnValue) {
        if (earliestReturnValue <= d && d <= latestReturnValue) {
            return true;
        } else {
            return false;
        }
    }
 
    public static void main(String[] args) {
    	System.out.println(inRange(1,2,4)); // False
    	System.out.println(inRange(2,2,4)); // True
    	System.out.println(inRange(3,2,4)); // True
    	System.out.println(inRange(4,2,4)); // True
    	System.out.println(inRange(5,2,4)); // False
    }
}
