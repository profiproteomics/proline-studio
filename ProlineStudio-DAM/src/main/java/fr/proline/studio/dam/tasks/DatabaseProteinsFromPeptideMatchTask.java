/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.BioSequence;
import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.util.DatabaseManager;
import fr.proline.studio.dam.AccessDatabaseThread;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;


/**
 *
 * @author JM235353
 */
public class DatabaseProteinsFromPeptideMatchTask extends AbstractDatabaseTask {
    
    private PeptideMatch peptideMatch = null;

    public DatabaseProteinsFromPeptideMatchTask(AbstractDatabaseCallback callback, PeptideMatch peptideMatch) {
        super(callback, Priority.NORMAL_3);
        this.peptideMatch = peptideMatch;        
    }

    @Override
    public boolean needToFetch() {
        return (peptideMatch.getTransientData().getProteinMatches() == null);
            
    }
    
    @Override
    public boolean fetchData() {

        EntityManager entityManagerMSI = DatabaseManager.getInstance().getMsiDbConnector( AccessDatabaseThread.getProjectIdTMP()).getEntityManagerFactory().createEntityManager();  //JPM.TODO : project id
        try {
            
            entityManagerMSI.getTransaction().begin();

            // Load Proteins for PeptideMatch
            TypedQuery<ProteinMatch> proteinMatchQuery = entityManagerMSI.createQuery("SELECT protm FROM ProteinMatch protm, SequenceMatch sm, PeptideMatch pepm, fr.proline.core.orm.msi.Peptide p WHERE pepm.id=:peptideMatchId AND  pepm.peptideId=p.id AND p.id=sm.id.peptideId AND sm.resultSetId=pepm.resultSet.id AND sm.id.proteinMatchId=protm.id ORDER BY protm.score DESC", ProteinMatch.class);
            proteinMatchQuery.setParameter("peptideMatchId", peptideMatch.getId());
            List<ProteinMatch> proteinMatchList = proteinMatchQuery.getResultList();
            

            // temporary Map to link a bioSequenceId to a ProteinMatch
            HashMap<Integer, ProteinMatch> biosequenceToProteinMap = new HashMap<Integer,ProteinMatch>();
            
            Iterator<ProteinMatch> it = proteinMatchList.iterator();
            while (it.hasNext()) {
                ProteinMatch proteinMatch = it.next();

                Integer bioSequenceId = proteinMatch.getBioSequenceId();
                if (bioSequenceId != null) {
                    biosequenceToProteinMap.put(bioSequenceId, proteinMatch);
                }
 
            }
            
            // retrieve mass
            if (biosequenceToProteinMap.size()>0) {
                Set idSet = biosequenceToProteinMap.keySet();
                List<Integer> ids = new ArrayList<Integer>(idSet.size());
                ids.addAll(idSet);


                Query massQuery = entityManagerMSI.createQuery("SELECT bs.id, bs FROM BioSequence bs WHERE bs.id IN (:listId)");
                massQuery.setParameter("listId", ids);
                
                List l = massQuery.getResultList();
                Iterator<Object[]> itMass=l.iterator();
                while (itMass.hasNext()) {
                    Object[] resCur = itMass.next();
                    Integer bioSequenceId = (Integer) resCur[0];
                    BioSequence bioSequence = (BioSequence) resCur[1];
                    ProteinMatch pm = biosequenceToProteinMap.get(bioSequenceId);
                    pm.getTransientData().setBioSequence(bioSequence);
                }
       
            }
            
            ProteinMatch[] proteins = proteinMatchList.toArray(new ProteinMatch[proteinMatchList.size()]);
            
            peptideMatch.getTransientData().setProteinMatches(proteins);
            
            
            entityManagerMSI.getTransaction().commit();
        } catch  (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }

        
        return true;
    }
    
    
}
