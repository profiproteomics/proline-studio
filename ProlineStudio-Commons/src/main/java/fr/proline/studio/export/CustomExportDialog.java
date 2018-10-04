package fr.proline.studio.export;

import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.proline.studio.export.model.CustomExportTableModel;
import fr.proline.studio.gui.CollapsablePanel;
import fr.proline.studio.gui.CollapseListener;
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.settings.FilePreferences;
import org.openide.windows.WindowManager;

/**
 *
 * @author AW
 *
 * In order to achieve custom export, we first load a default structure
 * defautConfig. We then load a custom file (if any) and display the saved info
 * first then add the remaining missing tabs and fields that were in the
 * default. Only both default and custom parameters are displayed, if any old or
 * newly added fields or tabs are present in a custom file, they will be
 * ignored. The default is the reference for allowing data to be displayed.
 */
public class CustomExportDialog extends DefaultDialog implements CollapseListener {

    private static final long serialVersionUID = 1L;

    private final static Logger logger = LoggerFactory.getLogger("ProlineStudio.Commons");

    private final static String EXPORT_PROTEIN_ALL = "All";
    private final static String EXPORT_PROTEIN_VALIDATED = "Validated only";
    private final static String[] EXPORT_PROTEIN_VALUES = new String[]{EXPORT_PROTEIN_VALIDATED, EXPORT_PROTEIN_ALL};

    private static final String JSON_EXTENSION = "json";
    private static final String TSV_EXTENSION = "tsv";

    private static final FileNameExtensionFilter FILTER_EXCEL = new FileNameExtensionFilter("Excel File (.xlsx)", "xlsx");
    private static final FileNameExtensionFilter FILTER_TSV = new FileNameExtensionFilter("Tabulation Separated Values (.tsv)", "tsv");

    private static final String DEFAULT_SERVER_CONFIG_KEY = "DEFAULT_SERVER_CONFIG";
    private static final String CURRENT_CONFIG_KEY = "CURRENT_CONFIG";

    private static final String ORIENTATION_ROW = "rows";
    private static final String ORIENTATION_COLUMNS = "columns";
    static String EXPORT_ALL_FIELD_LABEL = "Select/Unselect all fields";
    static String ORIENTATION_LABEL  = "Orientation:";
    
    private static CustomExportDialog m_singletonDialog = null;

    private JTextField m_fileTextField;
    private JComboBox m_exporTypeCombobox;

    // true if the user has to choose a file, false if it's a directory, in case of tsv or multi export
    private static boolean m_fileExportMode;

    private final JFileChooser m_fchooser;
    private final JFileChooser m_exportFchooser;

    private ProgressTask m_task = null;

    // created by AW:
    private JTabbedPane m_tabbedPane;

    private String m_configFile = "";

    //---
    private JComboBox comboBox_ProteinSets;
    private JComboBox comboBox_DateFormat;

    private JComboBox comboBox_NumberSeparator;
   
    public ExportConfig m_exportConfig;
    private ExportConfig m_exportDefaultConfig;

    protected boolean m_updateInProgress = true; // indicate when the table is built (to avoid calling event handler on every table update)
    public HashMap<String, String> m_tabTitleIdHashMap; // <title,id> keeps track of id/title for tabs, in case of renaming.
    
    private JPanel m_optionPanel; //for date format, protein sets,Number separator,export profile

    private JComboBox comboBox_exportProfile;

    /*private String m_previousConfigStr = null;
     private String m_defaultConfigStr = null;*/
    private HashMap<String, String> m_configServerKey2configMap = new HashMap<>();
    private String m_currentServerConfigStr = null;
    private String m_previousServerConfigStr = null;

    public static CustomExportDialog getDialog(Window parent, boolean fileExportMode) {
        m_fileExportMode = fileExportMode;
        if (m_singletonDialog == null) {
            m_singletonDialog = new CustomExportDialog(parent, fileExportMode);
        }
        return m_singletonDialog;
    }

    public void setTask(DefaultDialog.ProgressTask task) {
        m_task = task;
    }

    private CustomExportDialog(Window parent, boolean fileExportMode) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setButtonVisible(BUTTON_LOAD, true);
        setButtonVisible(BUTTON_SAVE, true);
        setButtonEnabled(BUTTON_SAVE, false);

        setResizable(true);

        m_fileExportMode = fileExportMode;

        setTitle("Export");

        setButtonVisible(BUTTON_HELP, true);

        setDocumentationSuffix("id.37m2jsg");

        Preferences preferences = NbPreferences.root();
        String defaultExportPath = preferences.get("DefaultExcelExportPath", System.getProperty("user.home"));

        String path = preferences.absolutePath();

        File f = new File(path);
        path = f.getAbsolutePath();

        //m_lastDefaultExportConfig =  preferences.get("DefaultExportConfig", null);
        //m_lastExportConfig =  preferences.get("ExportConfig", null);
        m_tabTitleIdHashMap = new HashMap<>(); // this is used to store tab id/tab title matching
        /**
         * @kx here: createCustomExportPanel
         */
        setInternalComponent(createCustomExportPanel(defaultExportPath));

        setButtonName(BUTTON_OK, "Export");

        m_fchooser = new JFileChooser(new File(defaultExportPath));
        m_exportFchooser = new JFileChooser(new File(defaultExportPath));

        m_fchooser.addChoosableFileFilter(FILTER_EXCEL);
        m_fchooser.addChoosableFileFilter(FILTER_TSV);

        m_fchooser.setMultiSelectionEnabled(false);
        m_exportFchooser.setMultiSelectionEnabled(false);
        FileNameExtensionFilter filterJson = new FileNameExtensionFilter("Custom Export Config (." + JSON_EXTENSION + ")", JSON_EXTENSION);
        m_exportFchooser.setFileFilter(filterJson);

    }

    public boolean isFileExportMode() {
        return m_fileExportMode;
    }

    private boolean isTsv() {
        return m_exporTypeCombobox.getSelectedItem() != null && m_exporTypeCombobox.getSelectedItem().toString().contains(TSV_EXTENSION);
    }

    private void loadExportConfig() {
        // decode json 
        m_exportConfig = new ExportConfig();
        String jsonString = "";

        String path = m_configFile.trim();
        Path filePath = Paths.get(m_configFile.trim());

        FilePreferences filePreferences = new FilePreferences(new File(path), null, "");
        String fileDefaultServerConfig = filePreferences.get(DEFAULT_SERVER_CONFIG_KEY, null);

        boolean showWarning = false;

        if (fileDefaultServerConfig == null) {
            // old file format : json config directly saved in file

            try {
                jsonString = new String(Files.readAllBytes(filePath));
            } catch (IOException e) {

                logger.error("Error while loading config " + e);
            }

            showWarning = true;
        } else {
            jsonString = filePreferences.get(CURRENT_CONFIG_KEY, null);

            showWarning = (fileDefaultServerConfig.compareTo(m_currentServerConfigStr) != 0);
        }

        if (!filePath.toString().equals("")) {
            Gson gson = new Gson();
            String messageHashMapJsonString = jsonString;
            m_exportConfig = gson.fromJson(messageHashMapJsonString, m_exportConfig.getClass());
        }

        if (showWarning) {
            InfoDialog errorDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "Warning", "The version of the Export Settings file does not correspond. It could lead to an error during the export.");
            errorDialog.setButtonVisible(InfoDialog.BUTTON_CANCEL, false);
            errorDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
            errorDialog.setVisible(true);
        }
    }

    private void fillExportPossibleValues(ExportConfig param) {
        if (param != null) {
            if (param.format_values != null) {
                String[] reformatedParamValues = new String[param.format_values.length];
                for (int i = 0; i < param.format_values.length; i++) {
                    if (param.format_values[i].contains("xls")) {
                        reformatedParamValues[i] = "Excel (." + param.format_values[i] + ")";
                    } else if (param.format_values[i].contains("tsv")) {
                        reformatedParamValues[i] = "Tabulation separated values (." + param.format_values[i] + ")";
                    } else if (param.format_values[i].contains("csv")) {
                        reformatedParamValues[i] = "Comma separated values (." + param.format_values[i] + ")";
                    }
                }
                m_exporTypeCombobox.setModel(new DefaultComboBoxModel(reformatedParamValues));
            }

            if (param.date_format_values != null) {
                comboBox_DateFormat.setModel(new DefaultComboBoxModel(param.date_format_values));
            }

            if (param.decimal_separator_values != null) {
                comboBox_NumberSeparator.setModel(new DefaultComboBoxModel(param.decimal_separator_values));
            }

            comboBox_ProteinSets.setModel(new DefaultComboBoxModel(new String[]{"All", "Validated only"}));
            if (param.data_export.all_protein_set) {
                comboBox_ProteinSets.setSelectedIndex(0);
            } else {
                comboBox_ProteinSets.setSelectedIndex(1);
            }
            comboBox_exportProfile.setModel(new DefaultComboBoxModel(new String[]{"Best", "All"}));
            if (param.data_export.best_profile) {

                comboBox_exportProfile.setSelectedIndex(0);
            } else {

                comboBox_exportProfile.setSelectedIndex(1);
            }

        }

    }
    /**
     * load value from config parametre to comboBox_DateFormat,comboBox_NumberSeparator,comboBox_ProteinSets,comboBox_exportProfile
     * @param param 
     */
    private void selectLoadedExportValues(ExportConfig param) {
        if (param != null) {

            if (param.date_format != null) {
                comboBox_DateFormat.setSelectedItem(param.date_format);
            }

            if (param.decimal_separator != null) {
                comboBox_NumberSeparator.setSelectedItem(param.decimal_separator);
            }

            if (param.data_export.all_protein_set) {
                comboBox_ProteinSets.setSelectedItem(EXPORT_PROTEIN_ALL);
            } else {
                comboBox_ProteinSets.setSelectedItem(EXPORT_PROTEIN_VALIDATED);
            }
            if (param.data_export.best_profile) {
                comboBox_exportProfile.setSelectedIndex(0);
            } else {
                comboBox_exportProfile.setSelectedIndex(1);
            }

        }

    }

    /**
     * For each sheet, create the tab panel. <br>
     * Fill the field name id, it's display field name, and its export selection(yes/non)
     * @param defaultParam
     * @param param, user loaded parameter file, only selected sheet parameter can be in it
     */
    private void fillExportFormatTable(ExportConfig defaultParam, ExportConfig param) {
        //reset panes:
        m_updateInProgress = true;
        m_tabbedPane.removeAll();

        // get list of sheets from defaut, make the Hashmap title-id, but no tab panel is created
        m_tabTitleIdHashMap.clear();
        for (int i = 0; i < defaultParam.sheets.length; i++) {
            m_tabTitleIdHashMap.put(defaultParam.sheets[i].title, defaultParam.sheets[i].id);
        }
        ArrayList<String> addedTabs = new ArrayList<>(); // tabs added (which are in default and custom)
        // Begin from the param, if it is also exist in default param, create the tab
        // create tab panes :createSheetTab
        // then create CustomExportTableModel for this table
        // then fill each row (field)
        if (param != null) {
            for (int i = 0; i < param.sheets.length; i++) {
                ExportExcelSheet sheet = param.sheets[i];
                
                if (m_tabTitleIdHashMap.containsValue(sheet.id)) {
                    
                    addedTabs.add(sheet.id);
                   
                    // read fields to fill in jtable into this tabbed pane
                    JTable table = createSheetTab(sheet, true); //this sheet must be selected
                    CustomExportTableModel tableModel = new CustomExportTableModel();
                    // now add the fields
                    // add fields contained both in param and defaultparam
                    ArrayList<String> defaultFieldsList = getFieldsFromParamSheet(defaultParam.sheets, sheet.id);
                    ArrayList<String> addedFieldsList = new ArrayList<>();// to know which fields have already been added

                    for (int j = 0; j < sheet.fields.length; j++) {
                        // if the field to add is contained in default field list
                        ExportExcelSheetField field = sheet.fields[j];
                        if (defaultFieldsList.contains(field.id)) {
                            tableModel.addRow(field, Boolean.TRUE);//the selected row(field) is added at the top
                            addedFieldsList.add(field.id);
                        }
                        //if not, dont add it
                    }
                    // now add the remaining default fields not already added from custom config
                    int sheetIndexInDefaultConfig = defaultParam.getIndexOfSheet(sheet.id);// find the right sheet in default config
                    ExportExcelSheetField[] allFields = defaultParam.sheets[sheetIndexInDefaultConfig].fields;
                    for (int j = 0; j < allFields.length; j++) {
                        ExportExcelSheetField field = allFields[j];
                        if (!addedFieldsList.contains(field.id)) {
                            // add the remaining fields to add
                            tableModel.addRow(field, Boolean.FALSE);//the no selected row(field) is added
                        }
                    }
                    table.setModel(tableModel);
                } else {
                    // if not in default, do not add it!
                }

            }
        }

        // now add the remaining default sheets that are not already added.
        for (int i = 0; i < defaultParam.sheets.length; i++) {
            ExportExcelSheet sheet = defaultParam.sheets[i];

            if (!addedTabs.contains(sheet.id)) {
               boolean enabled;
                if (param != null) { //if custom param file is loaded
                    enabled = false; // disable default not saved tab
                } else {
                    enabled = sheet.default_displayed;
                }
                // add the missing tab
                 // read fields to fill in jtable into this tabbed pane
                JTable table = createSheetTab(sheet, enabled);
 
                CustomExportTableModel tableModel = new CustomExportTableModel();
                for (int j = 0; j < sheet.fields.length; j++) {
                    ExportExcelSheetField field = sheet.fields[j];
                    tableModel.addRow(field);
                }
                table.setModel(tableModel);
            }

        }
        m_updateInProgress = false;
    }


    /**
     * from the parameter sheet, get all the fields of a sheet, on sheet is
     * represented by one tab in the Custom Options
     *
     * @param sheets
     * @param sheetId
     * @return
     */
    private ArrayList<String> getFieldsFromParamSheet(ExportExcelSheet[] sheets, String sheetId) {
        ArrayList<String> fieldsId = new ArrayList<>();
        for (int i = 0; i < sheets.length; i++) {
            if (sheets[i].id.equals(sheetId)) {

                for (int j = 0; j < sheets[i].fields.length; j++) {
                    fieldsId.add(sheets[i].fields[j].id);
                }
            }
        }

        return (fieldsId);
    }

    /**
     * also listen to File button, export file and type
     *
     * @param defaultExportPath
     * @return
     */
    public final JPanel createCustomExportPanel(String defaultExportPath) {

        final JPanel insidePanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        JLabel lblExportToFile = new JLabel("Export to file:");
        insidePanel.add(lblExportToFile, c);

        c.gridx++;
        c.weightx = 1;
        m_fileTextField = new JTextField(50);
        m_fileTextField.setText(defaultExportPath);
        insidePanel.add(m_fileTextField, c);
        c.weightx = 0;

        final JButton addFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
        addFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (m_exporTypeCombobox.getSelectedItem() != null) {

                    if (m_fileExportMode) {
                        // file mode
                        m_fchooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        if (m_exporTypeCombobox.getSelectedItem().toString().contains("xls")) {
                            m_fchooser.setFileFilter(FILTER_EXCEL);
                        } else {
                            m_fchooser.setFileFilter(FILTER_TSV);
                        }
                    } else {
                        // directory
                        m_fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    }
                }

                String textFile = m_fileTextField.getText().trim();
                if (textFile.length() > 0) {
                    File currentFile = new File(textFile);
                    if (currentFile.isDirectory()) {
                        m_fchooser.setCurrentDirectory(currentFile);
                    } else if (m_fileExportMode) {
                        m_fchooser.setSelectedFile(currentFile);
                    } else {
                        m_fchooser.setCurrentDirectory(currentFile);
                    }
                }

                int result = m_fchooser.showOpenDialog(addFileButton);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = m_fchooser.getSelectedFile();

                    String absolutePath = file.getAbsolutePath();
                    String fileName = file.getName();
                    if (m_fileExportMode) {
                        if (fileName.indexOf('.') == -1) {
                            if (isTsv()) {
                                absolutePath += "." + TSV_EXTENSION;
                            } else {
                                absolutePath += "." + "xlsx"; //+ exporterInfo.getFileExtension();
                            }
                        }
                    } else if (!fileName.endsWith("\\")) {
                        absolutePath += "\\";
                    }
                    m_fileTextField.setText(absolutePath);
                }
            }
        });
        c.gridx++;
        insidePanel.add(addFileButton, c);

        JLabel lbl_exportType = new JLabel("Export Type:");
        m_exporTypeCombobox = new JComboBox();
        m_exporTypeCombobox.setModel(new DefaultComboBoxModel(new String[]{"xlsx", "xls", "csv", "tsv"}));
        c.gridx = 0;
        c.gridy++;
        insidePanel.add(lbl_exportType, c);

        c.gridx++;
        c.weightx = 1;
        insidePanel.add(m_exporTypeCombobox, c);
        c.weightx = 0;
        //@kx create optionPanel, #########important panel
        m_optionPanel = createOptionPanel();
        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy++;
        c.weighty = 1;
        c.weightx = 1;
        //creat CollapsablePanel custom options
        CollapsablePanel collapsablePanel = new CollapsablePanel("Custom Options", m_optionPanel, false);
        insidePanel.add(collapsablePanel, c);

        collapsablePanel.addCollapseListener(this);

        return insidePanel;

    }

    /**
     * optionPanel, add 4 commboBox, without listen, Date format,Protein
     * sets,Number separator,Export profile,
     *
     * call createTabbedOptionPanel
     *
     * @return
     */
    private JPanel createOptionPanel() {
        JPanel optionPanel = new JPanel(new GridBagLayout());
        optionPanel.setVisible(false);
        optionPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel lblDateFormat = new JLabel("Date format:");
        comboBox_DateFormat = new JComboBox();
        comboBox_DateFormat.setModel(new DefaultComboBoxModel(new String[]{"yyyyMMdd HH:mm:ss", "ddMMyyyy HH:mm:ss", "MMddyyyy HH:mm:ss"}));

        JLabel lblProteinSets = new JLabel("Protein sets:");
        comboBox_ProteinSets = new JComboBox();
        comboBox_ProteinSets.setModel(new DefaultComboBoxModel(EXPORT_PROTEIN_VALUES));

        JLabel lblNumberSeparator = new JLabel("Number separator:");
        comboBox_NumberSeparator = new JComboBox();
        comboBox_NumberSeparator.setModel(new DefaultComboBoxModel(new String[]{".", ","}));

        JLabel lblExportProfile = new JLabel("Export profile:");
        comboBox_exportProfile = new JComboBox();
        comboBox_exportProfile.setModel(new DefaultComboBoxModel(new String[]{"Best", "All"}));

        c.gridx = 0;
        c.gridy = 0;
        optionPanel.add(lblDateFormat, c);

        c.gridx++;
        optionPanel.add(comboBox_DateFormat, c);

        c.gridx++;
        optionPanel.add(lblProteinSets, c);

        c.gridx++;
        optionPanel.add(comboBox_ProteinSets, c);

        c.gridx = 0;
        c.gridy++;
        optionPanel.add(lblNumberSeparator, c);

        c.gridx++;
        optionPanel.add(comboBox_NumberSeparator, c);

        c.gridx++;
        optionPanel.add(lblExportProfile, c);

        c.gridx++;
        optionPanel.add(comboBox_exportProfile, c);

        c.gridwidth = 4;
        c.gridx = 0;
        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        //add createTabbedOptionPanel
        //optionPanel.add(createTabbedOptionPanel(), c);
        m_tabbedPane = createTabbedPane();
        optionPanel.add(m_tabbedPane, c);
        return optionPanel;
    }

    /**
     * @return
     */
    private JTabbedPane createTabbedPane() {

        final DnDTabbedPane tabbedPane = new DnDTabbedPane(JTabbedPane.TOP);
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {

                updatePresentationModeForNewlySelectedTab();

            }
        });

        // add listener to allow tab rename:
        TabTitleEditListener l = new TabTitleEditListener(tabbedPane, this);
        tabbedPane.addMouseListener(l);

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return tabbedPane;
    }

    @Override
    protected boolean saveCalled() {
        saveConfigFile();
        return false;
    }

    @Override
    protected boolean loadCalled() {

        boolean loaded = loadConfigFile();

        // extend option panel if needed
        if (loaded && !m_optionPanel.isVisible()) {
            collapse(false);
        }

        return false;
    }

    /**
     * update m_tabTitleIdHashMap,
     */
    protected void recalculateTabTitleIdHashMap() {
       
        //because after renamed, rebuild it in order to keep tabs ids stored .
        m_tabTitleIdHashMap.clear();
        for (int i = 0; i < m_tabbedPane.getTabCount(); i++) {

            Component comp = m_tabbedPane.getTabComponentAt(i);
            if ((comp == null) || !(comp instanceof CheckboxTabPanel)) {
                continue; // editing mode
            }

            CheckboxTabPanel c = ((CheckboxTabPanel) m_tabbedPane.getTabComponentAt(i));

            m_tabTitleIdHashMap.put(c.getText(), ((CheckboxTabPanel) m_tabbedPane.getTabComponentAt(i)).getSheetId());
        }

    }

    protected void recalculateTabsIds() {
        //because drag n drop looses tooltiptext info, rebuild it in order to keep tabs ids stored there.

        //RECALCULTAING tab ids 
        // 1st: get the list of ids from defaultParam
        // 2nd: find which one is missing from list
        // 3: add the missing one to tooltiptext.
        if (m_exportDefaultConfig == null) {
            return;
        }
        if (m_exportDefaultConfig.sheets.length == 0) {
            return;
        }
        // 1st
        ArrayList<String> idFullList = new ArrayList<>();
        for (int i = 0; i < m_exportDefaultConfig.sheets.length; i++) {
            idFullList.add(m_exportDefaultConfig.sheets[i].id);
        }

        int removedAtIndex = -1;
        for (int i = 0; i < m_tabbedPane.getTabCount(); i++) {

            Component comp = m_tabbedPane.getTabComponentAt(i);
            if (!(comp instanceof CheckboxTabPanel)) {
                return; // editing mode
            }

            CheckboxTabPanel c = (CheckboxTabPanel) comp;
            String sheetId = (c == null) ? null : c.getSheetId();

            if (sheetId == null) { // if tool tip has been erased
                removedAtIndex = i;

            } else {
                idFullList.remove(sheetId);
            }
        }
        if (removedAtIndex > -1) {
            if (idFullList.size() > 1) {
                logger.warn("Problem: more than one missing ID");
            } else if (idFullList.size() == 1) {
                //logger.warn("Fixed the missing id: " + idFullList.get(0));
                CheckboxTabPanel cRemoved = (CheckboxTabPanel) m_tabbedPane.getTabComponentAt(removedAtIndex);
                if (cRemoved != null) {
                    cRemoved.setSheetId(cRemoved.getSheetId());
                }
            }
        }

    }
    /**
     * When the load button is called, load the given value from m_configFile 
     * @return 
     */
    protected boolean loadConfigFile() {

        //-------
        String configFile = m_configFile.trim();

        if (configFile.length() > 0) {
            File currentFile = new File(configFile);
            if (currentFile.isDirectory()) {
                m_exportFchooser.setCurrentDirectory(currentFile);
            } else {
                m_exportFchooser.setSelectedFile(currentFile);
            }
        }

        int result = m_exportFchooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = m_exportFchooser.getSelectedFile();

            String absolutePath = file.getAbsolutePath();

            m_configFile = absolutePath;
            loadExportConfig();
            if (m_exportDefaultConfig != null) {
                // reorder param to contain all fields...

                fillExportFormatTable(m_exportDefaultConfig, m_exportConfig);

                updatePresentationModeForNewlySelectedTab();
                selectLoadedExportValues(m_exportConfig);

                //m_exportConfig = m_exportDefaultConfig; // allows all fields to be present in m_exportConfig, in case some tabs were disabled and avoid problems in processing...
            } else {

            }
            return true;
        }

        return false;
    }

    /**
     * recalcultaeTabIds, recalculateTabTitleIdHashMap and update the selected
     * row/column comboBox_Orientation
     */
    protected void updatePresentationModeForNewlySelectedTab() {

        if (!m_updateInProgress) { // update only when no update in progress
            m_updateInProgress = true;

            recalculateTabsIds();
            recalculateTabTitleIdHashMap();
            m_updateInProgress = false;

        }

    }

    protected void saveConfigFile() {
        String configFile = m_configFile.trim();

        if (configFile.length() > 0) {
            File currentFile = new File(configFile);
            if (currentFile.isDirectory()) {
                m_exportFchooser.setCurrentDirectory(currentFile);
            } else {
                m_exportFchooser.setSelectedFile(currentFile);
            }

        }

        int result = m_exportFchooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = m_exportFchooser.getSelectedFile();

            String absolutePath = file.getAbsolutePath();
            // add json if needed
            if (!absolutePath.endsWith("." + JSON_EXTENSION)) {
                absolutePath += "." + JSON_EXTENSION;
            }
            m_configFile = absolutePath;
            File f = new File(absolutePath);

            if (f.exists()) {
                String message = "The file already exists. Do you want to overwrite it ?";
                String title = "Overwrite ?";
                String[] options = {"Yes", "No"};
                int reply = JOptionPane.showOptionDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "Yes");
                if (reply != JOptionPane.YES_OPTION) {

                    return; // cancel save
                }
                f.delete();
            }

            try {

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String jsonString = gson.toJson(generateConfigFileFromGUI());

                FilePreferences filePreferences = new FilePreferences(f, null, "");
                filePreferences.put(DEFAULT_SERVER_CONFIG_KEY, m_currentServerConfigStr);
                filePreferences.put(CURRENT_CONFIG_KEY, jsonString);

            } catch (Exception e) {
                logger.error("Error while saving the configuration " + e);
            }

        }

    }

    protected void presentationModeChanged() {
        // update the m_presentation attribute when changed for a specific ExportConfigSheet
         recalculateTabsIds();
        recalculateTabTitleIdHashMap();
    }
    
    protected void orientationModeChanged(String sheetId) {
        int selectedTab = m_tabbedPane.getSelectedIndex();
        recalculateTabsIds();
        recalculateTabTitleIdHashMap();
    }

    protected String tabTitleToTabId(String title) { // return the tab id from its known title (supposed to be unique)

        return m_tabTitleIdHashMap.get(title);

    }

    protected int tabTitleToTabPosition(String tabTitle) {
        // returns the position in int for the specified tab title (excel sheet title)
        // it assumes the title names are unique
        for (int i = 0; i < m_tabbedPane.getTabCount(); i++) {
            if (((CheckboxTabPanel) m_tabbedPane.getTabComponentAt(i)).getText().equals(tabTitle)) {
                return i;
            }
        }
        return m_tabbedPane.getTabCount(); // return an out of range number, indicating it has not found the tab by its given name.
    }

    protected int sheetNameToSheetIndex(String sheetTitle) {
        // returns the position in int for the specified tab id (excel sheet title)
        // it assumes the names are unique
        for (int i = 0; i < m_exportConfig.sheets.length; i++) {
            if (m_exportConfig.sheets[i].title.equals(sheetTitle)) {
                return i;
            }
        }
        return m_exportConfig.sheets.length; // return an out of range number, indicating it has not found the sheet
    }

    protected int sheetIdToSheetIndex(String sheetId) {
        // returns the position in int for the specified tab id (excel sheet title)
        // it assumes the names are unique
        for (int i = 0; i < m_exportConfig.sheets.length; i++) {
            if (m_exportConfig.sheets[i].id.equals(sheetId)) {
                return i;
            }
        }
        return m_exportConfig.sheets.length; // return an out of range number, indicating it has not found the sheet
    }

    /**
     * EXPORT ACTION
     * generate configle file and export data
     *
     * @return
     */
    protected ExportConfig generateConfigFileFromGUI() {

        // this method creates an ExportConfig structure to export.
        ExportConfig ec = new ExportConfig();

        // global parameters 
        if (m_exporTypeCombobox.getSelectedIndex() == 0) {
            ec.format = "xlsx";
        } else if (m_exporTypeCombobox.getSelectedIndex() == 1) {
            ec.format = "tsv";
        }
        if (comboBox_NumberSeparator.getSelectedIndex() == 0) {
            ec.decimal_separator = ".";
        } else if (comboBox_NumberSeparator.getSelectedIndex() == 1) {
            ec.decimal_separator = ",";
        }
        ec.date_format = (String) comboBox_DateFormat.getSelectedItem();

        ec.data_export = new ExportDataExport();
        ec.data_export.all_protein_set = comboBox_ProteinSets.getSelectedItem().equals("All");

        ec.data_export.best_profile = comboBox_exportProfile.getSelectedItem().equals("Best");

        // extra infos for default options (sent from server only)
        ec.format_values = null; //["xlsx","tsv"],
        ec.decimal_separator_values = null; //": [".",","],
        ec.date_format_values = null; //": ["YYYY:MM:DD HH:mm:ss","YYYY:MM:DD"],
        ec.sheet_presentation_values = null; //": ["rows","columns"]

        int nbActiveTabs = 0;
        for (int i = 0; i < m_tabbedPane.getTabCount(); i++) { // go through tab panes and jtables
            if (m_tabbedPane.isEnabledAt(i)) {
                nbActiveTabs++;
            }
        }
        ec.sheets = new ExportExcelSheet[nbActiveTabs];

        int usedTabNumber = 0; // the tab location for the new structure (smaller than the full table - disabled tabs)
        for (int i = 0; i < m_tabbedPane.getTabCount(); i++) { // go through tab panes and jtables
            if (m_tabbedPane.isEnabledAt(i)) { // save only enabled panes (hence excel sheets)
                ec.sheets[usedTabNumber] = new ExportExcelSheet();
                //get the orientation value
                JPanel panelTemp = (JPanel) m_tabbedPane.getComponentAt(i);
                JPanel sheetOptionPane = (JPanel) panelTemp.getComponent(0);//getComponent(0) is the select all field checkBox JPanel

                JComboBox orientation = (JComboBox)sheetOptionPane.getComponent(1);
                
                if (orientation.getSelectedIndex() == 0){
                    ec.sheets[usedTabNumber].setPresentation(ORIENTATION_ROW);
                }else if(orientation.getSelectedIndex() == 1){
                    ec.sheets[usedTabNumber].setPresentation(ORIENTATION_COLUMNS);
                }
                 // get the jtable out of the jpane...   
                JScrollPane jsp = (JScrollPane) panelTemp.getComponent(1);
                JTable tableRef = (JTable) jsp.getViewport().getComponents()[0];

                int nbRows = tableRef.getRowCount();
                int nbSelectedRows = 0;
                for (int row = 0; row < nbRows; row++) { // count selected rows to be exported
                    if (tableRef.getValueAt(row, 2).equals(true)) {
                        nbSelectedRows++;
                    }
                }
                

                ec.sheets[usedTabNumber].id = tabTitleToTabId(((CheckboxTabPanel) m_tabbedPane.getTabComponentAt(i)).getText());
                ec.sheets[usedTabNumber].title = ((CheckboxTabPanel) m_tabbedPane.getTabComponentAt(i)).getText();
 
                ec.sheets[usedTabNumber].fields = new ExportExcelSheetField[nbSelectedRows];

                // copy all selected sheet fields into new structure
                int newStructRow = 0; // position in new sheet structure 
                for (int currentRow = 0; currentRow < nbRows; currentRow++) {
                    if (tableRef.getValueAt(currentRow, 2).equals(true)) { // if selected row then add it
                        ec.sheets[usedTabNumber].fields[newStructRow] = new ExportExcelSheetField();
                        ec.sheets[usedTabNumber].fields[newStructRow].id = tableRef.getValueAt(currentRow, 0).toString();
                        ec.sheets[usedTabNumber].fields[newStructRow].title = tableRef.getValueAt(currentRow, 1).toString();

                        newStructRow++;
                    }
                }
                usedTabNumber++;
            }
        }

        return ec;

    }

    public String getFileName() {
        return m_fileTextField.getText().trim();
    }

    public ExporterFactory.ExporterInfo getExporterInfo() {
        return (ExporterFactory.ExporterInfo) m_exporTypeCombobox.getSelectedItem();
    }

    /**
     * if export button is called, first check diffrent input parameter, then
     * generate the config file
     *
     * @return
     */
    @Override
    protected boolean okCalled() {
        // Ok Button is : setButtonName(BUTTON_OK, "Export");
        String fileName = m_fileTextField.getText().trim();

        if (fileName.length() == 0) {
            setStatus(true, "You must fill the file name.");
            highlight(m_fileTextField);
            return false;
        }

        File f = new File(fileName);
        if (isFileExportMode() && f.exists()) {
            String message = "The file already exists. Do you want to overwrite it ?";
            String title = "Overwrite ?";
            String[] options = {"Yes", "No"};
            int reply = JOptionPane.showOptionDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "Yes");
            if (reply != JOptionPane.YES_OPTION) {
                setStatus(true, "File already exists.");
                return false;
            }
        }

        if (isFileExportMode()) {
            FileWriter fw = null;
            try {
                fw = new FileWriter(f);
                fw.write("t");
            } catch (Exception e) {
                setStatus(true, fileName + " is not writable.");
                highlight(m_fileTextField);
                return false;
            } finally {
                try {
                    if (fw != null) {
                        fw.close();
                    }

                    f.delete();
                } catch (Exception e2) {
                }
            }
        } else if (!f.canWrite()) {
            setStatus(true, "Cannot write in this directory");
            highlight(m_fileTextField);
            return false;
        }

        // check config
        ExportConfig config = generateConfigFileFromGUI();
        String msgError = checkTitles(config);
        if (!msgError.isEmpty()) {
            JOptionPane.showMessageDialog(this, msgError);
            return false;
        }

        startTask(m_singletonDialog.m_task);

        Preferences preferences = NbPreferences.root();
        preferences.put("DefaultExcelExportPath", f.getAbsoluteFile().getParentFile().getAbsolutePath());

        String exportConfigStr = getExportConfig();
        m_configServerKey2configMap.put(m_currentServerConfigStr, exportConfigStr);

        return false;

    }

    @Override
    protected boolean cancelCalled() {

        return true;
    }

    /**
     * *
     * returns the JSON String corresponding to the export configuration
     *
     * @return
     */
    public String getExportConfig() {
        logger.debug("getExportConfig");
        m_exportConfig = generateConfigFileFromGUI();
        return m_exportConfig == null ? null : new GsonBuilder().create().toJson(m_exportConfig);
    }

    public String getFileExtension() {
        m_exportConfig = generateConfigFileFromGUI();
        return m_exportConfig == null ? null : m_exportConfig.format;
    }

    /**
     * set the defaultConfiguration
     *
     * @param serverConfigStr the JSON string
     */
    public boolean setDefaultExportConfig(String serverConfigStr) {

        m_currentServerConfigStr = serverConfigStr;

        boolean mustUpdateConfig = false;

        String configStrToApply = m_configServerKey2configMap.get(serverConfigStr);
        if (configStrToApply == null) {
            configStrToApply = serverConfigStr;
            m_configServerKey2configMap.put(serverConfigStr, serverConfigStr);
        }

        mustUpdateConfig = (serverConfigStr.compareTo(configStrToApply) != 0) || (m_previousServerConfigStr == null) || (m_currentServerConfigStr.compareTo(m_previousServerConfigStr) != 0);

        m_previousServerConfigStr = m_currentServerConfigStr;

        if (mustUpdateConfig) {

            logger.debug("setDefaultExportConfig");

            m_configFile = "";
            m_exportDefaultConfig = new Gson().fromJson(serverConfigStr, ExportConfig.class);  //JPM.TODO
            // create a hashmap of tabs titles and ids in case of renaming
            m_tabTitleIdHashMap.clear();
            for (int i = 0; i < m_exportDefaultConfig.sheets.length; i++) {
                m_tabTitleIdHashMap.put(m_exportDefaultConfig.sheets[i].title, m_exportDefaultConfig.sheets[i].id);

            }

            fillExportPossibleValues(m_exportDefaultConfig);
            //m_exportConfig = m_exportDefaultConfig; // this in order to have the config like the default one, before one is loaded.
            if (m_exportDefaultConfig != null) {
                fillExportFormatTable(m_exportDefaultConfig, m_exportConfig);
                //m_exportConfig = m_exportDefaultConfig; // this in order to have the config like the default one, before one is loaded.
            }

            //////////////////
            Gson gson = new Gson();
            String messageHashMapJsonString = configStrToApply;
            m_exportConfig = new ExportConfig();
            m_exportConfig = gson.fromJson(messageHashMapJsonString, m_exportConfig.getClass());

            fillExportFormatTable(m_exportDefaultConfig, m_exportConfig);

            updatePresentationModeForNewlySelectedTab();
            selectLoadedExportValues(m_exportConfig);

        }

        return mustUpdateConfig;
    }

    /* return true if the configuration is ok regarding the titles (should not be empty and 1 sheet can not contain 2 same title) */
    private String checkTitles(ExportConfig config) {
        String errorsOnConfig = "";
        ExportExcelSheet[] allSheets = config.sheets;
        int s = 1;
        for (ExportExcelSheet sheet : allSheets) {
            if (sheet.title == null || sheet.title.trim().isEmpty()) {
                errorsOnConfig += "The sheet at position " + (s) + " has no title! \n";
            }
            ExportExcelSheetField[] allFields = sheet.fields;
            int f = 0;
            for (ExportExcelSheetField field : allFields) {
                if (field.title == null || field.title.trim().isEmpty()) {
                    errorsOnConfig += "The field in the sheet " + sheet.title + " at position " + (f + 1) + " has no title! \n";
                } else if (sheet.containsFieldTitle(field.title, f)) {
                    errorsOnConfig += "The field " + field.title + " in the sheet " + sheet.title + " (at position " + (f + 1) + ") is already defined. \n";
                }
                f++;
            }
            s++;
        }
        return errorsOnConfig;
    }

    public void updateFileExport(boolean mustUpdateConfig) {
        String text = m_fileTextField.getText().trim();
        if (!text.isEmpty()) {
            // file or dir?
            boolean isDir = new File(text).isDirectory();
            if (!isFileExportMode() && !isDir) {
                int id = text.lastIndexOf("\\");
                if (id != -1) {
                    String newText = text.substring(0, id + 1);
                    m_fileTextField.setText(newText);
                }
            }
        }

    }

    @Override
    public void collapse(boolean collapse) {
        boolean isExpanded = !collapse;
        m_optionPanel.setVisible(isExpanded);

        setButtonEnabled(BUTTON_SAVE, isExpanded);

        if (!isExpanded) {
            //disable custom parameters and restore default ones
            m_exportConfig = null; //m_exportDefaultConfig;
            fillExportFormatTable(m_exportDefaultConfig, m_exportConfig);
            recalculateTabsIds();
            recalculateTabTitleIdHashMap();
        }
        m_singletonDialog.revalidate();
        m_singletonDialog.repack();
    }

    /**
     *
     * set all the field export option true or false
     *
     * @param isAllSelected field Export option in
     */
    private void updateExportField(int indexCur, boolean isAllSelected) {

        JPanel panelTemp = (JPanel) m_tabbedPane.getComponentAt(indexCur);
        //getComponent(0) is the select all field checkBox JPanel
        JScrollPane jsp = (JScrollPane) panelTemp.getComponent(1);
        JTable tableRef = (JTable) jsp.getViewport().getComponents()[0];

        for (int j = 0; j < tableRef.getRowCount(); j++) {
            tableRef.setValueAt(isAllSelected, j, 2);
        }
    }


    /**
     * create a panel who has the orientation JComboBox and select all/non JCheckBox
     * @param sheet, the sheet of CheckboxTabPanel
     * @return
     */
    private JPanel createSheetOptionPane(ExportExcelSheet sheet) {
        JComboBox orientationComboBox;
        JLabel orientationLabel;
        JCheckBox allSelectedChkBox;
        JLabel allSelectLabel;
       
        //create Orientation  component
        orientationComboBox = new JComboBox();
        orientationLabel = new JLabel(ORIENTATION_LABEL);

        orientationComboBox.setModel(new DefaultComboBoxModel(new String[]{ORIENTATION_ROW, ORIENTATION_COLUMNS}));
        String defaultOrientation  =  sheet.getPresentation();
        if (defaultOrientation.equals(ORIENTATION_ROW)){
            orientationComboBox.setSelectedItem(ORIENTATION_ROW);
        }else{
             orientationComboBox.setSelectedItem(ORIENTATION_COLUMNS);
        }
             
       
        orientationComboBox.setName("");
        orientationComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!m_updateInProgress) {
                    orientationModeChanged(sheet.getId());
                }
            }
        });
        
        //create select all/non field component
        allSelectLabel = new JLabel(this.EXPORT_ALL_FIELD_LABEL);
        allSelectedChkBox = new JCheckBox();
        allSelectedChkBox.setSelected(true);
                allSelectedChkBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                boolean isSelected = allSelectedChkBox.isSelected();

                int indexCur = -1; //the index of the tab 
                int nbTabs = m_tabbedPane.getTabCount();
                String titleId;
                for (int i = 0; i < nbTabs; i++) {
                    CheckboxTabPanel tabComponent = (CheckboxTabPanel) m_tabbedPane.getTabComponentAt(i);
                    
                    titleId = tabComponent.getSheetId();
                   
                    if (titleId.equals(sheet.getId())) {
                        indexCur = i;
                        break;
                    }
                }

                if (indexCur != -1) {
                    updateExportField(indexCur, isSelected);
                }
            }
        });
       
        
        // layout
        JPanel selectFieldPane = new JPanel();
        selectFieldPane.setLayout(new BoxLayout(selectFieldPane, BoxLayout.LINE_AXIS));
        
        orientationLabel.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
        selectFieldPane.add(orientationLabel);
        selectFieldPane.add(orientationComboBox);
        selectFieldPane.add(Box.createHorizontalGlue());
        allSelectLabel.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
        selectFieldPane.add(allSelectLabel);

        selectFieldPane.add(allSelectedChkBox);
        selectFieldPane.add(Box.createRigidArea(new Dimension(80, 0)));

        return selectFieldPane;
    }

    /**
     * create one tab:<br>
     * 1 the Tabitle :CheckboxTabPanel, 
     * <br>2 call createSheetOptionPane for orientation and select fields,
     * <br>3 create a JScrollPane, in this scrollPane, 
     * <br>4 create a table,this table can be drag and down.
     * @param sheet, ExportExcelSheet
     * @param isSheetSelected
     * @return an empty JTable, in which we can add field name id, field name, selected/non for export
     */
    private JTable createSheetTab(ExportExcelSheet sheet, boolean isSheetSelected) {
        JPanel tablePanel = new JPanel();
        m_tabbedPane.addTab(null, tablePanel);
        tablePanel.setLayout(new BorderLayout(0, 0));
        CheckboxTabPanel closableTabPanel = new CheckboxTabPanel(m_tabbedPane, sheet.title, sheet.id);
        int tabIndex = m_tabbedPane.getTabCount() - 1;
        m_tabbedPane.setTabComponentAt(tabIndex, closableTabPanel);
        m_tabbedPane.setEnabledAt(tabIndex, isSheetSelected);//############setEnable the new tab
        // put id in tooltip in order to find the tab title from the tooltip even if renamed.
        // @todo TODO: find a better way...
         m_tabbedPane.setToolTipTextAt(tabIndex, sheet.id+ " Rename the title by Right click to Enable/Disable");
        
        closableTabPanel.setSelected(isSheetSelected);//############setSelected with given value
        JPanel selecAllFieldPane = createSheetOptionPane(sheet);

        JScrollPane tableScrollPane = new JScrollPane();
        tableScrollPane.getViewport().setBackground(Color.white);
        tablePanel.add(selecAllFieldPane, BorderLayout.NORTH);
        tablePanel.add(tableScrollPane, BorderLayout.CENTER);

        JTable table = new JTable();

        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setSelectionMode(0); // Allow only 1 item to be selected TODO: add possibility to select blocks or multiple independent rows to move (with drag n drop)
        table.setTransferHandler(new TableRowTransferHandler(table));

        table.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                e.consume();
                JComponent c = (JComponent) e.getSource();
                TransferHandler handler = c.getTransferHandler();
                handler.exportAsDrag(c, e, TransferHandler.MOVE);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });

        //CustomExportTableModel tableModel = new CustomExportTableModel();
        tableScrollPane.setViewportView(table);
        return table;
    }
}
