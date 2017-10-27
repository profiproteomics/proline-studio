package fr.proline.studio.types;

/**
 * Class used to add information to a Table Model in the Data Analyzer
 * This class corresponds to a reference to the Xic Group of a column
 * 
 * @author JM235353
 */
public class XicGroup {

    private Long m_id;
    private String m_name;
    
    public XicGroup(Long id, String name) {
        if (id == null) {
            m_id = -1l; // JPM.WART temporary wart for Spectral Count
        } else {
            m_id = id;
        }
        m_name = name;
    }
    
    public long getId() {
        return m_id;
    }
    
    public String getName() {
        return m_name;
    }

    
}
