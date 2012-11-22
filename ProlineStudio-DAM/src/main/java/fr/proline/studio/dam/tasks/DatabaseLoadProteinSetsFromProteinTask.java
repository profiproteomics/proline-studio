/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author JM235353
 */
public class DatabaseLoadProteinSetsFromProteinTask extends AbstractDatabaseTask {

    private ProteinMatch proteinMatch;
    
    public DatabaseLoadProteinSetsFromProteinTask(AbstractDatabaseCallback callback, ProteinMatch proteinMatch) {
        super(callback, Priority.NORMAL_3);
        this.proteinMatch = proteinMatch;        
    }
    
    @Override
    public boolean needToFetch() {
        return (proteinMatch.getTransientData().getProteinSetArray() == null);
    }
    
    @Override
    public boolean fetchData() {

        EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(ProlineRepository.Databases.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
        try {

            entityManagerMSI.getTransaction().begin();

            
            // Load Protein Sets and their typical Protein Match in the same time
            Query proteinSetQuery = entityManagerMSI.createQuery("SELECT ps, typicalPm FROM ProteinMatch typicalPm, ProteinSetProteinMatchItem ps_to_pm, ProteinSet ps WHERE ps_to_pm.proteinSet.id=ps.id AND ps_to_pm.proteinMatch.id=:proteinMatchId AND ps.typicalProteinMatchId=typicalPm.id ORDER BY ps_to_pm.resultSummary.id ASC");
            proteinSetQuery.setParameter("proteinMatchId", proteinMatch.getId());
            List l = proteinSetQuery.getResultList();

            ProteinSet[] proteinSetArray = new ProteinSet[l.size()];
            
            int index = 0;
            Iterator<Object[]> itProteinSetsQuery = l.iterator();
            while (itProteinSetsQuery.hasNext()) {
                Object[] resCur = itProteinSetsQuery.next();
                ProteinSet ps = (ProteinSet) resCur[0];
                ProteinMatch bestProteinMatch = (ProteinMatch) resCur[1];
                
                ProteinSet.TransientData data = ps.getTransientData();
                if (data.getTypicalProteinMatch() == null) {
                    data.setTypicalProteinMatch(bestProteinMatch);
                }
                
                proteinSetArray[index++] = ps;
            }
            

            ProteinMatch.TransientData data = proteinMatch.getTransientData();
            data.setProteinSetArray(proteinSetArray);

            // Load Proteins for each ProteinSet
            for (int i=0;i<proteinSetArray.length; i++) {
                DatabaseProteinsFromProteinSetTask.fetchProteins(entityManagerMSI, proteinSetArray[i]);
            }
            
            //JPM.TODO : load name of ResultSummary...
            
            
            entityManagerMSI.getTransaction().commit();
        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }



        return true;
    }


    
}
