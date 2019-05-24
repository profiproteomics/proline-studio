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
public class JSONPTMSite2 extends AbstractJSONPTMSite {
    //
    // Variables defined in Proline OM that will be read out from the database
    //@todo : try to make them private instead of public
    public Long id;

    //List of PepidesInstances which don't match this PTM but with same mass
    public Long[] isomericPeptideIds;
    
}
