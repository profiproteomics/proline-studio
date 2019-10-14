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
package fr.proline.studio.export;

import fr.proline.studio.export.model.CustomExportTableModel;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * Code written by AW: should be re-read and cleaned up
 * @author AW
 */
public class TableUtil {

    public static ArrayList<Vector> getSelectedList(JTable table) {

        ArrayList<Vector> al = new ArrayList<>();
        Vector v = new Vector();
        v.add(table.getModel().getValueAt(table.getSelectedRow(), 0));
        v.add(table.getModel().getValueAt(table.getSelectedRow(), 1));
        v.add(table.getModel().getValueAt(table.getSelectedRow(), 2));
        al.add(v);
        return al;
    }

    public static void removeSelected(JTable table) {

        int[] rows = table.getSelectedRows();
        TableModel tm = table.getModel();

        while (rows.length > 0) {
            ((DefaultTableModel) tm).removeRow(table.convertRowIndexToModel(rows[0]));

            rows = table.getSelectedRows();
        }
        table.clearSelection();

    }
    
    /**
     * insert in the given table, a new row
     * @param table, the object table
     * @param obj, the new row to add
     * @param i , the position where the new row to add
     */
    public static void addRowAt(JTable table, Vector obj, int i) {
        //copie le column name in colNames
        Object[] colNames = new Object[table.getColumnCount()];
        for (int col = 0; col < table.getColumnCount(); col++) {
            colNames[col] = table.getColumnName(col);
        }
        //creat a new empty table model
        //0String, not editable, 1 String editable, 2 boolean editable
        CustomExportTableModel model = new CustomExportTableModel();
        //add in the new table, one row is a new Vector
        int nbRows = table.getModel().getRowCount();
        for (int row = 0; row < nbRows; row++) {
            Vector v = new Vector();
            if (row < i) {
                v.add(table.getModel().getValueAt(row, 0));
                v.add(table.getModel().getValueAt(row, 1));
                v.add(table.getModel().getValueAt(row, 2));
                model.addRow(v);
            } else if (row == i) { // add new element
                model.addRow(obj);
                v.add(table.getModel().getValueAt(row, 0));
                v.add(table.getModel().getValueAt(row, 1));
                v.add(table.getModel().getValueAt(row, 2));
                model.addRow(v);

            } else {
                v.add(table.getModel().getValueAt(row, 0));
                v.add(table.getModel().getValueAt(row, 1));
                v.add(table.getModel().getValueAt(row, 2));
                model.addRow(v);
            }

        }
        if (i == nbRows) {
            model.addRow(obj);
        }
        table.setModel(model);

    }

}
