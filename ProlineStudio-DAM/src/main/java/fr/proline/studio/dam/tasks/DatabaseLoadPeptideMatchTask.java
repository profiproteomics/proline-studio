package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
/*import java.sql.Connection;
import java.sql.PreparedStatement; JDBC TEST*/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
    //public static final int SUB_TASK_SPECTRUM = 3;
    public static final int SUB_TASK_PROTEINSET_NAME_LIST = 3;
    public static final int SUB_TASK_COUNT_RSET = 4; // <<----- get in sync // SUB_TASK_PROTEINSET_NAME_LIST not used for RSET
    public static final int SUB_TASK_COUNT_RSM = 4; // <<----- get in sync
    
    private long m_projectId = -1;
    private ResultSet m_rset = null;
    private ResultSummary m_rsm = null;
    private ProteinMatch m_proteinMatch = null;
    
    // data kept for sub tasks
    private ArrayList<Long> m_peptideMatchIds = null;
    private HashMap<Long, PeptideMatch> m_peptideMatchMap = null;


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
    public DatabaseLoadPeptideMatchTask(AbstractDatabaseCallback callback, long projectId, ResultSet rset, ProteinMatch proteinMatch) {
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
                break;
            case LOAD_ALL_RSM:
                m_rsm.getTransientData().setPeptideMatches(null);
                break;
            case LOAD_PEPTIDES_FOR_PROTEIN_RSET:
                m_proteinMatch.getTransientData().setPeptideMatches(null);
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
                return (m_proteinMatch.getTransientData().getPeptideMatches() == null);
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

            /*ArrayList<PeptideMatch> peptideMatches;
            PeptideMatch[] peptideMatchArray;
            */
  
            Long rsetId = m_rset.getId();

            TypedQuery<Long> peptideMatchIdQuery = entityManagerMSI.createQuery("SELECT pm.id FROM PeptideMatch pm, Peptide p, MsQuery msq WHERE pm.resultSet.id=:rsetId AND pm.peptideId=p.id AND pm.msQuery=msq ORDER BY msq.initialId ASC, p.sequence ASC", Long.class);
            peptideMatchIdQuery.setParameter("rsetId", rsetId);
            List<Long> peptideMatchIds = peptideMatchIdQuery.getResultList();
            
            
            m_peptideMatchIds = new ArrayList<>(peptideMatchIds);
            long[] peptideMatchIdsArray = new long[m_peptideMatchIds.size()];
            for (int i = 0; i < peptideMatchIdsArray.length; i++) {
                peptideMatchIdsArray[i] = m_peptideMatchIds.get(i).longValue();
            }       
            m_rset.getTransientData().setPeptideMatchIds(peptideMatchIdsArray);
            
            
            int nb = peptideMatchIds.size();
            PeptideMatch[] peptideMatchArray = new PeptideMatch[nb];
            m_rset.getTransientData().setPeptideMatches(peptideMatchArray);
            
            
            // Load Peptide Match order by query id and Peptide name
            // (pm.msQuery is fetch because it is declared as lazy and it is needed later)
            // SELECT pm, pm.msQuery FROM PeptideMatch pm, Peptide p WHERE pm.resultSet.id=:rsetId AND pm.peptideId=p.id ORDER BY pm.msQuery.initialId ASC, p.sequence ASC
            /*Query peptideMatchQuery = entityManagerMSI.createQuery("SELECT pm, pm.msQuery FROM PeptideMatch pm, Peptide p WHERE pm.resultSet.id=:rsetId AND pm.peptideId=p.id ORDER BY pm.msQuery.initialId ASC, p.sequence ASC");
            peptideMatchQuery.setParameter("rsetId", rsetId);
            //List<PeptideMatch> peptideMatches = peptideMatchQuery.getResultList();
            List<Object[]> resultList = peptideMatchQuery.getResultList();*/

            /*peptideMatches = new ArrayList<>(resultList.size());
            peptideMatchArray = new PeptideMatch[resultList.size()];
            Iterator<Object[]> it = resultList.iterator();
            int index = 0;
            while (it.hasNext()) {
                Object[] resCur = it.next();
                PeptideMatch pm = (PeptideMatch) resCur[0];
                peptideMatchArray[index++] = pm;
                peptideMatches.add(pm);
                // resCur[1] : (pm.msQuery is fetch because it is declared as lazy and it is needed later)
            }*/

            //m_rset.getTransientData().setPeptideMatches(peptideMatchArray);

 
            // Retrieve Peptide Match Ids and create map of PeptideMatch in the same time
            /*int nb = peptideMatchArray.length;
            m_peptideMatchIds = new ArrayList<>(nb);
            m_peptideMatchMap = new HashMap<>();
            for (int i = 0; i < nb; i++) {
                PeptideMatch pm = peptideMatchArray[i];
                Long pmId = pm.getId();
                m_peptideMatchIds.add(i, pmId);
                m_peptideMatchMap.put(pmId, pm);
            }*/
            
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


             /**
             * Spectrum for each PeptideMatch
             *
             */
            // slice the task and get the first one
            //subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SPECTRUM, m_peptideMatchIds.size(), SLICE_SIZE);

            // execute the first slice now
            //fetchSpectrum(entityManagerMSI, subTask/*, DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getDataSource().getConnection()*/);           
            

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

            ArrayList<PeptideMatch> peptideMatches;
            PeptideMatch[] peptideMatchArray;



            // Load Peptide Match for a ProteinMatch ordered by query id and Peptide name
            // (pm.msQuery is fetch because it is declared as lazy and it is needed later)
            Query peptideMatchQuery = entityManagerMSI.createQuery("SELECT pm, pm.msQuery FROM PeptideMatch pm, Peptide p, SequenceMatch sm WHERE sm.id.proteinMatchId=:proteinId AND sm.bestPeptideMatchId=pm AND pm.peptideId=p.id ORDER BY pm.msQuery.initialId ASC, p.sequence ASC");
            peptideMatchQuery.setParameter("proteinId", m_proteinMatch.getId());

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

            m_proteinMatch.getTransientData().setPeptideMatches(peptideMatchArray);


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
            //subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SPECTRUM, peptideMatches.size(), SLICE_SIZE);

            // execute the first slice now
            //fetchSpectrum(entityManagerMSI, subTask /*, DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getDataSource().getConnection()*/);


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

            ArrayList<PeptideMatch> peptideMatches;
            PeptideMatch[] peptideMatchArray;
            
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
            //subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SPECTRUM, peptideMatches.size(), SLICE_SIZE);

            // execute the first slice now
            //fetchSpectrum(entityManagerMSI, subTask /*, DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getDataSource().getConnection()*/);     
            

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
                //case SUB_TASK_SPECTRUM:
                //   fetchSpectrum(entityManagerMSI, subTask /*, DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getDataSource().getConnection()*/);
                //    break;
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
        TypedQuery peptideMatchQuery = entityManagerMSI.createQuery("SELECT pm FROM PeptideMatch pm WHERE pm.id IN (:listId)",PeptideMatch.class);
        peptideMatchQuery.setParameter("listId", sliceOfPeptideMatchIds);
        List<PeptideMatch> resultList = peptideMatchQuery.getResultList();
        
        
        if (m_peptideMatchMap == null) {
            m_peptideMatchMap = new HashMap<>();
        }
        
        PeptideMatch[] peptideMatches = m_rset.getTransientData().getPeptideMatches();
        
        
        int i = subTask.getStartIndex();
        Iterator<PeptideMatch> it = resultList.iterator();
        while (it.hasNext()) {
            PeptideMatch peptideMatch = it.next();
            peptideMatches[i] = peptideMatch;
            m_peptideMatchMap.put(peptideMatch.getId(), peptideMatch);
            i++;
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

        Query msQueryQuery = entityManagerMSI.createQuery("SELECT pm.id, msq, s.precursorIntensity FROM PeptideMatch pm,MsQuery msq, Spectrum s WHERE pm.id IN (:listId) AND pm.msQuery=msq AND msq.spectrum=s");
        msQueryQuery.setParameter("listId", sliceOfPeptideMatchIds);


        List<Object[]> msQueries = msQueryQuery.getResultList();
        Iterator<Object[]> it = msQueries.iterator();
        while (it.hasNext()) {
            Object[] resCur = it.next();
            Long peptideMatchId = (Long) resCur[0];
            MsQuery q = (MsQuery) resCur[1];
            Float precursorIntensity = (Float) resCur[2];
            PeptideMatch peptideMatch = m_peptideMatchMap.get(peptideMatchId);
            peptideMatch.getTransientData().setIsMsQuerySet(true);
            peptideMatch.setMsQuery(q);
            q.setTransientPrecursorIntensity(precursorIntensity);
        }
        
        /*List sliceOfPeptideMatchIds = subTask.getSubList(m_peptideMatchIds);

        Query msQueryQuery = entityManagerMSI.createQuery("SELECT pm.id, msq.id, msq.initialId FROM PeptideMatch pm,MsQuery msq WHERE pm.id IN (:listId) AND pm.msQuery=msq");
        msQueryQuery.setParameter("listId", sliceOfPeptideMatchIds);


        List<Object[]> msQueries = msQueryQuery.getResultList();
        Iterator<Object[]> it = msQueries.iterator();
        while (it.hasNext()) {
            Object[] resCur = it.next();
            Long peptideMatchId = (Long) resCur[0];
            
            Long msQueryId = (Long) resCur[1];
            Integer msQueryInitialId = (Integer) resCur[2];
            MsQuery q = new MsQuery();
            q.setId(msQueryId);
            q.setInitialId(msQueryInitialId);
            PeptideMatch peptideMatch = m_peptideMatchMap.get(peptideMatchId);
            peptideMatch.getTransientData().setIsMsQuerySet(true);
            peptideMatch.setMsQuery(q);
        }*/
        

		
        
    }
    
    private void fetchSpectrum(EntityManager entityManagerMSI, SubTask subTask /*, Connection connection SQL TEST*/) throws Exception {
        
        try {
        
        List sliceOfPeptideMatchIds = subTask.getSubList(m_peptideMatchIds);

        
        //JPM.PUTBACK
        
        Query msQueryQuery = entityManagerMSI.createQuery("SELECT pm.id, s FROM PeptideMatch pm,MsQuery msq, Spectrum s WHERE pm.id IN (:listId) AND pm.msQuery=msq AND msq.spectrum.id=s.id");
        

        msQueryQuery.setParameter("listId", sliceOfPeptideMatchIds);

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
        
        /*
        Query msQueryQuery = entityManagerMSI.createQuery("SELECT pm.id, s.id, s.firstCycle, s.firstScan, s.firstTime, s.instrumentConfigId, s.intensityList, s.isSummed, s.lastCycle, s.lastScan, s.lastTime, s.mozList, s.peakCount, s.peaklistId, s.precursorCharge, s.precursorIntensity, s.precursorMoz, s.serializedProperties, s.title FROM PeptideMatch pm,MsQuery msq, Spectrum s WHERE pm.id IN (:listId) AND pm.msQuery=msq AND msq.spectrum.id=s.id");
        msQueryQuery.setParameter("listId", sliceOfPeptideMatchIds);


        List<Object[]> msQueries = msQueryQuery.getResultList();
        Iterator<Object[]> it = msQueries.iterator();
        while (it.hasNext()) {
            Object[] resCur = it.next();
            
            Long peptideMatchId = (Long) resCur[0];
            Long spectrumId = (Long) resCur[1];
            Integer firstCycle = (Integer) resCur[2];
            Integer firstScan = (Integer) resCur[3];
            Float firstTime = (Float) resCur[4];
            Long instrumentConfigId = (Long) resCur[5];
            byte[] intensityList = (byte[]) resCur[6];
            Boolean isSummed = (Boolean) resCur[7];
            Integer lastCycle = (Integer) resCur[8];
            Integer lastScan = (Integer) resCur[9];
            Float lastTime = (Float) resCur[10];
            byte[] mozList = (byte[]) resCur[11];
            Integer peakCount = (Integer) resCur[12];
            Long peaklistId = (Long) resCur[13];
            Integer precursorCharge = (Integer) resCur[14];
            Float precursorIntensity = (Float) resCur[15];
            Double precursorMoz = (Double) resCur[16];
            String serializedProperties = (String) resCur[17];
            String title = (String) resCur[18];

            Spectrum spectrum = new Spectrum();
            spectrum.setId(spectrumId);
            spectrum.setFirstCycle(firstCycle);
            spectrum.setFirstScan(firstScan);
            spectrum.setFirstTime(firstTime);
            spectrum. setInstrumentConfigId(instrumentConfigId);
            spectrum.setIntensityList(intensityList);
            spectrum.setIsSummed(isSummed);
            spectrum.setLastCycle(lastCycle);
            spectrum.setLastScan(lastScan);
            spectrum.setLastTime(lastTime);
            spectrum.setMozList(mozList);
            spectrum.setPeakCount(peakCount);
            spectrum.setPeaklistId(peaklistId);
            spectrum.setPrecursorCharge(precursorCharge);
            spectrum.setPrecursorIntensity(precursorIntensity);
            spectrum.setPrecursorMoz(precursorMoz);
            spectrum.setSerializedProperties(serializedProperties);
            spectrum.setTitle(title);
            
            
            PeptideMatch peptideMatch = m_peptideMatchMap.get(peptideMatchId);
            MsQuery q = peptideMatch.getMsQuery();
            q.setTransientIsSpectrumSet(true);
            q.setSpectrum(spectrum);
        }*/
        
        /*int nb = sliceOfPeptideMatchIds.size();
        //String selectSQL = "SELECT pm.id, s.id, s.first_cycle, s.first_scan, s.first_time, s.instrument_config_id, s.intensity_list, s.is_summed, s.last_cycle, s.last_scan, s.last_time, s.moz_list, s.peak_count, s.peaklist_id, s.precursor_charge, s.precursor_intensity, s.precursor_moz, s.serialized_properties, s.title FROM peptide_match pm,ms_query msq, spectrum s WHERE pm.ms_query_id=msq.id AND msq.spectrum_id=s.id AND pm.id IN (";
        
        String selectSQL = "SELECT pm.id, s.id, s.precursor_intensity FROM peptide_match pm,ms_query msq, spectrum s WHERE pm.ms_query_id=msq.id AND msq.spectrum_id=s.id AND pm.id IN (";
        int capacity = selectSQL.length()+2*nb;
        StringBuilder sb = new StringBuilder(capacity);
        sb.append(selectSQL);
        for (int i=0;i<nb;i++) {
            if (i<nb-1) {
                sb.append("?,");
            } else {
                sb.append("?)");
            }
        }
        
        PreparedStatement preparedStatement = connection.prepareStatement(sb.toString());
        for (int i=0;i<nb;i++) {
            preparedStatement.setLong(i+1, (Long) sliceOfPeptideMatchIds.get(i));
        }
        
        java.sql.ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Long peptideMatchId = rs.getLong(1);
                Long spectrumId = rs.getLong(2);
                /*Integer firstCycle = rs.getInt(3);
                Integer firstScan = rs.getInt(4);
                Float firstTime = rs.getFloat(5);
                Long instrumentConfigId = rs.getLong(6);
                byte[] intensityList = rs.getBytes(7);
                Boolean isSummed = rs.getBoolean(8);
                Integer lastCycle = rs.getInt(9);
                Integer lastScan = rs.getInt(10);
                Float lastTime = rs.getFloat(11);
                byte[] mozList = rs.getBytes(12);
                Integer peakCount = rs.getInt(13);
                Long peaklistId = rs.getLong(14);
                Integer precursorCharge = rs.getInt(15);*/
                /*Float precursorIntensity = rs.getFloat(3);
                /*Double precursorMoz = rs.getDouble(17);
                String serializedProperties = rs.getString(18);
                String title = rs.getString(19);*/
                
                
                /*Spectrum spectrum = new Spectrum();
                spectrum.setId(spectrumId);
                /*spectrum.setFirstCycle(firstCycle);
                spectrum.setFirstScan(firstScan);
                spectrum.setFirstTime(firstTime);
                spectrum.setInstrumentConfigId(instrumentConfigId);
                spectrum.setIntensityList(intensityList);
                spectrum.setIsSummed(isSummed);
                spectrum.setLastCycle(lastCycle);
                spectrum.setLastScan(lastScan);
                spectrum.setLastTime(lastTime);
                spectrum.setMozList(mozList);
                spectrum.setPeakCount(peakCount);
                spectrum.setPeaklistId(peaklistId);
                spectrum.setPrecursorCharge(precursorCharge);*/
                /*spectrum.setPrecursorIntensity(precursorIntensity);
                /*spectrum.setPrecursorMoz(precursorMoz);
                spectrum.setSerializedProperties(serializedProperties);
                spectrum.setTitle(title);*/

/*
                PeptideMatch peptideMatch = m_peptideMatchMap.get(peptideMatchId);
                MsQuery q = peptideMatch.getMsQuery();
                q.setTransientIsSpectrumSet(true);
                q.setSpectrum(spectrum);
            }*/
         
        
 
        } finally {
            //connection.close();
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
