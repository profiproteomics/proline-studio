package fr.proline.studio.rserver.data;

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
    protected RExpression m_RExpression;
    
    protected DataTypes m_dataType;

    
    /**
     * Subclasses must overriden this method to give the name of the data
     *
     * @return
     */
    public String getName() {
        return m_name;
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
    
    public void setRExpression(RExpression e) {
        m_RExpression = e;
    }

    public RExpression getRExpression() {
        return m_RExpression;
    }

}
