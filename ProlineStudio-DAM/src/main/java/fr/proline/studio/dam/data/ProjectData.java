package fr.proline.studio.dam.data;

import fr.proline.studio.dam.data.AbstractData;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadIdentificationTask;
import java.util.List;

/**
 * Correspond to the opened Project from UDS DB
 *
 * @author JM235353
 */
public class ProjectData extends AbstractData {

    private Project project = null;

    public ProjectData(Project project) {
        dataType = AbstractData.DataTypes.PROJECT;
        this.project = project;
    }

    @Override
    public String getName() {
        if (project != null) {
            return project.getName();
        }

        return "";
    }

    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list) {

        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseLoadIdentificationTask(callback, project, list));

    }
}
