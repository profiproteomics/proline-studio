/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.dam.tasks;


import fr.proline.studio.dam.data.ProjectIdentificationData;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.UserAccount;
import fr.proline.core.orm.uds.ProjectUserAccountMap;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.data.DRawFile;
import fr.proline.studio.dam.tasks.data.ProjectInfo;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
    private Project m_p = null;
    private List<AbstractData> m_list = null;
    private String m_name = null;
    private String m_description = null;
    private List<UserAccount> m_userAccountList = null;
    private ArrayList<ProjectInfo> m_resultProjectsList = null;
    private ArrayList<DRawFile> m_resultRawFilesList = null;
    private List<Long> m_projectIds = null;

    private int m_action;

    private final static int LOAD_PROJECT   = 0;
    private final static int CHANGE_SETTINGS_PROJECT = 1;
    private final static int LOAD_PROJECTS_LIST = 2;
    private final static int LOAD_RAW_FILES_LIST = 3;

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

    /**
     * Load the Projects List
     * @param resultProjectsList
     */
    public void initLoadProjectsList(ArrayList<ProjectInfo> resultProjectsList) {
        setTaskInfo(new TaskInfo("Load Projects List", true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_resultProjectsList = resultProjectsList;
        
        m_action = LOAD_PROJECTS_LIST;
    }
    
    
    /**
     * Load the rawFiles associated to a list of projects
     * @param   projectIds: list of project ids to retrieve
     *          resultRawfiles: the resulting list of raw files
     */
    public void initLoadRawFilesList(List<Long> projectIds, ArrayList<DRawFile> resultRawfiles) {
        setTaskInfo(new TaskInfo("Load Raw files from projects List", true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_resultRawFilesList = resultRawfiles;
        m_projectIds = projectIds;
        m_action = LOAD_RAW_FILES_LIST;
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
            case LOAD_PROJECTS_LIST:
                return loadProjectsList();
            case LOAD_RAW_FILES_LIST:
                return loadRawFilesList();
        }
        return false; // should not happen
    }

    private boolean loadProject() {

        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        boolean result = true;
        try {
            entityManagerUDS.getTransaction().begin();

            TypedQuery<Project> projectQuery = entityManagerUDS.createQuery("SELECT p FROM fr.proline.core.orm.uds.Project p, fr.proline.core.orm.uds.UserAccount user, fr.proline.core.orm.uds.ProjectUserAccountMap puam  WHERE  user.login=:user AND user.id = puam.userAccount.id AND puam.project.id = p.id ORDER BY p.name ASC", Project.class);
            projectQuery.setParameter("user", m_user);
            List<Project> projectList = projectQuery.getResultList();



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

    private boolean loadProjectsList() {

        m_resultProjectsList.clear();
        
        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        boolean result = true;
        try {
            entityManagerUDS.getTransaction().begin();
            Query querySize = entityManagerUDS.createNativeQuery("" +
                    "SELECT p.id, p.name, p.description, p.serialized_properties, p.owner, d.size, p.db_name, p.last_dataset_date, raw_files_count\n" +
                    "FROM  (\n" +
                    "SELECT \n" +
                    "project.id as id, \n" +
                    "project.name as name, \n" +
                    "project.description as description, \n" +
                    "project.serialized_properties AS serialized_properties,\n" +
                    "user_account.login as owner, \n" +
                    "external_db.name as db_name,\n" +
                    "max(dataset.creation_timestamp) AS last_dataset_date,\n" +
                    "count(DISTINCT(run_identification.raw_file_identifier)) AS raw_files_count\n" +
                    "FROM \n" +
                    "  public.project_db_map, \n" +
                    "  public.external_db, \n" +
                    "  public.user_account,\n" +
                    "  public.project\n" +
                    "LEFT JOIN public.data_set dataset ON dataset.project_id = project.id\n" +
                    "LEFT JOIN public.run_identification ON dataset.id = run_identification.id\n" +
                    "WHERE \n" +
                    "  project.owner_id = user_account.id AND\n" +
                    "  project_db_map.project_id = project.id AND\n" +
                    "  project_db_map.external_db_id = external_db.id\n" +
                    "GROUP BY \n" +
                    "  project.id, user_account.login, external_db.name\n" +
                    ") p\n" +
                    "LEFT JOIN (SELECT \n" +
                    "  pg_database.datname as db_name,\n" +
                    "  pg_database_size(pg_database.datname) AS size, \n" +
                    "  pg_database_size(pg_database.datname) as raw_size\n" +
                    "FROM pg_database) d\n" +
                    "ON p.db_name = d.db_name\n" +
                    "ORDER BY p.id -- d.raw_size DESC");

            List<Object[]> results = querySize.getResultList();
            HashMap<Long, ProjectInfo> projectMap = new HashMap<>();
            Iterator<Object[]> it = results.iterator();
            while (it.hasNext()) {
                Object[] resCur = it.next();
                long id = ((BigInteger)resCur[0]).longValue();
                BigInteger sizeInBits = (BigInteger) resCur[5];
                double sizeInMB = (sizeInBits == null) ? 0.0 : sizeInBits.doubleValue()/(1024*1024);
                Timestamp lastDatasetDate = (Timestamp) resCur[7];
                ProjectInfo projectToDB = projectMap.get(id);
                if (projectToDB == null) {
                    projectToDB = new ProjectInfo(id, (String) resCur[1],(String) resCur[2],(String) resCur[3], (String)resCur[4]);
                    projectMap.put(id, projectToDB);
                    m_resultProjectsList.add(projectToDB);
                }
                projectToDB.addDb((String) resCur[6], sizeInMB);
                projectToDB.setLastDatasetDate(lastDatasetDate);
                projectToDB.setRawFilesCount(((BigInteger)resCur[8]).intValue());
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


    private boolean loadRawFilesList() {

        m_resultRawFilesList.clear();

        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        boolean result = true;
        try {
            entityManagerUDS.getTransaction().begin();
            Query query = entityManagerUDS.createNativeQuery("" +
                    "SELECT t.identifier, t.raw_file_cTimestamp, t.raw_file_properties, t.raw_file_dir, t.raw_file_name, t.project_count, array_to_string(t.project_ids,',')\n" +
                            "FROM \n" +
                            "(SELECT raw_file.identifier AS identifier, raw_file.creation_timestamp AS raw_file_cTimestamp, raw_file.serialized_properties AS raw_file_properties,\n" +
                            "        raw_file.mzdb_file_directory AS raw_file_dir, raw_file.mzdb_file_name AS raw_file_name, count(distinct(data_set.project_id)) AS project_count, array_agg(distinct(data_set.project_id)) AS project_ids \n" +
                            "\t\tFROM raw_file, run_identification, data_set\n" +
                            "\t\tWHERE data_set.id = run_identification.id\n" +
                            "\t\tAND run_identification.raw_file_identifier = raw_file.identifier \n" +
                            "\t\tGROUP BY raw_file.identifier ORDER BY identifier) t , run_identification, data_set, project\n" +
                            "WHERE data_set.id = run_identification.id\n" +
                            "AND run_identification.raw_file_identifier = t.identifier\n" +
                            "AND data_set.project_id = project.id\n" +
                            "AND project.id IN :projectIds\n" +
                            "group by project.id, project.name, project.owner_id, project.serialized_properties,  t.identifier,t.raw_file_cTimestamp, t.raw_file_properties, t.raw_file_dir, t.raw_file_name,t.project_count, t.project_ids\n" +
                            "ORDER BY identifier\n"
                    );
            query.setParameter("projectIds", m_projectIds);
            List<Object[]> results = query.getResultList();
            Iterator<Object[]> it = results.iterator();
            while (it.hasNext()) {
                Object[] resCur = it.next();
                DRawFile rawFile = new DRawFile();
                rawFile.setIdentifier((String)resCur[0]);
                rawFile.setCreationTimestamp((Timestamp)resCur[1]);
                rawFile.setSerializedProperties((String)resCur[2]);
                rawFile.setRawFileDirectory((String)resCur[3]);
                rawFile.setRawFileName((String)resCur[4]);
                rawFile.setProjectsCount(((BigInteger)resCur[5]).intValue());
                rawFile.setProjectIds((String)resCur[6]);
                m_resultRawFilesList.add(rawFile);
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

        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            Project p = entityManagerUDS.find(Project.class, m_p.getId());
            p.setName(m_name);
            p.setDescription(m_description);
            
            if (m_userAccountList != null) {
                int nb = m_userAccountList.size();
                
                m_p.getProjectUserAccountMap().clear();
                
                
                // remove members if needed
                LinkedList<UserAccount> userToBeRemovedList = new LinkedList<>();
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
                        userToBeRemovedList.add(userAccountFromProject);
                        
                    }
                }
                for (UserAccount userAccount : userToBeRemovedList) {
                    p.removeMember(userAccount);
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
