package fr.proline.studio.dam.tasks.data.ptm;

import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.uds.dto.DDataset;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Map<Long, Map<Long, PTMPeptideInstance>> m_ptmPeptideByPeptideIdByProtMatchId = new HashMap<>();

    public PTMDataset(DDataset dataset) {
        
        if (dataset == null) throw new IllegalArgumentException("dataset from which PTM sites are extracted cannot be null");
        
        this.m_dataset = dataset;
    }
    
    public DDataset getDataset() {
        return m_dataset;
    }

    
    public boolean isIdentification() {
      return m_dataset.isIdentification();
    }

    public boolean isQuantitation() {
      return m_dataset.isQuantitation();
    }
    
    public List<PTMSite> getPTMSites() {
        return m_proteinPTMSites;
    }

    public void setPTMSites(List<PTMSite> proteinPTMSites) {
        this.m_proteinPTMSites = proteinPTMSites;
        m_proteinPTMSites.stream().forEach(site -> site.setDataset(this));
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
    }

    public Collection<PTMPeptideInstance> getPTMPeptideInstance(Long proteinMatchId) {
        return m_ptmPeptideByPeptideIdByProtMatchId.get(proteinMatchId).values();
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
