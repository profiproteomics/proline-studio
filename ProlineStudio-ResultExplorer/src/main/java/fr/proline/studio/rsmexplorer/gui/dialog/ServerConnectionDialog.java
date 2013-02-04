package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import javax.swing.*;

/**
 *
 * @author jm235353
 */
public class ServerConnectionDialog extends DefaultDialog {
    
    private JTextField serverURLTextField;
    private JTextField userTextField;
    private JPasswordField passwordField;
    private JCheckBox rememberPasswordCheckBox;
    
    private static ServerConnectionDialog singletonDialog = null;
    
    public static ServerConnectionDialog getDialog(Window parent) {
        if (singletonDialog == null) {
            singletonDialog = new ServerConnectionDialog(parent);
        }

        return singletonDialog;
    }
    
    private ServerConnectionDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Server Connection");

        JPanel internalPanel = createInternalPanel();
        
        setInternalComponent(internalPanel);
        
        initDefaults();
        
    }
    
    private JPanel createInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());


        JPanel URLPanel = createURLPanel();
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
    
     
    private JPanel createURLPanel() {
        
        JPanel URLPanel = new JPanel(new GridBagLayout());
        URLPanel.setBorder(BorderFactory.createTitledBorder(" Server Parameter "));
        
        JLabel serverLabel = new JLabel("Server URL :");
        serverURLTextField = new JTextField(30);

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
        URLPanel.add(serverURLTextField, c);
        


        
        return URLPanel;
    }
    
    private JPanel createLoginPanel() {
        
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBorder(BorderFactory.createTitledBorder(" User Parameters "));
        
        JLabel userLabel = new JLabel("Project User :");
        userTextField = new JTextField(30);
        JLabel passwordLabel = new JLabel("Password :");
        passwordField = new JPasswordField();
        rememberPasswordCheckBox = new JCheckBox("Remember Password");

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
        loginPanel.add(userTextField, c);
        
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        loginPanel.add(passwordLabel, c);
        
         c.gridx = 1;
        c.weightx = 1;
        loginPanel.add(passwordField, c);
        
        c.gridy++;
        loginPanel.add(rememberPasswordCheckBox, c);

        
        return loginPanel;
    }

    
    private void initDefaults() {

        ServerConnectionManager serverConnectionManager = ServerConnectionManager.getServerConnectionManager();

        serverURLTextField.setText(serverConnectionManager.getServerURL());
        userTextField.setText(serverConnectionManager.getProjectUser());
        
        String password = serverConnectionManager.getDatabasePassword();
        passwordField.setText(password);
        rememberPasswordCheckBox.setSelected(!password.isEmpty());

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


        
        String serverURL = serverURLTextField.getText();
        if (serverURL.isEmpty()) {
            setStatus(true, "You must fill the Server URL");
            highlight(serverURLTextField);
            return false;
        }
        if (serverURL.length() < "http://".length()) {
            setStatus(true, "The Server URL is incorrect");
            highlight(serverURLTextField);
            return false;
        }
        
        String user = userTextField.getText();
        if (user.isEmpty()) {
            setStatus(true, "You must fill the Project User");
            highlight(userTextField);
            return false;
        }


        
        return true;
        
    }
    
    private void connect() {
    
        setBusy(true);
        
        String serverURL = serverURLTextField.getText();
        String projectUser  = userTextField.getText();
        String password = new String(passwordField.getPassword());
        
        
        
        Runnable callback = new Runnable() {

            @Override
            public void run() {
                setBusy(false);
                
                ServerConnectionManager serverManager = ServerConnectionManager.getServerConnectionManager();
                if (serverManager.isConnectionFailed() ) {
                    String connectionError = serverManager.getConnectionError();
                    setStatus(true, connectionError);
                    JOptionPane.showMessageDialog(singletonDialog, connectionError, "Database Connection Error", JOptionPane.ERROR_MESSAGE);
                } else if (serverManager.isConnectionDone()) {
                    storeDefaults();
                    setVisible(false);     
                }
       
            }
            
        };
        
        
        ServerConnectionManager serverManager = ServerConnectionManager.getServerConnectionManager();
        serverManager.tryServerConnection(callback, serverURL, projectUser, password);
        
    }
    
    private void storeDefaults() {
        ServerConnectionManager serverManager = ServerConnectionManager.getServerConnectionManager();

        serverManager.setServerURL(serverURLTextField.getText());
        serverManager.setProjectUser(userTextField.getText());

        if (rememberPasswordCheckBox.isSelected()) {
            serverManager.setDatabasePassword(new String(passwordField.getPassword()));
        }

        
        serverManager.saveParameters();
    }
    
}
