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
