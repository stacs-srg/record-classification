/*
 * Copyright 2015 Digitising Scotland project:
 * <http://digitisingscotland.cs.st-andrews.ac.uk/>
 *
 * This file is part of the module util.
 *
 * util is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * util is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with util. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.digitising_scotland.util.MTree.experiments.euclidean;

import uk.ac.standrews.cs.digitising_scotland.util.MTree.Distance;

/**
 * Created by graham on 22/03/2017.
 */
public class EuclideanDistance implements Distance<Point> {

    public float distance(Point p1, Point p2) {
        float xdistance = p1.x - p2.x;
        float ydistance = p1.y - p2.y;

        return (float) Math.sqrt((xdistance * xdistance) + (ydistance * ydistance));
    }

}

