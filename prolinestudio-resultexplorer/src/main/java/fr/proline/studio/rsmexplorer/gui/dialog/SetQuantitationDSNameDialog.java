/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
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
