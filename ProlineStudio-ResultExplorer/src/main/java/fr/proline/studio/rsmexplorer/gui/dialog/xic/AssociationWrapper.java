/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog.xic;

import java.io.File;

/**
 *
 * @author AK249877
 */
public class AssociationWrapper {
    
    public enum AssociationType {

        ASSOCIATED, NOT_ASSOCIATED
    }
    
    private final AssociationType m_associationType;
    private final String m_path;
    
    public AssociationWrapper(String path, AssociationType associationType){
        m_path = path;
        m_associationType = associationType;
    }
    
    public String getPath(){
        return m_path;
    }
    
    public AssociationType getAssociationType(){
        return m_associationType;
    }
    
}
