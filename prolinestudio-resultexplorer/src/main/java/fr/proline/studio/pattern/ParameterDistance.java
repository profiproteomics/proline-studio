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
package fr.proline.studio.pattern;

/**
 * Used for the calculation of a distance between two databoxes
 * @author JM235353
 */
public class ParameterDistance implements Comparable {
 
    private static int ID = 0;
    
    private double m_distance;
    private int m_id;
    
    public ParameterDistance(double averageDistance) {
        m_distance = averageDistance;
        m_id = ID++;
    }

    @Override
    public int compareTo(Object o) {
        double cmp =  m_distance-((ParameterDistance)o).m_distance;
        if (!almostZero(cmp)) {
           if (cmp>0) {
               return -1;
           } else {
               return 1;
           }
        }
        return m_id-((ParameterDistance)o).m_id;
    }
    
    private static boolean almostZero(double a) {
        return StrictMath.abs(a)<0.001;
    }
    
    
}
