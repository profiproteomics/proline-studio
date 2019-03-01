package fr.proline.studio.pattern;

/**
 * Definition of a parameter exhanged between databoxes
 * @author JM235353
 */
public class DataParameter {

    private Class m_c;
    private boolean m_isList;
    private boolean m_isCompulsory;

    
    public DataParameter(Class c, boolean isList) {
        this(c, isList, true);
    }
    public DataParameter(Class c, boolean isList, boolean isCompulsory) {
        m_c = c;
        m_isList = isList;
        m_isCompulsory = isCompulsory;
    }

    public Class getParameterClass() {
        return m_c;
    }
    
    public boolean getParameterIsList() {
        return m_isList;
    }
    
    @Override
    public boolean equals(Object p) {
        if (p instanceof DataParameter) {
            return m_c.equals(((DataParameter)p).m_c);
        }
        return false;
    }
    
    public boolean equalsData(Class dataC) {
        return m_c.equals(dataC);
    }

    public boolean isCompatibleWithOutParameter(DataParameter outParameter) {
        if (!m_c.equals(outParameter.m_c)) {
            return false;
        }
        if (m_isList && !outParameter.m_isList) {
            return false;
        }
        return true;

    }
    
    public boolean isCompulsory() {
        return m_isCompulsory;
    }
    
    @Override
    public int hashCode() {
        return m_c.hashCode();
    }
    
}