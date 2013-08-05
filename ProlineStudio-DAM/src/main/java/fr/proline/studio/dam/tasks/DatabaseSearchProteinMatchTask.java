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
 * Used to Search a ProteinMatch (accession) in a Search Result (rset)
 * @author JM235353
 */
public class DatabaseSearchProteinMatchTask extends AbstractDatabaseTask {
 
    private long m_projectId = -1;
    private ResultSet m_rset = null;
    private String        m_searchAccession = null;
    private ArrayList<Long>     m_searchResult = null;
    
    public DatabaseSearchProteinMatchTask(AbstractDatabaseCallback callback, long projectId, ResultSet rset, String searchAccession, ArrayList<Long> searchResult) {
        super(callback, Priority.HIGH_1, new TaskInfo("Search Protein Set "+searchAccession, TASK_LIST_INFO));
        m_projectId = projectId;
        m_rset = rset;       
        m_searchAccession = searchAccession;
        m_searchResult = searchResult;
    }
    
    @Override
    public boolean fetchData() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector( m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            
            // Search the first ProteinSet which has a Best Protein Match with the searched name
            TypedQuery<Long> searchQuery = entityManagerMSI.createQuery("SELECT pm.id FROM ProteinMatch pm WHERE pm.resultSet.id=:rsetId AND pm.accession LIKE :search ORDER BY pm.score DESC", Long.class);
            searchQuery.setParameter("search", "%"+m_searchAccession+"%");
            searchQuery.setParameter("rsetId", m_rset.getId());
            List<Long> proteinMatchIdList = searchQuery.getResultList();

            
            m_searchResult.clear();
            m_searchResult.addAll(proteinMatchIdList);

            
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
    public boolean needToFetch() {
        return true;
    }
    
}
