/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import java.util.ArrayList;

/**
 *
 * @author AK249877
 */
public interface MsListener {  
    
    public void conversionPerformed(ArrayList<MsListenerConverterParameter> list);
    
    public void uploadPerformed(ArrayList<MsListenerParameter> list);
    
    public void downloadPerformed(ArrayList<MsListenerDownloadParameter> list);
    
    public void exportPerformed(ArrayList<MsListenerParameter> list);
    
    public void verificationPerformed(ArrayList<MsListenerParameter> list);
    
    public void msStateChanged();
    
}
