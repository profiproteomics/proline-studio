/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.actions;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.data.IdentificationFractionData;
import fr.proline.studio.dam.data.Data;
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

/**
 *
 * @author JM235353
 */
public class DatabaseLoadIdentificationFractionAction extends DatabaseAction{
    
    private Identification identification = null;
    private List<Data> list = null;
    
    public DatabaseLoadIdentificationFractionAction(DatabaseCallback callback, Identification identification, List<Data> list) {
        super(callback);
        this.identification = identification;
        this.list = list;
        
    }

    public boolean callbackInAWT() {
        return false;
    }
    
    public boolean fetchData() {

        
        EntityManager entityManagerUDS = ProlineDBManagement.getProlineDBManagement().getEntityManager(ProlineRepository.Databases.UDS, true);
        try {
            entityManagerUDS.getTransaction().begin();

            Identification identification2 = entityManagerUDS.merge(identification);
            
            
            List<IdentificationFraction> identificationFractions = identification2.getFractions();
            Iterator<IdentificationFraction> it = identificationFractions.iterator();
            while (it.hasNext()) {
                IdentificationFraction identificationFraction = it.next();
                identificationFraction.getRawFile(); // be sure that rawFile connection is loaded now
                list.add(new IdentificationFractionData(identificationFraction));
            }
            
            IdentificationSummary identificationSummary = identification2.getActiveSummary();
            if (identificationSummary != null) {
                Integer resultSummaryId = identificationSummary.getResultSummaryId();
                
                EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(ProlineRepository.Databases.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
                try {
                    
                    entityManagerMSI.getTransaction().begin();
                    
                    ResultSummary rsm = entityManagerMSI.find(ResultSummary.class, resultSummaryId);
                    if (rsm == null) {
                        // should not happen
                        logger.error("IdentificationSummary : ResultSummary not found with Id :"+resultSummaryId);
                    } else {
                        list.add(new ResultSummaryData(rsm));
                    }

                    entityManagerMSI.getTransaction().commit();
                    
                } catch  (RuntimeException e) {
                    logger.error("DatabaseLoadIdentificationFractionAction failed", e);
                }
                entityManagerMSI.close();
                
            }
            
            entityManagerUDS.getTransaction().commit();

        } catch (RuntimeException e) {
            logger.error("DatabaseLoadIdentificationFractionAction failed", e);
            entityManagerUDS.getTransaction().rollback();
        }

        entityManagerUDS.close();
        
        return true;
    }
    
    
}
