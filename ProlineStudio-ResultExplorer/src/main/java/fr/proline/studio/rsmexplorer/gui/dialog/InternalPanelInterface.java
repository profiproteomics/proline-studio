/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;

/**
 *
 * @author AK249877
 */
public interface InternalPanelInterface {
    public boolean cancelTriggered();
    public boolean okTriggered();
    public boolean defaultTriggered();
    public boolean backTriggered();
    public boolean saveTriggered();
    public boolean loadTriggered();
    
    public void setDialog(DefaultDialogInterface dialog);
    public void reinitializePanel();
}
