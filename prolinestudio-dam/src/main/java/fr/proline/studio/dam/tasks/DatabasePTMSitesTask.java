package fr.proline.studio.dam.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.ObjectTreeSchema;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.PtmSpecificity;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DSpectrum;
import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.core.orm.msi.dto.DPtmSiteProperties;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.data.ptm.JSONPTMSite;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;


/**
 *
 * @author JM235353
 */
public class DatabasePTMSitesTask extends AbstractDatabaseTask {

    private long m_projectId = -1;
    private ResultSummary m_rsm = null;
    private List<Long> m_rsmIds = null;
    
    //attributes to initialize when data is retrieve
    private PTMSite m_ptmSiteOutput = null;
    private List<PTMSite> m_ptmSitesOutput = null;    
    private List<PtmSpecificity> m_ptmsOutput = null;    

    final int SLICE_SIZE = 1000;
    
    private int m_action;
    
    private final static int LOAD_ALL_PTM_SITES_FOR_RSMS = 0;
    private final static int FILL_PTM_SITE_PEPINFO = 1;
    private final static int LOAD_IDENTIFIED_PTM_SPECIFICITIES = 2;
    private final static int FILL_ALL_PTM_SITES_PEPINFO = 3;

    
    public DatabasePTMSitesTask(AbstractDatabaseCallback callback) {
        super(callback, null);
    }
    
    private void init(TaskInfo taskInfo) {
        m_defaultPriority = Priority.NORMAL_1;
        m_taskInfo = taskInfo;
    }
    
    public void initLoadPTMSites(long projectId, ResultSummary rsm, List<PTMSite> ptmSiteArray) {
        init(new TaskInfo("Load All PTM Sites for " + rsm.getId(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_rsm = rsm;
        m_ptmSitesOutput = ptmSiteArray;
        m_action = LOAD_ALL_PTM_SITES_FOR_RSMS;
    }

    public void initFillPTMSite(long projectId, ResultSummary rsm, PTMSite ptmSiteToFill) {
        init(new TaskInfo("Load peptides of PTM Site " + ptmSiteToFill, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_ptmSiteOutput = ptmSiteToFill;
        m_rsm = rsm;
        m_action = FILL_PTM_SITE_PEPINFO;
    }

    public void initFillPTMSites(long projectId, ResultSummary rsm, List<PTMSite> ptmSitesToFill) {
        init(new TaskInfo("Load peptides of PTM Sites", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_ptmSitesOutput = ptmSitesToFill;
        m_rsm = rsm;
        m_action = FILL_ALL_PTM_SITES_PEPINFO;
    }

    public void initLoadUsedPTMs(Long projectId, Long rsmId, List<PtmSpecificity> ptmsToFill) {
        init(new TaskInfo("Load used PTMs from RSM id" + rsmId, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_rsmIds = new ArrayList<>();
        m_rsmIds.add(rsmId);
        m_ptmsOutput = ptmsToFill;
        m_action = LOAD_IDENTIFIED_PTM_SPECIFICITIES;
    }
        
    public void initLoadUsedPTMs(Long projectId, List<Long> rsmIds, List<PtmSpecificity> ptmsToFill) {
        init(new TaskInfo("Load used PTMs from RSM ids" + rsmIds, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_rsmIds = rsmIds;
        m_ptmsOutput = ptmsToFill;
        m_action = LOAD_IDENTIFIED_PTM_SPECIFICITIES;
    }    
        
    
    @Override
    public boolean needToFetch() {
        switch(m_action) {
            case LOAD_IDENTIFIED_PTM_SPECIFICITIES:
            case FILL_ALL_PTM_SITES_PEPINFO:
            case LOAD_ALL_PTM_SITES_FOR_RSMS: {
                return true;
            }  
            case FILL_PTM_SITE_PEPINFO: {
                return !m_ptmSiteOutput.isLoaded();
            }              
        }
        return false; // should not be called 
    }

    @Override
    public boolean fetchData() {
        if (needToFetch()) {
            switch (m_action) {
                case LOAD_ALL_PTM_SITES_FOR_RSMS: {
                    return fetchAllPTMSites();
                }
                case FILL_PTM_SITE_PEPINFO: {
                    return fetchPTMSitePeptideMatches();
                }
                case LOAD_IDENTIFIED_PTM_SPECIFICITIES: {
                    return fetchIdentifiedPTMs();
                }
                case FILL_ALL_PTM_SITES_PEPINFO: {
                    return fetchAllPTMSitesPeptideMatches();
                }
            }
           
            return true; // should not happen
        }
        return true; // should not happen
    }

    
    private boolean fetchIdentifiedPTMs() {
        long start = System.currentTimeMillis();        
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();
            long stop = System.currentTimeMillis();
            m_logger.debug("Identified PTM entityManagerMSI.getTransaction().begin {} ms", (stop-start));
            start = stop;
            
            TypedQuery<PtmSpecificity> query = entityManagerMSI.createQuery("SELECT DISTINCT(pp.specificity) FROM PeptidePtm pp, PeptideInstance pi JOIN FETCH pp.specificity.ptm ptm WHERE pi.resultSummary.id in (:rsmIds) AND pi.peptide.id = pp.peptide.id", PtmSpecificity.class);
            query.setParameter("rsmIds", m_rsmIds);
            m_ptmsOutput.addAll(query.getResultList());
            
            stop = System.currentTimeMillis();
            m_logger.debug("Identified PTM fetched in {} ms", (stop-start));
            
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

        return true;    
    }
    
    private boolean fetchPTMSitePeptideMatches(){
        
        long start = System.currentTimeMillis();        
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

            fetchPTMSiteData(m_ptmSiteOutput, entityManagerMSI, mapper);
                        
            long stop = System.currentTimeMillis();
            m_logger.debug("PTM Site {} filled in {} ms", m_ptmSiteOutput, (stop-start));

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
    
    private boolean fetchAllPTMSitesPeptideMatches(){
        
        long start = System.currentTimeMillis();       
        m_logger.debug(" START fetchAllPTMSitesPeptideMatches task ");
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
//            int count = 1;
//            for (PTMSite site : m_ptmSiteArray) {
//                if (!site.isLoaded()) {
//                    if ( (count % 100) == 0) m_logger.info("#{} loading site {}", count, site);
//                    fetchPTMSiteData(site, entityManagerMSI, mapper);
//                    count++;
//                }
//            }
            
            fetchPTMSitesData(m_ptmSitesOutput, entityManagerMSI, mapper);
            
            long stop = System.currentTimeMillis();
            m_logger.debug("??/{} PTM Sites filled in {} ms", m_ptmSitesOutput.size(), (stop-start));

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

    private void fetchPTMSitesData(List<PTMSite> sitesToFill, EntityManager entityManagerMSI, ObjectMapper mapper) throws IOException {
        HashMap<Long, Peptide> allPeptidesMap = new HashMap();
        HashMap<Long, DPeptideInstance> leafPeptideInstancesById = new HashMap<>();
        
        Set<Long> pepInstanceIds = sitesToFill.stream().flatMap( s -> Arrays.stream(s.getPeptideInstanceIds()) ).collect(Collectors.toSet());
        
         m_logger.debug("fetchPTMSitesData: pepInstanceIds " +pepInstanceIds.size());
        //---- Load Peptide Match + Spectrum / MSQuery information for all peptideInstance of PTMSite
        Query peptidesQuery = entityManagerMSI.createQuery("SELECT pm, pi\n"
                + "              FROM fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstance pi \n"
                + "              WHERE pipm.id.peptideInstanceId IN ( :peptideInstanceList ) AND pipm.id.peptideInstanceId=pi.id AND pipm.id.peptideMatchId=pm.id ");
        peptidesQuery.setParameter("peptideInstanceList",new ArrayList<>(pepInstanceIds));
        List l = peptidesQuery.getResultList();
        Iterator<Object[]> itPeptidesQuery = l.iterator();
        //---- Create List of DPeptideMatch (linked to DSpectrum + DMsQuery) from query resumlt
        m_logger.debug("fetchPTMSitesData: query result size "+l.size());
        while (itPeptidesQuery.hasNext()) {
            Object[] resCur = itPeptidesQuery.next();
            
            PeptideMatch pm = (PeptideMatch) resCur[0];
            PeptideInstance pi = (PeptideInstance) resCur[1];
            
            DSpectrum spectrum = new DSpectrum();
            spectrum.setFirstTime(pm.getMsQuery().getSpectrum().getFirstTime());
            spectrum.setPrecursorIntensity(pm.getMsQuery().getSpectrum().getPrecursorIntensity());
            spectrum.setTitle(pm.getMsQuery().getSpectrum().getTitle());
            
            DPeptideMatch dpm = new DPeptideMatch(pm.getId(), pm.getRank(), pm.getCharge(), pm.getDeltaMoz(), pm.getExperimentalMoz(), pm.getMissedCleavage(), pm.getScore(), pm.getResultSet().getId(), pm.getCDPrettyRank(), pm.getSDPrettyRank());
            dpm.setRetentionTime(spectrum.getFirstTime());
            dpm.setSerializedProperties(pm.getSerializedProperties());

            JsonNode node = mapper.readTree(dpm.getSerializedProperties());
            JsonNode child = node.get("ptm_site_properties");
            DPtmSiteProperties properties = mapper.treeToValue(child, DPtmSiteProperties.class);
            dpm.setPtmSiteProperties(properties);
            
            Peptide p = (Peptide) pi.getPeptide();
            p.getTransientData().setPeptideReadablePtmStringLoaded();
            allPeptidesMap.put(p.getId(), p);
            
            DMsQuery msq = new DMsQuery(pm.getId(), pm.getMsQuery().getId(), pm.getMsQuery().getInitialId(), spectrum.getPrecursorIntensity());
            msq.setDSpectrum(spectrum);
            
            dpm.setPeptide(p);
            dpm.setMsQuery(msq);
            
            if (!leafPeptideInstancesById.containsKey(pi.getId())) {
                DPeptideInstance dpi = new DPeptideInstance(pi.getId(), pi.getPeptide().getId(), pi.getValidatedProteinSetCount(), pi.getElutionTime());
                dpi.setResultSummary(pi.getResultSummary());
                dpi.setPeptide(p);
                dpi.setPeptideMatches(new ArrayList<>());
                leafPeptideInstancesById.put(dpi.getId(), dpi);
            }
            if(pi.getBestPeptideMatchId() == dpm.getId())
                 leafPeptideInstancesById.get(pi.getId()).setBestPeptideMatch(dpm);
            leafPeptideInstancesById.get(pi.getId()).getPeptideMatches().add(dpm);
        }
        m_logger.debug(" created leafPeptideInstancesById. Nbr pepIns "+leafPeptideInstancesById.size());
        //--- Retrieve Parent peptideInstances
        Long rsetId = m_rsm.getResultSet().getId();
        TypedQuery<PeptideInstance> parentPeptideInstanceQuery = entityManagerMSI.createQuery("SELECT pi FROM fr.proline.core.orm.msi.PeptideInstance pi WHERE pi.peptide.id IN (:listId) AND pi.resultSummary.id=:rsmId", PeptideInstance.class);
        parentPeptideInstanceQuery.setParameter("listId", allPeptidesMap.keySet());
        parentPeptideInstanceQuery.setParameter("rsmId", m_rsm.getId());
        Iterator<PeptideInstance> it = parentPeptideInstanceQuery.getResultList().iterator();
        Map<Long, DPeptideInstance> parentPeptideInstancesByPepId = new HashMap<>();
        while (it.hasNext()) {
            PeptideInstance pi = it.next();
            DPeptideInstance dpi = new DPeptideInstance(pi.getId(), pi.getPeptide().getId(), pi.getValidatedProteinSetCount(), pi.getElutionTime());
            dpi.setPeptide(pi.getPeptide());
            parentPeptideInstancesByPepId.put(dpi.getPeptideId(), dpi);
        }
         m_logger.debug(" parentPeptideInstancesByPepId created ");
        //--- Retrieve PeptideReadablePtmString
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
        m_logger.debug(" PeptideReadablePtmString LOADED");
        // fetch Generic PTM Data
        fetchGenericPTMData(entityManagerMSI);
        //--- fetch Specific Data for the Peptides Found
        HashMap<Long, ArrayList<DPeptidePTM>> ptmMap = fetchPTMDataForPeptides(entityManagerMSI, new ArrayList(allPeptidesMap.keySet()));
        List<DPeptideMatch> allpeptideMatches = leafPeptideInstancesById.values().stream().flatMap(pi -> pi.getPeptideMatches().stream()).collect(Collectors.toList());
         m_logger.debug(" allpeptideMatches  "+allpeptideMatches.size());
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
        m_logger.info("{} peptides matching to {} peptide matches retrieved", allPeptides.size(), allpeptideMatches.size());
        m_logger.info("{} peptide instances retrieved", parentPeptideInstancesByPepId.size());
        for (PTMSite site : sitesToFill) {
            // insert .filter(key -> leafPeptideInstancesById.containsKey(key)) ? Theoretically no : all instances has been retrieved .. 
            List<DPeptideInstance> leafPeptideInstances = Arrays.stream(site.getPeptideInstanceIds()).map(id -> leafPeptideInstancesById.get(id)).collect(Collectors.toList());
            Set<DPeptideInstance> parentPeptideInstancesAsSet = leafPeptideInstances.stream().map(pi -> pi.getPeptideId()).map(id -> parentPeptideInstancesByPepId.get(id)).collect(Collectors.toSet());            
            List<DPeptideInstance> parentPeptideInstances = new ArrayList<>();
            parentPeptideInstances.addAll(parentPeptideInstancesAsSet);
            site.setPeptideInstances(parentPeptideInstances, leafPeptideInstances);
        }
    }
    
    
    private void fetchPTMSiteData(PTMSite siteToFill, EntityManager entityManagerMSI, ObjectMapper mapper) throws IOException {
        HashMap<Long, Peptide> allPeptidesMap = new HashMap();
        HashMap<Long, DPeptideInstance> leafPeptideInstancesById = new HashMap<>();
        
        //Get PeptideInstance id : IF PTM v1 : data exist in PTMSite othewise we need to load them 
        ArrayList<Long> peptideInstanceIds = new ArrayList<>();
        Long[] pepInst  = siteToFill.getPeptideInstanceIds();
        if(pepInst == null){
            ArrayList<Long> allPepIds = siteToFill.getPeptideIds();
            Query peptidesQuery = entityManagerMSI.createQuery("SELECT pi.id FROM fr.proline.core.orm.msi.PeptideInstance pi"
                        + "   WHERE pi.peptide.id IN (:peptideIdsList) AND pi.resultSummary.id in (:rmsIds)");
            peptidesQuery.setParameter("peptideIdsList", allPepIds);
            List<Long> rsmIds = siteToFill.getPTMdataset().getLeafResultSummaryIds();
            rsmIds.add(siteToFill.getPTMdataset().getDataset().getResultSummaryId());
            peptidesQuery.setParameter("rmsIds", rsmIds);        
            
//            Iterator<Long> itPeptidesQuery = peptidesQuery.getResultList().iterator();                        
//            while (itPeptidesQuery.hasNext()) {            
                peptideInstanceIds.addAll(  peptidesQuery.getResultList());
//            }            
        } else {
            peptideInstanceIds.addAll(Arrays.asList(pepInst));
        }
        
        
        //---- Load Peptide Match + Spectrum / MSQuery information for all peptideInstance of PTMSite
        Query peptidesQuery = entityManagerMSI.createQuery("SELECT pm, pi\n"
                + "              FROM fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstance pi \n"
                + "              WHERE pipm.id.peptideInstanceId IN ( :peptideInstanceList ) AND pipm.id.peptideInstanceId=pi.id AND pipm.id.peptideMatchId=pm.id ");
        peptidesQuery.setParameter("peptideInstanceList",peptideInstanceIds);
        List l = peptidesQuery.getResultList();
        Iterator<Object[]> itPeptidesQuery = l.iterator();
        //---- Create List of DPeptideMatch (linked to DSpectrum + DMsQuery) from query resumlt
        while (itPeptidesQuery.hasNext()) {
            Object[] resCur = itPeptidesQuery.next();
            
            PeptideMatch pm = (PeptideMatch) resCur[0];
            PeptideInstance pi = (PeptideInstance) resCur[1];
            
            DSpectrum spectrum = new DSpectrum();
            spectrum.setFirstTime(pm.getMsQuery().getSpectrum().getFirstTime());
            spectrum.setPrecursorIntensity(pm.getMsQuery().getSpectrum().getPrecursorIntensity());
            spectrum.setTitle(pm.getMsQuery().getSpectrum().getTitle());
            
            DPeptideMatch dpm = new DPeptideMatch(pm.getId(), pm.getRank(), pm.getCharge(), pm.getDeltaMoz(), pm.getExperimentalMoz(), pm.getMissedCleavage(), pm.getScore(), pm.getResultSet().getId(), pm.getCDPrettyRank(), pm.getSDPrettyRank());
            dpm.setRetentionTime(spectrum.getFirstTime());
            dpm.setSerializedProperties(pm.getSerializedProperties());
            
            JsonNode node = mapper.readTree(dpm.getSerializedProperties());
            JsonNode child = node.get("ptm_site_properties");
            DPtmSiteProperties properties = mapper.treeToValue(child, DPtmSiteProperties.class);
            dpm.setPtmSiteProperties(properties);
            
            Peptide p = (Peptide) pi.getPeptide();
            p.getTransientData().setPeptideReadablePtmStringLoaded();
            allPeptidesMap.put(p.getId(), p);
            
            DMsQuery msq = new DMsQuery(pm.getId(), pm.getMsQuery().getId(), pm.getMsQuery().getInitialId(), spectrum.getPrecursorIntensity());
            msq.setDSpectrum(spectrum);
            
            dpm.setPeptide(p);
            dpm.setMsQuery(msq);
            
            if (!leafPeptideInstancesById.containsKey(pi.getId())) {
                DPeptideInstance dpi = new DPeptideInstance(pi.getId(), pi.getPeptide().getId(), pi.getValidatedProteinSetCount(), pi.getElutionTime());
                dpi.setResultSummary(pi.getResultSummary());
                dpi.setPeptide(p);
                dpi.setPeptideMatches(new ArrayList<>());
                leafPeptideInstancesById.put(dpi.getId(), dpi);
            }
            
            leafPeptideInstancesById.get(pi.getId()).getPeptideMatches().add(dpm);
        }
        //--- Retrieve Parent peptideInstances
        Long rsetId = m_rsm.getResultSet().getId();
        TypedQuery<PeptideInstance> parentPeptideInstanceQuery = entityManagerMSI.createQuery("SELECT pi FROM fr.proline.core.orm.msi.PeptideInstance pi WHERE pi.peptide.id IN (:listId) AND pi.resultSummary.id=:rsmId", PeptideInstance.class);
        parentPeptideInstanceQuery.setParameter("listId", allPeptidesMap.keySet());
        parentPeptideInstanceQuery.setParameter("rsmId", m_rsm.getId());
        Iterator<PeptideInstance> it = parentPeptideInstanceQuery.getResultList().iterator();
        List<DPeptideInstance> parentPeptideInstances = new ArrayList<>();
        while (it.hasNext()) {
            PeptideInstance pi = it.next();
            DPeptideInstance dpi = new DPeptideInstance(pi.getId(), pi.getPeptide().getId(), pi.getValidatedProteinSetCount(), pi.getElutionTime());
            dpi.setPeptide(pi.getPeptide());
            parentPeptideInstances.add(dpi);
        }
        //--- Retrieve PeptideReadablePtmString
        Query ptmStingQuery = entityManagerMSI.createQuery("SELECT p.id, ptmString FROM fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.PeptideReadablePtmString ptmString WHERE p.id IN (:listId) AND ptmString.peptide=p AND ptmString.resultSet.id=:rsetId");
        ptmStingQuery.setParameter("listId", allPeptidesMap.keySet());
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
        // fetch Generic PTM Data
        fetchGenericPTMData(entityManagerMSI);
        //--- fetch Specific Data for the Peptides Found
        HashMap<Long, ArrayList<DPeptidePTM>> ptmMap = fetchPTMDataForPeptides(entityManagerMSI, new ArrayList(allPeptidesMap.keySet()));
        List<DPeptideMatch> allpeptideMatches = leafPeptideInstancesById.values().stream().flatMap(pi -> pi.getPeptideMatches().stream()).collect(Collectors.toList());
        for (DPeptideMatch pm : allpeptideMatches) {
            Peptide p = pm.getPeptide();
            Long idPeptide = p.getId();
            pm.setPeptidePTMArray(ptmMap.get(idPeptide));
            
            HashMap<Integer, DPeptidePTM> mapToPtm = new HashMap<>();
            ArrayList<DPeptidePTM> ptmList = ptmMap.get(p.getId());
            if (ptmList != null) {
                for (DPeptidePTM peptidePTM : ptmList) {
                    mapToPtm.put((int) peptidePTM.getSeqPosition(), peptidePTM);
                }
                p.getTransientData().setDPeptidePtmMap(mapToPtm);
            }
        }
        siteToFill.setPeptideInstances(parentPeptideInstances, leafPeptideInstancesById.values().stream().collect(Collectors.toList()));
    }
    
    private boolean fetchAllPTMSites() {
        
        long start = System.currentTimeMillis();
        m_logger.debug(" START fetchAllPTMSites");
        EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();
            
            Long rsmId = m_rsm.getId();
            
            TypedQuery<DProteinMatch> typicalProteinQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinMatch(pm.id, pm.accession, pm.score, pm.peptideCount, pm.resultSet.id, pm.description, pm.serializedProperties, pepset.id, pepset.score, pepset.sequenceCount, pepset.peptideCount, pepset.peptideMatchCount, pepset.resultSummaryId) FROM PeptideSetProteinMatchMap pset_to_pm JOIN pset_to_pm.proteinMatch as pm JOIN pset_to_pm.peptideSet as pepset JOIN pepset.proteinSet as ps WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true AND ps.representativeProteinMatchId=pm.id  ORDER BY pepset.score DESC", DProteinMatch.class);

            typicalProteinQuery.setParameter("rsmId", rsmId);
            List<DProteinMatch> typicalProteinMatchesArray = typicalProteinQuery.getResultList();
            
            ResultSummary rsm = entityManagerMSI.find(ResultSummary.class, rsmId);
            // TODO : following line code will fail if ptm sites were not previously identified. Check this first and returns properly
            // 
            if(rsm.getObjectTreeIdByName().isEmpty() || rsm.getObjectTreeIdByName().get(ObjectTreeSchema.SchemaName.PTM_SITES.toString())==null){
                throw new RuntimeException("Identify PTM algorythm should be run first !");
            }
            ObjectTree ot = entityManagerMSI.find(ObjectTree.class, rsm.getObjectTreeIdByName().get(ObjectTreeSchema.SchemaName.PTM_SITES.toString()));
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            List<JSONPTMSite> values = mapper.readValue(ot.getClobData(), mapper.getTypeFactory().constructCollectionType(List.class, JSONPTMSite.class));

            Long[] bestPeptideMatchIdsArray = values.stream().map(site -> site.bestPeptideMatchId).distinct().toArray(Long[]::new);
//            Map<Long, Long[]> peptideInstanceIdsBybestPMId = values.stream().collect(Collectors.toMap(site -> site.bestPeptideMatchId, site -> site.peptideInstanceIds, (l1, l2) -> {return Stream.concat(Arrays.stream(l1), Arrays.stream(l2)).distinct().toArray(Long[]::new);}));
            
            long stop = System.currentTimeMillis();
            m_logger.debug("{} typical ProtMatches and {} PTMSites loaded in {} ms", typicalProteinMatchesArray.size(), values.size(), (stop-start));
            start = stop;
            
            HashMap<Long, Peptide> allPeptidesMap = new HashMap();
            ArrayList<DPeptideMatch> peptideMatchArray = new ArrayList<>();
            Map<Long, DPeptideMatch> peptideMatchById = new HashMap();
                    
            SubTaskManager subTaskManager = new SubTaskManager(1);
            SubTask subTask = subTaskManager.sliceATaskAndGetFirst(0, bestPeptideMatchIdsArray.length, SLICE_SIZE);
            while (subTask != null) {
//                Query peptidesQuery = entityManagerMSI.createQuery("SELECT pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.serializedProperties, sp.firstTime, sp.precursorIntensity, sp.title, p, ms.id, ms.initialId\n"
//                        + "              FROM fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.MsQuery ms, fr.proline.core.orm.msi.Spectrum sp  \n"
//                        + "              WHERE pipm.id.peptideMatchId IN (:peptideMatchList) AND pi.id IN (:peptideInstanceList) AND pipm.id.peptideInstanceId=pi.id AND pipm.id.peptideMatchId=pm.id AND pm.peptideId=p.id AND pm.msQuery=ms AND ms.spectrum=sp ");
//                List subList = subTask.getSubList(Arrays.asList(bestPeptideMatchIdsArray));
//                peptidesQuery.setParameter("peptideMatchList", subList);
//                List<Long> peptideInstanceIds = peptideInstanceIdsBybestPMId.entrySet().stream().filter(entry -> subList.contains(entry.getKey())).map(entry -> entry.getValue()).flatMap(x -> Arrays.stream(x)).distinct().collect(Collectors.toList());
//                peptidesQuery.setParameter("peptideInstanceList", peptideInstanceIds);

                Query peptidesQuery = entityManagerMSI.createQuery("SELECT pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.serializedProperties, sp.firstTime, sp.precursorIntensity, sp.title, p, ms.id, ms.initialId\n"
                        + "              FROM fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.MsQuery ms, fr.proline.core.orm.msi.Spectrum sp  \n"
                        + "              WHERE pipm.id.peptideMatchId IN (:peptideMatchList) AND pipm.id.peptideInstanceId=pi.id AND pipm.id.peptideMatchId=pm.id AND pm.peptideId=p.id AND pm.msQuery=ms AND ms.spectrum=sp");
                List subList = subTask.getSubList(Arrays.asList(bestPeptideMatchIdsArray));

                peptidesQuery.setParameter("peptideMatchList", subList);
                //peptidesQuery.setParameter("rmsId", rsmId);


                List l = peptidesQuery.getResultList(); 
                Iterator<Object[]> itPeptidesQuery = l.iterator();
                //Create List of DPeptideMatch (linked to DSpectrum + DMsQuery) from query resumlt 
                while (itPeptidesQuery.hasNext()) {
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

                    DPeptideMatch pm = new DPeptideMatch(pmId, pmRank, pmCharge, pmDeltaMoz, pmExperimentalMoz, pmMissedCleavage, pmScore, pmResultSetId, pmCdPrettyRank, pmSdPrettyRank);
                    pm.setRetentionTime(firstTime);
                    pm.setSerializedProperties(serializedProperties);
                    
                    JsonNode node = mapper.readTree(serializedProperties);
                    JsonNode child = node.get("ptm_site_properties");
                    DPtmSiteProperties properties = mapper.treeToValue(child, DPtmSiteProperties.class);
                    pm.setPtmSiteProperties(properties);
                    if(!peptideMatchById.containsKey(pmId))
                        peptideMatchById.put(pmId, pm);
                    peptideMatchArray.add(pm);

                    Peptide p = (Peptide) resCur[14];
                    p.getTransientData().setPeptideReadablePtmStringLoaded();
                    allPeptidesMap.put(p.getId(), p);

                    Long msqId = (Long) resCur[15];
                    Integer msqInitialId = (Integer) resCur[16];

                    DMsQuery msq = new DMsQuery(pmId, msqId, msqInitialId, precursorIntensity);
                    msq.setDSpectrum(spectrum);

                    pm.setPeptide(p);
                    pm.setMsQuery(msq);

                }
                
                subTask = subTaskManager.getNextSubTask();
                
            }
            
            stop = System.currentTimeMillis();
            m_logger.debug("Best PSM + Peptide + Spectrum + Query from PepInstances created in {} ms", (stop-start));
            start = stop;
            
            // Retrieve PeptideReadablePtmString
            Long rsetId = m_rsm.getResultSet().getId();
            Query ptmStingQuery = entityManagerMSI.createQuery("SELECT p.id, ptmString FROM fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.PeptideReadablePtmString ptmString WHERE p.id IN (:listId) AND ptmString.peptide=p AND ptmString.resultSet.id=:rsetId");
            ptmStingQuery.setParameter("listId", allPeptidesMap.keySet());
            ptmStingQuery.setParameter("rsetId", rsetId);

            List<Object[]> ptmStrings = ptmStingQuery.getResultList();
            Iterator<Object[]> it = ptmStrings.iterator();
            while (it.hasNext()) {
                Object[] res = it.next();
                Long peptideId = (Long) res[0];
                PeptideReadablePtmString ptmString = (PeptideReadablePtmString) res[1];
                Peptide peptide = allPeptidesMap.get(peptideId);
                peptide.getTransientData().setPeptideReadablePtmString(ptmString);
            }           
            
            stop = System.currentTimeMillis();
            m_logger.debug("PeptideReadablePtmString loaded in {} ms", (stop-start));
            start = stop;
            
            // fetch Generic PTM Data
            fetchGenericPTMData(entityManagerMSI);
            
            stop = System.currentTimeMillis();
            m_logger.debug("Generic PTM data loaded in {} ms", (stop-start));
            start = stop;
            
            // fetch Specific Data for the Peptides Found
            HashMap<Long, ArrayList<DPeptidePTM>> ptmMap = fetchPTMDataForPeptides(entityManagerMSI, new ArrayList(allPeptidesMap.keySet()));
        
            for (DPeptideMatch pm : peptideMatchArray) {
                Peptide p = pm.getPeptide();
                Long idPeptide = p.getId();
                pm.setPeptidePTMArray(ptmMap.get(idPeptide));
                
                HashMap<Integer, DPeptidePTM> mapToPtm = new HashMap<>();
                ArrayList<DPeptidePTM> ptmList = ptmMap.get(idPeptide);
                if (ptmList != null) {
                    for (DPeptidePTM peptidePTM : ptmList) {
                        mapToPtm.put((int) peptidePTM.getSeqPosition(), peptidePTM);
                    }
                    p.getTransientData().setDPeptidePtmMap(mapToPtm);
                }
            }
            
            stop = System.currentTimeMillis();
            m_logger.debug("PTM data for peptides loaded in {} ms", (stop-start));
            start = stop;
            
            // create the list of PTMSites            
            Map<Long, DProteinMatch> proteinMatchMap = typicalProteinMatchesArray.stream().collect(Collectors.toMap(item -> item.getId(), item -> item));
           // Map<Long, DPeptideMatch> peptideMatchMap = peptideMatchArray.stream().collect(Collectors.toMap(item -> item.getId(), item -> item));

            for (JSONPTMSite jsonSite : values) {
                PTMSite site = new PTMSite(jsonSite,proteinMatchMap.get(jsonSite.proteinMatchId) );
                site.setBestPeptideMatch(peptideMatchById.get(jsonSite.bestPeptideMatchId));
                site.setPTMSpecificity(DInfoPTM.getInfoPTMMap().get(jsonSite.ptmDefinitionId));
                if (site.getProteinMatch() != null) {
                    m_ptmSitesOutput.add(site);
                }
            }
            
            stop = System.currentTimeMillis();
            m_logger.debug("{} PTM Sites built in {} ms", m_ptmSitesOutput.size(), (stop-start));

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

    private boolean fetchGenericPTMData(EntityManager entityManagerMSI) {

        HashMap<Long, DInfoPTM> infoPTMMAp = DInfoPTM.getInfoPTMMap();
        if (!infoPTMMAp.isEmpty()) {
            return true; // already loaded
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

        return true;
    }

    
    private HashMap<Long, ArrayList<DPeptidePTM>> fetchPTMDataForPeptides(EntityManager entityManagerMSI, ArrayList<Long> allPeptidesIds) {

        HashMap<Long, ArrayList<DPeptidePTM>> ptmMap = new HashMap<>();
            
        SubTaskManager subTaskManager = new SubTaskManager(1);
        SubTask subTask = subTaskManager.sliceATaskAndGetFirst(0, allPeptidesIds.size(), SLICE_SIZE);
        while (subTask != null) {
  
            TypedQuery<DPeptidePTM> ptmQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DPeptidePTM(pptm.peptide.id, pptm.specificity.id, pptm.seqPosition) \n"
                + "FROM PeptidePtm as pptm  \n"
                + "WHERE pptm.peptide.id IN (:peptideList) ", DPeptidePTM.class);
            ptmQuery.setParameter("peptideList", subTask.getSubList(allPeptidesIds));

            List<DPeptidePTM> ptmList = ptmQuery.getResultList();

            Iterator<DPeptidePTM> it = ptmList.iterator();
            while (it.hasNext()) {
                DPeptidePTM ptm = it.next();
                Long peptideId = ptm.getIdPeptide();
                ArrayList<DPeptidePTM> list = ptmMap.get(peptideId);
                if (list == null) {
                    list =new ArrayList<>();
                    ptmMap.put(peptideId, list);
                }
                list.add(ptm);
                    
            }
                
            subTask = subTaskManager.getNextSubTask();
        }

        return ptmMap;

    }
    
    public static void fetchReadablePTMData(EntityManager entityManagerMSI, Long rsetId, HashMap<Long, Peptide> peptideMap) {
        if ((peptideMap == null) || peptideMap.isEmpty()) return;
        // Retrieve PeptideReadablePtmString
        Query ptmStingQuery = entityManagerMSI.createQuery("SELECT p.id, ptmString "
            + "FROM fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.PeptideReadablePtmString ptmString "
            + "WHERE p.id IN (:listId) AND ptmString.peptide=p AND ptmString.resultSet.id=:rsetId");
        ptmStingQuery.setParameter("listId", peptideMap.keySet());
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
    

    public static void fetchPTMDataForPeptides(EntityManager entityManagerMSI, HashMap<Long, Peptide> peptideById) {

        if (!peptideById.isEmpty()) {
            TypedQuery<DPeptidePTM> ptmQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DPeptidePTM(pptm.peptide.id, pptm.specificity.id, pptm.seqPosition) FROM fr.proline.core.orm.msi.PeptidePtm pptm WHERE pptm.peptide.id IN (:peptideIds)", DPeptidePTM.class);
            ptmQuery.setParameter("peptideIds", peptideById.keySet());
            List<DPeptidePTM> ptmList = ptmQuery.getResultList();

            Iterator<DPeptidePTM> it = ptmList.iterator();
            while (it.hasNext()) {
                DPeptidePTM ptm = it.next();

                Peptide p = peptideById.get(ptm.getIdPeptide());
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
