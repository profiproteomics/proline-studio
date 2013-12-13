package fr.proline.studio.dam.tasks;


import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
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
    public static final int SUB_TASK_COUNT_RSET = 4; // <<----- get in sync // SUB_TASK_PROTEINSET_NAME_LIST not used for RSET
    public static final int SUB_TASK_COUNT_RSM = 4; // <<----- get in sync
    
    private long m_projectId = -1;
    private ResultSet m_rset = null;
    private ResultSummary m_rsm = null;
    private DProteinMatch m_proteinMatch = null;
    
    // data kept for sub tasks
    private ArrayList<Long> m_peptideMatchIds = null;
    private HashMap<Long, DPeptideMatch> m_peptideMatchMap = null;
    private HashMap<Long, Integer> m_peptideMatchPosition = null;


    private int m_action;
    
    private final static int LOAD_ALL_RSET   = 0;
    private final static int LOAD_ALL_RSM = 1;
    private final static int LOAD_PEPTIDES_FOR_PROTEIN_RSET = 2;
    
    public DatabaseLoadPeptideMatchTask(AbstractDatabaseCallback callback, long projectId, ResultSet rset) {
        super(callback, SUB_TASK_COUNT_RSET, new TaskInfo("Load Peptide Matches for Search Result "+rset.getId(), TASK_LIST_INFO));
        m_projectId = projectId;
        m_rset = rset;   
        m_action = LOAD_ALL_RSET;
    }
    public DatabaseLoadPeptideMatchTask(AbstractDatabaseCallback callback, long projectId, ResultSet rset, DProteinMatch proteinMatch) {
        super(callback, SUB_TASK_COUNT_RSET, new TaskInfo("Load Peptide Matches for Protein Match " + proteinMatch.getAccession() , TASK_LIST_INFO));
        m_projectId = projectId;
        m_rset = rset;
        m_proteinMatch = proteinMatch;
        m_action = LOAD_PEPTIDES_FOR_PROTEIN_RSET;
    }
    public DatabaseLoadPeptideMatchTask(AbstractDatabaseCallback callback, long projectId, ResultSummary rsm) {
        super(callback, SUB_TASK_COUNT_RSM, new TaskInfo("Load Peptide Matches for Identification Summary "+rsm.getId(), TASK_LIST_INFO));
        m_projectId = projectId;
        m_rsm = rsm;
        m_action = LOAD_ALL_RSM;

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
             }
             return false; // should never be called
        } else {
            // fetch data of SubTasks
            return fetchDataSubTask();
        }
    }

    
    public boolean fetchAllRsetMainTask() {

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            Long rsetId = m_rset.getId();

            TypedQuery<Long> peptideMatchIdQuery = entityManagerMSI.createQuery("SELECT pm.id FROM PeptideMatch pm, Peptide p, MsQuery msq WHERE pm.resultSet.id=:rsetId AND pm.peptideId=p.id AND pm.msQuery=msq ORDER BY msq.initialId ASC, p.sequence ASC", Long.class);
            peptideMatchIdQuery.setParameter("rsetId", rsetId);
            List<Long> peptideMatchIds = peptideMatchIdQuery.getResultList();
            
            
            m_peptideMatchIds = new ArrayList<>(peptideMatchIds);
            long[] peptideMatchIdsArray = new long[m_peptideMatchIds.size()];
            m_peptideMatchPosition = new HashMap<>();
            for (int i = 0; i < peptideMatchIdsArray.length; i++) {
                Long id = m_peptideMatchIds.get(i).longValue();
                peptideMatchIdsArray[i] = id;
                m_peptideMatchPosition.put(id, i);
            }       
            m_rset.getTransientData().setPeptideMatchIds(peptideMatchIdsArray);

            
            int nb = peptideMatchIds.size();
            DPeptideMatch[] peptideMatchArray = new DPeptideMatch[nb];
            m_rset.getTransientData().setPeptideMatches(peptideMatchArray);
            

            
            /**
             * PeptideMatches
             */
            SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE_MATCH, m_peptideMatchIds.size(), SLICE_SIZE);
            
            // execute the first slice now
            fetchPeptideMatch(entityManagerMSI, subTask);
            
            /**
             * Peptide for each PeptideMatch
             *
             */
            // slice the task and get the first one
            subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE, m_peptideMatchIds.size(), SLICE_SIZE);

            // execute the first slice now
            fetchPeptide(entityManagerMSI, subTask);
            
            /**
             * MS_Query for each PeptideMatch
             *
             */
            // slice the task and get the first one
            subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_MSQUERY, m_peptideMatchIds.size(), SLICE_SIZE);

            // execute the first slice now
            fetchMsQuery(entityManagerMSI, subTask);


            entityManagerMSI.getTransaction().commit();
            
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
            m_taskError = new TaskError(e);
            entityManagerMSI.getTransaction().rollback();
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

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            ArrayList<DPeptideMatch> peptideMatches;
            DPeptideMatch[] peptideMatchArray;



            // Load Peptide Match for a ProteinMatch ordered by query id and Peptide name
            // (pm.msQuery is fetch because it is declared as lazy and it is needed later)
            TypedQuery peptideMatchQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DPeptideMatch(pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id) FROM PeptideMatch pm, Peptide p, SequenceMatch sm WHERE sm.id.proteinMatchId=:proteinId AND sm.bestPeptideMatchId=pm AND pm.peptideId=p.id ORDER BY pm.msQuery.initialId ASC, p.sequence ASC", DPeptideMatch.class);
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
            m_peptideMatchMap = new HashMap<>();
            for (int i = 0; i < nb; i++) {
                DPeptideMatch pm = peptideMatchArray[i];
                Long pmId = pm.getId();
                m_peptideMatchIds.add(i, pmId);
                m_peptideMatchMap.put(pmId, pm);
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
            fetchMsQuery(entityManagerMSI, subTask);



            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            entityManagerMSI.getTransaction().rollback();
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
        
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            ArrayList<DPeptideMatch> peptideMatches;
            DPeptideMatch[] peptideMatchArray;
            
            Long rsmId = m_rsm.getId();


            // Load Peptide Match which have a Peptide Instance in the rsm
            // SELECT pm, pm.msQuery FROM fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.Peptide p WHERE pipm.resultSummary.id=:rsmId AND pipm.peptideMatch=pm AND pm.peptideId=p.id ORDER BY pm.msQuery.initialId ASC, p.sequence ASC
            TypedQuery peptideMatchQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DPeptideMatch(pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id) FROM fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.Peptide p WHERE pipm.resultSummary.id=:rsmId AND pipm.id.peptideMatchId=pm.id AND pm.peptideId=p.id ORDER BY pm.msQuery.initialId ASC, p.sequence ASC", DPeptideMatch.class);
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
            for (int i = 0; i < nb; i++) {
                DPeptideMatch pm = peptideMatchArray[i];
                Long pmId = pm.getId();
                m_peptideMatchIds.add(i, pmId);
                m_peptideMatchMap.put(pmId, pm);
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
            fetchMsQuery(entityManagerMSI, subTask);


            /**
             * ProteinSet String list for each PeptideMatch
             *
             */
            // slice the task and get the first one
            subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PROTEINSET_NAME_LIST, peptideMatches.size(), SLICE_SIZE);

            // execute the first slice now
            fetchProteinSetName(entityManagerMSI, subTask);


            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
            m_taskError = new TaskError(e);
            entityManagerMSI.getTransaction().rollback();
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

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
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
                    fetchMsQuery(entityManagerMSI, subTask);
                    break;
                case SUB_TASK_PROTEINSET_NAME_LIST:
                    fetchProteinSetName(entityManagerMSI, subTask);
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
    
    
    /**
     * Retrieve Peptide Match for a Sub Task
     *
     * @param entityManagerMSI
     * @param slice
     */
    private void fetchPeptideMatch(EntityManager entityManagerMSI, SubTask subTask) {

        List sliceOfPeptideMatchIds = subTask.getSubList(m_peptideMatchIds);

        // Load Peptide Matches
        TypedQuery peptideMatchQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DPeptideMatch(pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id) FROM PeptideMatch pm WHERE pm.id IN (:listId)", DPeptideMatch.class);
        peptideMatchQuery.setParameter("listId", sliceOfPeptideMatchIds);
        List<DPeptideMatch> resultList = peptideMatchQuery.getResultList();
        
        
        if (m_peptideMatchMap == null) {
            m_peptideMatchMap = new HashMap<>();
        }
        

        DPeptideMatch[] peptideMatches = m_rset.getTransientData().getPeptideMatches();
        
        //int i = subTask.getStartIndex();
        Iterator<DPeptideMatch> it = resultList.iterator();
        while (it.hasNext()) {
            DPeptideMatch peptideMatch = it.next();
            Long id = peptideMatch.getId();
            int position = m_peptideMatchPosition.get(id);
            peptideMatches[position] = peptideMatch;
            m_peptideMatchMap.put(id, peptideMatch);
            //i++;
        }


    }
    
    /**
     * Retrieve Peptide for a Sub Task
     *
     * @param entityManagerMSI
     * @param slice
     */
    private void fetchPeptide(EntityManager entityManagerMSI, SubTask subTask) {

        List sliceOfPeptideMatchIds = subTask.getSubList(m_peptideMatchIds);

        Query peptideQuery = entityManagerMSI.createQuery("SELECT pm.id, p FROM PeptideMatch pm, fr.proline.core.orm.msi.Peptide p WHERE pm.id IN (:listId) AND pm.peptideId=p.id");
        peptideQuery.setParameter("listId", sliceOfPeptideMatchIds);

        HashMap<Long, Peptide> peptideMap= new HashMap<>();
        
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
        
        // Retrieve PeptideReadablePtmString
        Long rsetId = (m_rset != null) ? m_rset.getId() : m_rsm.getResultSet().getId();
        Query ptmStingQuery = entityManagerMSI.createQuery("SELECT p.id, ptmString FROM fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.PeptideReadablePtmString ptmString WHERE p.id IN (:listId) AND ptmString.peptide=p AND ptmString.resultSet.id=:rsetId");
        ptmStingQuery.setParameter("listId", peptideMap.keySet());
        ptmStingQuery.setParameter("rsetId", rsetId);

        List<Object[]> ptmStrings = ptmStingQuery.getResultList();
        it = ptmStrings.iterator();
        while (it.hasNext()) {
            Object[] res = it.next();
            Long peptideId = (Long) res[0];
            PeptideReadablePtmString ptmString = (PeptideReadablePtmString)  res[1];
            Peptide peptide = peptideMap.get(peptideId);
            peptide.getTransientData().setPeptideReadablePtmString(ptmString);
        }
 
    }
    

    
    /**
     * Retrieve MsQuery for a Sub Task
     *
     * @param entityManagerMSI
     * @param slice
     */
    private void fetchMsQuery(EntityManager entityManagerMSI, SubTask subTask) {


       List sliceOfPeptideMatchIds = subTask.getSubList(m_peptideMatchIds);

        TypedQuery<DMsQuery> msQueryQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DMsQuery(pm.id, msq.id, msq.initialId, s.precursorIntensity) FROM PeptideMatch pm,MsQuery msq, Spectrum s WHERE pm.id IN (:listId) AND pm.msQuery=msq AND msq.spectrum=s", DMsQuery.class);
        msQueryQuery.setParameter("listId", sliceOfPeptideMatchIds);


        List<DMsQuery> msQueries = msQueryQuery.getResultList();
        Iterator<DMsQuery> it = msQueries.iterator();
        while (it.hasNext()) {
            DMsQuery q = it.next();
            DPeptideMatch peptideMatch = m_peptideMatchMap.get(q.getPeptideMatchId());
            peptideMatch.setMsQuery(q);
        }


		
        
    }
    
    
 
    /**
     * Retrieve MsQuery for a Sub Task
     *
     * @param entityManagerMSI
     * @param slice
     */
    private void fetchProteinSetName(EntityManager entityManagerMSI, SubTask subTask) {


        List sliceOfPeptideMatchIds = subTask.getSubList(m_peptideMatchIds);

        Query proteinSetQuery = entityManagerMSI.createQuery("SELECT typpm.accession, pepm.id FROM fr.proline.core.orm.msi.PeptideMatch pepm, fr.proline.core.orm.msi.PeptideInstance pepi, fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pi_pm, fr.proline.core.orm.msi.ProteinSet prots, fr.proline.core.orm.msi.PeptideSetPeptideInstanceItem ps_pi, fr.proline.core.orm.msi.PeptideSet peps, fr.proline.core.orm.msi.ProteinMatch typpm WHERE pepm.id IN (:listId) AND pi_pm.peptideMatch=pepm AND pi_pm.peptideInstance=pepi AND ps_pi.peptideInstance=pepi AND ps_pi.peptideSet=peps AND peps.proteinSet=prots AND prots.typicalProteinMatchId = typpm.id ORDER BY pepm.id ASC, typpm.accession ASC");

        proteinSetQuery.setParameter("listId", sliceOfPeptideMatchIds);


        StringBuilder sb = new StringBuilder();
        DPeptideMatch prevPeptideMatch = null;

        List<Object[]> msQueries = proteinSetQuery.getResultList();
        Iterator<Object[]> it = msQueries.iterator();
        while (it.hasNext()) {
            Object[] resCur = it.next();
            String proteinName = (String) resCur[0];
            Long peptideMatchId = (Long) resCur[1];

            DPeptideMatch peptideMatch = m_peptideMatchMap.get(peptideMatchId);

            if ((prevPeptideMatch != null) && (peptideMatch != prevPeptideMatch)) {
                prevPeptideMatch.setProteinSetStringList(sb.toString());
                sb.setLength(0);
                sb.append(proteinName);
            } else {
                sb.append(", ").append(proteinName);
            }
            prevPeptideMatch = peptideMatch;
        }
        prevPeptideMatch.setProteinSetStringList(sb.toString());
    }

}
