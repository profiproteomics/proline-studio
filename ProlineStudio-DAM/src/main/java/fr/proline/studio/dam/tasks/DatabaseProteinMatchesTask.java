package fr.proline.studio.dam.tasks;


import fr.proline.core.orm.msi.BioSequence;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;


/**
 * Load Protein Matches for a Rset or for a PeptideMatch
 * @author JM235353
 */
public class DatabaseProteinMatchesTask extends AbstractDatabaseSlicerTask {
    
    // used to slice the task in sub tasks
    private static final int SLICE_SIZE = 1000;
    
        // different possible subtasks
    public static final int SUB_TASK_BIOSEQUENCE = 0;
    public static final int SUB_TASK_COUNT = 1; // <<----- get in sync
    
        // data kept for sub tasks
    private ArrayList<Long> m_proteinMatchIds = null;
    private HashMap<Long, DProteinMatch> m_proteinMatchMap = null;
    
    private long m_projectId = -1;
    private DPeptideMatch m_peptideMatch = null;
    private ResultSet m_rset = null;


    
    private int m_action;
    
    private final static int LOAD_PROTEINS_FROM_PEPTIDE_MATCH  = 0;
    private final static int LOAD_ALL_PROTEINS_OF_RSET = 1;
    
    
    public DatabaseProteinMatchesTask(AbstractDatabaseCallback callback, long projectId, DPeptideMatch peptideMatch) {
        super(callback);
        init(SLICE_SIZE, new TaskInfo("Load Proteins for a Peptide Match "+getPeptideName(peptideMatch), false, TASK_LIST_INFO));
        m_projectId = projectId;
        m_peptideMatch = peptideMatch;     
        m_action = LOAD_PROTEINS_FROM_PEPTIDE_MATCH;
    }
    
    public DatabaseProteinMatchesTask(AbstractDatabaseCallback callback, long projectId, ResultSet rset) {
        super(callback);
        init(SLICE_SIZE, new TaskInfo("Load Proteins of Search Result "+rset.getId(), false, TASK_LIST_INFO));
        m_projectId = projectId;
        m_rset = rset;        
        m_action = LOAD_ALL_PROTEINS_OF_RSET;
    }

    private static String getPeptideName(DPeptideMatch peptideMatch) {
        
        String name;
        
        Peptide peptide = peptideMatch.getPeptide();
        if (peptide != null) {
            name = peptide.getSequence();
        } else {
            name = String.valueOf(peptideMatch.getId());
        }
        
        return name;
    }
    
    
    @Override
    // must be implemented for all AbstractDatabaseSlicerTask 
    public void abortTask() {
        super.abortTask();
        switch (m_action) {
            case LOAD_ALL_PROTEINS_OF_RSET:
                m_rset.getTransientData().setProteinMatches(null);
                break;
            case LOAD_PROTEINS_FROM_PEPTIDE_MATCH:
                m_peptideMatch.setProteinMatches(null);
                break;
        }
    }
    
    @Override
    public boolean needToFetch() {
        
         switch (m_action) {
            case LOAD_ALL_PROTEINS_OF_RSET:
                return (m_rset.getTransientData().getProteinMatches() == null);
            case LOAD_PROTEINS_FROM_PEPTIDE_MATCH:
                return (m_peptideMatch.getProteinMatches() == null);
        }
        return false; // should not happen
        
        
            
    }
    
    @Override
    public boolean fetchData() {
        switch (m_action) {
            case LOAD_ALL_PROTEINS_OF_RSET:
                if (needToFetch()) {
                    return fechDataForRset();
                } else {
                    // fetch data of SubTasks
                    return fetchDataSubTask();
                }
            case LOAD_PROTEINS_FROM_PEPTIDE_MATCH:
                if (needToFetch()) {
                    return fetchDataForPeptideMatch();
                }  else {
                    // fetch data of SubTasks
                    return fetchDataSubTask();
                }
        }
        return true; // should not happen
    }
    
        /**
     * Fetch data of a Subtask
     *
     * @return
     */
    private boolean fetchDataSubTask() {
        SubTask slice = m_subTaskManager.getNextSubTask();
        if (slice == null) {
            return true; // nothing to do : should not happen
        }
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        
        try {

            entityManagerMSI.getTransaction().begin();

            switch (slice.getSubTaskId()) {
                case SUB_TASK_BIOSEQUENCE:
                    fetchBiosequence(entityManagerMSI, slice);
                    break;


            }


            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
            m_taskError = new TaskError(e);
            entityManagerMSI.getTransaction().rollback();
            return false;
        } finally {
            entityManagerMSI.close();
        }

        return true;
    }

    private boolean fechDataForRset() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            // Load Proteins for PeptideMatch
            TypedQuery<DProteinMatch> proteinMatchQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinMatch(protm.id, protm.accession, protm.score, protm.peptideCount, protm.resultSet.id, protm.description, protm.bioSequenceId)  FROM ProteinMatch protm WHERE protm.resultSet.id=:resultSetId ORDER BY protm.score DESC", DProteinMatch.class);
            proteinMatchQuery.setParameter("resultSetId", m_rset.getId());
            List<DProteinMatch> proteinMatchList = proteinMatchQuery.getResultList();

            
            int nbProteins = proteinMatchList.size();
            DProteinMatch[] proteins = proteinMatchList.toArray(new DProteinMatch[nbProteins]);

            m_rset.getTransientData().setProteinMatches(proteins);

            m_proteinMatchIds = new ArrayList<>(nbProteins);
            m_proteinMatchMap = new HashMap<>(nbProteins);
            for (int i=0;i<nbProteins;i++) {
                DProteinMatch pm = proteins[i];
                Long id = Long.valueOf(pm.getId());
                m_proteinMatchIds.add(id);
                m_proteinMatchMap.put(id, pm);
            }
            
            /**
             * Biosequence for each Protein Match
             *
             */
            // slice the task and get the first one
            SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_BIOSEQUENCE, m_proteinMatchIds.size(), SLICE_SIZE);

            // execute the first slice now
            fetchBiosequence(entityManagerMSI, subTask);

            
            
            entityManagerMSI.getTransaction().commit();
        } catch (RuntimeException e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerMSI.getTransaction().rollback();
            return false;
        } finally {
            entityManagerMSI.close();
        }
        return true;
    }
    
    private boolean fetchDataForPeptideMatch() {

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {
            
            entityManagerMSI.getTransaction().begin();

            // Load Proteins for PeptideMatch
            TypedQuery<DProteinMatch> proteinMatchQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinMatch(protm.id, protm.accession, protm.score, protm.peptideCount, protm.resultSet.id, protm.description, protm.bioSequenceId) FROM ProteinMatch protm, SequenceMatch sm, PeptideMatch pepm, fr.proline.core.orm.msi.Peptide p WHERE pepm.id=:peptideMatchId AND  pepm.peptideId=p.id AND p.id=sm.id.peptideId AND sm.resultSetId=pepm.resultSet.id AND sm.id.proteinMatchId=protm.id ORDER BY protm.score DESC", DProteinMatch.class);
            proteinMatchQuery.setParameter("peptideMatchId", m_peptideMatch.getId());
            List<DProteinMatch> proteinMatchList = proteinMatchQuery.getResultList();

            int nbProteins = proteinMatchList.size();
            DProteinMatch[] proteins = proteinMatchList.toArray(new DProteinMatch[nbProteins]);
            
            m_peptideMatch.setProteinMatches(proteins);
            
            m_proteinMatchIds = new ArrayList<>(nbProteins);
            m_proteinMatchMap = new HashMap<>(nbProteins);
            for (int i=0;i<nbProteins;i++) {
                DProteinMatch pm = proteins[i];
                Long id = Long.valueOf(pm.getId());
                m_proteinMatchIds.add(id);
                m_proteinMatchMap.put(id, pm);
            }
            
           /**
             * Biosequence for each Protein Match
             *
             */
            // slice the task and get the first one
            SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_BIOSEQUENCE, m_proteinMatchIds.size(), SLICE_SIZE);

            // execute the first slice now
            fetchBiosequence(entityManagerMSI, subTask);

            
            
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
    
    private void fetchBiosequence(EntityManager entityManagerMSI, SubTask subTask) {

        List sliceOfProteinMatchIds = subTask.getSubList(m_proteinMatchIds);
        
        // temporary Map to link a bioSequenceId to a ProteinMatch
        HashMap<Long, DProteinMatch> biosequenceToProteinMap = new HashMap<>();
        HashMap<String, DProteinMatch> accessionToProteinMap = new HashMap<>();

        Iterator<Long> itId = sliceOfProteinMatchIds.iterator();
        while (itId.hasNext()) {
            Long proteinMatchId =  itId.next();
            DProteinMatch proteinMatch = m_proteinMatchMap.get(proteinMatchId);

            Long bioSequenceId = proteinMatch.getBioSequenceId();
            if (bioSequenceId != null) {
                biosequenceToProteinMap.put(bioSequenceId, proteinMatch);
            } else {
                accessionToProteinMap.put(proteinMatch.getAccession(), proteinMatch);
            }

        }

        // retrieve biosequence
        if (biosequenceToProteinMap.size() > 0) {
            Set idSet = biosequenceToProteinMap.keySet();
            List<Integer> ids = new ArrayList<>(idSet.size());
            ids.addAll(idSet);


            TypedQuery<BioSequence> biosequenceQuery = entityManagerMSI.createQuery("SELECT bs FROM BioSequence bs WHERE bs.id IN (:listId)", BioSequence.class);
            biosequenceQuery.setParameter("listId", ids);

            List<BioSequence> l = biosequenceQuery.getResultList();
            Iterator<BioSequence> itBiosequence = l.iterator();
            while (itBiosequence.hasNext()) {
                BioSequence bioSequence = itBiosequence.next();
                DProteinMatch pm = biosequenceToProteinMap.get(bioSequence.getId());
                pm.setBioSequence(bioSequence);
            }

        }

    }
    
    
    
}
