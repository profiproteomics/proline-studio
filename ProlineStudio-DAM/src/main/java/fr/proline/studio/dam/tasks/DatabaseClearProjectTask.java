
package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.data.ClearProjectData;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * clear project by removing rs and rsm
 * @author MB243701
 */
public class DatabaseClearProjectTask extends AbstractDatabaseTask {
    
    private int m_action;
    
    private final static int LOAD_DATA_PROJECT_CLEAR   = 0;
    private final static int LOAD_DATA_DATASET_CLEAR   = 1;
    private final static int LOAD_DATA_TRASH   = 2;
    
    private Project m_project = null;
    private List<ClearProjectData> m_listDataToClear;
    private List<ClearProjectData> m_listOpenedData;
    
    private Long m_projectId;
    private List<Long> m_datasetIds;
    private List<ClearProjectData> m_listDataToClearTrash;
    private List<ClearProjectData> m_listOpenedDataForDs;
    
    
    private Long m_trashId;
    private List<Long> m_trashDatasetIds;
    
    
    
    public DatabaseClearProjectTask(AbstractDatabaseCallback callback) {
        super(callback, null);
    }
    
    public void initLoadDataToClearProject(Project p, List<ClearProjectData> listDataToClear, List<ClearProjectData> openedData){
        setTaskInfo(new TaskInfo("Load Data to Delete for Project "+(p == null?"":p.getName()), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_project = p;
        m_listDataToClear = listDataToClear;
        m_listOpenedData = openedData;
        
        m_action = LOAD_DATA_PROJECT_CLEAR;
    }
    
    public void initLoadDataToClearTrash(Long projectId, List<Long> datasetIds, List<ClearProjectData> listDataToClearTrash, List<ClearProjectData> openedData){
        setTaskInfo(new TaskInfo("Load Data to Delete for  Trash ", true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        
        m_projectId = projectId;
        m_datasetIds = datasetIds;
        m_listDataToClearTrash = listDataToClearTrash;
        m_listOpenedDataForDs = openedData;
        
        m_action = LOAD_DATA_DATASET_CLEAR;
    }
    
    public void initLoadDataInTrash(Long projectId, Long trashId, List<Long> trashDatasetIds){
        setTaskInfo(new TaskInfo("Load Data In Trash "+trashId, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        
        m_projectId = projectId;
        m_trashId = trashId;
        m_trashDatasetIds = trashDatasetIds;
        
        m_action = LOAD_DATA_TRASH;
    }
    
    @Override
    public boolean needToFetch() {
        return true; 

    }

    @Override
    public boolean fetchData() {
        switch (m_action) {
            case LOAD_DATA_PROJECT_CLEAR:
                return loadDataToClearProject();
            case LOAD_DATA_DATASET_CLEAR:
                return loadDataToClearTrash();
            case LOAD_DATA_TRASH:
                return loadDataInTrash();
        }
        return false; // should not happen
    }
    
    private boolean loadDataToClearProject() {
        m_logger.info("loadDataToClearProject "+m_project.getId());
        return loadDataToClear(m_project.getId(), null, m_listDataToClear, m_listOpenedData);
    }
    
    
    private boolean loadDataToClearTrash() {
        String dsIds= "";
        m_logger.info("loadDataToClearTrash "+m_projectId +" for trash with dsIds = "+m_datasetIds.stream().map((dsId) -> dsId+",").reduce(dsIds, String::concat));
        return loadDataToClear(m_projectId, m_datasetIds, m_listDataToClearTrash, m_listOpenedDataForDs);
    }
    
    
    private boolean loadDataToClear(long projectId, List<Long> datasetIds, List<ClearProjectData> dataToClear, List<ClearProjectData> openedData){
        boolean result = true;
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            boolean hasDs = datasetIds != null && !datasetIds.isEmpty();
            List<Long> listResultSetIdsUds = new ArrayList();
            entityManagerUDS.getTransaction().begin();
            // get the rs 
            String queryRsUds = "SELECT DISTINCT(ds.resultSetId) "
                + "FROM fr.proline.core.orm.uds.Dataset ds  "
                + "WHERE ds.resultSetId IS NOT NULL AND ds.project.id=:projectId ";
            if (hasDs){
                queryRsUds += "AND ds.id IN (:list) ";
            }
            queryRsUds += "ORDER BY ds.resultSetId";
            TypedQuery<Long> queryAllRsUds = entityManagerUDS.createQuery(queryRsUds, Long.class);
            queryAllRsUds.setParameter("projectId", projectId);
            if (hasDs){
                queryAllRsUds.setParameter("list", datasetIds);
            }
            listResultSetIdsUds = queryAllRsUds.getResultList();
            // Get rsm used in quanti
            List<Long> listRsmIdsUds = new ArrayList();
            String queryRsmUds = "SELECT DISTINCT(mqc.quantResultSummaryId) "
                + "FROM fr.proline.core.orm.uds.MasterQuantitationChannel mqc, fr.proline.core.orm.uds.Dataset ds "
                + "WHERE mqc.dataset.id =ds.id AND "
                + "mqc.quantResultSummaryId IS NOT NULL AND "
                + "ds.project.id=:projectId  ";
            if (hasDs){
                queryRsmUds+="AND ds.id IN (:list)  ";
            }
            queryRsmUds+="ORDER BY mqc.quantResultSummaryId";
            TypedQuery<Long> queryAllRsmUds = entityManagerUDS.createQuery(queryRsmUds, Long.class);
                queryAllRsmUds.setParameter("projectId", projectId);
            if (hasDs){
                queryAllRsmUds.setParameter("list", datasetIds);
            }
            listRsmIdsUds = queryAllRsmUds.getResultList();
            String queryRsmUds2 = "SELECT DISTINCT(qc.identResultSummaryId) "
                + "FROM fr.proline.core.orm.uds.QuantitationChannel qc, fr.proline.core.orm.uds.Dataset ds "
                + "WHERE qc.dataset.id =ds.id AND "
                + "qc.identResultSummaryId IS NOT NULL AND "
                + "ds.project.id=:projectId   ";
            if (hasDs){
                queryRsmUds2+= "AND ds.id IN (:list)  ";
            }
            queryRsmUds2+= "ORDER BY qc.identResultSummaryId";
            TypedQuery<Long> queryAllRsmUds2 = entityManagerUDS.createQuery(queryRsmUds2, Long.class);
            queryAllRsmUds2.setParameter("projectId", projectId);
            if (hasDs){
                queryAllRsmUds2.setParameter("list", datasetIds);
            }
            listRsmIdsUds.addAll(queryAllRsmUds2.getResultList());
            // TODO get the rsm referenced in serialized properties of SC
            
            entityManagerUDS.getTransaction().commit();
            
            
            
            // MSI
            ArrayList<Long> openedRsmId = new ArrayList();
            ArrayList<Long> openedRsId = new ArrayList();
            for (ClearProjectData d : openedData) {
                if (d.isResultSet()){
                    openedRsId.add(d.getId());
                }else if (d.isResultSummary()){
                    openedRsmId.add(d.getId());
                }
            }
            entityManagerMSI.getTransaction().begin();
            // add the rs linked to quanti rsm
            String queryLinked = "SELECT rsm.resultSet.id "
                + "FROM fr.proline.core.orm.msi.ResultSummary rsm "
                + "WHERE rsm.resultSet.id IS NOT NULL AND rsm.id IN (:list) ";
            if (!openedRsmId.isEmpty()){
                queryLinked += " AND rsm.id NOT IN (:openedRsmlist) ";
            }
            if (!listRsmIdsUds.isEmpty()) {
                TypedQuery<Long> queryLinkedRs = entityManagerMSI.createQuery(queryLinked, Long.class);
                queryLinkedRs.setParameter("list", listRsmIdsUds);
                if (!openedRsmId.isEmpty()){
                    queryLinkedRs.setParameter("openedRsmlist", openedRsmId);
                }
                listResultSetIdsUds.addAll(queryLinkedRs.getResultList());
            }
            // get the decoy rs
            String queryRsDecoy = "SELECT rs.decoyResultSet.id "
                    + "FROM fr.proline.core.orm.msi.ResultSet rs "
                    + "WHERE rs.decoyResultSet.id IS NOT NULL AND rs.id IN (:list) ";
            if (!openedRsId.isEmpty()){
                queryRsDecoy += " AND (rs.id NOT IN (:openedRslist) AND rs.decoyResultSet.id NOT IN (:openedRslist)) ";
            }
            queryRsDecoy += " ORDER BY rs.decoyResultSet.id";
            if (!listResultSetIdsUds.isEmpty()) {
                TypedQuery<Long> sqlQueryDecoyRs = entityManagerMSI.createQuery(queryRsDecoy, Long.class);
                sqlQueryDecoyRs.setParameter("list", listResultSetIdsUds);
                if (!openedRsId.isEmpty()){
                    sqlQueryDecoyRs.setParameter("openedRslist", openedRsId);
                }
                listResultSetIdsUds.addAll(sqlQueryDecoyRs.getResultList());
            }
            //rs
            List<ResultSet> listResultSetMsi = new ArrayList();
            String queryRsMsi = "SELECT rs "
                    + "FROM fr.proline.core.orm.msi.ResultSet rs  "
                    + "WHERE rs.type not like 'DECOY%' AND ";
            if (hasDs){
                queryRsMsi += " rs.id  IN (:list) ";
            }else{
                queryRsMsi += " rs.id  NOT IN (:list) ";
            }
            if (!openedRsId.isEmpty()){
                queryRsMsi += " AND rs.id NOT IN (:openedRslist) ";
            }
            queryRsMsi += " ORDER BY rs.id ";
            if (listResultSetIdsUds.isEmpty()) {
                queryRsMsi = "SELECT rs FROM fr.proline.core.orm.msi.ResultSet rs WHERE rs.type not like 'DECOY%' ";
                if (!openedRsId.isEmpty()){
                    queryRsMsi += " AND  rs.id NOT IN (:openedRslist) ";
                }
                queryRsMsi += " ORDER BY rs.id ";
            }
            TypedQuery<ResultSet> queryAllRsMsi = entityManagerMSI.createQuery(queryRsMsi, ResultSet.class);
            if (!listResultSetIdsUds.isEmpty()) {
                queryAllRsMsi.setParameter("list", listResultSetIdsUds);
            }
            if (!openedRsId.isEmpty()){
                queryAllRsMsi.setParameter("openedRslist", openedRsId);
            }
            listResultSetMsi = queryAllRsMsi.getResultList();
            List<Long> listResultSetMsiIds = new ArrayList();
            for (ResultSet rs : listResultSetMsi) {
                listResultSetMsiIds.add(rs.getId());
            }
            // rsm
            List<ResultSummary> listRSMMsi = new ArrayList();
            String queryRSMMsi = "SELECT rsm "
                    + "FROM fr.proline.core.orm.msi.ResultSummary rsm  "
                    + "WHERE rsm.resultSet.id IN (:list) ";
            if (!openedRsmId.isEmpty()){
                queryRSMMsi += " AND rsm.id NOT IN (:openedRsmList) ";
            }
            if (listResultSetMsi.isEmpty()) {
                queryRSMMsi = "SELECT rsm FROM fr.proline.core.orm.msi.ResultSummary rsm   ";
                if (!openedRsmId.isEmpty()){
                    queryRSMMsi += " WHERE rsm.id NOT IN (:openedRsmList) ";
                }
            }
            
            queryRSMMsi += " ORDER BY rsm.id ";
            TypedQuery<ResultSummary> queryAllRSMMsi = entityManagerMSI.createQuery(queryRSMMsi, ResultSummary.class);
            if (!listResultSetMsi.isEmpty()) {
                queryAllRSMMsi.setParameter("list", listResultSetMsiIds);
            }
            if (!openedRsmId.isEmpty()){
                queryAllRSMMsi.setParameter("openedRsmList", openedRsmId);
            }
            listRSMMsi = queryAllRSMMsi.getResultList();
            entityManagerMSI.getTransaction().commit();
                
            // data
            // merge result data
            for (ResultSet rs : listResultSetMsi) {
                dataToClear.add(new ClearProjectData(projectId, rs));
            }
            for (ResultSummary rsm : listRSMMsi) {
                dataToClear.add(new ClearProjectData(projectId, rsm));
            }
        }catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerMSI.getTransaction().rollback();
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            result = false;
        } finally {
            entityManagerMSI.close();
            entityManagerUDS.close();
        }
        return result;
        
    }
    
    private boolean loadDataInTrash() {
        m_logger.info("loadDataInTrash "+m_trashId);
        boolean result = true;
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();
            Dataset ds = entityManagerUDS.find(Dataset.class, m_trashId);
            List<Dataset> children = ds.getChildren();
            children.stream().forEach((d) -> {
                m_trashDatasetIds.add(d.getId());
            });
            entityManagerUDS.getTransaction().commit();
        }catch (Exception e) {
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
    
}
