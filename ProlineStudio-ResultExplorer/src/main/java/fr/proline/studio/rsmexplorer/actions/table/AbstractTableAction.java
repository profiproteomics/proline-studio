package fr.proline.studio.rsmexplorer.actions.table;

import fr.proline.studio.pattern.AbstractDataBox;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JTable;

/**
 * Base class for all Actions on a Table
 * @author JM235353
 */
public abstract class AbstractTableAction extends AbstractAction {

    private final JTable m_table;
    
     public AbstractTableAction(JTable table, String name) {
        super(name);

        m_table = table;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        actionPerformed(m_table, m_table.getSelectedRows());
    }
    
    public abstract void actionPerformed(JTable table, int[] selectedRows);
    
}
