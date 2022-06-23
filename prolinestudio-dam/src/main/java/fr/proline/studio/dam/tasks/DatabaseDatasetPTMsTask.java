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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.msi.dto.*;
import fr.proline.core.orm.msi.repository.ObjectTreeSchemaRepository;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.Exceptions;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.data.ptm.*;
import fr.proline.studio.performancetest.PerformanceTest;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author JM235353
 */
public class DatabaseDatasetPTMsTask extends AbstractDatabaseSlicerTask {

    private long m_projectId = -1;
    private DDataset m_dataset = null;
    private PTMDatasetPair m_ptmDatasetPair = null;

    private PTMDataset m_clusterPtmDataset = null;
    private PTMDataset m_sitePtmDataset = null;

    private boolean m_loadAnnotatedDataset;

    //attributes to initialize when data is retrieve
    private List<PTMDatasetPair> m_ptmDatasetPairOutput = null;
    private List<PTMSite> m_ptmSitesOutput = null;

    public static final int NO_SUB_TASK_COUNT = 0;

    public static final int SUB_TASK_PTMSITE_PEPTIDES = 0;
    public static final int SUB_TASK_PTMCLUSTER_PEPTIDES = 1;
    public static final int SUB_TASK_COUNT_PTMDATASET = 2; // <<----- get in sync


    public static final int SUB_TASK_PTMSITE_PEPINSTANCES = 0;
    public static final int SUB_TASK_COUNT_PTMSITE_PEPINSTANCES = 1;// <<----- get in sync

    final int SLICE_SIZE = 1000;

    // data kept for sub tasks
    private List<Long> m_bestSitesPepMatchIds = null;
    private HashMap<Long, List<Long>> m_ptmSiteIdsByBestPepMatchId = null;
    private List<Long> m_bestClustersPepMatchIds = null;
    private HashMap<Long, List<PTMCluster>> m_ptmClustersByBestPepMatchId = null;
    private List<Long> m_peptideIds = null;

    private int m_action;
    private final static int LOAD_PTMDATASETPAIR = 0;
    private final static int FILL_ALL_PTM_SITES_PEPINFO = 1;
    private final static int SAVE_PTMDATASET = 2;

    public final static String ERROR_PTM_CLUSTER_LOADING = "PTM Cluster Loading Error";
    public final static String ERROR_PTM_CLUSTER_SAVING = "Error Saving Annotated PTM Dataset";

    public DatabaseDatasetPTMsTask(AbstractDatabaseCallback callback) {
        super(callback);
    }


    /**
     * Read PTMDataset information from datastore (JSON...): create both object PTMDataset with cluster and PTMDataset for site only
     * @param projectId : Id of the project the dataset belongs to
     * @param dataset : dataset to get PTMDatasets for
     * @param ptmDatasetset output list where to store created data
     */
    public void initLoadPTMDataset(Long projectId, DDataset dataset, List<PTMDatasetPair> ptmDatasetset, boolean loadAnnotatedDataset) {
        init(SUB_TASK_COUNT_PTMDATASET, new TaskInfo("Load PTM Dataset for " + dataset.getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_ptmDatasetPairOutput = ptmDatasetset;
        m_dataset = dataset;
        m_loadAnnotatedDataset = loadAnnotatedDataset;
        m_action = LOAD_PTMDATASETPAIR;
    }

    /**
     * Read PtmSite's peptide information from datastore and register in PTMSite object
     * @param projectId : Project associated dataset belongs to
     * @param ptmDSPair : PTMDatasets containing generic information (dataset and result summary info)
     * @param ptmSitesToFill : PTMSite to fill with peptide info
     */
    public void initFillPTMSites(long projectId, PTMDatasetPair ptmDSPair, List<PTMSite> ptmSitesToFill) {
        init(SUB_TASK_COUNT_PTMSITE_PEPINSTANCES, new TaskInfo("Load peptides of PTM Sites", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_ptmSitesOutput = ptmSitesToFill;
        m_ptmDatasetPair = ptmDSPair;
        m_dataset = m_ptmDatasetPair.getDataset();
        m_action = FILL_ALL_PTM_SITES_PEPINFO;
    }

    /**
     * Add annotated PTMDataset information in datastore : create new property object to resultsummary
     * @param projectId : Id of the project the dataset belongs to

     * @param ptmDataset PTMDataset to save
     */
    public void initAddAnnotatedPTMDataset(Long projectId, PTMDataset ptmDataset) {
        init(NO_SUB_TASK_COUNT, new TaskInfo("Save PTM Dataset for " + ptmDataset.getDataset().getName(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_clusterPtmDataset = ptmDataset;
        m_dataset = ptmDataset.getDataset();
        m_action = SAVE_PTMDATASET;
    }

    @Override
    public boolean needToFetch() {
        switch (m_action) {
            case FILL_ALL_PTM_SITES_PEPINFO:
                return (m_peptideIds == null || m_peptideIds.isEmpty());
            case LOAD_PTMDATASETPAIR: {
                return (m_ptmDatasetPairOutput == null || m_ptmDatasetPairOutput.isEmpty() );
            }
            case SAVE_PTMDATASET:
                return  true;
       }
        return false; // should not be called 
    }

    @Override
    public boolean fetchData() {
        try {
        switch (m_action) {

            case LOAD_PTMDATASETPAIR: {
                if (needToFetch()) {
                    return fetchPTMDatasetSetMainTask();
                } else {
                    // fetch data of SubTasks
                    return fetchDataSubTaskFor(LOAD_PTMDATASETPAIR);
                }
                }
            case FILL_ALL_PTM_SITES_PEPINFO: {
                if (needToFetch()) {
                    return fetchAllPTMSitesPeptideMatches();
                } else // fetch data of SubTasks
                {
                    return fetchDataSubTaskFor(FILL_ALL_PTM_SITES_PEPINFO);
                }
            }
            case SAVE_PTMDATASET:{
                return savePTMDataset();
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
    private boolean fetchDataSubTaskFor(int action) {
        SubTask slice = m_subTaskManager.getNextSubTask();
        if (slice == null) {
            return true; // nothing to do : should not happen
        }

        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {
            entityManagerMSI.getTransaction().begin();
            switch (action) {
                case LOAD_PTMDATASETPAIR:
                    switch (slice.getSubTaskId()) {
                        case SUB_TASK_PTMSITE_PEPTIDES:
                            fetchSiteBestPeptideMatches(slice, entityManagerMSI);
                            break;
                        case SUB_TASK_PTMCLUSTER_PEPTIDES:
                            fetchClusterBestPeptideMatches(slice, entityManagerMSI);
                            break;
                    }
                    break;
                case FILL_ALL_PTM_SITES_PEPINFO:
                    switch (slice.getSubTaskId()) {
                        case SUB_TASK_PTMSITE_PEPINSTANCES:
                            fetchPTMSitesPepInstances(slice, entityManagerMSI);
                            break;
                    }
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

    //Entry point for LOAD_PTMDATASETSET action
    private boolean fetchPTMDatasetSetMainTask() {

        PerformanceTest.startTime("fetchPTMDatasetSetMainTask");
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();

        try {

            entityManagerMSI.getTransaction().begin();
            JSONPTMDataset jsonDS =  readJSONPTMDataset(entityManagerMSI);

            //--- Create cluster PTMDataset
            m_clusterPtmDataset = new PTMDataset(m_dataset);
            m_clusterPtmDataset.setLeafResultSummaryIds(Arrays.asList(jsonDS.leafResultSummaryIds));

            //--- Create site PTMDataset
            m_sitePtmDataset = new PTMDataset(m_dataset);
            m_sitePtmDataset.setLeafResultSummaryIds(Arrays.asList(jsonDS.leafResultSummaryIds));

            m_ptmDatasetPairOutput.add(new PTMDatasetPair(m_sitePtmDataset,m_clusterPtmDataset));

            //** Read and create PTMSite / PTMCluster
            createPTMDatasetPTMSites(jsonDS, entityManagerMSI);

            for (Long i : jsonDS.ptmIds) {
                List<DInfoPTM> ptmSpecificities = DInfoPTM.getInfoPTMForPTM(i);
                if(ptmSpecificities.size()>0){
                    ptmSpecificities.forEach(dInfoPTM -> {
                        m_clusterPtmDataset.addInfoPTM(dInfoPTM);
                        m_sitePtmDataset.addInfoPTM(dInfoPTM);
                    });
                }
            }
            createPTMDatasetPTMClusters(jsonDS, entityManagerMSI);


        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            if (m_taskError == null) {
                // we do not replace an error already set.
                m_taskError = new TaskError(e);
            }
            try {
                entityManagerMSI.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerMSI.close();
        }

        PerformanceTest.stopTime("fetchPTMDatasetMainTask");


        return true;
    }

    //Entry point for FILL_ALL_PTM_SITES_PEPINFO action
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

    //ENTRY POINT for SAVE_PTMDATASET action
    private boolean savePTMDataset(){
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();

        try {

            ResultSummary rsm = entityManagerMSI.find(ResultSummary.class, m_dataset.getResultSummaryId());
            if(rsm ==null){
                m_taskError = new TaskError(ERROR_PTM_CLUSTER_SAVING," Unable to save PTMDataset for dataset "+m_clusterPtmDataset.getDataset().getName()+", No identification Summary attached");
                return  false;
            }
            entityManagerMSI.getTransaction().begin();
            if (rsm.getObjectTreeIdByName() != null && rsm.getObjectTreeIdByName().get("result_summary.ptm_dataset_annotated") != null) {
                //remove previous object tree
                Long obId = rsm.getObjectTreeIdByName().get("result_summary.ptm_dataset_annotated");
                rsm.removeObject("result_summary.ptm_dataset_annotated");
//                entityManagerMSI.createNativeQuery("ALTER TABLE object_tree DISABLE TRIGGER ALL;").executeUpdate();
                entityManagerMSI.createNativeQuery("DELETE FROM result_summary_object_tree_map WHERE object_tree_id = " + obId + " AND schema_name = 'result_summary.ptm_dataset_annotated'; ").executeUpdate();
                entityManagerMSI.createNativeQuery("DELETE FROM object_tree WHERE id = " + obId +"; ").executeUpdate();
            }



            JSONPTMDataset jsonPTMDataset = m_clusterPtmDataset.createJSONPTMDataset();
            Gson gson = new GsonBuilder().serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
            String jsonString = gson.toJson(jsonPTMDataset);

            ObjectTree newPTMDataset = new ObjectTree() ;
            newPTMDataset.setSchema(ObjectTreeSchemaRepository.loadOrCreateObjectTreeSchema(entityManagerMSI, ObjectTreeSchema.SchemaName.PTM_DATASET_ANNOTATED.toString()));
            newPTMDataset.setClobData(jsonString);
            entityManagerMSI.persist(newPTMDataset);

            rsm.putObject("result_summary.ptm_dataset_annotated", newPTMDataset.getId());
            entityManagerMSI.merge(rsm);
            entityManagerMSI.getTransaction().commit();

        }catch (Exception e){
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



    private JSONPTMDataset readJSONPTMDataset(EntityManager entityManagerMSI) throws JsonProcessingException {
        //--- Read PTM data in object tree associated to rsm
        ResultSummary rsm = entityManagerMSI.find(ResultSummary.class, m_dataset.getResultSummaryId());
        String schemaName =  m_loadAnnotatedDataset ? "result_summary.ptm_dataset_annotated" : "result_summary.ptm_dataset";
        if (rsm.getObjectTreeIdByName().isEmpty() || rsm.getObjectTreeIdByName().get(schemaName) == null) {
            m_taskError = new TaskError(ERROR_PTM_CLUSTER_LOADING, "\"Identification Modification Sites\"  has not been run on this dataset.");
            throw new RuntimeException("\"Identification Modification Sites\"  has not been run on this dataset.");
        }
        ObjectTree ot = entityManagerMSI.find(ObjectTree.class, rsm.getObjectTreeIdByName().get(schemaName));
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        return mapper.readValue(ot.getClobData(), JSONPTMDataset.class);
    }


    /*
     * Private method called to create PTMSites from read JSON properties
     * Read PTMSite V2
     */
    private void createPTMDatasetPTMSites(JSONPTMDataset jsonDataset, EntityManager entityManagerMSI){

        
        PerformanceTest.startTime("createPTMDatasetPTMSites");
        
        List<PTMSite> ptmSites = new ArrayList<>();
        List<PTMSite> clusterPtmSites = new ArrayList<>();

        //---- Get associated Parent RSM  ProteinMatches
        Long rsmId = m_clusterPtmDataset.getDataset().getResultSummaryId();
        TypedQuery<DProteinMatch> typicalProteinQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinMatch(pm.id, pm.accession, pm.score, pm.peptideCount, pm.resultSet.id, pm.description, pm.geneName, pm.serializedProperties, pepset.id, pepset.score, pepset.sequenceCount, pepset.peptideCount, pepset.peptideMatchCount, pepset.resultSummaryId) "
                + "FROM PeptideSetProteinMatchMap pset_to_pm JOIN pset_to_pm.proteinMatch as pm JOIN pset_to_pm.peptideSet as pepset JOIN pepset.proteinSet as ps "
                + "WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true AND ps.representativeProteinMatchId=pm.id  ORDER BY pepset.score DESC", DProteinMatch.class);

        typicalProteinQuery.setParameter("rsmId", rsmId);
        List<DProteinMatch> typicalProteinMatchesArray = typicalProteinQuery.getResultList();


        //--- Get Protein Match mapping between parent and leaf rsm
        List<String> accessions = typicalProteinMatchesArray.stream().map(DProteinMatch::getAccession).collect(Collectors.toList());
        Map<String, List<Long>> allProtMatchesIdPerAccession = new HashMap<>();
        int bufferSize = 10000;
        int readAccessionCount = 0;
        while(readAccessionCount < accessions.size()){
            
            PerformanceTest.startTime("createPTMDatasetPTMSites-STEP1");
            
            Query protMatchQuery = entityManagerMSI.createQuery("SELECT pm.id, pm.accession FROM ProteinMatch pm, ResultSummary rsm WHERE pm.accession IN (:accList) AND rsm.resultSet.id = pm.resultSet.id AND rsm.id in (:rsmIds) ");
            int lastIndex = Math.min((readAccessionCount + bufferSize), accessions.size());
            protMatchQuery.setParameter("accList", accessions.subList(readAccessionCount, lastIndex));
            protMatchQuery.setParameter("rsmIds", m_clusterPtmDataset.getLeafResultSummaryIds());

            Iterator<Object[]> proteinMatchesResult = protMatchQuery.getResultList().iterator();
            while(proteinMatchesResult.hasNext()){
                Object[] results = proteinMatchesResult.next();
                Long prMatchAcc = (Long) results[0];
                String acc = (String) results[1];
                if(!allProtMatchesIdPerAccession.containsKey(acc))
                    allProtMatchesIdPerAccession.put(acc, new ArrayList<>());
                allProtMatchesIdPerAccession.get(acc).add(prMatchAcc);
            }
            readAccessionCount = lastIndex;
            
            PerformanceTest.stopTime("createPTMDatasetPTMSites-STEP1");
        }
        m_clusterPtmDataset.setLeafProtMatchesIdPerAccession(allProtMatchesIdPerAccession);
        m_sitePtmDataset.setLeafProtMatchesIdPerAccession(allProtMatchesIdPerAccession);


        //----  fetch Generic PTM Data
        DatabasePTMsTask.fetchGenericPTMData(entityManagerMSI);

        m_ptmSiteIdsByBestPepMatchId = new HashMap<>();
        //---- Create the list of PTMSites            
        Map<Long, DProteinMatch> proteinMatchMap = typicalProteinMatchesArray.stream().collect(Collectors.toMap(DProteinMatch::getId, item -> item));
        PerformanceTest.startTime("createPTMDatasetPTMSites-STEP2");
        for (JSONPTMSite2 jsonSite : jsonDataset.ptmSites) {

            PTMSite site = new PTMSite(jsonSite, proteinMatchMap.get(jsonSite.proteinMatchId));
            site.setPTMSpecificity(DInfoPTM.getInfoPTMMap().get(jsonSite.ptmDefinitionId));
            if (site.getProteinMatch() != null) {
                ptmSites.add(site);
            }

            PTMSite clusterSite = new PTMSite(jsonSite, proteinMatchMap.get(jsonSite.proteinMatchId));
            clusterSite.setPTMSpecificity(DInfoPTM.getInfoPTMMap().get(jsonSite.ptmDefinitionId));
            if (clusterSite.getProteinMatch() != null) {
                clusterPtmSites.add(clusterSite);
            }

            m_ptmSiteIdsByBestPepMatchId.computeIfAbsent(jsonSite.bestPeptideMatchId, k -> new ArrayList<>());
            m_ptmSiteIdsByBestPepMatchId.get(jsonSite.bestPeptideMatchId).add(jsonSite.id);
        }
        PerformanceTest.stopTime("createPTMDatasetPTMSites-STEP2");
        m_clusterPtmDataset.setPTMSites(clusterPtmSites);
        m_sitePtmDataset.setPTMSites(ptmSites);

        PerformanceTest.stopTime("createPTMDatasetPTMSites");
        
        
        //---- Runs subtasks to get peptide matches
        //Get Best Peptide MatchesProteinMatches
        m_bestSitesPepMatchIds = Arrays.stream(jsonDataset.ptmSites).map(site -> site.bestPeptideMatchId).distinct().collect(Collectors.toList());
        SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PTMSITE_PEPTIDES, m_bestSitesPepMatchIds.size(), SLICE_SIZE);
        fetchSiteBestPeptideMatches(subTask, entityManagerMSI); //get first slice
    }


    private boolean fillClusterPeptidesMaps(PTMCluster ptmCluster, Long bestPepMatchID) {
        if (ptmCluster.getPTMSites() == null || ptmCluster.getPTMSites().isEmpty()) {
            return false;
        }

        m_bestClustersPepMatchIds.add(bestPepMatchID);
        if (!m_ptmClustersByBestPepMatchId.containsKey(bestPepMatchID)) {
            m_ptmClustersByBestPepMatchId.put(bestPepMatchID, new ArrayList<>());
        }
        m_ptmClustersByBestPepMatchId.get(bestPepMatchID).add(ptmCluster);
        return true;
    }

    private void createPTMDatasetPTMClusters(JSONPTMDataset jsonDataset,  EntityManager entityManagerMSI) {

        PerformanceTest.startTime("createPTMDatasetPTMClusters");

        m_bestClustersPepMatchIds = new ArrayList<>();
        m_ptmClustersByBestPepMatchId = new HashMap<>();

        //Create PTMCluster for SitePTMDataset
        List<PTMCluster> allClusters = new ArrayList<>();
        for (PTMSite site : m_sitePtmDataset.getPTMSites()) {
            PTMCluster ptmCluster = new PTMCluster(site.getId(), site.getLocalisationConfidence(), 2/*SELECTED AUTO*/,null, null,
                    Collections.singletonList(site.getId()), site.getPeptideIds(), m_sitePtmDataset);
            Long bestPepMatchID = site.getBestProbabilityPepMatchId();
            if(fillClusterPeptidesMaps(ptmCluster, bestPepMatchID))
                allClusters.add(ptmCluster);
            ptmCluster.setPTMSitesCount(1); //Only One PTMSite per Cluster.
        }
        m_sitePtmDataset.setPTMClusters(allClusters);

        //Create PTMCluster for SitePTMDataset
        allClusters = new ArrayList<>();
        for (JSONPTMCluster cluster : jsonDataset.ptmClusters) {
            PTMCluster ptmCluster = new PTMCluster(cluster, m_clusterPtmDataset);
            Long bestPepMatchID = cluster.bestPeptideMatchId;
            if(fillClusterPeptidesMaps(ptmCluster, bestPepMatchID))
                allClusters.add(ptmCluster);
        }
        m_clusterPtmDataset.setPTMClusters(allClusters);

        PerformanceTest.stopTime("createPTMDatasetPTMClusters");

        // TODO : try to simplify this since the same peptideMatches will already be loaded for sites, except that this
        // TODO: is done in a subtasks. When executing this method, the subtasks is not necessarily done.
        //---- Runs subtasks to get peptide matches
        SubTask subTask = m_subTaskManager.sliceATaskAndGetFirst(SUB_TASK_PTMCLUSTER_PEPTIDES, m_bestClustersPepMatchIds.size(), SLICE_SIZE);
        fetchClusterBestPeptideMatches(subTask, entityManagerMSI); //get first slice
    }

    // 
    private void fetchSiteBestPeptideMatches(SubTask subTask, EntityManager entityManagerMSI) {
        List<Long> sliceOfPeptideMatchIds = subTask.getSubList(m_bestSitesPepMatchIds);
        fetchBestPeptideMatches(sliceOfPeptideMatchIds, entityManagerMSI, true);
    }

    private void fetchClusterBestPeptideMatches(SubTask subTask, EntityManager entityManagerMSI) {
        List<Long> sliceOfPeptideMatchIds = subTask.getSubList(m_bestClustersPepMatchIds);
        fetchBestPeptideMatches(sliceOfPeptideMatchIds, entityManagerMSI, false);
    }

    /* Method called for subTask execution to get peptide Matches info
     */
    private void fetchBestPeptideMatches(List<Long> sliceOfPeptideMatchIds, EntityManager entityManagerMSI, boolean isSite) {

        PerformanceTest.startTime("fetchBestPeptideMatches");
        
        PerformanceTest.startTime("fetchBestPeptideMatches-STEP1");
        
        Query peptidesQuery = entityManagerMSI.createQuery("SELECT pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.serializedProperties, sp.firstTime, sp.precursorIntensity, sp.title, p, ms.id, ms.initialId, ptmString\n"
                + "   FROM fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.PeptideReadablePtmString ptmString, fr.proline.core.orm.msi.MsQuery ms, fr.proline.core.orm.msi.Spectrum sp  \n"
                + "   WHERE pipm.id.peptideMatchId IN (:peptideMatchList) AND pipm.id.peptideInstanceId=pi.id AND pipm.id.peptideMatchId=pm.id AND pm.peptideId=p.id AND pm.msQuery=ms AND ms.spectrum=sp AND pi.resultSummary.id in (:rmsIds) AND ptmString.peptide=p AND ptmString.resultSet.id=pm.resultSet.id");
        peptidesQuery.setParameter("peptideMatchList", sliceOfPeptideMatchIds);
        List<Long> rsmIds = m_clusterPtmDataset.getLeafResultSummaryIds();
        rsmIds.add(m_clusterPtmDataset.getDataset().getResultSummaryId());
        peptidesQuery.setParameter("rmsIds", rsmIds);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        List<Object[]> l = peptidesQuery.getResultList();
        Iterator<Object[]> itPeptidesQuery = l.iterator();
        List<DPeptideMatch> allPepMatches = new ArrayList<>();
        //Create List of DPeptideMatch (linked to DSpectrum + DMsQuery) from query resumlt 
        
        PerformanceTest.stopTime("fetchBestPeptideMatches-STEP1");
        
        while (itPeptidesQuery.hasNext()) {
            
            PerformanceTest.startTime("fetchBestPeptideMatches-STEP2");
            
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

            PerformanceTest.stopTime("fetchBestPeptideMatches-STEP2");
            
            PerformanceTest.startTime("fetchBestPeptideMatches-STEP3");
            
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
            
            PerformanceTest.stopTime("fetchBestPeptideMatches-STEP3");
            
            PerformanceTest.startTime("fetchBestPeptideMatches-STEP4");

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

            PerformanceTest.stopTime("fetchBestPeptideMatches-STEP4");
            
            PerformanceTest.startTime("fetchBestPeptideMatches-STEP5");
            
            if (isSite) {
                List<Long> siteIds = m_ptmSiteIdsByBestPepMatchId.get(pmId);
                if (siteIds != null && !siteIds.isEmpty()) {
                    siteIds.forEach(siteId -> {
                        PTMSite finalSite = m_clusterPtmDataset.getPTMSite(siteId);
                        if (finalSite != null) {
                            finalSite.setBestProbabilityPepMatch(pm);
                        }
                        finalSite = m_sitePtmDataset.getPTMSite(siteId);
                        if (finalSite != null) {
                            finalSite.setBestProbabilityPepMatch(pm);
                        }
                    });
                }
            } else {
                List<PTMCluster> clusters = m_ptmClustersByBestPepMatchId.get(pmId);
                if (clusters != null && !clusters.isEmpty()) {
                    clusters.forEach(cluster -> cluster.setRepresentativePepMatch(pm));
                }
            }
            allPepMatches.add(pm);
            
            PerformanceTest.stopTime("fetchBestPeptideMatches-STEP5");
        }


        PerformanceTest.startTime("fetchBestPeptideMatches-STEP6");

        //--  fetch Specific PTM Data for the Peptides Found
        List<Long> pepId = allPepMatches.stream().map(peM -> peM.getPeptide().getId()).collect(Collectors.toList());

        HashMap<Long, ArrayList<DPeptidePTM>> ptmMap = DatabasePTMsTask.fetchPeptidePTMForPeptides(entityManagerMSI, new ArrayList<>(pepId));
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

        PerformanceTest.stopTime("fetchBestPeptideMatches-STEP6");
        PerformanceTest.stopTime("fetchBestPeptideMatches");
    }


    private void fetchPTMSitesPepInstances(SubTask subTask, EntityManager entityManagerMSI) throws IOException {
        List<Long> sliceOfPeptideIds = subTask.getSubList(m_peptideIds);
        fetchPTMSitesPepInstances(sliceOfPeptideIds, entityManagerMSI);
    }

    private void fetchPTMSitesPepInstances(List<Long> sliceOfPeptideIds, EntityManager entityManagerMSI) throws IOException {

        PerformanceTest.startTime("fetchPTMSitesPepInstances");

        long start = System.currentTimeMillis();
        m_logger.debug(" @@ START fetchPTMSitesPepInstances SUBTASK ");
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        int nbrPepIds = sliceOfPeptideIds.size();
        Long rsetId = m_dataset.getResultSetId();
        if(rsetId == null && m_dataset.getResultSummary() != null && m_dataset.getResultSummary().getResultSet() != null)
            rsetId = m_dataset.getResultSummary().getResultSet().getId();
        Long dsRsmId = m_dataset.getResultSummaryId();
        //Same getLeafResultSummaryIds for Cluster or SiteCluster PTMDataset
        List<Long> allRsmIds = m_ptmDatasetPair.getClusterPTMDataset().getLeafResultSummaryIds();
        boolean datasetIsLeaf = (allRsmIds.size() == 1 && m_ptmDatasetPair.getClusterPTMDataset().getLeafResultSummaryIds().get(0).equals(dsRsmId));

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

        HashMap<Long, Peptide> allPeptidesMap = new HashMap<>();
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
        List<Object[]> l = dataQuery.getResultList();
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
            Peptide p = pi.getPeptide();
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
                    DPeptideInstance dpi = new DPeptideInstance(pi);
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
                    DPeptideInstance dpi = new DPeptideInstance(pi);
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
        DatabasePTMsTask.fillReadablePTMDataForPeptides(entityManagerMSI,rsetId,allPeptidesMap, null);

        PerformanceTest.stopTime("ptmStingQuery");

        PerformanceTest.startTime("fetchGenericPTMData");

        // fetch Generic PTM Data
        DatabasePTMsTask.fetchGenericPTMData(entityManagerMSI);

        PerformanceTest.stopTime("fetchGenericPTMData");


        PerformanceTest.startTime("fetchPeptidePTMForPeptides");

        //--- fetch Specific Data for the Peptides Found
        HashMap<Long, ArrayList<DPeptidePTM>> ptmMap = DatabasePTMsTask.fetchPeptidePTMForPeptides(entityManagerMSI, new ArrayList<>(allPeptidesMap.keySet()));

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

        m_logger.info(" @@ {} peptides matching to {} peptide matches retrieved", allPeptidesMap.size(), allpeptideMatches.size());
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
                List<DPeptideInstance> parentPeptideInstances = new ArrayList<>(parentPeptideInstancesAsSet);
                site.addPeptideInstances(parentPeptideInstances, leafPeptideInstances);
            }
        }

        PerformanceTest.stopTime("m_ptmSitesOutput loop");

        end = System.currentTimeMillis();
        m_logger.debug(" @@ END fetchPTMSitesPepInstances subtask "+" in "+(end-start)+" ms");

        PerformanceTest.stopTime("fetchPTMSitesPepInstances");

    }

}
