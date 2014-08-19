package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.Window;
import javax.swing.*;

/**
 * Dialog to let the user change his password
 * @author VD225637
 */
public class ChangePasswordDialog extends DefaultDialog {

    protected JPasswordField m_oldPasswordField;
    protected JPasswordField m_passwordField;
    protected JPasswordField m_passwordRepeatField;

    public ChangePasswordDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("Change Password");
        setButtonVisible(BUTTON_HELP, false);
        setButtonVisible(BUTTON_DEFAULT, false);
        JPanel internalPanel = createInternalPanel();
        setInternalComponent(internalPanel);

    }

    private JPanel createInternalPanel() {
        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());
        
        JLabel oldPasswordLabel = new JLabel("Old password :");
        m_oldPasswordField = new JPasswordField(20);
        
        JLabel passwordLabel = new JLabel("New password :");
        m_passwordField = new JPasswordField(20);

        JLabel passwordRepeatLabel = new JLabel("Confirm new password :");
        m_passwordRepeatField = new JPasswordField(20);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;        
        internalPanel.add(oldPasswordLabel, c);

        c.gridx = 1;
        c.weightx = 1;
        internalPanel.add(m_oldPasswordField, c);
        
        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        internalPanel.add(passwordLabel, c);

        c.gridx = 1;
        c.weightx = 1;
        internalPanel.add(m_passwordField, c);

        c.gridy++;
        c.gridx = 0;
        c.weightx = 0;
        internalPanel.add(passwordRepeatLabel, c);

        c.gridx = 1;
        c.weightx = 1;
        internalPanel.add(m_passwordRepeatField, c);

        return internalPanel;
    }

    public String getUser(){
        return ServerConnectionManager.getServerConnectionManager().getProjectUser();
    }
    
    public String getPassword(){
        return new String(m_passwordField.getPassword());
    }
    
     public String getOldPassword(){
        return new String(m_oldPasswordField.getPassword());
    }
       
    @Override
    protected boolean okCalled() {

        char[] oldPswd = m_oldPasswordField.getPassword();
        char[] psw1 = m_passwordField.getPassword();
        char[] psw2 = m_passwordRepeatField.getPassword();

        if(psw1.length == 0 ) {
            setStatus(true, "Password must be specified");
            highlight(m_passwordField);
            return false;
        }
        
        if(psw2.length ==0 ){
            setStatus(true, "Repeat password ");
            highlight(m_passwordRepeatField);
            return false;  
        }
        
        if(oldPswd.length ==0){
             setStatus(true, "Old password must be specified");
            highlight(m_oldPasswordField);
            return false;  
        }
        
        if(psw1.length != psw2.length) {
            setStatus(true, "Password mismatch");
            highlight(m_passwordField);
            return false;
        }

        
        boolean equalsPsw = true;
        for (int i = 0; i < psw1.length; i++) {
            if (psw1[i] != psw2[i]) {
                equalsPsw = false;
                break;
            }
        }
        if (!equalsPsw) {
            setStatus(true, "Password mismatch");
            highlight(m_passwordField);
            return false;
        }
        return true;
    }
}
