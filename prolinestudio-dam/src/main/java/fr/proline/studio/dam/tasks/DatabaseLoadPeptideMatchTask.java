package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.SequenceMatch;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptideSet;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.msi.dto.DSpectrum;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;


/**
 * Task used to load Peptide Matches
 * @author JM235353
 */
public class DatabaseLoadPeptideMatchTask extends AbstractDatabaseSlicerTask {
    
    // used to slice the task in sub tasks
    private static final int SLICE_SIZE = 1000;
    // different possible subtasks
    public static final int SUB_TASK_PEPTIDE_MATCH = 0;
    public static final int SUB_TASK_PEPTIDE = 1;
    public static final int SUB_TASK_MSQUERY = 2;
    public static final int SUB_TASK_PROTEINSET_NAME_LIST = 3;
    public static final int SUB_TASK_SRC_DAT_FILE = 4;
    public static final int SUB_TASK_COUNT_RSET = 5; // <<----- get in sync // SUB_TASK_PROTEINSET_NAME_LIST not used for RSET
    public static final int SUB_TASK_COUNT_RSM = 5; // <<----- get in sync
    
    private long m_projectId = -1;
    private ResultSet m_rset = null;
    private ResultSummary m_rsm = null;
    private DProteinMatch m_proteinMatch = null;
    private DProteinSet m_proteinSet = null;
    private PeptideInstance m_peptideInstance = null;
    private DMsQuery m_msQuery = null;
    private List<DPeptideMatch> m_listPeptideMatches =null;
    
    // data kept for sub tasks
    private ArrayList<Long> m_peptideMatchIds = null;
    private HashMap<Long, DPeptideMatch> m_peptideMatchMap = null;
    private HashMap<Long,  ArrayList<DPeptideMatch>> m_peptideMatchSequenceMatchArrayMap = null;
    private HashMap<Long, Integer> m_peptideMatchPosition = null;


    private final int m_action;
    
    private final static int LOAD_ALL_RSET   = 0;
    private final static int LOAD_ALL_RSM = 1;
    private final static int LOAD_PEPTIDES_FOR_PROTEIN_RSET = 2;
    private final static int LOAD_PSM_FOR_PROTEIN_SET_RSM = 3;
    private final static int LOAD_PSM_FOR_PEPTIDE_RSM = 4;
    private final static int LOAD_PSM_FOR_MSQUERY = 5;
    
    public DatabaseLoadPeptideMatchTask(AbstractDatabaseCallback callback, long projectId, ResultSet rset) {
        super(callback, SUB_TASK_COUNT_RSET, new TaskInfo("Load Peptide Matches for Search Result "+rset.getId(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_rset = rset;   
        m_action = LOAD_ALL_RSET;
    }
    public DatabaseLoadPeptideMatchTask(AbstractDatabaseCallback callback, long projectId, ResultSet rset, DProteinMatch proteinMatch) {
        super(callback, SUB_TASK_COUNT_RSET, new TaskInfo("Load Peptide Matches for Protein Match " + proteinMatch.getAccession() , false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_projectId = projectId;
        m_rset = rset;
        m_proteinMatch = proteinMatch;
        m_action = LOAD_PEPTIDES_FOR_PROTEIN_RSET;
    }
    public DatabaseLoadPeptideMatchTask(AbstractDatabaseCallback callback, long projectId, ResultSummary rsm) {
        super(callback, SUB_TASK_COUNT_RSM, new TaskInfo("Load Peptide Matches for Identification Summary "+rsm.getId(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_rsm = rsm;
        m_action = LOAD_ALL_RSM;
    }
    public DatabaseLoadPeptideMatchTask(AbstractDatabaseCallback callback, long projectId, ResultSummary rsm, DProteinSet proteinSet) {
        super(callback, SUB_TASK_COUNT_RSM, new TaskInfo("Load Peptide Matches for Protein Set "+proteinSet.getTypicalProteinMatch().getAccession(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_projectId = projectId;
        m_rsm = rsm;
        m_proteinSet = proteinSet;
        m_action = LOAD_PSM_FOR_PROTEIN_SET_RSM;

    }
    public DatabaseLoadPeptideMatchTask(AbstractDatabaseCallback callback, long projectId, PeptideInstance pi) {
        super(callback, SUB_TASK_COUNT_RSM, new TaskInfo("Load PSM for Peptide " + pi.getTransientData().getBestPeptideMatch().getPeptide().getSequence(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_projectId = projectId;
        m_peptideInstance = pi;
        m_rsm = pi.getResultSummary();
        m_action = LOAD_PSM_FOR_PEPTIDE_RSM;
    }
    
    public DatabaseLoadPeptideMatchTask(AbstractDatabaseCallback callback, long projectId, DMsQuery msQuery, ResultSummary rsm, ResultSet rs, List<DPeptideMatch> listPeptideMatches){
        super(callback, SUB_TASK_COUNT_RSM, new TaskInfo("Load PSM for MsQuery " + (msQuery == null ? "null": msQuery.getId()), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_msQuery = msQuery;
        m_rsm = rsm;
        m_rset = rs;
        m_listPeptideMatches = listPeptideMatches;
        m_action = LOAD_PSM_FOR_MSQUERY;
    }
    
    

    @Override
    // must be implemented for all AbstractDatabaseSlicerTask 
    public void abortTask() {
        super.abortTask();
        switch (m_action) {
            case LOAD_ALL_RSET:
                m_rset.getTransientData().setPeptideMatchIds(null);
                m_rset.getTransientData().setPeptideMatches(null);
                break;
            case LOAD_ALL_RSM:
                m_rsm.getTransientData().setPeptideMatches(null);
                m_rsm.getTransientData().setPeptideMatchesId(null);
                break;
            case LOAD_PEPTIDES_FOR_PROTEIN_RSET:
                m_proteinMatch.setPeptideMatches(null);
                m_proteinMatch.setPeptideMatchesId(null);
                break;
            case LOAD_PSM_FOR_PROTEIN_SET_RSM:
                m_proteinSet.getTypicalProteinMatch().setPeptideMatches(null);
                break;
            case LOAD_PSM_FOR_PEPTIDE_RSM:
                m_peptideInstance.getTransientData().setPeptideMatches(null);
                m_peptideInstance.getTransientData().setPeptideMatchesId(null);
                break;
            case LOAD_PSM_FOR_MSQUERY:
                m_listPeptideMatches = null;
        }
    }
    
    
    @Override
    public boolean needToFetch() {
        switch (m_action) {
            case LOAD_ALL_RSET:
                return (m_rset.getTransientData().getPeptideMatchIds() == null);
            case LOAD_ALL_RSM:
                return (m_rsm.getTransientData().getPeptideMatches() == null);
            case LOAD_PEPTIDES_FOR_PROTEIN_RSET:
                return (m_proteinMatch.getPeptideMatches() == null);
            case LOAD_PSM_FOR_PROTEIN_SET_RSM:
                return (m_proteinSet.getTypicalProteinMatch().getPeptideMatches() == null);
            case LOAD_PSM_FOR_PEPTIDE_RSM:
                return (m_peptideInstance.getTransientData().getPeptideMatches() == null);
            case LOAD_PSM_FOR_MSQUERY:
                return (m_listPeptideMatches == null || m_listPeptideMatches.isEmpty());
        }

        return false; // should never be called
    }
    
    @Override
    public boolean fetchData() {
        if (needToFetch()) {
            // first data are fetched
             switch (m_action) {
                case LOAD_ALL_RSET:
                    return fetchAllRsetMainTask();
                case LOAD_ALL_RSM:
                    return fetchAllRsmMainTask();
                case LOAD_PEPTIDES_FOR_PROTEIN_RSET:
                    return fetchPeptidesForProteinRsetMainTask();
                case LOAD_PSM_FOR_PROTEIN_SET_RSM:
                    return fetchPeptidesForProteinSetRsmMainTask();
                case LOAD_PSM_FOR_PEPTIDE_RSM:
                    return fetchPSMForPeptideInstanceMainTask();
                case LOAD_PSM_FOR_MSQUERY: 
                    return fetchPSMForMsQueryMainTask();
             }
             return false; // should never be called
        } else {
            // fetch data of SubTasks
            return fetchDataSubTask();
        }
    }

    
    public boolean fetchAllRsetMainTask() {

        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            Long rsetId = m_rset.getId();
            //"SELECT pm.id FROM PeptideMatch pm, Peptide p, MsQuery msq 
            // WHERE pm.resultSet.id=:rsetId AND pm.peptideId=p.id AND pm.msQuery=msq 
            // ORDER BY msq.initialId ASC, p.sequence ASC", Long.class);
            TypedQuery<Long> peptideMatchIdQuery = entityManagerMSI.createQuery("SELECT pm.id FROM PeptideMatch pm, fr.proline.core.orm.msi.Peptide p, MsQuery msq WHERE pm.resultSet.id=:rsetId AND pm.peptideId=p.id AND pm.msQuery=msq ORDER BY msq.initialId ASC, p.sequence ASC", Long.class);
            peptideMatchIdQuery.setParameter("rsetId", rsetId);
            List<Long> peptideMatchIds = peptideMatchIdQuery.getResultList();
            
            
            m_peptideMatchIds = new ArrayList<>(peptideMatchIds);
            long[] peptideMatchIdsArray = new long[m_peptideMatchIds.size()];
            m_peptideMatchPosition = new HashMap<>();
            for (int i = 0; i < peptideMatchIdsArray.length; i++) {
                Long id = m_peptideMatchIds.get(i);
                peptideMatchIdsArray[i] = id;
                m_peptideMatchPosition.put(id, i);
            }       
            m_rset.getTransientData().setPeptideMatchIds(peptideMatchIdsArray);

            
            int nb = peptideMatchIds.size();
            DPeptideMatch[] peptideMatchArray = new DPeptideMatch[nb];
            m_rset.getTransientData().setPeptideMatches(peptideMatchArray);
            

            int nbPeptideMatch = m_peptideMatchIds.size();
            if (nbPeptideMatch > 0) {

                /**
                 * PeptideMatches
                 */
                SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE_MATCH, nbPeptideMatch, SLICE_SIZE);

                // execute the first slice now
                fetchPeptideMatch(entityManagerMSI, subTask);

                /**
                 * Peptide for each PeptideMatch
                 *
                 */
                // slice the task and get the first one
                subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE, nbPeptideMatch, SLICE_SIZE);

                // execute the first slice now
                fetchPeptide(entityManagerMSI, subTask);

                /**
                 * MS_Query for each PeptideMatch
                 *
                 */
                // slice the task and get the first one
                subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_MSQUERY, nbPeptideMatch, SLICE_SIZE);

                // execute the first slice now
                fetchMsQuery(entityManagerMSI, subTask, m_peptideMatchIds, m_proteinMatch, m_peptideMatchSequenceMatchArrayMap, m_peptideMatchMap);

                /**
                 * Src .dat File for each PeptideMatch (merged data only)
                 *
                 */
                ResultSet.Type rsType = m_rset.getType();
                boolean mergedData = (rsType == ResultSet.Type.USER) || (rsType == ResultSet.Type.DECOY_USER); // Merge or Decoy Merge
                if (mergedData) {
                    // slice the task and get the first one
                    subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SRC_DAT_FILE, nbPeptideMatch, SLICE_SIZE);
                
                    // execute the first slice now
                    fetchSrcDatFile(entityManagerMSI, subTask);
                }
                
            }

            entityManagerMSI.getTransaction().commit();
            
        } catch (Exception e) {
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

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;

        return true;
    }
    
    public boolean fetchPeptidesForProteinRsetMainTask() {

        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();

            ArrayList<DPeptideMatch> peptideMatches;
            DPeptideMatch[] peptideMatchArray;

            // Load Peptide Match for a ProteinMatch ordered by query id and Peptide name
            // (pm.msQuery is fetch because it is declared as lazy and it is needed later)
            TypedQuery peptideMatchQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DPeptideMatch(pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.isDecoy, pm.serializedProperties) FROM PeptideMatch pm,  fr.proline.core.orm.msi.Peptide p, SequenceMatch sm WHERE sm.id.proteinMatchId=:proteinId AND sm.bestPeptideMatchId=pm.id AND pm.peptideId=p.id ORDER BY pm.msQuery.initialId ASC, p.sequence ASC", DPeptideMatch.class);
            peptideMatchQuery.setParameter("proteinId", m_proteinMatch.getId());

            List<DPeptideMatch> resultList = peptideMatchQuery.getResultList();

            peptideMatches = new ArrayList<>(resultList.size());
            peptideMatchArray = new DPeptideMatch[resultList.size()];
            long[] peptideMatchIds = new long[resultList.size()];
            Iterator<DPeptideMatch> it = resultList.iterator();
            int index = 0;
            while (it.hasNext()) {
                DPeptideMatch pm = it.next();
                peptideMatchIds[index] = pm.getId();
                peptideMatchArray[index++] = pm;
                peptideMatches.add(pm);
            }

            m_proteinMatch.setPeptideMatchesId(peptideMatchIds);
            m_proteinMatch.setPeptideMatches(peptideMatchArray);


            // Retrieve Peptide Match Ids and create map of PeptideMatch in the same time
            int nb = peptideMatchArray.length;
            m_peptideMatchIds = new ArrayList<>(nb);
            m_peptideMatchSequenceMatchArrayMap = new HashMap<>();
            for (int i = 0; i < nb; i++) {
                DPeptideMatch pm = peptideMatchArray[i];
                Long pmId = pm.getId();
                m_peptideMatchIds.add(i, pmId);
                
                ArrayList<DPeptideMatch> sequenceMatchArray = m_peptideMatchSequenceMatchArrayMap.get(pmId);
                if (sequenceMatchArray == null) {
                    sequenceMatchArray = new ArrayList<>();
                    m_peptideMatchSequenceMatchArrayMap.put(pmId, sequenceMatchArray);
                }
                sequenceMatchArray.add(pm);
            }


            /**
             * Peptide for each PeptideMatch
             *
             */
            // slice the task and get the first one
            SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE, peptideMatches.size(), SLICE_SIZE);

            // execute the first slice now
            fetchPeptide(entityManagerMSI, subTask);

            /**
             * MS_Query for each PeptideMatch
             *
             */
            // slice the task and get the first one
            subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_MSQUERY, peptideMatches.size(), SLICE_SIZE);

            // execute the first slice now
            fetchMsQuery(entityManagerMSI, subTask, m_peptideMatchIds, m_proteinMatch, m_peptideMatchSequenceMatchArrayMap, m_peptideMatchMap);

            /**
             * Src .dat File for each PeptideMatch (merged data only)
             *
             */
            ResultSet.Type rsType = m_rset.getType();
            boolean mergedData = (rsType == ResultSet.Type.USER) || (rsType == ResultSet.Type.DECOY_USER); // Merge or Decoy Merge
            if (mergedData) {
                // slice the task and get the first one
                subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SRC_DAT_FILE, peptideMatches.size(), SLICE_SIZE);

                // execute the first slice now
                fetchSrcDatFile(entityManagerMSI, subTask);
            }

            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
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

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;

        return true;
    }

    
    public boolean fetchAllRsmMainTask() {
        
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            ArrayList<DPeptideMatch> peptideMatches;
            DPeptideMatch[] peptideMatchArray;
            
            Long rsmId = m_rsm.getId();


            // Load Peptide Match which have a Peptide Instance in the rsm
            // SELECT pm, pm.msQuery FROM fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.Peptide p WHERE pipm.resultSummary.id=:rsmId AND pipm.peptideMatch=pm AND pm.peptideId=p.id ORDER BY pm.msQuery.initialId ASC, p.sequence ASC
            TypedQuery peptideMatchQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DPeptideMatch(pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.isDecoy, pm.serializedProperties) FROM fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.Peptide p WHERE pipm.resultSummary.id=:rsmId AND pipm.id.peptideMatchId=pm.id AND pm.peptideId=p.id ORDER BY pm.msQuery.initialId ASC, p.sequence ASC", DPeptideMatch.class);
            peptideMatchQuery.setParameter("rsmId", rsmId);
            List<DPeptideMatch> resultList = peptideMatchQuery.getResultList();

            peptideMatches = new ArrayList<>(resultList.size());
            peptideMatchArray = new DPeptideMatch[resultList.size()];
            long[] peptideMatchIdsArray = new long[resultList.size()];
            Iterator<DPeptideMatch> it = resultList.iterator();
            int index = 0;
            while (it.hasNext()) {
                DPeptideMatch pm = it.next();
                peptideMatchIdsArray[index] = pm.getId();
                peptideMatchArray[index++] = pm;
                
                peptideMatches.add(pm);
                // resCur[1] : (pm.msQuery is fetch because it is declared as lazy and it is needed later)
            }

            m_rsm.getTransientData().setPeptideMatches(peptideMatchArray);
            m_rsm.getTransientData().setPeptideMatchesId(peptideMatchIdsArray);
            
                
            // Retrieve Peptide Match Ids and create map of PeptideMatch in the same time
            int nb = peptideMatchArray.length;
            m_peptideMatchIds = new ArrayList<>(nb);
            m_peptideMatchMap = new HashMap<>();
            //VDS TODO integrate this loop with previous one (query result) ?
            for (int i = 0; i < nb; i++) {
                DPeptideMatch pm = peptideMatchArray[i];
                Long pmId = pm.getId();
                m_peptideMatchIds.add(i, pmId);
                m_peptideMatchMap.put(pmId, pm);
            }
            
            if (nb > 0) { // check that there is at least one PSM validated
                /**
                 * Peptide for each PeptideMatch
                 *
                 */
                // slice the task and get the first one
                SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE, peptideMatches.size(), SLICE_SIZE);

                // execute the first slice now
                fetchPeptide(entityManagerMSI, subTask);

                /**
                 * MS_Query for each PeptideMatch
                 *
                 */
                // slice the task and get the first one
                subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_MSQUERY, peptideMatches.size(), SLICE_SIZE);

                // execute the first slice now
                fetchMsQuery(entityManagerMSI, subTask, m_peptideMatchIds, m_proteinMatch, m_peptideMatchSequenceMatchArrayMap, m_peptideMatchMap);


                /**
                 * ProteinSet String list for each PeptideMatch
                 *
                 */
                // slice the task and get the first one
                subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PROTEINSET_NAME_LIST, peptideMatches.size(), SLICE_SIZE);

                // execute the first slice now
                fetchProteinSetName(entityManagerMSI, subTask);

                
                /**
                 * Src .dat File for each PeptideMatch (merged data only)
                 *
                 */
                ResultSet.Type rsType = m_rsm.getResultSet().getType();
                boolean mergedData = (rsType == ResultSet.Type.USER) || (rsType == ResultSet.Type.DECOY_USER); // Merge or Decoy Merge
                if (mergedData) {
                    // slice the task and get the first one
                    subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SRC_DAT_FILE, peptideMatches.size(), SLICE_SIZE);

                    // execute the first slice now
                    fetchSrcDatFile(entityManagerMSI, subTask);
                }
                
            }

            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
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

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;

        return true;
    }
    
    private boolean fetchPeptidesForProteinSetRsmMainTask() {

        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            ArrayList<DPeptideMatch> peptideMatches;
            DPeptideMatch[] peptideMatchArray;
            
            Long rsmId = m_rsm.getId();

            DProteinMatch typicalProteinMatch = m_proteinSet.getTypicalProteinMatch();
            m_proteinMatch = typicalProteinMatch;

            // Retrieve peptideSet of a typicalProteinMatch
            DPeptideSet peptideSet = typicalProteinMatch.getPeptideSet(rsmId);
            if (peptideSet == null) {
                TypedQuery<DPeptideSet> peptideSetQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DPeptideSet(ps.id, ps.score, ps.sequenceCount, ps.peptideCount, ps.peptideMatchCount, ps.resultSummaryId) FROM PeptideSet ps, PeptideSetProteinMatchMap ps_to_pm WHERE ps_to_pm.id.proteinMatchId=:proteinMatchId AND ps_to_pm.id.peptideSetId=ps.id AND ps_to_pm.resultSummary.id=:rsmId", DPeptideSet.class);
                peptideSetQuery.setParameter("proteinMatchId", typicalProteinMatch.getId());
                peptideSetQuery.setParameter("rsmId", rsmId);
                peptideSet = peptideSetQuery.getSingleResult();
                typicalProteinMatch.setPeptideSet(rsmId,peptideSet);
            }
            

            
            // Retrieve the list of PeptideInstance, PeptideMatch, Peptide, MSQuery, Spectrum of a PeptideSet
        Query psmQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DPeptideMatch(pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.isDecoy, pm.serializedProperties) FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem ps_to_pi, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm WHERE ps_to_pi.peptideSet.id=:peptideSetId AND ps_to_pi.peptideInstance.id=pi.id AND pipm.resultSummary.id=:rsmId AND pipm.id.peptideMatchId=pm.id AND pipm.id.peptideInstanceId=pi.id ORDER BY pm.score DESC");
            psmQuery.setParameter("peptideSetId", peptideSet.getId());
            psmQuery.setParameter("rsmId", rsmId);
            List<DPeptideMatch> resultList = psmQuery.getResultList();

            peptideMatches = new ArrayList<>(resultList.size());
            peptideMatchArray = new DPeptideMatch[resultList.size()];
            long[] peptideMatchIdsArray = new long[resultList.size()];
            Iterator<DPeptideMatch> it = resultList.iterator();
            int index = 0;
            while (it.hasNext()) {
                DPeptideMatch pm = it.next();
                peptideMatchIdsArray[index] = pm.getId();
                peptideMatchArray[index++] = pm;
                
                peptideMatches.add(pm);
                // resCur[1] : (pm.msQuery is fetch because it is declared as lazy and it is needed later)
            }

            // Retrieve Peptide Match Ids and create map of PeptideMatch in the same time
            int nb = peptideMatchArray.length;
            m_peptideMatchIds = new ArrayList<>(nb);
            m_peptideMatchMap = new HashMap<>();
            //VDS TODO integrate this loop with previous one (query result)
            for (int i = 0; i < nb; i++) {
                DPeptideMatch pm = peptideMatchArray[i];
                Long pmId = pm.getId();
                m_peptideMatchIds.add(i, pmId);
                m_peptideMatchMap.put(pmId, pm);
            }
                        
            m_peptideMatchSequenceMatchArrayMap = new HashMap<>();
            for (int i = 0; i < nb; i++) {
                DPeptideMatch pm = peptideMatchArray[i];
                Long pmId = pm.getId();
                m_peptideMatchIds.add(i, pmId);
                
                ArrayList<DPeptideMatch> sequenceMatchArray = m_peptideMatchSequenceMatchArrayMap.get(pmId);
                if (sequenceMatchArray == null) {
                    sequenceMatchArray = new ArrayList<>();
                    m_peptideMatchSequenceMatchArrayMap.put(pmId, sequenceMatchArray);
                }
                sequenceMatchArray.add(pm);
            }
            
            typicalProteinMatch.setPeptideMatches(peptideMatchArray);
            typicalProteinMatch.setPeptideMatchesId(peptideMatchIdsArray);


            
            if (nb > 0) { // check that there is at least one PSM validated
                /**
                 * Peptide for each PeptideMatch
                 *
                 */
                // slice the task and get the first one
                SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE, peptideMatches.size(), SLICE_SIZE);

                // execute the first slice now
                fetchPeptide(entityManagerMSI, subTask);

                /**
                 * MS_Query for each PeptideMatch
                 *
                 */
                // slice the task and get the first one
                subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_MSQUERY, peptideMatches.size(), SLICE_SIZE);

                // execute the first slice now
                fetchMsQuery(entityManagerMSI, subTask, m_peptideMatchIds, m_proteinMatch, m_peptideMatchSequenceMatchArrayMap, m_peptideMatchMap);


                /**
                 * ProteinSet String list for each PeptideMatch
                 *
                 */
                // slice the task and get the first one
                subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PROTEINSET_NAME_LIST, peptideMatches.size(), SLICE_SIZE);

                // execute the first slice now
                fetchProteinSetName(entityManagerMSI, subTask);

                
                /**
                 * Src .dat File for each PeptideMatch (merged data only)
                 *
                 */
                ResultSet.Type rsType = m_rsm.getResultSet().getType();
                boolean mergedData = (rsType == ResultSet.Type.USER) || (rsType == ResultSet.Type.DECOY_USER); // Merge or Decoy Merge
                if (mergedData) {
                    // slice the task and get the first one
                    subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SRC_DAT_FILE, peptideMatches.size(), SLICE_SIZE);

                    // execute the first slice now
                    fetchSrcDatFile(entityManagerMSI, subTask);
                }
                
            }

            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
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

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;

        return true;
    }
    
    
    private boolean fetchPSMForPeptideInstanceMainTask()  {

        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            ArrayList<DPeptideMatch> peptideMatches;
            DPeptideMatch[] peptideMatchArray;
            
            Long rsmId = m_rsm.getId();


            // Load Peptide Match which have a Peptide Instance in the rsm
            // SELECT pm, pm.msQuery FROM fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.Peptide p WHERE pipm.resultSummary.id=:rsmId AND pipm.peptideMatch=pm AND pm.peptideId=p.id ORDER BY pm.msQuery.initialId ASC, p.sequence ASC
            TypedQuery peptideMatchQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DPeptideMatch(pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.isDecoy, pm.serializedProperties) FROM fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.Peptide p WHERE pipm.resultSummary.id=:rsmId AND pipm.id.peptideMatchId=pm.id AND pm.peptideId=p.id AND pipm.id.peptideInstanceId=:piId ORDER BY pm.msQuery.initialId ASC, p.sequence ASC", DPeptideMatch.class);
            peptideMatchQuery.setParameter("rsmId", rsmId);
            peptideMatchQuery.setParameter("piId", m_peptideInstance.getId());
            
            List<DPeptideMatch> resultList = peptideMatchQuery.getResultList();

            peptideMatches = new ArrayList<>(resultList.size());
            peptideMatchArray = new DPeptideMatch[resultList.size()];
            long[] peptideMatchIdsArray = new long[resultList.size()];
            Iterator<DPeptideMatch> it = resultList.iterator();
            int index = 0;
            while (it.hasNext()) {
                DPeptideMatch pm = it.next();
                peptideMatchIdsArray[index] = pm.getId();
                peptideMatchArray[index++] = pm;
                
                peptideMatches.add(pm);
                // resCur[1] : (pm.msQuery is fetch because it is declared as lazy and it is needed later)
            }

            m_peptideInstance.getTransientData().setPeptideMatches(peptideMatchArray);
            m_peptideInstance.getTransientData().setPeptideMatchesId(peptideMatchIdsArray);
            
                
            // Retrieve Peptide Match Ids and create map of PeptideMatch in the same time
            int nb = peptideMatchArray.length;
            m_peptideMatchIds = new ArrayList<>(nb);
            m_peptideMatchMap = new HashMap<>();
            for (int i = 0; i < nb; i++) {
                DPeptideMatch pm = peptideMatchArray[i];
                Long pmId = pm.getId();
                m_peptideMatchIds.add(i, pmId);
                m_peptideMatchMap.put(pmId, pm);
            }
            
            if (nb > 0) { // check that there is at least one PSM validated
                /**
                 * Peptide for each PeptideMatch
                 *
                 */
                // slice the task and get the first one
                SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE, peptideMatches.size(), SLICE_SIZE);

                // execute the first slice now
                fetchPeptide(entityManagerMSI, subTask);

                /**
                 * MS_Query for each PeptideMatch
                 *
                 */
                // slice the task and get the first one
                subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_MSQUERY, peptideMatches.size(), SLICE_SIZE);

                // execute the first slice now
                fetchMsQuery(entityManagerMSI, subTask, m_peptideMatchIds, m_proteinMatch, m_peptideMatchSequenceMatchArrayMap, m_peptideMatchMap);


                /**
                 * ProteinSet String list for each PeptideMatch
                 *
                 */
                // slice the task and get the first one
                subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PROTEINSET_NAME_LIST, peptideMatches.size(), SLICE_SIZE);

                // execute the first slice now
                fetchProteinSetName(entityManagerMSI, subTask);

                /**
                 * Src .dat File for each PeptideMatch (merged data only)
                 *
                 */
                ResultSet.Type rsType = m_rsm.getResultSet().getType();
                boolean mergedData = (rsType == ResultSet.Type.USER) || (rsType == ResultSet.Type.DECOY_USER); // Merge or Decoy Merge
                if (mergedData) {
                    // slice the task and get the first one
                    subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SRC_DAT_FILE, peptideMatches.size(), SLICE_SIZE);

                    // execute the first slice now
                    fetchSrcDatFile(entityManagerMSI, subTask);
                }
                
            }

            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
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

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;

        return true;
    }
    
    /**
     * Fetch data of a Subtask
     *
     * @return
     */
    private boolean fetchDataSubTask() {
        SubTask subTask = m_subTaskManager.getNextSubTask();
        if (subTask == null) {
            return true; // nothing to do : should not happen
        }

        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            switch (subTask.getSubTaskId()) {
                case SUB_TASK_PEPTIDE_MATCH:
                    fetchPeptideMatch(entityManagerMSI, subTask);
                    break;
                case SUB_TASK_PEPTIDE:
                    fetchPeptide(entityManagerMSI, subTask);
                    break;
                case SUB_TASK_MSQUERY:
                    fetchMsQuery(entityManagerMSI, subTask, m_peptideMatchIds, m_proteinMatch, m_peptideMatchSequenceMatchArrayMap, m_peptideMatchMap);
                    break;
                case SUB_TASK_PROTEINSET_NAME_LIST:
                    fetchProteinSetName(entityManagerMSI, subTask);
                    break;
                case SUB_TASK_SRC_DAT_FILE:
                    fetchSrcDatFile(entityManagerMSI, subTask);
                    break;
            }


            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
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
    
    
    /**
     * Retrieve Peptide Match for a Sub Task
     *
     * @param entityManagerMSI
     */
    private void fetchPeptideMatch(EntityManager entityManagerMSI, SubTask subTask) {

        List sliceOfPeptideMatchIds = subTask.getSubList(m_peptideMatchIds);

        // Load Peptide Matches
        TypedQuery peptideMatchQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DPeptideMatch(pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.isDecoy, pm.serializedProperties) FROM PeptideMatch pm WHERE pm.id IN (:listId)", DPeptideMatch.class);
        peptideMatchQuery.setParameter("listId", sliceOfPeptideMatchIds);
        List<DPeptideMatch> resultList = peptideMatchQuery.getResultList();
        
        
        if (m_peptideMatchMap == null) {
            m_peptideMatchMap = new HashMap<>();
        }
        

        DPeptideMatch[] peptideMatches = m_rset.getTransientData().getPeptideMatches();

        Iterator<DPeptideMatch> it = resultList.iterator();
        while (it.hasNext()) {
            DPeptideMatch peptideMatch = it.next();
            Long id = peptideMatch.getId();
            int position = m_peptideMatchPosition.get(id);
            peptideMatches[position] = peptideMatch;
            m_peptideMatchMap.put(id, peptideMatch);
        }


    }
    
    /**
     * Retrieve Peptide for a Sub Task
     *
     * @param entityManagerMSI
     */
    private void fetchPeptide(EntityManager entityManagerMSI, SubTask subTask) {

        List sliceOfPeptideMatchIds = subTask.getSubList(m_peptideMatchIds);

        
        HashMap<Long, Peptide> peptideMap = new HashMap<>();
        if (m_proteinMatch != null) {
            
            // if the peptide is fetched for a specific proteinMatch, we look for the SequenceMatch in the same time
            long proteinMatchId = m_proteinMatch.getId();

            Query peptideQuery = entityManagerMSI.createQuery("SELECT pm.id, p, sm FROM PeptideMatch pm, fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.SequenceMatch as sm WHERE pm.id IN (:listId) AND pm.peptideId=p.id AND sm.id.proteinMatchId=:proteinMatchId AND sm.id.peptideId=p.id");
            peptideQuery.setParameter("listId", sliceOfPeptideMatchIds);
            peptideQuery.setParameter("proteinMatchId", proteinMatchId);


            ArrayList<DPeptideMatch> sequenceMatchListPrevious = null;
            int indexArray = 0;
            List<Object[]> peptides = peptideQuery.getResultList();
            Iterator<Object[]> it = peptides.iterator();
            while (it.hasNext()) {
                Object[] res = it.next();
                Long peptideMatchId = (Long) res[0];
                Peptide peptide = (Peptide) res[1];
                SequenceMatch sm = (SequenceMatch) res[2];
                //peptide.getTransientData().setSequenceMatch(sm);
                peptide.getTransientData().setPeptideReadablePtmStringLoaded();
                ArrayList<DPeptideMatch> sequenceMatchList = m_peptideMatchSequenceMatchArrayMap.get(peptideMatchId);
                if (sequenceMatchListPrevious != sequenceMatchList) {
                    sequenceMatchListPrevious = sequenceMatchList;
                    indexArray = 0;
                }
                sequenceMatchList.get(indexArray).setPeptide(peptide);
                sequenceMatchList.get(indexArray).setSequenceMatch(sm);
                indexArray++;
                peptideMap.put(peptide.getId(), peptide);
            }

            DatabasePTMSitesTask.fetchPTMDataForPeptides(entityManagerMSI, peptideMap);
            
        } else {

            Query peptideQuery = entityManagerMSI.createQuery("SELECT pm.id, p FROM PeptideMatch pm, fr.proline.core.orm.msi.Peptide p WHERE pm.id IN (:listId) AND pm.peptideId=p.id");
            peptideQuery.setParameter("listId", sliceOfPeptideMatchIds);
            List<Object[]> peptides = peptideQuery.getResultList();
            Iterator<Object[]> it = peptides.iterator();
            while (it.hasNext()) {
                Object[] res = it.next();
                Long peptideMatchId = (Long) res[0];
                Peptide peptide = (Peptide) res[1];
                peptide.getTransientData().setPeptideReadablePtmStringLoaded();
                m_peptideMatchMap.get(peptideMatchId).setPeptide(peptide);
                peptideMap.put(peptide.getId(), peptide);
            }
        }
        
        
  
        
        // Retrieve PeptideReadablePtmString
        Long rsetId = (m_rset != null) ? m_rset.getId() : m_rsm.getResultSet().getId();
        Query ptmStingQuery = entityManagerMSI.createQuery("SELECT p.id, ptmString FROM fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.PeptideReadablePtmString ptmString WHERE p.id IN (:listId) AND ptmString.peptide=p AND ptmString.resultSet.id=:rsetId");
        ptmStingQuery.setParameter("listId", peptideMap.keySet());
        ptmStingQuery.setParameter("rsetId", rsetId);

        List<Object[]> ptmStrings = ptmStingQuery.getResultList();
         Iterator<Object[]> it = ptmStrings.iterator();
        while (it.hasNext()) {
            Object[] res = it.next();
            Long peptideId = (Long) res[0];
            PeptideReadablePtmString ptmString = (PeptideReadablePtmString)  res[1];
            Peptide peptide = peptideMap.get(peptideId);
            peptide.getTransientData().setPeptideReadablePtmString(ptmString);
        }
        
        DatabasePTMSitesTask.fetchPTMDataForPeptides(entityManagerMSI, peptideMap);
       
 
    }
    

    
    /**
     * Retrieve MsQuery for a Sub Task
     *
     * @param entityManagerMSI
     */
    public static void fetchMsQuery(EntityManager entityManagerMSI, SubTask subTask, ArrayList<Long> peptideMatchIds, HashMap<Long, DPeptideMatch> peptideMatchMap) {
        fetchMsQuery(entityManagerMSI, subTask, peptideMatchIds, null, null, peptideMatchMap);
    }
    
    private static void fetchMsQuery(EntityManager entityManagerMSI, SubTask subTask, ArrayList<Long> peptideMatchIds, DProteinMatch proteinMatch, HashMap<Long,  ArrayList<DPeptideMatch>> peptideMatchSequenceMatchArrayMap, HashMap<Long, DPeptideMatch> peptideMatchMap) {


       List sliceOfPeptideMatchIds = subTask.getSubList(peptideMatchIds);

        Query msQueryQuery = entityManagerMSI.createQuery("SELECT pm.id, msq.id, msq.initialId, s.firstTime, s.precursorIntensity, s.title FROM PeptideMatch pm,MsQuery msq, Spectrum s WHERE pm.id IN (:listId) AND pm.msQuery=msq AND msq.spectrum=s");
        msQueryQuery.setParameter("listId", sliceOfPeptideMatchIds);

        List<Object[]> msQueries = msQueryQuery.getResultList();
        Iterator<Object[]> it = msQueries.iterator();
        while (it.hasNext()) {
            Object[] res = it.next();
            Float firstTime = (Float) res[3];
            Float precursorIntensity = (Float) res[4];
            String title = (String) res[5];
            
            DSpectrum spectrum = new DSpectrum();
            spectrum.setFirstTime(firstTime);
            spectrum.setPrecursorIntensity(precursorIntensity);
            spectrum.setTitle(title);
            
            DMsQuery q = new DMsQuery((Long) res[0], (Long) res[1], (Integer) res[2], precursorIntensity);
            q.setDSpectrum(spectrum);
            if (proteinMatch != null) {
                 ArrayList<DPeptideMatch> sequenceMatchArray = peptideMatchSequenceMatchArrayMap.get(q.getPeptideMatchId());
                for (DPeptideMatch peptideMatch : sequenceMatchArray) {
                    peptideMatch.setMsQuery(q);
                    peptideMatch.setRetentionTime(spectrum.getFirstTime());
                }
            } else {
                DPeptideMatch peptideMatch = peptideMatchMap.get(q.getPeptideMatchId());
                peptideMatch.setMsQuery(q);
                peptideMatch.setRetentionTime(spectrum.getFirstTime());
            }
            
            
        }
	        
    }
    
    private void fetchSrcDatFile(EntityManager entityManagerMSI, SubTask subTask) {
        fetchSrcDatFile(entityManagerMSI, subTask, m_peptideMatchIds, m_proteinMatch, m_peptideMatchSequenceMatchArrayMap, m_peptideMatchMap);
    }
    
    public static void fetchSrcDatFile(EntityManager entityManagerMSI, SubTask subTask, ArrayList<Long> peptideMatchIds, DProteinMatch proteinMatch, HashMap<Long,  ArrayList<DPeptideMatch>> peptideMatchSequenceMatchArrayMap, HashMap<Long, DPeptideMatch> peptideMatchMap) {

        List sliceOfPeptideMatchIds = subTask.getSubList(peptideMatchIds);
        
        Query query = entityManagerMSI.createQuery("SELECT pm.id, msiSearch.resultFileName "
                + "FROM PeptideMatch pm, MsQuery msq, MsiSearch msiSearch "
                + "WHERE pm.id IN (:listId) AND pm.msQuery=msq AND msq.msiSearch = msiSearch");
        query.setParameter("listId", sliceOfPeptideMatchIds);
        
        List<Object[]> resultList = query.getResultList();
        Iterator<Object[]> it = resultList.iterator();
        while (it.hasNext()) {
            Object[] res = it.next();
            Long pmId = (Long) res[0];
            String datFile = (String) res[1];
            
            if (proteinMatch != null) {
                 ArrayList<DPeptideMatch> sequenceMatchArray = peptideMatchSequenceMatchArrayMap.get(pmId);
                for (DPeptideMatch peptideMatch : sequenceMatchArray) {
                    peptideMatch.setSourceDatFile(datFile);
                }
            } else {
                DPeptideMatch peptideMatch = peptideMatchMap.get(pmId);
                peptideMatch.setSourceDatFile(datFile);
            }
 
        }
        
    }
 
    /**
     * Retrieve MsQuery for a Sub Task
     *
     * @param entityManagerMSI
     */
    private void fetchProteinSetName(EntityManager entityManagerMSI, SubTask subTask) {


        List sliceOfPeptideMatchIds = subTask.getSubList(m_peptideMatchIds);

        Query proteinSetQuery = entityManagerMSI.createQuery("SELECT typpm.accession, pepm.id FROM fr.proline.core.orm.msi.PeptideMatch pepm, fr.proline.core.orm.msi.PeptideInstance pepi, fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pi_pm, fr.proline.core.orm.msi.ProteinSet prots, fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem ps_pi, fr.proline.core.orm.msi.PeptideSet peps, fr.proline.core.orm.msi.ProteinMatch typpm WHERE pepm.id IN (:listId) AND pi_pm.peptideMatch=pepm AND pi_pm.resultSummary.id=:rsmId AND pi_pm.peptideInstance=pepi AND ps_pi.peptideInstance=pepi AND ps_pi.peptideSet=peps AND peps.proteinSet=prots AND prots.representativeProteinMatchId = typpm.id AND prots.isValidated=true ORDER BY pepm.id ASC, typpm.accession ASC");

        proteinSetQuery.setParameter("listId", sliceOfPeptideMatchIds);
        proteinSetQuery.setParameter("rsmId", m_rsm == null ? -1: m_rsm.getId());
        

        ArrayList<String> proteinSetNameArray = new ArrayList();
        long prevPeptideMatchId = -1;

        List<Object[]> msQueries = proteinSetQuery.getResultList();
        Iterator<Object[]> it = msQueries.iterator();
        while (it.hasNext()) {
            Object[] resCur = it.next();
            String proteinName = (String) resCur[0];
            Long peptideMatchId = (Long) resCur[1];

            if (peptideMatchId != prevPeptideMatchId) {
                if (prevPeptideMatchId != -1) {
                    DPeptideMatch prevPeptideMatch = m_peptideMatchMap.get(prevPeptideMatchId);
                    String[] proteinSetNames = proteinSetNameArray.toArray(new String[proteinSetNameArray.size()]);
                    prevPeptideMatch.setProteinSetStringArray(proteinSetNames);
                    proteinSetNameArray.clear();
                    proteinSetNameArray.add(proteinName);
                } else {
                    proteinSetNameArray.add(proteinName);
                }
            } else {
                proteinSetNameArray.add(proteinName);
            }

            prevPeptideMatchId = peptideMatchId;
        }
        if (prevPeptideMatchId != -1) {
            DPeptideMatch prevPeptideMatch = m_peptideMatchMap.get(prevPeptideMatchId);
            String[] proteinSetNames = proteinSetNameArray.toArray(new String[proteinSetNameArray.size()]);
            prevPeptideMatch.setProteinSetStringArray(proteinSetNames);
        }
        
        Iterator itIds = sliceOfPeptideMatchIds.iterator();
        while (itIds.hasNext()) {
            Long peptideMatchId = (Long) itIds.next();
            DPeptideMatch peptideMatch = m_peptideMatchMap.get(peptideMatchId);
            if (peptideMatch.getProteinSetStringArray() == null) {
                String[] proteinSetNames = new String[0];
                peptideMatch.setProteinSetStringArray(proteinSetNames);
            }
        }
    }
    
    public boolean fetchPSMForMsQueryMainTask() {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();

            ArrayList<DPeptideMatch> peptideMatches;
            DPeptideMatch[] peptideMatchArray;
            List<Long> rsIdList = new ArrayList();
            rsIdList.add(m_rset.getId());
            if (m_rset.getDecoyResultSet() != null){
                rsIdList.add(m_rset.getDecoyResultSet().getId());
            }
            String query = "SELECT new fr.proline.core.orm.msi.dto.DPeptideMatch(pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.isDecoy, pm.serializedProperties) "
                    + "FROM fr.proline.core.orm.msi.PeptideMatch pm "
                    + "WHERE pm.msQuery.id =:msQueryId AND "
                    + "pm.resultSet.id IN (:rsIdList) "
                    + "ORDER BY pm.msQuery.initialId ASC";
            TypedQuery peptideMatchQuery = entityManagerMSI.createQuery(query, DPeptideMatch.class);
            peptideMatchQuery.setParameter("msQueryId", m_msQuery.getId());
            peptideMatchQuery.setParameter("rsIdList", rsIdList );
            
            List<DPeptideMatch> resultList = peptideMatchQuery.getResultList();
            int nbP = resultList.size();
            peptideMatches = new ArrayList<>(nbP);
            peptideMatchArray = new DPeptideMatch[nbP];
            Iterator<DPeptideMatch> it = resultList.iterator();
            int index = 0;
            while (it.hasNext()) {
                DPeptideMatch pm = it.next();
                peptideMatchArray[index++] = pm;                
                peptideMatches.add(pm);
                // resCur[1] : (pm.msQuery is fetch because it is declared as lazy and it is needed later)
            }
            
            
            // Retrieve Peptide Match Ids and create map of PeptideMatch in the same time
            int nb = peptideMatchArray.length;
            m_peptideMatchIds = new ArrayList<>(nb);
            m_peptideMatchMap = new HashMap<>();
            //VDS TODO integrate this with previous loop
            for (int i = 0; i < nb; i++) {
                DPeptideMatch pm = peptideMatchArray[i];
                pm.setValidated(false);
                Long pmId = pm.getId();
                m_peptideMatchIds.add(i, pmId);
                m_peptideMatchMap.put(pmId, pm);
            }
                                        
            
        
            String queryValidated = "SELECT pipmm.id.peptideMatchId , count(*) "
                    + "FROM fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipmm  "
                    + "WHERE pipmm.id.peptideMatchId IN (:listId) AND "
                    + " pipmm.resultSummary.id =:rsmId "
                    + "GROUP BY pipmm.id.peptideMatchId";
            if (m_peptideMatchIds != null && !m_peptideMatchIds.isEmpty() && m_rsm != null) {
                Query q = entityManagerMSI.createQuery(queryValidated);
                q.setParameter("rsmId", m_rsm.getId());
                q.setParameter("listId", m_peptideMatchIds);
                List<Object[]> rso = q.getResultList();
                for (Object[] o : rso) {
                    Long pmId = (Long) o[0];
                    Integer nbPI = ((Long) o[1]).intValue();
                    m_peptideMatchMap.get(pmId).setValidated(nbPI > 0);
                }
            }
            
            
            m_listPeptideMatches.addAll(Arrays.asList(peptideMatchArray));
            m_listPeptideMatches.sort(new Comparator<DPeptideMatch>() {
                @Override
                public int compare(DPeptideMatch o1, DPeptideMatch o2) {
                    return o1.getRank() - o2.getRank();
                }
            });
            
            if (nb > 0) { // check that there is at least one PSM validated
                /**
                 * Peptide for each PeptideMatch
                 *
                 */
                // slice the task and get the first one
                SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE, peptideMatches.size(), SLICE_SIZE);

                // execute the first slice now
                fetchPeptide(entityManagerMSI, subTask);

                /**
                 * MS_Query for each PeptideMatch
                 *
                 */
                // slice the task and get the first one
                subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_MSQUERY, peptideMatches.size(), SLICE_SIZE);

                // execute the first slice now
                fetchMsQuery(entityManagerMSI, subTask, m_peptideMatchIds, m_proteinMatch, m_peptideMatchSequenceMatchArrayMap, m_peptideMatchMap);


                /**
                 * ProteinSet String list for each PeptideMatch
                 *
                 */
                // slice the task and get the first one
                subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PROTEINSET_NAME_LIST, peptideMatches.size(), SLICE_SIZE);

                // execute the first slice now
                fetchProteinSetName(entityManagerMSI, subTask);

            }
            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
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

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;

        return true;
    }


}
