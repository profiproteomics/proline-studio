package fr.proline.studio.dam.tasks;


import fr.proline.core.orm.msi.PeptideSet;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;




/**
 * Load Proteins of a Protein Set and dispatch them in subset or sameset 
 * @author JM235353
 */
public class DatabaseProteinsFromProteinSetTask extends AbstractDatabaseTask {
    
    private long m_projectId = -1;
    private DProteinSet m_proteinSet = null;

    public DatabaseProteinsFromProteinSetTask(AbstractDatabaseCallback callback, long projectId, DProteinSet proteinSet) {
        super(callback, Priority.NORMAL_3, new TaskInfo("Load Proteins of a Protein Set "+getProteinSetName(proteinSet), false, TASK_LIST_INFO));
        m_projectId = projectId;
        m_proteinSet = proteinSet;        
    }
    
    private static String getProteinSetName(DProteinSet proteinSet) {

        String name;
        
        DProteinMatch proteinMatch = proteinSet.getTypicalProteinMatch();
        if (proteinMatch != null) {
            name = proteinMatch.getAccession();
        } else {
            name = String.valueOf(proteinSet.getId());
        }

        return name;
    }


    @Override
    public boolean needToFetch() {
        return (m_proteinSet.getSameSet() == null);
            
    }
    
    @Override
    public boolean fetchData() {

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            
            entityManagerMSI.getTransaction().begin();
            
            fetchProteins(entityManagerMSI, m_proteinSet);
            
            entityManagerMSI.getTransaction().commit();
        } catch  (RuntimeException e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
            m_taskError = new TaskError(e);
            entityManagerMSI.getTransaction().rollback();
            return false;
        } finally {
            entityManagerMSI.close();
        }

        
        return true;
    }
    
    protected static void fetchProteins( EntityManager entityManagerMSI, DProteinSet proteinSet) {
        // number of proteins in sameset
        //ProteinMatch typicalProtein = proteinSet.getTransientData().getTypicalProteinMatch();

        //int peptitesCountInSameSet = typicalProtein.getPeptideCount();

        Long rsmId = proteinSet.getResultSummaryId();

        // Load Proteins and their peptide count for the current result summary
        TypedQuery<DProteinMatch> proteinMatchQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinMatch(pm.id, pm.accession, pm.score, pm.peptideCount, pm.resultSet.id, pm.description, pm.bioSequenceId, pepset) FROM ProteinMatch pm, ProteinSetProteinMatchItem ps_to_pm, PeptideSet pepset, PeptideSetProteinMatchMap pepset_to_pm WHERE ps_to_pm.proteinSet.id=:proteinSetId AND ps_to_pm.proteinMatch.id=pm.id AND ps_to_pm.resultSummary.id=:rsmId AND pepset_to_pm.resultSummary.id=:rsmId AND pepset_to_pm.id.peptideSetId=pepset.id AND pepset_to_pm.id.proteinMatchId=pm.id ORDER BY pepset.score DESC", DProteinMatch.class);
        proteinMatchQuery.setParameter("proteinSetId", proteinSet.getId());
         proteinMatchQuery.setParameter("rsmId", rsmId);
        List<DProteinMatch> proteinMatchList = proteinMatchQuery.getResultList();



        // Dispatch Proteins in sameSet and subSet
        ArrayList<DProteinMatch> sameSet = new ArrayList<>(proteinMatchList.size());
        ArrayList<DProteinMatch> subSet = new ArrayList<>(proteinMatchList.size());

        // temporary Map to link a bioSequenceId to a ProteinMatch
        HashMap<Long, DProteinMatch> biosequenceToProteinMap = new HashMap<>();
        HashMap<String, DProteinMatch> accessionToProteinMap = new HashMap<>();

        Iterator<DProteinMatch> it = proteinMatchList.iterator();
        int peptitesCountInSameSet = 0;
        while (it.hasNext()) {
            DProteinMatch proteinMatch = it.next();
            PeptideSet peptideSet = proteinMatch.getPeptideSet(rsmId);
            int peptideCount = peptideSet.getPeptideCount();
            if (peptideCount>peptitesCountInSameSet) {
                peptitesCountInSameSet = peptideCount;
            }
        }
        it = proteinMatchList.iterator();
        while (it.hasNext()) {

            DProteinMatch proteinMatch = it.next();
            PeptideSet peptideSet = proteinMatch.getPeptideSet(rsmId);

            proteinMatch.setPeptideSet(rsmId, peptideSet);
            
            if (peptideSet.getPeptideCount() == peptitesCountInSameSet) {
                // put protein in same set
                sameSet.add(proteinMatch);
            } else {
                // put protein in sub set
                subSet.add(proteinMatch);
            }

            /*Long bioSequenceId = proteinMatch.getBioSequenceId();
            if (bioSequenceId != null) {
                biosequenceToProteinMap.put(bioSequenceId, proteinMatch);
            } else {*/  //JPM.TODO
                accessionToProteinMap.put(proteinMatch.getAccession(), proteinMatch);
            //}

        }

        // retrieve biosequence
        /*if (!biosequenceToProteinMap.isEmpty()) {
            Set idSet = biosequenceToProteinMap.keySet();
            List<Integer> ids = new ArrayList<>(idSet.size());
            ids.addAll(idSet);


            Query bioseqQuery = entityManagerMSI.createQuery("SELECT bs.id, bs FROM fr.proline.core.orm.msi.BioSequence bs WHERE bs.id IN (:listId)");
            bioseqQuery.setParameter("listId", ids);

            List l = bioseqQuery.getResultList();
            Iterator<Object[]> itBioseq = l.iterator();
            while (itBioseq.hasNext()) {
                Object[] resCur = itBioseq.next();
                Long bioSequenceId = (Long) resCur[0];
                fr.proline.core.orm.msi.BioSequence bioSequence = (fr.proline.core.orm.msi.BioSequence) resCur[1];
                DProteinMatch pm =  biosequenceToProteinMap.get(bioSequenceId);
                pm.setBioSequence(bioSequence);
            }

        }*/ //JPM.TODO



        DProteinMatch[] sameSetArray = sameSet.toArray(new DProteinMatch[sameSet.size()]);
        DProteinMatch[] subSetArray = subSet.toArray(new DProteinMatch[subSet.size()]);

        // check if Proteins are in same set or sub set.
        proteinSet.setSameSet(sameSetArray);
        proteinSet.setSubSet(subSetArray);

    }
    
    
}
