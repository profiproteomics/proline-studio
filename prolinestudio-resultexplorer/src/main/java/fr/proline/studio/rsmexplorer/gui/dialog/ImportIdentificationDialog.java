/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.uds.FragmentationRuleSet;
import fr.proline.core.orm.uds.InstrumentConfiguration;
import fr.proline.core.orm.uds.PeaklistSoftware;
import fr.proline.studio.NbPreferences;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dpm.serverfilesystem.RootInfo;
import fr.proline.studio.dpm.serverfilesystem.ServerFile;
import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.DefaultStorableDialog;
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.parameter.*;
import fr.proline.studio.utils.IconManager;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.prefs.Preferences;

/**
 *
 * Dialog used to start the import of identifications by selecting multiple
 * files, a parser and its parameters. The fields are filled with last used
 * parameters. If they not exist, these fields are filled with default values.
 *
 * @author jm235353
 */
public class ImportIdentificationDialog extends DefaultStorableDialog {

    private static ImportIdentificationDialog m_singletonDialog = null;

    private static final String OMSSA_PARSER = "Omssa Parser";
    private static final String MASCOT_PARSER = "Mascot";
    private static final String XTANDEM_PARSER = "X!Tandem Parser";
    private static final String MZ_IDENT_PARSER = "MzIdentML";

    private final static String[] PARSER_NAMES = {MASCOT_PARSER, OMSSA_PARSER, XTANDEM_PARSER, MZ_IDENT_PARSER};
    // ABU : added a map to consider the case when a parser handles more than one extensions
    // the key is the extension, the value is the id of the parser in the other arrays
    private static final Map<String, Integer> EXTENSION_TO_PARSER = new HashMap<>();

    static {
        EXTENSION_TO_PARSER.put("dat", 0);
        EXTENSION_TO_PARSER.put("omx", 1);
        EXTENSION_TO_PARSER.put("omx.bz2", 1);
        EXTENSION_TO_PARSER.put("xml", 2);
        EXTENSION_TO_PARSER.put("mzid", 3);
    }
    //private final static String[] FILE_EXTENSIONS = {"dat", "omx"};
    private final static String[] FILE_EXTENSIONS_DESCRIPTION = {"Mascot Identification Result", "Omssa Identification Result", "X!Tandem Identification Result", "MZIdentML Identification Result"};
    private final static String[] PARSER_IDS = {"mascot.dat", "omssa.omx", "xtandem.xml", "mzidentml.mzid"};

    private final static String[] DECOY_VALUES = {null, "No Decoy", "Software Engine Decoy", "Concatenated Decoy"};
    private final static String[] DECOY_VALUES_ASSOCIATED_KEYS = DECOY_VALUES;
    private static final int CONCATENATED_DECOY_INDEX = 3;

    private final static String SETTINGS_KEY = "ImporteIdentification";

    private JList<File> m_fileList;

    private JButton m_addFileButton;
    private JButton m_removeFileButton;

    private JComboBox m_parserComboBox;
    private int m_previousParserIndex = -1;
    private ParameterList m_sourceParameterList;
    private StringParameter m_decoyRegexParameter;

    private JComboBox m_instrumentsComboBox = null;
    private JComboBox m_fragmentationRuleSetsComboBox = null;
    private JComboBox m_peaklistSoftwaresComboBox = null;

    private JComboBox m_decoyComboBox = null;
    private JLabel m_decoyAccessionRegexLabel = null;
    private JTextField m_decoyRegexTextField = null;
    private JButton m_regexButton;

    private JPanel m_parserParametersPanel = null;

    private boolean m_rootPathError = false;

    private ServerFile m_defaultDirectory = null;

    public static ImportIdentificationDialog getDialog(Window parent/*, long projectId*/) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new ImportIdentificationDialog(parent);
        }

        m_singletonDialog.reinitialize();

        return m_singletonDialog;
    }

    private ImportIdentificationDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Import Search Results");

        setDocumentationSuffix("id.147n2zr");

        setResizable(true);
        setMinimumSize(new Dimension(200, 240));

        initInternalPanel();

        restoreInitialParameters(NbPreferences.root());
    }

    /***  DefaultStorableDialog Abstract methods ***/

    @Override
    protected String getSettingsKey() {
        return SETTINGS_KEY;
    }


    @Override
    protected void resetParameters() throws Exception {
        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        parameterList.initDefaults();
    }

    @Override
    protected void loadParameters(Preferences filePreferences) throws Exception {

        Preferences preferences = NbPreferences.root();
        String[] keys = filePreferences.keys();
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String value = filePreferences.get(key, null);
            preferences.put(key, value);
        }

        restoreInitialParameters(preferences);

        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        parameterList.loadParameters(filePreferences);
        m_sourceParameterList.loadParameters(filePreferences);
    }

    @Override
    protected  void saveParameters(Preferences preferences) {

        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();

        // save parser
        String parserSelected = parameterList.toString();
        preferences.put("IdentificationParser", parserSelected);

        // save file path
        if (m_defaultDirectory != null) {
            preferences.put("IdentificationFilePath", m_defaultDirectory.getAbsolutePath());
        }

        // Save Other Parameters
        m_sourceParameterList.saveParameters(preferences);
        parameterList.saveParameters(preferences);

        if (m_decoyRegexTextField.isEnabled()) {
            ArrayList<String> regexArrayList = readRegexArray(m_decoyRegexTextField.getText());
            writeRegexArray(preferences, regexArrayList);
        }

    }

    @Override
    protected boolean checkParameters() {
        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();

        // check source parameters
        ParameterError error = m_sourceParameterList.checkParameters();

        // check specific parameters
        if (error == null) {
            error = parameterList.checkParameters();
        }

        // report error
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }

        return true;
    }

    private void initInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());

        // create fileSelectionPanel
        JPanel fileSelectionPanel = createFileSelectionPanel();

        // create all other parameters panel
        JPanel allParametersPanel = createAllParametersPanel();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        internalPanel.add(fileSelectionPanel, c);

        c.gridy++;
        c.weighty = 0;
        internalPanel.add(allParametersPanel, c);

        setInternalComponent(internalPanel);

    }

    private JPanel createAllParametersPanel() {
        JPanel allParametersPanel = new JPanel(new GridBagLayout());
        allParametersPanel.setBorder(BorderFactory.createTitledBorder(" Parameters "));

        // create parserPanel
        JPanel parserPanel = createParserPanel();

        // create decoyPanel
        JPanel decoyPanel = createDecoyPanel();

        // create parameter panel
        m_parserParametersPanel = createParametersPanel();

        // create panel with option save Spectrum Matches
        //JPanel saveSpectrumPanel = createSaveSpectrumPanel();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        allParametersPanel.add(parserPanel, c);

        c.gridy++;
        allParametersPanel.add(decoyPanel, c);

        c.gridy++;
        allParametersPanel.add(m_parserParametersPanel, c);

        /*c.gridy++;
         allParametersPanel.add(saveSpectrumPanel, c);*/
        // init the first parser parameters panel selected
        m_parserComboBox.setSelectedIndex(0);

        return allParametersPanel;
    }

    private JPanel createFileSelectionPanel() {

        // Creation of Objects for File Selection Panel
        JPanel fileSelectionPanel = new JPanel(new GridBagLayout());
        fileSelectionPanel.setBorder(BorderFactory.createTitledBorder(" Files Selection "));

        m_fileList = new JList<>(new DefaultListModel());
        JScrollPane m_fileListScrollPane = new JScrollPane(m_fileList) {

            private Dimension preferredSize = new Dimension(360, 200);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }
        };

        m_addFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
        m_addFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        m_removeFileButton = new JButton(IconManager.getIcon(IconManager.IconType.ERASER));
        m_removeFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        // Placement of Objects for File Selection Panel
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 3;
        c.weightx = 1.0;
        c.weighty = 1.0;
        fileSelectionPanel.add(m_fileListScrollPane, c);

        c.gridx++;
        c.gridheight = 1;
        c.weightx = 0;
        c.weighty = 0;
        fileSelectionPanel.add(m_addFileButton, c);

        c.gridy++;
        fileSelectionPanel.add(m_removeFileButton, c);

        c.gridy++;
        fileSelectionPanel.add(Box.createVerticalStrut(30), c);

        // Actions on objects
        m_fileList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                boolean sometingSelected = (m_fileList.getSelectedIndex() != -1);
                m_removeFileButton.setEnabled(sometingSelected);
            }
        });

        m_addFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setStatus(false, String.format("%d file(s)", m_fileList.getModel().getSize()));
                if (m_rootPathError) {
                    LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Server has returned no Root Path for Result Files. There is a problem with the server installation, please contact your administrator.");

                    InfoDialog errorDialog = new InfoDialog(m_singletonDialog, InfoDialog.InfoType.WARNING, "Root Path Error", "Server has returned no Root Path for Result Files.\nThere is a problem with the server installation, please contact your administrator.", true);
                    errorDialog.setButtonVisible(DefaultDialog.BUTTON_CANCEL, false);
                    errorDialog.setLocationRelativeTo(m_singletonDialog);
                    errorDialog.setVisible(true);
                    return;
                }

                JFileChooser fchooser;
                if ((m_defaultDirectory != null) && (m_defaultDirectory.isDirectory())) {
                    fchooser = new JFileChooser(m_defaultDirectory, ServerFileSystemView.getServerFileSystemView());
                } else {
                    // should not happen in fact
                    fchooser = new JFileChooser(ServerFileSystemView.getServerFileSystemView());
                }
                fchooser.setMultiSelectionEnabled(true);

                // ABU : for each parser, add all its extensions
                String[] filters = new String[FILE_EXTENSIONS_DESCRIPTION.length];
                for (String key : EXTENSION_TO_PARSER.keySet()) {
                    int i = EXTENSION_TO_PARSER.get(key);
                    if (filters[i] == null) {
                        filters[i] = "";
                    }
                    if (!filters[i].equals("")) {
                        filters[i] += ";";
                    }
                    if (key.contains(".")) { // if the extension contains a dot, only keep what is on its right (ie. "omx.bz2" -> "bz2")
                        int indexOfDot = key.lastIndexOf('.');
                        filters[i] += key.substring(indexOfDot + 1);
                    } else {
                        filters[i] += key;
                    }
                }
                for (int i = 0; i < filters.length; i++) { // extensions with a dot inside will not be considered
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(FILE_EXTENSIONS_DESCRIPTION[i], filters[i].split(";"));
                    fchooser.addChoosableFileFilter(filter);
                }

                int result = fchooser.showOpenDialog(m_singletonDialog);
                if (result == JFileChooser.APPROVE_OPTION) {

                    boolean hasFilesPreviously = (m_fileList.getModel().getSize() != 0);

                    File[] files = fchooser.getSelectedFiles();
                    int nbFiles = files.length;
                    for (int i = 0; i < nbFiles; i++) {
                        ((DefaultListModel) m_fileList.getModel()).addElement(files[i]);
                    }
                    setStatus(false, String.format("%d file(s)", m_fileList.getModel().getSize()));

                    // select Parser according to the extension of the first file
                    // ABU : use the map to deal with different extensions for the same parser
                    //if ((nbFiles > 0) && !hasFilesPreviously) {
                    if (nbFiles > 0) { //Even if has files previously, check again software engin
                        File f = files[0];
                        String fileName = f.getName();
                        int parserIndex = -1;
                        for (String key : EXTENSION_TO_PARSER.keySet()) {
                            String extension = "." + key;
                            if (fileName.endsWith(extension)) {
                                parserIndex = EXTENSION_TO_PARSER.get(key);
                                break;
                            }
                        }
                        if (parserIndex >= 0) {
                            m_parserComboBox.setSelectedIndex(parserIndex);
                        }

                    }

                    if (nbFiles > 0) {
                        File f = files[0];
                        f = f.getParentFile();
                        if ((f != null) && (f.isDirectory())) {
                            if (f instanceof ServerFile) {
                                m_defaultDirectory = (ServerFile) f;
                            }
                        }
                    }
                }
            }
        });

        m_removeFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                List<File> selectedValues = m_fileList.getSelectedValuesList();
                Iterator<File> it = selectedValues.iterator();
                while (it.hasNext()) {
                    ((DefaultListModel) m_fileList.getModel()).removeElement(it.next());
                }
                setStatus(false, String.format("%d file(s)", m_fileList.getModel().getSize()));
                m_removeFileButton.setEnabled(false);
            }
        });

        return fileSelectionPanel;

    }

    private JPanel createParserPanel() {
        // Creation of Objects for the Parser Panel
        JPanel parserPanel = new JPanel(new GridBagLayout());

        JLabel parserLabel = new JLabel("Software Engine :");
        parserLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        m_parserComboBox = new JComboBox(createParameters());

        // Placement of Objects for Parser Panel
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        parserPanel.add(parserLabel, c);

        c.gridx++;
        c.gridwidth = 2;
        c.weightx = 1;
        parserPanel.add(m_parserComboBox, c);

        m_sourceParameterList = createSourceParameters();
        m_sourceParameterList.updateValues(NbPreferences.root());

        c.gridx = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridy++;
        JLabel instrumentLabel = new JLabel("Instrument :");
        instrumentLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        parserPanel.add(instrumentLabel, c);

        c.gridx++;
        c.gridwidth = 2;
        c.weightx = 1;
        parserPanel.add(m_instrumentsComboBox, c);

        c.gridx = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridy++;
        JLabel fragRuleSetLabel = new JLabel("Fragmentation Rule Set :");
        fragRuleSetLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        parserPanel.add(fragRuleSetLabel, c);

        c.gridx++;
        c.gridwidth = 1;
        c.weightx = 1;
        parserPanel.add(m_fragmentationRuleSetsComboBox, c);

        c.gridx++;
        c.weightx = 0;
        JButton viewFragmentationRuleSet = new JButton(IconManager.getIcon(IconManager.IconType.DOCUMENT_LIST));
        viewFragmentationRuleSet.setMargin(new java.awt.Insets(2, 2, 2, 2));
        viewFragmentationRuleSet.setToolTipText("View Fragmentation Rule Sets");
        viewFragmentationRuleSet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FragmentationRuleSetViewer viewer = new FragmentationRuleSetViewer(m_singletonDialog);
                viewer.setVisible(true);

            }
        });
        parserPanel.add(viewFragmentationRuleSet, c);

        c.gridx = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridy++;
        JLabel peaklistSoftwareLabel = new JLabel("Peaklist Software :");
        peaklistSoftwareLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        parserPanel.add(peaklistSoftwareLabel, c);

        c.gridx++;
        c.gridwidth = 2;
        c.weightx = 1;
        parserPanel.add(m_peaklistSoftwaresComboBox, c);

        /*
         c.gridx = 0;
         c.gridwidth = 1;
         c.weightx = 0;
         c.gridy++;
         JLabel decoyLabel = new JLabel("Decoy :");
         decoyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         parserPanel.add(decoyLabel, c);
        
         c.gridx++;
         c.gridwidth = 2;
         c.weightx = 1;
         parserPanel.add(m_decoyComboBox, c);
        
        
         c.gridx = 0;
         c.gridwidth = 1;
         c.weightx = 0;
         c.gridy++;
         m_decoyAccessionRegexLabel = new JLabel("Decoy Accession Regex :");
         m_decoyAccessionRegexLabel.setHorizontalAlignment(SwingConstants.RIGHT);
         parserPanel.add(m_decoyAccessionRegexLabel, c);
        
         c.gridx++;
         c.weightx = 1;
         parserPanel.add(m_decoyRegexTextField, c);
        
         c.gridx++;
         c.weightx = 0;
         m_regexButton = new JButton(IconManager.getIcon(IconManager.IconType.DOCUMENT_LIST));
         m_regexButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
         parserPanel.add(m_regexButton, c);
         */
        m_parserComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int parserIndex = m_parserComboBox.getSelectedIndex();
                if (parserIndex == m_previousParserIndex) {
                    return;
                }

                m_previousParserIndex = parserIndex;

                initParameters();

                // resize the dialog
                repack();
            }
        });

        return parserPanel;
    }

    private JPanel createDecoyPanel() {
        // Creation of Objects for the Parser Panel
        JPanel decoyPanel = new JPanel(new GridBagLayout());
        decoyPanel.setBorder(BorderFactory.createTitledBorder(" Decoy Parameters "));

        // Placement of Objects for Parser Panel
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridy = 0;
        JLabel decoyLabel = new JLabel("Decoy :");
        decoyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        decoyPanel.add(decoyLabel, c);

        c.gridx++;
        c.gridwidth = 2;
        c.weightx = 1;
        decoyPanel.add(m_decoyComboBox, c);

        c.gridx = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridy++;
        m_decoyAccessionRegexLabel = new JLabel("Decoy Accession Regex :");
        m_decoyAccessionRegexLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        decoyPanel.add(m_decoyAccessionRegexLabel, c);

        c.gridx++;
        c.weightx = 1;
        decoyPanel.add(m_decoyRegexTextField, c);

        c.gridx++;
        c.weightx = 0;
        m_regexButton = new JButton(IconManager.getIcon(IconManager.IconType.DOCUMENT_LIST));
        m_regexButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        decoyPanel.add(m_regexButton, c);

        m_decoyComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateDecoyRegexEnabled();
            }
        });

        m_regexButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                ArrayList<String> regexArrayList = readRegexArray(null);
                SelectRegexDialog regexDialog = SelectRegexDialog.getDialog(m_singletonDialog, regexArrayList);
                regexDialog.setLocationRelativeTo(m_regexButton);
                regexDialog.setVisible(true);
                if (regexDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {

                    String selectedRegex = regexDialog.getSelectedRegex();
                    if (selectedRegex != null) {
                        m_decoyRegexTextField.setText(selectedRegex);
                    }
                    regexArrayList = regexDialog.getRegexArrayList();
                    writeRegexArray(NbPreferences.root(), regexArrayList);

                }
            }
        });

        return decoyPanel;
    }

    private ArrayList<String> readRegexArray(String regexToAdd) {

        ArrayList<String> regexArrayList = new ArrayList();
        Preferences preferences = NbPreferences.root();
        int i = 1;
        while (true) {
            String regex = preferences.get("DecoyRegex_" + i, null);
            if (regex == null) {
                break;
            }
            if ((regexToAdd != null) && (regex.compareTo(regexToAdd) == 0)) {
                regexToAdd = null;
                regexArrayList.add(0, regex);
            } else {
                regexArrayList.add(regex);
            }
            i++;
        }
        if (regexToAdd != null) {
            regexArrayList.add(0, regexToAdd);
        }

        boolean grenobleRegexFound = false;
        boolean strasbourgRegexFound = false;
        for (i = 0; i < regexArrayList.size(); i++) {
            String regex = regexArrayList.get(i);
            if (regex.compareTo("###REV###\\S+") == 0) { // Grenoble Regex
                grenobleRegexFound = true;
            } else if (regex.compareTo("sp\\|REV_\\S+") == 0) { // Strasbourg Regex
                strasbourgRegexFound = true;
            }
        }
        if (!grenobleRegexFound) {
            regexArrayList.add("###REV###\\S+");
        }
        if (!strasbourgRegexFound) {
            regexArrayList.add("sp\\|REV_\\S+");
        }

        return regexArrayList;
    }

    private void writeRegexArray(Preferences preferences, ArrayList<String> regexArrayList) {

        // remove previous regex
        int i = 1;
        while (true) {
            String key = "DecoyRegex_" + i;

            String regex = preferences.get(key, null);
            if (regex == null) {
                break;
            }
            preferences.remove(key);
            i++;
        }

        // put new regex
        for (i = 0; i < regexArrayList.size(); i++) {
            String key = "DecoyRegex_" + (i + 1);
            preferences.put(key, regexArrayList.get(i));
        }
    }

    private void updateDecoyRegexEnabled() {
        boolean enabled = (m_decoyComboBox.getSelectedIndex() == CONCATENATED_DECOY_INDEX);
        m_decoyRegexParameter.setUsed(enabled); // done anyway to be sure there is not a problem at initialization
        m_decoyAccessionRegexLabel.setEnabled(enabled);
        m_decoyRegexTextField.setEnabled(enabled);
        m_regexButton.setEnabled(enabled);

    }

    private JPanel createParametersPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(" Parser Parameters "));
        return panel;
    }

    private void initParameters() {

        // remove all parameters
        m_parserParametersPanel.removeAll();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;

        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        m_parserParametersPanel.add(parameterList.getPanel(), c);

//        // allow spectrum matches for all parsers except Mascot
//        boolean allowSaveSpectrumMatches = (mzIdentiParameterList.toString().compareTo(MASCOT_PARSER) != 0);
//        m_saveSpectrumCheckBox.setEnabled(allowSaveSpectrumMatches);
//        if (!allowSaveSpectrumMatches) {
//            m_saveSpectrumCheckBox.setSelected(false);
//        }
    }

    private void reinitialize() {

        // reinit of files selection
        ((DefaultListModel) m_fileList.getModel()).removeAllElements();
        setStatus(false, String.format("%d file(s)", m_fileList.getModel().getSize()));
        m_removeFileButton.setEnabled(false);

        // reinit of some parameters
        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        parameterList.clean();

        //reinit FragmentationRuleSets
        FragmentationRuleSet[] allFRS = DatabaseDataManager.getDatabaseDataManager().getFragmentationRuleSetsWithNullArray();
        String selectedFragmRuleSet = ((ObjectParameter) m_sourceParameterList.getParameter("fragmentation_rule_set")).getStringValue();
        m_fragmentationRuleSetsComboBox.removeAllItems();
        for (int i = 0; i < allFRS.length; i++) {
            m_fragmentationRuleSetsComboBox.addItem(allFRS[i]);
        }
        ((ObjectParameter) m_sourceParameterList.getParameter("fragmentation_rule_set")).updateObjects(allFRS);
        ((ObjectParameter) m_sourceParameterList.getParameter("fragmentation_rule_set")).setValue(selectedFragmRuleSet);

        //reinit FragmentationRuleSets
        PeaklistSoftware[] allPS = DatabaseDataManager.getDatabaseDataManager().getPeaklistSoftwaresWithNullArray();
        String selectedPeaklistSoft = ((ObjectParameter) m_sourceParameterList.getParameter("peaklist_software")).getStringValue();
        m_peaklistSoftwaresComboBox.removeAllItems();
        for (int i = 0; i < allPS.length; i++) {
            m_peaklistSoftwaresComboBox.addItem(allPS[i]);
        }
        ((ObjectParameter) m_sourceParameterList.getParameter("peaklist_software")).updateObjects(allPS);
        ((ObjectParameter) m_sourceParameterList.getParameter("peaklist_software")).setValue(selectedPeaklistSoft);

        updateDecoyRegexEnabled();
    }

    @Override
    protected boolean okCalled() {

        // check parameters
        if (!checkParametersForOK()) {
            return false;
        }

        saveParameters(NbPreferences.root());

        return true;

    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }


    private boolean checkParametersForOK() {
        // check files selected
        int nbFiles = m_fileList.getModel().getSize();
        if (nbFiles == 0) {
            setStatus(true, "You must select a file to import.");
            highlight(m_fileList);
            return false;
        }

        return checkParameters();
    }


    private void restoreInitialParameters(Preferences preferences) {
        String parser = preferences.get("IdentificationParser", null);

        int parserIndex = -1;
        if (parser != null) {

            int nbParsers = PARSER_NAMES.length;
            for (int i = 0; i < nbParsers; i++) {
                String parserCur = PARSER_NAMES[i];
                if (parser.compareToIgnoreCase(parserCur) == 0) {
                    parserIndex = i;
                    break;
                }
            }
        }
        if (parserIndex == -1) {
            parserIndex = 0; // select first parser of the list
        }

        // select the parser
        m_parserComboBox.setSelectedIndex(parserIndex);

        ArrayList<String> roots = ServerFileSystemView.getServerFileSystemView().getLabels(RootInfo.TYPE_RESULT_FILES);
        if ((roots == null) || (roots.isEmpty())) {
            // check that the server has sent me at least one root path
            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Server has returned no Root Path for Result Files. There is a problem with the server installation, please contact your administrator.");
            m_rootPathError = true;
            return;
        } else {
            m_rootPathError = false;
        }

        //
        String filePath = preferences.get("IdentificationFilePath", null);
        if (filePath == null) {
            if (roots.size() >= 1) {
                filePath = roots.get(0);
            }
        }
        if (filePath != null) {
            ServerFile f = new ServerFile(filePath, filePath, true, 0, 0);
            if (f.isDirectory()) {
                m_defaultDirectory = f;
            }
        }

    }

    public File[] getFilePaths() {

        DefaultListModel model = ((DefaultListModel) m_fileList.getModel());
        int nbFiles = model.getSize();
        File[] filePaths = new File[nbFiles];
        for (int i = 0; i < nbFiles; i++) {
            filePaths[i] = ((File) model.getElementAt(i));
        }

        return filePaths;
    }

    public HashMap<String, String> getParserArguments() {
        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        return parameterList.getValues();
    }

    public long getInstrumentId() {

        InstrumentConfiguration instrument = (InstrumentConfiguration) m_sourceParameterList.getParameter("instrument").getObjectValue();
        return instrument.getId();
    }

    public long getPeaklistSoftwareId() {
        PeaklistSoftware peaklistSoftware = (PeaklistSoftware) m_sourceParameterList.getParameter("peaklist_software").getObjectValue();
        return peaklistSoftware.getId();
    }

    public long getFragmentationRuleSetId() {
        FragmentationRuleSet fragmentationRuleSet = (FragmentationRuleSet) m_sourceParameterList.getParameter("fragmentation_rule_set").getObjectValue();
        if (fragmentationRuleSet == null) {
            return -1L;
        }
        return fragmentationRuleSet.getId();
    }


    public String getParserId() {
        return PARSER_IDS[m_parserComboBox.getSelectedIndex()];
    }

    public String getDecoyRegex() {
        if (m_decoyRegexTextField.isEnabled()) {
            return m_decoyRegexTextField.getText();
        }
        return null;
    }

    private ParameterList[] createParameters() {
        ParameterList[] plArray = new ParameterList[4];
        plArray[0] = createMascotParser();
        plArray[1] = createOmssaParser();
        plArray[2] = createXtandemParser();
        plArray[3] = createMzIdentParser();
        return plArray;
    }

    private ParameterList createMascotParser() {
        ParameterList parameterList = new ParameterList(MASCOT_PARSER);
        parameterList.add(new DoubleParameter("subset.threshold", "Subset Threshold", JTextField.class, Double.valueOf(1.0), Double.valueOf(0), Double.valueOf(1)));

        return parameterList;
    }

    private ParameterList createOmssaParser() {
        ParameterList parameterList = new ParameterList(OMSSA_PARSER);

        Preferences preferences = NbPreferences.root();

        String[] fileFilterNames = {"Usermods XML File"};
        String[] fileFilterExtensions = {"xml"};
        parameterList.add(new FileParameter(ServerFileSystemView.getServerFileSystemView(), "usermod.xml.file", "Usermods file path", JTextField.class, preferences.get("Omssa_Parser.Usermods_file_path", ""), fileFilterNames, fileFilterExtensions));

        String[] fileFilterNames2 = {"PTM composition File"};
        String[] fileFilterExtensions2 = {"txt"};
        parameterList.add(new FileParameter(ServerFileSystemView.getServerFileSystemView(), "ptm.composition.file", "PTM composition file path", JTextField.class, preferences.get("Omssa_Parser.PTM_composition_file_path", ""), fileFilterNames2, fileFilterExtensions2));

        return parameterList;
    }

    private ParameterList createXtandemParser() {
        ParameterList parameterList = new ParameterList(XTANDEM_PARSER);

        Preferences preferences = NbPreferences.root();
        parameterList.add(new StringParameter("protein.parsing.rule", "Protein parsing rule", JTextField.class, preferences.get("XtandemParser.protein_parsing_rule", ""), 0, 50));

        return parameterList;
    }

    private ParameterList createMzIdentParser() {
        ParameterList mzIdentParameterList = new ParameterList(MZ_IDENT_PARSER);

        Preferences preferences = NbPreferences.root();
        mzIdentParameterList.add(new StringParameter("Test", "Test", JTextField.class, preferences.get("MzIdentParser.test", ""), 0, 20));

        return mzIdentParameterList;
    }

    private ParameterList createSourceParameters() {

        ParameterList parameterList = new ParameterList("Parameter Source");

        AbstractParameterToString<InstrumentConfiguration> instrumentToString = new AbstractParameterToString<InstrumentConfiguration>() {
            @Override
            public String toString(InstrumentConfiguration o) {
                return o.getName();
            }
        };

        AbstractParameterToString<PeaklistSoftware> softwareToString = new AbstractParameterToString<PeaklistSoftware>() {
            @Override
            public String toString(PeaklistSoftware o) {
                String version = o.getVersion();
                if (version == null) {
                    return o.getName();
                }
                return o.getName() + " " + version;
            }
        };

        AbstractParameterToString<FragmentationRuleSet> fragmentationRuleSetToString = new AbstractParameterToString<FragmentationRuleSet>() {
            @Override
            public String toString(FragmentationRuleSet o) {
                return o.getName();
            }
        };

        m_instrumentsComboBox = new JComboBox(DatabaseDataManager.getDatabaseDataManager().getInstrumentsWithNullArray());
        final ObjectParameter<InstrumentConfiguration> instrumentParameter = new ObjectParameter<>("instrument", "Instrument", m_instrumentsComboBox, DatabaseDataManager.getDatabaseDataManager().getInstrumentsWithNullArray(), null, -1, instrumentToString);
        parameterList.add(instrumentParameter);
        m_instrumentsComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                instrumentParameter.setUsed(true);  //JPM.WART : found a better fix (parameters not saved if it has never been set)
            }
        });

        m_fragmentationRuleSetsComboBox = new JComboBox(DatabaseDataManager.getDatabaseDataManager().getFragmentationRuleSetsWithNullArray());
        final ObjectParameter<FragmentationRuleSet> fragmentationRuleSetParameter = new ObjectParameter<>("fragmentation_rule_set", "FragmentationRuleSet", m_fragmentationRuleSetsComboBox, DatabaseDataManager.getDatabaseDataManager().getFragmentationRuleSetsWithNullArray(), null, -1, fragmentationRuleSetToString);
        parameterList.add(fragmentationRuleSetParameter);
        m_fragmentationRuleSetsComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fragmentationRuleSetParameter.setUsed(true);  //JPM.WART : found a better fix (parameters not saved if it has never been set)
            }
        });

        m_peaklistSoftwaresComboBox = new JComboBox(DatabaseDataManager.getDatabaseDataManager().getPeaklistSoftwaresWithNullArray());
        final ObjectParameter<PeaklistSoftware> peaklistParameter = new ObjectParameter("peaklist_software", "Peaklist Software", m_peaklistSoftwaresComboBox, DatabaseDataManager.getDatabaseDataManager().getPeaklistSoftwaresWithNullArray(), null, -1, softwareToString);
        parameterList.add(peaklistParameter);
        m_peaklistSoftwaresComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                peaklistParameter.setUsed(true);   //JPM.WART : found a better fix (parameters not saved if it has never been set)
            }
        });

        m_decoyComboBox = new JComboBox(DECOY_VALUES);
        final ObjectParameter<String> decoyParameter = new ObjectParameter<>("decoy_type", "Decoy", m_decoyComboBox, DECOY_VALUES, DECOY_VALUES_ASSOCIATED_KEYS, 0, null);
        parameterList.add(decoyParameter);
        m_decoyComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                decoyParameter.setUsed(true); //JPM.WART : found a better fix (parameters not saved if it has never been set)
            }
        });

        m_decoyRegexTextField = new JTextField(20);
        m_decoyRegexParameter = new StringParameter("decoy_accession", "Decoy Accession", m_decoyRegexTextField, "", Integer.valueOf(2), null);
        m_decoyRegexParameter.setUsed(false);
        m_decoyRegexParameter.setCompulsory(false);
        parameterList.add(m_decoyRegexParameter);

        return parameterList;
    }

    /**
     * Class used to select Regex previously used
     */
    public static class SelectRegexDialog extends DefaultDialog {

        private static SelectRegexDialog m_selectRegexSingletonDialog = null;

        private JList<String> m_regexList;
        private JScrollPane m_regexListScrollPane;
        private JButton m_removeRegexButton;

        private String m_selectedRegex = null;
        private ArrayList<String> m_regexArrayList = null;

        public static SelectRegexDialog getDialog(JDialog parent, ArrayList<String> regexArrayList) {
            if (m_selectRegexSingletonDialog == null) {
                m_selectRegexSingletonDialog = new SelectRegexDialog(parent);
            }
            m_selectRegexSingletonDialog.initData(regexArrayList);

            return m_selectRegexSingletonDialog;
        }

        private SelectRegexDialog(JDialog parent) {
            super(parent, Dialog.ModalityType.APPLICATION_MODAL);

            setTitle("Select Decoy Accession Regex");
            setResizable(false);
            setMinimumSize(new Dimension(200, 240));

            setButtonVisible(DefaultDialog.BUTTON_HELP, false);

            initInternalPanel();

        }

        private void initData(ArrayList<String> regexArrayList) {
            m_regexArrayList = regexArrayList;

            DefaultListModel<String> model = (DefaultListModel<String>) m_regexList.getModel();
            model.clear();

            int nb = regexArrayList.size();
            for (int i = 0; i < nb; i++) {
                model.addElement(regexArrayList.get(i));
            }
        }

        private void initInternalPanel() {

            JPanel internalPanel = new JPanel();
            internalPanel.setLayout(new java.awt.GridBagLayout());

            // create regexSelectionPanel
            JPanel regexSelectionPanel = createRegexSelectionPanel();

            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(5, 5, 5, 5);

            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1.0;
            c.weighty = 1.0;
            internalPanel.add(regexSelectionPanel, c);

            setInternalComponent(internalPanel);

        }

        private JPanel createRegexSelectionPanel() {

            // Creation of Objects for Regex Selection Panel
            JPanel regexSelectionPanel = new JPanel(new GridBagLayout());
            regexSelectionPanel.setBorder(BorderFactory.createTitledBorder(" Regex Selection "));

            m_regexList = new JList<>(new DefaultListModel<String>());

            // double clicking on regex -> select it and click on ok button
            m_regexList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        m_selectRegexSingletonDialog.doClick(DefaultDialog.BUTTON_OK);
                    }
                }
            });

            m_regexListScrollPane = new JScrollPane(m_regexList) {

                private Dimension preferredSize = new Dimension(280, 140);

                @Override
                public Dimension getPreferredSize() {
                    return preferredSize;
                }
            };

            m_removeRegexButton = new JButton(IconManager.getIcon(IconManager.IconType.ERASER));
            m_removeRegexButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

            // Placement of Objects for Regex Selection Panel
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.NORTHWEST;
            c.fill = GridBagConstraints.BOTH;
            c.insets = new java.awt.Insets(5, 5, 5, 5);

            c.gridx = 0;
            c.gridy = 0;
            c.gridheight = 3;
            c.weightx = 1.0;
            c.weighty = 1.0;
            regexSelectionPanel.add(m_regexListScrollPane, c);

            c.gridx++;
            c.gridheight = 1;
            c.weightx = 0;
            c.weighty = 0;
            regexSelectionPanel.add(m_removeRegexButton, c);

            c.gridy++;
            regexSelectionPanel.add(Box.createVerticalStrut(30), c);

            // Actions on objects
            m_regexList.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    boolean sometingSelected = (m_regexList.getSelectedIndex() != -1);
                    m_removeRegexButton.setEnabled(sometingSelected);
                }
            });

            m_removeRegexButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    List<String> selectedValues = m_regexList.getSelectedValuesList();
                    Iterator<String> it = selectedValues.iterator();
                    while (it.hasNext()) {
                        ((DefaultListModel) m_regexList.getModel()).removeElement(it.next());
                    }
                    m_removeRegexButton.setEnabled(false);
                }
            });

            return regexSelectionPanel;

        }

        @Override
        protected boolean okCalled() {
            m_selectedRegex = m_regexList.getSelectedValue();

            DefaultListModel<String> model = (DefaultListModel<String>) m_regexList.getModel();
            int size = model.getSize();
            if (m_regexArrayList == null) {
                m_regexArrayList = new ArrayList<>(size);
            } else {
                m_regexArrayList.clear();
            }
            for (int i = 0; i < size; i++) {
                m_regexArrayList.add(model.elementAt(i));
            }
            return true;
        }

        @Override
        protected boolean cancelCalled() {
            m_selectedRegex = null;
            return true;
        }

        public String getSelectedRegex() {
            return m_selectedRegex;
        }

        public ArrayList<String> getRegexArrayList() {
            return m_regexArrayList;
        }
    }

}
