package fr.proline.studio.filter.actions;


import fr.proline.studio.filter.FilterTableModelInterfaceV2;
import fr.proline.studio.table.AbstractTableAction;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * Action used to clear the restrain put on displayed rows of a model
 * @author JM235353
 */
public abstract class ClearRestrainAction extends AbstractTableAction {

    public ClearRestrainAction() {
        super("View All Data");
    }


    @Override
    public void actionPerformed(int col, int row, int[] selectedRows, JTable table) {
        TableModel tableModel = table.getModel();
        if (!(tableModel instanceof FilterTableModelInterfaceV2)) {
            return;
        }
        
        FilterTableModelInterfaceV2 filterTableModel = (FilterTableModelInterfaceV2) tableModel;
        
        filterTableModel.restrain(null);

        filteringDone();
    }

    public abstract void filteringDone();
    
    @Override
    public void updateEnabled(int row, int col, int[] selectedRows, JTable table) {
        TableModel tableModel = table.getModel();
        if (!(tableModel instanceof FilterTableModelInterfaceV2)) {
            setEnabled(false);
            return;
        }
        
        FilterTableModelInterfaceV2 filterTableModel = (FilterTableModelInterfaceV2) tableModel;
        setEnabled(filterTableModel.hasRestrain());
        
    }
    
}
