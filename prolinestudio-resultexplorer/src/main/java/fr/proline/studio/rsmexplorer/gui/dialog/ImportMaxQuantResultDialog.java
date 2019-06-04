/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.uds.InstrumentConfiguration;
import fr.proline.core.orm.uds.PeaklistSoftware;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.AbstractParameterToString;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.StringParameter;
import fr.proline.studio.settings.FilePreferences;
import fr.proline.studio.settings.SettingsDialog;
import fr.proline.studio.settings.SettingsUtils;
import fr.proline.studio.utils.IconManager;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class ImportMaxQuantResultDialog extends DefaultDialog {

    private final static String SETTINGS_KEY = "ImportMaxQuantResult";

    //Import parameters
    private ParameterList m_sourceParameterList;
    private JComboBox m_instrumentsComboBox = null;
    private JTextField m_accessionRegexpTF = null;
    private JCheckBox m_importQuantitationCB = null;

    //To select folder containing result
    private JList<File> m_fileList;
    private JScrollPane m_fileListScrollPane;
    private JButton m_addFileButton;
    private JButton m_removeFileButton;
    private String m_defaultImportMQPath;
    private JFileChooser m_fchooser;

    private static ImportMaxQuantResultDialog m_singletonDialog;

    public static ImportMaxQuantResultDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new ImportMaxQuantResultDialog(parent);
        }

        m_singletonDialog.reinitialize();

        return m_singletonDialog;
    }

    private ImportMaxQuantResultDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);

        setTitle("Import MaxQuant Results");

        setButtonVisible(BUTTON_LOAD, true);
        setButtonVisible(BUTTON_SAVE, true);

        setResizable(true);
        setMinimumSize(new Dimension(200, 240));

        initInternalPanel();

        restoreInitialParameters(NbPreferences.root());
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

    private JPanel createFileSelectionPanel() {

        Preferences preferences = NbPreferences.root();
        m_defaultImportMQPath = preferences.get("DefaultImportMQResultPath", "");

        // Creation of Objects for File Selection Panel
        JPanel fileSelectionPanel = new JPanel(new GridBagLayout());
        fileSelectionPanel.setBorder(BorderFactory.createTitledBorder(" Files Selection "));

        m_fileList = new JList<>(new DefaultListModel());
        m_fileListScrollPane = new JScrollPane(m_fileList) {

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
                if (m_defaultImportMQPath != null && m_defaultImportMQPath.length() > 0) {
                    m_fchooser = new JFileChooser(new File(m_defaultImportMQPath));
                } else {
                    m_fchooser = new JFileChooser();
                }
                m_fchooser.setMultiSelectionEnabled(false);
                m_fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int result = m_fchooser.showOpenDialog(m_singletonDialog);
                if (result == JFileChooser.APPROVE_OPTION) {

                    boolean hasFilesPreviously = (m_fileList.getModel().getSize() != 0);

                    File[] files = new File[1];
                    files[0] = m_fchooser.getSelectedFile();
                    int nbFiles = files.length;
                    for (int i = 0; i < nbFiles; i++) {
                        ((DefaultListModel) m_fileList.getModel()).addElement(files[i]);
                    }
                    setStatus(false, String.format("%d file(s)", m_fileList.getModel().getSize()));
                    if (nbFiles > 0) {
                        File f = files[0];
                        f = f.getParentFile();
                        if ((f != null) && (f.isDirectory())) {
                            m_defaultImportMQPath = f.getAbsolutePath();
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

    private JPanel createAllParametersPanel() {
        JPanel allParametersPanel = new JPanel(new GridBagLayout());
        allParametersPanel.setBorder(BorderFactory.createTitledBorder(" Parameters "));

        m_sourceParameterList = createSourceParameters();
        m_sourceParameterList.updateValues(NbPreferences.root());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridy++;
        JLabel instrumentLabel = new JLabel("Instrument :");
        instrumentLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        allParametersPanel.add(instrumentLabel, c);

        c.gridx++;
        c.gridwidth = 2;
        c.weightx = 1;
        allParametersPanel.add(m_instrumentsComboBox, c);

        c.gridx = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridy++;
        JLabel peaklistSoftwareLabel = new JLabel("Accession regexp :");
        peaklistSoftwareLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        allParametersPanel.add(peaklistSoftwareLabel, c);

        c.gridx++;
        c.gridwidth = 2;
        c.weightx = 1;
        allParametersPanel.add(m_accessionRegexpTF, c);

        c.gridx = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridy++;
        allParametersPanel.add(new JPanel(), c);

        c.gridx++;
        c.gridwidth = 2;
        c.weightx = 1;
        allParametersPanel.add(m_importQuantitationCB, c);

        return allParametersPanel;
    }

    private void reinitialize() {
        // reinit of files selection
        ((DefaultListModel) m_fileList.getModel()).removeAllElements();
        m_removeFileButton.setEnabled(false);
        setStatus(false, String.format("%d file(s)", m_fileList.getModel().getSize()));
        restoreInitialParameters(NbPreferences.root());
    }

    public long getInstrumentId() {
        InstrumentConfiguration instrument = (InstrumentConfiguration) m_sourceParameterList.getParameter("instrument").getObjectValue();
        return instrument.getId();
    }

    public String getAccessionRegexp() {
        return m_sourceParameterList.getParameter("accession_regexp").getStringValue();
    }

    public Boolean getImportQuantitation() {
        return (Boolean) m_sourceParameterList.getParameter("import_quant_result").getObjectValue();
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

    @Override
    protected boolean saveCalled() {
        // check parameters
        if (!checkParametersForSave()) {
            return false;
        }

        JFileChooser fileChooser = SettingsUtils.getFileChooser(SETTINGS_KEY);
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            FilePreferences filePreferences = new FilePreferences(f, null, "");

            saveParameters(filePreferences);

            SettingsUtils.addSettingsPath(SETTINGS_KEY, f.getAbsolutePath());
            SettingsUtils.writeDefaultDirectory(SETTINGS_KEY, f.getParent());
        }

        return false;
    }

    @Override
    protected boolean loadCalled() {

        SettingsDialog settingsDialog = new SettingsDialog(this, SETTINGS_KEY);
        settingsDialog.setLocationRelativeTo(this);
        settingsDialog.setVisible(true);

        if (settingsDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            if (settingsDialog.isDefaultSettingsSelected()) {
                reinitialize();
            } else {
                try {
                    File settingsFile = settingsDialog.getSelectedFile();
                    FilePreferences filePreferences = new FilePreferences(settingsFile, null, "");

                    Preferences preferences = NbPreferences.root();
                    String[] keys = filePreferences.keys();
                    for (int i = 0; i < keys.length; i++) {
                        String key = keys[i];
                        String value = filePreferences.get(key, null);
                        preferences.put(key, value);
                    }

                    restoreInitialParameters(preferences);

                    m_sourceParameterList.loadParameters(filePreferences);

                } catch (Exception e) {
                    LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Parsing of User Settings File Failed", e);
                    setStatus(true, "Parsing of your Settings File failed");
                }
            }
        }

        return false;
    }

    private boolean checkParametersForOK() {
        // check files selected
        int nbFiles = m_fileList.getModel().getSize();
        if (nbFiles == 0) {
            setStatus(true, "You must select a MaxQuant result folder to import.");
            highlight(m_fileList);
            return false;
        }

        return checkParametersForSave();
    }

    private boolean checkParametersForSave() {

        // check source parameters
        ParameterError error = m_sourceParameterList.checkParameters();

        // report error
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }

        return true;
    }

    private void saveParameters(Preferences preferences) {

        // save file path
        if (m_defaultImportMQPath != null) {
            preferences.put("DefaultImportMQResultPath", m_defaultImportMQPath);
        }

        // Save Other Parameters    
        m_sourceParameterList.saveParameters(preferences);
    }

    private void restoreInitialParameters(Preferences preferences) {
        m_defaultImportMQPath = preferences.get("DefaultImportMQResultPath", null);

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

        m_instrumentsComboBox = new JComboBox(DatabaseDataManager.getDatabaseDataManager().getInstrumentsWithNullArray());
        final ObjectParameter<InstrumentConfiguration> instrumentParameter = new ObjectParameter<>("instrument", "Instrument", m_instrumentsComboBox, DatabaseDataManager.getDatabaseDataManager().getInstrumentsWithNullArray(), null, -1, instrumentToString);
        parameterList.add(instrumentParameter);
        m_instrumentsComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                instrumentParameter.setUsed(true);  //JPM.WART : found a better fix (parameters not saved if it has never been set)
            }
        });

        m_accessionRegexpTF = new JTextField(20);
        m_accessionRegexpTF.setToolTipText(NbBundle.getMessage(ImportMaxQuantResultDialog.class, "ImportMaxQuantResultDialog.accessionRegExp.Tooltip.text"));
        StringParameter accessionParameter = new StringParameter("accession_regexp", "Accession regular expression", m_accessionRegexpTF, "", 0, null);
        accessionParameter.setUsed(true);
        parameterList.add(accessionParameter);

        m_importQuantitationCB = new JCheckBox("Import quantitation values");
        BooleanParameter importParameter = new BooleanParameter("import_quant_result", "Import quantitation results", m_importQuantitationCB, false);
        importParameter.setUsed(true);
        parameterList.add(importParameter);

        return parameterList;

    }
}
