package fr.proline.studio.export;

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

    public static void addRowAt(JTable table, Vector obj, int i) {
        Object[] colNames = new Object[table.getColumnCount()];
        for (int col = 0; col < table.getColumnCount(); col++) {
            colNames[col] = table.getColumnName(col);
        }
        DefaultTableModel model = new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Internal field name", "Displayed field name", "Exported"
                }
        ) {
            Class[] types = new Class[]{
                java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean[]{
                false, true, true
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };

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
