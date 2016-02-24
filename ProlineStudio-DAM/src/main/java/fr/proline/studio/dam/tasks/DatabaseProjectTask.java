package fr.proline.studio.dam.tasks;

import fr.proline.studio.dam.data.ProjectIdentificationData;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.UserAccount;
import fr.proline.core.orm.uds.ProjectUserAccountMap;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Load a Project from the UDS DB
 *
 * @author JM235353
 */
public class DatabaseProjectTask extends AbstractDatabaseTask {

    private String m_user = null;
    private Project m_p = null;
    private List<AbstractData> m_list = null;
    private String m_name = null;
    private String m_description = null;
    private ArrayList<UserAccount> m_userAccountList = null;

    private int m_action;
    
    private final static int LOAD_PROJECT   = 0;
    private final static int CHANGE_SETTINGS_PROJECT = 1;
    
    public DatabaseProjectTask(AbstractDatabaseCallback callback) {
        super(callback, null);

    }
    
    /**
     * To load a project
     * @param user
     * @param list 
     */
    public void initLoadProject(String user, List<AbstractData> list) {
        setTaskInfo(new TaskInfo("Load Projects for User "+user, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_user = user;
        m_list = list;
        
        m_action = LOAD_PROJECT;
    }
    
    /**
     * To rename a project
     * @param p
     * @param name 
     * @param description
     * @param userAccountList
     */
    public void initChangeSettingsOfProject(Project p, String name, String description, ArrayList<UserAccount> userAccountList) {
        setTaskInfo(new TaskInfo("Change Settings of a Project", true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_p = p;
        m_name = name;
        m_description = description;
        m_userAccountList = userAccountList;
        
        m_action = CHANGE_SETTINGS_PROJECT;
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
            case CHANGE_SETTINGS_PROJECT:
                return changeProjectSettings();
        }
        return false; // should not happen
    }

    private boolean loadProject() {

        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        boolean result = true;
        try {
            entityManagerUDS.getTransaction().begin();

            TypedQuery<Project> projectQuery = entityManagerUDS.createQuery("SELECT p FROM fr.proline.core.orm.uds.Project p, fr.proline.core.orm.uds.UserAccount user, fr.proline.core.orm.uds.ProjectUserAccountMap puam  WHERE  user.login=:user AND user.id = puam.userAccount.id AND puam.project.id = p.id ORDER BY p.name ASC", Project.class);
            projectQuery.setParameter("user", m_user);
            List<Project> projectList = projectQuery.getResultList();


            //HashMap<Integer, ProjectData> projectMap = new HashMap<>();
            Iterator<Project> it = projectList.iterator();
            while (it.hasNext()) {
                Project projectCur = it.next();

                // avoid lazy initialization problem
                Set<ProjectUserAccountMap> members = projectCur.getProjectUserAccountMap();
                for (ProjectUserAccountMap projectUserAccount : members) {
                    projectUserAccount.getUserAccount();
                }
                
                ProjectIdentificationData projectDataCur = new ProjectIdentificationData(projectCur);
                projectDataCur.setHasChildren(true); // always has a Trash
                //projectMap.put(projectIdCur, projectDataCur);
                m_list.add(projectDataCur);
            }

 
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            result = false;
        } finally {
            entityManagerUDS.close();
        }



        return result;
    }

    private boolean changeProjectSettings() {

        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            Project p = entityManagerUDS.find(Project.class, m_p.getId());
            p.setName(m_name);
            p.setDescription(m_description);
            
            if (m_userAccountList != null) {
                int nb = m_userAccountList.size();
                //p.getProjectUserAccountMap().clear();
                m_p.getProjectUserAccountMap().clear();
                // remove members if needed
                for(ProjectUserAccountMap element : p.getProjectUserAccountMap()){
                    UserAccount userAccountFromProject = element.getUserAccount();
                    boolean isInList = false;
                    for(int i = 0; i < nb; i++) {
                        UserAccount userAccount = m_userAccountList.get(i);
                        if(userAccount.getId() == userAccountFromProject.getId()){
                            isInList = true;
                            break;
                        }
                    }
                    if(!isInList){
                        p.removeMember(userAccountFromProject);
                    }
                }
                //add the new members
                for (int i = 0; i < nb; i++) {
                    UserAccount userAccount = m_userAccountList.get(i);
                    UserAccount userAccountInDB = entityManagerUDS.find(UserAccount.class, userAccount.getId());
                    if (!p.isProjectMember(userAccountInDB)){
                        p.addMember(userAccountInDB, true); // TODO add write permissions
                    }
                    m_p.addMember(userAccount, true);// TODO add write permissions
                }
                
            }
            entityManagerUDS.merge(p);
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }
}
