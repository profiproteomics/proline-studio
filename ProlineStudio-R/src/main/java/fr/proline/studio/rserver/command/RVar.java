package fr.proline.studio.rserver.command;


/**
 *
 * @author JM235353
 */
public class RVar {
    
    public static final int MSN_SET = 0;
    public static final int GRAPHIC = 1;
    

    
    private String m_var = null;
    private int m_type;
    
    private String m_fullDisplay = null;
    

    private Object m_attachedData = null;
    
    public RVar() {
        
    }
    
    public RVar(String v, int type) {
        setVar(v, type);
    }
    
    public final void setVar(String v, int type) {
        m_var = v;
        m_type = type;
    }

    public String getVar() {
        return m_var;
    }

    public int getType() {
        return m_type;
    }
    
    @Override
    public String toString() {
        return m_var;
    }
    
    public String getFullDisplay() {
        if (m_fullDisplay == null) {
            return m_var;
        }
        return m_fullDisplay;
    }
    
    public void setFullDisplay(String fullDisplay) {
        m_fullDisplay = fullDisplay;
    }
    
    public void setAttachedData(Object attachedData) {
        m_attachedData = attachedData;
    }
    
    public Object getAttachedData() {
        return m_attachedData;
    }

}
