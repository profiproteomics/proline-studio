package fr.proline.studio.filter.actions;

import fr.proline.studio.filter.FilterTableModelInterface;
import fr.proline.studio.table.AbstractTableAction;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 *
 * @author JM235353
 */
public class RestrainAction extends AbstractTableAction {

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
    }

    @Override
    public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
        setEnabled((selectedRows!=null) && (selectedRows.length>0));
    }
    
}
