/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

/**
 *
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
