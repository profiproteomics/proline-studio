package fr.proline.studio.filter.actions;

import fr.proline.studio.filter.FilterTableModelInterface;
import fr.proline.studio.table.AbstractTableAction;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import javax.swing.AbstractAction;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 *
 * @author JM235353
 */
public class ClearRestrainAction extends AbstractTableAction {

    public ClearRestrainAction() {
        super("View All Data");
    }


    @Override
    public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {
        TableModel tableModel = table.getModel();
        if (!(tableModel instanceof FilterTableModelInterface)) {
            return;
        }
        
        FilterTableModelInterface filterTableModel = (FilterTableModelInterface) tableModel;
        
        filterTableModel.restrain(null);

    }

    @Override
    public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
        TableModel tableModel = table.getModel();
        if (!(tableModel instanceof FilterTableModelInterface)) {
            setEnabled(false);
            return;
        }
        
        FilterTableModelInterface filterTableModel = (FilterTableModelInterface) tableModel;
        setEnabled(filterTableModel.hasRestrain());
        
    }
    
}
