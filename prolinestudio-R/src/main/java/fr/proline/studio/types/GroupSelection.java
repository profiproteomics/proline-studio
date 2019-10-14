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
