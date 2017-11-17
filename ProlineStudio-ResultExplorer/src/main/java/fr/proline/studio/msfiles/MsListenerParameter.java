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
public class MsListenerParameter {
    
    private final File m_file;
    private final boolean m_success;
    
    public MsListenerParameter(File file, boolean success){
        m_file = file;
        m_success = success;
    }
    
    public File getFile(){
        return m_file;
    }
    
    public boolean wasSuccessful(){
        return m_success;
    }
    
}
