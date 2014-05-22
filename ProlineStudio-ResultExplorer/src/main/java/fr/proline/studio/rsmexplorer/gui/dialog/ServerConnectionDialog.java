package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.uds.UserAccount;
import fr.proline.studio.dam.UDSDataManager;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.gui.ConnectionDialog;
import fr.proline.studio.rsmexplorer.actions.ConnectAction;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import java.awt.Window;
import javax.swing.*;
import org.openide.windows.WindowManager;

/**
 * Dialog to Connect to the server
 * @author jm235353
 */
public class ServerConnectionDialog extends ConnectionDialog {

    private boolean m_changingUser = false;
    private String m_keepLastPasswordForChangingUser = null;
    
    private static ServerConnectionDialog m_singletonDialog = null;
    
    public static ServerConnectionDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new ServerConnectionDialog(parent);
        }

        return m_singletonDialog;
    }
    
    private ServerConnectionDialog(Window parent) {
        super(parent, "Server Connection", " Server Parameter ", "Server Host :");

        setHelpURL("http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:startsession");

        
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
        
        /*if (m_changingUser) {
            // aleady connected to the server and UDS database, we check the new user
            String projectUser = m_userTextField.getText();
            String password = new String(m_passwordField.getPassword());
            
            UDSDataManager udsMgr = UDSDataManager.getUDSDataManager();
            boolean foundUser = false;
            UserAccount[] projectUsers = udsMgr.getProjectUsersArray();
            int nb = projectUsers.length;
            for (int i = 0; i < nb; i++) {
                UserAccount account = projectUsers[i];
                if (projectUser.compareToIgnoreCase(account.getLogin()) == 0) {
                    udsMgr.setProjectUser(account);
                    foundUser = true;
                    break;
                }
            }
            
            if (!foundUser) {
                 setStatus(true, "Unknown Project User: "+projectUser);
                 highlight(m_userTextField);
                 return false;
            }
            
            if (m_keepLastPasswordForChangingUser.compareTo(password) != 0) {
                setStatus(true, "Incorrect Password");
                highlight(m_passwordField);
                return false;
            }

            // start to load the data for the new user
            ProjectExplorerPanel.getProjectExplorerPanel().startLoadingProjects();

            
            return true;
 
        } else {*/
            connect(m_changingUser);
        //}
        
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
        
        String serverURL = m_serverURLTextField.getText();
        if (! serverURL.endsWith("/")) {
            //JPM.WART : server URL must ends with a "/"
            // if the user forgets it, the error is really strange (exception with no message reported)
            serverURL = serverURL+"/";
            m_serverURLTextField.setText(serverURL);
        }
        
        final String projectUser  = m_userTextField.getText();
        final String password = new String(m_passwordField.getPassword());
        
        
        
        Runnable callback = new Runnable() {

            @Override
            public void run() {
                setBusy(false);
                
                ServerConnectionManager serverManager = ServerConnectionManager.getServerConnectionManager();
                if (serverManager.isConnectionFailed() ) {
                    TaskError connectionError = serverManager.getConnectionError();
                    setStatus(true, connectionError.getErrorTitle());
                    JOptionPane.showMessageDialog(m_singletonDialog, connectionError, "Database Connection Error", JOptionPane.ERROR_MESSAGE);
                } else if (serverManager.isConnectionDone()) {
                    
                    m_keepLastPasswordForChangingUser = password;
                    
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
                    
                    if (changingUser) {

                        UDSDataManager udsMgr = UDSDataManager.getUDSDataManager();
                        boolean foundUser = false;
                        UserAccount[] projectUsers = udsMgr.getProjectUsersArray();
                        int nb = projectUsers.length;
                        for (int i = 0; i < nb; i++) {
                            UserAccount account = projectUsers[i];
                            if (projectUser.compareToIgnoreCase(account.getLogin()) == 0) {
                                udsMgr.setProjectUser(account);
                                foundUser = true;
                                break;
                            }
                        }


                        // start to load the data for the new user
                        ProjectExplorerPanel.getProjectExplorerPanel().startLoadingProjects();
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
