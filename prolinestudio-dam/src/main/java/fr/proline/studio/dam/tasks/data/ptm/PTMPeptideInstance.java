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

import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import java.util.ArrayList;
import java.util.Comparator;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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
  
    public List<DPeptideMatch> getPepMatchesOnProteinMatch(DProteinMatch proteinMatch) {
        List<DPeptideMatch> pepMatches = new ArrayList<>();
        List<Long> allowedProtMatchIds = m_sites.get(0).getPTMdataset().getProtMatchesIdForAccession(proteinMatch.getAccession());
        pepMatches.addAll(m_peptideInstance.getPeptideMatches().stream().filter(dpm -> allowedProtMatchIds.contains(dpm.getSequenceMatch().getId().getProteinMatchId())).collect(Collectors.toList()));
        return pepMatches;
    }

    @Override
    public String toString() {
        return "PTMPeptideInstance{" + this.getSequence()+" Position=("+ this.getStartPosition()+"-" + this.getStopPosition() +this.m_sites.toString()+ "}";
    }
  
  
}
