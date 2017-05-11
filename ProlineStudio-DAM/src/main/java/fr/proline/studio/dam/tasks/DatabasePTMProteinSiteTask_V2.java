package fr.proline.studio.dam.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import fr.proline.core.orm.msi.ObjectTree;
import fr.proline.core.orm.msi.ObjectTreeSchema;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideReadablePtmString;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DSpectrum;
import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.core.orm.msi.dto.DPtmSiteProperties;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import static fr.proline.studio.dam.tasks.AbstractDatabaseTask.m_logger;
import fr.proline.studio.dam.tasks.data.PTMSite;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;


/**
 *
 * @author JM235353
 */
public class DatabasePTMProteinSiteTask_V2 extends AbstractDatabaseTask {

    private long m_projectId = -1;
    private ResultSummary m_rsm = null;
    private PTMSite m_ptmSiteToFill = null;
    
    private ArrayList<PTMSite> m_ptmSiteArray = null;    
    final int SLICE_SIZE = 1000;
    
    private int m_action; // Specify whch action to run
    
    private final static int LOAD_ALL_PTM_SITES_FOR_RSMS = 0;
    private final static int FILL_PTM_SITE_PEPINFO= 1;
    
    public DatabasePTMProteinSiteTask_V2(AbstractDatabaseCallback callback, long projectId, ResultSummary rsm, ArrayList<PTMSite> ptmSiteArray) {
        super(callback, new TaskInfo("Load All PTM Sites for " + rsm.getId(), false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_rsm = rsm;
        m_ptmSiteArray = ptmSiteArray;
        m_action = LOAD_ALL_PTM_SITES_FOR_RSMS;
    }

    public DatabasePTMProteinSiteTask_V2(AbstractDatabaseCallback callback, long projectId, ResultSummary rsm, PTMSite ptmSiteToFill) {
        super(callback, new TaskInfo("Load peptides for PTM Sites " + ptmSiteToFill, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_projectId = projectId;
        m_ptmSiteToFill = ptmSiteToFill;
        m_rsm = rsm;
        m_action = FILL_PTM_SITE_PEPINFO;
    }
        
    @Override
    public boolean needToFetch() {
        switch(m_action) {
            case LOAD_ALL_PTM_SITES_FOR_RSMS: {
                return true;
            }  
            case FILL_PTM_SITE_PEPINFO: {
                return !m_ptmSiteToFill.isAllPeptideMatchesLoaded();
            }              
        }
        return false; // should not be called 
    }

    @Override
    public boolean fetchData() {
        if (needToFetch()) {
            switch (m_action) {
                case LOAD_ALL_PTM_SITES_FOR_RSMS: {
                    return fetchAllProteinSets();
                }
                case FILL_PTM_SITE_PEPINFO: {
                    return loadPTMSitePeptideMatches();
                }
                
            }
           
            return true; // should not happen
        }
        return true; // should not happen
    }

    private boolean loadPTMSitePeptideMatches(){
        
        long start = System.currentTimeMillis();        
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            long stop = System.currentTimeMillis();
            m_logger.info("MSI EMF create and transaction started in {} ms", (stop-start));
            start = stop;
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            
            HashMap<Long, Peptide> allPeptidesMap = new HashMap();
            HashMap<Long, List<DPeptideMatch>> peptideMatchByPepInstanceID = new HashMap<>();
            
   
            //---- Load Peptide Match + Spectrum / MSQuery information for all peptideInstance of PTMSite
            // SELECT pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank,
            // pm.sdPrettyRank, pm.serializedProperties, sp.firstTime, sp.precursorIntensity, sp.title, p, ms.id, ms.initialId, pi.id
            // FROM PeptideInstancePeptideMatchMap pipm, PeptideMatch pm, PeptideInstance pi, Peptide p, MsQuery ms, Spectrum sp"
            // WHERE  pipm.id.peptideInstanceId IN (:peptideInstanceList) AND pipm.id.peptideInstanceId=pi.id AND pipm.id.peptideMatchId=pm.id 
            // AND pm.peptideId=p.id AND pm.msQuery=ms AND ms.spectrum=sp
            Query peptidesQuery = entityManagerMSI.createQuery("SELECT pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.serializedProperties, sp.firstTime, sp.precursorIntensity, sp.title, pi.id, p, ms.id, ms.initialId\n"
                        + "              FROM fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.MsQuery ms, fr.proline.core.orm.msi.Spectrum sp  \n"
                        + "              WHERE pipm.id.peptideInstanceId IN ( :peptideInstanceList ) AND pipm.id.peptideInstanceId=pi.id AND pipm.id.peptideMatchId=pm.id AND pm.peptideId=p.id AND pm.msQuery=ms AND ms.spectrum=sp ");

            peptidesQuery.setParameter("peptideInstanceList",Arrays.asList(m_ptmSiteToFill.peptideInstanceIds));
                
            List l = peptidesQuery.getResultList(); 
            Iterator<Object[]> itPeptidesQuery = l.iterator();
            
            //---- Create List of DPeptideMatch (linked to DSpectrum + DMsQuery) from query resumlt 
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

                Long piId = (Long) resCur[14];
                if (!peptideMatchByPepInstanceID.containsKey(piId)) {
                    peptideMatchByPepInstanceID.put(piId, new ArrayList<>());
                }
                peptideMatchByPepInstanceID.get(piId).add(pm);

                Peptide p = (Peptide) resCur[15];
                p.getTransientData().setPeptideReadablePtmStringLoaded();
                allPeptidesMap.put(p.getId(), p);

                Long msqId = (Long) resCur[16];
                Integer msqInitialId = (Integer) resCur[17];

                DMsQuery msq = new DMsQuery(pmId, msqId, msqInitialId, precursorIntensity);
                msq.setDSpectrum(spectrum);

                pm.setPeptide(p);
                pm.setMsQuery(msq);

            }
                               
            stop = System.currentTimeMillis();
            m_logger.info("PSMs for PTM Site created in {} ms", (stop-start));
            start = stop;
            
            //--- Retrieve PeptideReadablePtmString
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
            m_logger.info("PeptideReadablePtmString loaded in {} ms", (stop-start));
            start = stop;
            
            // fetch Generic PTM Data
            fetchGenericPTMData();
            
            stop = System.currentTimeMillis();
            m_logger.info("Generic PTM data loaded in {} ms", (stop-start));
            start = stop;
            
            //--- fetch Specific Data for the Peptides Found
            HashMap<Long, ArrayList<DPeptidePTM>> ptmMap = fetchPTMDataForPeptides(new ArrayList(allPeptidesMap.keySet()));
            List<DPeptideMatch> allpeptideMatches = peptideMatchByPepInstanceID.values().stream().flatMap(listPepM -> listPepM.stream()).collect(Collectors.toList());
            
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
            
            stop = System.currentTimeMillis();
            m_logger.info("PTM data for peptides loaded in {} ms", (stop-start));
            start = stop;
            
            // create the list of PTMSites            
            m_ptmSiteToFill.setPepInstancePepMatchesMap(peptideMatchByPepInstanceID);
                        
            stop = System.currentTimeMillis();
            m_logger.info("PTM Sites filled in {} ms", (stop-start));

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
    
    private boolean fetchAllProteinSets() {
        
        long start = System.currentTimeMillis();
        
        EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
        try {

            entityManagerMSI.getTransaction().begin();

            long stop = System.currentTimeMillis();
            m_logger.info("MSI EMF create and transaction started in {} ms", (stop-start));
            start = stop;
            
            Long rsmId = m_rsm.getId();
            
            // Load Typical Protein Matches
            // SELECT new fr.proline.core.orm.msi.dto.DProteinMatch( pm.id, pm.accession, pm.score, pm.peptideCount, pm.resultSet.id,
            //              pm.description, pepset.id, pepset.score, pepset.sequenceCount, pepset.peptideCount, pepset.peptideMatchCount, pepset.resultSummaryId) 
            // FROM PeptideSetProteinMatchMap pset_to_pm JOIN pset_to_pm.proteinMatch as pm JOIN pset_to_pm.peptideSet as pepset JOIN pepset.proteinSet as ps 
            // WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true AND ps.representativeProteinMatchId=pm.id  ORDER BY pepset.score DESC"
            TypedQuery<DProteinMatch> typicalProteinQuery = entityManagerMSI.createQuery("SELECT new fr.proline.core.orm.msi.dto.DProteinMatch(pm.id, pm.accession, pm.score, pm.peptideCount, pm.resultSet.id, pm.description, pm.serializedProperties, pepset.id, pepset.score, pepset.sequenceCount, pepset.peptideCount, pepset.peptideMatchCount, pepset.resultSummaryId) FROM PeptideSetProteinMatchMap pset_to_pm JOIN pset_to_pm.proteinMatch as pm JOIN pset_to_pm.peptideSet as pepset JOIN pepset.proteinSet as ps WHERE ps.resultSummary.id=:rsmId AND ps.isValidated=true AND ps.representativeProteinMatchId=pm.id  ORDER BY pepset.score DESC", DProteinMatch.class);
            
            typicalProteinQuery.setParameter("rsmId", rsmId);
            List<DProteinMatch> typicalProteinMatchesArray = typicalProteinQuery.getResultList();
            
            ResultSummary rsm = entityManagerMSI.find(ResultSummary.class, rsmId);
            ObjectTree ot = entityManagerMSI.find(ObjectTree.class, rsm.getObjectTreeIdByName().get(ObjectTreeSchema.SchemaName.PTM_SITES.toString()));
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            List<PTMSite> values = mapper.readValue(ot.getClobData(), mapper.getTypeFactory().constructCollectionType(List.class, PTMSite.class));

            Long[] bestPeptideMatchIdsArray = values.stream().map(site -> site.bestPeptideMatchId).distinct().toArray(Long[]::new);
                                    
            stop = System.currentTimeMillis();
            m_logger.info("{} typical ProtMatches and {} PTMSItes loaded in {} ms", typicalProteinMatchesArray.size(), values.size(), (stop-start));
            start = stop;
            
            HashMap<Long, Peptide> allPeptidesMap = new HashMap();
            ArrayList<DPeptideMatch> peptideMatchArray = new ArrayList<>();
            
            SubTaskManager subTaskManager = new SubTaskManager(1);
            SubTask subTask = subTaskManager.sliceATaskAndGetFirst(0, bestPeptideMatchIdsArray.length, SLICE_SIZE);
            while (subTask != null) {
//                Query peptidesQuery = entityManagerMSI.createQuery("SELECT pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.serializedProperties, sp.firstTime, sp.precursorIntensity, sp.title, p, ms.id, ms.initialId\n"
//                        + "              FROM fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.MsQuery ms, fr.proline.core.orm.msi.Spectrum sp  \n"
//                        + "              WHERE pipm.id.peptideMatchId IN (:peptideMatchList) AND pipm.resultSummary.id = :rsmId AND pipm.id.peptideInstanceId=pi.id AND pipm.id.peptideMatchId=pm.id AND pm.peptideId=p.id AND pm.msQuery=ms AND ms.spectrum=sp ");
//                peptidesQuery.setParameter("peptideMatchList", subTask.getSubList(Arrays.asList(bestPeptideMatchIdsArray)));
//                peptidesQuery.setParameter("rsmId", rsmId);

                Query peptidesQuery = entityManagerMSI.createQuery("SELECT pm.id, pm.rank, pm.charge, pm.deltaMoz, pm.experimentalMoz, pm.missedCleavage, pm.score, pm.resultSet.id, pm.cdPrettyRank, pm.sdPrettyRank, pm.serializedProperties, sp.firstTime, sp.precursorIntensity, sp.title, p, ms.id, ms.initialId\n"
                        + "              FROM fr.proline.core.orm.msi.PeptideInstancePeptideMatchMap pipm, fr.proline.core.orm.msi.PeptideMatch pm, fr.proline.core.orm.msi.PeptideInstance pi, fr.proline.core.orm.msi.Peptide p, fr.proline.core.orm.msi.MsQuery ms, fr.proline.core.orm.msi.Spectrum sp  \n"
                        + "              WHERE pipm.id.peptideMatchId IN (:peptideMatchList) AND pipm.id.peptideInstanceId=pi.id AND pipm.id.peptideMatchId=pm.id AND pm.peptideId=p.id AND pm.msQuery=ms AND ms.spectrum=sp ");
                peptidesQuery.setParameter("peptideMatchList", subTask.getSubList(Arrays.asList(bestPeptideMatchIdsArray)));
                
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
            m_logger.info("PSM from PM created in {} ms", (stop-start));
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
            m_logger.info("PeptideReadablePtmString loaded in {} ms", (stop-start));
            start = stop;
            
            // fetch Generic PTM Data
            fetchGenericPTMData();
            
            stop = System.currentTimeMillis();
            m_logger.info("Generic PTM data loaded in {} ms", (stop-start));
            start = stop;
            
            // fetch Specific Data for the Peptides Found
            HashMap<Long, ArrayList<DPeptidePTM>> ptmMap = fetchPTMDataForPeptides(new ArrayList(allPeptidesMap.keySet()));
        
            for (DPeptideMatch pm : peptideMatchArray) {
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
            
            stop = System.currentTimeMillis();
            m_logger.info("PTM data for peptides loaded in {} ms", (stop-start));
            start = stop;
            
            // create the list of PTMSites            
            Map<Long, DProteinMatch> proteinMatchMap = typicalProteinMatchesArray.stream().collect(Collectors.toMap(item -> item.getId(), item -> item));
            Map<Long, DPeptideMatch> peptideMatchMap = peptideMatchArray.stream().collect(Collectors.toMap(item -> item.getId(), item -> item));

            for (PTMSite site : values) {

                site.setProteinMatch(proteinMatchMap.get(site.proteinMatchId));
                site.setBestPeptideMatch(peptideMatchMap.get(site.bestPeptideMatchId));
                site.setPTMSpcificity(DInfoPTM.getInfoPTMMap().get(site.ptmDefinitionId));
                if (site.getProteinMatch() != null) {
                    m_ptmSiteArray.add(site);
                }
            }
            
            stop = System.currentTimeMillis();
            m_logger.info("PTM Sites built in {} ms", (stop-start));
            start = stop;

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

    private boolean fetchGenericPTMData() {

        HashMap<Long, DInfoPTM> infoPTMMAp = DInfoPTM.getInfoPTMMap();
        if (!infoPTMMAp.isEmpty()) {
            return true; // already loaded
        }
        
        EntityManager entityManagerPS = DataStoreConnectorFactory.getInstance().getPsDbConnector().createEntityManager();
        try {

            entityManagerPS.getTransaction().begin();

            TypedQuery<DInfoPTM> ptmInfoQuery = entityManagerPS.createQuery("SELECT new fr.proline.core.orm.msi.dto.DInfoPTM(spec.id, spec.residue, spec.location, ptm.shortName, evidence.composition, evidence.monoMass) \n"
                    + "FROM fr.proline.core.orm.ps.PtmSpecificity as spec, fr.proline.core.orm.ps.Ptm as ptm, fr.proline.core.orm.ps.PtmEvidence as evidence \n"
                    + "WHERE spec.ptm=ptm AND ptm=evidence.ptm AND evidence.type='Precursor' ", DInfoPTM.class);

            List<DInfoPTM> ptmInfoList = ptmInfoQuery.getResultList();

            Iterator<DInfoPTM> it = ptmInfoList.iterator();
            while (it.hasNext()) {
                DInfoPTM infoPTM = it.next();
                DInfoPTM.addInfoPTM(infoPTM);
            }

            entityManagerPS.getTransaction().commit();

            return true;
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerPS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return false;
        } finally {
            entityManagerPS.close();
        }

    }
    
    private HashMap<Long, ArrayList<DPeptidePTM>> fetchPTMDataForPeptides(ArrayList<Long> allPeptidesIds) {


        EntityManager entityManagerPS = DataStoreConnectorFactory.getInstance().getPsDbConnector().createEntityManager();
        try {

            entityManagerPS.getTransaction().begin();

            HashMap<Long, ArrayList<DPeptidePTM>> ptmMap = new HashMap<>();
            
            SubTaskManager subTaskManager = new SubTaskManager(1);
            SubTask subTask = subTaskManager.sliceATaskAndGetFirst(0, allPeptidesIds.size(), SLICE_SIZE);
            while (subTask != null) {
  
                TypedQuery<DPeptidePTM> ptmQuery = entityManagerPS.createQuery("SELECT new fr.proline.core.orm.msi.dto.DPeptidePTM(pptm.peptide.id, pptm.specificity.id, pptm.seqPosition) \n"
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
                    
                    //Peptide
                    
                }
 
                
                subTask = subTaskManager.getNextSubTask();
            }

            entityManagerPS.getTransaction().commit();

            return ptmMap;
        } catch (Exception e) {
            m_logger.error(getClass().getSimpleName() + " failed", e);
            m_taskError = new TaskError(e);
            try {
                entityManagerPS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_logger.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return null;
        } finally {
            entityManagerPS.close();
        }

    }
    

    
}
