/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import java.io.File;

/**
 *
 * @author AK249877
 */
public class MsListenerConverterParameter extends MsListenerParameter {

    private ConversionSettings m_conversionSettings;
    
    public MsListenerConverterParameter(File file, boolean success, ConversionSettings conversionSettings) {
        super(file, success);
        m_conversionSettings = conversionSettings;
    }
    
    public ConversionSettings getConversionSettings(){
        return m_conversionSettings;
    }
    
}
