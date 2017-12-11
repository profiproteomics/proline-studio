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
public class MsListenerDownloadParameter {
    
    private final File m_destinationFile, m_remoteFile;
    private final boolean m_success;
    
    public MsListenerDownloadParameter(File destinationFile, File remoteFile,boolean success){
        m_destinationFile = destinationFile;
        m_remoteFile = remoteFile;
        m_success = success;
    }
    
    public File getDestinationFile(){
        return m_destinationFile;
    }
    
    public File getRemoteFile(){
        return m_remoteFile;
    }
    
    public boolean wasSuccessful(){
        return m_success;
    }
    
}
