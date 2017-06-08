/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import fr.proline.mzscope.ui.MgfExportParameters;

/**
 *
 * @author AK249877
 */
public class MgfExportSettings {
    
    private final String m_destinationURL;
    private final MgfExportParameters m_mgfExportParameters;
    
    public MgfExportSettings(String destination, MgfExportParameters mgfExportParameters){
        m_destinationURL = destination;
        m_mgfExportParameters = mgfExportParameters;
    }
    
    public String getDestinationDirectory(){
        return m_destinationURL;
    }
    
    public MgfExportParameters getMgfExportParameters(){
        return m_mgfExportParameters;
    }
}
