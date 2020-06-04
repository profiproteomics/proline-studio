/* 
 * Copyright (C) 2019 VD225637
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.SequenceMatch;
import fr.proline.core.orm.msi.Spectrum;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DSpectrum;
import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.core.orm.msi.dto.DPtmSiteProperties;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.data.ptm.JSONPTMCluster;
import fr.proline.studio.dam.tasks.data.ptm.JSONPTMDataset;
import fr.proline.studio.dam.tasks.data.ptm.JSONPTMSite2;
import fr.proline.studio.dam.tasks.data.ptm.PTMCluster;
import fr.proline.studio.dam.tasks.data.ptm.PTMDataset;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.openide.util.Exceptions;

import fr.proline.studio.performancetest.PerformanceTest;

/**
 *
 * @author JM235353
 */
public class DatabasePTMsTask extends AbstractDatabaseSlicerTask {

    private long m_projectId = -1;
    private DDataset m_dataset = null;
    private PTMDataset m_ptmDataset = null;

    //attributes to initialize when data is retrieve
    private List<PTMSite> m_ptmSitesOutput = null;
    private List<PTMDataset> m_ptmDatasetOutput = null;

    public static final int SUB_TASK_PTMSITE_PEPTIDES = 0;
    public static final int SUB_TASK_PTMCLUSTER_PEPTIDES = 1;
    public static final int SUB_TASK_PTMSITE_PEPINSTANCES = 2;
    public static final int SUB_TASK_COUNT = 3; // <<----- get in sync  
    final int SLICE_SIZE = 1000;

    // data kept for sub tasks
    private List<Long> m_bestSitesPepMatchIds = null;
    private HashMap<Long, List<PTMSite>> m_ptmSitesByBestPepMatchId = null;
    private List<Long> m_bestClustersPepMatchIds = null;
    private HashMap<Long, List<PTMCluster>> m_ptmClustersByBestPepMatchId = null;
    private List<Long> m_peptideIds = null;

    private int m_action;
    private final static int LOAD_PTMDATASET = 0;
    private final static int FILL_ALL_PTM_SITES_PEPINFO = 1;
    private final static int FILL_ALL_PTM_INFO = 2;

    private boolean m_loadSitesAsClusters;

    public DatabasePTMsTask(AbstractDatabaseCallback callback) {
        super(callback);
    }

    public void initFillPTMInfo(Long projectId) {
        init(SUB_TASK_COUNT, new TaskInfo("Load PTM Info", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_action = FILL_ALL_PTM_INFO;
    }

    public void initFillPTMSites(long projectId, PTMDataset ptmDS, List<PTMSite> ptmSitesToFill) {
        init(SUB_TASK_COUNT, new TaskInfo("Load peptides of PTM Sites", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_ptmSitesOutput = ptmSitesToFill;
        m_ptmDataset = ptmDS;
        m_action = FILL_ALL_PTM_SITES_PEPINFO;
    }

    public void initLoadPTMDataset(Long projectId, DDataset dataset, List<PTMDataset> ptmDataset, boolean loadSitesAsClusters) {
        init(SUB_TASK_COUNT, new TaskInfo("Load PTM Dataset for " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_ptmDatasetOutput = ptmDataset;
        m_dataset = dataset;
        m_action = LOAD_PTMDATASET;
        m_loadSitesAsClusters = loadSitesAsClusters;
    }

    @Override
    public boolean needToFetch() {
        switch (m_action) {
            case FILL_ALL_PTM_SITES_PEPINFO:
                return (m_peptideIds == null || m_peptideIds.isEmpty());
            case LOAD_PTMDATASET: {
                return (m_ptmDatasetOutput == null || m_ptmDatasetOutput.isEmpty());
            }
            case FILL_ALL_PTM_INFO: {
                return DInfoPTM.getInfoPTMMap().isEmpty();
            }
        }
        return false; // should not be called 
    }

    @Override
    public boolean fetchData() {
        try {
        switch (m_action) {
            case LOAD_PTMDATASET: {
                if (needToFetch()) {
                    return fetchPTMDatasetMainTask();
                } else {
                    // fetch data of SubTasks
                    return fetchDataSubTaskFor();
                }
            }
            case FILL_ALL_PTM_SITES_PEPINFO: {
                if (needToFetch()) {
                    return fetchAllPTMSitesPeptideMatches();
                } else // fetch data of SubTasks
                {
                    return fetchDataSubTaskFor();
                }
            }
            case FILL_ALL_PTM_INFO: {
                if (needToFetch()) {
                    return fetchPTMInfo();
                }
            }
        }
        } finally {
            PerformanceTest.displayTimeAllThreads();
        }
        return true; // should not happen                
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
                case SUB_TASK_PTMSITE_PEPTIDES:
                    fetchSiteBestPeptideMatches(slice, entityManagerMSI);
                    break;
                case SUB_TASK_PTMCLUSTER_PEPTIDES:
                    fetchClusterBestPeptideMatches(slice, entityManagerMSI);
                    break;
                case SUB_TASK_PTMSITE_PEPINSTANCES:
                    fetchPTMSitesPepInstances(slice, entityManagerMSI);
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

    private boolean fetchPTMInfo() {
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            this.fetchGenericPTMData(entityManagerMSI);
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

    // Entry point for this Database tasks. 
    private boolean fetchPTMDatasetMainTask() {
        
        PerformanceTest.startTime("fetchPTMDatasetMainTask");
        
        long start = System.currentTimeMillis();
        //m_logger.debug(" START fetchPTMDatasetMainTask task ");
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            //--- Read PTM data in object tree associated to rsm
            ResultSummary rsm = entityManagerMSI.find(ResultSummary.class, m_dataset.getResultSummaryId());
            if (rsm.getObjectTreeIdByName().isEmpty() || rsm.getObjectTreeIdByName().get("result_summary.ptm_dataset") == null) {
                throw new RuntimeException(" PTM Identification (v2) has not been run on this dataset.");
            }
            ObjectTree ot = entityManagerMSI.find(ObjectTree.class, rsm.getObjectTreeIdByName().get("result_summary.ptm_dataset"));
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            JSONPTMDataset jsonDS = mapper.readValue(ot.getClobData(), JSONPTMDataset.class);

            //--- Create PTMDataset
            m_ptmDataset = new PTMDataset(m_dataset);
            m_ptmDataset.setIsVersion2(true);
            m_ptmDataset.setLeafResultSummaryIds(Arrays.asList(jsonDS.leafResultSummaryIds));

            m_ptmDatasetOutput.add(m_ptmDataset);
            //** Read and create PTMSite
            createPTMDatasetPTMSites(jsonDS, entityManagerMSI);
            for (Long i : jsonDS.ptmIds) {
                m_ptmDataset.addInfoPTM(DInfoPTM.getInfoPTMMap().get(i));
            }
            if (!m_loadSitesAsClusters) {
                //** Read and create PTMCluster
                createPTMDatasetPTMCluster(jsonDS, entityManagerMSI);
            } else {
                createPTMDatasetPTMCluster(entityManagerMSI);
            }

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
        long end = System.currentTimeMillis();
        //m_logger.debug(" END fetchPTMDatasetMainTask task "+(end-start)+" ms");
        
        PerformanceTest.stopTime("fetchPTMDatasetMainTask");
        
        
        return true;
    }

    /*
     * Private method called to create PTMSites from read JSON properties 
     * @param ptmDS 
     */
    private boolean createPTMDatasetPTMSites(JSONPTMDataset jsonDataset, EntityManager entityManagerMSI) throws Exception {
        //Read PTMSite V2      
        
        PerformanceTest.startTime("createPTMDatasetPTMSites");
        
        List<PTMSite> ptmSites = new ArrayList<>();
        long start = System.currentTimeMillis();

        //---- Get associated Parent RSM  ProteinMatches
        Long rsmId = m_ptmDataset.getDataset().getResultSummaryId();
        TypedQuery<DProteinMatch> typicalProteinQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinMatch(pm.id, pm.accession, pm.score, pm.peptideCount, pm.resultSet.id, pm.description, pm.serializedProperties, pepset.id, pepset.score, pepset.sequenceCount, pepset.peptideCount, pepset.peptideMatchCount, pepset.resultSummaryId) "
                + "FROM PeptideSetProteinMatchMap pset_to_pm JOIN pset_to_pm.proteinMatch as pm JOIN pset_to_pm.peptideSet as pepset JOIN pepset.proteinSet as ps "
                + "WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true AND ps.representativeProteinMatchId=pm.id  ORDER BY pepset.score DESC", DProteinMatch.class);

        typicalProteinQuery.setParameter("rsmId", rsmId);
        List<DProteinMatch> typicalProteinMatchesArray = typicalProteinQuery.getResultList();
        long stop = System.currentTimeMillis();
        m_logger.debug(" ** {} typical ProtMatches and {} PTMSites loaded in {} ms", typicalProteinMatchesArray.size(), jsonDataset.ptmSites.length, (stop - start));
        start = stop;
        
        //--- Get Protein Match mapping between parent and leaf rsm
        List<String> accessions = typicalProteinMatchesArray.stream().map(DProteinMatch::getAccession).collect(Collectors.toList());
        Map<String, List<Long>> allProtMatchesIdPerAccession = new HashMap<>();
        int bufferSize = 10000;
        int readAccessionCount = 0;
        while(readAccessionCount < accessions.size()){
            
            PerformanceTest.startTime("STEP1");
            
            Query protMatchQuery = entityManagerMSI.createQuery("SELECT pm.id, pm.accession FROM ProteinMatch pm, ResultSummary rsm WHERE pm.accession IN (:accList) AND rsm.resultSet.id = pm.resultSet.id AND rsm.id in (:rsmIds) ");
            int lastIndex = ((readAccessionCount+bufferSize) > accessions.size()) ? accessions.size() : readAccessionCount+bufferSize;
            protMatchQuery.setParameter("accList", accessions.subList(readAccessionCount, lastIndex));
            protMatchQuery.setParameter("rsmIds", m_ptmDataset.getLeafResultSummaryIds());
                        
            Iterator<Object[]> proteinMatchesResult = protMatchQuery.getResultList().iterator();
            while(proteinMatchesResult.hasNext()){
                Object[] results = proteinMatchesResult.next();
                Long prMatchAcc = (Long) results[0];
                String acc = (String) results[1];
                if(!allProtMatchesIdPerAccession.containsKey(acc))
                    allProtMatchesIdPerAccession.put(acc, new ArrayList<Long>());
                allProtMatchesIdPerAccession.get(acc).add(prMatchAcc);
            }
            readAccessionCount = lastIndex;
            
            PerformanceTest.stopTime("STEP1");
        }
        m_ptmDataset.setLeafProtMatchesIdPerAccession(allProtMatchesIdPerAccession);
        stop = System.currentTimeMillis();
        m_logger.debug(" ** Creates ProtMatches map for leaf RSM, map size = {} ; loaded in {} ms", allProtMatchesIdPerAccession.size(),  (stop - start));
        start = stop;
        
        //----  fetch Generic PTM Data
        fetchGenericPTMData(entityManagerMSI);

        m_ptmSitesByBestPepMatchId = new HashMap<>();
        //---- Create the list of PTMSites            
        Map<Long, DProteinMatch> proteinMatchMap = typicalProteinMatchesArray.stream().collect(Collectors.toMap(item -> item.getId(), item -> item));
        for (JSONPTMSite2 jsonSite : jsonDataset.ptmSites) {
            PTMSite site = new PTMSite(jsonSite, proteinMatchMap.get(jsonSite.proteinMatchId));
            //site.setBestPeptideMatch(peptideMatchMap.get(jsonSite.bestPeptideMatchId));
            site.setPTMSpecificity(DInfoPTM.getInfoPTMMap().get(jsonSite.ptmDefinitionId));
            if (site.getProteinMatch() != null) {
                ptmSites.add(site);
            }

            if (!m_ptmSitesByBestPepMatchId.containsKey(jsonSite.bestPeptideMatchId)) {
                m_ptmSitesByBestPepMatchId.put(jsonSite.bestPeptideMatchId, new ArrayList<>());
            }
            m_ptmSitesByBestPepMatchId.get(jsonSite.bestPeptideMatchId).add(site);
        }
        stop = System.currentTimeMillis();
        m_logger.debug(" ** {} PTM Sites built in {} ms", ptmSites.size(), (stop - start));
        start = stop;
        m_ptmDataset.setPTMSites(ptmSites);

        
        PerformanceTest.stopTime("createPTMDatasetPTMSites");
        
        
        //---- Runs subtasks to get peptide matches
        //Get Best Peptide MatchesProteinMatches
        m_bestSitesPepMatchIds = Arrays.asList(jsonDataset.ptmSites).stream().map(site -> site.bestPeptideMatchId).distinct().collect(Collectors.toList());
        SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PTMSITE_PEPTIDES, m_bestSitesPepMatchIds.size(), SLICE_SIZE);
        fetchSiteBestPeptideMatches(subTask, entityManagerMSI); //get first slice            
        stop = System.currentTimeMillis();
        
        
        
        m_logger.debug(" ** First {} Best PSM + Peptide + Spectrum + Query from PepInstances created in {} ms", SLICE_SIZE, (stop - start));

        return true;
    }

    private boolean createPTMDatasetPTMCluster(JSONPTMDataset jsonDataset, EntityManager entityManagerMSI) throws Exception {
        long start = System.currentTimeMillis();
        
        PerformanceTest.startTime("createPTMDatasetPTMCluster");
        
        m_bestClustersPepMatchIds = new ArrayList<>();

        List<PTMCluster> allClusters = new ArrayList<>();
        m_ptmClustersByBestPepMatchId = new HashMap<>();
        for (JSONPTMCluster cluster : jsonDataset.ptmClusters) {
            PTMCluster ptmCluster = new PTMCluster(cluster, m_ptmDataset);
            if (ptmCluster.getPTMSites() == null || ptmCluster.getPTMSites().isEmpty()) {
                continue;
            }
            Long bestPepMatchID = cluster.bestPeptideMatchId;
            m_bestClustersPepMatchIds.add(bestPepMatchID);
            allClusters.add(ptmCluster);
            if (!m_ptmClustersByBestPepMatchId.containsKey(bestPepMatchID)) {
                m_ptmClustersByBestPepMatchId.put(bestPepMatchID, new ArrayList<>());
            }
            m_ptmClustersByBestPepMatchId.get(bestPepMatchID).add(ptmCluster);
        }
        m_ptmDataset.setPTMClusters(allClusters);
        
        PerformanceTest.stopTime("createPTMDatasetPTMCluster");
        
        long stop = System.currentTimeMillis();
        m_logger.debug(" -- Created {} Cluster in {} ms", allClusters.size(), (stop - start));
        start = stop;

        //---- Runs subtasks to get peptide matches
        //Get Best Peptide Matches 
        SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PTMCLUSTER_PEPTIDES, m_bestClustersPepMatchIds.size(), SLICE_SIZE);
        fetchClusterBestPeptideMatches(subTask, entityManagerMSI); //get first slice                    
        stop = System.currentTimeMillis();
        m_logger.debug(" -- First {} Best PSM + Peptide + Spectrum + Query from PepInstances created in {} ms", SLICE_SIZE, (stop - start));
        return true;
    }


    private boolean createPTMDatasetPTMCluster(EntityManager entityManagerMSI) throws Exception {

        long start = System.currentTimeMillis();
        m_bestClustersPepMatchIds = new ArrayList<>();

        PerformanceTest.startTime("createPTMDatasetPTMCluster");
        
        List<PTMCluster> allClusters = new ArrayList<>();
        m_ptmClustersByBestPepMatchId = new HashMap<>();
        for (PTMSite site: m_ptmDataset.getPTMSites()) {
            PTMCluster ptmCluster = new PTMCluster(site.getId(), site.getLocalisationConfidence(), Arrays.asList(site.getId()), site.getPeptideIds() , m_ptmDataset);
            if (ptmCluster.getPTMSites() == null || ptmCluster.getPTMSites().isEmpty()) {
                continue;
            }
            Long bestPepMatchID = site.getBestProbabilityPepMatchId();
            m_bestClustersPepMatchIds.add(bestPepMatchID);
            allClusters.add(ptmCluster);
            if (!m_ptmClustersByBestPepMatchId.containsKey(bestPepMatchID)) {
                m_ptmClustersByBestPepMatchId.put(bestPepMatchID, new ArrayList<>());
            }
            m_ptmClustersByBestPepMatchId.get(bestPepMatchID).add(ptmCluster);
        }
        m_ptmDataset.setPTMClusters(allClusters);
        
        PerformanceTest.stopTime("createPTMDatasetPTMCluster");
        
        long stop = System.currentTimeMillis();
        m_logger.debug(" -- Created {} Cluster in {} ms", allClusters.size(), (stop - start));
        start = stop;

        // TODO : try to simplify this since the same peptideMatches will already be loaded for sites, except that this
        // TODO: is done in a subtasks. When executing this method, the subtasks is not necessarily done.
        //---- Runs subtasks to get peptide matches
        SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PTMCLUSTER_PEPTIDES, m_bestClustersPepMatchIds.size(), SLICE_SIZE);
        fetchClusterBestPeptideMatches(subTask, entityManagerMSI); //get first slice
        stop = System.currentTimeMillis();
        m_logger.debug(" -- First {} Best PSM + Peptide + Spectrum + Query from PepInstances created in {} ms", SLICE_SIZE, (stop - start));
        return true;
    }

    private void fetchGenericPTMData(EntityManager entityManagerMSI) {

        PerformanceTest.startTime("fetchGenericPTMData");
        
        HashMap<Long, DInfoPTM> infoPTMMAp = DInfoPTM.getInfoPTMMap();
        if (!infoPTMMAp.isEmpty()) {
            PerformanceTest.stopTime("fetchGenericPTMData");
            return; // already loaded
        }

        TypedQuery<DInfoPTM> ptmInfoQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DInfoPTM(spec.id, spec.residue, spec.location, ptm.shortName, evidence.composition, evidence.monoMass) \n"
                + "FROM fr.proline.core.orm.msi.PtmSpecificity as spec, fr.proline.core.orm.msi.Ptm as ptm, fr.proline.core.orm.msi.PtmEvidence as evidence \n"
                + "WHERE spec.ptm=ptm AND ptm=evidence.ptm AND evidence.type='Precursor' ", DInfoPTM.class);
        List<DInfoPTM> ptmInfoList = ptmInfoQuery.getResultList();

        Iterator<DInfoPTM> it = ptmInfoList.iterator();
        while (it.hasNext()) {
            DInfoPTM infoPTM = it.next();
            DInfoPTM.addInfoPTM(infoPTM);
        }

        PerformanceTest.stopTime("fetchGenericPTMData");
    }
    
    // 
    private boolean fetchSiteBestPeptideMatches(SubTask subTask, EntityManager entityManagerMSI) {
        List<Long> sliceOfPeptideMatchIds = subTask.getSubList(m_bestSitesPepMatchIds);
        return fetchBestPeptideMatches(sliceOfPeptideMatchIds, entityManagerMSI, true);
    }

    private boolean fetchClusterBestPeptideMatches(SubTask subTask, EntityManager entityManagerMSI) {
        List<Long> sliceOfPeptideMatchIds = subTask.getSubList(m_bestClustersPepMatchIds);
        return fetchBestPeptideMatches(sliceOfPeptideMatchIds, entityManagerMSI, false);
    }

    /* Method called for subTask execution to get peptide Matches info
     */
    private boolean fetchBestPeptideMatches(List<Long> sliceOfPeptideMatchIds, EntityManager entityManagerMSI, boolean isSite) {
        long start = System.currentTimeMillis();

        PerformanceTest.startTime("fetchBestPeptideMatches");
        
        PerformanceTest.startTime("STEP1");
        
        Query peptidesQuery = entityManagerMSI.createQuery("SELECT pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.serializedProperties, sp.firstTime, sp.precursorIntensity, sp.title, p, ms.id, ms.initialId, ptmString\n"
                + "   FROM fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.PeptideReadablePtmString ptmString, fr.proline.core.orm.msi.MsQuery ms, fr.proline.core.orm.msi.Spectrum sp  \n"
                + "   WHERE pipm.id.peptideMatchId IN (:peptideMatchList) AND pipm.id.peptideInstanceId=pi.id AND pipm.id.peptideMatchId=pm.id AND pm.peptideId=p.id AND pm.msQuery=ms AND ms.spectrum=sp AND pi.resultSummary.id in (:rmsIds) AND ptmString.peptide=p AND ptmString.resultSet.id=pm.resultSet.id");
        peptidesQuery.setParameter("peptideMatchList", sliceOfPeptideMatchIds);
        List<Long> rsmIds = m_ptmDataset.getLeafResultSummaryIds();
        rsmIds.add(m_ptmDataset.getDataset().getResultSummaryId());
        peptidesQuery.setParameter("rmsIds", rsmIds);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        List l = peptidesQuery.getResultList();
        Iterator<Object[]> itPeptidesQuery = l.iterator();
        List<DPeptideMatch> allPepMatches = new ArrayList<>();
        //Create List of DPeptideMatch (linked to DSpectrum + DMsQuery) from query resumlt 
        
        PerformanceTest.stopTime("STEP1");
        
        
        
        while (itPeptidesQuery.hasNext()) {
            
            PerformanceTest.startTime("STEP2");
            
            Object[] resCur = itPeptidesQuery.next();

            Long pmId = (Long) resCur[0];
            Integer pmRank = (Integer) resCur[1];
            Integer pmCharge = (Integer) resCur[2];
            Float pmDeltaMoz = (Float) resCur[3];
            Double pmExperimentalMoz = (Double) resCur[4];
            Integer pmMissedCleavage = (Integer) resCur[5];
            Float pmScore = (Float) resCur[6];
            Long pmResultSetId = (Long) resCur[7];

            Integer pmCdPrettyRank = (Integer) resCur[8];
            Integer pmSdPrettyRank = (Integer) resCur[9];
            String serializedProperties = (String) resCur[10];
            Float firstTime = (Float) resCur[11];
            Float precursorIntensity = (Float) resCur[12];
            String title = (String) resCur[13];

            DSpectrum spectrum = new DSpectrum();
            spectrum.setFirstTime(firstTime);
            spectrum.setPrecursorIntensity(precursorIntensity);
            spectrum.setTitle(title);

            PerformanceTest.stopTime("STEP2");
            
            PerformanceTest.startTime("STEP3");
            
            DPeptideMatch pm = new DPeptideMatch(pmId, pmRank, pmCharge, pmDeltaMoz, pmExperimentalMoz, pmMissedCleavage, pmScore, pmResultSetId, pmCdPrettyRank, pmSdPrettyRank);
            pm.setRetentionTime(firstTime);
            pm.setSerializedProperties(serializedProperties);

            try {
                JsonNode node = mapper.readTree(serializedProperties);
                JsonNode child = node.get("ptm_site_properties");
                DPtmSiteProperties properties = mapper.treeToValue(child, DPtmSiteProperties.class);
                pm.setPtmSiteProperties(properties);
            } catch (IOException ex) {
                //VDS FIXME : If no PTMSite properties, do we keep the peptide?
                Exceptions.printStackTrace(ex);
            }
            
            PerformanceTest.stopTime("STEP3");
            
            PerformanceTest.startTime("STEP4");

            Peptide p = (Peptide) resCur[14];
            Long msqId = (Long) resCur[15];
            Integer msqInitialId = (Integer) resCur[16];
            PeptideReadablePtmString ptmString = (PeptideReadablePtmString) resCur[17];
            p.getTransientData().setPeptideReadablePtmString(ptmString);
            p.getTransientData().setPeptideReadablePtmStringLoaded();

            DMsQuery msq = new DMsQuery(pmId, msqId, msqInitialId, precursorIntensity);
            msq.setDSpectrum(spectrum);

            pm.setPeptide(p);
            pm.setMsQuery(msq);

            PerformanceTest.stopTime("STEP4");
            
            PerformanceTest.startTime("STEP5");
            
            if (isSite) {
                List<PTMSite> sites = m_ptmSitesByBestPepMatchId.get(pmId);
                if (sites != null && !sites.isEmpty()) {
                    sites.stream().forEach(site -> {
                        PTMSite finalSite = m_ptmDataset.getPTMSite(site.getId());
                        if (finalSite != null) {
                            finalSite.setBestProbabilityPepMatch(pm);
                        }
                    });
                }
            } else {
                List<PTMCluster> clusters = m_ptmClustersByBestPepMatchId.get(pmId);
                if (clusters != null && !clusters.isEmpty()) {
                    clusters.stream().forEach(cluster -> {
                        cluster.setRepresentativePepMatch(pm);
                    });
                }
            }
            allPepMatches.add(pm);
            
            PerformanceTest.stopTime("STEP5");
        }

        long stop = System.currentTimeMillis();
        m_logger.debug("  --- {} PeptideMatches loaded in {} ms", sliceOfPeptideMatchIds.size(), (stop - start));
        start = stop;

        PerformanceTest.startTime("STEP6");
            
        
        //--  fetch Specific PTM Data for the Peptides Found
        List<Long> pepId = allPepMatches.stream().map(peM -> peM.getPeptide().getId()).collect(Collectors.<Long>toList());

        HashMap<Long, ArrayList<DPeptidePTM>> ptmMap = fetchPeptidePTMForPeptides(entityManagerMSI, new ArrayList<>(pepId));
        for (DPeptideMatch pm : allPepMatches) {
            Peptide p = pm.getPeptide();
            Long idPeptide = p.getId();
            ArrayList<DPeptidePTM> ptmList = ptmMap.get(idPeptide);
            pm.setPeptidePTMArray(ptmList);

            HashMap<Integer, DPeptidePTM> mapToPtm = new HashMap<>();
            if (ptmList != null) {
                for (DPeptidePTM peptidePTM : ptmList) {
                    mapToPtm.put((int) peptidePTM.getSeqPosition(), peptidePTM);
                }
                p.getTransientData().setDPeptidePtmMap(mapToPtm);
            }
        }

        PerformanceTest.stopTime("STEP6");
        
        stop = System.currentTimeMillis();
        m_logger.debug(" --- Associated PTM data loaded in {} ms", (stop - start));
        
        PerformanceTest.stopTime("fetchBestPeptideMatches");
        
        
        return true;
    }

    private HashMap<Long, ArrayList<DPeptidePTM>> fetchPeptidePTMForPeptides(EntityManager entityManagerMSI, ArrayList<Long> allPeptidesIds) {

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
            ArrayList<DPeptidePTM> list = ptmMap.get(peptideId);
            if (list == null) {
                list = new ArrayList<>();
                ptmMap.put(peptideId, list);
            }
            list.add(ptm);

        }
        
        PerformanceTest.stopTime("fetchPeptidePTMForPeptides");
        

        return ptmMap;
    }

    private boolean fetchAllPTMSitesPeptideMatches() {

        PerformanceTest.startTime("fetchAllPTMSitesPeptideMatches");
        
        long start = System.currentTimeMillis();
        m_logger.debug(" START fetchAllPTMSitesPeptideMatches task ");
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            //*** Get Best Peptide MatchesProteinMatches            
            Set<Long> allPepIds = m_ptmSitesOutput.stream().flatMap(s -> s.getPeptideIds().stream()).collect(Collectors.toSet());
            m_peptideIds = new ArrayList<>();
            m_peptideIds.addAll(allPepIds);

            SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PTMSITE_PEPINSTANCES, m_peptideIds.size(), SLICE_SIZE);
            fetchPTMSitesPepInstances(subTask, entityManagerMSI);

            long stop = System.currentTimeMillis();
            m_logger.debug(" fetchAllPTMSitesPeptideMatches :{} of {} PTM Sites filled in {} ms", SLICE_SIZE, m_ptmSitesOutput.size(), (stop - start));

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

        PerformanceTest.stopTime("fetchAllPTMSitesPeptideMatches");
        
        
        return true;
    }

    private boolean fetchPTMSitesPepInstances(SubTask subTask, EntityManager entityManagerMSI) throws IOException {
        List<Long> sliceOfPeptideIds = subTask.getSubList(m_peptideIds);
        return fetchPTMSitesPepInstances(sliceOfPeptideIds, entityManagerMSI);
    }

    private boolean fetchPTMSitesPepInstances(List<Long> sliceOfPeptideIds, EntityManager entityManagerMSI) throws IOException {
        
        PerformanceTest.startTime("fetchPTMSitesPepInstances");
        
        long start = System.currentTimeMillis();
        m_logger.debug(" @@ START fetchPTMSitesPepInstances SUBTASK ");
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        
        int nbrPepIds = sliceOfPeptideIds.size();
        Long rsetId = m_ptmDataset.getDataset().getResultSetId();
        Long dsRsmId = m_ptmDataset.getDataset().getResultSummaryId();
        List<Long> allRsmIds = m_ptmDataset.getLeafResultSummaryIds();
        boolean datasetIsLeaf = (allRsmIds.size() == 1 && m_ptmDataset.getLeafResultSummaryIds().get(0).equals(dsRsmId));

        //Test if dataset is also the leaf dataset
        if(!datasetIsLeaf){
            allRsmIds.add(dsRsmId);
        }

        PerformanceTest.startTime("peptidesQuery");
        
        Query peptidesQuery = entityManagerMSI.createQuery("SELECT pi.id FROM fr.proline.core.orm.msi.PeptideInstance pi"
                + "   WHERE pi.peptide.id IN (:peptideIdsList) AND pi.resultSummary.id in (:rmsIds)");
        peptidesQuery.setParameter("peptideIdsList", sliceOfPeptideIds);
        peptidesQuery.setParameter("rmsIds", allRsmIds);

        Iterator<Long> itPeptidesQuery = peptidesQuery.getResultList().iterator();
        List<Long> peptideInstanceIds = new ArrayList<>();
        while (itPeptidesQuery.hasNext()) {
            Long resCur = itPeptidesQuery.next();
            peptideInstanceIds.add(resCur);
        }
        
        int nbrPepInstIds = peptideInstanceIds.size();
        
        PerformanceTest.stopTime("peptidesQuery");
        
        long end = System.currentTimeMillis();
        m_logger.debug(" @@ fetchPTMSitesPepInstances: Read "+nbrPepInstIds+" pep instances corresponding to " + nbrPepIds+" peptides in "+(end-start)+" ms" );
        start = end;
        
        PerformanceTest.startTime("dataQuery");
        
        HashMap<Long, Peptide> allPeptidesMap = new HashMap();
        HashMap<Long, DPeptideInstance> leafPeptideInstancesById = new HashMap<>();
        HashMap<Long, List<DPeptideInstance>> leafPeptideInstancesByPepId = new HashMap<>();
        Map<Long, DPeptideInstance> parentPeptideInstancesByPepId = new HashMap<>();

        //---- Load Peptide Match + Spectrum / MSQuery information for all peptideInstance of PTMSite
        Query dataQuery = entityManagerMSI.createQuery("SELECT pm, pi, sm, sp.firstTime, sp.precursorIntensity, sp.title, "
                + " msq.id, msq.initialId "
                + "  FROM fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstance pi, "
                + " fr.proline.core.orm.msi.SequenceMatch sm , fr.proline.core.orm.msi.MsQuery msq , fr.proline.core.orm.msi.Spectrum sp "
                + "  WHERE pipm.id.peptideInstanceId IN ( :peptideInstanceList ) AND pipm.id.peptideInstanceId=pi.id AND pipm.id.peptideMatchId=pm.id "
                + " AND sm.id.peptideId = pi.peptide.id  AND sm.resultSetId = pm.resultSet.id AND sm.id.peptideId=pm.peptideId AND pm.msQuery.id = msq.id AND msq.spectrum.id = sp.id ");
        dataQuery.setParameter("peptideInstanceList", new ArrayList<>(peptideInstanceIds));
        List l = dataQuery.getResultList();
        Iterator<Object[]> itPeptidesMatchesQuery = l.iterator();        
        
        PerformanceTest.stopTime("dataQuery");
        
        //---- Create List of DPeptideMatch (linked to DSpectrum + DMsQuery) from query resumlt
        end = System.currentTimeMillis();
        m_logger.debug(" @@ fetchPTMSitesPepInstances: query result size " + l.size()+" in "+(end-start)+" ms" );
        start= end;
        
        PerformanceTest.startTime("itPeptidesMatchesQuery loop");
        
        while (itPeptidesMatchesQuery.hasNext()) {

            
            PerformanceTest.startTime("STEP1");
            
            Object[] resCur = itPeptidesMatchesQuery.next();

            PeptideMatch pm = (PeptideMatch) resCur[0];
            PeptideInstance pi = (PeptideInstance) resCur[1];
            SequenceMatch sm =  (SequenceMatch) resCur[2];
            Float firstTime = (Float) resCur[3];
            Float precursorIntensity = (Float) resCur[4];
            String spectrumTitle = (String) resCur[5];
            Long msQueryId = (Long) resCur[6];
            Integer msQueryInitialId = (Integer) resCur[7];
            

            PerformanceTest.stopTime("STEP1");
            
            PerformanceTest.startTime("STEP2");
            
            DSpectrum spectrum = new DSpectrum();
            spectrum.setFirstTime(firstTime);
            spectrum.setPrecursorIntensity(precursorIntensity);
            spectrum.setTitle(spectrumTitle);

            PerformanceTest.stopTime("STEP2");
            
            PerformanceTest.startTime("STEP3");
            
            DPeptideMatch dpm = new DPeptideMatch(pm.getId(), pm.getRank(), pm.getCharge(), pm.getDeltaMoz(), pm.getExperimentalMoz(), pm.getMissedCleavage(), pm.getScore(), pm.getResultSet().getId(), pm.getCDPrettyRank(), pm.getSDPrettyRank());
            dpm.setRetentionTime(spectrum.getFirstTime());
            dpm.setSerializedProperties(pm.getSerializedProperties());
            dpm.setSequenceMatch(sm);

            PerformanceTest.stopTime("STEP3");
            
            PerformanceTest.startTime("STEP4");
            JsonNode node = mapper.readTree(dpm.getSerializedProperties());
            JsonNode child = node.get("ptm_site_properties");
            DPtmSiteProperties properties = mapper.treeToValue(child, DPtmSiteProperties.class);
            dpm.setPtmSiteProperties(properties);
            PerformanceTest.stopTime("STEP4");

            
            
            
            PerformanceTest.startTime("STEP5");
            Peptide p = (Peptide) pi.getPeptide();
            p.getTransientData().setPeptideReadablePtmStringLoaded();
            allPeptidesMap.put(p.getId(), p);

            DMsQuery msq = new DMsQuery(pm.getId(), msQueryId, msQueryInitialId, spectrum.getPrecursorIntensity());
            msq.setDSpectrum(spectrum);

            dpm.setPeptide(p);
            dpm.setMsQuery(msq);

            PerformanceTest.stopTime("STEP5");

            
            PerformanceTest.startTime("STEP6");
            
            //TEST IF Leaf or Parent 
            if(dsRsmId.equals(pi.getResultSummary().getId()) ) {
                //PARENT PepInstance
                if(!parentPeptideInstancesByPepId.containsKey(p.getId())){                 
                    DPeptideInstance dpi = new DPeptideInstance(pi.getId(), pi.getPeptide().getId(), pi.getValidatedProteinSetCount(), pi.getElutionTime());
                    dpi.setResultSummary(pi.getResultSummary());
                    dpi.setPeptide(p);
                    dpi.setPeptideMatches(new ArrayList<>());
                    parentPeptideInstancesByPepId.put(p.getId(), dpi);
                }
                
                if (pi.getBestPeptideMatchId() == dpm.getId()) {
                    parentPeptideInstancesByPepId.get(p.getId()).setBestPeptideMatch(dpm);
                }
                parentPeptideInstancesByPepId.get(p.getId()).getPeptideMatches().add(dpm);
                
            }
            if( (!dsRsmId.equals(pi.getResultSummary().getId())) || datasetIsLeaf ) {
                //LEAF PeptideInstance
                if (!leafPeptideInstancesByPepId.containsKey(p.getId())) {
                    leafPeptideInstancesByPepId.put(p.getId(), new ArrayList<>());
                }
                List<DPeptideInstance> pepInsts = leafPeptideInstancesByPepId.get(p.getId());
                boolean alreadyread = false;
                for (DPeptideInstance readPepI : pepInsts) {
                    if (readPepI.getId() == pi.getId()) {
                        alreadyread = true;
                    }
                }
                if (!alreadyread) {
                    DPeptideInstance dpi = new DPeptideInstance(pi.getId(), pi.getPeptide().getId(), pi.getValidatedProteinSetCount(), pi.getElutionTime());
                    dpi.setResultSummary(pi.getResultSummary());
                    dpi.setPeptide(p);
                    dpi.setPeptideMatches(new ArrayList<>());
                    leafPeptideInstancesById.put(dpi.getId(), dpi);
                    pepInsts.add(dpi);
                }

                if (pi.getBestPeptideMatchId() == dpm.getId()) {
                    leafPeptideInstancesById.get(pi.getId()).setBestPeptideMatch(dpm);
                }
                leafPeptideInstancesById.get(pi.getId()).getPeptideMatches().add(dpm);
            }
            
            PerformanceTest.stopTime("STEP6");
        }
        
        
        
        PerformanceTest.stopTime("itPeptidesMatchesQuery loop");
        
        end = System.currentTimeMillis();
        m_logger.debug(" @@ fetchPTMSitesPepInstances: Created leafPeptideInstancesById. Nbr : " + leafPeptideInstancesById.size()+" in "+(end-start)+" ms");
        start= end;
              
        //--- Retrieve PeptideReadablePtmString
        
        PerformanceTest.startTime("ptmStingQuery");
        
        Set<Long> allPeptides = allPeptidesMap.keySet();
        Query ptmStingQuery = entityManagerMSI.createQuery("SELECT p.id, ptmString FROM fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.PeptideReadablePtmString ptmString WHERE p.id IN (:listId) AND ptmString.peptide=p AND ptmString.resultSet.id=:rsetId");
        ptmStingQuery.setParameter("listId", allPeptides);
        ptmStingQuery.setParameter("rsetId", rsetId);
        List<Object[]> ptmStrings = ptmStingQuery.getResultList();
        Iterator<Object[]> ito = ptmStrings.iterator();
        while (ito.hasNext()) {
            Object[] res = ito.next();
            Long peptideId = (Long) res[0];
            PeptideReadablePtmString ptmString = (PeptideReadablePtmString) res[1];
            Peptide peptide = allPeptidesMap.get(peptideId);
            peptide.getTransientData().setPeptideReadablePtmString(ptmString);
        }

        PerformanceTest.stopTime("ptmStingQuery");
        
        PerformanceTest.startTime("fetchGenericPTMData");
        
        // fetch Generic PTM Data
        fetchGenericPTMData(entityManagerMSI);
        
        PerformanceTest.stopTime("fetchGenericPTMData");

        
        PerformanceTest.startTime("fetchPeptidePTMForPeptides");
        
        //--- fetch Specific Data for the Peptides Found
        HashMap<Long, ArrayList<DPeptidePTM>> ptmMap = fetchPeptidePTMForPeptides(entityManagerMSI, new ArrayList(allPeptidesMap.keySet()));

        PerformanceTest.stopTime("fetchPeptidePTMForPeptides");
        
         PerformanceTest.startTime("parentPeptideInstancesByPepId.values().stream().flatMap");
        
        List<DPeptideMatch> allpeptideMatches = leafPeptideInstancesById.values().stream().flatMap(pi -> pi.getPeptideMatches().stream()).collect(Collectors.toList());
        allpeptideMatches.addAll(parentPeptideInstancesByPepId.values().stream().flatMap(pi -> pi.getPeptideMatches().stream()).collect(Collectors.toList()));
        
        PerformanceTest.stopTime("parentPeptideInstancesByPepId.values().stream().flatMap");
        
        end = System.currentTimeMillis();        
        m_logger.debug(" @@ fetchPTMSitesPepInstances got PTM info  for all peptideMatches . nbr " + allpeptideMatches.size()+" in "+(end-start)+" ms");
        start= end;
        
        PerformanceTest.startTime("allpeptideMatches loop");
        
        for (DPeptideMatch pm : allpeptideMatches) {
            Peptide p = pm.getPeptide();
            Long idPeptide = p.getId();
            ArrayList<DPeptidePTM> ptmList = ptmMap.get(idPeptide);
            pm.setPeptidePTMArray(ptmList);

            HashMap<Integer, DPeptidePTM> mapToPtm = new HashMap<>();
            if (ptmList != null) {
                for (DPeptidePTM peptidePTM : ptmList) {
                    mapToPtm.put((int) peptidePTM.getSeqPosition(), peptidePTM);
                }
                p.getTransientData().setDPeptidePtmMap(mapToPtm);
            }
        }
        
        PerformanceTest.stopTime("allpeptideMatches loop");

        m_logger.info(" @@ {} peptides matching to {} peptide matches retrieved", allPeptides.size(), allpeptideMatches.size());
        m_logger.info(" @@ {} peptide instances retrieved", parentPeptideInstancesByPepId.size());
        
        PerformanceTest.startTime("m_ptmSitesOutput loop");
        for (PTMSite site : m_ptmSitesOutput) {
            //Verify if PTM site has at least one pep in this 'subtask'
            List<DPeptideInstance> leafPeptideInstances = new ArrayList<>();
            Set<DPeptideInstance> parentPeptideInstancesAsSet = new HashSet<>();
            for (Long pepId : site.getPeptideIds()) {
                if (leafPeptideInstancesByPepId.containsKey(pepId)) {
                    leafPeptideInstances.addAll(leafPeptideInstancesByPepId.get(pepId));
                }
                if (parentPeptideInstancesByPepId.containsKey(pepId)) {
                    parentPeptideInstancesAsSet.add(parentPeptideInstancesByPepId.get(pepId));
                }
            }
            if (!leafPeptideInstances.isEmpty() || !parentPeptideInstancesAsSet.isEmpty()) {
                List<DPeptideInstance> parentPeptideInstances = new ArrayList<>();
                parentPeptideInstances.addAll(parentPeptideInstancesAsSet);
                site.addPeptideInstances(parentPeptideInstances, leafPeptideInstances);
            }
        }
        
        PerformanceTest.stopTime("m_ptmSitesOutput loop");
        
        end = System.currentTimeMillis(); 
        m_logger.debug(" @@ END fetchPTMSitesPepInstances subtask "+" in "+(end-start)+" ms");
        
        PerformanceTest.stopTime("fetchPTMSitesPepInstances");
        
        return true;
    }

}
