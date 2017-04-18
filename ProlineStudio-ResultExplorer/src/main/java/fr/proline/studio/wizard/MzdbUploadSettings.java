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
    
    private final boolean m_deleteMzdb;
    private final String m_mountLabel;
    private final String m_destination;
    
    public MzdbUploadSettings(boolean deleteMzdb, String mountLabel, String destination){
        m_deleteMzdb = deleteMzdb;
        m_mountLabel = mountLabel;
        m_destination = destination;
    }
    
    public boolean getDeleteMzdb(){
        return m_deleteMzdb;
    }
    
    public String getMountLabel(){
        return m_mountLabel;
    }
    
    public String getDestination(){
        return m_destination;
    }
    
}
