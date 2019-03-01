package fr.proline.studio.rsmexplorer.gui.admin;

import fr.proline.core.orm.uds.UserAccount;
import fr.proline.studio.gui.DefaultDialog;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 *
 * @author JM235353
 */
public class UserAccountDialog extends DefaultDialog  {

    private static UserAccountDialog m_singletonDialog = null;

    private static final String PASSWORD_HIDDEN = "******";
    
    public enum DialogMode {
        CREATE_USER,
        MODIFY_USER
    }
    
    private DialogMode m_mode;
    private JTextField m_loginTextField;
    private JTextField m_passwordTextField;
    private JRadioButton m_userRadioButton;
    private JRadioButton m_adminRadioButton;

    private boolean m_initialAdminUserGroup;

    public static UserAccountDialog getDialog(Window parent, DialogMode mode) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new UserAccountDialog(parent);
        }
        
        m_singletonDialog.initMode(mode);
        
        return m_singletonDialog;
    }

    public UserAccountDialog(Window parent) {
        super(parent, Dialog.ModalityType.DOCUMENT_MODAL);

        //setDocumentationSuffix("h.eb8nfjv41vkz"); //JPM.TODO

        initInternalPanel();

    }

    private void initMode(DialogMode mode) {
        m_mode = mode;
       
        
        
        switch(mode) {
            case CREATE_USER:
                setTitle("Create User Account");
                m_loginTextField.setEnabled(true);
                m_initialAdminUserGroup = false;
                break;
            case MODIFY_USER:
                setTitle("Modify User Account");
                m_loginTextField.setEnabled(false);
                m_passwordTextField.setText("******");
                break;
        }
        m_loginTextField.setText("");
        m_userRadioButton.setSelected(true);
    }
    
    private void initInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());

        JLabel loginLabel = new JLabel("Login :");
        JLabel groupLabel = new JLabel("Group :");
        JLabel passwordLabel = new JLabel("Pasword :");
        
        m_loginTextField = new JTextField(30);
        
        ButtonGroup group = new ButtonGroup();
        m_userRadioButton = new JRadioButton("User");
        m_adminRadioButton = new JRadioButton("Admin");
        group.add(m_userRadioButton);
        group.add(m_adminRadioButton);
        
        m_passwordTextField = new JTextField(30);
        m_passwordTextField.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (m_passwordTextField.getText().compareTo(PASSWORD_HIDDEN) == 0) {
                    m_passwordTextField.setText("");
                }
            }
        });
        
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        internalPanel.add(loginLabel, c);
        
        c.gridx++;
        c.gridwidth = 2;
        internalPanel.add(m_loginTextField, c);
        
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        internalPanel.add(groupLabel, c);
        
        c.gridx++;
        internalPanel.add(m_userRadioButton, c);
        
        c.gridx++;
        internalPanel.add(m_adminRadioButton, c);

        c.gridx = 0;
        c.gridy++;
        internalPanel.add(passwordLabel, c);
        
        c.gridx++;
        c.gridwidth = 2;
        internalPanel.add(m_passwordTextField, c);
        
        setInternalComponent(internalPanel);

    }
    
    public void setUserAccountInfo(String logging, boolean isAdmin) {
        m_loginTextField.setText(logging);
        if (isAdmin) {
            m_adminRadioButton.setSelected(true);
            m_initialAdminUserGroup = true;
        } else {
            m_userRadioButton.setSelected(true);
            m_initialAdminUserGroup = false;
        }
    }

    public String getLogin() {
        return m_loginTextField.getText().trim();
    }
    
    public boolean isAdmin() {
        return m_adminRadioButton.isSelected();
    }
    
    public boolean userGroupChanged() {
        return m_adminRadioButton.isSelected() ^ m_initialAdminUserGroup;
    }
    
    public boolean passwordChanged() {
        String password = m_passwordTextField.getText().trim();
        return  ((!password.isEmpty()) && (m_passwordTextField.getText().compareTo(PASSWORD_HIDDEN) != 0));
    }
    
    public String getPassword() {
        return m_passwordTextField.getText();
    }
 
    private boolean checkParameters() {
        String login = getLogin();
        if (login.isEmpty() || (login.length()<3)) {
            setStatus(true, "Login must contain at least 3 characters.");
            highlight(m_loginTextField);
            return false;
        }

        if ((m_mode == DialogMode.CREATE_USER) && (getPassword().length()<4)) {
            setStatus(true, "Password must contain at least 4 characters");
            highlight(m_passwordTextField);
            return false;
        }

        return true;
    }
    
    @Override
    protected boolean okCalled() {
        
        // check parameters
        if (!checkParameters()) {
            return false;
        }
        
        return true;
    }



}
