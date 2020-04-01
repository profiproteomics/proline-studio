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
package fr.proline.studio.dam.tasks.data.ptm;

import fr.proline.core.orm.msi.SequenceMatch;
import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.uds.dto.DDataset;
import java.util.ArrayList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a set of PTM sites, displayed from a quantification dataset or from an identification dataset.
 * 
 * @author CB205360
 */
public class PTMDataset {
    
    protected static final Logger LOG = LoggerFactory.getLogger("ProlineStudio.DAM.Task");

    private DDataset m_dataset;

    private List<PTMSite> m_proteinPTMSites;
    
    //data for v2
    private List<Long> m_leafRSMIds; // Ids of the Leaf RSM where PTM info are read
    private List<DInfoPTM> m_ptmOfInterest; // Specified PTM to consider 
    private List<PTMCluster> m_ptmClusters; 
    
    //Lists of all PTMPeptideInstance for a specific Peptide and Protein Match : For parent RSM and leaf RSM
    private final Map<Long, Map<Long, List<PTMPeptideInstance>>> m_parentPtmPepInstByPepIdByProtMatchId = new HashMap<>();
    private final Map<Long, Map<Long, List<PTMPeptideInstance>>> m_leafPtmPepInstByPepInstIdByProtMatchId = new HashMap<>();
        
    //VDS: to overcome a missing information in ORM : Link between Protein Matches in merged RSM and child RSM
    private Map<String, List<Long>> m_allLeafProtMatchesIdPerAccession;
    private Boolean m_isVersion2;
    
    public PTMDataset(DDataset dataset) {
        
        if (dataset == null) throw new IllegalArgumentException("dataset from which PTM sites are extracted cannot be null");
        
        this.m_dataset = dataset;
        m_isVersion2 = false;
        m_ptmOfInterest = new ArrayList<>();
    }
    
    public DDataset getDataset() {
        return m_dataset;
    }
    
    public List<Long> getLeafResultSummaryIds(){
        if(!isVersion2() || m_leafRSMIds == null)
            return Collections.<Long>emptyList();
        return new ArrayList(m_leafRSMIds);
    }
    
    public void setLeafResultSummaryIds(List<Long> l){
        m_leafRSMIds = new ArrayList<>(l);
    }
    
    public boolean isVersion2(){
        return m_isVersion2;
    } 

    public void setIsVersion2(boolean isV2){
        m_isVersion2 = isV2;
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
    
    public List<PTMSite> getPTMSites() {
        return m_proteinPTMSites;
    }

    public void setPTMSites(List<PTMSite> proteinPTMSites) {        
        proteinPTMSites.stream().forEach(site -> site.setDataset(this));
        this.m_proteinPTMSites = proteinPTMSites;
    }

    public List<PTMCluster> getPTMClusters() {
        return m_ptmClusters;
    }

    public void setPTMClusters(List<PTMCluster> ptmClusters) {        
        this.m_ptmClusters = ptmClusters;
    }
   
    public PTMSite getPTMSite(Long id){
        if(!isVersion2())
            return null;
        else {
            Optional<PTMSite> ptmSite = m_proteinPTMSites.stream().filter(site -> site.getid().equals(id)).findFirst();
            return ptmSite.orElse(null);            
        }
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
           return Collections.EMPTY_LIST;
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
            return m.values().stream().flatMap(entry -> entry.stream()).collect(Collectors.toList());           
        else
           return Collections.EMPTY_LIST;
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
                List<SequenceMatch> pepInsSequenceMatches = peptideInstance.getPeptideMatches().stream().map(pepM -> pepM.getSequenceMatch()).collect(Collectors.toList());
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
    
    public void updateParentPTMPeptideInstanceClusters(){
        if(m_ptmClusters != null && !m_ptmClusters.isEmpty()){
            m_ptmClusters.forEach( ptmC -> {
                List<PTMPeptideInstance> ptmPepInsForCluster = ptmC.getParentPTMPeptideInstances();
                ptmPepInsForCluster.forEach( peI -> peI.addCluster(ptmC));
            } );
            
        }                
    }
    
    public void setLeafProtMatchesIdPerAccession(Map<String, List<Long>> allProtMatchesIdPerAccession){
        m_allLeafProtMatchesIdPerAccession = allProtMatchesIdPerAccession;
    }
    
    public List<Long> getProtMatchesIdForAccession(String protMatcherAccession){
        return m_allLeafProtMatchesIdPerAccession.getOrDefault(protMatcherAccession, new ArrayList<>());
    }
    
}
