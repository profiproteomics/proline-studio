package fr.proline.studio.dam.data;

import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.studio.dam.tasks.DatabaseDataSetTask;
import java.util.List;

/**
 * Correspond to the opened Project from UDS DB
 *
 * @author JM235353
 */
public class ProjectQuantitationData extends AbstractData {

    private Project m_project = null;
    private String m_temporaryName = null;

    public ProjectQuantitationData(Project project) {
        m_dataType = AbstractData.DataTypes.PROJECT_QUANTITATION;
        m_project = project;
    }
    
    public ProjectQuantitationData(String temporaryName) {
        m_dataType = AbstractData.DataTypes.PROJECT_QUANTITATION;
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
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list, AbstractDatabaseTask.Priority priority, boolean identificationDataset) {

        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initLoadParentDataset(m_project, list, false);
        if (priority != null) {
            task.setPriority(priority);
        }
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }
}
