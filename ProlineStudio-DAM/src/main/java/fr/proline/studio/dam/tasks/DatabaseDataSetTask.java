package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.MsiSearch;
import fr.proline.core.orm.msi.SearchSetting;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.ExternalDb;
import fr.proline.core.orm.uds.IdentificationDataset;
import fr.proline.core.orm.msi.SearchSettingsSeqDatabaseMap;
import fr.proline.core.orm.msi.Enzyme;
import fr.proline.core.orm.uds.*;

import fr.proline.core.orm.uds.repository.ExternalDbRepository;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.repository.ProlineDatabaseType;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

/**
 * Used to load dataset in two cases :
 * - parent dataset of a project
 * - children of a dataset
 * @author JM235353
 */
public class DatabaseDataSetTask extends AbstractDatabaseTask {
    
    private Project m_project = null;
    private DDataset m_dataset = null;
    private ArrayList<DDataset> m_datasetList = null;
    private List<AbstractData> m_list = null;
    private ResultSummary m_rsm = null;
    private String m_name = null;
    private DDataset m_parentDataset = null;
    private Long m_resultSetId = null;
    private Long m_resultSummaryId = null;
    private Aggregation.ChildNature m_datasetType;
    private String m_aggregateName;
    private boolean m_hasSuffix = false;
    private int m_suffixStart = 0;
    private int m_suffixStop = 0;
    private Long m_datasetId = null;
    private List<Long> m_dsChildRSMIds = null;
    private List<String> m_dsNames = null;
    private boolean m_identificationDataset;
    
    private int m_action;
    
    private final static int LOAD_PARENT_DATASET   = 0;
    private final static int LOAD_CHILDREN_DATASET = 1;
    private final static int LOAD_RSET_AND_RSM_OF_DATASET = 2;
    private final static int LOAD_DATASET_FOR_RSM = 3;
    private final static int RENAME_DATASET = 4;
    private final static int CREATE_AGGREGATE_DATASET = 5;
    private final static int CREATE_IDENTIFICATION_DATASET = 6;
    private final static int MODIFY_VALIDATED_DATASET = 7;
    private final static int REMOVE_VALIDATION_OF_DATASET = 8;
    private final static int MODIFY_MERGED_DATASET = 9;
    private final static int EMPTY_TRASH = 10;
    private final static int LOAD_DATASET_AND_RSM_INFO = 11;
    private final static int LOAD_DATASET = 12;
    private final static int CLEAR_DATASET = 13;
    private final static int LOAD_QUANTITATION = 14;
     
    private static final Object WRITE_DATASET_LOCK = new Object();
    
    public DatabaseDataSetTask(AbstractDatabaseCallback callback) {
        super(callback, null);
    }
    
    /**
     * Load all Parent Dataset of a project
     * @param project
     * @param list 
     * @param identificationDataset
     */
    public void initLoadParentDataset(Project project, List<AbstractData> list, boolean identificationDataset) {
        setTaskInfo(new TaskInfo("Load "+(identificationDataset ? "Identification" : "Quantitation")+" Data for Project "+project.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_project = project;
        m_list = list;
        m_identificationDataset = identificationDataset;
        m_action = LOAD_PARENT_DATASET;
        setPriority(Priority.HIGH_1);
    }
    
    /**
     * Load all children dataset of a dataset
     * @param parentDataset
     * @param list 
     * @param identificationDataset
     */
    public void initLoadChildrenDataset(DDataset parentDataset, List<AbstractData> list, boolean identificationDataset) {
        setTaskInfo(new TaskInfo("Load Data for Dataset "+parentDataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_project = parentDataset.getProject();
        m_parentDataset = parentDataset;
        m_list = list;
        m_identificationDataset = identificationDataset;
        m_action = LOAD_CHILDREN_DATASET;
        setPriority(Priority.HIGH_1);
    }
    
    /**
     * Load Rset and Rsm of a dataset
     * @param dataset 
     */
    public void initLoadRsetAndRsm(DDataset dataset) {
        setTaskInfo(new TaskInfo("Load Search Result and Identification Summary for Dataset "+dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_dataset = dataset;

        m_action = LOAD_RSET_AND_RSM_OF_DATASET;
    }
    
    /**
     * Load Quantitation of a dataset
     * @param project 
     * @param dataset 
     */
    public void initLoadQuantitation(Project project, DDataset dataset) {
        setTaskInfo(new TaskInfo("Load Quantitation for Dataset "+dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_dataset = dataset;
        m_project = project ;

        m_action = LOAD_QUANTITATION;
    }

    /**
     * Load Rset and Rsm of a dataset list
     * @param datasetList 
     */
    public void initLoadRsetAndRsm(ArrayList<DDataset> datasetList) {
        setTaskInfo(new TaskInfo("Load Search Result and Identification Summary for multiple Dataset", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        
        m_datasetList = datasetList;

        m_action = LOAD_RSET_AND_RSM_OF_DATASET;
    }

    /**
     * Load a dataset from its rsm
     * @param rsm 
     */
    public void initLoadDatasetForRsm(ResultSummary rsm) {
        setTaskInfo(new TaskInfo("Load data for Identification Summary "+rsm.getId(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_rsm = rsm;
        m_action = LOAD_DATASET_FOR_RSM;
    }
    
    /**
     * rename a dataset
     * @param dataset
     * @param oldName
     * @param name
     */
    public void initRenameDataset(DDataset dataset, String oldName, String name) {
        setTaskInfo(new TaskInfo("Rename Dataset "+oldName+" to "+name, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_name = name;
        m_dataset = dataset;
        m_action = RENAME_DATASET;
    }

    
    public void initCreateDatasetAggregate(Project project, DDataset parentDataset, Aggregation.ChildNature datasetType, String aggregateName, ArrayList<DDataset> datasetList) {
        initCreateDatasetAggregate(project, parentDataset, datasetType, aggregateName, false, 0, 0, datasetList);
    }
    public void initCreateDatasetAggregate(Project project, DDataset parentDataset, Aggregation.ChildNature datasetType, String aggregateName, boolean hasSuffix, int suffixStart, int suffixStop, ArrayList<DDataset> datasetList) {
        setTaskInfo(new TaskInfo("Create Dataset "+aggregateName, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_project = project;
        m_parentDataset = parentDataset;
        m_datasetType = datasetType;
        m_aggregateName = aggregateName;
        m_hasSuffix = hasSuffix;
        m_suffixStart = suffixStart;
        m_suffixStop = suffixStop;
        m_datasetList = datasetList;
        m_action = CREATE_AGGREGATE_DATASET;
    }
    
    public void initCreateDatasetForIdentification(Project project, DDataset parentDataset, Aggregation.ChildNature datasetType, String aggregateName, Long resultSetId, Long resultSummaryId, ArrayList<DDataset> datasetList, TaskInfo taskInfo) {
        setTaskInfo(taskInfo);
        setPriority(Priority.HIGH_2);
        m_project = project;
        m_parentDataset = parentDataset;
        m_datasetType = datasetType;
        m_aggregateName = aggregateName;
        m_resultSetId = resultSetId;
        m_resultSummaryId = resultSummaryId;
        m_datasetList = datasetList;
        m_action = CREATE_IDENTIFICATION_DATASET;
    }
    
    public void initModifyDatasetForValidation(DDataset dataset, Long resultSummaryId, TaskInfo taskInfo) {
        setTaskInfo(taskInfo);
        setPriority(Priority.HIGH_2);
        m_dataset = dataset;
        m_resultSummaryId = resultSummaryId;
        m_action = MODIFY_VALIDATED_DATASET;
    }
    
    public void initModifyDatasetToRemoveValidation(DDataset dataset) {
        setTaskInfo(new TaskInfo("Remove Identification Summary from Dataset "+dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        setPriority(Priority.HIGH_2);
        m_dataset = dataset;
        m_action = REMOVE_VALIDATION_OF_DATASET;
    }
    
    public void initModifyDatasetForMerge(DDataset dataset, Long resultSetId, Long resultSummaryId, TaskInfo taskInfo) {
        setTaskInfo(taskInfo);
        setPriority(Priority.HIGH_2);
        m_dataset = dataset;
        m_resultSetId = resultSetId;
        m_resultSummaryId = resultSummaryId;
        m_action = MODIFY_MERGED_DATASET;
    }
    
    public void initEmptyTrash(DDataset trashDataset, boolean identificationDataset) {
        String type = Dataset.DatasetType.IDENTIFICATION.name();
        if (!identificationDataset) {
            type = Dataset.DatasetType.QUANTITATION.name();
        }
        setTaskInfo(new TaskInfo("Empty Trash "+type, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        setPriority(Priority.HIGH_2);
        m_dataset = trashDataset;
        m_identificationDataset = identificationDataset;
        m_action = EMPTY_TRASH;
    }

    /**
     * Load Dataset with specific ID
     * @param datasetId Dataset Id
     * @param project project Dataset belongs to
     */
    public void initLoadDatasetAndRSMInfo(Long datasetId, ArrayList<Long> rsmIds, ArrayList<DDataset> returnedDatasetList, ArrayList<String> returnedDatasetNames, Project project) {
        setTaskInfo(new TaskInfo("Load DataSet "+datasetId+" and get RSMs names", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_project = project;
        m_datasetId = datasetId;
        m_dsChildRSMIds = rsmIds;
        m_action = LOAD_DATASET_AND_RSM_INFO;
        m_datasetList = returnedDatasetList;
        m_dsNames = returnedDatasetNames;
        setPriority(Priority.HIGH_1);
    }
    
    public void initLoadDataset(Long datasetId, ArrayList<DDataset> returnedDatasetList) {
        setTaskInfo(new TaskInfo("Load DataSet "+datasetId, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_datasetId = datasetId;
        m_datasetList = returnedDatasetList;
        m_action = LOAD_DATASET;
        setPriority(Priority.HIGH_1);
    }
    
    /**
     * Load Rset and Rsm of a dataset
     * @param dataset 
     */
    public void initClearDataset(DDataset dataset) {
        setTaskInfo(new TaskInfo("Clear Dataset "+dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_dataset = dataset;

        m_action = CLEAR_DATASET;
    }
    
    
    @Override
    public boolean needToFetch() {

        switch (m_action) {

            case LOAD_PARENT_DATASET:
            case LOAD_CHILDREN_DATASET:
                // this task is used only one time for each node
                return true;
            case LOAD_DATASET_FOR_RSM:
                return (m_rsm.getTransientData().getDataSet() == null);
            case LOAD_RSET_AND_RSM_OF_DATASET:
                if (m_datasetList != null) {
                    int nbDataSet = m_datasetList.size();
                    for (int i=0;i<nbDataSet;i++) {
                        DDataset d = m_datasetList.get(i);
                        if (needToFetchRsetAndRsm(d)) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    return needToFetchRsetAndRsm(m_dataset);
                }
            case RENAME_DATASET:
            case CREATE_AGGREGATE_DATASET:
            case CREATE_IDENTIFICATION_DATASET:
            case MODIFY_VALIDATED_DATASET:
            case REMOVE_VALIDATION_OF_DATASET:
            case MODIFY_MERGED_DATASET:
            case EMPTY_TRASH:
            case LOAD_DATASET_AND_RSM_INFO:
            case LOAD_DATASET:
            case CLEAR_DATASET:
            case LOAD_QUANTITATION:
                return true; // done one time
         
        }

        return true; // should never be called
    }
    
    
    
    private boolean needToFetchRsetAndRsm(DDataset dataset) {
       if ((dataset.getResultSetId() != null) && (dataset.getResultSet() == null)) {
           // need to fetch a result set
           return true;
       }
       if ((dataset.getResultSummaryId() != null) && (dataset.getResultSummary() == null)) {
           // need to fetch a result summary
           return true;
       }
       return false;
    }
    
    
    @Override
    public boolean fetchData() {
        
        switch (m_action) {
            case LOAD_PARENT_DATASET:
                return fetchParentsDataSet();
            case LOAD_CHILDREN_DATASET:
                return fetchChildrenDataSet();
            case LOAD_RSET_AND_RSM_OF_DATASET:
                return fetchRsetAndRsm();
            case LOAD_DATASET_FOR_RSM:
                return fetchDatasetForRsm(m_rsm);
            case RENAME_DATASET:
                return renameDataset();
            case CREATE_AGGREGATE_DATASET:
                return createDataset(false);
            case CREATE_IDENTIFICATION_DATASET:
                return createDataset(true);
            case MODIFY_VALIDATED_DATASET:
                return modifyDatasetRSM();
            case REMOVE_VALIDATION_OF_DATASET:
                return removeValidationOfDataset();
            case MODIFY_MERGED_DATASET:
                return  modifyDatasetRsetAndRsm();
            case EMPTY_TRASH:
                return emptyTrash();
            case LOAD_DATASET_AND_RSM_INFO : 
                return fetchDatasetWithIDAndRSMInfo();
            case LOAD_DATASET:
                return fetchDataset();
            case CLEAR_DATASET:
                return clearDataset();
            case LOAD_QUANTITATION:
                return fetchQuantitation();
        }
        
        return false; // should never happen
    }

    public boolean fetchParentsDataSet() {

        long projectId = m_project.getId();

        ArrayList<Long> idList = new ArrayList<>();
        HashMap<Long, DDataset> ddatasetMap = new HashMap<>();
        ArrayList<Long> rsetIdList = new ArrayList<>();
        
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        try {
            entityManagerUDS.getTransaction().begin();


            
            // ---- load parent DataSet -----
            //JPM.HACK : there is a join done by Hibernate if we read the Aggregation at once,
            // But some Aggregation are null (for identifications) -> identifications are not loaded
            // So we load aggregations afterwards
            
            List<DDataset> datasetListSelected;
            if (m_identificationDataset) {
                TypedQuery<DDataset> dataSetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name, d.type, d.childrenCount, d.resultSetId, d.resultSummaryId, d.number) FROM Dataset d WHERE (d.parentDataset IS null) AND d.type<>:quantitationType AND d.project.id=:projectId  ORDER BY d.number ASC", DDataset.class);
                dataSetQuery.setParameter("projectId", projectId);
                dataSetQuery.setParameter("quantitationType", Dataset.DatasetType.QUANTITATION);
                datasetListSelected = dataSetQuery.getResultList();
            } else {
                TypedQuery<DDataset> dataSetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name, d.type, d.childrenCount, d.resultSetId, d.resultSummaryId, d.number) FROM Dataset d WHERE (d.parentDataset IS null) AND ( d.type=:quantitationType or d.type=:trashType)  AND d.project.id=:projectId  ORDER BY d.number ASC", DDataset.class);
                dataSetQuery.setParameter("projectId", projectId);
                dataSetQuery.setParameter("quantitationType", Dataset.DatasetType.QUANTITATION);
                dataSetQuery.setParameter("trashType", Dataset.DatasetType.TRASH);
                datasetListSelected = dataSetQuery.getResultList();
            }


            
            
            
            DDataset trash = null;
            Iterator<DDataset> it = datasetListSelected.iterator();
            while (it.hasNext()) {
                DDataset datasetCur = it.next();
               
                
                if (datasetCur.getType() == Dataset.DatasetType.TRASH) {
                    trash = datasetCur;
                } else {
                    m_list.add(new DataSetData(datasetCur));
                    Long resultSetId = datasetCur.getResultSetId();
                    if (resultSetId != null) {
                        rsetIdList.add(resultSetId);
                    }
                }
                
                                
                Long id = datasetCur.getId();
                idList.add(id);
                ddatasetMap.put(id, datasetCur);
            }
            
            
            // Load Aggregation and QuantitationMethod separately
            if (!m_list.isEmpty()) {

                // Load Aggregation
                Query aggregationQuery = entityManagerUDS.createQuery("SELECT d.id, d.aggregation FROM Dataset d WHERE d.id IN (:listId)");
                aggregationQuery.setParameter("listId", idList);
                List<Object[]> results = aggregationQuery.getResultList();
                Iterator<Object[]> itAgg = results.iterator();
                while (itAgg.hasNext()) {
                    Object[] resCur = itAgg.next();
                    Long id = (Long) resCur[0];
                    Aggregation aggregation = (Aggregation) resCur[1];
                    ddatasetMap.get(id).setAggregation(aggregation);
                }
                
                
                // Load QuantitationMethod
                Query quantitationQuery = entityManagerUDS.createQuery("SELECT d.id, d.method FROM Dataset d WHERE d.id IN (:listId)");
                quantitationQuery.setParameter("listId", idList);
                results = quantitationQuery.getResultList();
                itAgg = results.iterator();
                while (itAgg.hasNext()) {
                    Object[] resCur = itAgg.next();
                    Long id = (Long) resCur[0];
                    QuantitationMethod quantitationMethod = (QuantitationMethod) resCur[1];
                    ddatasetMap.get(id).setQuantitationMethod(quantitationMethod);
                }
                
   
        
            }
            
            

            if (trash != null) {
                m_list.add(new DataSetData(trash));
            }
            

    
            
            m_project.getTransientData().setChildrenNumber(m_list.size());
            
            // add the UDS connection to the Netbeans Service
            ExternalDb msiDb = ExternalDbRepository.findExternalByTypeAndProject(entityManagerUDS, ProlineDatabaseType.MSI, entityManagerUDS.merge(m_project));
            
            
            
            try {
                ConnectionManager cm = ConnectionManager.getDefault();

                JDBCDriver driver = JDBCDriverManager.getDefault().getDrivers( DatabaseDataManager.getDatabaseDataManager().getUdsJdbcDriver() )[0]; //JPM.WART : same driver for uds and msi

                String udsJdbcUrl = DatabaseDataManager.getDatabaseDataManager().getUdsJdbcURL();
                
                String msiJdbcUrl = udsJdbcUrl.substring(0, udsJdbcUrl.lastIndexOf('/')+1) + msiDb.getDbName();
                
                
                DatabaseConnection dbconn = DatabaseConnection.create(driver, msiJdbcUrl, msiDb.getDbUser(), "public", msiDb.getDbPassword(), true);

                cm.addConnection(dbconn);
            } catch (Exception e) {

                String message = e.getMessage();
                if ((message == null) || (message.indexOf("connection already exists") == -1)) { //JPM.WART : avoid error because the connection already exist
                    m_logger.error(getClass().getSimpleName() + " failed to add UDS connection to Services ", e);
                }
            }
            
            
            
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
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

        completeMergeInfo(projectId, rsetIdList);
        
        
        return true;
    }
    

    public boolean fetchChildrenDataSet() {

        long parentDatasetId = m_parentDataset.getId();

        ArrayList<Long> rsetIdList = new ArrayList<>();
        
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        try {
            entityManagerUDS.getTransaction().begin();

            ArrayList<Long> idList = new ArrayList<>();
            HashMap<Long, DDataset> ddatasetMap = new HashMap<>();

            //JPM.HACK : there is a join done by Hibernate if we read the Aggregation at once,
            // But some Aggregation are null (for identifications) -> identifications are not loaded
            // So we load aggregations afterwards
            TypedQuery<DDataset> datasetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name,  d.type, d.childrenCount, d.resultSetId, d.resultSummaryId, d.number)  FROM Dataset d WHERE d.parentDataset.id=:parentDatasetId ORDER BY d.number ASC", DDataset.class);
            if (m_parentDataset.getType() == Dataset.DatasetType.TRASH) {
                if (m_identificationDataset){
                    datasetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name,  d.type, d.childrenCount, d.resultSetId, d.resultSummaryId, d.number)  FROM Dataset d WHERE d.parentDataset.id=:parentDatasetId AND d.type<>:quantitationType ORDER BY d.number ASC", DDataset.class);
                }else { 
                    datasetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name,  d.type, d.childrenCount, d.resultSetId, d.resultSummaryId, d.number)  FROM Dataset d WHERE d.parentDataset.id=:parentDatasetId AND d.type=:quantitationType ORDER BY d.number ASC", DDataset.class);
                }
                datasetQuery.setParameter("quantitationType", Dataset.DatasetType.QUANTITATION);
            }
            datasetQuery.setParameter("parentDatasetId", parentDatasetId);
            List<DDataset> dataSetResultList = datasetQuery.getResultList();
            Iterator<DDataset> itDataset = dataSetResultList.iterator();
            while (itDataset.hasNext()) {
                DDataset datasetCur = itDataset.next();
                m_list.add(new DataSetData(datasetCur));
                
                Long id = datasetCur.getId();
                idList.add(id);
                ddatasetMap.put(id, datasetCur);
                Long resultSetId = datasetCur.getResultSetId();
                if (resultSetId!=null) {
                    rsetIdList.add(resultSetId);
                }
            }
            
            // Load Aggregation and QuantitationMethod separately
            if (!m_list.isEmpty()) {

                // Load Aggregation
                Query aggregationQuery = entityManagerUDS.createQuery("SELECT d.id, d.aggregation FROM Dataset d WHERE d.id IN (:listId)");
                aggregationQuery.setParameter("listId", idList);
                List<Object[]> results = aggregationQuery.getResultList();
                Iterator<Object[]> it = results.iterator();
                while (it.hasNext()) {
                    Object[] resCur = it.next();
                    Long id = (Long) resCur[0];
                    Aggregation aggregation = (Aggregation) resCur[1];
                    ddatasetMap.get(id).setAggregation(aggregation);
                }
                
                // Load QuantitationMethod
                Query quantitationQuery = entityManagerUDS.createQuery("SELECT d.id, d.method FROM Dataset d WHERE d.id IN (:listId)");
                quantitationQuery.setParameter("listId", idList);
                results = quantitationQuery.getResultList();
                it = results.iterator();
                while (it.hasNext()) {
                    Object[] resCur = it.next();
                    Long id = (Long) resCur[0];
                    QuantitationMethod quantitationMethod = (QuantitationMethod) resCur[1];
                    ddatasetMap.get(id).setQuantitationMethod(quantitationMethod);
                }
        
            }
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
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

        completeMergeInfo(m_parentDataset.getProject().getId(), rsetIdList);
        
        return true;
    }
    
        private boolean completeMergeInfo(Long projectId, ArrayList<Long> rsetIdList) {
        if (!m_list.isEmpty() && !rsetIdList.isEmpty()) {
            // fetch if there is a merged rsm
            EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();
            try {

                entityManagerMSI.getTransaction().begin();

                HashSet<Long> m_rsetFromMergedRsm = new HashSet<>();

                Query mergeInfoQuery = entityManagerMSI.createQuery("SELECT r.id, r.mergedRsmId FROM ResultSet r WHERE r.id IN (:listId)");
                mergeInfoQuery.setParameter("listId", rsetIdList);
                List<Object[]> results = mergeInfoQuery.getResultList();
                Iterator<Object[]> itMergedInfo = results.iterator();
                while (itMergedInfo.hasNext()) {
                    Object[] resCur = itMergedInfo.next();
                    Long rsetId = (Long) resCur[0];
                    Long mergedRsmId = (Long) resCur[1];
                    if (mergedRsmId != null) {
                        m_rsetFromMergedRsm.add(rsetId);
                    }
                }

                Iterator<AbstractData> it = m_list.iterator();
                while (it.hasNext()) {
                    AbstractData data = (AbstractData) it.next();
                   
                    if ( data.getDataType() != AbstractData.DataTypes.DATA_SET) {
                        continue;
                    }
                    DataSetData datasetData = (DataSetData) data;
                    DDataset dataset = datasetData.getDataset();
                    Long rsetId = dataset.getResultSetId();
                    if (m_rsetFromMergedRsm.contains(rsetId)) {
                        dataset.setMergeInformation(DDataset.MergeInformation.MERGE_IDENTIFICATION_SUMMARY);
                    } else {
                        dataset.setMergeInformation(DDataset.MergeInformation.MERGE_UNKNOW);
                    }
                }

                entityManagerMSI.getTransaction().commit();
            } catch (Exception e) {
                m_logger.error(getClass().getSimpleName() + " failed", e);
                m_taskError = new TaskError(e);
                try {
                    entityManagerMSI.getTransaction().rollback();
                } catch (Exception rollbackException) {
                    m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
                }
                return false;
            } finally {
                entityManagerMSI.close();
            }
        }

        return true;
    }

        public boolean fetchRsetAndRsm() {

        long projectId = -1;
        if (m_dataset != null) {
            projectId = m_dataset.getProject().getId();
        } else if (m_datasetList != null) {
            projectId = m_datasetList.get(0).getProject().getId();
        }
        
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();       
        try {

            entityManagerMSI.getTransaction().begin();

            if (m_datasetList != null) {
                int nbDataset = m_datasetList.size();
                for (int i=0; i<nbDataset;i++) {
                    fetchRsetAndRsmForOneDataset(entityManagerMSI, m_datasetList.get(i));
                }
                
            } else if (m_dataset != null) {
                fetchRsetAndRsmForOneDataset(entityManagerMSI, m_dataset);
            }
            entityManagerMSI.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerMSI.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerMSI.close();
        }

        return true;
    }
        
    public boolean fetchQuantitation() {

        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();       
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_project.getId()).getEntityManagerFactory().createEntityManager();
        try {

            entityManagerUDS.getTransaction().begin();
            entityManagerMSI.getTransaction().begin();

            // force initialization of lazy data (data will be needed for the display of properties)
            Dataset datasetDB = entityManagerUDS.find(Dataset.class, m_dataset.getId());
            QuantitationMethod quantMethodDB = datasetDB.getMethod();
            List<MasterQuantitationChannel> listMasterQuantitationChannels = datasetDB.getMasterQuantitationChannels();
            Set<QuantitationLabel> labels = quantMethodDB.getLabels();
            Map<String, Long> objectTreeIdByName = datasetDB.getObjectTreeIdByName();
            
            
            // fill the current object with the db object
            m_dataset.setQuantitationMethod(quantMethodDB);
            m_dataset.getQuantitationMethod().setLabels(labels);
            m_dataset.setDescription(datasetDB.getDescription());
            
            List<DMasterQuantitationChannel> masterQuantitationChannels = null;
            
            if (listMasterQuantitationChannels != null && !listMasterQuantitationChannels.isEmpty()) {
                masterQuantitationChannels = new ArrayList<>();
                for (Iterator<MasterQuantitationChannel> it = listMasterQuantitationChannels.iterator(); it.hasNext();) {
                    MasterQuantitationChannel masterQuantitationChannel = it.next();
                    List<QuantitationChannel> listQuantitationChannels = masterQuantitationChannel.getQuantitationChannels();
                    List<DQuantitationChannel> listDQuantChannels = new ArrayList();
                    for (QuantitationChannel qc : listQuantitationChannels) {
                        DQuantitationChannel dqc = new DQuantitationChannel(qc);
                        // search resultFileName  and raw path
                        String resultFileName = "";
                        String rawPath = "";
                         String queryMsi = "SELECT msi.resultFileName, pl.path "
                                + "FROM MsiSearch msi, Peaklist pl, ResultSet rs, ResultSummary rsm "
                                + " WHERE rsm.id=:rsmId AND rsm.resultSet.id = rs.id AND rs.msiSearch.id = msi.id "
                                + "AND msi.peaklist.id = pl.id ";
                        Query qMsi = entityManagerMSI.createQuery(queryMsi);
                        qMsi.setParameter("rsmId", qc.getIdentResultSummaryId());
                        try{
                            Object[] res = (Object[])qMsi.getSingleResult();
                            resultFileName = (String)res[0];
                            rawPath = (String)res[1];
                        }catch(NoResultException | NonUniqueResultException e){
                            
                        }
                        dqc.setResultFileName(resultFileName);
                        dqc.setRawFilePath(rawPath);
                        
                        listDQuantChannels.add(dqc);
                    }
                    DMasterQuantitationChannel dMaster = new DMasterQuantitationChannel(masterQuantitationChannel.getId(), masterQuantitationChannel.getName(), 
                            masterQuantitationChannel.getQuantResultSummaryId(), listDQuantChannels, masterQuantitationChannel.getDataset(),
                            masterQuantitationChannel.getSerializedProperties());
                    // load the dataset for which the id is stored in the serialized properties
                    Map<String, Object> propertiesMap = dMaster.getSerializedPropertiesAsMap();
                    DDataset identDataset = null;
                    if (propertiesMap != null) {
                       Object o = propertiesMap.get("ident_dataset_id");
                       if (o != null) {
                            Long identDatasetId = Long.parseLong(o.toString());
                            Dataset identDatasetDB = entityManagerUDS.find(Dataset.class, identDatasetId);
                            if (identDatasetDB != null) {
                                identDataset = new DDataset(identDatasetDB.getId(), identDatasetDB.getProject(), identDatasetDB.getName(), identDatasetDB.getType(), 
                                    identDatasetDB.getChildrenCount(), identDatasetDB.getResultSetId(), identDatasetDB.getResultSummaryId(), identDatasetDB.getNumber());
                            }
                       }
                    }
                    dMaster.setIdentDataset(identDataset);
                    // load the list of quantitation channel linked to this masterQuantitationChannel
                    if (listQuantitationChannels != null && !listQuantitationChannels.isEmpty()) {
                        int id2 = 0;
                        for (Iterator<QuantitationChannel> itChannel = listQuantitationChannels.iterator(); itChannel.hasNext();) {
                            QuantitationChannel quantitationChannel = itChannel.next();
                            // load biologicalSample
                            BiologicalSample biologicalSample = quantitationChannel.getBiologicalSample();
                            dMaster.getQuantitationChannels().get(id2).setBiologicalSample(biologicalSample);
                            id2++;
                        }
                    }
                    masterQuantitationChannels.add(dMaster);
                }// end of the for
            }
            m_dataset.setMasterQuantitationChannels(masterQuantitationChannels);
            
            // load ObjectTree corresponding to the QUANT_PROCESSING_CONFIG
            if (objectTreeIdByName != null && objectTreeIdByName.get("quantitation.label_free_config") != null){
                Long objectId = objectTreeIdByName.get("quantitation.label_free_config");
                String queryObject = "SELECT clobData FROM fr.proline.core.orm.uds.ObjectTree WHERE id=:objectId ";
                Query qObject = entityManagerUDS.createQuery(queryObject);
                qObject.setParameter("objectId", objectId);
                try{
                    String clobData = (String)qObject.getSingleResult();
                    ObjectTree objectTree = new ObjectTree();
                    objectTree.setId(objectId);
                    objectTree.setClobData(clobData);
                    m_dataset.setQuantProcessingConfig(objectTree);
                }catch(NoResultException | NonUniqueResultException e){
                            
                }
            }
            // load ObjectTree corresponding to the POST_QUANT_PROCESSING_CONFIG
            if (objectTreeIdByName != null && objectTreeIdByName.get("quantitation.post_quant_processing_config") != null){
                Long objectId = objectTreeIdByName.get("quantitation.post_quant_processing_config");
                String queryObject = "SELECT clobData FROM fr.proline.core.orm.uds.ObjectTree WHERE id=:objectId ";
                Query qObject = entityManagerUDS.createQuery(queryObject);
                qObject.setParameter("objectId", objectId);
                try{
                    String clobData = (String)qObject.getSingleResult();
                    ObjectTree objectTree = new ObjectTree();
                    objectTree.setId(objectId);
                    objectTree.setClobData(clobData);
                    m_dataset.setPostQuantProcessingConfig(objectTree);
                }catch(NoResultException | NonUniqueResultException e){
                            
                }
            }
            
            entityManagerUDS.getTransaction().commit();
            entityManagerMSI.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerUDS.getTransaction().rollback();
                entityManagerMSI.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerUDS.close();
            entityManagerMSI.close();
        }

        return true;
    }
    
    private void fetchRsetAndRsmForOneDataset(EntityManager entityManagerMSI, DDataset d) {

        Long rsetId = d.getResultSetId();
        if (rsetId != null) {
            ResultSet rsetFound = entityManagerMSI.find(ResultSet.class, rsetId);

            // force initialization of lazy data (data will be needed for the display of properties)
            MsiSearch msiSearch = rsetFound.getMsiSearch();
            if (msiSearch != null) {
                SearchSetting searchSetting = msiSearch.getSearchSetting();
                Set<Enzyme> enzymeSet = searchSetting.getEnzymes();
                Iterator<Enzyme> it = enzymeSet.iterator();
                while (it.hasNext()) {
                    it.next();
                }

                Set<SearchSettingsSeqDatabaseMap> searchSettingsSeqDatabaseMapSet = searchSetting.getSearchSettingsSeqDatabaseMaps();
                Iterator<SearchSettingsSeqDatabaseMap> itSeqDbMap = searchSettingsSeqDatabaseMapSet.iterator();
                while (itSeqDbMap.hasNext()) {
                    itSeqDbMap.next();

                }
            }
            
            
            d.setResultSet(rsetFound);
        }

        Long rsmId = d.getResultSummaryId();
        if (rsmId != null) {
            ResultSummary rsmFound = entityManagerMSI.find(ResultSummary.class, rsmId);

            rsmFound.getTransientData().setDDataset(d);
            d.setResultSummary(rsmFound);
        }
    }
    
    
    private boolean fetchDatasetForRsm(ResultSummary rsm) {
        
        Long rsmId = rsm.getId();

        
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        try {
            entityManagerUDS.getTransaction().begin();

            // load dataset for rsm
            TypedQuery<Dataset> dataSetQuery = entityManagerUDS.createQuery("SELECT d FROM Dataset d WHERE d.resultSummaryId=:rsmId", Dataset.class);
            dataSetQuery.setParameter("rsmId", rsmId);
            Dataset dataSet = dataSetQuery.getSingleResult();
            rsm.getTransientData().setDataSet(dataSet);
            


     
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
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
    
    private boolean fetchDataset() {
                
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();

        try {
            entityManagerUDS.getTransaction().begin();

            // *** load dataset for specified id
            TypedQuery<DDataset> dataSetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name, d.type, d.childrenCount, d.resultSetId, d.resultSummaryId, d.number)  FROM Dataset d WHERE d.id=:dsId", DDataset.class);
            dataSetQuery.setParameter("dsId", m_datasetId);
            DDataset ddataSet = dataSetQuery.getSingleResult();
            m_datasetList.add(ddataSet);
            
            Dataset.DatasetType datasetType = ddataSet.getType();
            if(datasetType.equals(Dataset.DatasetType.AGGREGATE)){
                 // Load Aggregation
                TypedQuery<Aggregation>  aggregationQuery = entityManagerUDS.createQuery("SELECT d.aggregation FROM Dataset d WHERE d.id = :dsId", Aggregation.class);
                aggregationQuery.setParameter("dsId", m_datasetId);
                Aggregation aggregation = aggregationQuery.getSingleResult();
                ddataSet.setAggregation(aggregation);
            }
            
            if(datasetType.equals(Dataset.DatasetType.QUANTITATION)){
                // Load QuantitationMethod
                TypedQuery<QuantitationMethod> quantitationQuery = entityManagerUDS.createQuery("SELECT d.method FROM Dataset d WHERE d.id = :dsId", QuantitationMethod.class);
                quantitationQuery.setParameter("dsId", m_datasetId);
                QuantitationMethod quantitationMethod = quantitationQuery.getSingleResult();
                ddataSet.setQuantitationMethod(quantitationMethod);
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
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }
    
    private boolean fetchDatasetWithIDAndRSMInfo() {
        
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        
        try {
            entityManagerUDS.getTransaction().begin();

            // *** load dataset for specified id
            TypedQuery<DDataset> dataSetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name,  d.type, d.childrenCount, d.resultSetId, d.resultSummaryId, d.number)  FROM Dataset d WHERE d.id=:dsId", DDataset.class);
            dataSetQuery.setParameter("dsId", m_datasetId);
            DDataset ddataSet = null;
            try{
                ddataSet = dataSetQuery.getSingleResult();
                m_datasetList.add(ddataSet);
            }catch(NoResultException e) {
                m_logger.error(getClass().getSimpleName()+" failed -- Dataset with id="+m_datasetId+" doesn't exist anymore in the database", e);
                m_taskError = new TaskError(e);
                try {
                    entityManagerUDS.getTransaction().rollback();
                } catch (Exception rollbackException) {
                    m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
                }
            return false;
            }
                
            TypedQuery<Aggregation>  aggregationQuery = entityManagerUDS.createQuery("SELECT d.aggregation FROM Dataset d WHERE d.id = :dsId", Aggregation.class);
            aggregationQuery.setParameter("dsId", m_datasetId);
            Aggregation aggregation = aggregationQuery.getSingleResult();
            ddataSet.setAggregation(aggregation);
            
            //m_datasetList.add(ddataSet);

            //****  Get all DS for searched RSMs
            Query rsmDSQuery = entityManagerUDS.createQuery("SELECT d.id, d.resultSummaryId FROM Dataset d WHERE d.resultSummaryId IN (:listId) and d.project.id = :pjId ");
            rsmDSQuery.setParameter("listId", m_dsChildRSMIds);
            rsmDSQuery.setParameter("pjId", m_project.getId());
            Map<Long,List<Long>> dsIdPerRSMIds = new HashMap<>();
            Iterator<Object[]> it = rsmDSQuery.getResultList().iterator();
            while (it.hasNext()) {
                Object[] resCur = it.next();
                Long dsId = (Long) resCur[0];
                Long rsmId = (Long) resCur[1];
                m_logger.debug("--- FOR RSM   "+rsmId+" DS = "+dsId);
                //A RSM could belong to multiple DS
                if(dsIdPerRSMIds.containsKey(rsmId)){
                    dsIdPerRSMIds.get(rsmId).add(dsId);
                } else {
                    ArrayList<Long> rsmDSs = new ArrayList<>(); 
                    rsmDSs.add(dsId);
                    dsIdPerRSMIds.put(rsmId, rsmDSs);   
                }                
            }
            
            //****  Get all none root DS informations
            Query dsQueries = entityManagerUDS.createQuery("SELECT d.id, d.parentDataset.id, d.name FROM Dataset d WHERE d.project.id = :pjId and d.parentDataset is not null ");
            dsQueries.setParameter("pjId", m_project.getId());
            List<Object[]> results = dsQueries.getResultList();
            Map<Long,Object[]> dsInfoPerDsIds = new HashMap<>();
            Iterator<Object[]> it2 = results.iterator();
            while (it2.hasNext()) {
                Object[] resCur = it2.next();
                Long dsId = (Long) resCur[0];
                Object[] dsInfo = new Object[2];
                dsInfo[0] = (Long) resCur[1];
                dsInfo[1] = (String) resCur[2];
                dsInfoPerDsIds.put(dsId, dsInfo);                
            }
            
            entityManagerUDS.getTransaction().commit();
            
            //*** Find DS of searched rsm and which belongs to specfied DS childs            
            for(Long nextRSMId : m_dsChildRSMIds){
                
                boolean foundDS = false; // Correct DS found
                Iterator<Long> rsmDSsIdIt = dsIdPerRSMIds.get(nextRSMId).iterator();
                
                while(!foundDS && rsmDSsIdIt.hasNext() ){
                    Long nextDSId = rsmDSsIdIt.next(); // DS in hierarchy to test 
                    Long rsmDSId = nextDSId; // Keep DS assciated to RSM
                    while(!foundDS && nextDSId != null){
                        if(m_datasetId.equals(nextDSId)){ // current DS RSM is the good one (in correct branch)
                            foundDS = true;
                            //Get initial DS for RSM
                            Object[] rsmDSInfo = dsInfoPerDsIds.get(rsmDSId);
                            if(rsmDSInfo == null){
                                // RSM DS is root so should be reference DS! 
                                m_dsNames.add(ddataSet.getName());
                            } else {
                                m_dsNames.add((String)rsmDSInfo[1]);
                            }     
                        } else {
                            //Get current DS info 
                            Object[] dsInfo = dsInfoPerDsIds.get(nextDSId);
                            if(dsInfo == null){ //No parent, root reached without finding ref DS. Search in other branch
                                nextDSId = null;
                            } else { // Set current DS = parent DS
                              nextDSId = (Long) dsInfo[0];
                            }
                        }                        
                    }// End search in current branch
                } //End search in all hierarchies for current RSM
                
            }//End go through all searched RSMs

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
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
 
   
    private boolean renameDataset() {
        
            
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        try {
            entityManagerUDS.getTransaction().begin();

            String renameSQL = "UPDATE Dataset d set d.name= :name where d.id = :datasetId";
            Query renameQuery = entityManagerUDS.createQuery(renameSQL);
            renameQuery.setParameter("datasetId", m_dataset.getId());
            renameQuery.setParameter("name", m_name);
            renameQuery.executeUpdate();
     
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
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
    
    private boolean clearDataset() {


        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            String renameSQL = "UPDATE Dataset d set d.resultSetId=null,d.resultSummaryId=null where d.id = :datasetId";
            Query renameQuery = entityManagerUDS.createQuery(renameSQL);
            renameQuery.setParameter("datasetId", m_dataset.getId());
            renameQuery.executeUpdate();

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
    
    private boolean createDataset(boolean identificationDataset) {
                 
        synchronized(WRITE_DATASET_LOCK) {
        
            EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
            try {
                entityManagerUDS.getTransaction().begin();




                if (m_hasSuffix) {
                    for (int i = m_suffixStart; i <= m_suffixStop; i++) {
                        String datasetName = m_aggregateName + String.valueOf(i);
                        createDataSetImpl(entityManagerUDS, datasetName, identificationDataset);
                    }

                } else {
                    createDataSetImpl(entityManagerUDS, m_aggregateName, identificationDataset);
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
                return false;
            } finally {
                entityManagerUDS.close();
            }

        }
        
        return true;
    }
    
    private void createDataSetImpl(EntityManager entityManagerUDS, String datasetName, boolean identificationDataset) {

        //JPM.TODO : reuse objects for multiple queries
        
        Project mergedProject = entityManagerUDS.merge(m_project);
        
        
        
        Dataset mergedParentDataset = (m_parentDataset == null) ? null : entityManagerUDS.find(Dataset.class, m_parentDataset.getId());
  
        
        Dataset d;
        if (identificationDataset) {
            d = new IdentificationDataset();
            d.setProject(mergedProject);
            d.setType(Dataset.DatasetType.IDENTIFICATION);
        } else {
            d = new Dataset(mergedProject);
            d.setType(Dataset.DatasetType.AGGREGATE);
            
            Aggregation aggregation = DatabaseDataManager.getDatabaseDataManager().getAggregation(m_datasetType);
            Aggregation mergedAggregation = entityManagerUDS.merge(aggregation);
            d.setAggregation(mergedAggregation);
 
        }

        d.setName(datasetName);
        d.setResultSetId(m_resultSetId);
        d.setResultSummaryId(m_resultSummaryId);
        d.setChildrenCount(0); // this aggregate has no child for the moment
        
        // number of children of the parent
        if (mergedParentDataset != null) {
            mergedParentDataset.addChild(d);
            /*try {
                m_parentDataset[0].addChild(d);
            } catch (org.hibernate.LazyInitializationException e) {
                // JPM.WART
                // if this exception happens : the children count has not been updated
                // I update it by hand
                m_parentDataset[0].setChildrenCount(mergedParentDataset.getChildrenCount());
                
            }*/
        } else {
            int childrenCount = m_project.getTransientData().getChildrenNumber();
            d.setNumber(childrenCount);
            m_project.getTransientData().setChildrenNumber(childrenCount+1);
        }

        entityManagerUDS.persist(d);
        if (mergedParentDataset != null) {
            entityManagerUDS.merge(mergedParentDataset);
        }

        DDataset ddataset = new DDataset(d.getId(), d.getProject(), d.getName(), d.getType(), d.getChildrenCount(), d.getResultSetId(), d.getResultSummaryId(), d.getNumber());
        ddataset.setAggregation(d.getAggregation());
        
        m_datasetList.add(ddataset);
    }
    
    
    private boolean modifyDatasetRSM() {
        
        synchronized(WRITE_DATASET_LOCK) {
        
            EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
            try {
                entityManagerUDS.getTransaction().begin();

                Dataset mergedDataset = entityManagerUDS.find(Dataset.class, m_dataset.getId());

                m_dataset.setResultSummaryId(m_resultSummaryId);
                mergedDataset.setResultSummaryId(m_resultSummaryId);
                                       
                entityManagerUDS.merge(mergedDataset);

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
        
        }
        
        return true;
    }
    
    private boolean removeValidationOfDataset() {

        synchronized (WRITE_DATASET_LOCK) {

            EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
            try {
                entityManagerUDS.getTransaction().begin();



                Dataset mergedDataset = entityManagerUDS.find(Dataset.class, m_dataset.getId());

                m_dataset.setResultSummaryId(null);
                mergedDataset.setResultSummaryId(null);
                
                m_dataset.setResultSummary(null);                          
                mergedDataset.getTransientData().setResultSummary(null);

                entityManagerUDS.merge(mergedDataset);

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

        }

        return true;
    }

    private boolean modifyDatasetRsetAndRsm() {

        synchronized (WRITE_DATASET_LOCK) {

            EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
            try {
                entityManagerUDS.getTransaction().begin();

                Dataset mergedDataset = entityManagerUDS.find(Dataset.class, m_dataset.getId());

                m_dataset.setResultSetId(m_resultSetId);
                mergedDataset.setResultSetId(m_resultSetId);
                
                m_dataset.setResultSummaryId(m_resultSummaryId);
                mergedDataset.setResultSummaryId(m_resultSummaryId);

                entityManagerUDS.merge(mergedDataset);

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

        }

        return true;
    }

    private boolean emptyTrash() {
        synchronized (WRITE_DATASET_LOCK) {
            EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
            try {
                entityManagerUDS.getTransaction().begin();

                Dataset mergedTrash = entityManagerUDS.find(Dataset.class, m_dataset.getId()); // dataset is the Trash
                removeChildren(entityManagerUDS, mergedTrash, m_identificationDataset);
                List<Dataset> children = mergedTrash.getChildren();
                mergedTrash.setChildren(children);
                mergedTrash.setChildrenCount(children == null ?0:children.size());
                if (children != null) {
                    int pos = 0;
                    for (Iterator<Dataset> it = children.iterator(); it.hasNext();) {
                        Dataset child = it.next();
                        child.setNumber(pos);
                        entityManagerUDS.persist(child);
                    }
                }
                entityManagerUDS.persist(mergedTrash);

                entityManagerUDS.getTransaction().commit();

            } catch (Exception e) {
                m_logger.error("updateDatasetAndProjectsTree failed", e);
                try {
                    entityManagerUDS.getTransaction().rollback();
                } catch (Exception rollbackException) {
                    m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
                }
                return false;
            } finally {
                entityManagerUDS.close();
            }

        }

        return true;
    }

    private void removeChildren(EntityManager entityManagerUDS, Dataset d, boolean identificationTrash) {
        List<Dataset> children = d.getChildren();
        if (children != null) {
            int nbChildren = children.size() ;
            for (int i = nbChildren-1; i >= 0; i--) {
                Dataset child = children.get(i);
                removeChildren(entityManagerUDS, child, identificationTrash);
                boolean isDatasetIdentification = (child.getMethod() == null);
                if ((isDatasetIdentification && identificationTrash) || (!isDatasetIdentification && !identificationTrash)) {
                    children.remove(child);
                }
            }
            //children.clear();
        }
        boolean isDatasetIdentification = (d.getMethod() == null);
        if ((isDatasetIdentification && identificationTrash) || (!isDatasetIdentification && !identificationTrash)) {
            entityManagerUDS.remove(d);
        }
    }

    /**
     * Called when the tree Project/Dataset has been modified by the user by
     * Drag & Drop
     *
     * @param databaseObjectsToModify HashMap whose keys can be Project or
     * Parent Dataset
     */
    public static boolean updateDatasetAndProjectsTree(LinkedHashMap<Object, ArrayList<DDataset>> databaseObjectsToModify, boolean identificationTree) {
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();


            Iterator it = databaseObjectsToModify.keySet().iterator();
            while (it.hasNext()) {
                Object parentObject = it.next();
                ArrayList<DDataset> datasetList = databaseObjectsToModify.get(parentObject);

                int nbDataset = datasetList.size();
                ArrayList<Dataset> mergedDatasetList = new ArrayList<>(nbDataset);

                boolean childrenNotLoaded = false;
                for (int i = 0; i < nbDataset; i++) {
                    DDataset dataset = datasetList.get(i);
                    if (dataset == null) {
                        childrenNotLoaded = true; // some children have not been loaded already
                        continue;
                    }
                    Dataset mergedDataset = entityManagerUDS.find(Dataset.class, dataset.getId());
                    mergedDatasetList.add(mergedDataset);
                }


                DDataset parentDataset;
                Dataset mergedParentDataset = null;
                Project parentProject;
                //Project mergedParentProject = null;
                if (parentObject instanceof DDataset) {
                    parentDataset = (DDataset) parentObject;
                    mergedParentDataset = entityManagerUDS.find(Dataset.class, parentDataset.getId());
                    
                    if (mergedParentDataset.getType() == Dataset.DatasetType.TRASH) {
                        if (identificationTree) {
                            // load quanti trash
                            TypedQuery<DDataset> datasetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name,  d.type, d.childrenCount, d.resultSetId, d.resultSummaryId, d.number)  FROM Dataset d WHERE d.parentDataset.id=:parentDatasetId AND d.type=:quantitationType ORDER BY d.number ASC", DDataset.class);
                            datasetQuery.setParameter("parentDatasetId", mergedParentDataset.getId());
                            datasetQuery.setParameter("quantitationType",Dataset.DatasetType.QUANTITATION);
                            List<DDataset> quantiInTrash = datasetQuery.getResultList();
                             
                            mergedParentDataset.replaceAllChildren(mergedDatasetList);
                            for (Iterator<DDataset> qt = quantiInTrash.iterator(); qt.hasNext();) {
                               DDataset aQuantiInTrash = qt.next();
                               Dataset wholeDataset = entityManagerUDS.find(Dataset.class, aQuantiInTrash.getId());
                               mergedParentDataset.addChild(wholeDataset);
                            }
                              
                        }else  {
                            // load identification trash
                            TypedQuery<DDataset> datasetQuery = entityManagerUDS.createQuery("SELECT new fr.proline.core.orm.uds.dto.DDataset(d.id, d.project, d.name,  d.type, d.childrenCount, d.resultSetId, d.resultSummaryId, d.number)  FROM Dataset d WHERE d.parentDataset.id=:parentDatasetId AND d.type<>:quantitationType ORDER BY d.number ASC", DDataset.class);
                            
                            datasetQuery.setParameter("parentDatasetId", mergedParentDataset.getId());
                            datasetQuery.setParameter("quantitationType",Dataset.DatasetType.QUANTITATION);
                            List<DDataset> identificationInTrash = datasetQuery.getResultList();
                             
                            mergedParentDataset.replaceAllChildren(mergedDatasetList);
                            for (Iterator<DDataset> qt = identificationInTrash.iterator(); qt.hasNext();) {
                               DDataset aIdentificationInTrash = qt.next();
                               Dataset wholeDataset = entityManagerUDS.find(Dataset.class, aIdentificationInTrash.getId());
                               mergedParentDataset.addChild(wholeDataset);
                            }
                        }
                        // change number position for children
                        List<Dataset> childs = mergedParentDataset.getChildren();
                        int pos=0;
                        for (Dataset dataset : childs) {
                            dataset.setNumber(pos);
                            pos++;
                        }
                    }else if (childrenNotLoaded) {
                        for (int i=0;i<mergedDatasetList.size();i++) {
                            mergedParentDataset.addChild(mergedDatasetList.get(i));
                        }
                    } else {
                        mergedParentDataset.replaceAllChildren(mergedDatasetList);
                    }

                    
                    
                    parentDataset.setChildrenCount(mergedParentDataset.getChildrenCount());


                } else if (parentObject instanceof Project) {
                    parentProject = (Project) parentObject;
                    parentProject.getTransientData().setChildrenNumber(nbDataset);
                    
                    // order the children which can have been moved
                    for (int i = 0; i < mergedDatasetList.size(); i++) {
                        Dataset mergedChildDataset = mergedDatasetList.get(i);
                        mergedChildDataset.setNumber(i);
                    }
                }

               for (int i = 0; i < mergedDatasetList.size(); i++) {
                    Dataset mergedDataset = mergedDatasetList.get(i);
                    entityManagerUDS.merge(mergedDataset);
                }
                if (mergedParentDataset != null) {
                    entityManagerUDS.merge(mergedParentDataset);
                }


            }

            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error("updateDatasetAndProjectsTree failed", e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(DatabaseDataSetTask.class.getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }
}
