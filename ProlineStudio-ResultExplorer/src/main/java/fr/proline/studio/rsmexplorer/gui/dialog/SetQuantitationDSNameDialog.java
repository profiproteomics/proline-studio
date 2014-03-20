/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.Window;

/**
 * 
 * Dialog to specify Quantitation Name 
 *
 * @author VD225637
 */
public class SetQuantitationDSNameDialog extends DefaultDialog {

    private static SetQuantitationDSNameDialog m_singletonDialog = null;
   
    private SetQuantitationDSNamePanel m_quantiDSPanel = null;
   
    public static SetQuantitationDSNameDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new SetQuantitationDSNameDialog(parent);
        }

        m_singletonDialog.m_quantiDSPanel.reinitialize();
        
        return m_singletonDialog;
    }
    
    private SetQuantitationDSNameDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Set Quantitaion Dataset Name");
        setButtonVisible(BUTTON_HELP, false);
        setButtonVisible(BUTTON_DEFAULT, false);
        m_quantiDSPanel = new SetQuantitationDSNamePanel();

        setInternalComponent(m_quantiDSPanel);
    }
    
        @Override
    protected boolean okCalled() {
        
        String name = m_quantiDSPanel.getQuantiDSName();
       
        if (name.isEmpty()) {
            setStatus(true, "You must fill the dataset name.");
            highlight(m_quantiDSPanel.getNameTextfield());
            return false;
        }

        return true;
    }
        
    public String getQuantiDSName() {
        return m_quantiDSPanel.getQuantiDSName();
    }

    public String getQuantiDSDescriptionName() {
        return m_quantiDSPanel.getQuantiDSDescr();
    }
     
}
