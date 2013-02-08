/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.AccessDatabaseThread;
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

    private Integer projectId = null;
    private ResultSummary rsm = null;
    private String        searchAccession = null;
    private ArrayList<Integer>     searchResult = null;
    
    public DatabaseSearchProteinSetsTask(AbstractDatabaseCallback callback, Integer projectId, ResultSummary rsm, String searchAccession, ArrayList<Integer> searchResult) {
        super(callback, Priority.HIGH_1);
        this.projectId = projectId;
        this.rsm = rsm;       
        this.searchAccession = searchAccession;
        this.searchResult = searchResult;
    }
    
    @Override
    public boolean fetchData() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector( projectId).getEntityManagerFactory().createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            
            // Search the first ProteinSet which has a Best Protein Match with the searched name
            TypedQuery<Integer> searchQuery = entityManagerMSI.createQuery("SELECT ps.id FROM ProteinSet ps, ProteinMatch pm WHERE ps.typicalProteinMatchId=pm.id AND pm.accession LIKE :search ORDER BY ps.score DESC", Integer.class);
            searchQuery.setParameter("search", "%"+searchAccession+"%");
            List<Integer> proteinSetIdList = searchQuery.getResultList();
            
            if (proteinSetIdList.isEmpty()) {
                // No ProteinSet found, we search for a Protein Match in the subset
                searchQuery = entityManagerMSI.createQuery("SELECT ps.id FROM ProteinSet ps, ProteinMatch pm, ProteinSetProteinMatchItem ps_to_pm WHERE ps_to_pm.proteinSet.id=ps.id AND ps_to_pm.proteinMatch.id=pm.id AND ps_to_pm.resultSummary.id=:rsmId  AND pm.accession LIKE :search ORDER BY ps.score DESC", Integer.class);
                searchQuery.setParameter("search", "%"+searchAccession+"%");
                searchQuery.setParameter("rsmId", rsm.getId());
                
                proteinSetIdList = searchQuery.getResultList();  
            }
            
            searchResult.clear();
            searchResult.addAll(proteinSetIdList);
            
            
            
            
            entityManagerMSI.getTransaction().commit();
        } catch  (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
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
