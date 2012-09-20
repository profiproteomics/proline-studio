package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dam.*;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;


/**
 * Load Protein Sets and their Typical ProteinMatch of a Result Summary
 * @author JM235353
 */
public class DatabaseProteinSetsTask extends AbstractDatabaseTask{
    
    private ResultSummary rsm = null;

    public DatabaseProteinSetsTask(AbstractDatabaseCallback callback, ResultSummary rsm) {
        super(callback);
        this.rsm = rsm;        
    }


    
    @Override
    public boolean fetchData() {

        EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(ProlineRepository.Databases.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
        try {
            
            entityManagerMSI.getTransaction().begin();
            
            Integer rsmId = rsm.getId();
            
            ORMDataManager memMgr = ORMDataManager.instance();
            
            // Load Protein Sets
            TypedQuery<ProteinSet> proteinSetsQuery = entityManagerMSI.createQuery("SELECT ps FROM ProteinSet ps WHERE ps.resultSummary.id=:rsmId", ProteinSet.class);
            proteinSetsQuery.setParameter("rsmId", rsmId);
            List<ProteinSet> proteinSets = proteinSetsQuery.getResultList();
            
            ProteinSet[] proteinSetArray = proteinSets.toArray(new ProteinSet[proteinSets.size()]);
            memMgr.put(ResultSummary.class, rsmId, "ProteinSet[]", proteinSetArray);
            
            
            
            Iterator<ProteinSet> it = proteinSets.iterator();
            while (it.hasNext()) {
                ProteinSet proteinSetCur = it.next();
                
                // Load Typical Protein Match for each  Protein Set
                Integer typicalProteinMatchId = proteinSetCur.getProteinMatchId();
                ProteinMatch proteinMatch = entityManagerMSI.find(ProteinMatch.class, typicalProteinMatchId);
                proteinMatch.getPeptideCount(); // force lazy peptideMatchCount to be loaded to be used later
                memMgr.put(ProteinSet.class, proteinSetCur.getId(), "ProteinMatch", proteinMatch);
            
            }
            
            
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
