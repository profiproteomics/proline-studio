package fr.proline.studio.rserver.data;


import fr.proline.studio.rserver.command.AbstractCommand;
import fr.proline.studio.rserver.command.RVar;

/**
 * Must be extended by classes used as User Data in RExplorer Nodes
 * @author JM235353
 */
public abstract class AbstractRData {

    public enum DataTypes {
        MAIN,
        MSN_SET,
        GRAPHIC
    }
    protected String m_name;
    protected String m_longDisplayName;
    protected AbstractCommand m_command;
    protected RVar m_var;
    
    protected DataTypes m_dataType;

    
    /**
     * Subclasses must overriden this method to give the name of the data
     *
     * @return
     */
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public DataTypes getDataType() {
        return m_dataType;
    }
    
    public void setLongDisplayName(String longDisplayName) {
        m_longDisplayName = longDisplayName;
    }
    
    public String getLongDisplayName() {
        return m_longDisplayName;
    }
    
    public void setCommand(AbstractCommand c) {
        m_command = c;
    }

    public AbstractCommand getCommand() {
        return m_command;
    }

    public void setVar(RVar v) {
        m_var = v;
    }

    public RVar getVar() {
        return m_var;
    }
}
