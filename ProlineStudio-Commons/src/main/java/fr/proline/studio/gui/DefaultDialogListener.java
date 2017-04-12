/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.gui;

/**
 *
 * @author AK249877
 */
public interface DefaultDialogListener {
    
    public void okPerformed(DefaultDialog d);
    
    public void cancelPerformed(DefaultDialog d);
    
    public void defaultPerformed(DefaultDialog d);
    
    public void backPerformed(DefaultDialog d);
    
    public void savePerformed(DefaultDialog d);
    
    public void loadPerformed(DefaultDialog d);
    
}
