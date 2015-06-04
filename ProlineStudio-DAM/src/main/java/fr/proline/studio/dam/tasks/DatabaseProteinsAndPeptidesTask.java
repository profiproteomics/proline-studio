package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import static fr.proline.studio.dam.tasks.AbstractDatabaseTask.TASK_LIST_INFO;
import static fr.proline.studio.dam.tasks.AbstractDatabaseTask.m_logger;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 *
 * @author JM235353
 */
public class DatabaseProteinsAndPeptidesTask extends AbstractDatabaseTask {
    
    private long m_projectId = -1;
    private ResultSummary m_rsm = null;
    
    public DatabaseProteinsAndPeptidesTask(AbstractDatabaseCallback callback, long projectId, ResultSummary rsm) {
        super(callback, new TaskInfo("Load All Proteins and Peptides for "+rsm.getId(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
    
        m_projectId = projectId;
        m_rsm = rsm;
    }
    
    @Override
    public boolean needToFetch() {
        return true;
    }
    
    @Override
    public boolean fetchData() {
        if (needToFetch()) {
            // first data are fetched
            return fetchAllProteinsAndPeptides();
        }
        return true; // should not happen
    }
    
    private boolean fetchAllProteinsAndPeptides() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            

            //JPM.TODO : for adjacent matrix
            
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
    
}
