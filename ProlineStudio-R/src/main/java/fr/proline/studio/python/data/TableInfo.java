package fr.proline.studio.python.data;

import fr.proline.studio.table.GlobalTableModelInterface;
import javax.swing.ImageIcon;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author JM235353
 */
public class TableInfo {
    
    private final int m_id;
    private final String m_name;
    private String m_fullName = null;
    private final JXTable m_table;
    
    private ImageIcon m_icon = null;

    public TableInfo(int id, String name, JXTable table) {
        m_id = id;
        m_name = name;
        m_table = table;
    }
    
    public GlobalTableModelInterface getModel() {
        if (m_table == null) {
            return null;
        }
        return (GlobalTableModelInterface) m_table.getModel();
    }
    
    public JXTable getTable() {
        return m_table;
    }
    
    public void setFullName(String fullName) {
        m_fullName = fullName;
    }

    public ImageIcon getIcon() {
        return m_icon;
    }
    public void setIcon(ImageIcon icon) {
        m_icon = icon;
    }
    
    public String getFullName() {
        return m_fullName;
    }
    
   
    public String getNameWithId() {
        return m_id+": "+m_name;
    }
    
    public int getId() {
        return m_id;
    }
    
    
}
