package fr.proline.studio.dam.tasks;

import fr.proline.studio.dam.data.ProjectIdentificationData;
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
    private String m_description = null;

    private int m_action;
    
    private final static int LOAD_PROJECT   = 0;
    private final static int CHANGE_NAME_DESCRIPTION_PROJECT = 1;
    
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
    public void initChangeNameAndDescriptionProject(long projectId, String name, String description) {
        setTaskInfo(new TaskInfo("Change Name/Description of a Project", true, TASK_LIST_INFO));
        m_projectId = projectId;
        m_name = name;
        m_description = description;
        
        m_action = CHANGE_NAME_DESCRIPTION_PROJECT;
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
            case CHANGE_NAME_DESCRIPTION_PROJECT:
                return changeNameDescriptionProject();
        }
        return false; // should not happen
    }

    private boolean loadProject() {

        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        boolean result = true;
        try {
            entityManagerUDS.getTransaction().begin();

            TypedQuery<Project> projectQuery = entityManagerUDS.createQuery("SELECT p FROM fr.proline.core.orm.uds.Project p, fr.proline.core.orm.uds.UserAccount user WHERE p.owner.id=user.id AND user.login=:user ORDER BY p.name ASC", Project.class);
            projectQuery.setParameter("user", m_user);
            List<Project> projectList = projectQuery.getResultList();


            //HashMap<Integer, ProjectData> projectMap = new HashMap<>();
            Iterator<Project> it = projectList.iterator();
            while (it.hasNext()) {
                Project projectCur = it.next();

                ProjectIdentificationData projectDataCur = new ProjectIdentificationData(projectCur);
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
            result = false;
        } finally {
            entityManagerUDS.close();
        }

        // initialize the MSI DB in batch //JPM.TODO ??? remove
        //DatabaseConnectionTask msiConnection = new DatabaseConnectionTask(null, projectId);
        //AccessDatabaseThread.getAccessDatabaseThread().addTask(msiConnection);


        return result;
    }

    private boolean changeNameDescriptionProject() {

        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            String hqlUpdate = "UPDATE Project p set p.name= :name, p.description= :description where p.id = :projectId";
            Query modifyProjectQuery = entityManagerUDS.createQuery(hqlUpdate);
            modifyProjectQuery.setParameter("projectId", m_projectId);
            modifyProjectQuery.setParameter("name", m_name);
            modifyProjectQuery.setParameter("description", m_description);
            modifyProjectQuery.executeUpdate();

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
