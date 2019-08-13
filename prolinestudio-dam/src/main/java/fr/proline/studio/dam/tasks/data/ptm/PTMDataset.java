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
    private List<Long> m_leafRSMIds;
    private List<DInfoPTM> m_ptmOfInterest;
    private List<PTMCluster> m_ptmClusters;
    
    private final Map<Long, Map<Long, List<PTMPeptideInstance>>> m_ptmPeptideByPeptideIdByProtMatchId = new HashMap<>();
    
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
        return new ArrayList(m_leafRSMIds);
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
        Map<Long, List<PTMPeptideInstance>> m =  m_ptmPeptideByPeptideIdByProtMatchId.get(proteinMatchId);
        if(m != null)
            return m.values().stream().flatMap(entry -> entry.stream()).collect(Collectors.toList());           
        else
           return Collections.EMPTY_LIST;
    }


    /**
     * return the PTMPeptideInstance corresponding to the specified peptide instance identifying specified ProteinMatch
     * If not yet defined, create a new PTMPeptideInstance
     * @param proteinMatch
     * @param peptideInstance
     * @param protPosition
     * @return
     */
    public PTMPeptideInstance getPTMPeptideInstance(DProteinMatch proteinMatch, DPeptideInstance peptideInstance, Integer protPosition) {

        Long proteinMatchId = proteinMatch.getId();
        if (!m_ptmPeptideByPeptideIdByProtMatchId.containsKey(proteinMatchId)) {
            m_ptmPeptideByPeptideIdByProtMatchId.put(proteinMatchId, new HashMap<>());
        }

        Map<Long, List<PTMPeptideInstance>> ptmPepInstanceByPepId = m_ptmPeptideByPeptideIdByProtMatchId.get(proteinMatchId);
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
    
}
