/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.tasks.data.ptm;

/**
 *
 * @author VD225637
 */
public class JSONPTMDataset {
    //
    // Variables defined in Proline OM that will be read out from the database
    //@todo : try to make them private instead of public
    
    // array of of the PTMs of interest
    public Long[] ptmIds;
    
    // array of Leaf RSM
    public Long[] leafResultSummaryIds;
  
    // array of PtmSites identified
    public JSONPTMSite2[] ptmSites;
    
    // array of Ptm Clusters
    public JSONPTMCluster[] ptmClusters;
       
}
