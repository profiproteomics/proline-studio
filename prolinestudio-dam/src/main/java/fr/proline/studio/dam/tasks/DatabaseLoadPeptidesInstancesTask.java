package fr.proline.studio.dam.tasks;



import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.SequenceMatch;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideSet;
import fr.proline.core.orm.msi.dto.DSpectrum;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * Load Peptides corresponding to a ProteinMatch
 *
 * @author JM235353
 */
public class DatabaseLoadPeptidesInstancesTask extends AbstractDatabaseSlicerTask {

    private final int m_action;
    
    private final static int LOAD_PEPTIDE_INSTANCE_FOR_PROTEIN_MATCH   = 0;
    private final static int LOAD_PEPTIDE_INSTANCES_FOR_RSM   = 1;
    
        // used to slice the task in sub tasks
    private static final int SLICE_SIZE = 1000;
    
    public static final int SUB_TASK_PROTEINSET_NAME_LIST = 0;
    public static final int SUB_TASK_MSQUERY = 1;
    public static final int SUB_TASK_SRC_DAT_FILE = 2;
    public static final int SUB_TASK_COUNT = 3; // <<----- get in sync
    
    
    private long m_projectId;
    private DProteinMatch m_proteinMatch = null;
    private ArrayList<DProteinMatch> m_proteinMatchArray = null;
    private ArrayList<ResultSummary> m_rsmList = null;
    
    private ResultSummary m_rsm = null;

    // data kept for sub tasks
    private ArrayList<Long> m_peptideMatchIds = null;
    private HashMap<Long, DPeptideMatch> m_peptideMatchMap = null;
    
    public DatabaseLoadPeptidesInstancesTask(AbstractDatabaseCallback callback, long projectId, DProteinMatch proteinMatch, ArrayList<ResultSummary> rsmList) {
        super(callback, SUB_TASK_COUNT, new TaskInfo("Load Peptide Sets for Protein Match "+proteinMatch.getAccession(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_projectId = projectId;
        m_proteinMatch = proteinMatch;
        m_proteinMatchArray = null;
        m_rsmList = rsmList;
        m_action = LOAD_PEPTIDE_INSTANCE_FOR_PROTEIN_MATCH;
        
    }
    
    public DatabaseLoadPeptidesInstancesTask(AbstractDatabaseCallback callback, long projectId, ArrayList<DProteinMatch> proteinMatchArray, ArrayList<ResultSummary> rsmList) {
        super(callback, SUB_TASK_COUNT, new TaskInfo("Load Peptide Sets for multiple Protein Matches", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        m_projectId = projectId;
        m_proteinMatch = null;
        m_proteinMatchArray = proteinMatchArray;
        m_rsmList = rsmList;
        m_action = LOAD_PEPTIDE_INSTANCE_FOR_PROTEIN_MATCH;
    }

    public DatabaseLoadPeptidesInstancesTask(AbstractDatabaseCallback callback, long projectId, ResultSummary rsm) {
        super(callback, SUB_TASK_COUNT, new TaskInfo("Load Peptides for Identification Summary "+rsm.getId(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_rsm = rsm;
        m_action = LOAD_PEPTIDE_INSTANCES_FOR_RSM;
    }

    
    
    @Override
    public boolean needToFetch() {
        switch(m_action) {
            case LOAD_PEPTIDE_INSTANCE_FOR_PROTEIN_MATCH: {
                int size = m_rsmList.size();
                for (int i = 0; i < size; i++) {
                    ResultSummary rsm = m_rsmList.get(i);
                    DProteinMatch pm = (m_proteinMatchArray != null) ? m_proteinMatchArray.get(i) : m_proteinMatch;
                    if (needToFetch(pm, rsm)) {
                        return true;
                    }
                }
                return false;
            }
            case LOAD_PEPTIDE_INSTANCES_FOR_RSM: {
                return (m_rsm.getTransientData().getPeptideInstanceArray() == null);
            }
                
                
        }

        return false; // should not be called
    }
    
    private boolean needToFetch(DProteinMatch proteinMatch, ResultSummary rsm) {
        if (proteinMatch == null) {
            return false;
        }
        DPeptideSet peptideSet = proteinMatch.getPeptideSet(rsm.getId());
        if (peptideSet == null) {
            return true;
        }
        return ( peptideSet.getPeptideInstances() == null);
    }
    

    @Override
    public boolean fetchData() {
        if (needToFetch()) {
            switch (m_action) {
                case LOAD_PEPTIDE_INSTANCE_FOR_PROTEIN_MATCH: {
                    return fetchDataForProteinMatch();
                }
                case LOAD_PEPTIDE_INSTANCES_FOR_RSM: {
                    return fetchDataForRsm();
                }
                
            }
            return false; // should not happen
        } else {
            // fetch data of SubTasks
            return fetchDataSubTask();
        }
    }
    
        /**
     * Fetch data of a Subtask
     *
     * @return true on task execution success
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
                case SUB_TASK_PROTEINSET_NAME_LIST:
                    fetchProteinSetName(entityManagerMSI, subTask);
                    break;
                case SUB_TASK_MSQUERY:
                    DatabaseLoadPeptideMatchTask.fetchMsQuery(entityManagerMSI, subTask, m_peptideMatchIds, m_peptideMatchMap);
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

    private boolean fetchDataForRsm() {
        
        HashMap<Long, Peptide> peptideMap = new HashMap<>();
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();

        try {
            
            entityManagerMSI.getTransaction().begin();

            ArrayList<PeptideInstance> peptideInstanceList = new ArrayList<>();
            // SELECT pi, pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, p, pm.cdPrettyRank, pm.sdPrettyRank, sp.firstTime 
            // FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.MsQuery ms,
           // fr.proline.core.orm.msi.Spectrum sp  
            // WHERE pi.resultSummary.id=:rsmId AND pi.bestPeptideMatchId=pm.id AND pm.peptideId=p.id AND pm.msQuery=ms AND ms.spectrum=sp ORDER BY pm.score DESC
            Query peptideInstancesQuery = entityManagerMSI.createQuery("SELECT pi, pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, p, pm.cdPrettyRank, pm.sdPrettyRank, sp.firstTime, pm.serializedProperties FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.MsQuery ms, fr.proline.core.orm.msi.Spectrum sp  WHERE pi.resultSummary.id=:rsmId AND pi.bestPeptideMatchId=pm.id AND pm.peptideId=p.id AND pm.msQuery=ms AND ms.spectrum=sp ORDER BY pm.score DESC");
            peptideInstancesQuery.setParameter("rsmId", m_rsm.getId());
            
            List l = peptideInstancesQuery.getResultList();
            m_peptideMatchIds = new ArrayList<>(l.size());
            m_peptideMatchMap = new HashMap<>();
            Iterator<Object[]> itPeptidesQuery = l.iterator();
            while (itPeptidesQuery.hasNext()) {
                Object[] resCur = itPeptidesQuery.next();
                PeptideInstance pi = (PeptideInstance) resCur[0];
                
                Long pmId = (Long) resCur[1];
                Integer pmRank = (Integer) resCur[2];
                Integer pmCharge = (Integer) resCur[3];
                Float pmDeltaMoz = (Float) resCur[4];
                Double pmExperimentalMoz = (Double) resCur[5];
                Integer pmMissedCleavage = (Integer) resCur[6];
                Float pmScore = (Float) resCur[7];
                Long pmResultSetId = (Long) resCur[8];
                Integer pmCdPrettyRank = (Integer) resCur[10];
                Integer pmSdPrettyRank = (Integer) resCur[11];
                Float firstTime = (Float) resCur[12];
                String pmSerializedProp = (String) resCur[13];
                DPeptideMatch pm = new DPeptideMatch(pmId, pmRank, pmCharge, pmDeltaMoz, pmExperimentalMoz, pmMissedCleavage, pmScore, pmResultSetId, pmCdPrettyRank, pmSdPrettyRank, pmSerializedProp);
                pm.setRetentionTime(firstTime);
                m_peptideMatchIds.add(pmId);
                m_peptideMatchMap.put(pmId, pm);
                
                Peptide p = (Peptide) resCur[9];
                p.getTransientData().setPeptideReadablePtmStringLoaded();
                peptideMap.put(p.getId(), p);

                pi.getTransientData().setBestPeptideMatch(pm);
                pm.setPeptide(p);

                peptideInstanceList.add(pi);
            }

            int nbPeptides = peptideInstanceList.size();
            PeptideInstance[] peptideInstances = peptideInstanceList.toArray(new PeptideInstance[nbPeptides]);
            m_rsm.getTransientData().setPeptideInstanceArray(peptideInstances);
            
            DatabasePTMSitesTask.fetchReadablePTMData(entityManagerMSI, m_rsm.getResultSet().getId(), peptideMap);
            DatabasePTMSitesTask.fetchPTMDataForPeptides(entityManagerMSI, peptideMap);
             
            // slice the task and get the first one and execute the first slice now
            SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst( SUB_TASK_PROTEINSET_NAME_LIST, nbPeptides, SLICE_SIZE );
            fetchProteinSetName(entityManagerMSI, subTask);
            
            // slice the task and get the first one and execute the first slice
            subTask = m_subTaskManager.sliceATaskAndGetFirst( SUB_TASK_MSQUERY, nbPeptides, SLICE_SIZE );
            DatabaseLoadPeptideMatchTask.fetchMsQuery(entityManagerMSI, subTask, m_peptideMatchIds, m_peptideMatchMap); 
            
            ResultSet.Type rsType = m_rsm.getResultSet().getType();
            boolean mergedData = (rsType == ResultSet.Type.USER) || (rsType == ResultSet.Type.DECOY_USER); // Merge or Decoy Merge
            if (mergedData) {
                // slice the task and get the first one and execute the first slice now
                subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SRC_DAT_FILE, m_peptideMatchMap.size(), SLICE_SIZE);
                DatabaseLoadPeptideMatchTask.fetchSrcDatFile(entityManagerMSI, subTask, m_peptideMatchIds, null, null, m_peptideMatchMap);
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

        return true;
    }
    

    /**
     * Retrieve MsQuery for a Sub Task
     *
     * @param entityManagerMSI : the entity manager to use for database queries
     * @param subTask : the subtask that must be completed
     */
    private void fetchProteinSetName(EntityManager entityManagerMSI, SubTask subTask) {

        List sliceOfPeptideMatchIds = subTask.getSubList(m_peptideMatchIds);

        Query proteinSetQuery = entityManagerMSI.createQuery("SELECT typpm.accession, pepm.id FROM fr.proline.core.orm.msi.PeptideMatch pepm, fr.proline.core.orm.msi.PeptideInstance pepi, fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pi_pm, fr.proline.core.orm.msi.ProteinSet prots, fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem ps_pi, fr.proline.core.orm.msi.PeptideSet peps, fr.proline.core.orm.msi.ProteinMatch typpm WHERE pepm.id IN (:listId) AND pi_pm.peptideMatch=pepm AND pi_pm.resultSummary.id=:rsmId AND pi_pm.peptideInstance=pepi AND ps_pi.peptideInstance=pepi AND ps_pi.peptideSet=peps AND peps.proteinSet=prots AND prots.representativeProteinMatchId = typpm.id AND prots.isValidated=true ORDER BY pepm.id ASC, typpm.accession ASC");

        proteinSetQuery.setParameter("listId", sliceOfPeptideMatchIds);
        proteinSetQuery.setParameter("rsmId", m_rsm == null ? -1 : m_rsm.getId());

        ArrayList<String> proteinSetNameArray = new ArrayList<>();
        long prevPeptideMatchId = -1;

        List<Object[]> resultList = proteinSetQuery.getResultList();
        Iterator<Object[]> it = resultList.iterator();
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
    
    private boolean fetchDataForProteinMatch() {

        HashMap<Long, Peptide> peptideMap = new HashMap<>();
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();

        try {

            entityManagerMSI.getTransaction().begin();

            int size = m_rsmList.size();
            for (int i = 0; i < size; i++) {
                ResultSummary rsm = m_rsmList.get(i);
                DProteinMatch pm = (m_proteinMatchArray != null) ? m_proteinMatchArray.get(i) : m_proteinMatch;
                if (!needToFetch(pm, rsm)) {
                    continue;
                }

                fetchPeptideDataForProteinMatch(entityManagerMSI, rsm, pm, peptideMap);
            }
            
            DatabasePTMSitesTask.fetchPTMDataForPeptides(entityManagerMSI, peptideMap);
            
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
    
    
    public static void fetchPeptideDataForProteinMatch(EntityManager entityManagerMSI, ResultSummary rsm, DProteinMatch proteinMatch, HashMap<Long, Peptide> peptideMap) {

        // Retrieve peptideSet of a proteinMatch
        DPeptideSet peptideSet = proteinMatch.getPeptideSet(rsm.getId());
        if (peptideSet == null) {
            TypedQuery<DPeptideSet> peptideSetQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DPeptideSet(ps.id, ps.score, ps.sequenceCount, ps.peptideCount, ps.peptideMatchCount, ps.resultSummaryId) FROM PeptideSet ps, PeptideSetProteinMatchMap ps_to_pm WHERE ps_to_pm.id.proteinMatchId=:proteinMatchId AND ps_to_pm.id.peptideSetId=ps.id AND ps_to_pm.resultSummary.id=:rsmId", DPeptideSet.class);
            peptideSetQuery.setParameter("proteinMatchId", proteinMatch.getId());
            peptideSetQuery.setParameter("rsmId", rsm.getId());
            peptideSet = peptideSetQuery.getSingleResult();
            proteinMatch.setPeptideSet(rsm.getId(), peptideSet);
            
        }

        //JPM.TODO : speed up the peptidesQuery

        // Retrieve the list of PeptideInstance, PeptideMatch, Peptide, MSQuery, Spectrum of a PeptideSet
        Query peptidesQuery = entityManagerMSI.createQuery("SELECT pi, pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, p, sm, ms.id, ms.initialId, pm.cdPrettyRank, pm.sdPrettyRank, sp.firstTime, sp.precursorIntensity, sp.title, pm.serializedProperties FROM fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem ps_to_pi, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.SequenceMatch as sm, fr.proline.core.orm.msi.MsQuery ms, fr.proline.core.orm.msi.Spectrum sp WHERE ps_to_pi.peptideSet.id=:peptideSetId AND ps_to_pi.peptideInstance.id=pi.id AND pi.bestPeptideMatchId=pm.id AND pm.peptideId=p.id AND sm.id.proteinMatchId=:proteinMatchId AND sm.id.peptideId=p.id AND pm.msQuery=ms AND ms.spectrum=sp ORDER BY pm.score DESC");

        peptidesQuery.setParameter("peptideSetId", peptideSet.getId());
        peptidesQuery.setParameter("proteinMatchId", proteinMatch.getId());

        HashMap<Long, Peptide> peptideMapForPtm = new  HashMap<>();
        
        ArrayList<DPeptideInstance> peptideInstanceList = new ArrayList<>();
        List l = peptidesQuery.getResultList(); 
        Iterator<Object[]> itPeptidesQuery = l.iterator();
        while (itPeptidesQuery.hasNext()) {
            Object[] resCur = itPeptidesQuery.next();
            PeptideInstance pi = (PeptideInstance) resCur[0];
            DPeptideInstance dpi = new DPeptideInstance(pi.getId(), pi.getPeptide().getId(), pi.getValidatedProteinSetCount(), pi.getElutionTime());
            
            Long pmId = (Long) resCur[1];
            Integer pmRank = (Integer) resCur[2];
            Integer pmCharge = (Integer) resCur[3];
            Float pmDeltaMoz = (Float) resCur[4];
            Double pmExperimentalMoz = (Double) resCur[5];
            Integer pmMissedCleavage = (Integer) resCur[6];
            Float pmScore = (Float) resCur[7];
            Long pmResultSetId = (Long) resCur[8];
            Integer pmCdPrettyRank = (Integer) resCur[13];
            Integer pmSdPrettyRank = (Integer) resCur[14];
            Float firstTime = (Float) resCur[15];
            Float precursorIntensity = (Float) resCur[16];
            String title = (String) resCur[17];
            String pmSerializedProp = (String) resCur[18];
            
            DSpectrum spectrum = new DSpectrum();
            spectrum.setFirstTime(firstTime);
            spectrum.setPrecursorIntensity(precursorIntensity);
            spectrum.setTitle(title);
            //JPM.TODO ?
            DPeptideMatch pm = new DPeptideMatch(pmId, pmRank, pmCharge, pmDeltaMoz, pmExperimentalMoz, pmMissedCleavage, pmScore, pmResultSetId, pmCdPrettyRank, pmSdPrettyRank, pmSerializedProp);
            pm.setRetentionTime(firstTime);
            
            Peptide p = (Peptide) resCur[9];
            peptideMapForPtm.put(p.getId(), p);

            SequenceMatch sm = (SequenceMatch) resCur[10];
            Long msqId = (Long) resCur[11];
            Integer msqInitialId = (Integer) resCur[12];

            DMsQuery msq = new DMsQuery(pmId, msqId, msqInitialId, precursorIntensity);
            msq.setDSpectrum(spectrum);

            dpi.setBestPeptideMatch(pm);

            pm.setSequenceMatch(sm);
            p.getTransientData().setPeptideReadablePtmStringLoaded();

            pm.setPeptide(p);
            pm.setMsQuery(msq);

            peptideInstanceList.add(dpi);
        }

        DatabasePTMSitesTask.fetchReadablePTMData(entityManagerMSI, rsm.getResultSet().getId(), peptideMapForPtm);

        int nbPeptides = peptideInstanceList.size();
        DPeptideInstance[] peptideInstances = peptideInstanceList.toArray(new DPeptideInstance[nbPeptides]);
        peptideSet.setPeptideInstances(peptideInstances);
        
        for (int i = 0; i < nbPeptides; i++) {
            peptideMap.put(peptideInstances[i].getPeptideId(), (peptideInstances[i].getBestPeptideMatch()).getPeptide());
        }

        // Retrieve the list of Protein Sets of Peptides
        // typical ProteinMatch is loaded in the same time, to avoid lazy fetch 
        Query proteinGroupsQuery = entityManagerMSI.createQuery("SELECT ps.id, p, pm.id, pm.accession, pm.score, pm.peptideCount, pm.description, pm.serializedProperties FROM fr.proline.core.orm.msi.ProteinMatch pm, fr.proline.core.orm.msi.ProteinSet ps, fr.proline.core.orm.msi.SequenceMatch sm, fr.proline.core.orm.msi.Peptide p WHERE ps.representativeProteinMatchId=pm.id AND pm.id=sm.id.proteinMatchId AND p.id=sm.id.peptideId AND p.id IN (:peptideIds) AND sm.resultSetId=:rsetId AND ps.resultSummary.id=:rsmId AND ps.isValidated=true");
        proteinGroupsQuery.setParameter("rsmId", rsm.getId());
        proteinGroupsQuery.setParameter("rsetId", rsm.getResultSet().getId());
        proteinGroupsQuery.setParameter("peptideIds", peptideMap.keySet());
        l = proteinGroupsQuery.getResultList();
        Iterator<Object[]> itProteinGroupsQuery = l.iterator();
        while (itProteinGroupsQuery.hasNext()) {
            Object[] resCur = itProteinGroupsQuery.next();
            Long proteinSetId = (Long) resCur[0];
            Peptide p = (Peptide) resCur[1];
            
            Long proteinMatchId = (Long) resCur[2];
            String accession = (String) resCur[3];
            Float score = (Float) resCur[4];
            Integer peptideCount = (Integer) resCur[5];
            String description = (String) resCur[6];
            String serializedProperties = (String) resCur[7];

            DProteinMatch pm = new DProteinMatch(proteinMatchId, accession, score, peptideCount, rsm.getResultSet().getId(), description, serializedProperties);
            DProteinSet proteinSet = new DProteinSet(proteinSetId, proteinMatchId, rsm.getId());
            proteinSet.setTypicalProteinMatch(pm);

            ArrayList<DProteinSet> proteinSetArray = p.getTransientData().getProteinSetArray();
            if (proteinSetArray == null) {
                proteinSetArray = new ArrayList<>();
                p.getTransientData().setProteinSetArray(proteinSetArray);
            }
            proteinSetArray.add(proteinSet);
        }
    }
}
