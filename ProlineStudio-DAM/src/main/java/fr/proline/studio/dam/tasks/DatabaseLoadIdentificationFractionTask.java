package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.data.IdentificationFractionData;
import fr.proline.studio.dam.data.AbstractData;
import fr.proline.core.orm.uds.Identification;
import fr.proline.core.orm.uds.IdentificationFraction;
import fr.proline.core.orm.uds.IdentificationSummary;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dam.*;
import fr.proline.studio.dam.data.ResultSummaryData;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * For an Identification : load identification fractions and identification summary
 * (with its Result Summary)
 * @author JM235353
 */
public class DatabaseLoadIdentificationFractionTask extends AbstractDatabaseTask {
    
    private Identification identification = null;
    private List<AbstractData> list = null;
    
    public DatabaseLoadIdentificationFractionTask(AbstractDatabaseCallback callback, Identification identification, List<AbstractData> list) {
        super(callback);
        this.identification = identification;
        this.list = list;
        
    }
    
    @Override
    public boolean needToFetch() {
        return true; // anyway this task is used only one time for each node
            
    }
    
    @Override
    public boolean fetchData() {

        
        EntityManager entityManagerUDS = ProlineDBManagement.getProlineDBManagement().getEntityManager(ProlineRepository.Databases.UDS, true);
        try {
            entityManagerUDS.getTransaction().begin();

            // We are obliged to merge, to avoid lazy exception in identification
            // because the previous entityManager has been closed
            Identification identificationMerged = entityManagerUDS.merge(identification);
            
            // load identification fractions
            List<IdentificationFraction> identificationFractions = identificationMerged.getFractions();
            Iterator<IdentificationFraction> it = identificationFractions.iterator();
            while (it.hasNext()) {
                IdentificationFraction identificationFraction = it.next();
                identificationFraction.getRawFile(); // be sure that rawFile connection is loaded now
                list.add(new IdentificationFractionData(identificationFraction));
            }
            
            // load identification summary
            IdentificationSummary identificationSummary = identificationMerged.getActiveSummary();
            if (identificationSummary != null) {
                Integer resultSummaryId = identificationSummary.getResultSummaryId();
                
                EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(ProlineRepository.Databases.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
                try {
                    
                    entityManagerMSI.getTransaction().begin();
                    
                    // load result summary of the identification summary
                    ResultSummary rsm = entityManagerMSI.find(ResultSummary.class, resultSummaryId);
                    if (rsm == null) {
                        // should not happen
                        logger.error("IdentificationSummary : ResultSummary not found with Id :"+resultSummaryId);
                    } else {
                        
                        // Check if the ResultSummary has Children without loading them.
                        TypedQuery<Long> isEmptyQuery = entityManagerMSI.createQuery("SELECT count(*) FROM ResultSummary rs WHERE rs.id=:resultSummaryId AND rs.children IS EMPTY", Long.class);
                        isEmptyQuery.setParameter("resultSummaryId", rsm.getId());
                        boolean hasChildren = (isEmptyQuery.getSingleResult() == 0);
                        
                        list.add(new ResultSummaryData(rsm, hasChildren));
                    }

                    entityManagerMSI.getTransaction().commit();
                    
                } catch  (RuntimeException e) {
                    logger.error("DatabaseLoadIdentificationFractionAction failed", e);
                } finally {
                    entityManagerMSI.close();
                }
            }
            
            entityManagerUDS.getTransaction().commit();

        } catch (RuntimeException e) {
            logger.error("DatabaseLoadIdentificationFractionAction failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }
        
        return true;
    }
    
    
}
