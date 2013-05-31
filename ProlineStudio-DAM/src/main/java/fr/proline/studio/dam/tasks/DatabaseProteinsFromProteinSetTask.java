package fr.proline.studio.dam.tasks;


import fr.proline.core.orm.msi.PeptideSet;
import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.*;
import fr.proline.studio.dam.taskinfo.TaskInfo;
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
        super(callback, Priority.NORMAL_3, new TaskInfo("Load Proteins of a Protein Set "+getProteinSetName(proteinSet), TASK_LIST_INFO));
        this.projectId = projectId;
        this.proteinSet = proteinSet;        
    }
    
    private static String getProteinSetName(ProteinSet proteinSet) {

        String name;
        
        ProteinMatch proteinMatch = proteinSet.getTransientData().getTypicalProteinMatch();
        if (proteinMatch != null) {
            name = proteinMatch.getAccession();
        } else {
            name = String.valueOf(proteinSet.getId());
        }

        return name;
    }
    

    @Override
    public boolean needToFetch() {
        return (proteinSet.getTransientData().getSameSet() == null);
            
    }
    
    @Override
    public boolean fetchData() {

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();
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
        Query proteinMatchQuery = entityManagerMSI.createQuery("SELECT pm, pepset FROM ProteinMatch pm, ProteinSetProteinMatchItem ps_to_pm, PeptideSet pepset, PeptideSetProteinMatchMap pepset_to_pm WHERE ps_to_pm.proteinSet.id=:proteinSetId AND ps_to_pm.proteinMatch.id=pm.id AND ps_to_pm.resultSummary.id=:rsmId AND pepset_to_pm.resultSummary.id=:rsmId AND pepset_to_pm.id.peptideSetId=pepset.id AND pepset_to_pm.id.proteinMatchId=pm.id ORDER BY pepset.score DESC");
        proteinMatchQuery.setParameter("proteinSetId", proteinSet.getId());
         proteinMatchQuery.setParameter("rsmId", rsmId);
        List<Object[]> proteinMatchList = proteinMatchQuery.getResultList();



        // Dispatch Proteins in sameSet and subSet
         ArrayList<ProteinMatch> sameSet = new ArrayList<>(proteinMatchList.size());
        ArrayList<ProteinMatch> subSet = new ArrayList<>(proteinMatchList.size());

        // temporary Map to link a bioSequenceId to a ProteinMatch
        HashMap<Integer, ProteinMatch> biosequenceToProteinMap = new HashMap<>();
        HashMap<String, ProteinMatch> accessionToProteinMap = new HashMap<>();

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
            } else {
                accessionToProteinMap.put(proteinMatch.getAccession(), proteinMatch);
            }

        }

        // retrieve biosequence
        if (!biosequenceToProteinMap.isEmpty()) {
            Set idSet = biosequenceToProteinMap.keySet();
            List<Integer> ids = new ArrayList<>(idSet.size());
            ids.addAll(idSet);


            Query bioseqQuery = entityManagerMSI.createQuery("SELECT bs.id, bs FROM fr.proline.core.orm.msi.BioSequence bs WHERE bs.id IN (:listId)");
            bioseqQuery.setParameter("listId", ids);

            List l = bioseqQuery.getResultList();
            Iterator<Object[]> itBioseq = l.iterator();
            while (itBioseq.hasNext()) {
                Object[] resCur = itBioseq.next();
                Integer bioSequenceId = (Integer) resCur[0];
                fr.proline.core.orm.msi.BioSequence bioSequence = (fr.proline.core.orm.msi.BioSequence) resCur[1];
                ProteinMatch pm =  biosequenceToProteinMap.get(bioSequenceId);
                pm.getTransientData().setBioSequenceMSI(bioSequence);
            }

        }
        if (!accessionToProteinMap.isEmpty()) {
            
            Set<String> accessionSet = accessionToProteinMap.keySet();
            List<String> accessionList = new ArrayList<>(accessionSet.size());
            accessionList.addAll(accessionSet);
            
            // we are going to look to a Biosequence in PDI db through ProteinIdentifier
            EntityManager entityManagerPDI = DataStoreConnectorFactory.getInstance().getPdiDbConnector().getEntityManagerFactory().createEntityManager();
            try {
                entityManagerPDI.getTransaction().begin();

                Query bioseqQuery = entityManagerPDI.createQuery("SELECT pi.value, pi.bioSequence FROM ProteinIdentifier pi WHERE pi.value IN (:listAccession) ");
                bioseqQuery.setParameter("listAccession", accessionList);

                List l = bioseqQuery.getResultList();
                Iterator<Object[]> itBioseq = l.iterator();
                while (itBioseq.hasNext()) {
                    Object[] resCur = itBioseq.next();
                    String accession = (String) resCur[0];
                    fr.proline.core.orm.pdi.BioSequence bioSequence = (fr.proline.core.orm.pdi.BioSequence) resCur[1];
                    ProteinMatch pm = accessionToProteinMap.get(accession);
                    pm.getTransientData().setBioSequencePDI(bioSequence);
                }
                
                entityManagerPDI.getTransaction().commit();
            } finally {
                entityManagerPDI.close();
            }
        }


        ProteinMatch[] sameSetArray = sameSet.toArray(new ProteinMatch[sameSet.size()]);
        ProteinMatch[] subSetArray = subSet.toArray(new ProteinMatch[subSet.size()]);

        // check if Proteins are in same set or sub set.
        proteinSet.getTransientData().setSameSet(sameSetArray);
        proteinSet.getTransientData().setSubSet(subSetArray);

    }
    
    
}
