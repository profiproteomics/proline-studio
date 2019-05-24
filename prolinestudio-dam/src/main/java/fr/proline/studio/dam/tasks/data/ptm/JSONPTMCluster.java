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
public class JSONPTMCluster {
    //
    // Variables defined in Proline OM that will be read out from the database
    //@todo : try to make them private instead of public
    
    // Array of PTM site locations Ids
    public Long[] ptmSiteLocations;
    
    // Best (highest PTM probability) peptide match for this PtmSiteTuple
    public Long bestPeptideMatchId;

    // The localization probability (percentage between 0 and 100)
    public Float localizationConfidence;
    
    // Array of clusterized peptides
    public Long[] peptideIds;
    
    // array of peptide having the same sequence and modification of a matching peptide, but with their ptm located at another position
    // those peptide did not match the ptm site, but they can confuse the quantification process
    public Long[] isomericPeptideIds;
     
}
