package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;


/**
 * Task used to load Peptide Matches
 * @author JM235353
 */
public class DatabaseLoadPeptideMatchTask extends AbstractDatabaseSlicerTask {
    
    // used to slice the task in sub tasks
    private static final int SLICE_SIZE = 100;
    // different possible subtasks
    public static final int SUB_TASK_PEPTIDE = 0;
    public static final int SUB_TASK_MSQUERY = 1;
    public static final int SUB_TASK_SPECTRUM = 2;
    public static final int SUB_TASK_PROTEINSET_NAME_LIST = 3;
    public static final int SUB_TASK_COUNT_RSET = 3; // <<----- get in sync // SUB_TASK_PROTEINSET_NAME_LIST not used for RSET
    public static final int SUB_TASK_COUNT_RSM = 4; // <<----- get in sync
    
    private long m_projectId = -1;
    private ResultSet m_rset = null;
    private ResultSummary m_rsm = null;
    
    // data kept for sub tasks
    private ArrayList<Long> m_peptideMatchIds = null;
    private HashMap<Long, PeptideMatch> m_peptideMatchMap = null;
    
    private boolean m_loadForRset;

    public DatabaseLoadPeptideMatchTask(AbstractDatabaseCallback callback, long projectId, ResultSet rset) {
        super(callback, SUB_TASK_COUNT_RSET, new TaskInfo("Load Peptide Matches for Search Result "+rset.getId(), TASK_LIST_INFO));
        m_projectId = projectId;
        m_rset = rset;   
        m_loadForRset = true;
    }
    public DatabaseLoadPeptideMatchTask(AbstractDatabaseCallback callback, long projectId, ResultSummary rsm) {
        super(callback, SUB_TASK_COUNT_RSM, new TaskInfo("Load Peptide Matches for Identification Summary "+rsm.getId(), TASK_LIST_INFO));
        m_projectId = projectId;
        m_rsm = rsm;
        m_loadForRset = false;

    }

    @Override
    public boolean needToFetch() {
        if (m_loadForRset) {
            return (m_rset.getTransientData().getPeptideMatches() == null);
        } else {
            return (m_rsm.getTransientData().getPeptideMatches() == null);
        }
    }
    
    @Override
    public boolean fetchData() {
        if (needToFetch()) {
            // first data are fetched
            return fetchDataMainTask();
        } else {
            // fetch data of SubTasks
            return fetchDataSubTask();
        }
    }

    public boolean fetchDataMainTask() {
        
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            ArrayList<PeptideMatch> peptideMatches;
            PeptideMatch[] peptideMatchArray;
            
            if (m_loadForRset) {

                Long rsetId = m_rset.getId();

                // Load Peptide Match order by query id and Peptide name
                // (pm.msQuery is fetch because it is declared as lazy and it is needed later)
                // SELECT pm, pm.msQuery FROM PeptideMatch pm, Peptide p WHERE pm.resultSet.id=:rsetId AND pm.peptideId=p.id ORDER BY pm.msQuery.initialId ASC, p.sequence ASC
                Query peptideMatchQuery = entityManagerMSI.createQuery("SELECT pm, pm.msQuery FROM PeptideMatch pm, Peptide p WHERE pm.resultSet.id=:rsetId AND pm.peptideId=p.id ORDER BY pm.msQuery.initialId ASC, p.sequence ASC");
                peptideMatchQuery.setParameter("rsetId", rsetId);
                //List<PeptideMatch> peptideMatches = peptideMatchQuery.getResultList();
                List<Object[]> resultList = peptideMatchQuery.getResultList();

                peptideMatches = new ArrayList<>(resultList.size());
                peptideMatchArray = new PeptideMatch[resultList.size()];
                Iterator<Object[]> it = resultList.iterator();
                int index = 0;
                while (it.hasNext()) {
                    Object[] resCur = it.next();
                    PeptideMatch pm = (PeptideMatch) resCur[0];
                    peptideMatchArray[index++] = pm;
                    peptideMatches.add(pm);
                    // resCur[1] : (pm.msQuery is fetch because it is declared as lazy and it is needed later)
                }

                m_rset.getTransientData().setPeptideMatches(peptideMatchArray);

            } else {
                Long rsmId = m_rsm.getId();


                // Load Peptide Match which have a Peptide Instance in the rsm
                // SELECT pm, pm.msQuery FROM fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.Peptide p WHERE pipm.resultSummary.id=:rsmId AND pipm.peptideMatch=pm AND pm.peptideId=p.id ORDER BY pm.msQuery.initialId ASC, p.sequence ASC
                Query peptideMatchQuery = entityManagerMSI.createQuery("SELECT pm, pm.msQuery FROM fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.Peptide p WHERE pipm.resultSummary.id=:rsmId AND pipm.id.peptideMatchId=pm.id AND pm.peptideId=p.id ORDER BY pm.msQuery.initialId ASC, p.sequence ASC");
                peptideMatchQuery.setParameter("rsmId", rsmId);
                List<Object[]> resultList = peptideMatchQuery.getResultList();

                peptideMatches = new ArrayList<>(resultList.size());
                peptideMatchArray = new PeptideMatch[resultList.size()];
                Iterator<Object[]> it = resultList.iterator();
                int index = 0;
                while (it.hasNext()) {
                    Object[] resCur = it.next();
                    PeptideMatch pm = (PeptideMatch) resCur[0];
                    peptideMatchArray[index++] = pm;
                    peptideMatches.add(pm);
                    // resCur[1] : (pm.msQuery is fetch because it is declared as lazy and it is needed later)
                }

                m_rsm.getTransientData().setPeptideMatches(peptideMatchArray);
            }
            
            // Retrieve Peptide Match Ids and create map of PeptideMatch in the same time
            int nb = peptideMatchArray.length;
            m_peptideMatchIds = new ArrayList<>(nb);
            m_peptideMatchMap = new HashMap<>();
            for (int i = 0; i < nb; i++) {
                PeptideMatch pm = peptideMatchArray[i];
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
             * Spectrum for each PeptideMatch
             *
             */
            // slice the task and get the first one
            subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SPECTRUM, peptideMatches.size(), SLICE_SIZE);

            // execute the first slice now
            fetchSpectrum(entityManagerMSI, subTask);           
            

            /**
             * ProteinSet String list for each PeptideMatch
             *
             */
            if (!m_loadForRset) {
                // slice the task and get the first one
                subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PROTEINSET_NAME_LIST, peptideMatches.size(), SLICE_SIZE);

                // execute the first slice now
                fetchProteinSetName(entityManagerMSI, subTask);
            }

            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
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
        SubTask slice = m_subTaskManager.getNextSubTask();
        if (slice == null) {
            return true; // nothing to do : should not happen
        }

        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            switch (slice.getSubTaskId()) {
                case SUB_TASK_PEPTIDE:
                    fetchPeptide(entityManagerMSI, slice);
                    break;
                case SUB_TASK_MSQUERY:
                    fetchMsQuery(entityManagerMSI, slice);
                    break;
                case SUB_TASK_SPECTRUM:
                    fetchSpectrum(entityManagerMSI, slice);
                    break;
                case SUB_TASK_PROTEINSET_NAME_LIST:
                    fetchProteinSetName(entityManagerMSI, slice);
                    break;
            }


            entityManagerMSI.getTransaction().commit();
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        } finally {
            entityManagerMSI.close();
        }

        return true;
    }
    
    
    /**
     * Retrieve Peptide for a Sub Task
     *
     * @param entityManagerMSI
     * @param slice
     */
    private void fetchPeptide(EntityManager entityManagerMSI, SubTask subTask) {

        List sliceOfPeptideMatchIds = subTask.getSubList(m_peptideMatchIds);

        Query peptideQuery = entityManagerMSI.createQuery("SELECT pm.id, p FROM PeptideMatch pm,fr.proline.core.orm.msi.Peptide p WHERE pm.id IN (:listId) AND pm.peptideId=p.id");
        peptideQuery.setParameter("listId", sliceOfPeptideMatchIds);

 
        
        int i = subTask.getStartIndex();
        List<Object[]> peptides = peptideQuery.getResultList();
        Iterator<Object[]> it = peptides.iterator();
        while (it.hasNext()) {
            Object[] res = it.next();
            Long peptideMatchId = (Long) res[0];
            Peptide peptide = (Peptide) res[1];
            m_peptideMatchMap.get(peptideMatchId).getTransientData().setPeptide(peptide);
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

        Query msQueryQuery = entityManagerMSI.createQuery("SELECT pm.id, msq FROM PeptideMatch pm,MsQuery msq WHERE pm.id IN (:listId) AND pm.msQuery=msq");
        msQueryQuery.setParameter("listId", sliceOfPeptideMatchIds);


        List<Object[]> msQueries = msQueryQuery.getResultList();
        Iterator<Object[]> it = msQueries.iterator();
        while (it.hasNext()) {
            Object[] resCur = it.next();
            Long peptideMatchId = (Long) resCur[0];
            MsQuery q = (MsQuery) resCur[1];
            PeptideMatch peptideMatch = m_peptideMatchMap.get(peptideMatchId);
            peptideMatch.getTransientData().setIsMsQuerySet(true);
            peptideMatch.setMsQuery(q);
        }
    }
    
    private void fetchSpectrum(EntityManager entityManagerMSI, SubTask subTask) {
        
        List sliceOfPeptideMatchIds = subTask.getSubList(m_peptideMatchIds);

        Query msQueryQuery = entityManagerMSI.createQuery("SELECT pm.id, s FROM PeptideMatch pm,MsQuery msq, Spectrum s WHERE pm.id IN (:listId) AND pm.msQuery=msq AND msq.spectrum.id=s.id");
        msQueryQuery.setParameter("listId", sliceOfPeptideMatchIds);

        int i = subTask.getStartIndex();
        List<Object[]> msQueries = msQueryQuery.getResultList();
        Iterator<Object[]> it = msQueries.iterator();
        while (it.hasNext()) {
            Object[] resCur = it.next();
            Long peptideMatchId = (Long) resCur[0];
            Spectrum spectrum = (Spectrum) resCur[1];
            PeptideMatch peptideMatch = m_peptideMatchMap.get(peptideMatchId);
            MsQuery q = peptideMatch.getMsQuery();
            q.setTransientIsSpectrumSet(true);
            q.setSpectrum(spectrum);
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
        PeptideMatch prevPeptideMatch = null;

        List<Object[]> msQueries = proteinSetQuery.getResultList();
        Iterator<Object[]> it = msQueries.iterator();
        while (it.hasNext()) {
            Object[] resCur = it.next();
            String proteinName = (String) resCur[0];
            Long peptideMatchId = (Long) resCur[1];

            PeptideMatch peptideMatch = m_peptideMatchMap.get(peptideMatchId);

            if ((prevPeptideMatch != null) && (peptideMatch != prevPeptideMatch)) {
                prevPeptideMatch.getTransientData().setProteinSetStringList(sb.toString());
                sb.setLength(0);
                sb.append(proteinName);
            } else {
                sb.append(", ").append(proteinName);
            }
            prevPeptideMatch = peptideMatch;
        }
        prevPeptideMatch.getTransientData().setProteinSetStringList(sb.toString());
    }

}
