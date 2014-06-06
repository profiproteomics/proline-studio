
package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.MsiSearch;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.uds.IdentificationDataset;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * This class provides methods to load and to create Runs and Raw file from the UDS db 
 *
 * @author vd225637
 */
public class DatabaseRunsTask extends AbstractDatabaseTask {

    private long m_projectId = -1;
    private DDataset m_dataset = null;
    private Long m_rsmId = null;
    private List<Long> m_runIds = null;
        
    private int m_action;
    
    private final static int LOAD_RUN_FOR_RSM = 0;
     
    public DatabaseRunsTask(AbstractDatabaseCallback callback, long projectId){
        super(callback, null);
        this.m_projectId = projectId;
    }
    
    /**
     * Load Run Id for specified RSMs
     * @return 
     */
    public void initLoadRunIdForRsm(Long rsmId, ArrayList<Long> runIds){
        setTaskInfo(new TaskInfo(" Load RunId for RSM with id "+rsmId,false, TASK_LIST_INFO));
        this.m_rsmId =rsmId;
        m_runIds = runIds;
        m_action = LOAD_RUN_FOR_RSM;
    }
    
    @Override
    public boolean fetchData() {
        switch (m_action) {
            case LOAD_RUN_FOR_RSM : 
                return fetchRunForRsm();
        }
        
        return false; 
    }
    
    public boolean fetchRunForRsm(){
         EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();
            //Get Run and Raw File
            TypedQuery<IdentificationDataset> runIdQuery = entityManagerUDS.createQuery("SELECT idfDS FROM IdentificationDataset idfDS WHERE idfDS.project.id = :pjId and idfDS.resultSummaryId =:rsmId  ", IdentificationDataset.class);
            runIdQuery.setParameter("pjId", m_projectId);            
            runIdQuery.setParameter("rsmId", m_rsmId);
            List<IdentificationDataset> idfDs = runIdQuery.getResultList();

            if(idfDs != null && idfDs.size()>0){
                m_runIds.add(idfDs.get(0).getRun().getId());
            }
            
            
            entityManagerUDS.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }
        
        return true;
    }

    @Override
    public boolean needToFetch() {
        switch (m_action) {
            case LOAD_RUN_FOR_RSM:
                return true;
        }
        
        return true; // should never be called
    }
    
    
    
}
