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
public class UploadSettings {
    
    private boolean m_deleteMzdb, m_createParentDirectory;
    private String m_mountLabel;
    
    public UploadSettings(boolean deleteMzdb, boolean createParentDirectory, String mountLabel){
        m_deleteMzdb = deleteMzdb;
        m_createParentDirectory = createParentDirectory;
        m_mountLabel = mountLabel;
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
    
}
