package fr.proline.studio.dam.tasks;

import fr.proline.studio.dam.data.ProjectData;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.core.orm.uds.Project;
import fr.proline.repository.Database;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.List;
import javax.persistence.EntityManager;

/**
 * Load a Project from the UDS DB
 *
 * @author JM235353
 */
public class DatabaseLoadProjectTask extends AbstractDatabaseTask {

    private Integer projectId = null;
    private List<AbstractData> list = null;

    public DatabaseLoadProjectTask(AbstractDatabaseCallback callback, Integer projectId, List<AbstractData> list) {
        super(callback);
        this.projectId = projectId;
        this.list = list;

    }

    @Override
    public boolean needToFetch() {
        return true; // anyway this task is used only one time for each node

    }

    @Override
    public boolean fetchData() {


        EntityManager entityManagerUDS = ProlineDBManagement.getProlineDBManagement().getEntityManager(Database.UDS, true);
        try {
            entityManagerUDS.getTransaction().begin();

            Project project = entityManagerUDS.find(Project.class, projectId);
            list.add(new ProjectData(project));

            
            
            entityManagerUDS.getTransaction().commit();

        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            entityManagerUDS.getTransaction().rollback();
        }

        entityManagerUDS.close();

        // initialize the MSI DB in batch 
        DatabaseConnectionTask msiConnection = new DatabaseConnectionTask(null, projectId);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(msiConnection);


        return true;
    }
}
