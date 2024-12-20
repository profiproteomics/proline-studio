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
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.uds.UserAccount;
import fr.proline.studio.WindowManager;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dock.AbstractTopPanel;
import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.gui.ConnectionDialog;
import fr.proline.studio.pattern.DataboxDataAnalyzer;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopPanel;
import fr.proline.studio.rsmexplorer.MzdbFilesTopPanel;
import fr.proline.studio.rsmexplorer.TaskLogTopPanel;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.TreeUtils;
import fr.proline.studio.rsmexplorer.gui.calc.DataAnalyzerPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Dialog to Connect to the server
 *
 * @author jm235353
 */
public class ServerConnectionDialog extends ConnectionDialog {

    private boolean m_changingUser = false;

    private static ServerConnectionDialog m_singletonDialog = null;

    public static ServerConnectionDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new ServerConnectionDialog(parent);
        }

        return m_singletonDialog;
    }

    private ServerConnectionDialog(Window parent) {
        super(parent, "Server Connection", " Server Parameter ", "Server Host :");

        setDocumentationSuffix("id.2szc72q");

        setButtonVisible(BUTTON_DEFAULT, true);

        initDefaults();

    }

    private void initDefaults() {

        ServerConnectionManager serverConnectionManager = ServerConnectionManager.getServerConnectionManager();

        m_serverURLTextField.setText(serverConnectionManager.getServerURL());
        m_userTextField.setText(serverConnectionManager.getProjectUser());

        String password = serverConnectionManager.getUserPassword();
        m_passwordField.setText(password);
        m_rememberPasswordCheckBox.setSelected(!password.isEmpty());

    }

    @Override
    protected boolean okCalled() {
        if (!checkParameters()) {
            return false;
        }

        if (m_changingUser) {
            // close all specific windows
            Set<AbstractTopPanel> tcs = WindowManager.getDefault().getMainWindow().getTopPanels();
            Iterator<AbstractTopPanel> itTop = tcs.iterator();
            while (itTop.hasNext()) {
                AbstractTopPanel topComponent = itTop.next();
                if ( (topComponent instanceof DataBoxViewerTopPanel)  && !(topComponent instanceof TaskLogTopPanel)) {

                    if (topComponent instanceof DataBoxViewerTopPanel) {
                        DataBoxViewerTopPanel databoxComponent = (DataBoxViewerTopPanel) topComponent;
                        WindowBox winBox = databoxComponent.getWindowBox();
                        if (winBox.getEntryBox() instanceof DataboxDataAnalyzer) {
                            DataboxDataAnalyzer analyzer = (DataboxDataAnalyzer) winBox.getEntryBox();
                            if (analyzer.getPanel() instanceof DataAnalyzerPanel) {
                                DataAnalyzerPanel panel = (DataAnalyzerPanel) analyzer.getPanel();
                                panel.clearDataAnalyzerPanel();
                            }
                        }
                    }

                    WindowManager.getDefault().getMainWindow().closeWindow(topComponent);


                }
            }

            // must remove all projects
            ProjectExplorerPanel.getProjectExplorerPanel().clearAll();
        }

        connect(m_changingUser);

        // dialog will be closed if the connection is established
        return false;
    }

    @Override
    protected boolean defaultCalled() {
        initDefaults();

        return false;
    }

    public void setChangeUser() {
        m_serverURLTextField.setEnabled(false);
        m_changingUser = true;
    }

    private void connect(final boolean changingUser) {

        setBusy(true);

        String serverURL = m_serverURLTextField.getText().trim();
        if (serverURL.startsWith("http") && !serverURL.endsWith("/")) {
            //JPM.WART : server URL must ends with a "/"
            // if the user forgets it, the error is really strange (exception with no message reported)
            serverURL = serverURL + "/";
            m_serverURLTextField.setText(serverURL);
        }

        final String projectUser = m_userTextField.getText();
        final String password = new String(m_passwordField.getPassword());

        Runnable callback = new Runnable() {

            @Override
            public void run() {
                setBusy(false);

                ServerConnectionManager serverManager = ServerConnectionManager.getServerConnectionManager();
                if (serverManager.isConnectionFailed()) {
                    TaskError connectionError = serverManager.getConnectionError();
                    setStatus(true, connectionError.getErrorTitle());
                    JOptionPane.showMessageDialog(m_singletonDialog, connectionError.getErrorTitle(), "Database Connection Error", JOptionPane.ERROR_MESSAGE);
                } else if (serverManager.isConnectionDone()) {

                    m_serverURLTextField.setText(serverManager.getServerURL()); // could have been automatically changed

                    storeDefaults();
                    setVisible(false);

                    // Open the Help
                    if (HelpDialog.showAtStart()) {
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                HelpDialog helpDialog = HelpDialog.getDialog(WindowManager.getDefault().getMainWindow());
                                helpDialog.centerToScreen();
                                helpDialog.setVisible(true);
                            }
                        });
                    }

                    //Test isRetrieveSeqServiceAvailable to init boolean which will be tested in action...
                    if (!ServerConnectionManager.getServerConnectionManager().isRetrieveSeqServiceAvailable() && SeqDBInfoDialog.showAtStart() /*!DatabaseDataManager.getDatabaseDataManager().isSeqDatabaseExists()*/) {
                        SeqDBInfoDialog seqDBInfoDialog = new SeqDBInfoDialog(WindowManager.getDefault().getMainWindow());
                        seqDBInfoDialog.centerToScreen();
                        seqDBInfoDialog.setVisible(true);
                    }

                    if (changingUser) {

                        DatabaseDataManager udsMgr = DatabaseDataManager.getDatabaseDataManager();
                        UserAccount[] projectUsers = udsMgr.getProjectUsersArray();
                        int nb = projectUsers.length;
                        boolean foundUser = false;
                        for (int i = 0; i < nb; i++) {
                            UserAccount account = projectUsers[i];
                            if (projectUser.compareToIgnoreCase(account.getLogin()) == 0) {
                                udsMgr.setLoggedUser(account);
                                foundUser=true;
                                break;
                            }
                        }

                        if(!foundUser){
                            setStatus(true,"No user found with login "+projectUser);
                            JOptionPane.showMessageDialog(m_singletonDialog, "No user found with login "+projectUser, "Connection Error", JOptionPane.ERROR_MESSAGE);
                        } else
                            // start to load the data for the new user
                            ProjectExplorerPanel.getProjectExplorerPanel().startLoadingProjects();

                    }

                    if (m_serverURLTextField != null && m_serverURLTextField.isEnabled()) {

                        if (MzdbFilesTopPanel.getExplorer() != null) {
                            MzdbFilesTopPanel.getExplorer().getTreeFileChooserPanel().initTree();
                            MzdbFilesTopPanel.getExplorer().getTreeFileChooserPanel().restoreTree(TreeUtils.TreeType.SERVER);
                        }
                        
                    }
                }
            }

        };

        ServerConnectionManager serverManager = ServerConnectionManager.getServerConnectionManager();
        serverManager.tryServerConnection(callback, serverURL, projectUser, password, changingUser);

    }

    private void storeDefaults() {
        ServerConnectionManager serverManager = ServerConnectionManager.getServerConnectionManager();

        serverManager.setServerURL(m_serverURLTextField.getText());
        serverManager.setProjectUser(m_userTextField.getText());

        if (m_rememberPasswordCheckBox.isSelected()) {
            serverManager.setUserPassword(new String(m_passwordField.getPassword()));
        } else {
            serverManager.setUserPassword("");
        }

        serverManager.saveParameters();
    }

}
