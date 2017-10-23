package fr.proline.studio.table;


import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JTable;

/**
 * Base class for all actions on JTable
 * @author JM235353
 */
public abstract class AbstractTableAction extends AbstractAction {
    
    private TablePopupMenu m_popup = null;
    
    public AbstractTableAction(String name) {
        super(name);
    }
    
    public Component getPopupPresenter() {
        return new JMenuItem(this);
    }
    
    public void setTablePopupMenu(TablePopupMenu popup) {
        m_popup = popup;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (m_popup == null) {
            return;
        }
        
        actionPerformed(m_popup.getCol(), m_popup.getRow(), m_popup.getSelectedRows(), m_popup.getTable());
    }
    
    public abstract void actionPerformed(int col, int row, int[] selectedRows, JTable table);
    

    
    public abstract void updateEnabled(int row, int col, int[] selectedRows, JTable table);
    

    
}
