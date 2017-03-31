/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.wizard;

/**
 *
 * @author AK249877
 */
public class MzdbUploadSettings {
    
    private boolean m_deleteMzdb, m_createParentDirectory;
    private String m_mountLabel;
    private String m_destination;
    
    public MzdbUploadSettings(boolean deleteMzdb, boolean createParentDirectory, String mountLabel){
        m_deleteMzdb = deleteMzdb;
        m_createParentDirectory = createParentDirectory;
        m_mountLabel = mountLabel;
    }
    
    public MzdbUploadSettings(boolean deleteMzdb, boolean createParentDirectory, String mountLabel, String destination){
        this(deleteMzdb, createParentDirectory, mountLabel);
        m_destination = destination;
    }
    
    public boolean getDeleteMzdb(){
        return m_deleteMzdb;
    }
    
    public boolean getCreateParentDirectory(){
        return m_createParentDirectory;
    }
    
    public String getMountLabel(){
        return m_mountLabel;
    }
    
    public String getDestination(){
        return m_destination;
    }
    
}
