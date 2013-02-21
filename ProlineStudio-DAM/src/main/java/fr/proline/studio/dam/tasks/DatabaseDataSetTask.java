package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.IdentificationDataset;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.UDSDataManager;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.Query;

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
        }
        
        return false; // should never happen
    }

    public boolean fetchParentsDataSet() {

        Integer projectId = project.getId();

        
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        try {
            entityManagerUDS.getTransaction().begin();


            
            // load parent DataSet
            TypedQuery<Dataset> dataSetQuery = entityManagerUDS.createQuery("SELECT d FROM Dataset d WHERE (d.parentDataset IS null) AND d.project.id=:projectId  ORDER BY d.fractionCount ASC", Dataset.class);
            dataSetQuery.setParameter("projectId", projectId);
            List<Dataset> datasetList = dataSetQuery.getResultList();

            Iterator<Dataset> it = datasetList.iterator();
            while (it.hasNext()) {
                Dataset datasetCur = it.next();
                list.add(new DataSetData(datasetCur));
            }
            
            project.getTransientData().setChildrenNumber(datasetList.size());
            
            //JPM.CLEAN
            /*if (project.getId() == 1) {
                DataSetTMP dataSetCur = new DataSetTMP();
                dataSetCur.description = "";
                dataSetCur.id = 1;
                dataSetCur.name = "CB_342";
                dataSetCur.parentDataSetId = null;
                dataSetCur.project = project;
                dataSetCur.resultSetId = null;
                dataSetCur.resultSummaryId = null;
                dataSetCur.aggregateType = DataSetTMP.BIOLOGICAL_SAMPLE;

                list.add(new DataSetData(dataSetCur));
            }*/
            
            
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
            projectId = datasetList.get(0).getId();
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
            //JPM.TODO : uncomment this code when the database is ready
            /*TypedQuery<DataSetTMP> dataSetQuery = entityManagerUDS.createQuery("SELECT d FROM Dataset d WHERE d.resultSummaryId=:rsmId", DataSetTMP.class);
            dataSetQuery.setParameter("rsmId", rsmId);
            DataSetTMP dataSet = dataSetQuery.getSingleResult();
            rsm.getTransientData().setDataSet(dataSet);*/
            
            /*JPM.CLEAN if (rsmId.intValue() == 2) {
                DataSetTMP dataSetCur = new DataSetTMP();
                dataSetCur.description = "";
                dataSetCur.id = 4;
                dataSetCur.name = "RSM 1";
                dataSetCur.parentDataSetId = 2;
                dataSetCur.project = project;
                dataSetCur.resultSetId = new Integer(13);
                dataSetCur.resultSummaryId = new Integer(2);
                
                rsm.getTransientData().setDataSet(dataSetCur);
            } else if (rsmId.intValue() == 4) {
                
                DataSetTMP dataSetCur = new DataSetTMP();
                dataSetCur.description = "";
                dataSetCur.id = 5;
                dataSetCur.name = "RSM 2";
                dataSetCur.parentDataSetId = 2;
                dataSetCur.project = project;
                dataSetCur.resultSetId = new Integer(13);
                dataSetCur.resultSummaryId = new Integer(4);
                
                rsm.getTransientData().setDataSet(dataSetCur);
            }*/

     
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

            //JPM.TODO
            /*
            String renameSQL = "UPDATE Dataset d set d.name= :name where d.id = :datasetId";
            Query renameQuery = entityManagerUDS.createQuery(renameSQL);
            renameQuery.setParameter("datasetId", dataSet.getId());
            renameQuery.setParameter("name", name);
            renameQuery.executeUpdate();*/
     
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
        Aggregation aggregation = UDSDataManager.getUDSDataManager().getAggregation(datasetType);
        Aggregation mergedAggregation = entityManagerUDS.merge(aggregation);
        
        Dataset d = null;
        if (identificationDataset) {
            d = new IdentificationDataset();
            d.setProject(mergedProject);
            d.setType(Dataset.DatasetType.IDENTIFICATION);
        } else {
            d = new Dataset(mergedProject);
            d.setType(Dataset.DatasetType.AGGREGATE);
        }

        d.setName(datasetName);
        d.setParentDataset(mergedParentDataset);
        d.setResultSetId(resultSetId);
        d.setResultSummaryId(resultSummaryId);
        d.setAggregation(mergedAggregation);
        d.setFractionCount(0); // this aggregate has no child for the moment
        
        // number of children of the parent
        if (mergedParentDataset != null) {
            Integer fractionCount = mergedParentDataset.getFractionCount();
            d.setNumber(fractionCount);
            mergedParentDataset.setFractionCount(fractionCount+1);
            parentDataset.setFractionCount(fractionCount+1);
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
}
