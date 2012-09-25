package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.*;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * Load Peptides corresponding to a ProteinMatch 
 * @author JM235353
 */
public class DatabaseLoadPeptidesInstancesTask extends AbstractDatabaseTask {
    
    private ProteinMatch proteinMatch = null;
    private Integer      rsmId = null;

    public DatabaseLoadPeptidesInstancesTask(AbstractDatabaseCallback callback, ProteinMatch proteinMatch, Integer rsmId) {
        super(callback);
        this.proteinMatch = proteinMatch;
        this.rsmId = rsmId;
    }
    
    @Override
    public boolean needToFetch() {
        return (proteinMatch.getTransientPeptideSet() == null);
    }
    
    @Override
    public boolean fetchData() {
        
        EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(ProlineRepository.Databases.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
        try {
            
            entityManagerMSI.getTransaction().begin();

            
            // Retrieve peptideSet of a peptideMatch
            TypedQuery<PeptideSet> peptideSetQuery = entityManagerMSI.createQuery("SELECT ps FROM PeptideSet ps, PeptideSetProteinMatchMap ps_to_pm WHERE ps_to_pm.id.proteinMatchId=:proteinMatchId AND ps_to_pm.id.peptideSetId=ps.id AND ps_to_pm.resultSummary.id=:rsmId", PeptideSet.class);
            peptideSetQuery.setParameter("proteinMatchId", proteinMatch.getId());
            peptideSetQuery.setParameter("rsmId", rsmId);
            PeptideSet peptideSet = peptideSetQuery.getSingleResult();
            proteinMatch.setTransientPeptideSet(peptideSet);
            
            
            // Retrieve peptideInstances of a peptideSet
            TypedQuery<PeptideInstance> peptideInstanceQuery = entityManagerMSI.createQuery("SELECT pi FROM PeptideInstance pi, PeptideSetPeptideInstanceItem ps_to_pi WHERE ps_to_pi.peptideSet.id=:peptideSetId AND ps_to_pi.peptideInstance.id=pi.id", PeptideInstance.class);
            peptideInstanceQuery.setParameter("peptideSetId", peptideSet.getId());
            List<PeptideInstance> peptideInstanceList = peptideInstanceQuery.getResultList();
            PeptideInstance[] peptideInstances = peptideInstanceList.toArray(new PeptideInstance[peptideInstanceList.size()]); 
            peptideSet.setTransientPeptideInstances(peptideInstances); 

            // Retrieve Best Peptide Match for each peptideInstance
            for (PeptideInstance peptideInstanceCur : peptideInstances) {
                PeptideMatch peptideMatch = entityManagerMSI.find(PeptideMatch.class, peptideInstanceCur.getBestPeptideMatchId());
                peptideInstanceCur.setTransientBestPeptideMatch(peptideMatch);
                
                // Retrieve peptide of the peptideMatch
                Peptide peptide = entityManagerMSI.find(Peptide.class, peptideMatch.getPeptideId());
                peptideMatch.setTransientPeptide(peptide);
                
            }
            
            
            entityManagerMSI.getTransaction().commit();
        } catch  (RuntimeException e) {
            logger.error("DatabaseLoadPeptidesInstancesTask failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }
        
        return true;
    }
    
    
}
