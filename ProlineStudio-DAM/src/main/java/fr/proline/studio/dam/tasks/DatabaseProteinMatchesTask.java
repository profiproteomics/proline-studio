package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;


/**
 *
 * @author JM235353
 */
public class DatabaseProteinMatchesTask extends AbstractDatabaseSlicerTask {
    
    // used to slice the task in sub tasks
    private static final int SLICE_SIZE = 100;
    
        // different possible subtasks
    public static final int SUB_TASK_BIOSEQUENCE = 0;
    public static final int SUB_TASK_COUNT = 1; // <<----- get in sync
    
        // data kept for sub tasks
    private ArrayList<Long> m_proteinMatchIds = null;
    private HashMap<Long, ProteinMatch> m_proteinMatchMap = null;
    
    private long m_projectId = -1;
    private PeptideMatch m_peptideMatch = null;
    private ResultSet m_rset = null;


    
    private int m_action;
    
    private final static int LOAD_PROTEINS_FROM_PEPTIDE_MATCH  = 0;
    private final static int LOAD_ALL_PROTEINS_OF_RSET = 1;
    
    
    public DatabaseProteinMatchesTask(AbstractDatabaseCallback callback, long projectId, PeptideMatch peptideMatch) {
        super(callback);
        init(SLICE_SIZE, new TaskInfo("Load Proteins for a Peptide Match "+getPeptideName(peptideMatch), TASK_LIST_INFO));
        m_projectId = projectId;
        m_peptideMatch = peptideMatch;     
        m_action = LOAD_PROTEINS_FROM_PEPTIDE_MATCH;
    }
    
    public DatabaseProteinMatchesTask(AbstractDatabaseCallback callback, long projectId, ResultSet rset) {
        super(callback);
        init(SLICE_SIZE, new TaskInfo("Load Proteins of Search Result "+rset.getId(), TASK_LIST_INFO));
        m_projectId = projectId;
        m_rset = rset;        
        m_action = LOAD_ALL_PROTEINS_OF_RSET;
    }

    private static String getPeptideName(PeptideMatch peptideMatch) {
        
        String name;
        
        Peptide peptide = peptideMatch.getTransientData().getPeptide();
        if (peptide != null) {
            name = peptide.getSequence();
        } else {
            name = String.valueOf(peptideMatch.getId());
        }
        
        return name;
    }
    
    @Override
    public boolean needToFetch() {
        
         switch (m_action) {
            case LOAD_ALL_PROTEINS_OF_RSET:
                return (m_rset.getTransientData().getProteinMatches() == null);
            case LOAD_PROTEINS_FROM_PEPTIDE_MATCH:
                return (m_peptideMatch.getTransientData().getProteinMatches() == null);
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
        SubTask slice = subTaskManager.getNextSubTask();
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
            logger.error(getClass().getSimpleName()+" failed", e);
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
            TypedQuery<ProteinMatch> proteinMatchQuery = entityManagerMSI.createQuery("SELECT protm FROM ProteinMatch protm WHERE protm.resultSet.id=:resultSetId ORDER BY protm.score DESC", ProteinMatch.class);
            proteinMatchQuery.setParameter("resultSetId", m_rset.getId());
            List<ProteinMatch> proteinMatchList = proteinMatchQuery.getResultList();

            
            int nbProteins = proteinMatchList.size();
            ProteinMatch[] proteins = proteinMatchList.toArray(new ProteinMatch[nbProteins]);

            m_rset.getTransientData().setProteinMatches(proteins);

            m_proteinMatchIds = new ArrayList<>(nbProteins);
            m_proteinMatchMap = new HashMap<>(nbProteins);
            for (int i=0;i<nbProteins;i++) {
                ProteinMatch pm = proteins[i];
                Long id = Long.valueOf(pm.getId());
                m_proteinMatchIds.add(id);
                m_proteinMatchMap.put(id, pm);
            }
            
            /**
             * Biosequence for each Protein Match
             *
             */
            // slice the task and get the first one
            SubTask subTask = subTaskManager.sliceATaskAndGetFirst(SUB_TASK_BIOSEQUENCE, m_proteinMatchIds.size(), SLICE_SIZE);

            // execute the first slice now
            fetchBiosequence(entityManagerMSI, subTask);

            
            
            entityManagerMSI.getTransaction().commit();
        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName() + " failed", e);
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
            TypedQuery<ProteinMatch> proteinMatchQuery = entityManagerMSI.createQuery("SELECT protm FROM ProteinMatch protm, SequenceMatch sm, PeptideMatch pepm, fr.proline.core.orm.msi.Peptide p WHERE pepm.id=:peptideMatchId AND  pepm.peptideId=p.id AND p.id=sm.id.peptideId AND sm.resultSetId=pepm.resultSet.id AND sm.id.proteinMatchId=protm.id ORDER BY protm.score DESC", ProteinMatch.class);
            proteinMatchQuery.setParameter("peptideMatchId", m_peptideMatch.getId());
            List<ProteinMatch> proteinMatchList = proteinMatchQuery.getResultList();

            int nbProteins = proteinMatchList.size();
            ProteinMatch[] proteins = proteinMatchList.toArray(new ProteinMatch[nbProteins]);
            
            m_peptideMatch.getTransientData().setProteinMatches(proteins);
            
            m_proteinMatchIds = new ArrayList<>(nbProteins);
            m_proteinMatchMap = new HashMap<>(nbProteins);
            for (int i=0;i<nbProteins;i++) {
                ProteinMatch pm = proteins[i];
                Long id = Long.valueOf(pm.getId());
                m_proteinMatchIds.add(id);
                m_proteinMatchMap.put(id, pm);
            }
            
           /**
             * Biosequence for each Protein Match
             *
             */
            // slice the task and get the first one
            SubTask subTask = subTaskManager.sliceATaskAndGetFirst(SUB_TASK_BIOSEQUENCE, m_proteinMatchIds.size(), SLICE_SIZE);

            // execute the first slice now
            fetchBiosequence(entityManagerMSI, subTask);

            
            
            entityManagerMSI.getTransaction().commit();
        } catch  (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }

        
        return true;
    }
    
    private void fetchBiosequence(EntityManager entityManagerMSI, SubTask subTask) {

        List sliceOfProteinMatchIds = subTask.getSubList(m_proteinMatchIds);
        
        // temporary Map to link a bioSequenceId to a ProteinMatch
        HashMap<Long, ProteinMatch> biosequenceToProteinMap = new HashMap<>();
        HashMap<String, ProteinMatch> accessionToProteinMap = new HashMap<>();

        Iterator<Long> itId = sliceOfProteinMatchIds.iterator();
        while (itId.hasNext()) {
            Long proteinMatchId =  itId.next();
            ProteinMatch proteinMatch = m_proteinMatchMap.get(proteinMatchId);

            Long bioSequenceId = proteinMatch.getBioSequenceId();
            if (bioSequenceId != null) {
                biosequenceToProteinMap.put(bioSequenceId, proteinMatch);
            } else {
                accessionToProteinMap.put(proteinMatch.getAccession(), proteinMatch);
                proteinMatch.getTransientData().setNoBioSequenceFound(true); // we are not sure that we will find the biosequence in the PDI
            }

        }

        // retrieve mass
        if (biosequenceToProteinMap.size() > 0) {
            Set idSet = biosequenceToProteinMap.keySet();
            List<Integer> ids = new ArrayList<>(idSet.size());
            ids.addAll(idSet);


            Query massQuery = entityManagerMSI.createQuery("SELECT bs.id, bs FROM BioSequence bs WHERE bs.id IN (:listId)");
            massQuery.setParameter("listId", ids);

            List l = massQuery.getResultList();
            Iterator<Object[]> itMass = l.iterator();
            while (itMass.hasNext()) {
                Object[] resCur = itMass.next();
                Long bioSequenceId = (Long) resCur[0];
                fr.proline.core.orm.msi.BioSequence bioSequence = (BioSequence) resCur[1];
                ProteinMatch pm = biosequenceToProteinMap.get(bioSequenceId);
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
                    pm.getTransientData().setNoBioSequenceFound(false); // we have found a biosequence in the PDI
                }

                entityManagerPDI.getTransaction().commit();
            } finally {
                entityManagerPDI.close();
            }
        }


    }
    
    
    
}
