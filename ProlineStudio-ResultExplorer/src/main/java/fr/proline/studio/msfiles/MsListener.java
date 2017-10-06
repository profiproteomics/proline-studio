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
public interface MsListener {  
    
    public void conversionPerformed(File f, ConversionSettings settings, boolean success);
    
    public void uploadPerformed(File f, boolean success);
    
    public void downloadPerformed(boolean success);
    
    public void exportPerformed(File f, boolean success);
    
}
