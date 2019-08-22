package fr.proline.studio.dam.tasks.data.ptm;

import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import java.util.ArrayList;
import java.util.Comparator;

import java.util.Iterator;
import java.util.List;

public class PTMPeptideInstance {

  private final DPeptideInstance m_peptideInstance;
  private final List<PTMSite> m_sites = new ArrayList<>();
  private final List<PTMCluster> m_clusters = new ArrayList<>();
  private Integer m_startPosition;

  public PTMPeptideInstance(DPeptideInstance peptideInstance) {
    m_peptideInstance = peptideInstance;
  }

  public String getSequence() {
    return m_peptideInstance.getPeptide().getSequence();
  }

  public void addPTMSite(PTMSite site) {
    if (!m_sites.contains(site)) {
      m_sites.add(site);
      m_sites.sort(Comparator.comparingInt(PTMSite::getPositionOnProtein));
    }
  }

  public void setStartPosition(int start) {
    m_startPosition = start;
  }

  public Integer getStartPosition() {
    return m_startPosition;
  }

  public Integer getStopPosition() {
    return m_startPosition + m_peptideInstance.getPeptide().getSequence().length();
  }

  public DPeptideInstance getPeptideInstance() {
    return m_peptideInstance;
  }

  public List<PTMSite> getSites() {
    return m_sites;
  }
   
  public void addCluster(PTMCluster cluster){
      m_clusters.add(cluster);
  }
   
  public List<PTMCluster> getClusters(){
      return m_clusters;
  }

  // !! VDS FIXME Add better solution to get Info ==> lazy data upload !!!
  public DPeptideMatch getBestPepMatch() {
    DPeptideMatch pepMatch = getPeptideInstance().getBestPeptideMatch();
    if (pepMatch == null) {
        //m_logger.warn("--- PTMPeptide (Xic) table: UNABLE to get peptide match associated to peptide instance "+ptmPepInstance.toString());
        //Try using ptmSite PTMSitePeptideInstance
        Iterator<PTMSite> siteIT = getSites().iterator();
        while (siteIT.hasNext()) {
            PTMSite nextSite = siteIT.next();
            PTMSitePeptideInstance ptmSitePepInst = nextSite.getPTMSitePeptideInstance(getPeptideInstance().getPeptideId());
            if (ptmSitePepInst != null) {
                pepMatch = ptmSitePepInst.getBestPeptideMatch();
                break;
            }
        }
    }
    return pepMatch;
  }
}
