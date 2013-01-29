package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.util.DatabaseManager;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DataSetTMP;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import java.util.ArrayList;
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
    private DataSetTMP dataSet = null;
    private ArrayList<DataSetTMP> dataSetList = null;
    private List<AbstractData> list = null;
    private ResultSummary rsm = null;
    private String name = null;
    private Integer projectId = null;
    private Integer parentDatasetId = null;
    private Integer resultSetId = null;
    private Integer resultSummaryId = null;
    private int datasetType;
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
    private final static int CREATE_DATASET = 5;
    
    public DatabaseDataSetTask(AbstractDatabaseCallback callback) {
        super(callback);
    }
    
    /**
     * Load all Parent Dataset of a project
     * @param project
     * @param list 
     */
    public void initLoadParentDataset(Project project, List<AbstractData> list) {
        this.project = project;
        this.list = list;
        action = LOAD_PARENT_DATASET;
    }
    
    /**
     * Load all children dataset of a dataset
     * @param parentDataSet
     * @param list 
     */
    public void initLoadChildrenDataset(DataSetTMP parentDataSet, List<AbstractData> list) {
        this.project = parentDataSet.project;
        this.dataSet = parentDataSet;
        this.list = list;
        action = LOAD_CHILDREN_DATASET;
    }
    
    /**
     * Load Rset and Rsm of a dataset
     * @param dataSet 
     */
    public void initLoadRsetAndRsm(DataSetTMP dataSet) {

        this.dataSet = dataSet;

        action = LOAD_RSET_AND_RSM_OF_DATASET;
    }

    /**
     * Load Rset and Rsm of a dataset list
     * @param dataSetList 
     */
    public void initLoadRsetAndRsm(ArrayList<DataSetTMP> dataSetList) {

       this.dataSetList = dataSetList;

        action = LOAD_RSET_AND_RSM_OF_DATASET;
    }

    /**
     * Load a dataset from its rsm
     * @param callback
     * @param rsm 
     */
    public void initLoadDatasetForRsm(ResultSummary rsm) {
        this.rsm = rsm;
        action = LOAD_DATASET_FOR_RSM;
    }
    
    /**
     * rename a dataset
     * @return 
     */
    public void initRenameDataset(DataSetTMP dataSet, String name) {
        this.name = name;
        this.dataSet = dataSet;
        action = RENAME_DATASET;
    }

    
    public void initCreateDatasetAggregate(Integer projectId, Integer parentDatasetId, int datasetType, String aggregateName, ArrayList<DataSetTMP> dataSetList) {
        initCreateDatasetAggregate(projectId, parentDatasetId, datasetType, aggregateName, false, 0, 0, dataSetList);
    }
    public void initCreateDatasetAggregate(Integer projectId, Integer parentDatasetId, int datasetType, String aggregateName, boolean hasSuffix, int suffixStart, int suffixStop, ArrayList<DataSetTMP> dataSetList) {
        this.projectId = projectId;
        this.parentDatasetId = parentDatasetId;
        this.datasetType = datasetType;
        this.aggregateName = aggregateName;
        this.hasSuffix = hasSuffix;
        this.suffixStart = suffixStart;
        this.suffixStop = suffixStop;
        this.dataSetList = dataSetList;
        action = CREATE_DATASET;
    }
    
    public void initCreateDatasetForIdentification(Integer projectId, Integer parentDatasetId, int datasetType, String aggregateName, Integer resultSetId, Integer resultSummaryId, ArrayList<DataSetTMP> dataSetList) {
        this.projectId = projectId;
        this.parentDatasetId = parentDatasetId;
        this.datasetType = datasetType;
        this.aggregateName = aggregateName;
        this.resultSetId = resultSetId;
        this.resultSummaryId = resultSummaryId;
        this.dataSetList = dataSetList;
        action = CREATE_DATASET;
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
                if (dataSetList != null) {
                    int nbDataSet = dataSetList.size();
                    for (int i=0;i<nbDataSet;i++) {
                        DataSetTMP d = dataSetList.get(i);
                        if (needToFetchRsetAndRsm(d)) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    return needToFetchRsetAndRsm(dataSet);
                }
            case RENAME_DATASET:
            case CREATE_DATASET:
                return true; // done one time
         
        }

        return true; // should never be called
    }
    private boolean needToFetchRsetAndRsm(DataSetTMP dataSet) {
       if ((dataSet.getResultSetId() != null) && (dataSet.getTransientData().getResultSet() == null)) {
           // need to fetch a result set
           return true;
       }
       if ((dataSet.getResultSummaryId() != null) && (dataSet.getTransientData().getResultSummary() == null)) {
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
            case CREATE_DATASET:
                return createDataset();
        }
        
        return false; // should never happen
    }

    public boolean fetchParentsDataSet() {

        Integer projectId = project.getId();

        
        EntityManager entityManagerUDS = DatabaseManager.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        try {
            entityManagerUDS.getTransaction().begin();


            
            // load parent DataSet
            //JPM.TODO : uncomment this code when the database is ready
            /*TypedQuery<DataSetTMP> dataSetQuery = entityManagerUDS.createQuery("SELECT d FROM DataSet d WHERE d.projectId=:projectId", DataSetTMP.class);
            dataSetQuery.setParameter("projectId", projectId);
            DataSetTMP dataSet = dataSetQuery.getSingleResult();*/

            //JPM.TODO
            if (project.getId() == 1) {
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
            }
            
            
            entityManagerUDS.getTransaction().commit();

        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }
    

    public boolean fetchChildrenDataSet() {

        Integer dataSetId = dataSet.getId();

        
        EntityManager entityManagerUDS = DatabaseManager.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        try {
            entityManagerUDS.getTransaction().begin();

            //JPM.TODO : uncomment this code when the database is ready
            /*TypedQuery<DataSetTMP> dataSetQuery = entityManagerUDS.createQuery("SELECT d FROM DataSet d WHERE d.id=:dataSetId", DataSetTMP.class);
            dataSetQuery.setParameter("dataSetId", dataSetId);
            List<DataSetTMP> dataSetResultList = dataSetQuery.getResultList();
            Iterator<DataSetTMP> itDataSet = dataSetResultList.iterator();
            while (itDataSet.hasNext()) {
                DataSetTMP dataSetCur = itDataSet.next();
                list.add(new DataSetData(dataSetCur));
            }*/
            
            if (dataSetId == 1) {

                //JPM.TODO
                DataSetTMP dataSetCur = new DataSetTMP();
                dataSetCur.description = "";
                dataSetCur.id = 2;
                dataSetCur.name = "CAVEN456";
                dataSetCur.parentDataSetId = 1;
                dataSetCur.project = project;
                dataSetCur.resultSetId = new Integer(13);
                dataSetCur.resultSummaryId = null;
                dataSet.aggregateType = DataSetTMP.OTHER;

                list.add(new DataSetData(dataSetCur));


                dataSetCur = new DataSetTMP();
                dataSetCur.description = "";
                dataSetCur.id = 3;
                dataSetCur.name = "CAVEN457";
                dataSetCur.parentDataSetId = 1;
                dataSetCur.project = project;
                dataSetCur.resultSetId = new Integer(13);
                dataSetCur.resultSummaryId = null;
                dataSet.aggregateType = DataSetTMP.OTHER;

                list.add(new DataSetData(dataSetCur));

            } else {
                //JPM.TODO
                DataSetTMP dataSetCur = new DataSetTMP();
                dataSetCur.description = "";
                dataSetCur.id = 4;
                dataSetCur.name = "RSM 1";
                dataSetCur.parentDataSetId = 2;
                dataSetCur.project = project;
                dataSetCur.resultSetId = new Integer(13);
                dataSetCur.resultSummaryId = new Integer(2);
                dataSet.aggregateType = DataSetTMP.OTHER;

                list.add(new DataSetData(dataSetCur));
                
                dataSetCur = new DataSetTMP();
                dataSetCur.description = "";
                dataSetCur.id = 5;
                dataSetCur.name = "RSM 2";
                dataSetCur.parentDataSetId = 2;
                dataSetCur.project = project;
                dataSetCur.resultSetId = new Integer(13);
                dataSetCur.resultSummaryId = new Integer(4);
                dataSet.aggregateType = DataSetTMP.OTHER;

                list.add(new DataSetData(dataSetCur));

            }
            
            entityManagerUDS.getTransaction().commit();

        } catch (RuntimeException e) {
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
        if (dataSet != null) {
            projectId = dataSet.getProjectId();
        } else if (dataSetList != null) {
            projectId = dataSetList.get(0).getProjectId();
        }
        
        EntityManager entityManagerMSI = DatabaseManager.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();       
        try {

            entityManagerMSI.getTransaction().begin();

            if (dataSetList != null) {
                int nbDataSet = dataSetList.size();
                for (int i=0; i<nbDataSet;i++) {
                    fetchRsetAndRsmForOneDataSet(entityManagerMSI, dataSetList.get(i));
                }
                
            } else if (dataSet != null) {
                fetchRsetAndRsmForOneDataSet(entityManagerMSI, dataSet);
            }
            entityManagerMSI.getTransaction().commit();

        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName() + " failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }

        return true;
    }
    
    private void fetchRsetAndRsmForOneDataSet(EntityManager entityManagerMSI, DataSetTMP d) {

        Integer resultSetId = d.getResultSetId();
        if (resultSetId != null) {
            ResultSet rset = entityManagerMSI.find(ResultSet.class, resultSetId);

            d.getTransientData().setResultSet(rset);
        }

        Integer resultSummaryId = d.getResultSummaryId();
        if (resultSummaryId != null) {
            ResultSummary rsm = entityManagerMSI.find(ResultSummary.class, resultSummaryId);

            rsm.getTransientData().setDataSet(d);
            d.getTransientData().setResultSummary(rsm);
        }
    }
    
    
    private boolean fetchDatasetForRsm(ResultSummary rsm) {
        
        Integer rsmId = rsm.getId();

        
        EntityManager entityManagerUDS = DatabaseManager.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        try {
            entityManagerUDS.getTransaction().begin();

            // load dataset for rsm
            //JPM.TODO : uncomment this code when the database is ready
            /*TypedQuery<DataSetTMP> dataSetQuery = entityManagerUDS.createQuery("SELECT d FROM DataSet d WHERE d.resultSummaryId=:rsmId", DataSetTMP.class);
            dataSetQuery.setParameter("rsmId", rsmId);
            DataSetTMP dataSet = dataSetQuery.getSingleResult();
            rsm.getTransientData().setDataSet(dataSet);*/
            
            if (rsmId.intValue() == 2) {
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
            }

     
            entityManagerUDS.getTransaction().commit();

        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }

        return true;
    }
    
    private boolean renameDataset() {
        
            
        EntityManager entityManagerUDS = DatabaseManager.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
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

        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }
        
        return true;
    }
    
    private boolean createDataset() {
                    
        EntityManager entityManagerUDS = DatabaseManager.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        try {
            entityManagerUDS.getTransaction().begin();

            if (hasSuffix) {
                for (int i=suffixStart;i<=suffixStop;i++) {
                    String datasetName = aggregateName+String.valueOf(i);
                    createDataSetImpl(entityManagerUDS, datasetName);
                }

            } else {
                createDataSetImpl(entityManagerUDS, aggregateName);
            }
            
            

     
            entityManagerUDS.getTransaction().commit();

        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }
        
        return true;
    }
    
    private void createDataSetImpl(EntityManager entityManagerUDS, String datasetName) {
                    //JPM.TODO
            /*
            String insertSQL = "INSERT INTO Dataset "; TODO
            Query insertQuery = entityManagerUDS.createQuery(insertSQL);
            insertQuery.executeUpdate();*/
        
        //JPM.TODO
        DataSetTMP datasetCur = new DataSetTMP();
        datasetCur.name = datasetName;
        datasetCur.aggregateType = datasetType;
        datasetCur.parentDataSetId = parentDatasetId;
        datasetCur.resultSetId = resultSetId;
        datasetCur.resultSummaryId = resultSummaryId;
        
        dataSetList.add(datasetCur);
    }
}
