package fr.proline.studio.table;


import java.awt.Point;
import java.util.ArrayList;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

/**
 *
 * @author JM235353
 */
public class TablePopupMenu extends JPopupMenu {

    private ArrayList<AbstractTableAction> m_actions = new ArrayList<>();
    
    private int m_row;
    private int m_col;
    private int[] m_selectedRows;
    private JTable m_table;

    
    public void addAction(AbstractTableAction action) {
        m_actions.add(action);
        if (action != null) {
            action.setTablePopupMenu(this);
        }
    }
    
    public int getRow() {
        return m_row;
    }
    
    public int getCol() {
        return m_col;
    }
    
    public int[] getSelectedRows() {
        return m_selectedRows;
    }
    
    public JTable getTable() {
        return m_table;
    }
    
    public void preparePopup() {

        int nbActions = m_actions.size();
        for (int i = 0; i < nbActions; i++) {
            AbstractTableAction action = m_actions.get(i);
            if (action == null) {
                addSeparator();
            } else {
                add(action.getPopupPresenter());
            }
        }

    }
    
    public void show(int mouseX, int mouseY, JTable table) {
        
        Point mousePoint = new Point(mouseX, mouseY);
        
        m_table = table;
        m_selectedRows = table.getSelectedRows();
        m_row = table.rowAtPoint(mousePoint);
        m_col = table.columnAtPoint(mousePoint);  

        // update actions
        int nbActions = m_actions.size();
        for (int i = 0; i < nbActions; i++) {
            AbstractTableAction action = m_actions.get(i);
            if (action != null) {
                action.updateEnabled(m_row, m_col, m_selectedRows, table);
            }
        }

        show(table, mouseX, mouseY);

    }
    
    
    
}
