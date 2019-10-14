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
import static fr.proline.studio.gui.DefaultDialog.BUTTON_CANCEL;
import static fr.proline.studio.gui.DefaultDialog.BUTTON_HELP;
import static fr.proline.studio.gui.DefaultDialog.BUTTON_OK;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * waiting dialog  during data loading
 * @author MB243701
 */
public class LoadWaitingDialog extends DefaultDialog{

    private DefaultDialog.ProgressTask m_task = null;
    
    
    public LoadWaitingDialog(Window parent, String waitingDescription) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Load data");

        setPreferredSize(new Dimension(300,150));
        setResizable(true);

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new BorderLayout());
        internalPanel.add(new JLabel(waitingDescription), BorderLayout.NORTH);
        setInternalComponent(internalPanel);

        // hide default  button
        setButtonVisible(BUTTON_HELP, false);
        setButtonVisible(BUTTON_CANCEL, false);
        setButtonName(BUTTON_OK, "Close");
    }
    
    @Override
    protected boolean okCalled() {
        return true;
    }
    
    public void setTask(DefaultDialog.ProgressTask task) {
        m_task = task;
        startTask(m_task);
    }
    
}
