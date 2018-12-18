package fr.proline.studio.dam.tasks.data.ptm;

import fr.proline.core.orm.msi.dto.DPeptideInstance;

import java.util.HashSet;
import java.util.Set;

public class PTMPeptideInstance {

  private DPeptideInstance m_peptideInstance;
  private Set<PTMSite> m_sites = new HashSet<>();
  private Integer m_startPosition;

  public PTMPeptideInstance(DPeptideInstance peptideInstance) {
    m_peptideInstance = peptideInstance;
  }

  public String getSequence() {
    return m_peptideInstance.getPeptide().getSequence();
  }

  public void addPTMSite(PTMSite site) {
    m_sites.add(site);
  }

  public void setStartPosition(int start) {
    m_startPosition = start;
  }

  public Integer getStartPosition() {
    return m_startPosition;
  }

  public DPeptideInstance getPeptideInstance() {
    return m_peptideInstance;
  }

  public Set<PTMSite> getSites() {
    return m_sites;
  }
}
