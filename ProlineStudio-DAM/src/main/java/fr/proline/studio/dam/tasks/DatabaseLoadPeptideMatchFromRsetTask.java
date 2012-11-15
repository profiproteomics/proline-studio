/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.ps.PeptidePtm;
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
    private static final int SLICE_SIZE = 100;
    // different possible subtasks
    public static final int SUB_TASK_PEPTIDE = 0;
    public static final int SUB_TASK_MSQUERY = 1;
    public static final int SUB_TASK_COUNT = 2; // <<----- get in sync

    
    private ResultSet rset = null;
    // data kept for sub tasks
    ArrayList<Integer> peptideMatchIds = null;
    /*HashMap<Integer, ProteinSet> proteinSetMap = null;
    ArrayList<Integer> proteinSetIds = null;*/

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

            // Load Peptide Match
            // SELECT pm FROM PeptideMatch pm WHERE pm.resultSet.id=:rsetId ORDER BY pm.score DESC
            TypedQuery<PeptideMatch> peptideMatchQuery = entityManagerMSI.createQuery("SELECT pm FROM PeptideMatch pm WHERE pm.resultSet.id=:rsetId ORDER BY pm.score DESC", PeptideMatch.class);
            peptideMatchQuery.setParameter("rsetId", rsetId);
            List<PeptideMatch> peptideMatches = peptideMatchQuery.getResultList();

            PeptideMatch[] peptideMatchArray = peptideMatches.toArray(new PeptideMatch[peptideMatches.size()]);
            rset.setTransientPeptideMatches(peptideMatchArray);
            
            
            // Retrieve Protein Match Ids
            peptideMatchIds = new ArrayList<Integer>(peptideMatchArray.length);

            int nb = peptideMatchArray.length;
            for (int i = 0; i < nb; i++) {
                peptideMatchArray[i].setTransientData(new PeptideMatch.TransientData());
                peptideMatchIds.add(i, peptideMatchArray[i].getId());
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

        TypedQuery<Peptide> peptideQuery = entityManagerMSI.createQuery("SELECT p FROM PeptideMatch pm,fr.proline.core.orm.msi.Peptide p WHERE pm.id IN (:listId) AND pm.peptideId=p.id", Peptide.class);
        peptideQuery.setParameter("listId", sliceOfPeptideMatchIds);


        int i = subTask.getStartIndex();
        List<Peptide> peptides = peptideQuery.getResultList();
        Iterator<Peptide> it = peptides.iterator();
        while (it.hasNext()) {
            Peptide peptide = it.next();
            peptideMatchArray[i++].getTransientData().setPeptide(peptide);
            
            peptide.setTransientData(new Peptide.TransientData());

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

        PeptideMatch[] peptideMatchArray = rset.getTransientPeptideMatches();
        List sliceOfPeptideMatchIds = subTask.getSubList(peptideMatchIds);

        TypedQuery<MsQuery> msQueryQuery = entityManagerMSI.createQuery("SELECT msq FROM PeptideMatch pm,MsQuery msq WHERE pm.id IN (:listId) AND pm.msQuery=msq", MsQuery.class);
        msQueryQuery.setParameter("listId", sliceOfPeptideMatchIds);

        int i = subTask.getStartIndex();
        List<MsQuery> msQueries = msQueryQuery.getResultList();
        Iterator<MsQuery> it = msQueries.iterator();
        while (it.hasNext()) {
            peptideMatchArray[i].getTransientData().setIsMsQuerySet(true);
            peptideMatchArray[i++].setMsQuery(it.next());
        }
    }


}
