
package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.MsiSearch;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import javax.persistence.EntityManager;

/**
 * This class provides methods to load and to create Runs and Raw file from the UDS db 
 *
 * @author vd225637
 */
public class DatabaseRunsTask extends AbstractDatabaseTask {

    private long m_projectId = -1;
    private DDataset m_dataset = null;
     
    public DatabaseRunsTask(AbstractDatabaseCallback callback, long projectId, DDataset identDataset){
        super(callback, new TaskInfo("Create Run and Raw for "+identDataset.getName(),false,TASK_LIST_INFO ));
        this.m_projectId = projectId;
        m_dataset = identDataset;
        
    }
    
    @Override
    public boolean fetchData() {
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();
            //Get Run and Raw File
            
            //If not exist, create from Peaklist and SearchSettings properties
            MsiSearch currentMsi =  m_dataset.getResultSet().getMsiSearch();
            
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
        return true;
    }
    
    
    
}
