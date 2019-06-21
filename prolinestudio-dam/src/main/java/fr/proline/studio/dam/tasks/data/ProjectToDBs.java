package fr.proline.studio.dam.tasks.data;

import java.util.Date;

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
    private String m_properties;
    private Date m_lastDatasetDate;
    private Integer m_rawFilesCount;


    public ProjectToDBs(long projectId, String name, String description, String properties, String user) {
        m_projectId = projectId;
        m_name = name;
        m_description = description;
        m_user = user;
        m_properties = properties;
    }
    
    public void addDb(String dbname, double size) {
        if (m_dbname == null) {
            m_dbname = dbname;
            m_size = size;
        } else {
            m_size += size;
            m_dbname += ", " + dbname;
        }
    }

    public void setLastDatasetDate(Date date) {
        m_lastDatasetDate = date;
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

    public String getProperties() {
        return m_properties;
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

    public Integer getRawFilesCount() {
        return m_rawFilesCount;
    }

    public void setRawFilesCount(int count) {
        m_rawFilesCount = count;
    }
}
