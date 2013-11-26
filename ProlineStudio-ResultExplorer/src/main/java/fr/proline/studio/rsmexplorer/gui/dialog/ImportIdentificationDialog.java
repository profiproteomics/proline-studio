package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.uds.InstrumentConfiguration;
import fr.proline.core.orm.uds.PeaklistSoftware;
import fr.proline.studio.dam.UDSDataManager;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.CertifyIdentificationTask;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.parameter.*;
import fr.proline.studio.progress.ProgressBarDialog;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.utils.IconManager;
import fr.proline.util.system.OSInfo;
import fr.proline.util.system.OSType;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;

/**
 *
 * Dialog used to start the import of identifications by selecting multiple
 * files, a parser and its parameters.
 * The fields are filled with last used parameters. If they not exist, these fields
 * are filled with default values.
 *
 * @author jm235353
 */
public class ImportIdentificationDialog extends DefaultDialog {

    private static ImportIdentificationDialog m_singletonDialog = null;
    
    private final static String[] PARSER_NAMES = {"Mascot", "Omssa"};
    private final static String[] FILE_EXTENSIONS = {"dat", "omx"};
    private final static String[] FILE_EXTENSIONS_DESCRIPTION = {"Mascot Identification Result", "Omssa Identification Result"};
    private final static String[] PARSER_IDS = { "MascotMSParser", "OmssaMSParser" };
    
    
    private final static String[] DECOY_VALUES = {null, "No Decoy", "Software Engine Decoy", "Concataned Decoy"};
    private final static String[] DECOY_VALUES_ASSOCIATED_KEYS = DECOY_VALUES;
    private static final int CONCATENATED_DECOY_INDEX = 3;
    
    
    private JList<File> m_fileList;
    private JScrollPane m_fileListScrollPane;

    private JButton m_addFileButton;
    private JButton m_removeFileButton;
    

    private JComboBox m_parserComboBox;
    private int m_previousParserIndex = -1;
    private ParameterList m_sourceParameterList;
    private StringParameter m_decoyRegexParameter;

    
    private JComboBox m_instrumentsComboBox = null;
    private JComboBox m_peaklistSoftwaresComboBox = null;
    private JCheckBox m_saveSpectrumCheckBox ;
    private JComboBox m_decoyComboBox = null;
    private JLabel m_decoyAccessionRegexLabel = null;
    private JTextField m_decoyRegexTextField = null;
    private JButton m_regexButton;
    
    private JPanel m_parserParametersPanel = null;


    private File m_defaultDirectory = null;
    
    private long m_projectId;
    
    public static ImportIdentificationDialog getDialog(Window parent, long projectId) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new ImportIdentificationDialog(parent);
        }

        m_singletonDialog.reinitialize();
        m_singletonDialog.m_projectId = projectId;

        return m_singletonDialog;
    }

    private ImportIdentificationDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);


        setTitle("Import Search Results");
        setResizable(true);
        setMinimumSize(new Dimension(200, 240));

        initInternalPanel();

        restoreInitialParameters();
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
        JPanel saveSpectrumPanel = createSaveSpectrumPanel();
        
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

        c.gridy++;
        allParametersPanel.add(saveSpectrumPanel, c);
        
        // init the first parser parameters panel selected
        m_parserComboBox.setSelectedIndex(0);
        
        return allParametersPanel;
    }
    
    private JPanel createFileSelectionPanel() {

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
                
                JFileChooser fchooser = new JFileChooser();
                if ((m_defaultDirectory!=null) && (m_defaultDirectory.isDirectory())) {
                    fchooser.setCurrentDirectory(m_defaultDirectory);
                }
                fchooser.setMultiSelectionEnabled(true);
                
                int nbFilters = FILE_EXTENSIONS_DESCRIPTION.length;
                //FileNameExtensionFilter defaultFilter = null;
                for (int i = 0; i < nbFilters; i++) {
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(FILE_EXTENSIONS_DESCRIPTION[i], FILE_EXTENSIONS[i]);
                    fchooser.addChoosableFileFilter(filter);
                    /*
                     * if (i == 0) { defaultFilter = filter;
                    }
                     */
                }
                //fchooser.setFileFilter(defaultFilter);
                int result = fchooser.showOpenDialog(m_singletonDialog);
                if (result == JFileChooser.APPROVE_OPTION) {

                    boolean hasFilesPreviously = (m_fileList.getModel().getSize() != 0);

                    File[] files = fchooser.getSelectedFiles();
                    int nbFiles = files.length;
                    for (int i = 0; i < nbFiles; i++) {
                        ((DefaultListModel) m_fileList.getModel()).addElement(files[i]);
                    }

                    // select Parser according to the extension of the first file
                    if ((nbFiles > 0) && !hasFilesPreviously) {
                        File f = files[0];
                        String fileName = f.getName();
                        int indexOfDot = fileName.lastIndexOf('.');
                        if (indexOfDot != -1) {
                            int parserIndex = -1;
                            String fileExtension = fileName.substring(indexOfDot + 1);
                            for (int i = 0; i < FILE_EXTENSIONS.length; i++) {
                                String extension = FILE_EXTENSIONS[i];
                                if (fileExtension.compareToIgnoreCase(extension) == 0) {
                                    parserIndex = i;
                                    break;
                                }
                            }
                            if (parserIndex >= 0) {
                                m_parserComboBox.setSelectedIndex(parserIndex);
                            }
                        }
                    }
                    
                    if (nbFiles>0) {
                        File f = files[0];
                        f = f.getParentFile();
                        if ((f!=null) && (f.isDirectory())) {
                            m_defaultDirectory = f;
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
        m_sourceParameterList.updateIsUsed();
        
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
        m_decoyAccessionRegexLabel = new JLabel("Decoy Accesion Regex :");
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
        
        /*
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
                    writeRegexArray(regexArrayList);
                }
            }
        });*/
        
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
        m_decoyAccessionRegexLabel = new JLabel("Decoy Accesion Regex :");
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
                    writeRegexArray(regexArrayList);
                }
            }
        });

        return decoyPanel;
    }
    
    private JPanel createSaveSpectrumPanel() {
        
        JPanel saveSpectrumPanel = new JPanel(new GridBagLayout());


        
        // Placement of Objects for Parser Panel
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        saveSpectrumPanel.add(m_saveSpectrumCheckBox, c);
        
        return saveSpectrumPanel;
    }
    
    private ArrayList<String> readRegexArray(String regexToAdd) {
        
        ArrayList<String> regexArrayList = new ArrayList();
        Preferences preferences = NbPreferences.root();
        int i = 1;
        while (true) {
            String regex = preferences.get("DecoyRegex_"+i, null);
            if (regex == null) {
                break;
            }
            if ((regexToAdd !=null) && (regex.compareTo(regexToAdd) == 0)) {
                regexToAdd = null;
                regexArrayList.add(0, regex);
            } else {
                regexArrayList.add(regex);
            }
            i++;
        } 
        if (regexToAdd!=null) {
            regexArrayList.add(0, regexToAdd);
        }
        
        if (regexArrayList.isEmpty()) {
            regexArrayList.add("###REV###\\S+"); // Grenoble Regex
            regexArrayList.add("sp\\|REV_\\S+"); // Strasbourg Regex
        }

        
        return regexArrayList;
    }
    private void writeRegexArray(ArrayList<String> regexArrayList) {
        Preferences preferences = NbPreferences.root();
        
        // remove previous regex
        int i = 1;
        while (true) {
            String key = "DecoyRegex_"+i;
        
            String regex = preferences.get(key, null);
            if (regex == null) {
                break;
            }
            preferences.remove(key);
            i++;
        }
        
        // put new regex
        for (i=0;i<regexArrayList.size();i++) {
            String key = "DecoyRegex_"+(i+1);
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

      
    }
    


    private void reinitialize() {
        
        // reinit of files selection
        ((DefaultListModel) m_fileList.getModel()).removeAllElements();
        m_removeFileButton.setEnabled(false);
        
        // reinit of some parameters
        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        parameterList.clean();
        
        updateDecoyRegexEnabled();
    }

    @Override
    protected boolean okCalled() {


        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        
        // check global parameters
        if (!checkParameters()) {
            return false;
        }
        
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
               

        // check that the server file path is defined
        Preferences preferences = NbPreferences.root();
        String serverFilePath = preferences.get("ServerIdentificationFilePath", null);
        if (serverFilePath == null) {
            
            OptionDialog dialog = new OptionDialog(this, "Server Search Result File Path", "Please define the file path where the search results (Mascot, Omssa files...) are saved.\nAsk to your IT Administrator if you don't know where it is.\nIf the data server is running on your computer, the file path can be empty.", "Search Result File Path");
                    
            // on windows, try to predict the path for identifications
            OSType osType = OSInfo.getOSType();
            if ((osType == OSType.WINDOWS_AMD64) || (osType == OSType.WINDOWS_X86)) {
                File f = m_fileList.getModel().getElementAt(0);
                try {
                    String path = f.getCanonicalPath();
                    int indexOfSeparator = path.indexOf(File.separatorChar);
                    if (indexOfSeparator != -1) {
                        path = path.substring(0, indexOfSeparator+1);
                        dialog.setText(path);
                    }
                } catch (IOException ioe) {
                    
                } 
            }
            
            dialog.setAllowEmptyText(true);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
            
            if (dialog.getButtonClicked() == OptionDialog.BUTTON_OK) {
                preferences.put("ServerIdentificationFilePath", dialog.getText());
            } else {
                return false;
            }
            
        }
        
        
        // save parser
        String parserSelected = parameterList.toString();
        preferences.put("IdentificationParser", parserSelected);
        
        // save file path
        if (m_defaultDirectory != null) {
          preferences.put("UserIdentificationFilePath", m_defaultDirectory.getAbsolutePath());  
        }

        
        // Save Other Parameters    
        m_sourceParameterList.saveParameters();
        parameterList.saveParameters();
        
        if (m_decoyRegexTextField.isEnabled()) {
            ArrayList<String> regexArrayList = readRegexArray(m_decoyRegexTextField.getText());
            writeRegexArray(regexArrayList);
        }

        
        // Certify Import
        if (!certifyImport()) {
            return false;
        }
        
        return true;

    }

    public boolean certifyImport() {

        // retrieve parameters
        File[] filePaths = getFilePaths();
        HashMap<String, String> parserArguments = getParserArguments();

        String parserId = getParserId();


        int nbFiles = filePaths.length;
        String[] canonicalPathArray = new String[nbFiles];
        for (int i = 0; i < nbFiles; i++) {
            File f = filePaths[i];

            // use canonicalPath when it is possible to be sure to have an unique path
            String canonicalPath;
            try {
                canonicalPath = f.getCanonicalPath();
            } catch (IOException ioe) {
                canonicalPath = f.getAbsolutePath(); // should not happen
            }
            canonicalPathArray[i] = canonicalPath;
        }

        final CertifyIdentificationProgress progressInterface = new CertifyIdentificationProgress();
        
        
        AbstractServiceCallback callback = new AbstractServiceCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                
                progressInterface.setLoaded();
                
                if (success) {
                    //JPM.TODO 
                    //ImportSearchResultAsRsetAction.fireListener(_project.getId());
                    //createDataset(identificationNode, _project, _parentDataset, _datasetName, _resultSetId[0], getTaskInfo());
                } else {
                    //JPM.TODO
                }
            }
        };





        String[] result = new String[1];
        CertifyIdentificationTask task = new CertifyIdentificationTask(callback, parserId, parserArguments, canonicalPathArray, m_projectId, result);
        AccessServiceThread.getAccessServiceThread().addTask(task);

        
        ProgressBarDialog dialog = ProgressBarDialog.getDialog(WindowManager.getDefault().getMainWindow(), progressInterface, "Pre-Import", "Check Result Files to Import. Please Wait.");
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.setButtonVisible(ImportIdentificationDialog.BUTTON_CANCEL, false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        

        if (result[0] == null) {
            return true;
        }

        return false;

    }
    

    @Override
    protected boolean cancelCalled() {
        return true;
    }

    @Override
    protected boolean defaultCalled() {
        ParameterList parameterList = (ParameterList) m_parserComboBox.getSelectedItem();
        parameterList.initDefaults();

        return false;
    }

    private boolean checkParameters() {
        
        // check files selected
        int nbFiles = m_fileList.getModel().getSize();
        if (nbFiles == 0) {
            setStatus(true, "You must select a file to import.");
            highlight(m_fileList);
            return false;
        }
        
        return true;
    }
    


    private void restoreInitialParameters() {
        Preferences preferences = NbPreferences.root();
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
        
        
        // Prefered User File Path
        String filePath = preferences.get("UserIdentificationFilePath", null);
        if (filePath != null) {
            File f = new File(filePath);
            if (f.isDirectory()) {
                m_defaultDirectory = f;
            }
        } else {
            // User Server File Path for Prefered User File Path
            String serverFilePath = preferences.get("ServerIdentificationFilePath", null);
            if (serverFilePath != null) {
                File f = new File(serverFilePath);
                if (f.isDirectory()) {
                    m_defaultDirectory = f;
                }
            }
        }
    }


    
    public File[] getFilePaths() {
        
        DefaultListModel model = ((DefaultListModel) m_fileList.getModel());
        int nbFiles = model.getSize();
        File[] filePaths = new File[nbFiles];
        for (int i=0;i<nbFiles;i++) {
            filePaths[i] =  ((File)model.getElementAt(i));
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
    
    public boolean getSaveSpectrumMatches() {
        return m_saveSpectrumCheckBox.isSelected();
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
        ParameterList[] plArray = new ParameterList[2];
        plArray[0] = createMascotParser();
        plArray[1] = createOmssaParser();
        return plArray;
    }
    
    private ParameterList createMascotParser() {
        ParameterList parameterList = new ParameterList("Mascot");
        parameterList.add(new DoubleParameter("ion.score.cutoff", "Ion Score Cutoff", JTextField.class, new Double(0.0), new Double(0), null));
        parameterList.add(new DoubleParameter("subset.threshold", "Subset Threshold", JTextField.class, new Double(1.0), new Double(0), new Double(1)));
        
        return parameterList;
    }
    
    private ParameterList createOmssaParser() {
        ParameterList parameterList = new ParameterList("Omssa Parser");
        
        Preferences preferences = NbPreferences.root();
        parameterList.add(new FileParameter("usermod.xml.file", "Usermods file path", JTextField.class, preferences.get("Omssa_Parser.Usermods_file_path", ""), "Usermods XML File", "xml"));
        parameterList.add(new FileParameter("ptm.composition.file", "PTM composition file path", JTextField.class, preferences.get("Omssa_Parser.PTM_composition_file_path", ""), "PTM composition File", "txt"));
//        parameterList.add(new BooleanParameter("fasta.contains.target", "Fasta contains target entries", JCheckBox.class, Boolean.TRUE));
//        parameterList.add(new BooleanParameter("fasta.contains.decoy", "Fasta contains decoy entries", JCheckBox.class, Boolean.TRUE));

        
        return parameterList;
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
                return o.getName();
            }  
        };
        
        m_instrumentsComboBox = new JComboBox(UDSDataManager.getUDSDataManager().getInstrumentsWithNullArray());
        ObjectParameter<InstrumentConfiguration> instrumentParameter = new ObjectParameter<>("instrument", "Instrument", m_instrumentsComboBox, UDSDataManager.getUDSDataManager().getInstrumentsWithNullArray(), null, -1, instrumentToString);
        parameterList.add(instrumentParameter);
        
        m_peaklistSoftwaresComboBox = new JComboBox(UDSDataManager.getUDSDataManager().getPeaklistSoftwaresWithNullArray());
        ObjectParameter<PeaklistSoftware> peaklistParameter = new ObjectParameter("peaklist_software", "Peaklist Software", m_peaklistSoftwaresComboBox, UDSDataManager.getUDSDataManager().getPeaklistSoftwaresWithNullArray(), null, -1, softwareToString);
        parameterList.add(peaklistParameter);
        
        m_decoyComboBox = new JComboBox(DECOY_VALUES);
        ObjectParameter<String> decoyParameter = new ObjectParameter<>("decoy_accession", "Decoy", m_decoyComboBox, DECOY_VALUES, DECOY_VALUES_ASSOCIATED_KEYS, 0, null);
        parameterList.add(decoyParameter);

        m_decoyRegexTextField = new JTextField(20);
        m_decoyRegexParameter = new StringParameter("decoy_accession", "Decoy Accession", m_decoyRegexTextField, "", new Integer(2), null);
        m_decoyRegexParameter.setUsed(false);
        parameterList.add(m_decoyRegexParameter);

        BooleanParameter saveSpectrumParameter = new BooleanParameter("save_spectrum_matches", "Save Spectrum Matches", JCheckBox.class, Boolean.FALSE);
        m_saveSpectrumCheckBox = (JCheckBox) saveSpectrumParameter.getComponent(null);
        parameterList.add(saveSpectrumParameter);
        
        
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
            setButtonVisible(DefaultDialog.BUTTON_DEFAULT, false);
            
            initInternalPanel();
            

        }
        
        private void initData(ArrayList<String> regexArrayList) {
            m_regexArrayList = regexArrayList;
            
            DefaultListModel<String> model = (DefaultListModel<String>) m_regexList.getModel();
            model.clear();
            
            int nb = regexArrayList.size();
            for (int i=0;i<nb;i++) {
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
            for (int i=0;i<size;i++) {
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
    
    
    public class CertifyIdentificationProgress implements ProgressInterface {

        private boolean m_isLoaded = false;

        @Override
        public boolean isLoaded() {
            return m_isLoaded;
        }

        @Override
        public int getLoadingPercentage() {
            return 0; // progress bar displayed as a waiting bar
        }

        public void setLoaded() {
            m_isLoaded = true;
        }
    };
        
    
}
