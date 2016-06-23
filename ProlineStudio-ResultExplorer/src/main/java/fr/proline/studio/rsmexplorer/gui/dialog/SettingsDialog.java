package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import fr.proline.studio.gui.AbstractParameterListTree;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.StringParameter;
import fr.proline.studio.rsmexplorer.actions.identification.ImportManager;
import java.awt.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.openide.util.NbPreferences;

/**
 * Help Dialog with links to the how to sections
 *
 * @author JM235353
 */
public class SettingsDialog extends DefaultDialog implements TreeSelectionListener {

    private static SettingsDialog m_singletonDialog = null;
    private AbstractParameterListTree m_parameterListTree;
    private ParameterList m_jmsParameterList, m_generalParameterList, m_securityParameterList;
    private JPanel m_cards;
    private Hashtable<String, JPanel> m_existingPanels;
    private Hashtable<String, ParameterList> m_existingLists;

    private static final String JMS_SETTINGS = "JMS Settings";
    private static final String GENERAL_SETTINGS = "General Settings";
    private static final String SECURITY_SETTINGS = "Security Settings";
    private static final String DIALOG_TITLE = "Proline Studio Settings";
    private static final String TREE_ROOT_NAME = "Settings Categories";

    private Preferences preferences;

    public static SettingsDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new SettingsDialog(parent);
        }

        return m_singletonDialog;
    }

    public SettingsDialog(Window parent) {
        super(parent, Dialog.ModalityType.MODELESS);

        setTitle(DIALOG_TITLE);

        setSize(new Dimension(640, 480));
        this.setMinimumSize(new Dimension(480, 360));
        setResizable(true);

        setButtonVisible(BUTTON_HELP, false);
        setButtonVisible(BUTTON_CANCEL, true);
        setButtonName(BUTTON_OK, "OK");
        setStatusVisible(true);

        m_existingPanels = new Hashtable<String, JPanel>();
        m_existingLists = new Hashtable<String, ParameterList>();

        preferences = NbPreferences.root();

        setInternalComponent(this.createInternalComponent());

    }

    private ParameterList getJMSParameterList() {
        m_jmsParameterList = new ParameterList(JMS_SETTINGS);

        StringParameter defaultServiceRequestQueueName = new StringParameter(JMSConnectionManager.DEFAULT_SERVICE_REQUEST_QUEUE_NAME, "JMS Request Queue Name", JTextField.class, JMSConnectionManager.DEFAULT_SERVICE_REQUEST_QUEUE_NAME, 5, null);
        m_jmsParameterList.add(defaultServiceRequestQueueName);

        return m_jmsParameterList;
    }

    private ParameterList getGeneralParameters() {
        m_generalParameterList = new ParameterList(GENERAL_SETTINGS);

        BooleanParameter gettingStartedParameter = new BooleanParameter("Hide_Getting_Started_Dialog", "Hide Getting Started Dialog On Startup", JCheckBox.class, preferences.getBoolean("Hide_Getting_Started_Dialog", Boolean.FALSE));
        m_generalParameterList.add(gettingStartedParameter);
             
        Object[] objectTable = {ImportManager.SEARCH_RESULT_NAME_SOURCE, ImportManager.PEAKLIST_PATH_SOURCE, ImportManager.MSI_SEARCH_FILE_NAME_SOURCE};             
        ObjectParameter nameSourceParameter = new ObjectParameter(ImportManager.DEFAULT_SEARCH_RESULT_NAME_SOURCE_KEY, "Imported data name based on", objectTable, 2, null);
        m_generalParameterList.add(nameSourceParameter);
        

        return m_generalParameterList;
    }

    private ParameterList getSecurityParameters() {
        m_securityParameterList = new ParameterList(SECURITY_SETTINGS);
        BooleanParameter rememberPasswordParameter = new BooleanParameter("Remember_Password", "Remember Password", JCheckBox.class, preferences.getBoolean("Remember_Password", Boolean.FALSE));
        m_securityParameterList.add(rememberPasswordParameter);
        return m_securityParameterList;
    }

    private JComponent createInternalComponent() {
        JPanel externalPanel = new JPanel();
        externalPanel.setLayout(new GridLayout(1, 1));
        externalPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 1));

        panel.setBorder(BorderFactory.createTitledBorder(DIALOG_TITLE));

        m_parameterListTree = new AbstractParameterListTree(TREE_ROOT_NAME, this);
        m_parameterListTree.addNodes(this.getJMSParameterList());
        m_parameterListTree.addNodes(this.getGeneralParameters());
        //m_parameterListTree.addNodes(this.getSecurityParameters());
        m_parameterListTree.expandAllRows();

        JScrollPane scrollPane = new JScrollPane(m_parameterListTree.getTree());

        m_cards = new JPanel();
        m_cards.setBorder(BorderFactory.createTitledBorder("Parameters"));
        m_cards.setLayout(new CardLayout());

        JPanel rootPanel = new JPanel();
        m_cards.add(rootPanel, TREE_ROOT_NAME);
        CardLayout cardLayout = (CardLayout) (m_cards.getLayout());
        cardLayout.show(m_cards, TREE_ROOT_NAME);
        m_existingPanels.put(TREE_ROOT_NAME, rootPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, m_cards);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(150);
        splitPane.setOneTouchExpandable(false);

        Dimension minimumSize = new Dimension(200, 100);
        scrollPane.setMinimumSize(minimumSize);
        m_cards.setMinimumSize(minimumSize);

        panel.add(splitPane);
        externalPanel.add(panel);

        return externalPanel;
    }

    @Override
    public void pack() {
        // forbid pack by overloading the method
    }

    @Override
    protected boolean okCalled() {
        ParameterError error = this.checkExistingLists();
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            m_parameterListTree.getTree().setSelectionPath(new TreePath(TREE_ROOT_NAME));
            return false;
        }
        this.saveExistingsLists();
        return true;

    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

    @Override
    public void valueChanged(TreeSelectionEvent tse) {
        if (tse.getSource() == m_parameterListTree.getTree()) {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) m_parameterListTree.getTree().getLastSelectedPathComponent();
            String panelKey = selectedNode.getUserObject().toString();

            if (m_existingPanels.containsKey(panelKey)) {
                CardLayout cardLayout = (CardLayout) (m_cards.getLayout());
                cardLayout.show(m_cards, panelKey);
            } else {
                JPanel newPanel = m_parameterListTree.getList().get(panelKey).getPanel(false);
                m_cards.add(newPanel, panelKey);
                CardLayout cardLayout = (CardLayout) (m_cards.getLayout());
                cardLayout.show(m_cards, panelKey);
                m_existingPanels.put(panelKey, newPanel);
                m_existingLists.put(panelKey, m_parameterListTree.getList().get(panelKey));
            }
        }
    }

    private ParameterError checkExistingLists() {
        Enumeration<String> enumKey = m_existingLists.keys();
        while (enumKey.hasMoreElements()) {
            String key = enumKey.nextElement();
            ParameterList currentList = m_existingLists.get(key);

            ParameterError currentError = currentList.checkParameters();
            if (currentError != null) {
                CardLayout cl = (CardLayout) (m_cards.getLayout());
                cl.show(m_cards, key);

                return currentError;
            }
        }
        return null;
    }

    private void saveExistingsLists() {
        Enumeration<String> enumKey = m_existingLists.keys();

        int x = 0;

        while (enumKey.hasMoreElements()) {
            
            String key = enumKey.nextElement();

            ParameterList currentList = m_existingLists.get(key);

            currentList.saveParameters(NbPreferences.root(), false);

        }

    }

}
