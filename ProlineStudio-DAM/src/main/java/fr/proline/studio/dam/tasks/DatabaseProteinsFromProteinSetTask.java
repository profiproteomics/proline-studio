package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.BioSequence;
import fr.proline.core.orm.msi.PeptideSet;
import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.core.orm.util.DatabaseManager;
import fr.proline.studio.dam.*;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;




/**
 * Load Proteins of a Protein Set and dispatch them in subset or sameset 
 * @author JM235353
 */
public class DatabaseProteinsFromProteinSetTask extends AbstractDatabaseTask {
    
    private Integer projectId = null;
    private ProteinSet proteinSet = null;

    public DatabaseProteinsFromProteinSetTask(AbstractDatabaseCallback callback, Integer projectId, ProteinSet proteinSet) {
        super(callback, Priority.NORMAL_3);
        this.projectId = projectId;
        this.proteinSet = proteinSet;        
    }

    @Override
    public boolean needToFetch() {
        return (proteinSet.getTransientData().getSameSet() == null);
            
    }
    
    @Override
    public boolean fetchData() {

        EntityManager entityManagerMSI = DatabaseManager.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();
        try {
            
            entityManagerMSI.getTransaction().begin();
            
            fetchProteins(entityManagerMSI, proteinSet);
            
            entityManagerMSI.getTransaction().commit();
        } catch  (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }

        
        return true;
    }
    
    protected static void fetchProteins( EntityManager entityManagerMSI, ProteinSet proteinSet) {
        // number of proteins in sameset
        //ProteinMatch typicalProtein = proteinSet.getTransientData().getTypicalProteinMatch();

        //int peptitesCountInSameSet = typicalProtein.getPeptideCount();

        Integer rsmId = proteinSet.getResultSummary().getId();

        // Load Proteins and their peptide count for the current result summary
        Query proteinMatchQuery = entityManagerMSI.createQuery("SELECT pm, pepset FROM ProteinMatch pm, ProteinSetProteinMatchItem ps_to_pm, PeptideSet pepset, PeptideSetProteinMatchMap pepset_to_pm WHERE ps_to_pm.proteinSet.id=:proteinSetId AND ps_to_pm.proteinMatch.id=pm.id AND ps_to_pm.resultSummary.id=:rsmId AND pepset_to_pm.resultSummary.id=:rsmId AND pepset_to_pm.id.peptideSetId=pepset.id AND pepset_to_pm.id.proteinMatchId=pm.id ORDER BY pm.score DESC");
        proteinMatchQuery.setParameter("proteinSetId", proteinSet.getId());
        proteinMatchQuery.setParameter("rsmId", rsmId);
        List<Object[]> proteinMatchList = proteinMatchQuery.getResultList();



        // Dispatch Proteins in sameSet and subSet
        ArrayList<ProteinMatch> sameSet = new ArrayList<ProteinMatch>(proteinMatchList.size());
        ArrayList<ProteinMatch> subSet = new ArrayList<ProteinMatch>(proteinMatchList.size());

        // temporary Map to link a bioSequenceId to a ProteinMatch
        HashMap<Integer, ProteinMatch> biosequenceToProteinMap = new HashMap<Integer, ProteinMatch>();

        Iterator<Object[]> it = proteinMatchList.iterator();
        int peptitesCountInSameSet = 0;
        while (it.hasNext()) {
            Object[] resCur = it.next();
            PeptideSet peptideSet = (PeptideSet) resCur[1];
            int peptideCount = peptideSet.getPeptideCount();
            if (peptideCount>peptitesCountInSameSet) {
                peptitesCountInSameSet = peptideCount;
            }
        }
        it = proteinMatchList.iterator();
        while (it.hasNext()) {
            Object[] resCur = it.next();
            ProteinMatch proteinMatch = (ProteinMatch) resCur[0];
            PeptideSet peptideSet = (PeptideSet) resCur[1];

            proteinMatch.getTransientData().setPeptideSet(rsmId, peptideSet);
            
            if (peptideSet.getPeptideCount() == peptitesCountInSameSet) {
                // put protein in same set
                sameSet.add(proteinMatch);
            } else {
                // put protein in sub set
                subSet.add(proteinMatch);
            }

            Integer bioSequenceId = proteinMatch.getBioSequenceId();
            if (bioSequenceId != null) {
                biosequenceToProteinMap.put(bioSequenceId, proteinMatch);
            }

        }

        // retrieve mass
        if (biosequenceToProteinMap.size() > 0) {
            Set idSet = biosequenceToProteinMap.keySet();
            List<Integer> ids = new ArrayList<Integer>(idSet.size());
            ids.addAll(idSet);


            Query massQuery = entityManagerMSI.createQuery("SELECT bs.id, bs FROM BioSequence bs WHERE bs.id IN (:listId)");
            massQuery.setParameter("listId", ids);

            List l = massQuery.getResultList();
            Iterator<Object[]> itMass = l.iterator();
            while (itMass.hasNext()) {
                Object[] resCur = itMass.next();
                Integer bioSequenceId = (Integer) resCur[0];
                BioSequence bioSequence = (BioSequence) resCur[1];
                ProteinMatch pm =  biosequenceToProteinMap.get(bioSequenceId);
                pm.getTransientData().setBioSequence(bioSequence);
            }

        }


        ProteinMatch[] sameSetArray = sameSet.toArray(new ProteinMatch[sameSet.size()]);
        ProteinMatch[] subSetArray = subSet.toArray(new ProteinMatch[subSet.size()]);

        // check if Proteins are in same set or sub set.
        proteinSet.getTransientData().setSameSet(sameSetArray);
        proteinSet.getTransientData().setSubSet(subSetArray);

    }
    
    
}
