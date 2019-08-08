package fr.proline.studio.dam.tasks.data.ptm;

import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;

import java.util.List;

public class PTMSitePeptideInstance {

  private final PTMPeptideInstance m_parentPTMPeptideInstance;
  private final PTMSite m_site;
  private final DPeptideMatch m_bestPeptideMatch;
  private final List<DPeptideInstance> m_leafPepInstances;

  public PTMSitePeptideInstance(PTMSite site, PTMPeptideInstance ptmPeptide, List<DPeptideInstance> leafPeptideInstances, DPeptideMatch bestPeptideMatch) {
    m_parentPTMPeptideInstance = ptmPeptide;
    m_bestPeptideMatch = bestPeptideMatch;
    m_leafPepInstances = leafPeptideInstances;
    m_site = site;
  }

  public PTMPeptideInstance getParentPTMPeptideInstance() {
    return m_parentPTMPeptideInstance;
  }

  public PTMSite getSite() {
    return m_site;
  }

  public DPeptideMatch getBestPeptideMatch() {
    return m_bestPeptideMatch;
  }

  public List<DPeptideInstance> getLeafPepInstances() {
    return m_leafPepInstances;
  }
}
