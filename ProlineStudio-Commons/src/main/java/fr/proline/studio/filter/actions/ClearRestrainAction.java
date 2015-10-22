package fr.proline.studio.filter.actions;


import fr.proline.studio.filter.FilterTableModelInterface;
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
        if (!(tableModel instanceof FilterTableModelInterface)) {
            return;
        }
        
        FilterTableModelInterface filterTableModel = (FilterTableModelInterface) tableModel;
        
        filterTableModel.restrain(null);

        filteringDone();
    }

    public abstract void filteringDone();
    
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
