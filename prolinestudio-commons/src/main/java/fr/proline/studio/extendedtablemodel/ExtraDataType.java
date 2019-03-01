package fr.proline.studio.extendedtablemodel;

/**
 * Type for extra data which can be provided by a TableModel extending ExtraDataForTableModelInterface.
 * 
 * @author JM235353
 */
public class ExtraDataType {
    
    private final Class m_class;
    private final boolean m_isList;
    
    /**
     * 
     * @param c   class of the extra data type
     * @param list  boolean indicating if it is a value or a list of values
     */
    public ExtraDataType(Class c, boolean list) {
        m_class = c;
        m_isList = list;
    }
    
    public Class getTypeClass() {
        return m_class;
    }
    
    public boolean isList() {
        return m_isList;
    }
    
}
