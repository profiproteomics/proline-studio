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
package fr.proline.studio.filter.actions;


import fr.proline.studio.filter.FilterTableModelInterface;
import fr.proline.studio.table.AbstractTableAction;
import java.util.HashSet;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * Action to restrain the visible rows of a model
 * @author JM235353
 */
public abstract class RestrainAction extends AbstractTableAction {

    public RestrainAction() {
        super("View Selected Data");
    }


    @Override
    public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {
        TableModel tableModel = table.getModel();
        if (!(tableModel instanceof FilterTableModelInterface)) {
            return;
        }
        
        FilterTableModelInterface filterTableModel = (FilterTableModelInterface) tableModel;
        
        HashSet<Integer> restrainRowSet = filterTableModel.getRestrainRowSet();
        if (restrainRowSet == null) {
            restrainRowSet = new HashSet<>();
        } else {
            restrainRowSet.clear();
        }

        for (int i=0;i<selectedRows.length;i++) {
            int filteredModelRow = table.convertRowIndexToModel(selectedRows[i]);
            int originalModelRow = filterTableModel.convertRowToOriginalModel(filteredModelRow);
            restrainRowSet.add(originalModelRow);
        }
        
        filterTableModel.restrain(restrainRowSet);
        
        filteringDone();
    }

    public abstract void filteringDone();

    @Override
    public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
        setEnabled((selectedRows!=null) && (selectedRows.length>0));
    }
    
}
