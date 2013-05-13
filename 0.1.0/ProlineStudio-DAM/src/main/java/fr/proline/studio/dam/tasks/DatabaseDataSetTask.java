package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.*;
import fr.proline.core.orm.uds.repository.ExternalDbRepository;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.repository.ProlineDatabaseType;
import fr.proline.studio.dam.UDSDataManager;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    
    private Project project = null;
    private Dataset dataset = null;
    private ArrayList<Dataset> datasetList = null;
    private List<AbstractData> list = null;
    private ResultSummary rsm = null;
    private String name = null;
    private Dataset parentDataset = null;
    private Integer resultSetId = null;
    private Integer resultSummaryId = null;
    private Aggregation.ChildNature datasetType;
    private String aggregateName;
    private boolean hasSuffix = false;
    private int suffixStart = 0;
    private int suffixStop = 0;
    
    
    
    private int action;
    
    private final static int LOAD_PARENT_DATASET   = 0;
    private final static int LOAD_CHILDREN_DATASET = 1;
    private final static int LOAD_RSET_AND_RSM_OF_DATASET = 2;
    private final static int LOAD_DATASET_FOR_RSM = 3;
    private final static int RENAME_DATASET = 4;
    private final static int CREATE_AGGREGATE_DATASET = 5;
    private final static int CREATE_IDENTIFICATION_DATASET = 6;
    private final static int MODIFY_VALIDATED_DATASET = 7;
    private final static int MODIFY_MERGED_DATASET = 8;
    
    public DatabaseDataSetTask(AbstractDatabaseCallback callback) {
        super(callback, null);
    }
    
    /**
     * Load all Parent Dataset of a project
     * @param project
     * @param list 
     */
    public void initLoadParentDataset(Project project, List<AbstractData> list) {
        setTaskInfo(new TaskInfo("Load Data", "Load Data for Project "+project.getName(), TASK_LIST_INFO));
        this.project = project;
        this.list = list;
        action = LOAD_PARENT_DATASET;
        setPriority(Priority.HIGH_1);
    }
    
    /**
     * Load all children dataset of a dataset
     * @param parentDataSet
     * @param list 
     */
    public void initLoadChildrenDataset(Dataset parentDataset, List<AbstractData> list) {
        setTaskInfo(new TaskInfo("Load Data", "Load Data for Aggregate "+parentDataset.getName(), TASK_LIST_INFO));
        this.project = parentDataset.getProject();
        this.parentDataset = parentDataset;
        this.list = list;
        action = LOAD_CHILDREN_DATASET;
        setPriority(Priority.HIGH_1);
    }
    
    /**
     * Load Rset and Rsm of a dataset
     * @param dataSet 
     */
    public void initLoadRsetAndRsm(Dataset dataset) {
        setTaskInfo(new TaskInfo("Load Data", "Load Rset and Rsm for Aggregate "+dataset.getName(), TASK_LIST_INFO));
        this.dataset = dataset;

        action = LOAD_RSET_AND_RSM_OF_DATASET;
    }

    /**
     * Load Rset and Rsm of a dataset list
     * @param dataSetList 
     */
    public void initLoadRsetAndRsm(ArrayList<Dataset> datasetList) {
        setTaskInfo(new TaskInfo("Load Data", "Load Rset and Rsm for multiple Aggregates", TASK_LIST_INFO));
        
        this.datasetList = datasetList;

        action = LOAD_RSET_AND_RSM_OF_DATASET;
    }

    /**
     * Load a dataset from its rsm
     * @param callback
     * @param rsm 
     */
    public void initLoadDatasetForRsm(ResultSummary rsm) {
        setTaskInfo(new TaskInfo("Load Data", "Load data for rsm", TASK_LIST_INFO));
        this.rsm = rsm;
        action = LOAD_DATASET_FOR_RSM;
    }
    
    /**
     * rename a dataset
     * @return 
     */
    public void initRenameDataset(Dataset dataset, String name) {
        setTaskInfo(new TaskInfo("Rename", "Rename "+dataset.getName()+" to "+name, TASK_LIST_INFO));
        this.name = name;
        this.dataset = dataset;
        action = RENAME_DATASET;
    }

    
    public void initCreateDatasetAggregate(Project project, Dataset parentDataset, Aggregation.ChildNature datasetType, String aggregateName, ArrayList<Dataset> datasetList) {
        initCreateDatasetAggregate(project, parentDataset, datasetType, aggregateName, false, 0, 0, datasetList);
    }
    public void initCreateDatasetAggregate(Project project, Dataset parentDataset, Aggregation.ChildNature datasetType, String aggregateName, boolean hasSuffix, int suffixStart, int suffixStop, ArrayList<Dataset> datasetList) {
        setTaskInfo(new TaskInfo("Crete Aggregate", "Create Aggregate "+aggregateName, TASK_LIST_INFO));
        this.project = project;
        this.parentDataset = parentDataset;
        this.datasetType = datasetType;
        this.aggregateName = aggregateName;
        this.hasSuffix = hasSuffix;
        this.suffixStart = suffixStart;
        this.suffixStop = suffixStop;
        this.datasetList = datasetList;
        action = CREATE_AGGREGATE_DATASET;
    }
    
    public void initCreateDatasetForIdentification(Project project, Dataset parentDataset, Aggregation.ChildNature datasetType, String aggregateName, Integer resultSetId, Integer resultSummaryId, ArrayList<Dataset> datasetList, TaskInfo taskInfo) {
        setTaskInfo(taskInfo);
        setPriority(Priority.HIGH_2);
        this.project = project;
        this.parentDataset = parentDataset;
        this.datasetType = datasetType;
        this.aggregateName = aggregateName;
        this.resultSetId = resultSetId;
        this.resultSummaryId = resultSummaryId;
        this.datasetList = datasetList;
        action = CREATE_IDENTIFICATION_DATASET;
    }
    
    public void initModifyDatasetForValidation(Dataset dataset, Integer resultSummaryId, TaskInfo taskInfo) {
        setTaskInfo(taskInfo);
        setPriority(Priority.HIGH_2);
        this.dataset = dataset;
        this.resultSummaryId = resultSummaryId;
        action = MODIFY_VALIDATED_DATASET;
    }
    
    public void initModifyDatasetForMerge(Dataset dataset, Integer resultSetId, TaskInfo taskInfo) {
        setTaskInfo(taskInfo);
        setPriority(Priority.HIGH_2);
        this.dataset = dataset;
        this.resultSetId = resultSetId;
        action = MODIFY_MERGED_DATASET;
    }
    
  
    @Override
    public boolean needToFetch() {

        switch (action) {

            case LOAD_PARENT_DATASET:
            case LOAD_CHILDREN_DATASET:
                // this task is used only one time for each node
                return true;
            case LOAD_DATASET_FOR_RSM:
                return (rsm.getTransientData().getDataSet() == null);
            case LOAD_RSET_AND_RSM_OF_DATASET:
                if (datasetList != null) {
                    int nbDataSet = datasetList.size();
                    for (int i=0;i<nbDataSet;i++) {
                        Dataset d = datasetList.get(i);
                        if (needToFetchRsetAndRsm(d)) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    return needToFetchRsetAndRsm(dataset);
                }
            case RENAME_DATASET:
            case CREATE_AGGREGATE_DATASET:
            case CREATE_IDENTIFICATION_DATASET:
            case MODIFY_VALIDATED_DATASET:
            case MODIFY_MERGED_DATASET:
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
        
        switch (action) {
            case LOAD_PARENT_DATASET:
                return fetchParentsDataSet();
            case LOAD_CHILDREN_DATASET:
                return fetchChildrenDataSet();
            case LOAD_RSET_AND_RSM_OF_DATASET:
                return fetchRsetAndRsm();
            case LOAD_DATASET_FOR_RSM:
                return fetchDatasetForRsm(rsm);
            case RENAME_DATASET:
                return renameDataset();
            case CREATE_AGGREGATE_DATASET:
                return createDataset(false);
            case CREATE_IDENTIFICATION_DATASET:
                return createDataset(true);
            case MODIFY_VALIDATED_DATASET:
                return modifyDatasetRSM();
            case MODIFY_MERGED_DATASET:
                return  modifyDatasetRset();
        }
        
        return false; // should never happen
    }

    public boolean fetchParentsDataSet() {

        Integer projectId = project.getId();

        
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
                    list.add(new DataSetData(datasetCur));
                }
            }
            
            boolean hasTrash = false;
            if (trash != null) {
                list.add(new DataSetData(trash));
                hasTrash = true;
            }
            
            // add Trash if it not exists

            if (!hasTrash) {
                Project mergedProject = entityManagerUDS.merge(project);
                Dataset trashDataset = new Dataset(mergedProject);
                trashDataset.setType(Dataset.DatasetType.TRASH);
            
                Aggregation aggregation = UDSDataManager.getUDSDataManager().getAggregation(Aggregation.ChildNature.OTHER);
                Aggregation mergedAggregation = entityManagerUDS.merge(aggregation);
                trashDataset.setAggregation(mergedAggregation);
                
                trashDataset.setName("Trash");
                trashDataset.setChildrenCount(0); // trash is empty

                trashDataset.setNumber(list.size());

                entityManagerUDS.persist(trashDataset);
                
                list.add(new DataSetData(trashDataset));
            }
    
            
            project.getTransientData().setChildrenNumber(list.size());
            
            // add the UDS connection to the Netbeans Service
            ExternalDb msiDb = ExternalDbRepository.findExternalByTypeAndProject(entityManagerUDS, ProlineDatabaseType.MSI, entityManagerUDS.merge(project));
            
            
            
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
                    logger.error(getClass().getSimpleName() + " failed to add UDS connection to Services ", e);
                }
            }
            
            
            
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }
    

    public boolean fetchChildrenDataSet() {

        Integer parentDatasetId = parentDataset.getId();

        
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        try {
            entityManagerUDS.getTransaction().begin();


            TypedQuery<Dataset> datasetQuery = entityManagerUDS.createQuery("SELECT d FROM Dataset d WHERE d.parentDataset.id=:parentDatasetId ORDER BY d.number ASC", Dataset.class);
            datasetQuery.setParameter("parentDatasetId", parentDatasetId);
            List<Dataset> dataSetResultList = datasetQuery.getResultList();
            Iterator<Dataset> itDataset = dataSetResultList.iterator();
            while (itDataset.hasNext()) {
                Dataset datasetCur = itDataset.next();
                list.add(new DataSetData(datasetCur));
            }
            
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }
    
    public boolean fetchRsetAndRsm() {

        Integer projectId = null;
        if (dataset != null) {
            projectId = dataset.getProject().getId();
        } else if (datasetList != null) {
            projectId = datasetList.get(0).getProject().getId();
        }
        
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();       
        try {

            entityManagerMSI.getTransaction().begin();

            if (datasetList != null) {
                int nbDataset = datasetList.size();
                for (int i=0; i<nbDataset;i++) {
                    fetchRsetAndRsmForOneDataset(entityManagerMSI, datasetList.get(i));
                }
                
            } else if (dataset != null) {
                fetchRsetAndRsmForOneDataset(entityManagerMSI, dataset);
            }
            entityManagerMSI.getTransaction().commit();

        } catch (Exception e) {
            logger.error(getClass().getSimpleName() + " failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }

        return true;
    }
    
    private void fetchRsetAndRsmForOneDataset(EntityManager entityManagerMSI, Dataset d) {

        Integer rsetId = d.getResultSetId();
        if (rsetId != null) {
            ResultSet rsetFound = entityManagerMSI.find(ResultSet.class, rsetId);

            d.getTransientData().setResultSet(rsetFound);
        }

        Integer rsmId = d.getResultSummaryId();
        if (rsmId != null) {
            ResultSummary rsmFound = entityManagerMSI.find(ResultSummary.class, rsmId);

            rsmFound.getTransientData().setDataSet(d);
            d.getTransientData().setResultSummary(rsmFound);
        }
    }
    
    
    private boolean fetchDatasetForRsm(ResultSummary rsm) {
        
        Integer rsmId = rsm.getId();

        
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
            logger.error(getClass().getSimpleName()+" failed", e);
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
            renameQuery.setParameter("datasetId", dataset.getId());
            renameQuery.setParameter("name", name);
            renameQuery.executeUpdate();
     
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }
        
        return true;
    }
    
    private boolean createDataset(boolean identificationDataset) {
                    
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        try {
            entityManagerUDS.getTransaction().begin();

            if (hasSuffix) {
                for (int i=suffixStart;i<=suffixStop;i++) {
                    String datasetName = aggregateName+String.valueOf(i);
                    createDataSetImpl(entityManagerUDS, datasetName, identificationDataset);
                }

            } else {
                createDataSetImpl(entityManagerUDS, aggregateName, identificationDataset);
            }
            
            

     
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }
        
        return true;
    }
    
    private void createDataSetImpl(EntityManager entityManagerUDS, String datasetName, boolean identificationDataset) {

        //JPM.TODO : reuse objects for multiple queries
        
        Project mergedProject = entityManagerUDS.merge(project);
        Dataset mergedParentDataset = (parentDataset == null) ? null : entityManagerUDS.merge(parentDataset);
         
        Dataset d;
        if (identificationDataset) {
            d = new IdentificationDataset();
            d.setProject(mergedProject);
            d.setType(Dataset.DatasetType.IDENTIFICATION);
        } else {
            d = new Dataset(mergedProject);
            d.setType(Dataset.DatasetType.AGGREGATE);
            
            Aggregation aggregation = UDSDataManager.getUDSDataManager().getAggregation(datasetType);
            Aggregation mergedAggregation = entityManagerUDS.merge(aggregation);
            d.setAggregation(mergedAggregation);
 
        }

        d.setName(datasetName);
        d.setParentDataset(mergedParentDataset);
        d.setResultSetId(resultSetId);
        d.setResultSummaryId(resultSummaryId);
        d.setChildrenCount(0); // this aggregate has no child for the moment
        
        // number of children of the parent
        if (mergedParentDataset != null) {
            Integer fractionCount = mergedParentDataset.getChildrenCount();
            d.setNumber(fractionCount);
            mergedParentDataset.setChildrenCount(fractionCount+1);
            parentDataset.setChildrenCount(fractionCount+1);
        } else {
            int childrenCount = project.getTransientData().getChildrenNumber();
            d.setNumber(childrenCount);
            project.getTransientData().setChildrenNumber(childrenCount+1);
        }

        entityManagerUDS.persist(d);
        if (mergedParentDataset != null) {
            entityManagerUDS.persist(mergedParentDataset);
        }

        datasetList.add(d);
    }
    
    
    private boolean modifyDatasetRSM() {
         EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        try {
            entityManagerUDS.getTransaction().begin();
            
            Dataset mergedDataset = entityManagerUDS.merge(dataset);
        
            dataset.setResultSummaryId(resultSummaryId);
            mergedDataset.setResultSummaryId(resultSummaryId);
            
            entityManagerUDS.persist(mergedDataset);
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }
        
        return true;
    }
    
        private boolean modifyDatasetRset() {
         EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        try {
            entityManagerUDS.getTransaction().begin();
            
            Dataset mergedDataset = entityManagerUDS.merge(dataset);
        
            dataset.setResultSetId(resultSetId);
            mergedDataset.setResultSetId(resultSetId);
            
            entityManagerUDS.persist(mergedDataset);
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }
        
        return true;
    }
    
    /**
     * Called when the tree Project/Dataset has been modified by the user by Drag & Drop
     * @param databaseObjectsToModify  HashMap whose keys can be Project or Parent Dataset
     */
    public static boolean updateDatasetAndProjectsTree(HashMap<Object, ArrayList<Dataset>> databaseObjectsToModify) {
         EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        try {
            entityManagerUDS.getTransaction().begin();
            
            Iterator it = databaseObjectsToModify.keySet().iterator();
            while (it.hasNext()) {
                Object parentObject = it.next();
                ArrayList<Dataset> datasetList = databaseObjectsToModify.get(parentObject);
                
                Dataset parentDataset = null;
                Dataset mergedParentDataset = null;
                Project parentProject = null;
                //Project mergedParentProject = null;
                if (parentObject instanceof Dataset) {
                    parentDataset = (Dataset) parentObject;
                    mergedParentDataset = entityManagerUDS.merge(parentDataset);
                } else if (parentObject instanceof Project) {
                    parentProject = (Project) parentObject;
                }
                
                
                // update children dataset
                int decForTrash = 0;
                int nbDataset = datasetList.size();
                for (int i=0;i<nbDataset;i++) {
                    
                    // update dataset
                    Dataset dataset = datasetList.get(i);
                    Dataset mergedDataset = entityManagerUDS.merge(dataset);
                   

                    if (dataset.getType() == Dataset.DatasetType.TRASH) {
                        mergedDataset.setNumber(nbDataset-1);
                        dataset.setNumber(nbDataset-1);
                        decForTrash = 1;
                    } else {
                        mergedDataset.setNumber(i-decForTrash);
                        dataset.setNumber(i-decForTrash);
                    }
                    mergedDataset.setParentDataset(mergedParentDataset); // can be null when it is directly in a Project
                    dataset.setParentDataset(parentDataset);
                    entityManagerUDS.persist(mergedDataset);
   
                }
                
                // updata parent object
                if (parentDataset != null) {
                    mergedParentDataset.setChildrenCount(nbDataset);
                    parentDataset.setChildrenCount(nbDataset);
                    entityManagerUDS.persist(mergedParentDataset);
                } else if (parentProject != null) {
                    parentProject.getTransientData().setChildrenNumber(nbDataset);
                }
            }
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            logger.error("updateDatasetAndProjectsTree failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }
        
        return true;
    }
}
