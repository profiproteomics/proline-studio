package fr.proline.studio.types;

import java.util.ArrayList;

/**
 *
 * Class used to add information to a Table Model in the Data Analyzer
 * This class correspond to a list of groups of columns used for calculations
 * on SC or XIC.
 * 
 * @author JM235353
 */
public class GroupSelection {
    
    private final ArrayList<ArrayList<Integer>> m_groups;
    
    public GroupSelection() {
        m_groups = new ArrayList();
    }
    
    public void addGroup(ArrayList<Integer> group) {
        m_groups.add(group);
    }
    
    public int getNumberOfGroups() {
        return m_groups.size();
    }
    
    public ArrayList<Integer> getGroup(int i) {
        return m_groups.get(i);
    }
    
    
}
