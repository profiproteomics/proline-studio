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
    //private final String m_name;
    private String m_dataName;
    private String m_typeName = null;
    private final JXTable m_table;
    
    private ImageIcon m_icon = null;

    public TableInfo(int id, String dataName, String typeName, JXTable table) {
        m_id = id;
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
    
    /*public void setFullName(String fullName) {
        m_fullName = fullName;
    }*/
    
    /*public String getDataName() {
        if (m_dataName!=null) {
            return m_dataName;
        }
        if (m_fullName == null) {
            return null;
        }
        int index = m_fullName.lastIndexOf(m_name);
        if (index != -1) {
            String dataName = m_fullName.substring(0, index).trim();
            return dataName;
        }
        return null;
    }
    
    public void setDataName(String dataName) {
        m_dataName = dataName;
    }*/

    public ImageIcon getIcon() {
        return m_icon;
    }
    public void setIcon(ImageIcon icon) {
        m_icon = icon;
    }
    
    /*public String getFullName() {
        return m_fullName;
    }*/
    
   
    public String getNameWithId() {
        return m_id+": "+getFullName();
    }
    
    public String getDataName() {
        return m_dataName;
    }
    
    public String getTypeName() {
        return m_typeName;
    }
    
    public String getFullName() {
        return m_dataName+" "+m_typeName;
    }
    
    public int getId() {
        return m_id;
    }
    
    
}
