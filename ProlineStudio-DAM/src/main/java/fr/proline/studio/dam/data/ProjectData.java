package fr.proline.studio.dam.data;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import java.util.List;

/**
 * Correspond to the opened Project from UDS DB
 *
 * @author JM235353
 */
public class ProjectData extends AbstractData {

    private Project m_project = null;
    private String m_temporaryName = null;

    public ProjectData(Project project) {
        m_dataType = AbstractData.DataTypes.PROJECT;
        m_project = project;
    }
    
    public ProjectData(String temporaryName) {
        m_dataType = AbstractData.DataTypes.PROJECT;
        m_temporaryName = temporaryName;
    }
    
    

    @Override
    public String getName() {
        if (m_project != null) {
            return m_project.getName();
        }
        if (m_temporaryName != null) {
            return m_temporaryName;
        }
        
        return "";
    }
    
    public void setProject(Project project) {
        m_project = project;
        m_temporaryName = null;
    }
    
    public Project getProject() {
        return m_project;
    }

    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list) {

        list.add(new AllImportedData());
        
        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initLoadParentDataset(m_project, list);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }
}
