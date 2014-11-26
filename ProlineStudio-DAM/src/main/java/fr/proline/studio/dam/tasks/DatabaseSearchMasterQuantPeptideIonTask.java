package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask.Priority;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Task to search a MasterQuantPeptideIon from its sequence
 * @author JM235353
 */
public class DatabaseSearchMasterQuantPeptideIonTask extends AbstractDatabaseTask {
   
    private long m_projectId = -1;
    private Long m_rsmId = null;
    private String        m_searchString = null;
    private ArrayList<Long>     m_searchResult = null;
    
    public DatabaseSearchMasterQuantPeptideIonTask(AbstractDatabaseCallback callback, long projectId, Long rsmId, String searchString, ArrayList<Long> searchResult) {
        super(callback, Priority.HIGH_1, new TaskInfo("Search MasterQuantPeptideIon "+searchString, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_rsmId = rsmId;       
        m_searchString = searchString;
        m_searchResult = searchResult;
    }
    
    @Override
    public boolean fetchData() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            
            // Search masterQuantPeptideIon with the searched name
            TypedQuery<Long> searchQuery = entityManagerMSI.createQuery("SELECT mqpi.id FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.MasterQuantPeptideIon mqpi, fr.proline.core.orm.msi.Peptide p WHERE pi.resultSummary.id=:rsmId AND pi.id=mqpi.peptideInstance.id AND pi.peptide.id=p.id AND p.sequence LIKE :search ORDER BY mqpi.charge ASC", Long.class);
            String searchStringSql = m_searchString.replaceAll("\\*", "%").replaceAll("\\?","_");
            searchQuery.setParameter("search", searchStringSql);
            searchQuery.setParameter("rsmId", m_rsmId);
            List<Long> peptideInstanceIdList = searchQuery.getResultList();

            m_searchResult.clear();
            m_searchResult.addAll(peptideInstanceIdList);

            entityManagerMSI.getTransaction().commit();
        } catch  (RuntimeException e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
            m_taskError = new TaskError(e);
            entityManagerMSI.getTransaction().rollback();
            return false;
        } finally {
            entityManagerMSI.close();
        }
        
        return true;
    }

    
    @Override
    public boolean needToFetch() {
        return true;
    }
    
}
