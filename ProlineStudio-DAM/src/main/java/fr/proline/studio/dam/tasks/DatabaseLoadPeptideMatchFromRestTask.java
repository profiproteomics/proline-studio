/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 *
 * @author JM235353
 */
public class DatabaseLoadPeptideMatchFromRestTask extends AbstractDatabaseTask {
    


    
    private ResultSet rset = null;
    // data kept for sub tasks
    /*ArrayList<Integer> proteinMatchIds = null;
    HashMap<Integer, ProteinSet> proteinSetMap = null;
    ArrayList<Integer> proteinSetIds = null;*/

    public DatabaseLoadPeptideMatchFromRestTask(AbstractDatabaseCallback callback, ResultSet rset) {
        super(callback);
        this.rset = rset;
    }

    @Override
    public boolean needToFetch() {
        return (rset.getTransientPeptideMatches() == null);
    }

    @Override
    public boolean fetchData() {

        EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(ProlineRepository.Databases.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
        try {

            entityManagerMSI.getTransaction().begin();

            Integer rsetId = rset.getId();

            // Load Peptide Match
            // SELECT pm FROM PeptideMatch pm WHERE pm.resultSet.id=:rsetId ORDER BY pm.score DESC
            TypedQuery<PeptideMatch> peptideMatchQuery = entityManagerMSI.createQuery("SELECT pm FROM PeptideMatch pm WHERE pm.resultSet.id=:rsetId ORDER BY pm.score DESC", PeptideMatch.class);
            peptideMatchQuery.setParameter("rsetId", rsetId);
            List<PeptideMatch> peptideMatches = peptideMatchQuery.getResultList();

            PeptideMatch[] peptideMatchArray = peptideMatches.toArray(new PeptideMatch[peptideMatches.size()]);
            
            System.out.println("Peptides : "+peptideMatchArray.length);
            rset.setTransientPeptideMatches(peptideMatchArray);




            entityManagerMSI.getTransaction().commit();
        } catch (RuntimeException e) {
            logger.error(this.getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }


        return true;
    }


}
