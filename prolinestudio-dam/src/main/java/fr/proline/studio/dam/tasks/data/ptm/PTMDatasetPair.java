package fr.proline.studio.dam.tasks.data.ptm;

import fr.proline.core.orm.uds.dto.DDataset;

public class PTMDatasetPair {

  private PTMDataset m_sitePTMDataset;
  private PTMDataset m_clusterPTMDataset;

  public PTMDatasetPair(PTMDataset sitePTMDataset, PTMDataset clusterPTMDataset) {
    if(sitePTMDataset == null || clusterPTMDataset == null)
      throw  new IllegalArgumentException("PTMDatasets should not be null");
    m_sitePTMDataset =  sitePTMDataset;
    m_clusterPTMDataset = clusterPTMDataset;
  }

  public DDataset getDataset() {
      return m_clusterPTMDataset.getDataset();
  }

  public PTMDataset getSitePTMDataset() {
    return m_sitePTMDataset;
  }

  public PTMDataset getClusterPTMDataset() {
    return m_clusterPTMDataset;
  }

  public void updateParentPTMPeptideInstanceClusters(){
      m_sitePTMDataset.updateParentPTMPeptideInstanceClusters();
      m_clusterPTMDataset.updateParentPTMPeptideInstanceClusters();
 }
}
