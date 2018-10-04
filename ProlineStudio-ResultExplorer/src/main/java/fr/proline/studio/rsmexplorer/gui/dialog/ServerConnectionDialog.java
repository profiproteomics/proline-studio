package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.uds.UserAccount;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.gui.ConnectionDialog;
import fr.proline.studio.pattern.DataboxDataAnalyzer;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.MzdbFilesTopComponent;
import fr.proline.studio.rsmexplorer.PropertiesTopComponent;
import fr.proline.studio.rsmexplorer.TaskLogTopComponent;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.TreeStateUtil;
import fr.proline.studio.rsmexplorer.gui.calc.DataAnalyzerPanel;
import java.awt.Window;
import java.util.Iterator;
import java.util.Set;
import javax.swing.*;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

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

        setDocumentationSuffix("id.1pxezwc");

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
            Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
            Iterator<TopComponent> itTop = tcs.iterator();
            while (itTop.hasNext()) {
                TopComponent topComponent = itTop.next();
                if (((topComponent instanceof DataBoxViewerTopComponent) || (topComponent instanceof PropertiesTopComponent)) && !(topComponent instanceof TaskLogTopComponent)) {

                    if (topComponent instanceof DataBoxViewerTopComponent) {
                        DataBoxViewerTopComponent databoxComponent = (DataBoxViewerTopComponent) topComponent;
                        WindowBox winBox = databoxComponent.getWindowBox();
                        if (winBox.getEntryBox() instanceof DataboxDataAnalyzer) {
                            DataboxDataAnalyzer analyzer = (DataboxDataAnalyzer) winBox.getEntryBox();
                            if (analyzer.getPanel() instanceof DataAnalyzerPanel) {
                                DataAnalyzerPanel panel = (DataAnalyzerPanel) analyzer.getPanel();
                                panel.clearDataAnalyzerPanel();
                            }
                        }
                    }

                    topComponent.close();

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

                    m_serverURLTextField.setText(serverManager.getServerURL()); // could have been automaticaly changed

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

                    // check if we have SeqDb
                    if (SeqDBInfoDialog.showAtStart() && !DatabaseDataManager.getDatabaseDataManager().isSeqDatabaseExists()) {
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                SeqDBInfoDialog seqDBInfoDialog = new SeqDBInfoDialog(WindowManager.getDefault().getMainWindow());
                                seqDBInfoDialog.centerToScreen();
                                seqDBInfoDialog.setVisible(true);
                            }
                        });
                    }

                    if (changingUser) {

                        DatabaseDataManager udsMgr = DatabaseDataManager.getDatabaseDataManager();
                        UserAccount[] projectUsers = udsMgr.getProjectUsersArray();
                        int nb = projectUsers.length;
                        for (int i = 0; i < nb; i++) {
                            UserAccount account = projectUsers[i];
                            if (projectUser.compareToIgnoreCase(account.getLogin()) == 0) {
                                udsMgr.setLoggedUser(account);
                                break;
                            }
                        }

                        // start to load the data for the new user
                        ProjectExplorerPanel.getProjectExplorerPanel().startLoadingProjects();

                    }

                    if (m_serverURLTextField != null && m_serverURLTextField.isEnabled()) {

                        if (MzdbFilesTopComponent.getExplorer() != null) {
                            MzdbFilesTopComponent.getExplorer().getTreeFileChooserPanel().initTree();
                            MzdbFilesTopComponent.getExplorer().getTreeFileChooserPanel().restoreTree(TreeStateUtil.TreeType.SERVER);
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
