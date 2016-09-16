/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;

import java.awt.Component;

/**
 *
 * @author AK249877
 */
public interface DefaultDialogInterface {
    
    public void setDialogStatus(boolean b, String s);
    
    public void highlightPanelComponent(Component c);
        
}
