package fr.proline.studio.dam.tasks;


import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * Load Protein Sets and their Typical ProteinMatch of a Result Summary The
 * sameset/subset, spectral count, specific spectral count are loaded too. Only
 * the SLICE_SIZE first data are loaded at once... Sub Tasks are created for
 * remaining data.
 *
 * @author JM235353
 */
public class DatabaseProteinSetsTask extends AbstractDatabaseSlicerTask {

    // used to slice the task in sub tasks
    private static final int SLICE_SIZE = 1000;
    // different possible subtasks
    public static final int SUB_TASK_TYPICAL_PROTEIN = 0;
    public static final int SUB_TASK_SPECTRAL_COUNT = 1;
    public static final int SUB_TASK_SPECIFIC_SPECTRAL_COUNT = 2;
    public static final int SUB_TASK_SAMESET_SUBSET_COUNT = 3;
    public static final int SUB_TASK_COUNT = 4; // <<----- get in sync
    
    
    private int m_action;
    private static final int LOAD_PROTEIN_SET_FOR_RSM = 0;
    private static final int LOAD_PROTEIN_SET_FOR_PEPTIDE_INSTANCE = 1;
    
    private long m_projectId = -1;
    private ResultSummary m_rsm = null;
    private PeptideInstance m_peptideInstance = null;
    
    // data kept for sub tasks
    private ArrayList<Long> m_proteinMatchIds = null;
    private HashMap<Long, DProteinSet> m_proteinSetMap = null;
    private ArrayList<Long> m_proteinSetIds = null;

    public DatabaseProteinSetsTask(AbstractDatabaseCallback callback) {
        super(callback);
       
    }
    
    public void initLoadProteinSets(long projectId, ResultSummary rsm) {
        init(SUB_TASK_COUNT, new TaskInfo("Load Protein Sets of Identification Summary "+rsm.getId(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_rsm = rsm;
        m_action = LOAD_PROTEIN_SET_FOR_RSM;
    }
    
    public void initLoadProteinSetForPeptideInstance(long projectId, PeptideInstance peptideInstance) {        
        init(SUB_TASK_COUNT, new TaskInfo("Load Protein Sets for Peptide Instance "+peptideInstance.getId(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_projectId = projectId;
        m_peptideInstance = peptideInstance;
        m_rsm = peptideInstance.getResultSummary();
        m_action = LOAD_PROTEIN_SET_FOR_PEPTIDE_INSTANCE; 
    }

    
    @Override
    // must be implemented for all AbstractDatabaseSlicerTask 
    public void abortTask() {
        super.abortTask();
        switch (m_action) {
            case LOAD_PROTEIN_SET_FOR_RSM:
                m_rsm.getTransientData().setProteinSetArray(null);
                break;
            case LOAD_PROTEIN_SET_FOR_PEPTIDE_INSTANCE:
                m_peptideInstance.getTransientData().setProteinSetArray(null);
                break;
        }
    }
    
    @Override
    public boolean needToFetch() {
        switch (m_action) {
            case LOAD_PROTEIN_SET_FOR_RSM:
                return (m_rsm.getTransientData().getProteinSetArray() == null);
            case LOAD_PROTEIN_SET_FOR_PEPTIDE_INSTANCE:
                return (m_peptideInstance.getTransientData().getProteinSetArray() == null);
        }
        return false; // should not happen
    }

    @Override
    public boolean fetchData() {

        if (m_action == LOAD_PROTEIN_SET_FOR_RSM) {
            if (needToFetch()) {
                // first data are fetched
                return fetchDataMainTaskForRSM();
            } else {
                // fetch data of SubTasks
                return fetchDataSubTaskFor();
            }
        } else if (m_action == LOAD_PROTEIN_SET_FOR_PEPTIDE_INSTANCE) {
            if (needToFetch()) {
                // first data are fetched
                return fetchDataMainTaskForPeptideInstance();
            } else {
                return fetchDataSubTaskFor();
            }
        } 
        return true; // should not happen
    }

    /**
     * Fetch first data for ProteinSet of a RSM. (all Protein Sets, but only a part of Typical proteins,
     * spectral count, specific spectral count...
     *
     * @return
     */
    private boolean fetchDataMainTaskForRSM() {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            
            entityManagerMSI.getTransaction().begin();
            
            // Load Protein Sets for RSM
            Long rsmId = m_rsm.getId();
            // SELECT ps FROM PeptideSet pepset JOIN pepset.proteinSet as ps WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true ORDER BY pepset.score DESC
            TypedQuery<DProteinSet> proteinSetsQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinSet(ps.id, ps.representativeProteinMatchId ,ps.resultSummary.id) FROM PeptideSet pepset JOIN pepset.proteinSet as ps WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true ORDER BY pepset.score DESC", DProteinSet.class);            
            proteinSetsQuery.setParameter("rsmId", rsmId);
            List<DProteinSet> proteinSets = proteinSetsQuery.getResultList();

            DProteinSet[] proteinSetArray = proteinSets.toArray(new DProteinSet[proteinSets.size()]);
            //Save in TransientData
            m_rsm.getTransientData().setProteinSetArray(proteinSetArray);

            //Load generic ProteinSet Data
            return fetchDataMainTask(entityManagerMSI, proteinSetArray);
            
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
    }
    
    /**
     * Fetch first data. (all Protein Sets, but only a part of Typical proteins,
     * spectral count, specific spectral count...
     *
     * @return
     */
    private boolean fetchDataMainTask(EntityManager entityManagerMSI, DProteinSet[] proteinSetArray) {

        m_proteinSetMap = new HashMap<>();
        for (DProteinSet proteinSetCur : proteinSetArray) {
            m_proteinSetMap.put(proteinSetCur.getId(), proteinSetCur);
        }

        // Retrieve Protein Match Ids &  prepare the list of ProteinSet Ids
        int nbProteinSets = proteinSetArray.length;
        m_proteinMatchIds = new ArrayList<>(nbProteinSets);
        m_proteinSetIds = new ArrayList<>(nbProteinSets);
        for (int i = 0; i < nbProteinSets; i++) {
            m_proteinMatchIds.add(i, proteinSetArray[i].getProteinMatchId());
            m_proteinSetIds.add(i, proteinSetArray[i].getId());
        }

        if (nbProteinSets > 0) {

            /**
             * Typical Protein Match for each Protein Set
             *
             */
            // slice the task and get the first one
            SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_TYPICAL_PROTEIN, m_proteinMatchIds.size(), SLICE_SIZE); // do not really slice : work on all ids

            // execute the first slice now
            typicalProteinMatch(entityManagerMSI, subTask);

            /**
             * Calculate Spectral Count
             *
             */
            // slice the task and get the first one
            subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SPECTRAL_COUNT, m_proteinSetIds.size(), SLICE_SIZE);

            // execute the first slice now
            spectralCount(entityManagerMSI, subTask);

            /**
             * Calculate Specific Spectral Count
             *
             */
            // slice the task and get the first one
            subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SPECIFIC_SPECTRAL_COUNT, m_proteinSetIds.size(), SLICE_SIZE);

            // execute the request now
            specificSpectralCount(entityManagerMSI, subTask);

        }

        /*
         * Calculate SameSet and Subset counts
         */
        if (nbProteinSets > 0) {
            // slice the task and get the first one
            SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SAMESET_SUBSET_COUNT, m_proteinSetIds.size(), SLICE_SIZE);

            // execute the request now
            sameSetAndSubSetCount(entityManagerMSI, m_proteinSetIds, subTask);
        }

        // set priority as low for the possible sub tasks
        m_defaultPriority = Priority.LOW;
        m_currentPriority = Priority.LOW;
        return true;
    }
    
    /**
    * Fetch first data for ProteinSet of a Peptide. (all Protein Sets, but only a part of Typical proteins,
    * spectral count, specific spectral count...
    *
    * @return
    */
    private boolean fetchDataMainTaskForPeptideInstance() {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            // Load Protein Sets
            Long pepInstanceId = m_peptideInstance.getId();
            // SELECT prots FROM fr.proline.core.orm.msi.ProteinSet prots, fr.proline.core.orm.msi.PeptideSet peps, fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem peps_to_pepi WHERE peps.proteinSet=prots AND peps.id=peps_to_pepi.id.peptideSetId AND peps_to_pepi.id.peptideInstanceId=:peptideInstanceId AND prots.isValidated=true ORDER BY prots.score DESC
            TypedQuery proteinSetsQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinSet(prots.id, prots.representativeProteinMatchId ,prots.resultSummary.id) FROM fr.proline.core.orm.msi.ProteinSet prots, fr.proline.core.orm.msi.PeptideSet peps, fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem peps_to_pepi WHERE peps.proteinSet=prots AND peps.id=peps_to_pepi.id.peptideSetId AND peps_to_pepi.id.peptideInstanceId=:peptideInstanceId AND prots.isValidated=true ORDER BY peps.score DESC", DProteinSet.class);
            proteinSetsQuery.setParameter("peptideInstanceId", pepInstanceId);
            List<DProteinSet> proteinSets = proteinSetsQuery.getResultList();

            DProteinSet[] proteinSetArray = proteinSets.toArray(new DProteinSet[proteinSets.size()]);
            // Save in transiant data
            m_peptideInstance.getTransientData().setProteinSetArray(proteinSetArray);
            //get all generic properties
            return fetchDataMainTask(entityManagerMSI, proteinSetArray);
            
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
    }
    
    
    /**
     * Fetch data of a Subtask
     *
     * @return
     */
    private boolean fetchDataSubTaskFor() {
        SubTask slice = m_subTaskManager.getNextSubTask();
        if (slice == null) {
            return true; // nothing to do : should not happen
        }
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        
        try {

            entityManagerMSI.getTransaction().begin();

            switch (slice.getSubTaskId()) {
                case SUB_TASK_TYPICAL_PROTEIN:
                    typicalProteinMatch(entityManagerMSI, slice);
                    break;
                case SUB_TASK_SPECTRAL_COUNT:
                    spectralCount(entityManagerMSI, slice);
                    break;
                case SUB_TASK_SPECIFIC_SPECTRAL_COUNT:
                    specificSpectralCount(entityManagerMSI, slice);
                    break;
                case SUB_TASK_SAMESET_SUBSET_COUNT:
                    sameSetAndSubSetCount(entityManagerMSI, m_proteinSetIds, slice);
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
     * Retrieve Typical Protein Match for a Sub Task
     *
     * @param entityManagerMSI
     * @param slice
     */
    private void typicalProteinMatch(EntityManager entityManagerMSI, SubTask subTask) {

        DProteinSet[] proteinSetArray = null;
        if (m_action == LOAD_PROTEIN_SET_FOR_RSM) {
            proteinSetArray = m_rsm.getTransientData().getProteinSetArray();
            
            
        } else if (m_action == LOAD_PROTEIN_SET_FOR_PEPTIDE_INSTANCE) {

            proteinSetArray = m_peptideInstance.getTransientData().getProteinSetArray();
        }
        
        List sliceOfProteinMatchIds = subTask.getSubList(m_proteinMatchIds);

            
            
        TypedQuery<DProteinMatch> typicalProteinQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinMatch(pm.id, pm.accession, pm.score, pm.peptideCount, pm.resultSet.id, pm.description, pm.serializedProperties, pepset.id, pepset.score, pepset.sequenceCount, pepset.peptideCount, pepset.peptideMatchCount, pepset.resultSummaryId) FROM PeptideSetProteinMatchMap pset_to_pm JOIN pset_to_pm.proteinMatch as pm JOIN pset_to_pm.peptideSet as pepset WHERE pm.id IN (:listId) AND pset_to_pm.resultSummary.id=:rsmId", DProteinMatch.class);
        typicalProteinQuery.setParameter("listId", sliceOfProteinMatchIds);
        typicalProteinQuery.setParameter("rsmId", m_rsm.getId());

        List<DProteinMatch> typicalProteinMatches = typicalProteinQuery.getResultList();
        HashMap<Long, DProteinMatch> typicalProteinMap = new HashMap<>();
        Iterator<DProteinMatch> itTypical = typicalProteinMatches.iterator();
        while (itTypical.hasNext()) {
            DProteinMatch pmCur = itTypical.next();
            typicalProteinMap.put(pmCur.getId(), pmCur);
        }
        for (int i = subTask.getStartIndex(); i <= subTask.getStopIndex(); i++) {
            DProteinSet proteinSetCur = proteinSetArray[i];
            proteinSetCur.setTypicalProteinMatch(typicalProteinMap.get(proteinSetCur.getProteinMatchId()));
        }
    }

 

    /**
     * Retrieve spectral count for a Sub Task
     *
     * @param entityManagerMSI
     * @param subTask
     */
    private void spectralCount(EntityManager entityManagerMSI, SubTask subTask) {

        DProteinSet[] proteinSetArray = null;
        if (m_action == LOAD_PROTEIN_SET_FOR_RSM) {

            proteinSetArray = m_rsm.getTransientData().getProteinSetArray();
        } else if (m_action == LOAD_PROTEIN_SET_FOR_PEPTIDE_INSTANCE) {

            proteinSetArray = m_peptideInstance.getTransientData().getProteinSetArray();
        }
        
        List sliceOfProteinSetIds = subTask.getSubList(m_proteinSetIds);


        HashMap<Long, Integer> spectralCountMap = new HashMap<>();

        // VDS: Change query to go through ProtSet -> PepSet -> PepInst : BCP BCP plus rapide! 
                //select ps.id, ps.representative_protein_match_id, sum(pepI.total_leaves_match_count), sum(pepI.peptide_match_count)
                //from protein_set ps, peptide_set peps, peptide_set_peptide_instance_item pspii, peptide_instance pepI
                //where pspii.peptide_set_id = peps.id
                //and pspii.peptide_instance_id = pepI.id
                //and peps.protein_set_id = ps.id
                //and ps.result_summary_id = 24
                //and pepI.result_summary_id = 24
                //GROUP BY ps.id, ps.representative_protein_match_id
        
        String spectralCountQueryString = "SELECT ps.representativeProteinMatchId, sum(pi.totalLeavesMatchCount), sum(pi.peptideMatchCount) "
                + "FROM ProteinSet ps, PeptideSet pepS, PeptideInstance pi, PeptideSetPeptideInstanceItem ps_to_pi "
                + "WHERE ps.id IN (:proteinSetIds) AND ps_to_pi.id.peptideSetId = pepS.id AND  ps_to_pi.id.peptideInstanceId=pi.id AND pepS.proteinSet.id = ps.id "                
                + "AND ps.resultSummary.id=:rsmId AND ps.resultSummary.id=ps_to_pi.resultSummary.id AND ps.resultSummary.id=pepS.resultSummaryId "
                + "GROUP BY ps.id";        
        Query spectralCountQuery = entityManagerMSI.createQuery(spectralCountQueryString);
        spectralCountQuery.setParameter("proteinSetIds", sliceOfProteinSetIds);
        spectralCountQuery.setParameter("rsmId", m_rsm.getId());
        List<Object[]>  spectralCountRes = spectralCountQuery.getResultList();
        Iterator<Object[]> spectralCountResIt = spectralCountRes.iterator();
        while (spectralCountResIt.hasNext()) {
            Object[] cur = spectralCountResIt.next();
            Long proteinMatchId = (Long) cur[0];
            Integer spectralCount = ((Long) cur[1]).intValue();
            Integer peptideMatchCount = ((Long) cur[2]).intValue();  //OLD VALUE for compatibility because with old rsm, totalLeavesMatchCount<0
            if (spectralCount>0) {
                spectralCountMap.put(proteinMatchId, spectralCount);
            } else {

                ResultSet.Type rsType = m_rsm.getResultSet().getType();
                boolean mergedData = (rsType == ResultSet.Type.USER) || (rsType == ResultSet.Type.DECOY_USER); // Merge or Decoy Merge
                if (!mergedData) {
                    spectralCountMap.put(proteinMatchId, peptideMatchCount);
                } else {
                    spectralCountMap.put(proteinMatchId, -1);
                }
            }
        }

        for (int i = subTask.getStartIndex(); i <= subTask.getStopIndex(); i++) {
            DProteinSet proteinSetCur = proteinSetArray[i];

            long proteinMatchId = proteinSetCur.getProteinMatchId();
            Integer spectralCount = spectralCountMap.get(proteinMatchId);
            if (spectralCount != null) {  // should not happen
                proteinSetCur.setSpectralCount(spectralCount);
            }
        }
        
    }

    /**
     * Retrieve Specific Spectral Count for a Subtask
     *
     * @param entityManagerMSI
     * @param subTask
     */
    private void specificSpectralCount(EntityManager entityManagerMSI, SubTask subTask) {

        DProteinSet[] proteinSetArray = null;
        if (m_action == LOAD_PROTEIN_SET_FOR_RSM) {
            proteinSetArray = m_rsm.getTransientData().getProteinSetArray();
        } else if (m_action == LOAD_PROTEIN_SET_FOR_PEPTIDE_INSTANCE) {
            proteinSetArray = m_peptideInstance.getTransientData().getProteinSetArray();
        }
        
        List sliceOfProteinSetIds = subTask.getSubList(m_proteinSetIds);

        HashMap<Long, Integer> spectralCountMap = new HashMap<>();

        // Prepare Specific Spectral count query
        
        // VDS: Change query to go through ProtSet -> PepSet -> PepInst : BCP BCP plus rapide! 
                //select ps.id, ps.representative_protein_match_id, sum(pepI.total_leaves_match_count), sum(pepI.peptide_match_count)
                //from protein_set ps, peptide_set peps, peptide_set_peptide_instance_item pspii, peptide_instance pepI
                //where pspii.peptide_set_id = peps.id
                //and pspii.peptide_instance_id = pepI.id
                //and peps.protein_set_id = ps.id
                //and ps.result_summary_id = 24
                //and pepI.result_summary_id = 24 
                //AND pi.validatedProteinSetCount=1  << NOT TESTED IN Specific case
                //GROUP BY ps.id, ps.representative_protein_match_id
        
       String specificSpectralCountQueryString = "SELECT ps.representativeProteinMatchId, sum(pi.totalLeavesMatchCount), sum(pi.peptideMatchCount) "
                + "FROM ProteinSet ps, PeptideSet pepS, PeptideInstance pi, PeptideSetPeptideInstanceItem ps_to_pi "
                + "WHERE ps.id IN (:proteinSetIds) AND ps_to_pi.id.peptideSetId = pepS.id AND  ps_to_pi.id.peptideInstanceId=pi.id AND pepS.proteinSet.id = ps.id "                
                + "AND ps.resultSummary.id=:rsmId AND ps.resultSummary.id=ps_to_pi.resultSummary.id AND ps.resultSummary.id=pepS.resultSummaryId "
                + "AND pi.validatedProteinSetCount=1 "
                + "GROUP BY ps.id";        

        Query specificSpectralCountQuery = entityManagerMSI.createQuery(specificSpectralCountQueryString);
        specificSpectralCountQuery.setParameter("proteinSetIds", sliceOfProteinSetIds);
        specificSpectralCountQuery.setParameter("rsmId", m_rsm.getId());

        List<Object[]> specificSpectralCountRes = specificSpectralCountQuery.getResultList();
        Iterator<Object[]> specificSpectralCountResIt = specificSpectralCountRes.iterator();
        while (specificSpectralCountResIt.hasNext()) {
            Object[] cur = specificSpectralCountResIt.next();
            Long proteinMatchId = (Long) cur[0];
            Integer specificSpectralCount = ((Long) cur[1]).intValue();
            Integer peptideMatchCount = ((Long) cur[2]).intValue();  //OLD VALUE for compatibility because with old rsm, totalLeavesMatchCount<0
            if (specificSpectralCount>0) {
                spectralCountMap.put(proteinMatchId, specificSpectralCount);
            } else {
                
                ResultSet.Type rsType = m_rsm.getResultSet().getType();
                boolean mergedData = (rsType == ResultSet.Type.USER) || (rsType == ResultSet.Type.DECOY_USER); // Merge or Decoy Merge
                if (!mergedData) {
                    spectralCountMap.put(proteinMatchId, peptideMatchCount);
                } else {
                    spectralCountMap.put(proteinMatchId, -1);
                }

            }
        }

        if (specificSpectralCountRes.size()<sliceOfProteinSetIds.size()) {
            // some protein Match have a specific spectral count == 0
            // so the previous SQL request return no data
            Iterator<Long> itProteinMatchId = sliceOfProteinSetIds.iterator();
            while (itProteinMatchId.hasNext()) {
                Long proteinMatchId = itProteinMatchId.next();
                if (!spectralCountMap.containsKey(proteinMatchId)) {
                    spectralCountMap.put(proteinMatchId, Integer.valueOf(0));
                }
            }
        }
        

        for (int i = subTask.getStartIndex(); i <= subTask.getStopIndex(); i++) {
            DProteinSet proteinSetCur = proteinSetArray[i];

            Integer specificSpectralCount = spectralCountMap.get(proteinSetCur.getProteinMatchId());
            if (specificSpectralCount != null) {  // should not happen
                proteinSetCur.setSpecificSpectralCount(specificSpectralCount);
            }
        }

    }

    /**
     * Retrieve SameSet/SubSet for a Sub Task
     *
     * @param entityManagerMSI
     * @param proteinSetIds
     * @param subTask
     */
    private void sameSetAndSubSetCount(EntityManager entityManagerMSI, ArrayList<Long> proteinSetIds, SubTask subTask) {

        List sliceOfProteinSetIds = subTask.getSubList(proteinSetIds);


        // SameSet count query
        /**
         * SELECT ps.id, count(pm) 
         * FROM ProteinSet ps, PeptideSet pepset, ProteinMatch pm, PeptideSetProteinMatchMap pepset_to_pm 
         * WHERE ps.id IN (:proteinSetIds) AND pepset.proteinSet=ps AND 
         * pepset_to_pm.id.peptideSetId=pepset.id AND pepset_to_pm.id.proteinMatchId=pm.id 
         * GROUP BY ps
         */
        String sameSetCountQueryString = "SELECT ps.id, count(pm) FROM ProteinSet ps, PeptideSet pepset, ProteinMatch pm, PeptideSetProteinMatchMap pepset_to_pm WHERE ps.id IN (:proteinSetIds) AND pepset.proteinSet=ps AND pepset_to_pm.id.peptideSetId=pepset.id AND pepset_to_pm.id.proteinMatchId=pm.id GROUP BY ps";
        Query sameSetCountQuery = entityManagerMSI.createQuery(sameSetCountQueryString);

        sameSetCountQuery.setParameter("proteinSetIds", sliceOfProteinSetIds);
        //sameSetCountQuery.setParameter("rsmId", rsm.getId());
        List<Object[]> sameSetCountRes = sameSetCountQuery.getResultList();
        Iterator<Object[]> sameSetCountResIt = sameSetCountRes.iterator();
        while (sameSetCountResIt.hasNext()) {
            Object[] cur = sameSetCountResIt.next();
            Long proteinSetId = (Long) cur[0];
            DProteinSet proteinSet = m_proteinSetMap.get(proteinSetId);
            int sameSetCount = ((Long) cur[1]).intValue();
            proteinSet.setSameSetCount(sameSetCount);
        }

        // All proteins in Protein Set count query
        // -> used to know number of proteins in Sub set
        /**
         * SELECT ps, count(pm) FROM ProteinMatch pm, ProteinSetProteinMatchItem
         * ps_to_pm, ProteinSet ps WHERE ps_to_pm.proteinSet.id IN
         * (:proteinSetIds) AND ps_to_pm.proteinSet.id = ps.id AND
         * ps_to_pm.proteinMatch.id=pm.id AND ps_to_pm.resultSummary.id=:rsmId
         * GROUP BY ps
         */
        String allCountQueryString = "SELECT ps.id, count(pm) FROM ProteinMatch pm, ProteinSetProteinMatchItem ps_to_pm, ProteinSet ps WHERE  ps_to_pm.proteinSet.id IN (:proteinSetIds) AND ps_to_pm.proteinSet.id = ps.id AND ps_to_pm.proteinMatch.id=pm.id AND ps_to_pm.resultSummary.id=:rsmId GROUP BY ps";
        Query allCountQuery = entityManagerMSI.createQuery(allCountQueryString);
        allCountQuery.setParameter("proteinSetIds", sliceOfProteinSetIds);
        allCountQuery.setParameter("rsmId", m_rsm.getId());
        List<Object[]> allCountRes = allCountQuery.getResultList();
        Iterator<Object[]> allCountResIt = allCountRes.iterator();
        while (allCountResIt.hasNext()) {
            Object[] cur = allCountResIt.next();
            Long proteinSetId = (Long) cur[0];
            DProteinSet proteinSet = m_proteinSetMap.get(proteinSetId);
            int allCount = ((Long) cur[1]).intValue();
            proteinSet.setSubSetCount(allCount - proteinSet.getSameSetCount());
        }

    }
}
