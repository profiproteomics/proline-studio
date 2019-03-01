package fr.proline.studio.table;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Front End to have access to the default renderers embeded in JTable
 * @author JM235353
 */
public class TableDefaultRendererManager {
    
    private static JTable m_t = null;
    
    public static TableCellRenderer getDefaultRenderer(Class c) {
        if (m_t == null) {
            m_t = new JTable();
        }
        return m_t.getDefaultRenderer(c);
    }
}
