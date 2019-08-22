/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks.data.ptm;

import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class PTMCluster {
    
      private final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ptm");

    private final List<PTMSite> m_sites;
    private final JSONPTMCluster m_jsonCluster;
    
    private DProteinMatch m_proteinMatch;    
    private DMasterQuantProteinSet m_masterQuantProteinSet;
    private DPeptideMatch m_bestPeptideMatch;
    private DMasterQuantPeptide m_bestQuantPeptideMatch;
    private List<PTMPeptideInstance> m_parentPTMPeptideInstances;
    private List<PTMPeptideInstance> m_leafPTMPeptideInstances;
    private List<DPeptideInstance> m_parentPeptideInstances;

//    //PTMPeptideInstance of leaf dataset by peptideId
//    private Map<Long, PTMPeptideInstance> m_ptmPeptideInstanceByPepId;

        
    private final PTMDataset m_ptmDataset;
    private Object m_expressionValue;

    public PTMCluster(JSONPTMCluster jsonValue, PTMDataset ptmds) {
        m_ptmDataset = ptmds;
        m_jsonCluster = jsonValue;        
        
        //*** Retieve other needed informations
        
        //Get clustered PTMSites
        List<Long> ptmSitesIds = Arrays.asList(jsonValue.ptmSiteLocations);        
        m_sites = m_ptmDataset.getPTMSites().stream().filter(site -> ptmSitesIds.contains(site.getid())).collect(Collectors.toList());        
        
        if(!m_sites.isEmpty()) {
            //Get ProteinMatch from one of the PTMSite : all should have same. VDS TODO To test ?
            m_proteinMatch = m_sites.get(0).getProteinMatch();
            m_masterQuantProteinSet = m_sites.get(0).getMasterQuantProteinSet();
        }        
    }
    
    public DProteinMatch getProteinMatch(){
        return m_proteinMatch;
    }
    
    public PTMDataset getPTMDataset(){
        return m_ptmDataset;
    }
    
    public void setBestPeptideMatch(DPeptideMatch peptideMatch) {
        m_bestPeptideMatch = peptideMatch;
    }

    public DPeptideMatch getBestPeptideMatch() {
        return m_bestPeptideMatch;
    }
    
    public void setBestQuantPeptideMatch(DMasterQuantPeptide quantPeptideMatch) {
        m_bestQuantPeptideMatch = quantPeptideMatch;
    }

    public DMasterQuantPeptide getBestQuantPeptideMatch() {
        return m_bestQuantPeptideMatch;
    }

    public List<DPeptideInstance> getParentPeptideInstances() {
        if (m_sites == null || m_sites.isEmpty())
            return null;
        if (m_parentPeptideInstances == null) {
            List<Long> pepIds = Arrays.asList(m_jsonCluster.peptideIds);
            m_parentPeptideInstances = m_sites.stream().flatMap(site -> site.getParentPeptideInstances().stream()).distinct().filter(pi -> pepIds.contains(pi.getPeptideId())).collect(Collectors.toList());
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
            List<Long> pepIds = Arrays.asList(m_jsonCluster.peptideIds);
            m_parentPTMPeptideInstances = new ArrayList<>();
            for (PTMPeptideInstance ptmPepI : ptmPeptides) {
                if (pepIds.contains(ptmPepI.getPeptideInstance().getPeptideId()) && !m_parentPTMPeptideInstances.contains(ptmPepI) ){
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
            List<Long> pepIds = Arrays.asList(m_jsonCluster.peptideIds);   
            
            // Get PTMSitePeptideInstance for all sites of this Cluster
            for(PTMSite nextSite : m_sites){                                
                allPTMSitePepInst.addAll(nextSite.getPTMSitePeptideInstances().stream().filter(ptmSitePepI ->pepIds.contains(ptmSitePepI.getParentPTMPeptideInstance().getPeptideInstance().getPeptideId())).collect(Collectors.toList()));
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
                            associatedParentPTMPepInst.getSites().forEach(parentSite -> leafPtmPepI.addPTMSite(parentSite) );
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

    public List<PTMSite> getClusteredSites(){
        return m_sites;
    }
    
    public Integer getPeptideCount() {
        return m_jsonCluster.peptideIds.length;
    }
     
    public void setQuantProteinSet(DMasterQuantProteinSet mqps) {
        m_masterQuantProteinSet = mqps;
    }

    public DMasterQuantProteinSet getMasterQuantProteinSet() {
        return m_masterQuantProteinSet;
    }
   
    public void setExpressionValue(Object value) {
        m_expressionValue =  value;
    }

    public Object getExpressionValue() {
      return m_expressionValue;
    }
}
