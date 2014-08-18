package fr.proline.studio.dam.data;

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.UDSDataManager;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.studio.dam.tasks.DatabaseProjectTask;
import java.util.List;

/**
 * User Data for Parent Node of all other Nodes in Result Explorer
 *
 * @author JM235353
 */
public class ParentData extends AbstractData {

    public ParentData() {
        m_dataType = DataTypes.MAIN;
        
        m_hasChildren = false;
    }

    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list, AbstractDatabaseTask.Priority priority) {
        DatabaseProjectTask task = new DatabaseProjectTask(callback);
        task.initLoadProject(UDSDataManager.getUDSDataManager().getProjectUserName(), list);
        if (priority != null) {
            task.setPriority(priority);
        }
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

    }

    @Override
    public String getName() {
        return "";
    }
}
