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
