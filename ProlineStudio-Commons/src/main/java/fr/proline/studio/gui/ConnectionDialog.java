package fr.proline.studio.gui;


import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import javax.swing.*;

/**
 * Dialog to Connect to the server
 * @author jm235353
 */
public abstract class ConnectionDialog extends DefaultDialog {
    
    protected JTextField m_serverURLTextField;
    protected JTextField m_userTextField;
    protected JPasswordField m_passwordField;
    protected JCheckBox m_rememberPasswordCheckBox;
  

    protected ConnectionDialog(Window parent, String title, String serverParameterLabel, String serverHostLabel) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle(title);

        JPanel internalPanel = createInternalPanel(serverParameterLabel, serverHostLabel);
        
        setInternalComponent(internalPanel);

    }
    
    private JPanel createInternalPanel(String serverParameterLabel, String serverHostLabel) {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());


        JPanel URLPanel = createHostPanel(serverParameterLabel, serverHostLabel);
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
    
     
    private JPanel createHostPanel(String serverParameterLabel, String serverHostLabel) {
        
        JPanel URLPanel = new JPanel(new GridBagLayout());
        URLPanel.setBorder(BorderFactory.createTitledBorder(serverParameterLabel));
        
        JLabel serverLabel = new JLabel(serverHostLabel);
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

    
    @Override
    protected abstract boolean okCalled();
 
    @Override
    protected boolean cancelCalled() {
        return true;
    }

    @Override
    protected abstract boolean defaultCalled();

    
    protected boolean checkParameters() {


        
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


}
