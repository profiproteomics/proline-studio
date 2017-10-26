package fr.proline.studio.table;

import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import javax.swing.ImageIcon;
import org.jdesktop.swingx.JXTable;

/**
 * Information attached to a Table
 * 
 * @author JM235353
 */
public class TableInfo {
    
    private final int m_id;
    private String m_userName;
    private String m_dataName;
    private String m_typeName = null;
    private final JXTable m_table;
    
    private ImageIcon m_icon = null;

    public TableInfo(int id, String userName, String dataName, String typeName, JXTable table) {
        m_id = id;
        m_userName = userName;
        m_dataName = dataName;
        m_typeName = typeName;
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


    public ImageIcon getIcon() {
        return m_icon;
    }
    public void setIcon(ImageIcon icon) {
        m_icon = icon;
    }

    public String getNameWithId() {
        return m_id+": "+getFullName();
    }
    
    public String getDataName() {
        if (m_userName != null) {
            return "";
        }
        return m_dataName;
    }
    
    public String getTypeName() {
        if (m_userName != null) {
            return m_userName;
        }
        return m_typeName;
    }
    
    public String getFullName() {
        if (m_userName != null) {
            return m_userName;
        }
        if (m_dataName == null) {
            return m_typeName;
        }
        return m_dataName+" "+m_typeName;
    }
    
    public int getId() {
        return m_id;
    }
    
    
}
