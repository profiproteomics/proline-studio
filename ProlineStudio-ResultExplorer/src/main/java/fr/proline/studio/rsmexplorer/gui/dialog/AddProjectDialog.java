package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.UserAccount;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.List;
import java.util.Set;
/**
 * Dialog to create or modify a Project . The owner will be the current user.
 * It is possible to add co-owners
 * @author jm235353
 */
public class AddProjectDialog extends DefaultDialog {
    
    private static AddProjectDialog m_singletonDialog = null;

    private JTextField m_nameTextField;
    private JTextArea m_descriptionTextArea;
    
    private JList<UserAccount> m_userAccountList;
    private JScrollPane m_userListScrollPane;

    private JButton m_addUserAccountButton;
    private JButton m_removeFileButton;
    
    private JPanel m_userAccountPanel;
    
    private boolean m_canModifyValues;

    
    public static AddProjectDialog getAddProjectDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new AddProjectDialog(parent);
        }

        m_singletonDialog.initialize(null);
        
        return m_singletonDialog;
    }

    public static AddProjectDialog getModifyProjectDialog(Window parent, Project p) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new AddProjectDialog(parent);
        }

        m_singletonDialog.initialize(p);
        
        return m_singletonDialog;
    }
    
    public void initialize(Project p) {

        // remove all owners if needed
        DefaultListModel userAccountListModel = ((DefaultListModel) m_userAccountList.getModel());
        userAccountListModel.clear();
        
        m_canModifyValues = true;
        
        // initialize fields
        if (p == null) {
            m_nameTextField.setText("");
            m_descriptionTextArea.setText("");
            
            setTitle("Add Project");

            setButtonVisible(BUTTON_HELP, true);
            setHelpURL("http://biodev.extra.cea.fr/docs/proline/doku.php?id=how_to:studio:createproject");

        } else {
            m_nameTextField.setText(p.getName());
            m_descriptionTextArea.setText(p.getDescription());
            
            setTitle("Modify Project Parameters");
            
            setButtonVisible(BUTTON_HELP, false); //JPM.TODO
            
            // User logged
            UserAccount loggedUserAccount = DatabaseDataManager.getDatabaseDataManager().getLoggedUser();
            
            // Main user of the project
            UserAccount mainUserAccount = p.getOwner();
            
            boolean isCreatorOfTheProject = (loggedUserAccount.getId() == mainUserAccount.getId());
            
            // fill owners
            Set<UserAccount> members = p.getMembers();
            for (UserAccount userAccount : members) {
                if ((isCreatorOfTheProject) && (userAccount.getId() == mainUserAccount.getId())) {
                    continue;
                }
                userAccountListModel.addElement(userAccount);
            }
            
            m_userListScrollPane.setEnabled(isCreatorOfTheProject);
            m_addUserAccountButton.setEnabled(isCreatorOfTheProject);
            m_removeFileButton.setEnabled(isCreatorOfTheProject);

            m_canModifyValues = DatabaseDataManager.getDatabaseDataManager().ownProject(p);
            
        }
        
        updateEnabled(m_canModifyValues);
    }

    private AddProjectDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setResizable(true);

        initInternalPanel();
    }

    
    private void initInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());


        JPanel projectParametersPanel = createProjectParametersPanel();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        internalPanel.add(projectParametersPanel, c);

        m_userAccountPanel = createUserAccountPanel();
        
        c.gridy++;
        c.weighty = 1.0;
        internalPanel.add(m_userAccountPanel, c);

        setInternalComponent(internalPanel);
    }
    
    
    private JPanel createProjectParametersPanel() {
        
        JPanel projectParametersPanel = new JPanel(new GridBagLayout());
        projectParametersPanel.setBorder(BorderFactory.createTitledBorder(" Project Parameters "));
        
        JLabel projectNameLabel = new JLabel("Name :");
        m_nameTextField = new JTextField(30);
        
        JLabel projectDescriptionLabel = new JLabel("Description :");
        m_descriptionTextArea = new JTextArea();
        JScrollPane desciptionScrollPane = new JScrollPane(m_descriptionTextArea) {

            private Dimension preferredSize = new Dimension(360, 200);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }
        };

        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        projectParametersPanel.add(projectNameLabel, c);
        
        c.gridx = 1;
        c.weightx = 1;
        projectParametersPanel.add(m_nameTextField, c);
        
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        projectParametersPanel.add(projectDescriptionLabel, c);
        
        c.gridx = 1;
        c.weightx = 1;
        c.weighty = 1;
        projectParametersPanel.add(desciptionScrollPane, c);
        

        // add blanks, so it is nicer.
        c.gridx = 2;
        c.weightx = 0;
        c.weighty = 0;
        projectParametersPanel.add(Box.createHorizontalStrut(26), c);
        
        c.gridx = 0;
        c.gridy++;
        projectParametersPanel.add(Box.createHorizontalStrut(60), c);
        
        return projectParametersPanel;
    }
    
    
    
    private JPanel createUserAccountPanel() {

        // Creation of Objects for File Selection Panel
        JPanel userAccountPanel = new JPanel(new GridBagLayout());
        userAccountPanel.setBorder(BorderFactory.createTitledBorder(" Project Users "));

        m_userAccountList = new JList<>(new DefaultListModel());
        m_userAccountList.setCellRenderer(UserAccountListRenderer.getRenderer()); 
        m_userListScrollPane = new JScrollPane(m_userAccountList) {

            private final Dimension preferredSize = new Dimension(360, 200);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }
        };

        m_addUserAccountButton = new JButton(IconManager.getIcon(IconManager.IconType.USERS));
        m_addUserAccountButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        m_removeFileButton = new JButton(IconManager.getIcon(IconManager.IconType.ERASER));
        m_removeFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        // Placement of Objects for File Selection Panel
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 3;
        c.weightx = 1.0;
        c.weighty = 1.0;
        userAccountPanel.add(m_userListScrollPane, c);


        c.gridx++;
        c.gridheight = 1;
        c.weightx = 0;
        c.weighty = 0;
        userAccountPanel.add(m_addUserAccountButton, c);

        c.gridy++;
        userAccountPanel.add(m_removeFileButton, c);

        c.gridy++;
        userAccountPanel.add(Box.createVerticalStrut(30), c);

        // add blanks, so it is nicer.
        c.gridy++;
        c.gridx = 0;
        userAccountPanel.add(Box.createHorizontalStrut(60), c);
        
        c.gridx = 2;
        userAccountPanel.add(Box.createHorizontalStrut(26), c);
        
        
        
        
        
        
        // Actions on objects
        m_userAccountList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                boolean sometingSelected = (m_userAccountList.getSelectedIndex() != -1);
                m_removeFileButton.setEnabled(sometingSelected);
            }


        });


        m_addUserAccountButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                // construct a HashSet with owners already in the list and with the logged owner
                DefaultListModel userAccountListModel = ((DefaultListModel) m_userAccountList.getModel());
                HashSet<Long> userAccountOfProjectSet = new HashSet<>();
                int nb = userAccountListModel.getSize();
                for (int i=0;i<nb;i++) {
                    userAccountOfProjectSet.add(((UserAccount)userAccountListModel.getElementAt(i)).getId());
                }
                userAccountOfProjectSet.add(DatabaseDataManager.getDatabaseDataManager().getLoggedUser().getId());

                // open the dialog to select new owners
                SelectUserAccountDialog dialog = new SelectUserAccountDialog(m_singletonDialog, userAccountOfProjectSet);
                dialog.setLocationRelativeTo(m_addUserAccountButton);
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    List<UserAccount> userAccounts = dialog.getSelectedUserAccounts();

                    for (UserAccount userAccount : userAccounts) {
                        userAccountListModel.addElement(userAccount);
                    }
                }
            }
        });

        m_removeFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                List<UserAccount> selectedValues = m_userAccountList.getSelectedValuesList();
                Iterator<UserAccount> it = selectedValues.iterator();
                while (it.hasNext()) {
                    ((DefaultListModel) m_userAccountList.getModel()).removeElement(it.next());
                }
                m_removeFileButton.setEnabled(false);
            }
        });

        return userAccountPanel;

    }

        
    private void updateEnabled(boolean canModifyValues) {
        m_nameTextField.setEnabled(canModifyValues);
        m_descriptionTextArea.setEnabled(canModifyValues);
        m_userAccountList.setEnabled(canModifyValues);
        m_addUserAccountButton.setEnabled(canModifyValues);
        m_removeFileButton.setEnabled(canModifyValues);
    }
    
    @Override
    protected boolean okCalled() {

        if (!m_canModifyValues) {
            return true;
        }

        // check parameters
        if (!checkParameters()) {
            return false;
        }

        return true;
    }

    @Override
    protected boolean cancelCalled() {

        return true;
    }
    
    private boolean checkParameters() {
        String name = m_nameTextField.getText();
        if (name.isEmpty()) {
            setStatus(true, "You must fill the Project Name.");
            highlight(m_nameTextField);
            return false;
        }
        if (name.length()>250) {
            setStatus(true, "Project Name must not exceed 250 characters.");
            highlight(m_nameTextField);
            return false;
        }
        
        String description = m_descriptionTextArea.getText();
        if (description.isEmpty()) {
            setStatus(true, "You must fill the Project Description.");
            highlight(m_descriptionTextArea);
            return false;
        }
        
        if (description.length()>1000) {
            setStatus(true, "Description must not exceed 1000 characters.");
            highlight(m_nameTextField);
            return false;
        }
        
        return true;
    }
 
    public boolean canModifyValues() {
        return m_canModifyValues;
    }
    
    public String getProjectName() {
        return m_nameTextField.getText();
    }
    
    public String getProjectDescription() {
        return m_descriptionTextArea.getText();
    }
    
    public ArrayList<UserAccount> getUserAccountList() {
        DefaultListModel userAccountListModel = ((DefaultListModel) m_userAccountList.getModel());
        int nb = userAccountListModel.getSize();
        ArrayList<UserAccount> userAccountList = new ArrayList<>(nb);
        userAccountList.add(DatabaseDataManager.getDatabaseDataManager().getLoggedUser());
        for (int i = 0; i < nb; i++) {
            userAccountList.add(((UserAccount) userAccountListModel.getElementAt(i)));
        }
        
        return userAccountList;
    }
    
    
    
    
    public class SelectUserAccountDialog extends DefaultDialog {

        private JList<UserAccount> m_userList;

        public SelectUserAccountDialog(Window parent, HashSet<Long> userAccountOfProjectSet) {
            super(parent, Dialog.ModalityType.APPLICATION_MODAL);

            setTitle("User Accounts");

            setButtonVisible(BUTTON_HELP, false);

            UserAccount[] users = DatabaseDataManager.getDatabaseDataManager().getProjectUsersArray();
            
            setInternalComponent(createInternalPanel(userAccountOfProjectSet, users));
        }

        private JPanel createInternalPanel(HashSet<Long> userAccountOfProjectSet, UserAccount[] users) {
            JPanel internalPanel = new JPanel(new GridBagLayout());
            internalPanel.setBorder(BorderFactory.createTitledBorder(" Select User Accounts "));

            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(5, 5, 5, 5);

            // create graphical objects
            DefaultListModel<UserAccount> listModel = new DefaultListModel<>();
            for (int i = 0; i < users.length; i++) {
                UserAccount userAccount = users[i];
                if (! userAccountOfProjectSet.contains(userAccount.getId())) {
                    listModel.addElement(users[i]);
                }
                
            }
            m_userList = new JList<>(listModel);
            m_userList.setCellRenderer(UserAccountListRenderer.getRenderer()); 

            JScrollPane filesListScrollPane = new JScrollPane(m_userList) {

                private final Dimension preferredSize = new Dimension(280, 140);

                @Override
                public Dimension getPreferredSize() {
                    return preferredSize;
                }
            };

            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1.0;
            c.weighty = 1.0;
            internalPanel.add(filesListScrollPane, c);

            return internalPanel;
        }

        @Override
        protected boolean okCalled() {

            return true;
        }

        @Override
        protected boolean cancelCalled() {
            return true;
        }

        public List<UserAccount> getSelectedUserAccounts() {
            return m_userList.getSelectedValuesList();
        }



        
    }
    
    private static class UserAccountListRenderer extends DefaultListCellRenderer {

        private static UserAccountListRenderer m_singleton = null;
        
        public static UserAccountListRenderer getRenderer() {
            if (m_singleton == null) {
                m_singleton = new UserAccountListRenderer();
            }
            return m_singleton;
        }
        
        private UserAccountListRenderer() {
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list, ((UserAccount) value).getLogin(), index, isSelected, cellHasFocus);
        }
    }

    
}
