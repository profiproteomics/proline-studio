/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.SystemInfoTask;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author VD225637
 */
public class SystemInfoDialog extends DefaultDialog {

    private static SystemInfoDialog m_singletonDialog = null;
    private JTextArea m_txtArea;

    protected SystemInfoDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("System Information ");
        setInternalComponent(createPanel());
        setResizable(true);
    }

    private JPanel createPanel() {
        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        // create objects
        JScrollPane scrollPane = new JScrollPane();
        m_txtArea = new JTextArea();
        m_txtArea.setRows(20);
        m_txtArea.setColumns(50);
        scrollPane.setViewportView(m_txtArea);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 3;
        internalPanel.add(scrollPane, c);

        return internalPanel;
    }

    public static SystemInfoDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new SystemInfoDialog(parent);
        }
        return m_singletonDialog;
    }

    public void updateInfo() {

        final String[] sysInfoResult = new String[1];
        AbstractJMSCallback callback = new AbstractJMSCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                m_txtArea.setText(sysInfoResult[0]);
                revalidate();
                repack();
            }
        };

        SystemInfoTask task = new SystemInfoTask(callback, sysInfoResult);
        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

    }

    @Override
    public void setVisible(boolean v) {
        centerToScreen();
//        repack();
        super.setVisible(v);
    }

 
}
