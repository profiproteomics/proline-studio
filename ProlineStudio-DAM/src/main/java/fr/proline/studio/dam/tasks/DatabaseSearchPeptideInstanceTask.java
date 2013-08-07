package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask.Priority;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Task to search a Peptide Instance from its sequence
 * @author JM235353
 */
public class DatabaseSearchPeptideInstanceTask extends AbstractDatabaseTask {
   
    private long m_projectId = -1;
    private ResultSummary m_rsm = null;
    private String        m_searchString = null;
    private ArrayList<Long>     m_searchResult = null;
    
    public DatabaseSearchPeptideInstanceTask(AbstractDatabaseCallback callback, long projectId, ResultSummary rsm, String searchString, ArrayList<Long> searchResult) {
        super(callback, Priority.HIGH_1, new TaskInfo("Search Peptide Instance "+searchString, TASK_LIST_INFO));
        m_projectId = projectId;
        m_rsm = rsm;       
        m_searchString = searchString;
        m_searchResult = searchResult;
    }
    
    @Override
    public boolean fetchData() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            
            // Search peptideMatches with the searched name
            TypedQuery<Long> searchQuery = entityManagerMSI.createQuery("SELECT pi.id FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p WHERE pi.resultSummary.id=:rsmId AND pi.bestPeptideMatchId=pm.id AND pm.peptideId=p.id AND p.sequence LIKE :search ORDER BY pm.score DESC", Long.class);
            searchQuery.setParameter("search", "%"+m_searchString+"%");
            searchQuery.setParameter("rsmId", m_rsm.getId());
            List<Long> peptideInstanceIdList = searchQuery.getResultList();

            m_searchResult.clear();
            m_searchResult.addAll(peptideInstanceIdList);

            entityManagerMSI.getTransaction().commit();
        } catch  (RuntimeException e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
            m_taskError = new TaskError(e);
            return false;
        } finally {
            entityManagerMSI.close();
        }
        
        return true;
    }

    @Override
    public void abortTask() {
        // nothing to do for task which are not inherited from AbstractDatabaseSlicerTask 
    }
    
    @Override
    public boolean needToFetch() {
        return true;
    }
    
}
