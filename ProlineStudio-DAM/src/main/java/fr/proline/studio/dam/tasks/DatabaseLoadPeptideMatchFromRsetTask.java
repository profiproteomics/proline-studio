/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.*;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 *
 * @author JM235353
 */
public class DatabaseLoadPeptideMatchFromRsetTask extends AbstractDatabaseSlicerTask {
    
    // used to slice the task in sub tasks
    private static final int SLICE_SIZE = 500;
    // different possible subtasks
    public static final int SUB_TASK_PEPTIDE = 0;
    public static final int SUB_TASK_MSQUERY = 1;
    public static final int SUB_TASK_SPECTRUM = 2;
    public static final int SUB_TASK_COUNT = 3; // <<----- get in sync

    
    private ResultSet rset = null;
    // data kept for sub tasks
    private ArrayList<Integer> peptideMatchIds = null;
    private HashMap<Integer, PeptideMatch> peptideMatchMap = null;
    

    public DatabaseLoadPeptideMatchFromRsetTask(AbstractDatabaseCallback callback, ResultSet rset) {
        super(callback, SUB_TASK_COUNT);
        this.rset = rset;
    }

    @Override
    public boolean needToFetch() {
        return (rset.getTransientPeptideMatches() == null);
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

        EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(ProlineRepository.Databases.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
        try {

            entityManagerMSI.getTransaction().begin();

            Integer rsetId = rset.getId();

            // Load Peptide Match order by query id and Peptide name
            // SELECT pm FROM PeptideMatch pm, Peptide p WHERE pm.resultSet.id=:rsetId AND pm.peptideId=p.id ORDER BY pm.msQuery.initialId ASC, p.sequence ASC
            TypedQuery<PeptideMatch> peptideMatchQuery = entityManagerMSI.createQuery("SELECT pm FROM PeptideMatch pm, Peptide p WHERE pm.resultSet.id=:rsetId AND pm.peptideId=p.id ORDER BY pm.msQuery.initialId ASC, p.sequence ASC", PeptideMatch.class);
            peptideMatchQuery.setParameter("rsetId", rsetId);
            List<PeptideMatch> peptideMatches = peptideMatchQuery.getResultList();

            PeptideMatch[] peptideMatchArray = peptideMatches.toArray(new PeptideMatch[peptideMatches.size()]);
            rset.setTransientPeptideMatches(peptideMatchArray);

            
            // Retrieve Peptide Match Ids and create map of PeptideMatch in the same time
            int nb = peptideMatchArray.length;
            peptideMatchIds = new ArrayList<Integer>(nb);
            peptideMatchMap = new HashMap<Integer, PeptideMatch>();
            for (int i = 0; i < nb; i++) {
                PeptideMatch pm = peptideMatchArray[i];
                Integer id = pm.getId();
                peptideMatchIds.add(i, id);
                peptideMatchMap.put(id, pm);
            }
            
            
            /**
             * Peptide for each PeptideMatch
             *
             */
            // slice the task and get the first one
            SubTask subTask = subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PEPTIDE, peptideMatches.size(), SLICE_SIZE);

            // execute the first slice now
            fetchPeptide(entityManagerMSI, subTask);
            
            /**
             * MS_Query for each PeptideMatch
             *
             */
            // slice the task and get the first one
            subTask = subTaskManager.sliceATaskAndGetFirst(SUB_TASK_MSQUERY, peptideMatches.size(), SLICE_SIZE);

            // execute the first slice now
            fetchMsQuery(entityManagerMSI, subTask);


             /**
             * Spectrum for each PeptideMatch
             *
             */
            // slice the task and get the first one
            subTask = subTaskManager.sliceATaskAndGetFirst(SUB_TASK_SPECTRUM, peptideMatches.size(), SLICE_SIZE);

            // execute the first slice now
            fetchSpectrum(entityManagerMSI, subTask);           
            



            entityManagerMSI.getTransaction().commit();
        } catch (RuntimeException e) {
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
    private boolean fetchDataSubTask() {
        SubTask slice = subTaskManager.getNextSubTask();
        if (slice == null) {
            return true; // nothing to do : should not happen
        }

        EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(ProlineRepository.Databases.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
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
            }


            entityManagerMSI.getTransaction().commit();
        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
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

        PeptideMatch[] peptideMatchArray = rset.getTransientPeptideMatches();
        List sliceOfPeptideMatchIds = subTask.getSubList(peptideMatchIds);

        Query peptideQuery = entityManagerMSI.createQuery("SELECT pm.id, p FROM PeptideMatch pm,fr.proline.core.orm.msi.Peptide p WHERE pm.id IN (:listId) AND pm.peptideId=p.id");
        peptideQuery.setParameter("listId", sliceOfPeptideMatchIds);

 
        
        int i = subTask.getStartIndex();
        List<Object[]> peptides = peptideQuery.getResultList();
        Iterator<Object[]> it = peptides.iterator();
        while (it.hasNext()) {
            Object[] res = it.next();
            Integer peptideMatchId = (Integer) res[0];
            Peptide peptide = (Peptide) res[1];
            peptideMatchMap.get(peptideMatchId).getTransientData().setPeptide(peptide);
        }
        
        /*
        Query peptideQuery = entityManagerMSI.createQuery("SELECT p, sm FROM PeptideMatch pm,fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.SequenceMatch as sm WHERE pm.id IN (:listId) AND pm.peptideId=p.id AND sm.id.peptideId=p.id ");
        peptideQuery.setParameter("listId", sliceOfPeptideMatchIds);

        HashMap<Integer, Peptide> peptideMap = new HashMap<Integer, Peptide>();
        int i = subTask.getStartIndex();
        List<Object[]> peptides = peptideQuery.getResultList();
        Iterator<Object[]> it = peptides.iterator();
        while (it.hasNext()) {
            Object[] curRes = it.next();
            Peptide peptide = (Peptide) curRes[0];
            peptideMatchArray[i++].getTransientData().setPeptide(peptide);
            
            peptide.setTransientData(new Peptide.TransientData());
            peptide.getTransientData().setSequenceMatch((SequenceMatch)curRes[1]);
            
            peptideMap.put(peptide.getId() , peptide);
        }
        

        EntityManager entityManagerPS = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(ProlineRepository.Databases.PS, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
        try {

            entityManagerPS.getTransaction().begin();
            
            TypedQuery<PeptidePtm> ptmQuery = entityManagerPS.createQuery("SELECT ptm FROM fr.proline.core.orm.ps.PeptidePtm ptm WHERE ptm.peptide.id IN (:peptideIds)", PeptidePtm.class);
            ptmQuery.setParameter("peptideIds", peptideMap.keySet());
            List<PeptidePtm> ptmList = ptmQuery.getResultList();
            
            Iterator<PeptidePtm> itPtm = ptmList.iterator();
            while (itPtm.hasNext()) {
                PeptidePtm ptm = itPtm.next();
                
                Peptide p = peptideMap.get(ptm.getPeptide().getId());
                HashMap<Integer, PeptidePtm> map = p.getTransientData().getPeptidePtmMap();
                if (map == null) {
                    map = new HashMap<Integer, PeptidePtm>();
                    p.getTransientData().setPeptidePtmMap(map);
                }
                map.put(ptm.getSeqPosition(), ptm);
               
            }
            
            entityManagerPS.getTransaction().commit();
        } catch (RuntimeException e) {
            logger.error(getClass().getSimpleName()+" failed", e);
        } finally {
            entityManagerPS.close();
        }*/
    }
    
    /**
     * Retrieve MsQuery for a Sub Task
     *
     * @param entityManagerMSI
     * @param slice
     */
    private void fetchMsQuery(EntityManager entityManagerMSI, SubTask subTask) {


        List sliceOfPeptideMatchIds = subTask.getSubList(peptideMatchIds);

        Query msQueryQuery = entityManagerMSI.createQuery("SELECT pm.id, msq FROM PeptideMatch pm,MsQuery msq WHERE pm.id IN (:listId) AND pm.msQuery=msq");
        msQueryQuery.setParameter("listId", sliceOfPeptideMatchIds);

        int i = subTask.getStartIndex();
        List<Object[]> msQueries = msQueryQuery.getResultList();
        Iterator<Object[]> it = msQueries.iterator();
        while (it.hasNext()) {
            Object[] resCur = it.next();
            Integer peptideMatchId = (Integer) resCur[0];
            MsQuery q = (MsQuery) resCur[1];
            PeptideMatch peptideMatch = peptideMatchMap.get(peptideMatchId);
            peptideMatch.getTransientData().setIsMsQuerySet(true);
            peptideMatch.setMsQuery(q);
        }
    }
    
    private void fetchSpectrum(EntityManager entityManagerMSI, SubTask subTask) {
        
        List sliceOfPeptideMatchIds = subTask.getSubList(peptideMatchIds);

        Query msQueryQuery = entityManagerMSI.createQuery("SELECT pm.id, s FROM PeptideMatch pm,MsQuery msq, Spectrum s WHERE pm.id IN (:listId) AND pm.msQuery=msq AND msq.spectrum.id=s.id");
        msQueryQuery.setParameter("listId", sliceOfPeptideMatchIds);

        int i = subTask.getStartIndex();
        List<Object[]> msQueries = msQueryQuery.getResultList();
        Iterator<Object[]> it = msQueries.iterator();
        while (it.hasNext()) {
            Object[] resCur = it.next();
            Integer peptideMatchId = (Integer) resCur[0];
            Spectrum spectrum = (Spectrum) resCur[1];
            PeptideMatch peptideMatch = peptideMatchMap.get(peptideMatchId);
            MsQuery q = peptideMatch.getMsQuery();
            q.setTransientIsSpectrumSet(true);
            q.setSpectrum(spectrum);
        }
    }
    


}
