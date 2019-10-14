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

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
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
//    private final static int LOAD_DATA_TRASH   = 2;
    
    private Project m_project = null;
    private Long m_projectId;
    private List<ClearProjectData> m_listDataToClear;
    private List<ClearProjectData> m_listOpenedData;
      
//    private List<ClearProjectData> m_listDataToClearTrash;
//    private List<ClearProjectData> m_listOpenedDataForDs;
   
        
//    private Long m_trashId;
//    private List<Long> m_trashDatasetIds;
    
    
    
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
    
    public void initLoadDataToClearTrash(Long projectId, List<ClearProjectData> listDataToClearTrash, List<ClearProjectData> openedData){
        setTaskInfo(new TaskInfo("Load Data to Delete for  Trash ", true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        
        m_projectId = projectId;       
        m_listDataToClear = listDataToClearTrash;
        m_listOpenedData = openedData;
        
        m_action = LOAD_DATA_DATASET_CLEAR;
    }
    
//    public void initLoadDataInTrash(Long projectId, Long trashId, List<Long> trashDatasetIds){
//        setTaskInfo(new TaskInfo("Load Data In Trash "+trashId, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
//        
//        m_projectId = projectId;
//        m_trashId = trashId;
//        m_trashDatasetIds = trashDatasetIds;
//        
//        m_action = LOAD_DATA_TRASH;
//    }
    
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
//            case LOAD_DATA_TRASH:
//                return loadDataInTrash(m_trashId, m_trashDatasetIds);
        }
        return false; // should not happen
    }
    
    private boolean loadDataToClearProject() {
        m_logger.debug("loadDataToClearProject "+m_project.getId());
        return loadDataToClear(m_project.getId(), false, m_listDataToClear, m_listOpenedData);
    }
    
    
    private boolean loadDataToClearTrash() {      
        m_logger.debug("loadDataToClearTrash "+m_projectId );
        return loadDataToClear(m_projectId, true, m_listDataToClear, m_listOpenedData);
    }
    
    
    private boolean loadDataToClear(long projectId, boolean clearTrashDataOnly, List<ClearProjectData> dataToClear, List<ClearProjectData> openedData){
        boolean result = true;

        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(projectId).createEntityManager();
        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            
            List<Long> dsInTrash = new ArrayList<>();
            //Get Dataset in Trash to take into account in queries ! 
            String getTrashDSId = "SELECT ds.id "
                    + "FROM fr.proline.core.orm.uds.Dataset ds  "
                    + "WHERE ds.type = 'TRASH' AND ds.project.id=:projectId ";
            TypedQuery<Long> queryGetTrashDSId = entityManagerUDS.createQuery(getTrashDSId, Long.class);
            queryGetTrashDSId.setParameter("projectId", projectId);

            Long trashId = queryGetTrashDSId.getSingleResult();
            loadDataInTrash(trashId, dsInTrash);
            
            //boolean clearTrashDataOnly = datasetIds != null && !datasetIds.isEmpty();
            List<Long> listResultSetIdsUds;
            
            //--- Get Info in UDS 
            entityManagerUDS.getTransaction().begin();
            
            // get the rs referenced by dataset
            String queryRsUds = "SELECT DISTINCT(ds.resultSetId) "
                + "FROM fr.proline.core.orm.uds.Dataset ds  "
                + "WHERE ds.resultSetId IS NOT NULL AND ds.project.id=:projectId ";
            if (clearTrashDataOnly){
                queryRsUds += "AND ds.id IN (:trashList) ";
            } else if (!dsInTrash.isEmpty()){
                queryRsUds += "AND ds.id NOT IN (:trashList)  ";
            }
            queryRsUds += "ORDER BY ds.resultSetId";
            TypedQuery<Long> queryAllRsUds = entityManagerUDS.createQuery(queryRsUds, Long.class);
            queryAllRsUds.setParameter("projectId", projectId);
            if (!dsInTrash.isEmpty()) 
                queryAllRsUds.setParameter("trashList", dsInTrash);                
            
            listResultSetIdsUds = queryAllRsUds.getResultList();
          
            // Get rsm referenced by dataset
             List<Long> listRsmIdsUds = new ArrayList<Long>();
             String queryRsmUds = "SELECT DISTINCT(ds.resultSummaryId) "
                + "FROM fr.proline.core.orm.uds.Dataset ds  "
                + "WHERE ds.resultSummaryId IS NOT NULL AND ds.project.id=:projectId ";
            if (clearTrashDataOnly){
                queryRsmUds += "AND ds.id IN (:trashList) ";
            }else  if (!dsInTrash.isEmpty()) {
                queryRsmUds += "AND ds.id NOT IN (:trashList)  ";
            }
            
            queryRsmUds += "ORDER BY ds.resultSummaryId";
            TypedQuery<Long> queryAllRsmUds = entityManagerUDS.createQuery(queryRsmUds, Long.class);
            queryAllRsmUds.setParameter("projectId", projectId);
            if (!dsInTrash.isEmpty())
                queryAllRsmUds.setParameter("trashList", dsInTrash);                
            listRsmIdsUds = queryAllRsmUds.getResultList();
            
            
            // Get rsm used in quanti
            List<Long> listQtRsmIdsUds = new ArrayList<>();
            String queryQtRsmUds = "SELECT DISTINCT(mqc.quantResultSummaryId) "
                + "FROM fr.proline.core.orm.uds.MasterQuantitationChannel mqc, fr.proline.core.orm.uds.Dataset ds "
                + "WHERE mqc.quantDataset.id =ds.id AND "
                + "mqc.quantResultSummaryId IS NOT NULL AND "
                + "ds.project.id=:projectId  ";
            if (clearTrashDataOnly){
                queryQtRsmUds+="AND ds.id IN (:trashList)  ";
            } else if (!dsInTrash.isEmpty()){
                queryQtRsmUds += "AND ds.id NOT IN (:trashList)  ";
            }
            queryQtRsmUds+="ORDER BY mqc.quantResultSummaryId";
            TypedQuery<Long> queryAllQtRsmUds = entityManagerUDS.createQuery(queryQtRsmUds, Long.class);
            queryAllQtRsmUds.setParameter("projectId", projectId);
            if (!dsInTrash.isEmpty())
                queryAllQtRsmUds.setParameter("trashList", dsInTrash);                
            listQtRsmIdsUds = queryAllQtRsmUds.getResultList();
            
            // Get check in the quant channel to clear project or if it's identification dataset
            if (!clearTrashDataOnly || (clearTrashDataOnly && listQtRsmIdsUds.isEmpty())){
                String queryRsmUds2 = "SELECT DISTINCT(qc.identResultSummaryId) "
                        + "FROM fr.proline.core.orm.uds.QuantitationChannel qc, fr.proline.core.orm.uds.Dataset ds "
                        + "WHERE qc.dataset.id =ds.id AND "
                        + "qc.identResultSummaryId IS NOT NULL AND "
                        + "ds.project.id=:projectId   ";
                if (clearTrashDataOnly) {
                    queryRsmUds2 += "AND ds.id IN (:trashList)  ";
                } else if (!dsInTrash.isEmpty())  {
                    queryRsmUds2 += "AND ds.id NOT IN (:trashList)  ";
                }
                queryRsmUds2 += "ORDER BY qc.identResultSummaryId";
                TypedQuery<Long> queryAllRsmUds2 = entityManagerUDS.createQuery(queryRsmUds2, Long.class);
                queryAllRsmUds2.setParameter("projectId", projectId);
                if (!dsInTrash.isEmpty())
                    queryAllRsmUds2.setParameter("trashList", dsInTrash);                                
                
                listQtRsmIdsUds.addAll(queryAllRsmUds2.getResultList());
            }
            
            // !! TODO get the rsm referenced in serialized properties of SC
            
            entityManagerUDS.getTransaction().commit();
            
            
             //--- Get Info in MSI  
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
            if (!listQtRsmIdsUds.isEmpty()) {
                String queryLinked = "SELECT rsm.resultSet.id "
                        + "FROM fr.proline.core.orm.msi.ResultSummary rsm "
                        + "WHERE rsm.resultSet.id IS NOT NULL AND rsm.id IN (:list) ";
                if (!openedRsmId.isEmpty()) {
                    queryLinked += " AND rsm.id NOT IN (:openedRsmlist) ";
                }

                TypedQuery<Long> queryLinkedRs = entityManagerMSI.createQuery(queryLinked, Long.class);
                queryLinkedRs.setParameter("list", listQtRsmIdsUds);
                if (!openedRsmId.isEmpty()) {
                    queryLinkedRs.setParameter("openedRsmlist", openedRsmId);
                }
                List<Long> tempRSIdsUds = new ArrayList<Long>();
                tempRSIdsUds = queryLinkedRs.getResultList();
                listResultSetIdsUds.addAll(tempRSIdsUds);
            }
            
            // get the decoy rs for all listed RS
            if (!listResultSetIdsUds.isEmpty()) {
                String queryRsDecoy = "SELECT rs.decoyResultSet.id "
                        + "FROM fr.proline.core.orm.msi.ResultSet rs "
                        + "WHERE rs.decoyResultSet.id IS NOT NULL AND rs.id IN (:list) ";
                if (!openedRsId.isEmpty()) {
                    queryRsDecoy += " AND (rs.id NOT IN (:openedRslist) AND rs.decoyResultSet.id NOT IN (:openedRslist)) ";
                }
                queryRsDecoy += " ORDER BY rs.decoyResultSet.id";

                TypedQuery<Long> sqlQueryDecoyRs = entityManagerMSI.createQuery(queryRsDecoy, Long.class);
                sqlQueryDecoyRs.setParameter("list", listResultSetIdsUds);
                if (!openedRsId.isEmpty()) {
                    sqlQueryDecoyRs.setParameter("openedRslist", openedRsId);
                }
                listResultSetIdsUds.addAll(sqlQueryDecoyRs.getResultList());
            }
            
            // get the decoy rsm for ident listed RSM
            if (!listRsmIdsUds.isEmpty()) {
                String queryRsmDecoy = "SELECT rsm.decoyResultSummary.id "
                        + "FROM fr.proline.core.orm.msi.ResultSummary rsm "
                        + "WHERE rsm.decoyResultSummary.id IS NOT NULL AND rsm.id IN (:list) ";
                if (!openedRsmId.isEmpty()) {
                    queryRsmDecoy += " AND (rsm.id NOT IN (:openedRsmlist) AND rsm.decoyResultSummary.id NOT IN (:openedRsmlist)) ";
                }
                queryRsmDecoy += " ORDER BY rsm.decoyResultSummary.id";

                TypedQuery<Long> sqlQueryDecoyRsm = entityManagerMSI.createQuery(queryRsmDecoy, Long.class);
                sqlQueryDecoyRsm.setParameter("list", listRsmIdsUds);
                if (!openedRsmId.isEmpty()) {
                    sqlQueryDecoyRsm.setParameter("openedRsmlist", openedRsmId);
                }
                listRsmIdsUds.addAll(sqlQueryDecoyRsm.getResultList());
            }
            
            // if clearTrashDataOnly : listResultSetIdsUds/listRsmIdsUds contains all rsId/rsmId to del 
            // else listResultSetIdsUds/listRsmIdsUds contains all rsId/rsmId to keep   
            
            // Get RS to Del from MSI
            List<ResultSet> listResultSetMsi = new ArrayList();
            StringBuilder queryRsMsiBuilder = new StringBuilder("SELECT rs FROM fr.proline.core.orm.msi.ResultSet rs WHERE");
            if (!clearTrashDataOnly && listResultSetIdsUds.isEmpty()) {
                if (!openedRsId.isEmpty()){
                    queryRsMsiBuilder.append(" rs.id NOT IN (:openedRslist) ");
                }
            } else {
                if (clearTrashDataOnly){
                    queryRsMsiBuilder.append( " rs.id  IN (:list) ");
                }else{
                    queryRsMsiBuilder.append( " rs.id  NOT IN (:list) ");
                }
                if (!openedRsId.isEmpty()){
                    queryRsMsiBuilder.append(" AND rs.id NOT IN (:openedRslist) ");
                }
            }
            queryRsMsiBuilder.append(" ORDER BY rs.id ");
                                   
            TypedQuery<ResultSet> queryAllRsMsi = entityManagerMSI.createQuery(queryRsMsiBuilder.toString(), ResultSet.class);
            if (!listResultSetIdsUds.isEmpty()) {
                queryAllRsMsi.setParameter("list", listResultSetIdsUds);
            }
            if (!openedRsId.isEmpty()){
                queryAllRsMsi.setParameter("openedRslist", openedRsId);
            }
            if (!(clearTrashDataOnly && listResultSetIdsUds.isEmpty())){
                listResultSetMsi = queryAllRsMsi.getResultList();
            }
            
            List<Long> listResultSetMsiIds = new ArrayList();
            for (ResultSet rs : listResultSetMsi) {
                listResultSetMsiIds.add(rs.getId());
            }
            
            // Get RSM to Del from MSI
            listRsmIdsUds.addAll(listQtRsmIdsUds); //Add Quanti RSM references
            List<ResultSummary> listResultSummaryMsi = new ArrayList();
            StringBuilder queryRsmMsiBuilder = new StringBuilder("SELECT rsm FROM fr.proline.core.orm.msi.ResultSummary rsm ");
            if (!clearTrashDataOnly && listRsmIdsUds.isEmpty()) {
                if (!openedRsmId.isEmpty()){
                    queryRsmMsiBuilder.append("WHERE rsm.id NOT IN (:openedRsmlist) ");
                }
            } else {
                if (clearTrashDataOnly){
                    queryRsmMsiBuilder.append( "WHERE rsm.id  IN (:list) ");
                }else{
                    queryRsmMsiBuilder.append( "WHERE rsm.id  NOT IN (:list) ");
                }
                if (!openedRsmId.isEmpty()){
                    queryRsmMsiBuilder.append(" AND rsm.id NOT IN (:openedRsmlist) ");
                }
            }
            queryRsmMsiBuilder.append(" ORDER BY rsm.id ");
                                   
            TypedQuery<ResultSummary> queryAllRsmMsi = entityManagerMSI.createQuery(queryRsmMsiBuilder.toString(), ResultSummary.class);
            if (!listRsmIdsUds.isEmpty()) {
                queryAllRsmMsi.setParameter("list", listRsmIdsUds);
            }
            if (!openedRsmId.isEmpty()){
                queryAllRsmMsi.setParameter("openedRsmlist", openedRsmId);
            }
            if (!(clearTrashDataOnly && listRsmIdsUds.isEmpty())){
                listResultSummaryMsi = queryAllRsmMsi.getResultList();
            }
                    
            // Get RSM associated to RS to Del 
            List<ResultSummary> listRSMMsi = new ArrayList();
            if(!listResultSetMsi.isEmpty()) {
                String queryRSMMsi = "SELECT rsm "
                        + "FROM fr.proline.core.orm.msi.ResultSummary rsm  "
                        + "WHERE rsm.resultSet.id IN (:list) ";
                if (!openedRsmId.isEmpty()) {
                    queryRSMMsi += " AND rsm.id NOT IN (:openedRsmList) ";
                }
                queryRSMMsi += " ORDER BY rsm.id ";    
                TypedQuery<ResultSummary> queryAllRSMMsi = entityManagerMSI.createQuery(queryRSMMsi, ResultSummary.class);
                queryAllRSMMsi.setParameter("list", listResultSetMsiIds);
                if (!openedRsmId.isEmpty()){
                    queryAllRSMMsi.setParameter("openedRsmList", openedRsmId);
                }
                listRSMMsi = queryAllRSMMsi.getResultList();
            }
           
            for (ResultSummary rsm : listRSMMsi) {
                if(!listResultSummaryMsi.contains(rsm))
                    listResultSummaryMsi.add(rsm);
            }
            entityManagerMSI.getTransaction().commit();
           
            // data
            // merge result data
            for (ResultSet rs : listResultSetMsi) {
                ClearProjectData clearData = new ClearProjectData(projectId, rs);
                if(rs.getType().equals(ResultSet.Type.QUANTITATION)){
                   clearData.setSelected(true);
                   clearData.setIsEditable(false);
                }
                dataToClear.add(clearData);
            }
            for (ResultSummary rsm : listResultSummaryMsi) {
                ClearProjectData clearData = new ClearProjectData(projectId, rsm);
                if(rsm.getResultSet() != null && rsm.getResultSet().getType().equals(ResultSet.Type.QUANTITATION)){
                   clearData.setSelected(true);
                   clearData.setIsEditable(false);
                }
                dataToClear.add(clearData);
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
    
    private boolean loadDataInTrash(Long trash_Id, List<Long> trashDatasetIds) {
        m_logger.info("loadDataInTrash "+trash_Id);
        boolean result = true;
        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            Dataset ds = entityManagerUDS.find(Dataset.class, trash_Id);
            List<Dataset> children = ds.getChildren();
            do{
                children.stream().forEach((d) -> {
                    trashDatasetIds.add(d.getId());
                });
                children = loadChildren(children);
            }while(!children.isEmpty());
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
    
    private List<Dataset> loadChildren( List<Dataset> datasets){
        List<Dataset> allChildren = new ArrayList();
        for(Dataset ds : datasets){
            List<Dataset> children = ds.getChildren();
            allChildren.addAll(children);
        }
        
        return allChildren;
    }
    
}
