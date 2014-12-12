package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 *
 * Used to Search a ProteinMatch of a Rset
 * 
 * @author JM235353
 */
public class DatabaseSearchPeptideMatchTask extends AbstractDatabaseTask {
  
    private long m_projectId = -1;
    private long m_rsetId = -1;
    private String        m_searchString = null;
    private ArrayList<Long>     m_searchResult = null;
    
    public DatabaseSearchPeptideMatchTask(AbstractDatabaseCallback callback, long projectId, long rsetId, String searchString, ArrayList<Long> searchResult) {
        super(callback, Priority.HIGH_1, new TaskInfo("Search Peptide Match "+searchString, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_rsetId = rsetId;       
        m_searchString = searchString;
        m_searchResult = searchResult;
    }
    
    @Override
    public boolean fetchData() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            
            // Search peptideMatches with the searched name
            TypedQuery<Long> searchQuery = entityManagerMSI.createQuery("SELECT pm.id FROM fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p WHERE pm.resultSet.id=:rsetId AND pm.peptideId=p.id AND p.sequence LIKE :search ORDER BY pm.msQuery.initialId ASC, p.sequence ASC", Long.class);
            
            String searchStringSql = m_searchString.replaceAll("\\*", "%").replaceAll("\\?","_");
            searchQuery.setParameter("search", searchStringSql);
            searchQuery.setParameter("rsetId", m_rsetId);
            List<Long> peptideMatchIdList = searchQuery.getResultList();

            m_searchResult.clear();
            m_searchResult.addAll(peptideMatchIdList);

            entityManagerMSI.getTransaction().commit();
        } catch  (RuntimeException e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
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

    
    @Override
    public boolean needToFetch() {
        return true;
    }
    
}
