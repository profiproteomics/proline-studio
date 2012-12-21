package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.util.DatabaseManager;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DataSetTMP;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.dam.data.ResultSummaryData;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Used to load dataset in two cases :
 * - parent dataset of a project
 * - children of a dataset
 * @author JM235353
 */
public class DatabaseLoadDataSetTask extends AbstractDatabaseTask {
    
    private Project project = null;
    private DataSetTMP dataSet = null;
    private List<AbstractData> list = null;

    private int action;
    
    private final static int LOAD_PARENT_DATA_SET   = 0;
    private final static int LOAD_CHILDREN_DATA_SET = 1;
    private final static int LOAD_RSET_AND_RSM_OF_DATA_SET    = 2;
    
    
    /**
     * Constructor used to load all Parent DataSet of a project
     */
    public DatabaseLoadDataSetTask(AbstractDatabaseCallback callback, Project project, List<AbstractData> list) {
        super(callback);
        this.project = project;
        this.list = list;
        action = LOAD_PARENT_DATA_SET;
    }
    
    public DatabaseLoadDataSetTask(AbstractDatabaseCallback callback, DataSetTMP parentDataSet, List<AbstractData> list) {
        super(callback);
        this.dataSet = parentDataSet;
        this.list = list; 
        action = LOAD_CHILDREN_DATA_SET; 
    }
    
    public DatabaseLoadDataSetTask(AbstractDatabaseCallback callback, DataSetTMP dataSet) {
        super(callback);
        this.dataSet = dataSet;

        action = LOAD_RSET_AND_RSM_OF_DATA_SET; 
    }
    
    
    @Override
    public boolean needToFetch() {
        return true; // this task is used only one time for each node

    }
    
    @Override
    public boolean fetchData() {
        
        switch (action) {
            case LOAD_PARENT_DATA_SET:
                return fetchParentsDataSet();
            case LOAD_CHILDREN_DATA_SET:
                return fetchChildrenDataSet();
            case LOAD_RSET_AND_RSM_OF_DATA_SET:
                return fetchRsetAndRsm();
        }
        
        return false; // should never happen
    }

    public boolean fetchParentsDataSet() {

        Integer projectId = project.getId();

        
        EntityManager entityManagerUDS = DatabaseManager.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();  
        try {
            entityManagerUDS.getTransaction().begin();

            // load parent DataSet
            /*IdentificationRepository identificationRepository = new IdentificationRepository(entityManagerUDS);
            List<Identification> identifications = identificationRepository.findIdentificationsByProject(projectId);

            Iterator<Identification> it = identifications.iterator();
            while (it.hasNext()) {
                Identification identification = it.next();
                list.add(new IdentificationData(identification));
            }*/

            //JPM.TODO
            DataSetTMP dataSet = new DataSetTMP();
            dataSet.description = "";
            dataSet.id = 1;
            dataSet.name = "CB_342";
            dataSet.parentDataSetId = null;
            dataSet.project = project;
            dataSet.resultSetId = null;
            dataSet.resultSummaryId = null;
            
            list.add(new DataSetData(dataSet));
            
            
            
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

            if (dataSetId == 1) {

                //JPM.TODO
                DataSetTMP dataSet = new DataSetTMP();
                dataSet.description = "";
                dataSet.id = 2;
                dataSet.name = "CAVEN456";
                dataSet.parentDataSetId = 1;
                dataSet.project = project;
                dataSet.resultSetId = new Integer(13);
                dataSet.resultSummaryId = null;

                list.add(new DataSetData(dataSet));


                dataSet = new DataSetTMP();
                dataSet.description = "";
                dataSet.id = 3;
                dataSet.name = "CAVEN457";
                dataSet.parentDataSetId = 1;
                dataSet.project = project;
                dataSet.resultSetId = new Integer(13);
                dataSet.resultSummaryId = null;

                list.add(new DataSetData(dataSet));

            } else {
                //JPM.TODO
                DataSetTMP dataSet = new DataSetTMP();
                dataSet.description = "";
                dataSet.id = 4;
                dataSet.name = "RSM 1";
                dataSet.parentDataSetId = 2;
                dataSet.project = project;
                dataSet.resultSetId = new Integer(13);
                dataSet.resultSummaryId = new Integer(2);

                list.add(new DataSetData(dataSet));
                
                dataSet = new DataSetTMP();
                dataSet.description = "";
                dataSet.id = 5;
                dataSet.name = "RSM 2";
                dataSet.parentDataSetId = 2;
                dataSet.project = project;
                dataSet.resultSetId = new Integer(13);
                dataSet.resultSummaryId = new Integer(4);

                list.add(new DataSetData(dataSet));

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

        EntityManager entityManagerMSI = DatabaseManager.getInstance().getMsiDbConnector(AccessDatabaseThread.getProjectIdTMP()).getEntityManagerFactory().createEntityManager();  //JPM.TODO : project id        
        try {

            entityManagerMSI.getTransaction().begin();

            Integer resultSetId = dataSet.getResultSetId();
            if (resultSetId != null) {
                ResultSet rset = entityManagerMSI.find(ResultSet.class, resultSetId);
            
                dataSet.getTransientData().setResultSet(rset);
            }
            
            Integer resultSummaryId = dataSet.getResultSummaryId();
            if (resultSummaryId != null) {
                ResultSummary rsm = entityManagerMSI.find(ResultSummary.class, resultSummaryId);
            
                dataSet.getTransientData().setResultSummary(rsm);
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
    
}
