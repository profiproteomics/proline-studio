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

    private Project project = null;
    private String temporaryName = null;

    public ProjectData(Project project) {
        dataType = AbstractData.DataTypes.PROJECT;
        this.project = project;
    }
    
    public ProjectData(String temporaryName) {
        dataType = AbstractData.DataTypes.PROJECT;
        this.temporaryName = temporaryName;
    }
    
    

    @Override
    public String getName() {
        if (project != null) {
            return project.getName();
        }
        if (temporaryName != null) {
            return temporaryName;
        }
        
        return "";
    }
    
    public void setProject(Project project) {
        this.project = project;
        temporaryName = null;
    }
    
    public Project getProject() {
        return project;
    }

    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list) {

        DatabaseDataSetTask task = new DatabaseDataSetTask(callback);
        task.initLoadParentDataset(project, list);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }
}
