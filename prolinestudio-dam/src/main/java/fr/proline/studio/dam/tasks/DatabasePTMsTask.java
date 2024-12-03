/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.PtmSpecificity;
import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.data.ptm.PTMCluster;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.performancetest.PerformanceTest;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Tasks to get Generic PTM information
 *
 * @author JM235353
 */
public class DatabasePTMsTask extends AbstractDatabaseSlicerTask {

    private long m_projectId = -1;

    //attributes to initialize when data is retrieve
    private List<PtmSpecificity> m_ptmsOutput = null;

    //Parameters for PTMs info for specific RSMs
    private List<Long> m_rsmIds = null;

    public static final int NO_SUB_TASK_COUNT = 0;


    private int m_action;
    private final static int FILL_ALL_PTM_INFO = 0;
    private final static int LOAD_IDENTIFIED_PTM_SPECIFICITIES = 1;


    public DatabasePTMsTask(AbstractDatabaseCallback callback) {
        super(callback);
    }

    /**
     * Get generic PTM information (DInfoPTM object from PTM Specificity) registered for specified project.
     *
     * @param projectId
     */
    public void initFillPTMInfo(Long projectId) {
        init(NO_SUB_TASK_COUNT, new TaskInfo("Load PTM Info", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_action = FILL_ALL_PTM_INFO;
    }

    /**
     * Get all PTMSpecificity used for specified result summary identification search
     * @param projectId
     * @param rsmId
     * @param ptmsToFill
     */
    public void initLoadUsedPTMs(Long projectId, Long rsmId, List<PtmSpecificity> ptmsToFill) {
        init(NO_SUB_TASK_COUNT, new TaskInfo("Load used PTMs from RSM id" + rsmId, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_rsmIds = new ArrayList<>();
        m_rsmIds.add(rsmId);
        m_ptmsOutput = ptmsToFill;
        m_action = LOAD_IDENTIFIED_PTM_SPECIFICITIES;
    }

    /**
     * Get all PTMSpecificity used for all specified result summary identification searches
     * @param projectId
     * @param rsmIds
     * @param ptmsToFill
     */
    public void initLoadUsedPTMs(Long projectId, List<Long> rsmIds, List<PtmSpecificity> ptmsToFill) {
        init(NO_SUB_TASK_COUNT, new TaskInfo("Load used PTMs from RSM ids" + rsmIds, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_rsmIds = rsmIds;
        m_ptmsOutput = ptmsToFill;
        m_action = LOAD_IDENTIFIED_PTM_SPECIFICITIES;
    }

    @Override
    public boolean needToFetch() {
        switch (m_action) {
            case FILL_ALL_PTM_INFO: {
                return DInfoPTM.getInfoPTMMap().isEmpty();
            }
            case LOAD_IDENTIFIED_PTM_SPECIFICITIES: {
                return true;
            }
        }
        return false; // should not be called 
    }

    @Override
    public boolean fetchData() {
        try {
        switch (m_action) {
            case FILL_ALL_PTM_INFO: {
                if (needToFetch()) {
                    return fetchPTMInfo();
                }
            }
            case LOAD_IDENTIFIED_PTM_SPECIFICITIES:  {
                if (needToFetch()) {
                    return fetchIdentifiedPTMs();
                }
            }
        }
        } finally {
//            PerformanceTest.displayTimeAllThreads(); //VDS For debug Only !
        }
        return true; // should not happen                
    }

    /*
     * Fetch data of a Subtask
     *
     * @return
     */
    //No subtask define in this classe currently !!
//    private boolean fetchDataSubTaskFor(int action) {
//        SubTask slice = m_subTaskManager.getNextSubTask();
//        if (slice == null) {
//            return true; // nothing to do : should not happen
//        }
//
//        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
//        try {
//            entityManagerMSI.getTransaction().begin();
//            switch (action) {
////                case FILL_ALL_PTM_SITES_PEPINFO:
////                    switch (slice.getSubTaskId()) {
////                        case SUB_TASK_PTMSITE_PEPINSTANCES:
////                            fetchPTMSitesPepInstances(slice, entityManagerMSI);
////                            break;
////                    }
//            }
//            entityManagerMSI.getTransaction().commit();
//        } catch (Exception e) {
//            m_logger.error(getClass().getSimpleName() + " failed", e);
//            m_taskError = new TaskError(e);
//            try {
//                entityManagerMSI.getTransaction().rollback();
//            } catch (Exception rollbackException) {
//                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
//            }
//            return false;
//        } finally {
//            entityManagerMSI.close();
//                    }
//
//        return true;
//    }

    private boolean fetchIdentifiedPTMs() {
//        PerformanceTest.startTime("fetchIdentifiedPTMs");
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            TypedQuery<PtmSpecificity> query = entityManagerMSI.createQuery("SELECT DISTINCT(pp.specificity) FROM PeptidePtm pp, PeptideInstance pi JOIN FETCH pp.specificity.ptm ptm WHERE pi.resultSummary.id in (:rsmIds) AND pi.peptide.id = pp.peptide.id", PtmSpecificity.class);
            query.setParameter("rsmIds", m_rsmIds);
            m_ptmsOutput.addAll(query.getResultList());

        }  catch (Exception e) {
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
//        PerformanceTest.stopTime("fetchIdentifiedPTMs");
        return true;
    }



    private boolean fetchPTMInfo() {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            fetchGenericPTMData(entityManagerMSI);
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

    static protected void fetchGenericPTMData(EntityManager entityManagerMSI) {

//        PerformanceTest.startTime("fetchGenericPTMData");
        
        HashMap<Long, DInfoPTM> infoPTMMAp = DInfoPTM.getInfoPTMMap();
        if (!infoPTMMAp.isEmpty()) {
//            PerformanceTest.stopTime("fetchGenericPTMData");
            return; // already loaded
        }

        TypedQuery<DInfoPTM> ptmInfoQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DInfoPTM(spec.id, ptm.id, spec.residue, spec.location, ptm.shortName, evidence.composition, evidence.monoMass) \n"
                + "FROM fr.proline.core.orm.msi.PtmSpecificity as spec, fr.proline.core.orm.msi.Ptm as ptm, fr.proline.core.orm.msi.PtmEvidence as evidence \n"
                + "WHERE spec.ptm=ptm AND ptm=evidence.ptm AND evidence.type='Precursor' ", DInfoPTM.class);
        List<DInfoPTM> ptmInfoList = ptmInfoQuery.getResultList();

        Iterator<DInfoPTM> it = ptmInfoList.iterator();
        while (it.hasNext()) {
            DInfoPTM infoPTM = it.next();
            DInfoPTM.addInfoPTM(infoPTM);
        }

//        PerformanceTest.stopTime("fetchGenericPTMData");
    }

    protected static HashMap<Long, ArrayList<DPeptidePTM>> fetchPeptidePTMForPeptides(EntityManager entityManagerMSI, ArrayList<Long> allPeptidesIds) {

        PerformanceTest.startTime("fetchPeptidePTMForPeptides");
        
        HashMap<Long, ArrayList<DPeptidePTM>> ptmMap = new HashMap<>();
        TypedQuery<DPeptidePTM> ptmQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DPeptidePTM(pptm.peptide.id, pptm.specificity.id, pptm.seqPosition) \n"
                + "FROM PeptidePtm as pptm  \n"
                + "WHERE pptm.peptide.id IN (:peptideList) ", DPeptidePTM.class);
        ptmQuery.setParameter("peptideList", allPeptidesIds);
        List<DPeptidePTM> ptmList = ptmQuery.getResultList();

        Iterator<DPeptidePTM> it = ptmList.iterator();
        while (it.hasNext()) {
            DPeptidePTM ptm = it.next();
            Long peptideId = ptm.getIdPeptide();
            ArrayList<DPeptidePTM> list = ptmMap.computeIfAbsent(peptideId, k -> new ArrayList<>());
            list.add(ptm);

        }
        
        PerformanceTest.stopTime("fetchPeptidePTMForPeptides");

        return ptmMap;
    }


    public static void /*fetchReadablePTMData*/fillReadablePTMDataForPeptides(EntityManager entityManagerMSI, Long rsetId, HashMap<Long, Peptide> peptideMap, List<Long> sliceOfPeptideMatchIds) {
        if ((peptideMap == null) || peptideMap.isEmpty()) {
            return;
        }

        if (sliceOfPeptideMatchIds == null) {
            sliceOfPeptideMatchIds = new ArrayList<>(peptideMap.keySet());
        }

        // Retrieve PeptideReadablePtmString
        Query ptmStingQuery = entityManagerMSI.createQuery("SELECT p.id, ptmString "
                + "FROM fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.PeptideReadablePtmString ptmString "
                + "WHERE p.id IN (:listId) AND ptmString.peptide=p AND ptmString.resultSet.id=:rsetId");
        ptmStingQuery.setParameter("listId", sliceOfPeptideMatchIds);
        ptmStingQuery.setParameter("rsetId", rsetId);

        List<Object[]> ptmStrings = ptmStingQuery.getResultList();
        Iterator<Object[]> it = ptmStrings.iterator();
        while (it.hasNext()) {
            Object[] res = it.next();
            Long peptideId = (Long) res[0];
            PeptideReadablePtmString ptmString = (PeptideReadablePtmString) res[1];
            Peptide peptide = peptideMap.get(peptideId);
            peptide.getTransientData().setPeptideReadablePtmString(ptmString);
        }

    }


    public static void /*fetchPTMDataForPeptides*/ fillPeptidePTMForPeptides(EntityManager entityManagerMSI, HashMap<Long, Peptide> peptideMap, List<Long> sliceOfPeptideMatchIds) {

        if (!peptideMap.isEmpty()) {

            if (sliceOfPeptideMatchIds == null) {
                sliceOfPeptideMatchIds = new ArrayList<>(peptideMap.keySet());
            }

            TypedQuery<DPeptidePTM> ptmQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DPeptidePTM(pptm.peptide.id, pptm.specificity.id, pptm.seqPosition) FROM fr.proline.core.orm.msi.PeptidePtm pptm WHERE pptm.peptide.id IN (:peptideIds)", DPeptidePTM.class);
            ptmQuery.setParameter("peptideIds", sliceOfPeptideMatchIds);
            List<DPeptidePTM> ptmList = ptmQuery.getResultList();

            Iterator<DPeptidePTM> it = ptmList.iterator();
            while (it.hasNext()) {
                DPeptidePTM ptm = it.next();

                Peptide p = peptideMap.get(ptm.getIdPeptide());
                HashMap<Integer, DPeptidePTM> map = p.getTransientData().getDPeptidePtmMap();
                if (map == null) {
                    map = new HashMap<>();
                    p.getTransientData().setDPeptidePtmMap(map);
                }

                map.put((int) ptm.getSeqPosition(), ptm);

            }
        }

    }

}
