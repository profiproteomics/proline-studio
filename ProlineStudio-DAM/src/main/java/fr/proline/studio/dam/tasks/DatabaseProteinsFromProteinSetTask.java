package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dam.*;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;


/**
 * Load Proteins of a Protein Set and dispatch them in subset or sameset 
 * @author JM235353
 */
public class DatabaseProteinsFromProteinSetTask extends AbstractDatabaseTask{
    
    private ProteinSet proteinSet = null;

    public DatabaseProteinsFromProteinSetTask(AbstractDatabaseCallback callback, ProteinSet proteinSet) {
        super(callback);
        this.proteinSet = proteinSet;        
    }

    @Override
    public boolean needToFetch() {
        return (proteinSet.getTransientSameSet() == null);
            
    }
    
    @Override
    public boolean fetchData() {

        EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(ProlineRepository.Databases.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
        try {
            
            entityManagerMSI.getTransaction().begin();
            
            // number of proteins in sameset
            ProteinMatch typicalProtein = proteinSet.getTransientTypicalProteinMatch(); 
            int peptitesCountInSameSet = typicalProtein.getPeptideCount();

            
            // Load Proteins 
            TypedQuery<ProteinMatch> proteinMatchQuery = entityManagerMSI.createQuery("SELECT pm FROM ProteinMatch pm, ProteinSetProteinMatchItem ps_to_pm WHERE ps_to_pm.proteinSet.id=:proteinSetId AND ps_to_pm.proteinMatch.id=pm.id AND ps_to_pm.resultSummary.id=:rsmId", ProteinMatch.class);
            proteinMatchQuery.setParameter("proteinSetId", proteinSet.getId());
            proteinMatchQuery.setParameter("rsmId", proteinSet.getResultSummary().getId());
            List<ProteinMatch> proteinMatchList = proteinMatchQuery.getResultList();
            
            // Dispatch Proteins in sameSet and subSet
            ArrayList<ProteinMatch> sameSet = new ArrayList<ProteinMatch>(proteinMatchList.size());
            ArrayList<ProteinMatch> subSet  = new ArrayList<ProteinMatch>(proteinMatchList.size());
            
            Iterator<ProteinMatch> it = proteinMatchList.iterator();
            while (it.hasNext()) {
                ProteinMatch proteinMatch = it.next();
                if (proteinMatch.getPeptideCount() == peptitesCountInSameSet) {
                    // put protein in same set
                    sameSet.add(proteinMatch);
                } else {
                    // put protein in sub set
                    subSet.add(proteinMatch);
                }
            }
            
            ProteinMatch[] sameSetArray  = sameSet.toArray(new ProteinMatch[sameSet.size()]);
            ProteinMatch[] subSetArray = subSet.toArray(new ProteinMatch[subSet.size()]);

            // check if Proteins are in same set or sub set.
            proteinSet.setTransientSameSet(sameSetArray);
            proteinSet.setTransientSubSet(subSetArray);
            
            
            entityManagerMSI.getTransaction().commit();
        } catch  (RuntimeException e) {
            logger.error("DatabaseProteinSetsAction failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }

        
        return true;
    }
    
    
}
