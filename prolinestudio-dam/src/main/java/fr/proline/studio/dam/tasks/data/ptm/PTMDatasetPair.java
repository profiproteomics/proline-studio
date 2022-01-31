package fr.proline.studio.dam.tasks.data.ptm;

import fr.proline.core.orm.uds.dto.DDataset;

public class PTMDatasetPair {

  private PTMDataset m_sitePTMDataset;
  private PTMDataset m_clusterPTMDataset;
  private int m_ptmDatasetType;
  private boolean m_shouldSave = false;

  public static int RAW_PTM_DATASET= 0;
  public static int ANNOTATED_PTM_DATASET= 1;

  public PTMDatasetPair(PTMDataset sitePTMDataset, PTMDataset clusterPTMDataset) {
    this(sitePTMDataset, clusterPTMDataset, false);
  }

  public PTMDatasetPair(PTMDataset sitePTMDataset, PTMDataset clusterPTMDataset, boolean isAnnotated) {
    if(sitePTMDataset == null || clusterPTMDataset == null)
      throw  new IllegalArgumentException("PTMDatasets should not be null");
    m_sitePTMDataset =  sitePTMDataset;
    m_clusterPTMDataset = clusterPTMDataset;
    m_ptmDatasetType = isAnnotated ? ANNOTATED_PTM_DATASET : RAW_PTM_DATASET;
  }


  public  boolean shouldSavePTMDataset(){
    return m_shouldSave;
  }

  public void setShouldSavePTMDataset(boolean shouldSave){
    m_shouldSave = shouldSave;
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
      m_sitePTMDataset.updateParentPTMPeptideInstanceClusters(false);
      m_clusterPTMDataset.updateParentPTMPeptideInstanceClusters(true);
 }

  public int getPTMDatasetType() {
    return m_ptmDatasetType;
  }


}
