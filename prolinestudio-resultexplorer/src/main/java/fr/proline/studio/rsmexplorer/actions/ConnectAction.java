/* 
 * Copyright (C) 2019
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
package fr.proline.studio.rsmexplorer.actions;


import fr.proline.studio.WindowManager;
import fr.proline.studio.dam.taskinfo.TaskInfoManager;
import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.ServerConnectionDialog;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;


/**
 * Action to connect or disconnect to a UDS database
 * @author jm235353
 */


public class ConnectAction  extends AbstractAction {

    private static ConnectAction m_action = null;
    
    private static boolean m_connectAction;
    
    private ConnectAction() {

        m_action = this;
        
        setConnectionType(true, true);
    }

    public static ConnectAction getAction() {
        if (m_action == null) {
            m_action = new ConnectAction();
        }
        return m_action;
    }

    
    public final static void setConnectionType(boolean connectAction, boolean enabled) {
        m_connectAction = connectAction;
        
        if (m_action != null) {
            m_action.setEnabled(enabled);
            if (connectAction) {
                m_action.putValue(Action.NAME, "Connect...");
            } else {
                m_action.putValue(Action.NAME, "Change User...");
            }
        }
    }

    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (m_connectAction) {
            // Connect...
        
            ServerConnectionManager serciceConnectionMgr = ServerConnectionManager.getServerConnectionManager();

            if ((serciceConnectionMgr.isNotConnected()) || (serciceConnectionMgr.isConnectionFailed())) {
                // the user need to enter connection parameters

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        ServerConnectionDialog serverConnectionDialog = ServerConnectionDialog.getDialog(WindowManager.getDefault().getMainWindow());
                        serverConnectionDialog.centerToScreen();
                        //databaseConnectionDialog.centerToFrame(WindowManager.getDefault().getMainWindow()); // does not work : main window has not its size most of the time at this point
                        serverConnectionDialog.setVisible(true);

                        ServerConnectionManager serciceConnectionMgr = ServerConnectionManager.getServerConnectionManager();
                        if (serciceConnectionMgr.isConnectionDone()) {
                            ProjectExplorerPanel.getProjectExplorerPanel().startLoadingProjects();
                        }
                    }
                });

            } else {

                // open the dialog to connect to another user
                ServerConnectionDialog serverConnectionDialog = ServerConnectionDialog.getDialog(WindowManager.getDefault().getMainWindow());
                serverConnectionDialog.setChangeUser();
                serverConnectionDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
                serverConnectionDialog.setVisible(true);
            }

        } else {
            // Disconnect...
            
            
            // check if there is tasks being done which ask not to disconnect/close the application
            if (TaskInfoManager.getTaskInfoManager().askBeforeExitingApp()) {
                InfoDialog exitDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "Warning", "You should not disconnect. Important tasks are being done.\nAre you sure you want to disconnect ?", true);
                exitDialog.setButtonName(DefaultDialog.BUTTON_OK, "Yes");
                exitDialog.setButtonName(DefaultDialog.BUTTON_CANCEL, "No");
                exitDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
                exitDialog.setVisible(true);

                if (exitDialog.getButtonClicked() == DefaultDialog.BUTTON_CANCEL) {
                    // No clicked
                    return;
                }

            }
            



            // open the dialog to connect to another user
            ServerConnectionDialog serverConnectionDialog = ServerConnectionDialog.getDialog(WindowManager.getDefault().getMainWindow());
            serverConnectionDialog.setChangeUser();
            serverConnectionDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
            serverConnectionDialog.setVisible(true);
            
            
        }
    }


    
}
