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
package fr.proline.studio.dam.tasks.data.ptm;

import fr.profi.util.StringUtils;
import fr.proline.core.orm.msi.SequenceMatch;
import fr.proline.core.orm.msi.dto.*;
import fr.proline.core.orm.uds.dto.DDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a set of PTM sites, displayed from a quantification dataset or from an identification dataset.
 * 
 * @author CB205360
 */
public class PTMDataset {
    
    protected static final Logger LOG = LoggerFactory.getLogger("ProlineStudio.DAM.Task");

    private DDataset m_dataset;

    private List<PTMSite> m_proteinPTMSites;

    private List<Long> m_leafRSMIds; // Ids of the Leaf RSM where PTM info are read
    private List<DInfoPTM> m_ptmOfInterest; // Specified PTM to consider 
    private List<PTMCluster> m_ptmClusters;

    //Lists of all PTMPeptideInstance for a specific Peptide and Protein Match : For parent RSM and leaf RSM
    private final Map<Long, Map<Long, List<PTMPeptideInstance>>> m_parentPtmPepInstByPepIdByProtMatchId = new HashMap<>();
    private final Map<Long, Map<Long, List<PTMPeptideInstance>>> m_leafPtmPepInstByPepInstIdByProtMatchId = new HashMap<>();
        
    //VDS: to overcome a missing information in ORM : Link between Protein Matches in merged RSM and child RSM
    private Map<String, List<Long>> m_allLeafProtMatchesIdPerAccession;

    public PTMDataset(DDataset dataset) {
        
        if (dataset == null) throw new IllegalArgumentException("dataset from which PTM sites are extracted cannot be null");
        
        this.m_dataset = dataset;
        m_ptmOfInterest = new ArrayList<>();
    }
    
    public DDataset getDataset() {
        return m_dataset;
    }
    
    public List<Long> getLeafResultSummaryIds(){
        if(m_leafRSMIds == null)
            return Collections.emptyList();
        return new ArrayList<>(m_leafRSMIds);
    }
    
    public void setLeafResultSummaryIds(List<Long> l){
        m_leafRSMIds = new ArrayList<>(l);
    }

    
    public boolean isIdentification() {
      return m_dataset.isIdentification();
    }

    public boolean isQuantitation() {
      return m_dataset.isQuantitation();
    }
    
    public void addInfoPTM(DInfoPTM ptm){
        m_ptmOfInterest.add(ptm);
    }

    public List<DInfoPTM> getInfoPTMs(){
        return  m_ptmOfInterest;
    }

    public List<PTMSite> getPTMSites() {
        return m_proteinPTMSites;
    }

    public void setPTMSites(List<PTMSite> proteinPTMSites) {        
        proteinPTMSites.forEach(site -> site.setDataset(this));
        this.m_proteinPTMSites = proteinPTMSites;
    }

    public List<PTMCluster> getPTMClusters() {
        return m_ptmClusters;
    }

    public PTMCluster getPTMCluster(Long clusterId) {
        Optional<PTMCluster> clusterOp =  m_ptmClusters.stream().filter(c -> c.getId().equals(clusterId)).findFirst();
        if(clusterOp.isPresent())
            return  clusterOp.get();
        else
            return null;
    }


//
//    public List<PTMCluster> getSiteAsPTMClusters() {
//        Map<PTMSite, List<PTMCluster>> clustersPerSite = new HashMap<>();
//        for(PTMCluster c : m_ptmClusters){
//            for(PTMSite site : c.getPTMSites() ){
//                List<PTMCluster> clusters = clustersPerSite.computeIfAbsent(site, k -> new ArrayList<>());
//                clusters.add(c);
//            }
//        }
//
//        if(m_ptmSiteAsClusters == null){
//            m_ptmSiteAsClusters = new ArrayList<>();
//            for (PTMSite site: m_proteinPTMSites) {
//                PTMCluster ptmCluster = new PTMCluster(site.getId(), site.getLocalisationConfidence(), Collections.singletonList(site.getId()), site.getPeptideIds() , this);
//                if (ptmCluster.getPTMSites() == null || ptmCluster.getPTMSites().isEmpty()) {
//                    continue;
//                }
//                DPeptideMatch bestPepMatch = site.getMostConfidentPepMatch();
//                ptmCluster.setRepresentativePepMatch(bestPepMatch);
//
//                if(isQuantitation()) {
//                    ptmCluster.setQuantProteinSet(site.getMasterQuantProteinSet());
//
//                    Long bestPepMatchPepId = bestPepMatch.getPeptide().getId();
//                    if(clustersPerSite.containsKey(site)) {
//                        for (PTMCluster cl : clustersPerSite.get(site)) {
//                            Long clPepId = null;
//                            if (cl.getRepresentativeMQPepMatch() != null && cl.getRepresentativeMQPepMatch().getPeptideInstance() != null) {
//                                clPepId = cl.getRepresentativeMQPepMatch().getPeptideInstance().getPeptideId();
//                            } else if (cl.getRepresentativeMQPepMatch() != null && cl.getRepresentativeMQPepMatch().getRepresentativePepMatch() != null && cl.getRepresentativeMQPepMatch().getRepresentativePepMatch().getPeptide() != null) {
//                                clPepId = cl.getRepresentativeMQPepMatch().getRepresentativePepMatch().getPeptide().getId();
//                            }
//
//                            if (clPepId != null && clPepId.equals(bestPepMatchPepId)) {
//                                DMasterQuantPeptide mqPep = cl.getRepresentativeMQPepMatch();
//                                ptmCluster.setRepresentativeMQPepMatch(mqPep);
//                                break;
//                            }
//                        }
//                    }
//                    if(ptmCluster.getRepresentativeMQPepMatch() == null){
//                        LOG.warn(" In QUANT Dataset did not found getRepresentativeMQPepMatch for siteAscluster id "+ptmCluster.getId());
//                    }
//
//                }
//                m_ptmSiteAsClusters.add(ptmCluster);
//            }
//        }
//        return m_ptmSiteAsClusters;
//    }

    public void setPTMClusters(List<PTMCluster> ptmClusters) {
        this.m_ptmClusters = ptmClusters;
    }
   
    public PTMSite getPTMSite(Long id){
        Optional<PTMSite> ptmSite = m_proteinPTMSites.stream().filter(site -> site.getId().equals(id)).findFirst();
        return ptmSite.orElse(null);
    }
    
    public void setQuantProteinSets(List<DMasterQuantProteinSet> masterQuantProteinSetList, Map<Long, Long> typicalProteinMatchIdByProteinMatchId) {
        Map<Long, DMasterQuantProteinSet> mqProteinSetByProteinMatchId = new HashMap<>();
        
        for(DMasterQuantProteinSet mqps : masterQuantProteinSetList) {
            if (mqps.getProteinSet() != null) {
                mqProteinSetByProteinMatchId.put(mqps.getProteinSet().getProteinMatchId(), mqps);
            } 
        }        
        
        for (PTMSite site : m_proteinPTMSites) {
            Long typicalPMId = typicalProteinMatchIdByProteinMatchId.get(site.getProteinMatch().getId());
            DMasterQuantProteinSet mqps = mqProteinSetByProteinMatchId.get(typicalPMId);
            site.setQuantProteinSet(mqps);
        }
        
        if(m_ptmClusters !=null) {
            for(PTMCluster cluster : m_ptmClusters){
                Long typicalPMId = typicalProteinMatchIdByProteinMatchId.get(cluster.getProteinMatch().getId());
                DMasterQuantProteinSet mqps = mqProteinSetByProteinMatchId.get(typicalPMId);
                cluster.setQuantProteinSet(mqps);            
            }
        }
    }

    public boolean isQuantDataLoaded(){
        if(m_ptmClusters == null || m_ptmClusters.isEmpty())
            return false;
        return  (m_ptmClusters.get(0).getMasterQuantProteinSet() != null);
    }

    /**
     * Get all PTMPeptideInstance defined for specified protein match Id and peptide instance.
     * The returned PTMPeptideInstance correspond to parent PTMPeptideInstance
     * 
     * @param proteinMatchId 
     * @param pepInstId
     * @return 
     */
    public List<PTMPeptideInstance> getLeafPTMPeptideInstances(Long proteinMatchId, Long pepInstId) {
        Map<Long, List<PTMPeptideInstance>> m =  m_leafPtmPepInstByPepInstIdByProtMatchId.get(proteinMatchId);
        if(m != null)
            return m.get(pepInstId);
        else
           return Collections.emptyList();
    }
    
    public void addLeafPTMPeptideInstance(PTMPeptideInstance pepInst, Long proteinMatchId){
        Map<Long, List<PTMPeptideInstance>> m =  m_leafPtmPepInstByPepInstIdByProtMatchId.get(proteinMatchId);
        if(m == null){
            m = new HashMap<>();
            List<PTMPeptideInstance> ptmPepInst = new ArrayList<>();
            ptmPepInst.add(pepInst);
            m.put( pepInst.getPeptideInstance().getId(), ptmPepInst );
        } else if (m.get(pepInst.getPeptideInstance().getId()) == null){
            List<PTMPeptideInstance> ptmPepInst = new ArrayList<>();
            ptmPepInst.add(pepInst);            
            m.put( pepInst.getPeptideInstance().getId(), ptmPepInst );
        } else {
            m.get(pepInst.getPeptideInstance().getId()).add(pepInst);
        }
            
        
        m_leafPtmPepInstByPepInstIdByProtMatchId.put(proteinMatchId,m);
    }

    
    /**
     * Get all PTMPeptideInstance defined for specified protein match Id.
     * The returned PTMPeptideInstance correspond to parent PTMPeptideInstance
     * 
     * @param proteinMatchId
     * @return 
     */
    public Collection<PTMPeptideInstance> getPTMPeptideInstances(Long proteinMatchId) {
        Map<Long, List<PTMPeptideInstance>> m =  m_parentPtmPepInstByPepIdByProtMatchId.get(proteinMatchId);
        if(m != null)
            return m.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        else
           return Collections.emptyList();
    }


    /**
     * Get the parent PTMPeptideInstance corresponding to the specified peptide instance identifying specified ProteinMatch
     * If not yet defined, create a new PTMPeptideInstance
     * @param proteinMatch
     * @param peptideInstance
     * @param protPosition
     * @return
     */
    public PTMPeptideInstance getPTMPeptideInstance(DProteinMatch proteinMatch, DPeptideInstance peptideInstance, Integer protPosition) {

        Long proteinMatchId = proteinMatch.getId();
        if (!m_parentPtmPepInstByPepIdByProtMatchId.containsKey(proteinMatchId)) {
            m_parentPtmPepInstByPepIdByProtMatchId.put(proteinMatchId, new HashMap<>());
        }

        Map<Long, List<PTMPeptideInstance>> ptmPepInstanceByPepId = m_parentPtmPepInstByPepIdByProtMatchId.get(proteinMatchId);
        PTMPeptideInstance foundPtmPepIns;
        
        List<PTMPeptideInstance> registeredPtmPepInsts =  ptmPepInstanceByPepId.get(peptideInstance.getPeptideId());
        
        if(registeredPtmPepInsts == null)
            registeredPtmPepInsts = new ArrayList<>();
        
        List<PTMPeptideInstance> potentialPtmPepInsts = registeredPtmPepInsts.stream().filter(ptmPepInst ->( protPosition >= ptmPepInst.getStartPosition() &&  protPosition <=ptmPepInst.getStopPosition())).collect(Collectors.toList());
        
        if (potentialPtmPepInsts.isEmpty()) {
            foundPtmPepIns = new PTMPeptideInstance(peptideInstance);
            registeredPtmPepInsts.add(foundPtmPepIns);
            
            //Get correct start position           
            if(peptideInstance.getPeptideMatches() != null){
                List<SequenceMatch> pepInsSequenceMatches = peptideInstance.getPeptideMatches().stream().map(DPeptideMatch::getSequenceMatch).collect(Collectors.toList());
                for(SequenceMatch sm : pepInsSequenceMatches){
                    if(protPosition >= sm.getId().getStart() && protPosition <= sm.getId().getStop()){
                        //found correct sm
                        foundPtmPepIns.setStartPosition(sm.getId().getStart() );
                        break;
                    }
                }
            } 
            ptmPepInstanceByPepId.put(peptideInstance.getPeptideId(), registeredPtmPepInsts);
        } else {
            foundPtmPepIns = potentialPtmPepInsts.get(0);
            if(potentialPtmPepInsts.size() > 1)
                LOG.warn(" ----- GET PTMPeptideInstance for Prot id "+proteinMatchId+" pep "+peptideInstance.getPeptide().getSequence()+" at position "+protPosition+" FOUND "+potentialPtmPepInsts.size() );
        }

        return foundPtmPepIns;
    }
    
    protected void updateParentPTMPeptideInstanceClusters(boolean updateSiteCount){
        if(m_ptmClusters != null && !m_ptmClusters.isEmpty()){
            m_ptmClusters.forEach( ptmC -> {
                final Integer[] siteCount = {ptmC.getPTMSitesCount()};
                List<PTMPeptideInstance> ptmPepInsForCluster = ptmC.getParentPTMPeptideInstances();
                ptmPepInsForCluster.forEach(
                        peI -> {
                            peI.addCluster(ptmC);
                            if(updateSiteCount) {
                                Long peISiteCount = peI.getPTMSites().stream().filter(ptmSite -> m_ptmOfInterest.contains(ptmSite.getPTMSpecificity())).count();
                                if (siteCount[0] < peISiteCount)
                                    siteCount[0] = peISiteCount.intValue();
                            }
                    });
                    ptmC.setPTMSitesCount(siteCount[0]);
            } );
            
        }                
    }
    
    public void setLeafProtMatchesIdPerAccession(Map<String, List<Long>> allProtMatchesIdPerAccession){
        m_allLeafProtMatchesIdPerAccession = allProtMatchesIdPerAccession;
    }
    
    public List<Long> getProtMatchesIdForAccession(String protMatcherAccession){
        return m_allLeafProtMatchesIdPerAccession.getOrDefault(protMatcherAccession, new ArrayList<>());
    }

    public List<PTMCluster> getColocatedClusters(PTMCluster cluster){

        ArrayList<PTMCluster> colocatedClusters = new ArrayList();
        DProteinMatch pmatch = cluster.getProteinMatch();

        int minStart = -1;
        int maxEnd = -1;
        try {
            minStart = cluster.getParentPTMPeptideInstances().stream().map(pi -> pi.getStartPosition()).min(Comparator.naturalOrder()).get();
            maxEnd = cluster.getParentPTMPeptideInstances().stream().map(pi -> pi.getStopPosition()).max(Comparator.naturalOrder()).get();
        }catch (NoSuchElementException e){
            LOG.error("Errpr getting peptides min/max bounds for cluster",e );
            return  colocatedClusters;
        }

        for(PTMCluster c : this.m_ptmClusters){
            if(c.getProteinMatch().equals(pmatch)) {
                if(c.getParentPTMPeptideInstances() != null) {
                    int clMinStart = c.getParentPTMPeptideInstances().stream().map(pi -> pi.getStartPosition()).min(Comparator.naturalOrder()).get();
                    int clMaxEnd = c.getParentPTMPeptideInstances().stream().map(pi -> pi.getStopPosition()).max(Comparator.naturalOrder()).get();
                    if( ( minStart <= clMinStart && clMinStart < maxEnd ) ||  ( minStart <= clMaxEnd && clMaxEnd <= maxEnd ) || ( clMinStart <= minStart && minStart < clMaxEnd ))
                        colocatedClusters.add(c);
                }
            }
        }
        return  colocatedClusters;
    }

    private boolean areColocalized(PTMCluster firstCluster, PTMCluster secondCluster){
        if(firstCluster == null || secondCluster == null)
            return false;

        DProteinMatch pmatch = firstCluster.getProteinMatch();
        if(!pmatch.equals(secondCluster.getProteinMatch()))
            return false;

        //Get first cluster bounds
        int minStart = -1;
        int maxEnd = -1;
        try {
            minStart = firstCluster.getParentPTMPeptideInstances().stream().map(pi -> pi.getStartPosition()).min(Comparator.naturalOrder()).get();
            maxEnd = firstCluster.getParentPTMPeptideInstances().stream().map(pi -> pi.getStopPosition()).max(Comparator.naturalOrder()).get();
        }catch (NoSuchElementException e){
            LOG.error("Error getting peptides min/max bounds for cluster",e );
            return  false;
        }

        if(secondCluster.getParentPTMPeptideInstances() != null) {
            int clMinStart = secondCluster.getParentPTMPeptideInstances().stream().map(pi -> pi.getStartPosition()).min(Comparator.naturalOrder()).get();
            int clMaxEnd = secondCluster.getParentPTMPeptideInstances().stream().map(pi -> pi.getStopPosition()).max(Comparator.naturalOrder()).get();
            if( ( minStart <= clMinStart && clMinStart < maxEnd ) ||  ( minStart <= clMaxEnd && clMaxEnd <= maxEnd ) || ( clMinStart <= minStart && minStart < clMaxEnd ))
                return  true;
        }


        return false;
    }

    public boolean mergeClusters(List<PTMCluster> clusters2Merge){

        if(clusters2Merge == null  || clusters2Merge.size() <= 1)
            return false;

        List<PTMCluster> finalClusters2Merge =  clusters2Merge.stream().filter( c -> c.getSelectionLevel()>=2).collect(Collectors.toList());
        if(finalClusters2Merge.size() <= 1)
            return false;

        PTMCluster firstCluster = finalClusters2Merge.get(0);
        finalClusters2Merge =  finalClusters2Merge.stream().filter( c -> areColocalized(firstCluster, c)).collect(Collectors.toList());

        if(finalClusters2Merge.size() <= 1)
            return false;

        List<Long> siteIds = firstCluster.getPTMSites().stream().map(PTMSite::getId).collect(Collectors.toList());
        PTMCluster mergedCluster = new PTMCluster(firstCluster.getId(), firstCluster.getLocalizationConfidence(), firstCluster.getSelectionLevel(),
                firstCluster.getSelectionNotation(), firstCluster.getSelectionInfo(), siteIds, firstCluster.getPeptideIds(), this);
        mergedCluster.setRepresentativePepMatch(firstCluster.getRepresentativePepMatch());

        //If Quant Data, Get data to calculate MqPeptide for merged
        //Warning : assume cluster representative MQPepMatch is an AggregatedMasterQuantPeptide !
        Map<Long, DMasterQuantPeptide> mqPepByPepInstId = new HashMap<>();
        if(isQuantitation()) {
            DMasterQuantPeptide finalClusterMQpep = firstCluster.getRepresentativeMQPepMatch();
            if (finalClusterMQpep instanceof AggregatedMasterQuantPeptide) {
                ((AggregatedMasterQuantPeptide) finalClusterMQpep).getAggregatedMQPeptides().forEach(mqPep -> {
                    mqPepByPepInstId.put(mqPep.getPeptideInstanceId(), mqPep);
                });
            } else {
                //Should not occur
                mqPepByPepInstId.put(finalClusterMQpep.getPeptideInstanceId(), finalClusterMQpep);
            }
        }

        // Go through Clusters and merge data into 'mergedCluster', if next cluster is colocated with first one !
        for(int i=1; i<finalClusters2Merge.size(); i++){
            PTMCluster nextCluster = finalClusters2Merge.get(i);

            if(areColocalized(firstCluster, nextCluster)){

                //Site count correspond to the max site count of all merged cluster, not the sum of sites
                if(mergedCluster.getPTMSitesCount() < nextCluster.getPTMSitesCount())
                    mergedCluster.setPTMSitesCount(nextCluster.getPTMSitesCount());

                //Add next cluster data to merged cluster
                mergedCluster.addSites(nextCluster.getPTMSites());
                mergedCluster.addPeptideIds(nextCluster.getPeptideIds());

                // set max LocalizationConfidence() as merged cluster LocalizationConfidence()
                if(mergedCluster.getLocalizationConfidence() < nextCluster.getLocalizationConfidence())
                    mergedCluster.setLocalizationConfidence(nextCluster.getLocalizationConfidence());

                //Set best Peptide match considering all clusters as representative for merged cluster. Best = greater  mascot delta score
                DPeptideMatch currentPepM = mergedCluster.getRepresentativePepMatch();
                DPeptideMatch nextPepM = nextCluster.getRepresentativePepMatch();
                if(currentPepM.getPtmSiteProperties() != null && currentPepM.getPtmSiteProperties().getMascotDeltaScore() != null){
                    if( (nextPepM.getPtmSiteProperties() !=null && nextPepM.getPtmSiteProperties().getMascotDeltaScore() != null)
                            && nextPepM.getPtmSiteProperties().getMascotDeltaScore() > currentPepM.getPtmSiteProperties().getMascotDeltaScore()){
                        mergedCluster.setRepresentativePepMatch(nextPepM);
                    }

                } else {
                    if(nextPepM.getPtmSiteProperties() !=null && nextPepM.getPtmSiteProperties().getMascotDeltaScore() != null){
                        mergedCluster.setRepresentativePepMatch(nextPepM);
                    }
                }

                //For all cluster ptmPeptideInstance, change associated cluster to merged one
                nextCluster.getParentPTMPeptideInstances().forEach(ptmPepI -> {
                    ptmPepI.addCluster(mergedCluster);
                    ptmPepI.removeCluster(nextCluster);
                });

                nextCluster.getLeafPTMPeptideInstances().forEach(ptmPepI -> {
                    ptmPepI.addCluster(mergedCluster);
                    ptmPepI.removeCluster(nextCluster);
                });


                // If Quanti data get quant information to calculated new adundance
                if(isQuantitation()) {
                    DMasterQuantPeptide nextClusterMQpep = nextCluster.getRepresentativeMQPepMatch();
                    if (nextClusterMQpep instanceof AggregatedMasterQuantPeptide) {
                        ((AggregatedMasterQuantPeptide) nextClusterMQpep).getAggregatedMQPeptides().forEach(mqPep -> {
                            if (!mqPepByPepInstId.containsKey(mqPep.getPeptideInstance().getPeptideId()))
                                mqPepByPepInstId.put(mqPep.getPeptideInstanceId(), mqPep);
                        });
                    } else {
                        //Should not occur
                        mqPepByPepInstId.put(nextClusterMQpep.getPeptideInstanceId(), nextClusterMQpep);
                    }
                }

            } //End clusters are colocalized
        } //End go through clusters

        if(isQuantitation())
            mergedCluster.setRepresentativeMQPepMatch(getRepresentativeMQPeptideForCluster(mergedCluster, mqPepByPepInstId));

        m_ptmClusters.removeAll(finalClusters2Merge);
        m_ptmClusters.add(mergedCluster);
        return true;

    }

    public JSONPTMDataset createJSONPTMDataset() throws IllegalAccessException {
        JSONPTMDataset ptmDS = new JSONPTMDataset();

        List<DInfoPTM> ptmInfos = getInfoPTMs();
        List<Long> ptmInfoIds = new ArrayList<>();
        for(int i=0 ; i<ptmInfos.size();i++){
            if(!ptmInfoIds.contains(ptmInfos.get(i).getIdPtm()))
                ptmInfoIds.add(ptmInfos.get(i).getIdPtm());
        }
        ptmDS.ptmIds = ptmInfoIds.toArray(new Long[ptmInfoIds.size()]);

        List<Long> leafRsmIds =getLeafResultSummaryIds();
        Long[] rsmIds = new Long[leafRsmIds.size()];
        for(int i=0 ; i<leafRsmIds.size();i++){
            rsmIds[i] = leafRsmIds.get(i);
        }
        ptmDS.leafResultSummaryIds =rsmIds;

        //--- Read Sites
        List<PTMSite> allSites = getPTMSites();
        JSONPTMSite2[] allJSONSites = new JSONPTMSite2[allSites.size()];
        for(int i=0 ; i<allSites.size();i++){
            AbstractJSONPTMSite newtPTMSite =  allSites.get(i).getJSONPtmSite();
            if(newtPTMSite instanceof JSONPTMSite2)
                allJSONSites[i] =(JSONPTMSite2) newtPTMSite;
            else {
                throw new IllegalAccessException("Can't Export old PTM Site informations ");
            }
        }
        ptmDS.ptmSites =allJSONSites;

        //--- Read Clustes
        List<PTMCluster> allClusters = getPTMClusters();
        JSONPTMCluster[] allJSONClusters = new JSONPTMCluster[allClusters.size()];
        for(int i=0 ; i<allClusters.size();i++){

            PTMCluster nextCluster = allClusters.get(i);
            JSONPTMCluster newtPTMCluster  = new JSONPTMCluster();
            newtPTMCluster.id = nextCluster.getId();
            newtPTMCluster.selectionLevel = nextCluster.getSelectionLevel();
            if(nextCluster.getSelectionNotation() != null)
                newtPTMCluster.selectionConfidence = nextCluster.getSelectionNotation();
            if(StringUtils.isNotEmpty(nextCluster.getSelectionInfo()))
                newtPTMCluster.selectionInformation = nextCluster.getSelectionInfo();
            newtPTMCluster.bestPeptideMatchId = nextCluster.getRepresentativePepMatch().getId();
            newtPTMCluster.localizationConfidence = nextCluster.getLocalizationConfidence();
            newtPTMCluster.isomericPeptideIds = new Long[0];

            allSites = nextCluster.getPTMSites();
            Long[] clusterJSONSites = new Long[allSites.size()];
            for(int index =0 ; index<allSites.size();index++){
                AbstractJSONPTMSite newtPTMSite =  allSites.get(index).getJSONPtmSite();
                if(newtPTMSite instanceof JSONPTMSite2)
                    clusterJSONSites[index] =((JSONPTMSite2) newtPTMSite).id;
                else {
                    throw new IllegalAccessException("Can't Export old PTM Site informations ");
                }
            }
            newtPTMCluster.ptmSiteLocations = clusterJSONSites;
            newtPTMCluster.peptideIds = nextCluster.getPeptideIds().toArray(new Long[0]);
            allJSONClusters[i] = newtPTMCluster;
        }
        ptmDS.ptmClusters =allJSONClusters;
        return ptmDS;
    }

    public DMasterQuantPeptide getRepresentativeMQPeptideForCluster(PTMCluster cluster, Map<Long, DMasterQuantPeptide> mqPepByPepInstId ){
        DMasterQuantPeptide bestMQPep = null;

        if (cluster.getMasterQuantProteinSet() != null) {

            // Get  Parent DPeptideInstance
            List<DPeptideInstance> parentPeptideInstances = cluster.getParentPeptideInstances();

            //
            // Sum of peptides
            List<DMasterQuantPeptide> mqPeps = parentPeptideInstances.stream().map(parentPepI -> mqPepByPepInstId.get(parentPepI.getId())).filter(Objects::nonNull).collect(Collectors.toList());
            bestMQPep = new AggregatedMasterQuantPeptide(mqPeps, m_dataset.getMasterQuantitationChannels().get(0));
        }
        return bestMQPep;
    }
}
