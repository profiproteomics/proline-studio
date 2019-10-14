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

import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.SystemInfoTask;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.StringTokenizer;
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
        m_txtArea.setEditable(false);
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
                
                String sysInfoText = sysInfoResult[0];

                StringBuilder sb = new StringBuilder();
                String serverURL = null;
                String queueName = null;
                StringTokenizer st = new StringTokenizer(sysInfoText,"\n");
                while (st.hasMoreTokens()) {
                    String line = st.nextToken();
                    if (serverURL == null) {
                        if ( line.indexOf("\"em1\"") != -1) {
                            int index = line.lastIndexOf(',');
                            if (index != -1) {
                                serverURL = line.substring(index+1, line.length());
                                sb.append("Server: ").append(serverURL).append("\n");
                            }
                        }
                    }
                    if (queueName == null) {
                        if ( line.indexOf("\"JMSDestination\"") != -1) {
                            String queueLabel = "HornetQQueue[";
                            int index1 = line.lastIndexOf(queueLabel);
                            int index2 = line.lastIndexOf(']');
                            if ((index1 != -1) && (index2!=-1)) {
                                queueName = line.substring(index1+queueLabel.length(), index2);
                                sb.append("JMS Proline Queue: ").append(queueName).append("\n\n\n");
                            }
                        }
                    }
                    if ((serverURL != null) && (queueName != null)) {
                        break;
                    }
                }
                

                sb.append(sysInfoText);
                        
                m_txtArea.setText(sb.toString());
                m_txtArea.setCaretPosition(0);
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
