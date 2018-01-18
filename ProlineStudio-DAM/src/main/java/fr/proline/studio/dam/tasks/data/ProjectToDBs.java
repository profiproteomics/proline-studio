package fr.proline.studio.dam.tasks.data;

/**
 * Class Used as template to display all projects and databases size for 
 * Admin dialog
 * 
 * @author JM235353
 */
public class ProjectToDBs {
    
    private long m_projectId;
    private String m_name;
    private String m_description;
    private String m_user;
    private double m_size; // in MB
    private String m_dbname;
    
    
    public ProjectToDBs(long projectId, String name, String description, String user, double size, String dbname) {
        m_projectId = projectId;
        m_name = name;
        m_description = description;
        m_user = user;
        m_size = size;
        m_dbname = dbname;
    }
    
    public void addDb(String dbname, double size) {
        m_size += size;
        m_dbname += ", "+dbname; 
    }
    
    public long getProjectId() {
        return m_projectId;
    }
    
    public String getName() {
        return m_name;
    }
    
    public String getDescription() {
        return m_description;
    }
    
    public String getUser() {
        return m_user;
    }
    
    public double getSize() {
        return m_size;
    }
    
    public String getDBName() {
        return m_dbname;
    }
}
