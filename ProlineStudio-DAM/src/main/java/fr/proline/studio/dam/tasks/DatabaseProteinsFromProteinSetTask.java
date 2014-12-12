package fr.proline.studio.dam.tasks;


import fr.proline.core.orm.msi.PeptideSet;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;




/**
 * Load Proteins of a Protein Set and dispatch them in subset or sameset 
 * @author JM235353
 */
public class DatabaseProteinsFromProteinSetTask extends AbstractDatabaseTask {
    
    private long m_projectId = -1;
    private DProteinSet m_proteinSet = null;

    public DatabaseProteinsFromProteinSetTask(AbstractDatabaseCallback callback, long projectId, DProteinSet proteinSet) {
        super(callback, Priority.NORMAL_3, new TaskInfo("Load Proteins of a Protein Set "+getProteinSetName(proteinSet), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_projectId = projectId;
        m_proteinSet = proteinSet;        
    }
    
    public static String getProteinSetName(DProteinSet proteinSet) {

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
            try {
                entityManagerMSI.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerMSI.close();
        }

        
        return true;
    }
    
    protected static void fetchProteins( EntityManager entityManagerMSI, DProteinSet proteinSet) {

        Long rsmId = proteinSet.getResultSummaryId();

        // Load Proteins and their peptide count for the current result summary
        TypedQuery<DProteinMatch> proteinMatchQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinMatch(pm.id, pm.accession, pm.score, pm.peptideCount, pm.resultSet.id, pm.description, pepset) FROM ProteinMatch pm, ProteinSetProteinMatchItem ps_to_pm, PeptideSet pepset, PeptideSetProteinMatchMap pepset_to_pm WHERE ps_to_pm.proteinSet.id=:proteinSetId AND ps_to_pm.proteinMatch.id=pm.id AND ps_to_pm.resultSummary.id=:rsmId AND pepset_to_pm.resultSummary.id=:rsmId AND pepset_to_pm.id.peptideSetId=pepset.id AND pepset_to_pm.id.proteinMatchId=pm.id ORDER BY pepset.score DESC", DProteinMatch.class);
        proteinMatchQuery.setParameter("proteinSetId", proteinSet.getId());
         proteinMatchQuery.setParameter("rsmId", rsmId);
        List<DProteinMatch> proteinMatchList = proteinMatchQuery.getResultList();



        // Dispatch Proteins in sameSet and subSet
        LinkedList<DProteinMatch> sameSet = new LinkedList<>();
        ArrayList<DProteinMatch> subSet = new ArrayList<>(proteinMatchList.size());

        Long idTypicalProteinMatch = proteinSet.getTypicalProteinMatch().getId();
        

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
                if (proteinMatch.getId() == idTypicalProteinMatch) {
                    // typical protein match is put first
                    sameSet.addFirst(proteinMatch);
                } else {
                    sameSet.add(proteinMatch);
                }
            } else {
                // put protein in sub set
                subSet.add(proteinMatch);
            }


        }

        
        
        // retrieve biosequence
        DatabaseBioSequenceTask.fetchData(proteinMatchList, rsmId);


        DProteinMatch[] sameSetArray = sameSet.toArray(new DProteinMatch[sameSet.size()]);
        DProteinMatch[] subSetArray = subSet.toArray(new DProteinMatch[subSet.size()]);

        // check if Proteins are in same set or sub set.
        proteinSet.setSameSet(sameSetArray);
        proteinSet.setSubSet(subSetArray);

    }
    
    
}
