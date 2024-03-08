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

import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;

import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a set of PTM sites co-located that are supported by at least a peptide identification showing these modification
 * sites.
 *
 * @author VD225637
 */
public class PTMCluster implements Comparable<PTMCluster>{
    
    private final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ptm");

    private final List<PTMSite> m_sites;
    private int m_sitesCount;
    private final List<Long> m_peptideIds;
    private final Long m_id;
    private Float m_localizationConfidence;

    private int m_selectionLevel;
    private Integer m_selectionNotation;
    private String m_selectionInfo;


    private List<Integer> m_positionsOnProtein;
    private List<Float> m_probabilities;

    private DProteinMatch m_proteinMatch;    
    private DMasterQuantProteinSet m_masterQuantProteinSet;
    private DPeptideMatch m_representativePepMatch;
    private AggregatedMasterQuantPeptide m_representativeMQPepMatch;
    private List<PTMPeptideInstance> m_parentPTMPeptideInstances;
    private List<PTMPeptideInstance> m_leafPTMPeptideInstances;
    private List<DPeptideInstance> m_parentPeptideInstances;

    private final PTMDataset m_ptmDataset;

    public PTMCluster(JSONPTMCluster jsonValue, PTMDataset ptmds) {
        this(jsonValue.id, jsonValue.localizationConfidence, jsonValue.selectionLevel,jsonValue.selectionConfidence, jsonValue.selectionInformation, Arrays.asList(jsonValue.ptmSiteLocations), Arrays.asList(jsonValue.peptideIds), ptmds);
    }

    public PTMCluster(Long id, Float confidence, Integer selectionLevel, Integer selectionNotation, String selectionInfo, List<Long> ptmSiteIds, List<Long> peptideIds, PTMDataset ptmds) {
        m_ptmDataset = ptmds;
        m_peptideIds = new ArrayList<>();
        m_peptideIds.addAll(peptideIds);
        m_localizationConfidence = confidence;
        m_sites = m_ptmDataset.getPTMSites().stream().filter(site -> ptmSiteIds.contains(site.getId())).sorted(Comparator.comparing(PTMSite::getPositionOnProtein)).collect(Collectors.toList());
        m_sitesCount = m_sites.size();
        m_id = id;

        m_selectionLevel = selectionLevel != null ? selectionLevel : 2;
        m_selectionNotation = selectionNotation;
        m_selectionInfo = selectionInfo != null ? selectionInfo : "";

        if(!m_sites.isEmpty()) {
            //Get ProteinMatch from one of the PTMSite : all should have same.
            m_proteinMatch = m_sites.get(0).getProteinMatch();
            m_masterQuantProteinSet = m_sites.get(0).getMasterQuantProteinSet();
        }

    }

    public int getSelectionLevel(){
        return  m_selectionLevel;
    }

    public void setSelectionLevel(int selectionLevel){
        if(selectionLevel < 0  || selectionLevel >3)
            throw new IllegalArgumentException("Invalid selection level specified");

        m_selectionLevel = selectionLevel;
    }

    /**
     *
     * @return PTLCluster status confidence as a positive number. Negative number indicates no notation has been done.
     */
    public Integer getSelectionNotation() {
        return m_selectionNotation;
    }

    public void setSelectionNotation(Integer selectionNotation) {
        this.m_selectionNotation = selectionNotation;
    }

    /**
     * @return Return description of PTMCluster status or empty String if none is defined
     */
    public String getSelectionInfo() {
        return m_selectionInfo;
    }

    public void setSelectionInfo(String selectionInfo) {
        this.m_selectionInfo = selectionInfo;
    }

    protected void addSites(List<PTMSite> newSites){
        boolean listChanged = false;
        for(PTMSite s : newSites){
            if(!m_sites.contains(s)) {
                m_sites.add(s);
                listChanged = true;
            }
        }
        if(listChanged){
            m_positionsOnProtein = null;
            m_probabilities = null;
            m_parentPTMPeptideInstances = null;
            m_leafPTMPeptideInstances = null;
            m_parentPeptideInstances = null;
        }
    }

    protected void addPeptideIds(List<Long> newPepIds){
        boolean listChanged = false;
        for(Long s : newPepIds){
            if(!m_peptideIds.contains(s)) {
                m_peptideIds.add(s);
                listChanged = true;
            }
        }
        if(listChanged){
            m_positionsOnProtein = null;
            m_probabilities = null;
            m_parentPTMPeptideInstances = null;
            m_leafPTMPeptideInstances = null;
            m_parentPeptideInstances = null;
        }
    }

    public void removePeptide(PTMPeptideInstance pep){
        if(pep == null)
            return;
        Long pepId = pep.getPeptideInstance().getPeptideId();
        if(m_peptideIds.contains(pepId)){
            m_peptideIds.remove(pepId);

            //Recalculated MQPep if needed
            AggregatedMasterQuantPeptide finalClusterMQpep = getRepresentativeMQPepMatch();
            if (finalClusterMQpep != null) {
                Map<Long, DMasterQuantPeptide> mqPepByPepInstId = new HashMap<>();
                ((AggregatedMasterQuantPeptide) finalClusterMQpep).getAggregatedMQPeptides().forEach(mqPep -> {
                    if(m_peptideIds.contains(mqPep.getPeptideInstance().getPeptideId()))
                        mqPepByPepInstId.put(mqPep.getPeptideInstanceId(), mqPep);
                });
                setRepresentativeMQPepMatch(m_ptmDataset.getRepresentativeMQPeptideForCluster(this, mqPepByPepInstId));
            }

            m_parentPTMPeptideInstances = null;
            m_leafPTMPeptideInstances = null;
            m_parentPeptideInstances = null;
        }
    }

    protected void setLocalizationConfidence(Float localizationConfidence){
        m_localizationConfidence = localizationConfidence;
    }
    public Long getId() {
        return m_id;
    }

    public DProteinMatch getProteinMatch(){
        return m_proteinMatch;
    }
    
    public PTMDataset getPTMDataset(){
        return m_ptmDataset;
    }
    
    public void setRepresentativePepMatch(DPeptideMatch peptideMatch) {
        m_representativePepMatch = peptideMatch;
    }

    public DPeptideMatch getRepresentativePepMatch() {
        return m_representativePepMatch;
    }
    
    public void setRepresentativeMQPepMatch(AggregatedMasterQuantPeptide quantPeptideMatch) {
        m_representativeMQPepMatch = quantPeptideMatch;
    }

    public Float getLocalizationConfidence() {
        return m_localizationConfidence;
    }

    public AggregatedMasterQuantPeptide getRepresentativeMQPepMatch() {
        return m_representativeMQPepMatch;
    }

    public List<DPeptideInstance> getParentPeptideInstances() {
        if (m_sites == null || m_sites.isEmpty())
            return null;
        if (m_parentPeptideInstances == null) {
            m_parentPeptideInstances = m_sites.stream().flatMap(site -> site.getParentPeptideInstances().stream()).distinct().filter(pi -> m_peptideIds.contains(pi.getPeptideId())).collect(Collectors.toList());
        }
        return m_parentPeptideInstances;
    }
    
    public PTMPeptideInstance getParentPTMPeptideInstance(Long peptideId) {
        if(m_sites == null || m_sites.isEmpty())
            return null;
        PTMPeptideInstance foundPTMPepins = null;
        for(PTMSite site: m_sites){
           PTMSitePeptideInstance ptmSiteIns = site.getPTMSitePeptideInstance(peptideId);
           if(ptmSiteIns!=null ){
               foundPTMPepins = ptmSiteIns.getParentPTMPeptideInstance();
               break;
           }
        }
        return foundPTMPepins;
    }
    
   
    /**
     * Return PARENT PTMPeptideInstance 
     * @return 
     */
    public List<PTMPeptideInstance> getParentPTMPeptideInstances() {
        if(m_sites == null || m_sites.isEmpty())
            return null;
        if (m_parentPTMPeptideInstances == null) {
            List<Integer> sitesPosition = m_sites.stream().map(site -> site.getPositionOnProtein()).collect(Collectors.toList());            
            Collection<PTMPeptideInstance> ptmPeptides = m_ptmDataset.getPTMPeptideInstances(m_proteinMatch.getId());
            m_parentPTMPeptideInstances = new ArrayList<>();
            for (PTMPeptideInstance ptmPepI : ptmPeptides) {
                if (m_peptideIds.contains(ptmPepI.getPeptideInstance().getPeptideId()) && !m_parentPTMPeptideInstances.contains(ptmPepI) ){
                    for(Integer sitePos : sitesPosition){
                        if(sitePos >= ptmPepI.getStartPosition() && sitePos <=ptmPepI.getStopPosition()){
                            m_parentPTMPeptideInstances.add(ptmPepI);
                            break;
                        }
                    }                    
                }
            }
        }
        return m_parentPTMPeptideInstances;
    }
    
    /**
     * Return leaf PTMPeptideInstance 
     * @return 
     */
    public List<PTMPeptideInstance> getLeafPTMPeptideInstances() {
        if(m_sites == null || m_sites.isEmpty())
            return null;
        if (m_leafPTMPeptideInstances == null) {
            m_leafPTMPeptideInstances = new ArrayList<>();            
            Set<PTMSitePeptideInstance> allPTMSitePepInst = new HashSet<>();
            
            // Get PTMSitePeptideInstance for all sites of this Cluster
            for(PTMSite nextSite : m_sites){                                
                allPTMSitePepInst.addAll(nextSite.getPTMSitePeptideInstances().stream().filter(ptmSitePepI ->m_peptideIds.contains(ptmSitePepI.getParentPTMPeptideInstance().getPeptideInstance().getPeptideId())).collect(Collectors.toList()));
            }
                        
            // Create Leaf PTMPeptideInstances for each parent PTMPeptideInstance (get from PTMSitePeptideInstance)
            List<PTMPeptideInstance> parentPTMPepInstanceDone = new ArrayList<>();
            allPTMSitePepInst.forEach(ptmSitePepIns -> {
                if(!parentPTMPepInstanceDone.contains(ptmSitePepIns.getParentPTMPeptideInstance())){
                    ptmSitePepIns.getLeafPepInstances().forEach(pepI -> { 
                        List<PTMPeptideInstance> dsPtmPepIns = m_ptmDataset.getLeafPTMPeptideInstances(m_proteinMatch.getId(),pepI.getId());
                        PTMPeptideInstance associatedParentPTMPepInst = ptmSitePepIns.getParentPTMPeptideInstance();
                        PTMPeptideInstance finalLeafPtmPepI = null;
                        if(dsPtmPepIns == null || dsPtmPepIns.isEmpty()){
                            finalLeafPtmPepI = new PTMPeptideInstance(pepI); 
                            finalLeafPtmPepI.setStartPosition(associatedParentPTMPepInst.getStartPosition());
                            finalLeafPtmPepI.addCluster(this);
                            final PTMPeptideInstance leafPtmPepI = finalLeafPtmPepI;
                            associatedParentPTMPepInst.getPTMSites().forEach(parentSite -> leafPtmPepI.addPTMSite(parentSite) );
                            m_leafPTMPeptideInstances.add(finalLeafPtmPepI); 
                            m_ptmDataset.addLeafPTMPeptideInstance(leafPtmPepI, m_proteinMatch.getId());
                        } else {
                            m_leafPTMPeptideInstances.addAll(dsPtmPepIns);
                        }
                    });
                    parentPTMPepInstanceDone.add(ptmSitePepIns.getParentPTMPeptideInstance());
                }
            });
                          
        }
        return m_leafPTMPeptideInstances;
    }

    public List<Integer> getPositionsOnProtein() {
        if (m_positionsOnProtein == null) {
            m_positionsOnProtein =  m_sites.stream().map(s -> s.getPositionOnProtein()).collect(ComparableList::new, ComparableList::add, ComparableList::addAll);
        }
        return m_positionsOnProtein;
    }

    public List<Float> getSiteConfidences() {
        if (m_probabilities == null) {
            m_probabilities = m_sites.stream().map(s -> s.getLocalisationConfidence()*100).collect(ComparableList::new, ComparableList::add, ComparableList::addAll);
        }
        return m_probabilities;
    }

    public List<PTMSite> getPTMSites(){
        return m_sites;
    }

    public Integer getPTMSitesCount(){
        return  m_sitesCount;
    }

    public void setPTMSitesCount(Integer sitesCount){
         m_sitesCount = sitesCount;
    }


    
    public Integer getPeptideCount() {
        return m_peptideIds.size();
    }


    public List<Long> getPeptideIds() {
        return m_peptideIds;
    }

    public void setQuantProteinSet(DMasterQuantProteinSet mqps) {
        m_masterQuantProteinSet = mqps;
    }

    public DMasterQuantProteinSet getMasterQuantProteinSet() {
        return m_masterQuantProteinSet;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;

        if(! (obj instanceof PTMCluster ))
            return false;

        PTMCluster objCluster= (PTMCluster) obj;
        return  objCluster.m_id.equals(m_id); //ID Should be enough ! && objCluster.m_proteinMatch.equals(m_proteinMatch) &&  objCluster.m_sites.equals(m_sites);
    }

    @Override
    public int compareTo(PTMCluster o) {                                 
        if(o == null)
            return 1;
                            
        Integer start = Integer.MAX_VALUE;
        Optional<Integer> optStart = getParentPTMPeptideInstances().stream().map( pi -> pi.getStartPosition()).sorted().findFirst();                            
        if(optStart.isPresent())
            start = optStart.get();
                            
        Integer o2Start = Integer.MAX_VALUE;
        Optional<Integer> o2OptStart = o.getParentPTMPeptideInstances().stream().map( pi -> pi.getStartPosition()).sorted().findFirst();
        if(o2OptStart.isPresent())
            o2Start = o2OptStart.get();
                            
                            
        return Integer.compare(start, o2Start);        
    }
}
