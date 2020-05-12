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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A peptide instance located on a protein sequence by start and stop positions. A single PTMPeptideInstance could be
 * involved in multiple clusters and multiple sites
 *
 */
public class PTMPeptideInstance {

  protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.DAM.Task");

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

  public List<PTMSite> getPTMSites() {
    return m_sites;
  }
   
  public void addCluster(PTMCluster cluster){
      m_clusters.add(cluster);
  }
   
  public List<PTMCluster> getClusters(){
      return m_clusters;
  }


  public DPeptideMatch getRepresentativePepMatch() {
    return _getRepresentativePepMatch(m_clusters);
  }

  public DPeptideMatch getRepresentativePepMatch(List<PTMCluster> clusters) {
    List<PTMCluster> matchingClusters = m_clusters.stream().filter(clusters::contains).collect(Collectors.toList());
    return matchingClusters.isEmpty() ? null : _getRepresentativePepMatch(matchingClusters);
  }

  public DPeptideMatch _getRepresentativePepMatch(List<PTMCluster> clusters) {

    //TODO: to be improved in case of searching the representative PSM for multiple clusters simultaneously
    if (clusters.size() > 1) {
      m_logger.debug("searching a representative PSM for multiple clusters simultaneously not yet implemented: use only the first cluster");
    }
    PTMCluster cluster = m_clusters.get(0);
    DPeptideMatch pepMatch = null;

    //Try using ptmSite PTMSitePeptideInstance
    Iterator<PTMSite> siteIT = cluster.getPTMSites().iterator();
    while (siteIT.hasNext()) {
      PTMSite nextSite = siteIT.next();
      PTMSitePeptideInstance ptmSitePepInst = nextSite.getPTMSitePeptideInstance(getPeptideInstance().getPeptideId());
      if (ptmSitePepInst != null) {
        pepMatch = ptmSitePepInst.getRepresentativePepMatch();
        break;
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
    return "PTMPeptideInstance{" + this.getSequence() + " Position=(" + this.getStartPosition() + "-" + this.getStopPosition() + this.m_sites.toString() + "}";
  }


}
