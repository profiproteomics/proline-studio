package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    private static final int SLICE_SIZE = 100;
    // different possible subtasks
    public static final int SUB_TASK_TYPICAL_PROTEIN = 0;
    public static final int SUB_TASK_SPECTRAL_COUNT = 1;
    public static final int SUB_TASK_SPECIFIC_SPECTRAL_COUNT = 2;
    public static final int SUB_TASK_SAMESET_SUBSET_COUNT = 3;
    public static final int SUB_TASK_COUNT = 4; // <<----- get in sync
    
    
    private int action;
    private static final int LOAD_PROTEIN_SET_FOR_RSM = 0;
    private static final int LOAD_PROTEIN_SET_FOR_PEPTIDE_INSTANCE = 1;
    private static final int LOAD_PROTEIN_SET_NUMBER = 2;
    
    private Integer projectId = null;
    private ResultSummary rsm = null;
    private PeptideInstance peptideInstance = null;
    private Dataset dataset = null;
    private Integer[] m_numberOfProteinSets = null;
    
    
    // data kept for sub tasks
    private ArrayList<Integer> proteinMatchIds = null;
    private HashMap<Integer, ProteinSet> proteinSetMap = null;
    private ArrayList<Integer> proteinSetIds = null;

    public DatabaseProteinSetsTask(AbstractDatabaseCallback callback) {
        super(callback);
       
    }
    
    public void initLoadProteinSets(Integer projectId, ResultSummary rsm) {
        init(SUB_TASK_COUNT, new TaskInfo("Load Data", "Load Protein Sets of a Result Summary", TASK_LIST_INFO));
        this.projectId = projectId;
        this.rsm = rsm;
        action = LOAD_PROTEIN_SET_FOR_RSM;
    }
    
    public void initCountProteinSets(Dataset dataset) {
        init(SUB_TASK_COUNT, new TaskInfo("Count Data", "Count Number of Protein Sets of a Result Summary", TASK_LIST_INFO));
        this.dataset = dataset;
        action = LOAD_PROTEIN_SET_NUMBER;
    }

    public void initLoadProteinSetForPeptideInstance(Integer projectId, PeptideInstance peptideInstance) {
        init(SUB_TASK_COUNT, new TaskInfo("Load Data", "Load Protein Sets for a Peptide Instance", TASK_LIST_INFO));
        this.projectId = projectId;
        this.peptideInstance = peptideInstance;
        this.rsm = peptideInstance.getResultSummary();
        action = LOAD_PROTEIN_SET_FOR_PEPTIDE_INSTANCE; 
    }

    @Override
    public boolean needToFetch() {
        switch (action) {
            case LOAD_PROTEIN_SET_FOR_RSM:
                return (rsm.getTransientData().getProteinSetArray() == null);
            case LOAD_PROTEIN_SET_FOR_PEPTIDE_INSTANCE:
                return (peptideInstance.getTransientData().getProteinSetArray() == null);
            case LOAD_PROTEIN_SET_NUMBER:
                rsm = dataset.getTransientData().getResultSummary();
                return (rsm.getTransientData().getNumberOfProteinSet() == null);
        }
        return false; // should not happen
    }

    @Override
    public boolean fetchData() {

        if (action == LOAD_PROTEIN_SET_FOR_RSM) {
            if (needToFetch()) {
                // first data are fetched
                return fetchDataMainTaskForRSM();
            } else {
                // fetch data of SubTasks
                return fetchDataSubTaskFor();
            }
        } else if (action == LOAD_PROTEIN_SET_FOR_PEPTIDE_INSTANCE) {
            if (needToFetch()) {
                // first data are fetched
                return fetchDataMainTaskForPeptideInstance();
            } else {
                return fetchDataSubTaskFor();
            }
        } else if (action == LOAD_PROTEIN_SET_NUMBER) {
            return fetchDataMainTaskForPSetNumber();
        }
        return true; // should not happen
    }

    /**
     * Fetch first data. (all Protein Sets, but only a part of Typical proteins,
     * spectral count, specific spectral count...
     *
     * @return
     */
    private boolean fetchDataMainTaskForRSM() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            Integer rsmId = rsm.getId();

            //long timeStart = System.currentTimeMillis();

            // Load Protein Sets
            // SELECT ps FROM ProteinSet ps WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true ORDER BY ps.score DESC
            TypedQuery<ProteinSet> proteinSetsQuery = entityManagerMSI.createQuery("SELECT ps FROM ProteinSet ps WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true ORDER BY ps.score DESC", ProteinSet.class);
            proteinSetsQuery.setParameter("rsmId", rsmId);
            List<ProteinSet> proteinSets = proteinSetsQuery.getResultList();

            ProteinSet[] proteinSetArray = proteinSets.toArray(new ProteinSet[proteinSets.size()]);
            rsm.getTransientData().setProteinSetArray(proteinSetArray);

            proteinSetMap = new HashMap<>();
            for (int i = 0; i < proteinSetArray.length; i++) {
                ProteinSet proteinSetCur = proteinSetArray[i];
                proteinSetMap.put(proteinSetCur.getId(), proteinSetCur);
            }



            /*
             * long timeStop = System.currentTimeMillis(); double delta =
             * ((double) (timeStop-timeStart))/1000; System.out.println("Load
             * Protein Sets : "+delta); timeStart = System.currentTimeMillis();
             */


            // Retrieve Protein Match Ids
            proteinMatchIds = new ArrayList<>(proteinSetArray.length);

            int nbProteinSets = proteinSetArray.length;
            for (int i = 0; i < nbProteinSets; i++) {
                proteinMatchIds.add(i, proteinSetArray[i].getProteinMatchId());
                proteinSetArray[i].getResultSummary(); // force fetch of lazy data
            }


            /**
             * Typical Protein Match for each Protein Set
             *
             */
            // slice the task and get the first one
            SubTask subTask = subTaskManager.sliceATaskAndGetFirst(SUB_TASK_TYPICAL_PROTEIN, proteinMatchIds.size(), SLICE_SIZE);

            // execute the first slice now
            typicalProteinMatch(entityManagerMSI, subTask);

            

            
            
            
            /*
             * timeStop = System.currentTimeMillis(); delta = ((double)
             * (timeStop-timeStart))/1000; System.out.println("Load Typical
             * Protein : "+delta); timeStart = System.currentTimeMillis();
             */


            /**
             * Calculate Spectral Count
             *
             */
            // slice the task and get the first one
            subTask = subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SPECTRAL_COUNT, proteinMatchIds.size(), SLICE_SIZE);

            // execute the first slice now
            spectralCount(entityManagerMSI, subTask);




            /*
             * timeStop = System.currentTimeMillis(); delta = ((double)
             * (timeStop-timeStart))/1000; System.out.println("Calculate
             * Spectral Count : "+delta); timeStart = System.currentTimeMillis();
             */


            /**
             * Calculate Specific Spectral Count
             *
             */
            // slice the task and get the first one
            subTask = subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SPECIFIC_SPECTRAL_COUNT, proteinMatchIds.size(), SLICE_SIZE);

            // execute the first slice now
            specificSpectralCount(entityManagerMSI, subTask);



            /*
             * timeStop = System.currentTimeMillis(); delta = ((double)
             * (timeStop-timeStart))/1000; System.out.println("Calculate
             * Specific Spectral Count : "+delta); timeStart = System.currentTimeMillis();
             */


            /*
             * Calculate SameSet and Subset counts
             */
            // prepare the list of ProteinSet Ids
            proteinSetIds = new ArrayList<>(proteinSetArray.length);

            int nb = proteinSetArray.length;
            for (int i = 0; i < nb; i++) {
                proteinSetIds.add(i, proteinSetArray[i].getId());
            }

            // slice the task and get the first one
            subTask = subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SAMESET_SUBSET_COUNT, proteinSetIds.size(), SLICE_SIZE);

            // execute the first slice now
            sameSetAndSubSetCount(entityManagerMSI, proteinSetIds, subTask);


            /*
             * timeStop = System.currentTimeMillis(); delta = ((double)
             * (timeStop-timeStart))/1000; System.out.println("Calculate SameSet
             * and SubSet : "+delta); timeStart = System.currentTimeMillis();
             */

            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }

        // set priority as low for the possible sub tasks
        defaultPriority = Priority.LOW;
        currentPriority = Priority.LOW;

        return true;
    }

    private boolean fetchDataMainTaskForPSetNumber() {
        
        
        projectId = dataset.getProject().getId();
        
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            Integer rsmId = rsm.getId();

            // Count Protein Sets
            TypedQuery<Long> countProteinSetsQuery = entityManagerMSI.createQuery("SELECT count(ps) FROM ProteinSet ps WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true", Long.class);
            countProteinSetsQuery.setParameter("rsmId", rsmId);
            Long proteinSetNumber = countProteinSetsQuery.getSingleResult();

            rsm.getTransientData().setNumberOfProteinSet(Integer.valueOf(proteinSetNumber.intValue()));
            
            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }


        return true;
    }
    
    
    private boolean fetchDataMainTaskForPeptideInstance() {
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            Integer pepInstanceId = peptideInstance.getId();

            // Load Protein Sets
            // SELECT prots FROM fr.proline.core.orm.msi.ProteinSet prots, fr.proline.core.orm.msi.PeptideSet peps, fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem peps_to_pepi WHERE peps.proteinSet=prots AND peps.id=peps_to_pepi.id.peptideSetId AND peps_to_pepi.id.peptideInstanceId=:peptideInstanceId AND prots.isValidated=true ORDER BY prots.score DESC
            TypedQuery proteinSetsQuery = entityManagerMSI.createQuery("SELECT prots FROM fr.proline.core.orm.msi.ProteinSet prots, fr.proline.core.orm.msi.PeptideSet peps, fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem peps_to_pepi WHERE peps.proteinSet=prots AND peps.id=peps_to_pepi.id.peptideSetId AND peps_to_pepi.id.peptideInstanceId=:peptideInstanceId AND prots.isValidated=true ORDER BY prots.score DESC", ProteinSet.class);
            proteinSetsQuery.setParameter("peptideInstanceId", pepInstanceId);
            List<ProteinSet> proteinSets = proteinSetsQuery.getResultList();
            
            ProteinSet[] proteinSetArray = proteinSets.toArray(new ProteinSet[proteinSets.size()]);
            peptideInstance.getTransientData().setProteinSetArray(proteinSetArray);
            
            

            proteinSetMap = new HashMap<>();
            for (int i = 0; i < proteinSetArray.length; i++) {
                ProteinSet proteinSetCur = proteinSetArray[i];
                proteinSetMap.put(proteinSetCur.getId(), proteinSetCur);
            }

            // Retrieve Protein Match Ids
            proteinMatchIds = new ArrayList<>(proteinSetArray.length);

            int nbProteinSets = proteinSetArray.length;
            for (int i = 0; i < nbProteinSets; i++) {
                proteinMatchIds.add(i, proteinSetArray[i].getProteinMatchId());
            }

            /**
             * Typical Protein Match for each Protein Set
             *
             */
            // slice the task and get the first one
            SubTask subTask = subTaskManager.sliceATaskAndGetFirst(SUB_TASK_TYPICAL_PROTEIN, proteinMatchIds.size(), SLICE_SIZE); // do not really slice : work on all ids

            // execute the first slice now
            typicalProteinMatch(entityManagerMSI, subTask);

            

            



            /**
             * Calculate Spectral Count
             *
             */
            // slice the task and get the first one
            subTask = subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SPECTRAL_COUNT, proteinMatchIds.size(), SLICE_SIZE);

            // execute the first slice now
            spectralCount(entityManagerMSI, subTask);


            /**
             * Calculate Specific Spectral Count
             *
             */
            // slice the task and get the first one
            subTask = subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SPECIFIC_SPECTRAL_COUNT, proteinMatchIds.size(), SLICE_SIZE);

            // execute the request now
            specificSpectralCount(entityManagerMSI, subTask);



            /*
             * Calculate SameSet and Subset counts
             */
            // prepare the list of ProteinSet Ids
            proteinSetIds = new ArrayList<>(proteinSetArray.length);

            int nb = proteinSetArray.length;
            for (int i = 0; i < nb; i++) {
                proteinSetIds.add(i, proteinSetArray[i].getId());
            }

            // slice the task and get the first one
            subTask = subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SAMESET_SUBSET_COUNT, proteinSetIds.size(), SLICE_SIZE);

            // execute the request now
            sameSetAndSubSetCount(entityManagerMSI, proteinSetIds, subTask);


            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }

        // set priority as low for the possible sub tasks
        defaultPriority = Priority.LOW;
        currentPriority = Priority.LOW;

        return true;
    }
    
    
    
    /**
     * Fetch data of a Subtask
     *
     * @return
     */
    private boolean fetchDataSubTaskFor() {
        SubTask slice = subTaskManager.getNextSubTask();
        if (slice == null) {
            return true; // nothing to do : should not happen
        }
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();
        
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
                    sameSetAndSubSetCount(entityManagerMSI, proteinSetIds, slice);
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

    /**
     * Retrieve Typical Protein Match for a Sub Task
     *
     * @param entityManagerMSI
     * @param slice
     */
    private void typicalProteinMatch(EntityManager entityManagerMSI, SubTask subTask) {

        ProteinSet[] proteinSetArray = null;
        if (action == LOAD_PROTEIN_SET_FOR_RSM) {
            proteinSetArray = rsm.getTransientData().getProteinSetArray();
        } else if (action == LOAD_PROTEIN_SET_FOR_PEPTIDE_INSTANCE) {
            proteinSetArray = peptideInstance.getTransientData().getProteinSetArray();
        }
        
        List sliceOfProteinMatchIds = subTask.getSubList(proteinMatchIds);

        Query typicalProteinQuery = entityManagerMSI.createQuery("SELECT pm, pepset FROM ProteinMatch pm, PeptideSet pepset, PeptideSetProteinMatchMap pset_to_pm  WHERE pm.id IN (:listId) AND pset_to_pm.id.proteinMatchId=pm.id AND pset_to_pm.id.peptideSetId=pepset.id AND pset_to_pm.resultSummary.id=:rsmId");
        typicalProteinQuery.setParameter("listId", sliceOfProteinMatchIds);
        typicalProteinQuery.setParameter("rsmId", rsm.getId());

        List<Object[]> typicalProteinMatches = typicalProteinQuery.getResultList();
        HashMap<Integer, ProteinMatch> typicalProteinMap = new HashMap<Integer, ProteinMatch>();
        Iterator<Object[]> itTypical = typicalProteinMatches.iterator();
        while (itTypical.hasNext()) {
            Object[] resCur = itTypical.next();
            ProteinMatch pmCur = (ProteinMatch) resCur[0];
            PeptideSet peptideSet = (PeptideSet) resCur[1];
            pmCur.getTransientData().setPeptideSet(rsm.getId(), peptideSet);
            typicalProteinMap.put(pmCur.getId(), pmCur);
        }
        for (int i = subTask.getStartIndex(); i <= subTask.getStopIndex(); i++) {
            ProteinSet proteinSetCur = proteinSetArray[i];
            proteinSetCur.getTransientData().setTypicalProteinMatch(typicalProteinMap.get(proteinSetCur.getProteinMatchId()));
        }
    }
    
    

    
    

    /**
     * Retrieve spectral count for a Sub Task
     *
     * @param entityManagerMSI
     * @param subTask
     */
    private void spectralCount(EntityManager entityManagerMSI, SubTask subTask) {

        ProteinSet[] proteinSetArray = null;
        if (action == LOAD_PROTEIN_SET_FOR_RSM) {
            proteinSetArray = rsm.getTransientData().getProteinSetArray();
        } else if (action == LOAD_PROTEIN_SET_FOR_PEPTIDE_INSTANCE) {
            proteinSetArray = peptideInstance.getTransientData().getProteinSetArray();
        }
        
        List sliceOfProteinMatchIds = subTask.getSubList(proteinMatchIds);


        HashMap<Integer, Integer> spectralCountMap = new HashMap<>();

        // SELECT ps_to_pm.id.proteinMatchId, count(pi_to_pm)
        // FROM PeptideInstance pi, PeptideSetPeptideInstanceItem ps_to_pi, PeptideSetProteinMatchMap ps_to_pm, PeptideInstancePeptideMatchMap pi_to_pm
        // WHERE ps_to_pm.id.proteinMatchId IN (:proteinMatchIds) AND ps_to_pm.resultSummary.id=:rsmId AND ps_to_pm.id.peptideSetId=ps_to_pi.id.peptideSetId AND ps_to_pm.resultSummary.id=ps_to_pi.resultSummary.id AND ps_to_pi.id.peptideInstanceId=pi.id AND pi_to_pm.id.peptideInstanceId=pi.id
        // GROUP BY ps_to_pm.id.proteinMatchId
        String spectralCountQueryString = "SELECT ps_to_pm.id.proteinMatchId, count(pi_to_pm) FROM PeptideInstance pi, PeptideSetPeptideInstanceItem ps_to_pi, PeptideSetProteinMatchMap ps_to_pm, PeptideInstancePeptideMatchMap pi_to_pm WHERE ps_to_pm.id.proteinMatchId IN (:proteinMatchIds) AND ps_to_pm.resultSummary.id=:rsmId AND ps_to_pm.id.peptideSetId=ps_to_pi.id.peptideSetId AND ps_to_pm.resultSummary.id=ps_to_pi.resultSummary.id AND ps_to_pi.id.peptideInstanceId=pi.id AND pi_to_pm.id.peptideInstanceId=pi.id GROUP BY ps_to_pm.id.proteinMatchId";

        Query spectralCountQuery = entityManagerMSI.createQuery(spectralCountQueryString);
        spectralCountQuery.setParameter("proteinMatchIds", sliceOfProteinMatchIds);
        spectralCountQuery.setParameter("rsmId", rsm.getId());

        List<Object[]> spectralCountRes = spectralCountQuery.getResultList();
        Iterator<Object[]> spectralCountResIt = spectralCountRes.iterator();
        while (spectralCountResIt.hasNext()) {
            Object[] cur = spectralCountResIt.next();
            Integer proteinMatchId = (Integer) cur[0];
            Integer spectralCount = ((Long) cur[1]).intValue();
            spectralCountMap.put(proteinMatchId, spectralCount);
        }

        for (int i = subTask.getStartIndex(); i <= subTask.getStopIndex(); i++) {
            ProteinSet proteinSetCur = proteinSetArray[i];
            ProteinSet.TransientData proteinSetData = proteinSetCur.getTransientData();

            Integer spectralCount = spectralCountMap.get(proteinSetCur.getProteinMatchId());
            if (spectralCount != null) {  // should not happen
                proteinSetData.setSpectralCount(spectralCount);
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

        ProteinSet[] proteinSetArray = null;
        if (action == LOAD_PROTEIN_SET_FOR_RSM) {
            proteinSetArray = rsm.getTransientData().getProteinSetArray();
        } else if (action == LOAD_PROTEIN_SET_FOR_PEPTIDE_INSTANCE) {
            proteinSetArray = peptideInstance.getTransientData().getProteinSetArray();
        }
        
        List sliceOfProteinMatchIds = subTask.getSubList(proteinMatchIds);


        HashMap<Integer, Integer> spectralCountMap = new HashMap<Integer, Integer>();

        // Prepare Specific Spectral count query
        spectralCountMap.clear();
        // SELECT ps_to_pm.id.proteinMatchId, count(pi_to_pm)
        // FROM PeptideInstance pi, PeptideSetPeptideInstanceItem ps_to_pi, PeptideSetProteinMatchMap ps_to_pm, PeptideInstancePeptideMatchMap pi_to_pm
        // WHERE ps_to_pm.id.proteinMatchId IN (:proteinMatchIds) AND ps_to_pm.resultSummary.id=:rsmId AND ps_to_pm.id.peptideSetId=ps_to_pi.id.peptideSetId AND ps_to_pm.resultSummary.id=ps_to_pi.resultSummary.id AND ps_to_pi.id.peptideInstanceId=pi.id AND pi.proteinSetCount=1 AND pi_to_pm.id.peptideInstanceId=pi.id
        // GROUP BY ps_to_pm.id.proteinMatchId
        String specificSpectralCountQueryString = "SELECT ps_to_pm.id.proteinMatchId, count(pi_to_pm) FROM PeptideInstance pi, PeptideSetPeptideInstanceItem ps_to_pi, PeptideSetProteinMatchMap ps_to_pm, PeptideInstancePeptideMatchMap pi_to_pm WHERE ps_to_pm.id.proteinMatchId IN (:proteinMatchIds) AND ps_to_pm.resultSummary.id=:rsmId AND ps_to_pm.id.peptideSetId=ps_to_pi.id.peptideSetId AND ps_to_pm.resultSummary.id=ps_to_pi.resultSummary.id AND ps_to_pi.id.peptideInstanceId=pi.id AND pi.proteinSetCount=1 AND pi_to_pm.id.peptideInstanceId=pi.id GROUP BY ps_to_pm.id.proteinMatchId";

        Query specificSpectralCountQuery = entityManagerMSI.createQuery(specificSpectralCountQueryString);
        specificSpectralCountQuery.setParameter("proteinMatchIds", sliceOfProteinMatchIds);
        specificSpectralCountQuery.setParameter("rsmId", rsm.getId());

        List<Object[]> specificSpectralCountRes = specificSpectralCountQuery.getResultList();
        Iterator<Object[]> specificSpectralCountResIt = specificSpectralCountRes.iterator();
        while (specificSpectralCountResIt.hasNext()) {
            Object[] cur = specificSpectralCountResIt.next();
            Integer proteinMatchId = (Integer) cur[0];
            Integer specificSpectralCount = ((Long) cur[1]).intValue();
            spectralCountMap.put(proteinMatchId, specificSpectralCount);
        }

        if (specificSpectralCountRes.size()<sliceOfProteinMatchIds.size()) {
            // some protein Match have a specific spectral count == 0
            // so the previous SQL request return no data
            Iterator<Integer> itProteinMatchId = sliceOfProteinMatchIds.iterator();
            while (itProteinMatchId.hasNext()) {
                Integer proteinMatchId = itProteinMatchId.next();
                if (!spectralCountMap.containsKey(proteinMatchId)) {
                    spectralCountMap.put(proteinMatchId, Integer.valueOf(0));
                }
            }
        }
        

        for (int i = subTask.getStartIndex(); i <= subTask.getStopIndex(); i++) {
            ProteinSet proteinSetCur = proteinSetArray[i];
            ProteinSet.TransientData proteinSetData = proteinSetCur.getTransientData();

            Integer specificSpectralCount = spectralCountMap.get(proteinSetCur.getProteinMatchId());
            if (specificSpectralCount != null) {  // should not happen
                proteinSetData.setSpecificSpectralCount(specificSpectralCount);
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
    private void sameSetAndSubSetCount(EntityManager entityManagerMSI, ArrayList<Integer> proteinSetIds, SubTask subTask) {

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
            Integer proteinSetId = (Integer) cur[0];
            ProteinSet proteinSet = proteinSetMap.get(proteinSetId);
            int sameSetCount = ((Long) cur[1]).intValue();
            proteinSet.getTransientData().setSameSetCount(sameSetCount);
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
        allCountQuery.setParameter("rsmId", rsm.getId());
        List<Object[]> allCountRes = allCountQuery.getResultList();
        Iterator<Object[]> allCountResIt = allCountRes.iterator();
        while (allCountResIt.hasNext()) {
            Object[] cur = allCountResIt.next();
            Integer proteinSetId = (Integer) cur[0];
            ProteinSet proteinSet = proteinSetMap.get(proteinSetId);
            int allCount = ((Long) cur[1]).intValue();
            ProteinSet.TransientData data = proteinSet.getTransientData();
            data.setSubSetCount(allCount - data.getSameSetCount());
        }

    }
}
