package fr.proline.studio.rsmexplorer.gui.admin;

import fr.proline.core.orm.uds.UserAccount;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.table.DecoratedTable;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.TablePopupMenu;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class UserAccountsPanel extends JPanel {
    
    private JDialog m_dialogOwner = null;
    private Boolean m_isEditable = true;
    
    private UserAccountsTable m_userAccountsTable;
    
    public UserAccountsPanel(JDialog dialog, Boolean editable) {
        m_isEditable = editable;
        m_dialogOwner = dialog;
        
        setBorder(BorderFactory.createTitledBorder("User Accounts"));
        
        setLayout(new java.awt.GridBagLayout());

        JScrollPane tableScrollPane = new JScrollPane();
        m_userAccountsTable = new UserAccountsTable(); 
        tableScrollPane.setViewportView(m_userAccountsTable);
        m_userAccountsTable.setFillsViewportHeight(true);
        
        
        JButton addUserButton = new JButton("Add User Account");
        addUserButton.setIcon(IconManager.getIcon(IconManager.IconType.PLUS_16X16));
        addUserButton.setEnabled(m_isEditable);
        JButton propertiesButton = new JButton("Modify User Account");
        propertiesButton.setIcon(IconManager.getIcon(IconManager.IconType.PROPERTIES));
        propertiesButton.setEnabled(m_isEditable);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(tableScrollPane, c);
        
        
        c.gridy++;
        c.gridwidth = 1;
        c.weighty = 0;
        add(Box.createHorizontalGlue(), c);
        
        c.gridx++;
        c.weightx = 0;
        add(addUserButton, c);
        
        c.gridx++;
        add(propertiesButton, c);
        
        
        addUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UserAccountDialog userAccountDialog = UserAccountDialog.getDialog(m_dialogOwner, UserAccountDialog.DialogMode.CREATE_USER);
                userAccountDialog.setLocationRelativeTo(m_dialogOwner);
                userAccountDialog.setVisible(true);
                if (userAccountDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                        AbstractJMSCallback callback = new AbstractJMSCallback() {

                            @Override
                            public boolean mustBeCalledInAWT() {
                                return false;
                            }

                            @Override
                            public void run(boolean success) {
                                  if (success) {
                                    // reload users
                                    m_userAccountsTable.updateAccounts();
                                } else {
                                    //JPM.TODO
                                }
                            }
                        };

                        fr.proline.studio.dpm.task.jms.CreateUserAccountTask task = new fr.proline.studio.dpm.task.jms.CreateUserAccountTask(callback, userAccountDialog.getLogin(), userAccountDialog.getPassword(), userAccountDialog.isAdmin() );
                        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
                }
                
                

            }
            
        });
        
        propertiesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                int selectedRow = m_userAccountsTable.getSelectedRow();
                if (selectedRow == -1) {
                    return;
                }
                UserAccountDialog userAccountDialog = UserAccountDialog.getDialog(m_dialogOwner, UserAccountDialog.DialogMode.MODIFY_USER);
                userAccountDialog.setLocationRelativeTo(m_dialogOwner);

                UserAccountsTableModel model = (UserAccountsTableModel) m_userAccountsTable.getModel();
                UserAccount user = model.getUserAccount(m_userAccountsTable.convertRowIndexToModel(selectedRow));
                userAccountDialog.setUserAccountInfo(user.getLogin(), DatabaseDataManager.isAdmin(user));
                userAccountDialog.setVisible(true);
                
                if (userAccountDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    
                    if (userAccountDialog.userGroupChanged()) {
                        AbstractJMSCallback callback = new AbstractJMSCallback() {

                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success) {
                                if (success) {
                                    // reload users
                                    m_userAccountsTable.updateAccounts();
                                } else {
                                    //JPM.TODO
                                }
                            }
                        };

                        fr.proline.studio.dpm.task.jms.ChangeUserGroupTask task = new fr.proline.studio.dpm.task.jms.ChangeUserGroupTask(callback, userAccountDialog.getLogin(), user.getId(), userAccountDialog.isAdmin());
                        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
                    }
                    if (userAccountDialog.passwordChanged()) {
                        AbstractJMSCallback callback = new AbstractJMSCallback() {

                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success) {
                                if (success) {
                                    // reload users
                                    m_userAccountsTable.updateAccounts();
                                } else {
                                    //JPM.TODO
                                }
                            }
                        };

                        fr.proline.studio.dpm.task.jms.ResetPasswordTask task = new fr.proline.studio.dpm.task.jms.ResetPasswordTask(callback, userAccountDialog.getLogin(), user.getId(), userAccountDialog.getPassword());
                        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
                    }
                    

                }
            }
            
        });
        
    }
    
    public class UserAccountsTable extends DecoratedTable {

        public UserAccountsTable() {
            
            initAccounts();
        }
        
        private void initAccounts() {
            UserAccount[]  accounts = DatabaseDataManager.getDatabaseDataManager().getProjectUsersArray();
            UserAccountsTableModel tableModel = new UserAccountsTableModel(accounts);

            setModel(tableModel);
            
            setRowSelectionInterval(0, 0);
        }
        
        public void updateAccounts() {
            
            int selectedRow = getSelectedRow();
            
            UserAccountsTableModel tableModel = (UserAccountsTableModel) getModel();
            
            long previouslySelectedId = -1;
            if (selectedRow != -1) {
                selectedRow = convertRowIndexToModel(selectedRow);
                UserAccount userAccount = tableModel.getUserAccount(selectedRow);
                previouslySelectedId = userAccount.getId();
            }
            
            
            UserAccount[]  accounts = DatabaseDataManager.getDatabaseDataManager().getProjectUsersArray();
            tableModel.setAccounts(accounts);
            
            if (previouslySelectedId != -1) {
                int row = tableModel.findUserAccount(previouslySelectedId);
                if (row != -1) {
                    row = convertRowIndexToView(row);
                    setRowSelectionInterval(row, row);
                }
                
            }
            
            
        }
        
        @Override
        public void valueChanged(ListSelectionEvent e) {

            super.valueChanged(e);
        }
        
        @Override
        public TablePopupMenu initPopupMenu() {
            return null;
        }

        @Override
        public void prepostPopupMenu() {
        }
        
    }
    
    public class UserAccountsTableModel extends DecoratedTableModel {

        private ArrayList<UserAccount> m_userAccountArray = null;
        
        public static final int COLTYPE_NAME = 0;
        public static final int COLTYPE_GROUP = 1;
        
        private final String[] m_columnNames = {"User", "Group"};

        public UserAccountsTableModel(UserAccount[]  userAccountArray) {
            m_userAccountArray = new ArrayList<>(userAccountArray.length);
            for (UserAccount user : userAccountArray) {
                m_userAccountArray.add(user);
            }
            
        }
        
        public int findUserAccount(long id) {

            for (int row=0;row<m_userAccountArray.size();row++) {
                UserAccount userAccount = m_userAccountArray.get(row);
                if (userAccount.getId() == id) {
                    return row;
                }
            }
            return -1;
        }
        
        public void setAccounts(UserAccount[]  userAccountArray) {
            m_userAccountArray.clear();
            for (UserAccount user : userAccountArray) {
                m_userAccountArray.add(user);
            }
            fireTableDataChanged();
        }
        
        public UserAccount getUserAccount(int row) {
            if (m_userAccountArray == null) {
                return null;
            }
            return m_userAccountArray.get(row);
        }
        
        
        @Override
        public String getColumnName(int col) {
            return m_columnNames[col];
        }
        
        @Override
        public Class getColumnClass(int col) {
            return String.class;
        }
        
        @Override
        public int getRowCount() {
            if (m_userAccountArray == null) {
                return 0;
            }
            return m_userAccountArray.size();
        }

        @Override
        public int getColumnCount() {
            return m_columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            
            UserAccount user = m_userAccountArray.get(rowIndex);
            
            switch (columnIndex) {
                case COLTYPE_NAME: {
                    return user.getLogin();
                }
                case COLTYPE_GROUP: {
                    return DatabaseDataManager.isAdmin(user) ? "Admin" : "User";
                }
            }
            return null;
        }

        @Override
        public String getToolTipForHeader(int col) {
            return null;
        }

        @Override
        public String getTootlTipValue(int row, int col) {
            return null;
        }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }
        
        TableCellRenderer renderer = null;
        
        switch (col) {

            case COLTYPE_NAME: {
                renderer =  new UserAccountRenderer();
                break;
            }
            case COLTYPE_GROUP: {
                renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
                break;
            }

        }

        m_rendererMap.put(col, renderer);
        return renderer;
        

    }
    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();
        
    }
    
    
    public class UserAccountRenderer extends DefaultTableCellRenderer {

        public UserAccountRenderer() {
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            UserAccount userAccount = (UserAccount) value;

           
            
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(JLabel.LEFT);

            if (userAccount == null) {
                label.setIcon(null);
                label.setText("");
                return label;
            } else {
                label.setText(userAccount.getLogin());
                
                boolean isAdmin = DatabaseDataManager.isAdmin(userAccount);
                
                
                label.setIcon(IconManager.getIcon(isAdmin ? IconManager.IconType.USER_ADMIN : IconManager.IconType.USER));
                //userAccount.get
            }

            return label;

        }

    }
    

}
