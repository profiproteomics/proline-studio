/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    
    
    public LoadWaitingDialog(Window parent) {
        super(parent, Dialog.ModalityType.MODELESS);

        setTitle("Load data");

        setSize(new Dimension(500,340));
        setResizable(true);

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new BorderLayout());
        internalPanel.add(new JLabel("Please wait while loading template to configure export"), BorderLayout.CENTER);
        setInternalComponent(internalPanel);

        // hide default and ol button
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
