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
package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author JM235353
 */
public class ProteinPTMSiteTableModelProcessing {
    
    public static String calculateDataWORedundance(AbstractTableModel model, ArrayList<String> modificationsArray, ArrayList<Character> residuesArray, HashMap<Character, Integer> residuesMap, List<PTMSite> proteinPTMSiteArray,HashMap<String, Integer> modificationsMap) {

        
        int nbRows = model.getRowCount();

        // List of different modifications and residues
        TreeSet<String> modificationTreeSet = new TreeSet<>();
        TreeSet<Character> residueTreeSet = new TreeSet<>();

        for (int i = 0; i < nbRows; i++) {
            String modification = (String) model.getValueAt(i, ProteinPTMSiteTableModel.COLTYPE_MODIFICATION);
            modificationTreeSet.add(modification);

            Character residue = (Character) model.getValueAt(i, ProteinPTMSiteTableModel.COLTYPE_RESIDUE_AA);
            if (residue != null) {
                residueTreeSet.add(residue);
            }
        }

        modificationsArray.addAll(modificationTreeSet);

        for (int i = 0; i < modificationsArray.size(); i++) {
            modificationsMap.put(modificationsArray.get(i), i);
        }

        // List of different residues
        residuesArray.addAll(residueTreeSet);
        for (int i = 0; i < residuesArray.size(); i++) {
            residuesMap.put(residuesArray.get(i), i);
        }

        // Count for each type of modification, the number of modifications
        HashMap<String, Integer> globalDistinctModificationsMap = getModificationCount(model, proteinPTMSiteArray);

        StringBuilder sb = new StringBuilder();
        Iterator<String> it = globalDistinctModificationsMap.keySet().iterator();
        while (it.hasNext()) {
            String modification = it.next();
            Integer nbModifications = globalDistinctModificationsMap.get(modification);
            sb.append(modification).append(":").append(nbModifications);
            if (it.hasNext()) {
                sb.append("   ");
            }
        }
        
        return sb.toString();

    }

    private static HashMap<String, Integer> getModificationCount(AbstractTableModel model, List<PTMSite> proteinPTMSiteArray) {
           
        int rowCount = proteinPTMSiteArray.size();
        HashMap<String, Integer> globalDistinctModificationsMap = new HashMap<>();

        for (int i = 0; i < rowCount; i++) {
            String modification = (String) model.getValueAt(i, ProteinPTMSiteTableModel.COLTYPE_MODIFICATION);
            Integer nb = globalDistinctModificationsMap.get(modification);
            if (nb == null) {
                globalDistinctModificationsMap.put(modification, 1);
            } else {
                globalDistinctModificationsMap.put(modification, nb + 1);
            }
        }
        return globalDistinctModificationsMap;        
    }
    
}
