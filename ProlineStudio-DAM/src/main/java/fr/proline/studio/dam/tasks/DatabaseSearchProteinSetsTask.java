/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 *
 * Used to Search a ProteinName (accession) among the Typical Protein Match
 * or in the SameSet/SubSet of Protein Set.
 * 
 * @author JM235353
 */
public class DatabaseSearchProteinSetsTask extends AbstractDatabaseTask {

    private long m_projectId = -1;
    private ResultSummary m_rsm = null;
    private String        m_searchAccession = null;
    private ArrayList<Long>     m_searchResult = null;
    
    public DatabaseSearchProteinSetsTask(AbstractDatabaseCallback callback, long projectId, ResultSummary rsm, String searchAccession, ArrayList<Long> searchResult) {
        super(callback, Priority.HIGH_1, new TaskInfo("Search Protein Set "+searchAccession, TASK_LIST_INFO));
        m_projectId = projectId;
        m_rsm = rsm;       
        m_searchAccession = searchAccession;
        m_searchResult = searchResult;
    }
    
    @Override
    public boolean fetchData() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector( m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            
            // Search the first ProteinSet which has a Best Protein Match with the searched name
            TypedQuery<Long> searchQuery = entityManagerMSI.createQuery("SELECT ps.id FROM ProteinSet ps, ProteinMatch pm, PeptideSet pepset WHERE ps.resultSummary.id=:rsmId AND ps.typicalProteinMatchId=pm.id AND ps.isValidated=true AND pm.accession LIKE :search AND pepset.proteinSet=ps ORDER BY pepset.score DESC", Long.class);
            searchQuery.setParameter("search", "%"+m_searchAccession+"%");
            searchQuery.setParameter("rsmId", m_rsm.getId());
            List<Long> proteinSetIdList = searchQuery.getResultList();
            
            if (proteinSetIdList.isEmpty()) {
                // No ProteinSet found, we search for a Protein Match in the subset
                searchQuery = entityManagerMSI.createQuery("SELECT ps.id FROM ProteinSet ps, ProteinMatch pm, ProteinSetProteinMatchItem ps_to_pm, PeptideSet pepset WHERE ps.isValidated=true AND ps_to_pm.proteinSet.id=ps.id AND ps_to_pm.proteinMatch.id=pm.id AND ps_to_pm.resultSummary.id=:rsmId  AND pm.accession LIKE :search AND pepset.proteinSet=ps ORDER BY pepset.score DESC", Long.class);
                searchQuery.setParameter("search", "%"+m_searchAccession+"%");
                searchQuery.setParameter("rsmId", m_rsm.getId());
                
                proteinSetIdList = searchQuery.getResultList();  
            }
            
            m_searchResult.clear();
            m_searchResult.addAll(proteinSetIdList);
            
            
            
            
            entityManagerMSI.getTransaction().commit();
        } catch  (RuntimeException e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
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
