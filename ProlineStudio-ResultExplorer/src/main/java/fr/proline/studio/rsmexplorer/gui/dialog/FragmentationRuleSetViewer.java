/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.gui.admin.FragmentationRuleSetPanel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import javax.swing.JPanel;

/**
 *
 * @author VD225637
 */
public class FragmentationRuleSetViewer extends DefaultDialog {
    
//    private static FragmentationRuleSetViewer m_singletonDialog = null;
//    
//    public static FragmentationRuleSetViewer getDialog(Window parent){
//       if (m_singletonDialog == null) {
//            m_singletonDialog = new FragmentationRuleSetViewer(parent);
//        }
//        return m_singletonDialog;   
//    }

    public  FragmentationRuleSetViewer(Window parent) {
        super(parent, Dialog.ModalityType.DOCUMENT_MODAL);
        
        setTitle("Fragmentation Rule Sets Viewer");
        setSize(new Dimension(640, 480));
        setMinimumSize(new Dimension(400, 360));
        setResizable(true);
        
        setButtonVisible(BUTTON_CANCEL, false);
        setButtonName(BUTTON_OK, "Close");
         initInternalPanel();
    }
    
    private void initInternalPanel() {
        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.BorderLayout());
        JPanel fragmentationRuleSetPanel = new FragmentationRuleSetPanel(this, false);
        
        internalPanel.add(fragmentationRuleSetPanel, BorderLayout.CENTER);
        setInternalComponent(internalPanel);
    }
    
    @Override
    public void pack() {
        // forbid pack by overloading the method
    }


    @Override
    protected boolean okCalled() {
        return true;
    }
}
