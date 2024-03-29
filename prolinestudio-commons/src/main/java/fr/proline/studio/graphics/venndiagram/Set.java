/* 
 * Copyright (C) 2019
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

import java.util.ArrayList;
import java.util.HashMap;


/**
 *
 * Set correspond to a Group(size) like a group of proteins of an analysis.
 * 
 * @author JM235353
 */
public class Set implements Comparable<Set> {
    
    private final String m_name;
    
    // list of intersection with other sets
    private ArrayList<SetIntersection> m_intersections = new ArrayList<>(16);
    
    // map of intersections with other set (Other Set, SetIntersection)
    private HashMap<Set, SetIntersection> m_intersectionsMap = new HashMap<>();
    
    private Circle m_circle;
    
    private int m_id = -1;

    private double m_size;
    private double m_specificSize;
    
    public Set(String name, double size, int id) {
        m_name = name;
        m_id = id;
        m_circle = new Circle(size);
        m_size = size;
    }

    public void setSpecificSize(double specificSize) {
        m_specificSize = specificSize;
    }

    public double getSpecificSize() {
        return m_specificSize;
    }
    
    public int getId() {
        return m_id;
    }
    
    public String getName() {
        return m_name;
    }
    
    public String getDisplayName() {
        return m_name+" : "+m_specificSize + "/"+m_size;
    }
    
    public Circle getCircle() {
        return m_circle;
    }
    
    public boolean intersect(Set s) {
        return m_intersectionsMap.containsKey(s);
    }
    
    public double getIntersection(Set s) {
        SetIntersection intersection = m_intersectionsMap.get(s);
        if (intersection == null) {
            return 0;
        }
        return intersection.getIntersectionSize();
    }
    
    public void addIntersection(SetIntersection intersection) {
        m_intersections.add(intersection);
        m_intersectionsMap.put(intersection.getOtherSet(this), intersection);
    }
    
    public int getNumberOfIntersections() {
        return m_intersections.size();
    }
    
    public int getNumberOfIntersectionsWithPositionedSet() {
        int nbIntersections = 0;
        for (Set s : m_intersectionsMap.keySet()) {
            if (s.getCircle().isPositionSet()) {
                nbIntersections++;
            }
        }
        return nbIntersections;
    }
    
    public double getIntersectionsSize() {
        double intersectionsSize = 0;
        
        for (SetIntersection intersection : m_intersections) {
            intersectionsSize += intersection.getIntersectionSize();
        }
        
        return intersectionsSize;
    }
    
    @Override
    public int compareTo(Set s) {
        
        // for the Sets with a circle whose position is already set,
        // we we put them at the end of the sorting list
        if (m_circle.isPositionSet()) {
            if (s.getCircle().isPositionSet()) {
                return 0;
            } else {
                return -1;
            }
        } else if (s.getCircle().isPositionSet()) {
            return 1;
        }
        
        
        int intersectionsWithPositionedSetDelta = getNumberOfIntersectionsWithPositionedSet()-s.getNumberOfIntersectionsWithPositionedSet();
        if (intersectionsWithPositionedSetDelta != 0) {
            return intersectionsWithPositionedSetDelta;
        }
        
        int intersectionsDelta = getNumberOfIntersections()-s.getNumberOfIntersections();
        if (intersectionsDelta != 0) {
            return intersectionsDelta;
        }
        
        double intersectionsSizeDelta = getIntersectionsSize()-s.getIntersectionsSize();
        if (intersectionsSizeDelta>0) {
            return 1;
        } else if (intersectionsSizeDelta<0) {
            return -1;
        }
        return 0;
    }
    

}
