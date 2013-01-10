package fr.proline.studio.dam.tasks;

import fr.proline.studio.dam.data.ProjectData;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.util.DatabaseManager;
import fr.proline.studio.dam.AccessDatabaseThread;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Load a Project from the UDS DB
 *
 * @author JM235353
 */
public class DatabaseProjectTask extends AbstractDatabaseTask {

    private Integer projectId = null;
    private List<AbstractData> list = null;
    private String name = null;

    private int action;
    
    private final static int LOAD_PROJECT   = 0;
    private final static int RENAME_PROJECT = 1;
    
    public DatabaseProjectTask(AbstractDatabaseCallback callback) {
        super(callback);

    }
    
    /**
     * To load a project
     * @param projectId
     * @param list 
     */
    public void initLoadProject(Integer projectId, List<AbstractData> list) {
        this.projectId = projectId;
        this.list = list;
        
        action = LOAD_PROJECT;
    }
    
    /**
     * To rename a project
     * @param projectId
     * @param list 
     */
    public void initRenameProject(Integer projectId, String name) {
        this.projectId = projectId;
        this.name = name;
        
        action = RENAME_PROJECT;
    }
    

    @Override
    public boolean needToFetch() {
        return true; // anyway this task is used only one time for each node

    }

    @Override
    public boolean fetchData() {
        switch (action) {
            case LOAD_PROJECT:
                return loadProject();
            case RENAME_PROJECT:
                return renameProject();
        }
        return false; // should not happen
    }

    private boolean loadProject() {

        EntityManager entityManagerUDS = DatabaseManager.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            Project project = entityManagerUDS.find(Project.class, projectId);
            list.add(new ProjectData(project));



            entityManagerUDS.getTransaction().commit();

        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName() + " failed", e);
            entityManagerUDS.getTransaction().rollback();
        }

        entityManagerUDS.close();

        // initialize the MSI DB in batch 
        DatabaseConnectionTask msiConnection = new DatabaseConnectionTask(null, projectId);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(msiConnection);


        return true;
    }

    private boolean renameProject() {

        EntityManager entityManagerUDS = DatabaseManager.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            String hqlUpdate = "UPDATE Project p set p.name= :name where p.id = :projectId";
            Query renameQuery = entityManagerUDS.createQuery(hqlUpdate);
            renameQuery.setParameter("projectId", AccessDatabaseThread.getProjectIdTMP()); //JPM.TODO
            renameQuery.setParameter("name", name);
            renameQuery.executeUpdate();

            entityManagerUDS.getTransaction().commit();

        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName() + " failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }
}
