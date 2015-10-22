package fr.proline.studio.id;

/**
 * id of a Project in uds
 * @author JM235353
 */
public class ProjectId {
    private long m_id = -1;
    
     public ProjectId() {
    }
     
    public ProjectId(long id) {
        m_id = id;
    }
    
    public void setId(long id) {
        m_id = id;
    }
    public long getId() {
        return m_id;
    }
}
