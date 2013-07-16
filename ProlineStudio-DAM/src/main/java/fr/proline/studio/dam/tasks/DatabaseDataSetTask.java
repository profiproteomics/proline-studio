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

import fr.proline.core.orm.uds.repository.ExternalDbRepository;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.repository.ProlineDatabaseType;
import fr.proline.studio.dam.UDSDataManager;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;

/**
 * Used to load dataset in two cases :
 * - parent dataset of a project
 * - children of a dataset
 * @author JM235353
 */
public class DatabaseDataSetTask extends AbstractDatabaseTask {
    
    private Project m_project = null;
    private Dataset m_dataset = null;
    private ArrayList<Dataset> m_datasetList = null;
    private List<AbstractData> m_list = null;
    private ResultSummary m_rsm = null;
    private String m_name = null;
    private Dataset m_parentDataset = null;
    private Long m_resultSetId = null;
    private Long m_resultSummaryId = null;
    private Aggregation.ChildNature m_datasetType;
    private String m_aggregateName;
    private boolean m_hasSuffix = false;
    private int m_suffixStart = 0;
    private int m_suffixStop = 0;
    
    
    
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
    
    private static final Object WRITE_DATASET_LOCK = new Object();
    
    public DatabaseDataSetTask(AbstractDatabaseCallback callback) {
        super(callback, null);
    }
    
    /**
     * Load all Parent Dataset of a project
     * @param project
     * @param list 
     */
    public void initLoadParentDataset(Project project, List<AbstractData> list) {
        setTaskInfo(new TaskInfo("Load Data for Project "+project.getName(), TASK_LIST_INFO));
        m_project = project;
        m_list = list;
        m_action = LOAD_PARENT_DATASET;
        setPriority(Priority.HIGH_1);
    }
    
    /**
     * Load all children dataset of a dataset
     * @param parentDataSet
     * @param list 
     */
    public void initLoadChildrenDataset(Dataset parentDataset, List<AbstractData> list) {
        setTaskInfo(new TaskInfo("Load Data for Aggregate "+parentDataset.getName(), TASK_LIST_INFO));
        m_project = parentDataset.getProject();
        m_parentDataset = parentDataset;
        m_list = list;
        m_action = LOAD_CHILDREN_DATASET;
        setPriority(Priority.HIGH_1);
    }
    
    /**
     * Load Rset and Rsm of a dataset
     * @param dataSet 
     */
    public void initLoadRsetAndRsm(Dataset dataset) {
        setTaskInfo(new TaskInfo("Load Search Result and Identification Summary for Dataset "+dataset.getName(), TASK_LIST_INFO));
        m_dataset = dataset;

        m_action = LOAD_RSET_AND_RSM_OF_DATASET;
    }

    /**
     * Load Rset and Rsm of a dataset list
     * @param dataSetList 
     */
    public void initLoadRsetAndRsm(ArrayList<Dataset> datasetList) {
        setTaskInfo(new TaskInfo("Load Search Result and Identification Summary for multiple Dataset", TASK_LIST_INFO));
        
        m_datasetList = datasetList;

        m_action = LOAD_RSET_AND_RSM_OF_DATASET;
    }

    /**
     * Load a dataset from its rsm
     * @param callback
     * @param rsm 
     */
    public void initLoadDatasetForRsm(ResultSummary rsm) {
        setTaskInfo(new TaskInfo("Load data for Identification Summary "+rsm.getId(), TASK_LIST_INFO));
        m_rsm = rsm;
        m_action = LOAD_DATASET_FOR_RSM;
    }
    
    /**
     * rename a dataset
     * @return 
     */
    public void initRenameDataset(Dataset dataset, String name) {
        setTaskInfo(new TaskInfo("Rename Dataset "+dataset.getName()+" to "+name, TASK_LIST_INFO));
        m_name = name;
        m_dataset = dataset;
        m_action = RENAME_DATASET;
    }

    
    public void initCreateDatasetAggregate(Project project, Dataset parentDataset, Aggregation.ChildNature datasetType, String aggregateName, ArrayList<Dataset> datasetList) {
        initCreateDatasetAggregate(project, parentDataset, datasetType, aggregateName, false, 0, 0, datasetList);
    }
    public void initCreateDatasetAggregate(Project project, Dataset parentDataset, Aggregation.ChildNature datasetType, String aggregateName, boolean hasSuffix, int suffixStart, int suffixStop, ArrayList<Dataset> datasetList) {
        setTaskInfo(new TaskInfo("Create Aggregate "+aggregateName, TASK_LIST_INFO));
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
    
    public void initCreateDatasetForIdentification(Project project, Dataset parentDataset, Aggregation.ChildNature datasetType, String aggregateName, Long resultSetId, Long resultSummaryId, ArrayList<Dataset> datasetList, TaskInfo taskInfo) {
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
    
    public void initModifyDatasetForValidation(Dataset dataset, Long resultSummaryId, TaskInfo taskInfo) {
        setTaskInfo(taskInfo);
        setPriority(Priority.HIGH_2);
        m_dataset = dataset;
        m_resultSummaryId = resultSummaryId;
        m_action = MODIFY_VALIDATED_DATASET;
    }
    
    public void initModifyDatasetToRemoveValidation(Dataset dataset) {
        setTaskInfo(new TaskInfo("Remove Identification Summary from Dataset "+dataset.getName(), TASK_LIST_INFO));
        setPriority(Priority.HIGH_2);
        m_dataset = dataset;
        m_action = REMOVE_VALIDATION_OF_DATASET;
    }
    
    public void initModifyDatasetForMerge(Dataset dataset, Long resultSetId, Long resultSummaryId, TaskInfo taskInfo) {
        setTaskInfo(taskInfo);
        setPriority(Priority.HIGH_2);
        m_dataset = dataset;
        m_resultSetId = resultSetId;
        m_resultSummaryId = resultSummaryId;
        m_action = MODIFY_MERGED_DATASET;
    }
    
    public void initEmptyTrash(Dataset trashDataset) {
        setTaskInfo(new TaskInfo("Empty Trash", TASK_LIST_INFO));
        setPriority(Priority.HIGH_2);
        m_dataset = trashDataset;
        m_action = EMPTY_TRASH;
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
                        Dataset d = m_datasetList.get(i);
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
                return true; // done one time
         
        }

        return true; // should never be called
    }
    private boolean needToFetchRsetAndRsm(Dataset dataset) {
       if ((dataset.getResultSetId() != null) && (dataset.getTransientData().getResultSet() == null)) {
           // need to fetch a result set
           return true;
       }
       if ((dataset.getResultSummaryId() != null) && (dataset.getTransientData().getResultSummary() == null)) {
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
        }
        
        return false; // should never happen
    }

    public boolean fetchParentsDataSet() {

        long projectId = m_project.getId();

        
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        try {
            entityManagerUDS.getTransaction().begin();


            
            // load parent DataSet
            TypedQuery<Dataset> dataSetQuery = entityManagerUDS.createQuery("SELECT d FROM Dataset d WHERE (d.parentDataset IS null) AND d.project.id=:projectId  ORDER BY d.number ASC", Dataset.class);
            dataSetQuery.setParameter("projectId", projectId);
            List<Dataset> datasetListSelected = dataSetQuery.getResultList();

            Dataset trash = null;
            Iterator<Dataset> it = datasetListSelected.iterator();
            while (it.hasNext()) {
                Dataset datasetCur = it.next();
               
                
                if (datasetCur.getType() == Dataset.DatasetType.TRASH) {
                    trash = datasetCur;
                } else {
                    m_list.add(new DataSetData(datasetCur));
                }
            }
            
            boolean hasTrash = false;
            if (trash != null) {
                m_list.add(new DataSetData(trash));
                hasTrash = true;
            }
            
            // add Trash if it not exists

            if (!hasTrash) {

                Project mergedProject = entityManagerUDS.merge(m_project);
                Dataset trashDataset = new Dataset(mergedProject);
                trashDataset.setType(Dataset.DatasetType.TRASH);

                Aggregation aggregation = UDSDataManager.getUDSDataManager().getAggregation(Aggregation.ChildNature.OTHER);
                Aggregation mergedAggregation = entityManagerUDS.merge(aggregation);
                trashDataset.setAggregation(mergedAggregation);

                trashDataset.setName("Trash");
                trashDataset.setChildrenCount(0); // trash is empty

                trashDataset.setNumber(m_list.size());

                entityManagerUDS.persist(trashDataset);

            }
    
            
            m_project.getTransientData().setChildrenNumber(m_list.size());
            
            // add the UDS connection to the Netbeans Service
            ExternalDb msiDb = ExternalDbRepository.findExternalByTypeAndProject(entityManagerUDS, ProlineDatabaseType.MSI, entityManagerUDS.merge(m_project));
            
            
            
            try {
                ConnectionManager cm = ConnectionManager.getDefault();

                JDBCDriver driver = JDBCDriverManager.getDefault().getDrivers( UDSDataManager.getUDSDataManager().getUdsJdbcDriver() )[0]; //JPM.WART : same driver for uds and msi

                String udsJdbcUrl = UDSDataManager.getUDSDataManager().getUdsJdbcURL();
                
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
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }
    

    public boolean fetchChildrenDataSet() {

        long parentDatasetId = m_parentDataset.getId();

        
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        try {
            entityManagerUDS.getTransaction().begin();


            TypedQuery<Dataset> datasetQuery = entityManagerUDS.createQuery("SELECT d FROM Dataset d WHERE d.parentDataset.id=:parentDatasetId ORDER BY d.number ASC", Dataset.class);
            datasetQuery.setParameter("parentDatasetId", parentDatasetId);
            List<Dataset> dataSetResultList = datasetQuery.getResultList();
            Iterator<Dataset> itDataset = dataSetResultList.iterator();
            while (itDataset.hasNext()) {
                Dataset datasetCur = itDataset.next();
                m_list.add(new DataSetData(datasetCur));
            }
            
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
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
            return false;
        } finally {
            entityManagerMSI.close();
        }

        return true;
    }
    
    private void fetchRsetAndRsmForOneDataset(EntityManager entityManagerMSI, Dataset d) {

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
            
            
            d.getTransientData().setResultSet(rsetFound);
        }

        Long rsmId = d.getResultSummaryId();
        if (rsmId != null) {
            ResultSummary rsmFound = entityManagerMSI.find(ResultSummary.class, rsmId);

            rsmFound.getTransientData().setDataSet(d);
            d.getTransientData().setResultSummary(rsmFound);
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
            entityManagerUDS.getTransaction().rollback();
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
            entityManagerUDS.getTransaction().rollback();
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
                entityManagerUDS.getTransaction().rollback();
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
        Dataset mergedParentDataset = (m_parentDataset == null) ? null : entityManagerUDS.merge(m_parentDataset);
         
        Dataset d;
        if (identificationDataset) {
            d = new IdentificationDataset();
            d.setProject(mergedProject);
            d.setType(Dataset.DatasetType.IDENTIFICATION);
        } else {
            d = new Dataset(mergedProject);
            d.setType(Dataset.DatasetType.AGGREGATE);
            
            Aggregation aggregation = UDSDataManager.getUDSDataManager().getAggregation(m_datasetType);
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
            try {
                m_parentDataset.addChild(d);
            } catch (org.hibernate.LazyInitializationException e) {
                // if this exception happens, there is in fact nothing to do
                // (children have never been loaded)
            }
        } else {
            int childrenCount = m_project.getTransientData().getChildrenNumber();
            d.setNumber(childrenCount);
            m_project.getTransientData().setChildrenNumber(childrenCount+1);
        }

        entityManagerUDS.persist(d);
        if (mergedParentDataset != null) {
            entityManagerUDS.persist(mergedParentDataset);
        }

        m_datasetList.add(d);
    }
    
    
    private boolean modifyDatasetRSM() {
        
        synchronized(WRITE_DATASET_LOCK) {
        
            EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
            try {
                entityManagerUDS.getTransaction().begin();



                Dataset mergedDataset = entityManagerUDS.merge(m_dataset);

                m_dataset.setResultSummaryId(m_resultSummaryId);
                mergedDataset.setResultSummaryId(m_resultSummaryId);
                                       
                entityManagerUDS.persist(mergedDataset);

                entityManagerUDS.getTransaction().commit();

            } catch (Exception e) {
                m_logger.error(getClass().getSimpleName() + " failed", e);
                entityManagerUDS.getTransaction().rollback();
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



                Dataset mergedDataset = entityManagerUDS.merge(m_dataset);

                m_dataset.setResultSummaryId(null);
                mergedDataset.setResultSummaryId(null);
                
                m_dataset.getTransientData().setResultSummary(null);                          
                mergedDataset.getTransientData().setResultSummary(null);

                entityManagerUDS.persist(mergedDataset);

                entityManagerUDS.getTransaction().commit();

            } catch (Exception e) {
                m_logger.error(getClass().getSimpleName() + " failed", e);
                entityManagerUDS.getTransaction().rollback();
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

                Dataset mergedDataset = entityManagerUDS.merge(m_dataset);

                m_dataset.setResultSetId(m_resultSetId);
                mergedDataset.setResultSetId(m_resultSetId);
                
                m_dataset.setResultSummaryId(m_resultSummaryId);
                mergedDataset.setResultSummaryId(m_resultSummaryId);

                entityManagerUDS.persist(mergedDataset);

                entityManagerUDS.getTransaction().commit();

            } catch (Exception e) {
                m_logger.error(getClass().getSimpleName() + " failed", e);
                entityManagerUDS.getTransaction().rollback();
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

                Dataset mergedTrash = entityManagerUDS.merge(m_dataset); // dataset is the Trash
                removeChildren(entityManagerUDS, mergedTrash);
                mergedTrash.setChildren(new ArrayList());
                mergedTrash.setChildrenCount(0);
                entityManagerUDS.persist(mergedTrash);

                entityManagerUDS.getTransaction().commit();

            } catch (Exception e) {
                m_logger.error("updateDatasetAndProjectsTree failed", e);
                entityManagerUDS.getTransaction().rollback();
                return false;
            } finally {
                entityManagerUDS.close();
            }

        }

        return true;
    }

    private void removeChildren(EntityManager entityManagerUDS, Dataset d) {
        List<Dataset> children = d.getChildren();
        if (children != null) {
            Iterator<Dataset> it = children.iterator();
            while (it.hasNext()) {
                Dataset child = it.next();
                removeChildren(entityManagerUDS, child);
            }
            children.clear();
        }
        entityManagerUDS.remove(d);
    }

    /**
     * Called when the tree Project/Dataset has been modified by the user by
     * Drag & Drop
     *
     * @param databaseObjectsToModify HashMap whose keys can be Project or
     * Parent Dataset
     */
    public static boolean updateDatasetAndProjectsTree(HashMap<Object, ArrayList<Dataset>> databaseObjectsToModify) {
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            Iterator it = databaseObjectsToModify.keySet().iterator();
            while (it.hasNext()) {
                Object parentObject = it.next();
                ArrayList<Dataset> datasetList = databaseObjectsToModify.get(parentObject);

                int nbDataset = datasetList.size();
                ArrayList<Dataset> mergedDatasetList = new ArrayList<Dataset>(nbDataset);

                for (int i = 0; i < nbDataset; i++) {
                    Dataset dataset = datasetList.get(i);
                    Dataset mergedDataset = entityManagerUDS.merge(dataset);
                    mergedDatasetList.add(mergedDataset);
                }


                Dataset parentDataset;
                Dataset mergedParentDataset = null;
                Project parentProject;
                //Project mergedParentProject = null;
                if (parentObject instanceof Dataset) {
                    parentDataset = (Dataset) parentObject;
                    mergedParentDataset = entityManagerUDS.merge(parentDataset);


                    mergedParentDataset.replaceAllChildren(mergedDatasetList);
                    try {
                        parentDataset.replaceAllChildren(datasetList);
                    } catch (org.hibernate.LazyInitializationException e) {
                        // if this exception happens, there is in fact nothing to do
                        // (children have never been loaded)
                    }

                } else if (parentObject instanceof Project) {
                    parentProject = (Project) parentObject;
                    parentProject.getTransientData().setChildrenNumber(nbDataset);
                }

                for (int i = 0; i < nbDataset; i++) {
                    Dataset mergedDataset = mergedDatasetList.get(i);
                    entityManagerUDS.persist(mergedDataset);
                }
                if (mergedParentDataset != null) {
                    entityManagerUDS.persist(mergedParentDataset);
                }


            }

            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_logger.error("updateDatasetAndProjectsTree failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }
}
