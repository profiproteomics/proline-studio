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
 *
 * Intersection between two Sets (m_s1 and m_s2)
 * For the calculations we only take in account intersection between two sets,
 * not intersection between multiple sets
 * 
 * @author JM235353
 */
public class SetIntersection {
    
    private Set m_s1;
    private Set m_s2;
    private double m_intersectionSize;
    
    public SetIntersection(Set s1, Set s2, double intersectionSize) {
        m_s1 = s1;
        m_s2 = s2;
        m_intersectionSize = intersectionSize;
    }
    
    public double getIntersectionSize() {
        return m_intersectionSize;
    }
    
    public Set getSet1() {
        return m_s1;
    }
    public Set getSet2() {
        return m_s2;
    }
    public Set getOtherSet(Set s) {
        if (s.equals(m_s1)) {
            return m_s2;
        }
        return m_s1;
    }
    
}
