package fr.proline.studio.graphics.venndiagram;

import fr.proline.studio.graphics.PlotVennDiagram;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;

/**
 *
 * @author JM235353
 */
public class IntersectArea implements Comparable<IntersectArea> {
    private final HashSet<Set> m_setIntersectedMap = new HashSet<>();
    
    private final HashSet<Set> m_setIntersectionsOriginMap = new HashSet<>();
    
    private Area m_intersectionArea = null;
    
    private String m_displayName = null;
    
    public IntersectArea(Area a) {
        m_intersectionArea = a;
    }
    
    public boolean isPotentialIntersect(IntersectArea intersectArea) {

        for (Set set1 : m_setIntersectedMap) {

            for (Set set2 : intersectArea.m_setIntersectedMap) {
                if (!set1.intersect(set2)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    
    public String getDisplayName(PlotVennDiagram vennDiagram) {
        if (m_displayName != null) {
            return m_displayName;
        }
        if (hasOneSet()) {
            m_displayName = getOnlySet().getDisplayName();
        } else {
            
            StringBuilder sb = new StringBuilder();
            
            Set[] setArray = new Set[m_setIntersectedMap.size()];
            setArray = m_setIntersectedMap.toArray(setArray);
            Arrays.sort(setArray, getSpectificSetComparator());
            for (int i = 0; i < setArray.length; i++) {
                sb.append("Set").append((setArray[i].getId()+1));
                if (i < setArray.length-1) {
                    sb.append(String.valueOf("\u2229")); // intersection character
                }
            }
            sb.append(": ");
            sb.append(vennDiagram.getIntersectionSize(setArray));
            
            m_displayName = sb.toString();
        }
        
        return m_displayName;
        
    }
    
    public  HashSet<Set> getIntersectedMap() {
        return m_setIntersectedMap;
    }
    
    public boolean hasOneSet() {
        return (m_setIntersectedMap.size() == 1);
    }
    
    public Set getOnlySet() {
        if (!hasOneSet()) {
            return null;
        }
        return m_setIntersectedMap.iterator().next();
    }
    
    public IntersectArea(Set s) {
        m_setIntersectedMap.add(s);
        m_setIntersectionsOriginMap.add(s);
        
        Circle c = s.getCircle();
        double x = c.getX() - c.getRadius();
        double y = (int) Math.round(c.getY() - c.getRadius());
        double size = c.getRadius() * 2;
        Ellipse2D.Double ellipse = new Ellipse2D.Double(x, y, size, size);
        
        m_intersectionArea = new Area(ellipse);

    }
    
    public void addSet(Set s) {
        m_setIntersectedMap.add(s);
        m_setIntersectionsOriginMap.add(s);
    }
    
    public void addOriginSet(Set s) {
        m_setIntersectionsOriginMap.add(s);
    }
    

    public Area getArea() {
        return m_intersectionArea;
    }
    
    public ArrayList<IntersectArea> intersect(IntersectArea otherArea) {
        
        int size1 = m_setIntersectionsOriginMap.size();
        int size2 = otherArea.m_setIntersectionsOriginMap.size();
        if (size1 <= size2) {
            if (testIntersectionDone(m_setIntersectionsOriginMap, otherArea.m_setIntersectionsOriginMap)) {
                return null;
            }
        } else if (testIntersectionDone(otherArea.m_setIntersectionsOriginMap, m_setIntersectionsOriginMap)) {
            return null;
        }

        // a1: intersection area : this INTER otherArea
        Area a1 = new Area(m_intersectionArea);
        a1.intersect(otherArea.getArea());
        if (a1.isEmpty()) {
            return null;
        }
        
        ArrayList<IntersectArea> resultList = new ArrayList<>();
        
        // a2: otherArea MINUS this
        Area a2 = new Area(otherArea.getArea());
        a2.subtract(m_intersectionArea);
        
        // a3: this area MINUS otherArea
        Area a3 = new Area(m_intersectionArea);
        a3.subtract(otherArea.getArea());
        
        
        IntersectArea ia1 = new IntersectArea(a1);
        for (Set s : m_setIntersectedMap) {
            ia1.addSet(s);
        }
        for (Set s : otherArea.m_setIntersectedMap) {
            ia1.addSet(s);
        }
        for (Set s : m_setIntersectionsOriginMap) {
            ia1.addOriginSet(s);
        }
        for (Set s : otherArea.m_setIntersectionsOriginMap) {
            ia1.addOriginSet(s);
        }
        resultList.add(ia1);
           
        IntersectArea ia2 = new IntersectArea(a2);
        for (Set s : otherArea.m_setIntersectedMap) {
            ia2.addSet(s);
        }
        for (Set s : m_setIntersectedMap) {
            ia2.addOriginSet(s);
        }
        for (Set s : otherArea.m_setIntersectedMap) {
            ia2.addOriginSet(s);
        }
        resultList.add(ia2);
        
        IntersectArea ia3 = new IntersectArea(a3);
        for (Set s : m_setIntersectedMap) {
            ia3.addSet(s);
        }
        for (Set s : m_setIntersectedMap) {
            ia3.addOriginSet(s);
        }
        for (Set s : otherArea.m_setIntersectedMap) {
            ia3.addOriginSet(s);
        }
        resultList.add(ia3);
        
        return resultList;
    }
    
    
    private boolean testIntersectionDone(HashSet<Set>  hashSet1, HashSet<Set>  hashSet2) {
        for (Set s1 : hashSet1) {
            if (! hashSet2.contains(s1)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(IntersectArea o) {
        int sizeA = m_setIntersectedMap.size();
        int sizeB = o.m_setIntersectedMap.size();
        if (sizeA != sizeB) {
            return sizeA-sizeB;
        }

        
        Object[] arrayA = m_setIntersectedMap.toArray();
        Arrays.sort(arrayA, getSpectificSetComparator());
        Object[] arrayB = o.m_setIntersectedMap.toArray();
        Arrays.sort(arrayB, getSpectificSetComparator());
        for (int i=0;i<sizeA;i++) {
            Set setA = (Set) arrayA[i];
            Set setB = (Set) arrayB[i];
            int delta = setA.getId()-setB.getId();
            if (delta != 0) {
                return delta;
            }
        }
        return 0;
        
        
    }
    private static Comparator getSpectificSetComparator() {
        if (m_specificSetComparator  == null) {
            m_specificSetComparator = new Comparator<Set>() {
                @Override
                public int compare(Set setA, Set setB) {

                    return setA.getId()-setB.getId();
                }
            };
        }
        return m_specificSetComparator;
    }
    
    private static Comparator m_specificSetComparator = null;
}
