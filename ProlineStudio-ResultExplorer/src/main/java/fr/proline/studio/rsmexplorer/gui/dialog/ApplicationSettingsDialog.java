package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import fr.proline.studio.gui.AbstractParameterListTree;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.FileParameter;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.StringParameter;
import fr.proline.studio.rsmexplorer.actions.identification.ImportManager;
import fr.proline.studio.table.DecoratedTable;
import java.awt.*;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;

/**
 * Help Dialog with links to the how to sections
 *
 * @author JM235353
 */
public class ApplicationSettingsDialog extends DefaultDialog implements TreeSelectionListener, TreeWillExpandListener {

    private static ApplicationSettingsDialog m_singletonDialog = null;
    private AbstractParameterListTree m_parameterListTree;
    private ParameterList m_jmsParameterList, m_generalParameterList, m_tablePrameterList, m_wizardParameterList;
    private JPanel m_cards;
    private final Hashtable<String, JPanel> m_existingPanels;
    private final Hashtable<String, ParameterList> m_existingLists;

    private static final String GENERAL_APPLICATION_SETTINGS = "General Application Settings";
    private static final String DIALOG_TITLE = "Proline Studio Settings";
    private static final String TREE_ROOT_NAME = "Settings Categories";

    private FileParameter m_converterFilePath;

    private final Preferences m_preferences;

    public static ApplicationSettingsDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new ApplicationSettingsDialog(parent);
            m_singletonDialog.selectDefault();
        }
        m_singletonDialog.updateSettings();

        return m_singletonDialog;
    }

    public ApplicationSettingsDialog(Window parent) {
        super(parent, Dialog.ModalityType.MODELESS);

        setTitle(DIALOG_TITLE);

        setSize(new Dimension(1024, 480));
        setMinimumSize(new Dimension(1024, 360));
        setResizable(true);

        setDocumentationSuffix("h.eb8nfjv41vkz");

        setButtonVisible(BUTTON_CANCEL, true);
        setButtonName(BUTTON_OK, "OK");
        setStatusVisible(true);

        m_existingPanels = new Hashtable<String, JPanel>();
        m_existingLists = new Hashtable<String, ParameterList>();

        m_preferences = NbPreferences.root();

        setInternalComponent(createInternalComponent());

    }

    private ParameterList getJMSParameterList() {
        m_jmsParameterList = new ParameterList(JMSConnectionManager.JMS_SETTINGS_PARAMLIST_KEY);

        StringParameter serviceRequestQueueName = new StringParameter(JMSConnectionManager.SERVICE_REQUEST_QUEUE_NAME_KEY, "Service Request Queue Name", JTextField.class, JMSConnectionManager.DEFAULT_SERVICE_REQUEST_QUEUE_NAME, 5, null);
        m_jmsParameterList.add(serviceRequestQueueName);

        m_jmsParameterList.loadParameters(m_preferences);

        return m_jmsParameterList;
    }

    private ParameterList getGeneralParameters() {

        m_generalParameterList = new ParameterList(GENERAL_APPLICATION_SETTINGS);

        JCheckBox gettingStartedCheckBox = new JCheckBox("Hide Getting Started Dialog On Startup");
        BooleanParameter gettingStartedParameter = new BooleanParameter("Hide_Getting_Started_Dialog", "Hide Getting Started Dialog On Startup", gettingStartedCheckBox, false);
        m_generalParameterList.add(gettingStartedParameter);

        Object[] namingAssosiatedTable = {"Search Name", "Peaklist", "Msi Search Filename", "Mascot Rule"};
        JComboBox namingComboBox = new JComboBox(namingAssosiatedTable);
        Object[] namingObjectTable = {ImportManager.SEARCH_RESULT_NAME_SOURCE, ImportManager.PEAKLIST_PATH_SOURCE, ImportManager.MSI_SEARCH_FILE_NAME_SOURCE, ImportManager.MASCOT_DAEMON_RULE};
        ObjectParameter nameSourceParameter = new ObjectParameter(ImportManager.DEFAULT_SEARCH_RESULT_NAME_SOURCE_KEY, "Default Search Result Name Source", namingComboBox, namingAssosiatedTable, namingObjectTable, 2, null);
        m_generalParameterList.add(nameSourceParameter);

        JCheckBox decoratedCheckBox = new JCheckBox("Export Decorated");
        BooleanParameter exportDecoratedParameter = new BooleanParameter("Export_Decorated", "Export Decorated", decoratedCheckBox, true);
        m_generalParameterList.add(exportDecoratedParameter);

        JCheckBox xicCheckBox = new JCheckBox("Use dataset type to create Xic Design by DnD");
        BooleanParameter xicTransferHandlerParameter = new BooleanParameter("XIC_Transfer_Handler_Retains_Structure", "XIC Transfer Handler Retains Structure", xicCheckBox, true);
        m_generalParameterList.add(xicTransferHandlerParameter);

        m_generalParameterList.loadParameters(m_preferences);

        return m_generalParameterList;
    }

    private ParameterList getMsFilesParameters() {

        m_wizardParameterList = new ParameterList("Conversion/Upload Settings");

        String[] converterExtentions = {"exe"};
        String[] converterFilterNames = {"raw2mzDB.exe"};
        m_converterFilePath = new FileParameter(null, "Converter_(.exe)", "Converter (.exe)", JTextField.class, "", converterFilterNames, converterExtentions);
        m_converterFilePath.setAllFiles(false);
        m_converterFilePath.setSelectionMode(JFileChooser.FILES_ONLY);
        m_converterFilePath.setDefaultDirectory(new File(m_preferences.get("mzDB_Settings.Converter_(.exe)", System.getProperty("user.home"))));
        m_wizardParameterList.add(m_converterFilePath);

        m_wizardParameterList.loadParameters(m_preferences);

        return m_wizardParameterList;

    }

    private ParameterList getTableParameters() {
        m_tablePrameterList = new ParameterList(DecoratedTable.TABLE_PARAMETERS);

        Object[] associatedTable = {"Automatic Column Size", "Fixed Column Size", "Smart Column Size"};
        JComboBox comboBox = new JComboBox(associatedTable);
        Object[] objectTable = {DecoratedTable.AUTOMATIC_COLUMNS_SIZE, DecoratedTable.FIXED_COLUMNS_SIZE, DecoratedTable.SMART_COLUMNS_SIZE};
        ObjectParameter columnsParameter = new ObjectParameter(DecoratedTable.DEFAULT_COLUMNS_ARRANGEMENT_KEY, DecoratedTable.DEFAULT_COLUMNS_ARRANGEMENT_NAME, comboBox, associatedTable, objectTable, DecoratedTable.DEFAULT_COLUMNS_ARRANGEMENT_INDEX, null);
        m_tablePrameterList.add(columnsParameter);

        IntegerParameter defaultFixedColumnSize = new IntegerParameter(DecoratedTable.DEFAULT_WIDTH_KEY, DecoratedTable.DEFAULT_WIDTH_NAME, JTextField.class, DecoratedTable.COLUMN_DEFAULT_WIDTH, DecoratedTable.COLUMN_MIN_WIDTH, DecoratedTable.COLUMN_MAX_WIDTH);
        m_tablePrameterList.add(defaultFixedColumnSize);

        m_tablePrameterList.loadParameters(m_preferences);

        AbstractLinkedParameters linkedParameters = new AbstractLinkedParameters(m_tablePrameterList) {

            @Override
            public void valueChanged(String value, Object associatedValue) {
                int m_width = Integer.parseInt(defaultFixedColumnSize.getStringValue());
                int m_selection = Integer.parseInt(columnsParameter.getStringValue());
                showParameter(defaultFixedColumnSize, m_selection == DecoratedTable.FIXED_COLUMNS_SIZE || m_selection == DecoratedTable.SMART_COLUMNS_SIZE, m_width);
                updateParameterListPanel();
            }

        };

        columnsParameter.addLinkedParameters(linkedParameters);

        int m_selection = Integer.parseInt(columnsParameter.getStringValue());
        linkedParameters.valueChanged((String) associatedTable[m_selection], objectTable[m_selection]);

        return m_tablePrameterList;
    }

    private JComponent createInternalComponent() {

        JPanel externalPanel = new JPanel();
        externalPanel.setLayout(new GridLayout(1, 1));
        externalPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 1));

        panel.setBorder(BorderFactory.createTitledBorder(DIALOG_TITLE));

        m_parameterListTree = new AbstractParameterListTree(TREE_ROOT_NAME, this, this);
        m_parameterListTree.addNodes(getJMSParameterList());
        m_parameterListTree.addNodes(getMsFilesParameters());
        m_parameterListTree.addNodes(getTableParameters());
        m_parameterListTree.addNodes(getGeneralParameters());
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
        splitPane.setDividerLocation(200);
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
        ParameterError error = checkExistingLists();
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            m_parameterListTree.getTree().setSelectionPath(new TreePath(TREE_ROOT_NAME));
            return false;
        }
        saveExistingsLists();

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
                JPanel newPanel = m_parameterListTree.getList().get(panelKey).getPanel();
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

            if (key.equalsIgnoreCase("Conversion/Upload Settings")) {
                if (m_converterFilePath.getStringValue().length() > 0) {
                    File f = new File(m_converterFilePath.getStringValue());
                    if (!f.exists()) {
                        ParameterError error = new ParameterError("The selected raw2mzDB.exe is not valid.", m_converterFilePath.getComponent());
                        return error;
                    }
                }
            } else {
                ParameterList currentList = m_existingLists.get(key);

                ParameterError currentError = currentList.checkParameters();
                if (currentError != null) {
                    CardLayout cl = (CardLayout) (m_cards.getLayout());
                    cl.show(m_cards, key);

                    return currentError;
                }
            }
        }
        return null;
    }

    private void saveExistingsLists() {
        Enumeration<String> enumKey = m_existingLists.keys();

        while (enumKey.hasMoreElements()) {
            String key = enumKey.nextElement();
            ParameterList currentList = m_existingLists.get(key);
            currentList.saveParameters(NbPreferences.root());
        }
        try {
            NbPreferences.root().flush();
        } catch (BackingStoreException e) {
            LoggerFactory.getLogger("ProlineStudio.DPM").error("Saving Parameters Failed", e);
        }
    }

    private void updateSettings() {
        Enumeration<String> enumKey = m_existingLists.keys();
        while (enumKey.hasMoreElements()) {
            String key = enumKey.nextElement();
            ParameterList currentList = m_existingLists.get(key);
            currentList.loadParameters(NbPreferences.root());
        }
    }

    private void selectDefault() {
        m_parameterListTree.selectNode(GENERAL_APPLICATION_SETTINGS);
    }

    @Override
    public void treeWillExpand(TreeExpansionEvent tee) throws ExpandVetoException {
        ;
    }

    @Override
    public void treeWillCollapse(TreeExpansionEvent tee) throws ExpandVetoException {
        ;
    }

}
