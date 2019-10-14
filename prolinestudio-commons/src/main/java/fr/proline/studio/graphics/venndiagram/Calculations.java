/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.graphics.venndiagram;

/**
 * Useful methods for Venn Diagram
 * @author JM235353
 */
public class Calculations {
    
    /**
     * 
     * Calculate relative position of two circles, so the intersection area is of a specified value
     * with a precision
     * 
     * @param precision   : value >0 and <1  0.01 correspond to 1%
     * @param areaSearched : intersection area searched
     * @param radius1 : radius of the first circle
     * @param radius2 : radius of the second circle
     * @return distance found for the given precision
     */
    public static double circleDistanceForIntersectionArea(double precision, double areaSearched, double radius1, double radius2) {
        
        // initial values for secant method
        double d0 = 0;
        double d1 = radius1+radius2;
        
        double f0 = Circle.intersectionArea(d0, radius1, radius2)-areaSearched;
        double f1 = Circle.intersectionArea(d1, radius1, radius2)-areaSearched;
        
        while (Math.abs(f1-f0)/areaSearched>precision) {
            double d2 =  (d0*f1-d1*f0)/(f1-f0);
            d0 = d1;
            d1 = d2;
            f0 = f1;
            f1 = Circle.intersectionArea(d2, radius1, radius2)-areaSearched;
        }
        
        return d1;
    } 
}
