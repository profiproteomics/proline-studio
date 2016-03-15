package fr.proline.studio.types;

import java.util.ArrayList;

/**
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
