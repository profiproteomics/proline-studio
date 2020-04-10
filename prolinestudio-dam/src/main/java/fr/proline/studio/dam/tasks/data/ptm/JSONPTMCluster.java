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

/**
 *
 * @author VD225637
 */
public class JSONPTMCluster {
    //
    // Variables defined in Proline OM that will be read out from the database
    //@todo : try to make them private instead of public

    public Long id;

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
