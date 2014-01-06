package fr.proline.studio.dam.tasks;

import fr.proline.studio.dam.data.ProjectData;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * Load a Project from the UDS DB
 *
 * @author JM235353
 */
public class DatabaseProjectTask extends AbstractDatabaseTask {

    private String m_user = null;
    private long m_projectId = -1;
    private List<AbstractData> m_list = null;
    private String m_name = null;

    private int m_action;
    
    private final static int LOAD_PROJECT   = 0;
    private final static int RENAME_PROJECT = 1;
    
    public DatabaseProjectTask(AbstractDatabaseCallback callback) {
        super(callback, null);

    }
    
    /**
     * To load a project
     * @param projectId
     * @param list 
     */
    public void initLoadProject(String user, List<AbstractData> list) {
        setTaskInfo(new TaskInfo("Load Projects for User "+user, false, TASK_LIST_INFO));
        m_user = user;
        m_list = list;
        
        m_action = LOAD_PROJECT;
    }
    
    /**
     * To rename a project
     * @param projectId
     * @param list 
     */
    public void initRenameProject(long projectId, String name) {
        setTaskInfo(new TaskInfo("Rename Project to "+name, true, TASK_LIST_INFO));
        m_projectId = projectId;
        m_name = name;
        
        m_action = RENAME_PROJECT;
    }

    
    @Override
    public boolean needToFetch() {
        return true; // anyway this task is used only one time for each node

    }

    @Override
    public boolean fetchData() {
        switch (m_action) {
            case LOAD_PROJECT:
                return loadProject();
            case RENAME_PROJECT:
                return renameProject();
        }
        return false; // should not happen
    }

    private boolean loadProject() {

        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            TypedQuery<Project> projectQuery = entityManagerUDS.createQuery("SELECT p FROM fr.proline.core.orm.uds.Project p, fr.proline.core.orm.uds.UserAccount user WHERE p.owner.id=user.id AND user.login=:user ORDER BY p.name ASC", Project.class);
            projectQuery.setParameter("user", m_user);
            List<Project> projectList = projectQuery.getResultList();


            //HashMap<Integer, ProjectData> projectMap = new HashMap<>();
            Iterator<Project> it = projectList.iterator();
            while (it.hasNext()) {
                Project projectCur = it.next();

                ProjectData projectDataCur = new ProjectData(projectCur);
                projectDataCur.setHasChildren(true); // always has a Trash
                //projectMap.put(projectIdCur, projectDataCur);
                m_list.add(projectDataCur);
            }
            
            
            /*if (projectMap.size() >0) {
                Query countQuery = entityManagerUDS.createQuery("SELECT p, count(d) FROM fr.proline.core.orm.uds.Project p, fr.proline.core.orm.uds.Dataset d WHERE d.project=p AND d.parentDataset=null AND p.id IN (:projectIds) GROUP BY p.id");
                countQuery.setParameter("projectIds", projectMap.keySet());
                List l = countQuery.getResultList();

                Iterator<Object[]> itCountQuery = l.iterator();
                while (itCountQuery.hasNext()) {
                    Object[] resCur = itCountQuery.next();
                    Project p = (Project) resCur[0];
                    Long countDataset = (Long) resCur[1];
                    if (countDataset == 0) {
                        countDataset += 1; // for Trash
                    }
                    projectMap.get(p.getId()).setHasChildren(countDataset > 0);
                }
            }*/
 
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerUDS.getTransaction().rollback();
        } finally {
            entityManagerUDS.close();
        }

        // initialize the MSI DB in batch //JPM.TODO ??? remove
        //DatabaseConnectionTask msiConnection = new DatabaseConnectionTask(null, projectId);
        //AccessDatabaseThread.getAccessDatabaseThread().addTask(msiConnection);


        return true;
    }

    private boolean renameProject() {

        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            String hqlUpdate = "UPDATE Project p set p.name= :name where p.id = :projectId";
            Query renameQuery = entityManagerUDS.createQuery(hqlUpdate);
            renameQuery.setParameter("projectId", m_projectId);
            renameQuery.setParameter("name", m_name);
            renameQuery.executeUpdate();

            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }
}
