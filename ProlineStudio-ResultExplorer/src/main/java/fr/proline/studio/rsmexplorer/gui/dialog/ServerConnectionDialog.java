package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import javax.swing.*;
import org.openide.windows.WindowManager;

/**
 * Dialog to Connect to the server
 * @author jm235353
 */
public class ServerConnectionDialog extends DefaultDialog {
    
    private JTextField m_serverURLTextField;
    private JTextField m_userTextField;
    private JPasswordField m_passwordField;
    private JCheckBox m_rememberPasswordCheckBox;
    
    private static ServerConnectionDialog m_singletonDialog = null;
    
    public static ServerConnectionDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new ServerConnectionDialog(parent);
        }

        return m_singletonDialog;
    }
    
    private ServerConnectionDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Server Connection");

        setHelpURL("http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:startsession");
        
        JPanel internalPanel = createInternalPanel();
        
        setInternalComponent(internalPanel);
        
        initDefaults();
        
        
    }
    
    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());


        JPanel URLPanel = createHostPanel();
        JPanel loginPanel = createLoginPanel();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 0;
        internalPanel.add(URLPanel, c);
        
        c.gridy++;
        internalPanel.add(loginPanel, c);
        
        return internalPanel;
    }
    
     
    private JPanel createHostPanel() {
        
        JPanel URLPanel = new JPanel(new GridBagLayout());
        URLPanel.setBorder(BorderFactory.createTitledBorder(" Server Parameter "));
        
        JLabel serverLabel = new JLabel("Server Host :");
        m_serverURLTextField = new JTextField(30);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        URLPanel.add(serverLabel, c);
        
        c.gridx = 1;
        c.weightx = 1;
        URLPanel.add(m_serverURLTextField, c);
        


        
        return URLPanel;
    }
    
    private JPanel createLoginPanel() {
        
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createTitledBorder(" User Parameters "));
        
        JLabel userLabel = new JLabel("User :");
        m_userTextField = new JTextField(30);
        JLabel passwordLabel = new JLabel("Password :");
        m_passwordField = new JPasswordField();
        m_rememberPasswordCheckBox = new JCheckBox("Remember Password");

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        loginPanel.add(userLabel, c);
        
        c.gridx = 1;
        c.weightx = 1;
        loginPanel.add(m_userTextField, c);
        
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        loginPanel.add(passwordLabel, c);
        
         c.gridx = 1;
        c.weightx = 1;
        loginPanel.add(m_passwordField, c);
        
        c.gridy++;
        loginPanel.add(m_rememberPasswordCheckBox, c);

        
        return loginPanel;
    }

    
    private void initDefaults() {

        ServerConnectionManager serverConnectionManager = ServerConnectionManager.getServerConnectionManager();

        m_serverURLTextField.setText(serverConnectionManager.getServerURL());
        m_userTextField.setText(serverConnectionManager.getProjectUser());
        
        String password = serverConnectionManager.getDatabasePassword();
        m_passwordField.setText(password);
        m_rememberPasswordCheckBox.setSelected(!password.isEmpty());

    }
    
    @Override
    protected boolean okCalled() {
        if (!checkParameters()) {
            return false;
        }
        
        connect();
        
        // dialog will be closed if the connection is established
        return false;
    }
 
    @Override
    protected boolean cancelCalled() {
        return true;
    }

    @Override
    protected boolean defaultCalled() {
        initDefaults();

        return false;
    }
    
    private boolean checkParameters() {


        
        String serverURL = m_serverURLTextField.getText();
        if (serverURL.isEmpty()) {
            setStatus(true, "You must fill the Server URL");
            highlight(m_serverURLTextField);
            return false;
        }
        if (serverURL.length() <= "http://".length()) {
            setStatus(true, "The Server URL is incorrect");
            highlight(m_serverURLTextField);
            return false;
        }
        
        String user = m_userTextField.getText();
        if (user.isEmpty()) {
            setStatus(true, "You must fill the User");
            highlight(m_userTextField);
            return false;
        }


        
        return true;
        
    }
    
    private void connect() {
    
        setBusy(true);
        
        String serverURL = m_serverURLTextField.getText();
        if (! serverURL.endsWith("/")) {
            //JPM.WART : server URL must ends with a "/"
            // if the user forgets it, the error is really strange (exception with no message reported)
            serverURL = serverURL+"/";
            m_serverURLTextField.setText(serverURL);
        }
        
        String projectUser  = m_userTextField.getText();
        String password = new String(m_passwordField.getPassword());
        
        
        
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
                }
       
            }
            
        };
        
        
        ServerConnectionManager serverManager = ServerConnectionManager.getServerConnectionManager();
        serverManager.tryServerConnection(callback, serverURL, projectUser, password);
        
    }
    
    private void storeDefaults() {
        ServerConnectionManager serverManager = ServerConnectionManager.getServerConnectionManager();

        serverManager.setServerURL(m_serverURLTextField.getText());
        serverManager.setProjectUser(m_userTextField.getText());

        if (m_rememberPasswordCheckBox.isSelected()) {
            serverManager.setDatabasePassword(new String(m_passwordField.getPassword()));
        }

        
        serverManager.saveParameters();
    }
    
}
