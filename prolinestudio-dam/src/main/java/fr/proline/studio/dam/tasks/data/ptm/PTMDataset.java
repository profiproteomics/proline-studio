package fr.proline.studio.dam.tasks.data.ptm;

import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.uds.dto.DDataset;
import java.sql.Array;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a set of PTM sites, displayed from a quantification dataset or from an identification dataset.
 * 
 * @author CB205360
 */
public class PTMDataset {
    
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.DAM.Task");

    private DDataset m_dataset;

    private List<PTMSite> m_proteinPTMSites;
    
    //data for v2
    private List<Long> m_leafRSMIds;
    private List<DInfoPTM> m_ptmOfInterest;
    private List<PTMCluster> m_ptmClusters;
    
    private Map<Long, Map<Long, PTMPeptideInstance>> m_ptmPeptideByPeptideIdByProtMatchId = new HashMap<>();
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
        if(!isV2() || m_leafRSMIds == null)
            return Collections.<Long>emptyList();
        return m_leafRSMIds;
    }
    
    public void setLeafResultSummaryIds(List<Long> l){
        m_leafRSMIds = new ArrayList<>(l);
    }
    
    public boolean isV2(){
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
        if(!isV2())
            return null;
        else{
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

    public Collection<PTMPeptideInstance> getPTMPeptideInstance(Long proteinMatchId) {
        Map m =  m_ptmPeptideByPeptideIdByProtMatchId.get(proteinMatchId);
        if(m != null)
            return m.values();
        else
           return Collections.EMPTY_LIST;
    }

    public PTMPeptideInstance getPTMPeptideInstance(Long proteinMatchId, Long peptideId) {
        if (!m_ptmPeptideByPeptideIdByProtMatchId.containsKey(proteinMatchId))
            return null;
        return m_ptmPeptideByPeptideIdByProtMatchId.get(proteinMatchId).get(peptideId);
    }

    /**
     * return the PTMPeptideInstance corresponding to the specified peptideId identifying specified ProteinMatch
     * If not yet defined, create a new PTMPeptideInstance
     * @param proteinMatchId
     * @param peptideInstance
     * @return
     */
    public PTMPeptideInstance getPTMPeptideInstance(Long proteinMatchId, DPeptideInstance peptideInstance) {

        if (!m_ptmPeptideByPeptideIdByProtMatchId.containsKey(proteinMatchId)) {
            m_ptmPeptideByPeptideIdByProtMatchId.put(proteinMatchId, new HashMap<>());
        }

        Map<Long, PTMPeptideInstance> map = m_ptmPeptideByPeptideIdByProtMatchId.get(proteinMatchId);

        if (! map.containsKey(peptideInstance.getPeptideId())) {
            PTMPeptideInstance ptmPeptide = new PTMPeptideInstance(peptideInstance);
            map.put(peptideInstance.getPeptideId(), ptmPeptide);
        }

        return map.get(peptideInstance.getPeptideId());
    }
    
}
