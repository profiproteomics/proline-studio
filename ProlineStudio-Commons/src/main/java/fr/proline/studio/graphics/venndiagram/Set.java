package fr.proline.studio.graphics.venndiagram;

import java.util.ArrayList;
import java.util.HashMap;


/**
 *
 * @author JM235353
 */
public class Set implements Comparable<Set> {
    
    private final String m_name;
    
    private ArrayList<SetIntersection> m_intersections = new ArrayList<>(16);
    private HashMap<Set, SetIntersection> m_intersectionsMap = new HashMap<>();
    
    public Circle m_circle;
    
    private int m_id = -1;

    private double m_size;
    
    public Set(String name, double size, int id) {
        m_name = name;
        m_id = id;
        m_circle = new Circle(size);
        m_size = size;
    }

    
    public int getId() {
        return m_id;
    }
    
    public String getName() {
        return m_name;
    }
    
    public String getDisplayName() {
        return "Set"+m_id+": "+m_name+" : "+m_size;
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
