/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks.data.ptm;

import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author VD225637
 */
public class PTMCluster {
    
    private List<PTMSite> m_sites;
    private JSONPTMCluster m_jsonCluster;
    
    private DProteinMatch m_proteinMatch;    
    private DMasterQuantProteinSet m_masterQuantProteinSet;

    private DPeptideMatch m_bestPeptideMatch;
    
//    //PeptideInstance of parent dataset
//    private List<DPeptideInstance> m_parentPeptideInstances;
//    //PTMPeptideInstance of leaf dataset by peptideId
//    private Map<Long, PTMPeptideInstance> m_ptmPeptideInstanceByPepId;

        
    private PTMDataset m_ptmDataset;
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
    
        

//    /**
//     * With leafInstances, we can create a map of leafPepInstanceByPepId
//     * For each peptide found in parentPeptideInstances, we instantiate a PTMPeptideInstance, set it's start postion.
//     * With the peptideId, we can find all of it's leafPeptide, so that a new PTMSitePeptideInstance is create. 
//     * at last, we put this PTMSitePeptideInstance in the m_ptmSitePeptideInstanceByPepId.
//     * @param parentPeptideInstances
//     * @param leafInstances 
//     */
//    public void setPeptideInstances(List<DPeptideInstance> parentPeptideInstances, List<DPeptideInstance> leafInstances) {
//        m_parentPeptideInstances = parentPeptideInstances;
//        m_ptmPeptideInstanceByPepId = new HashMap<>();
//        Map<Long, List<DPeptideInstance>> leafPepInstanceByPepId = leafInstances.stream().collect(Collectors.groupingBy(pi -> pi.getPeptideId()));
//        for (DPeptideInstance parentPeptideInstance : parentPeptideInstances) {
//            PTMPeptideInstance ptmPeptide = m_ptmDataset.getPTMPeptideInstance(m_proteinMatch.getId(), parentPeptideInstance);
//            Long peptideId = parentPeptideInstance.getPeptideId();
//            m_ptmPeptideInstanceByPepId.put(peptideId, ptmPeptide);
////            ptmPeptide.setStartPosition(getPositionOnProtein() - getPositionOnPeptide(peptideId));
////            ptmPeptide.addPTMSite(this);
//            List<DPeptideInstance> leafPeptideInstances = leafPepInstanceByPepId.get(peptideId);
//            //PTMSitePeptideInstance ptmSitePeptideInstance = new PTMSitePeptideInstance(this, ptmPeptide, leafPeptideInstances, getBestPeptideMatch(leafPeptideInstances));
//           // m_ptmSitePeptideInstanceByPepId.put(peptideId, ptmSitePeptideInstance);
//        }
//    }
    
    public List<DPeptideInstance> getParentPeptideInstances() {
        return m_sites.stream().flatMap(site -> site.getParentPeptideInstances().stream()).collect(Collectors.toList());        
    }
    
    public PTMPeptideInstance getPTMPeptideInstance(Long peptideId) {
        if(m_sites == null || m_sites.isEmpty())
            return null;
        return m_sites.get(0).getPTMSitePeptideInstance(peptideId).getPTMPeptideInstance();
    }
    
    public List<PTMPeptideInstance> getPTMPeptideInstances() {
        if(m_sites == null || m_sites.isEmpty())
            return null;
        Collection<PTMPeptideInstance> ptmPeptides = m_ptmDataset.getPTMPeptideInstance(m_proteinMatch.getId());
        List<Long> pepIds =  Arrays.asList(m_jsonCluster.peptideIds);
        List<PTMPeptideInstance> ptmPepIList = new ArrayList<>();
        for(PTMPeptideInstance ptmPepI : ptmPeptides){
            if(pepIds.contains(ptmPepI.getPeptideInstance().getPeptideId()) && !ptmPepIList.contains(ptmPepI))
                ptmPepIList.add(ptmPepI);                        
        }
        
        return ptmPepIList;
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
