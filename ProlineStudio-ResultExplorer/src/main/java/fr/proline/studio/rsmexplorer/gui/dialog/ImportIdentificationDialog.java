package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.uds.InstrumentConfiguration;
import fr.proline.core.orm.uds.PeaklistSoftware;
import fr.proline.studio.dam.UDSDataManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.parameter.*;
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

    private static ImportIdentificationDialog singletonDialog = null;
    
    private final static String[] PARSER_NAMES = {"Mascot", "Omssa"};
    private final static String[] FILE_EXTENSIONS = {"dat", "omx"};
    private final static String[] FILE_EXTENSIONS_DESCRIPTION = {"Mascot Identification Result", "Omssa Identification Result"};
    private final static String[] PARSER_IDS = { "mascot.dat", "omssa.omx" };
    
    
    private final static String[] DECOY_VALUES = {null, "No Decoy", "Software Engine Decoy", "Concataned Decoy"};
    private final static String[] DECOY_VALUES_ASSOCIATED_KEYS = DECOY_VALUES;
    private static final int CONCATENATED_DECOY_INDEX = 3;
    
    
    private JList<File> fileList;
    private JScrollPane fileListScrollPane;

    private JButton addFileButton;
    private JButton removeFileButton;
    

    private JComboBox parserComboBox;
    private int previousParserIndex = -1;
    private ParameterList sourceParameterList;
    private StringParameter decoyRegexParameter;

    
    private JComboBox instrumentsComboBox = null;
    private JComboBox peaklistSoftwaresComboBox = null;
    private JComboBox decoyComboBox = null;
    private JLabel decoyAccessionRegexLabel = null;
    private JTextField decoyRegexTextField = null;
    private JButton regexButton;
    
    private JPanel parserParametersPanel = null;


    private File defaultDirectory = null;
    

    public static ImportIdentificationDialog getDialog(Window parent) {
        if (singletonDialog == null) {
            singletonDialog = new ImportIdentificationDialog(parent);
        }

        singletonDialog.reinitialize();

        return singletonDialog;
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
        
        // create parameter panel
        parserParametersPanel = createParametersPanel();
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        allParametersPanel.add(parserPanel, c);
        
        c.gridy++;
        allParametersPanel.add(parserParametersPanel, c);

        
        // init the first parser parameters panel selected
        parserComboBox.setSelectedIndex(0);
        
        return allParametersPanel;
    }
    
    private JPanel createFileSelectionPanel() {

        // Creation of Objects for File Selection Panel
        JPanel fileSelectionPanel = new JPanel(new GridBagLayout());
        fileSelectionPanel.setBorder(BorderFactory.createTitledBorder(" Files Selection "));

        fileList = new JList<>(new DefaultListModel());
        fileListScrollPane = new JScrollPane(fileList) {

            private Dimension preferredSize = new Dimension(360, 200);

            @Override
            public Dimension getPreferredSize() {
                return preferredSize;
            }
        };

        addFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
        addFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        removeFileButton = new JButton(IconManager.getIcon(IconManager.IconType.ERASER));
        removeFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

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
        fileSelectionPanel.add(fileListScrollPane, c);


        c.gridx++;
        c.gridheight = 1;
        c.weightx = 0;
        c.weighty = 0;
        fileSelectionPanel.add(addFileButton, c);

        c.gridy++;
        fileSelectionPanel.add(removeFileButton, c);

        c.gridy++;
        fileSelectionPanel.add(Box.createVerticalStrut(30), c);


        // Actions on objects

        fileList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                boolean sometingSelected = (fileList.getSelectedIndex() != -1);
                removeFileButton.setEnabled(sometingSelected);
            }
        });


        addFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                JFileChooser fchooser = new JFileChooser();
                if ((defaultDirectory!=null) && (defaultDirectory.isDirectory())) {
                    fchooser.setCurrentDirectory(defaultDirectory);
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
                int result = fchooser.showOpenDialog(singletonDialog);
                if (result == JFileChooser.APPROVE_OPTION) {

                    boolean hasFilesPreviously = (fileList.getModel().getSize() != 0);

                    File[] files = fchooser.getSelectedFiles();
                    int nbFiles = files.length;
                    for (int i = 0; i < nbFiles; i++) {
                        ((DefaultListModel) fileList.getModel()).addElement(files[i]);
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
                                parserComboBox.setSelectedIndex(parserIndex);
                            }
                        }
                    }
                    
                    if (nbFiles>0) {
                        File f = files[0];
                        f = f.getParentFile();
                        if ((f!=null) && (f.isDirectory())) {
                            defaultDirectory = f;
                        }
                    }
                }
            }
        });

        removeFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                List<File> selectedValues = fileList.getSelectedValuesList();
                Iterator<File> it = selectedValues.iterator();
                while (it.hasNext()) {
                    ((DefaultListModel) fileList.getModel()).removeElement(it.next());
                }
                removeFileButton.setEnabled(false);
            }
        });

        return fileSelectionPanel;

    }

    private JPanel createParserPanel() {
        // Creation of Objects for the Parser Panel
        JPanel parserPanel = new JPanel(new GridBagLayout());

        JLabel parserLabel = new JLabel("Software Engine :");
        parserLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        parserComboBox = new JComboBox(createParameters());
        

        
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
        parserPanel.add(parserComboBox, c);

        sourceParameterList = createSourceParameters();
        sourceParameterList.updateIsUsed();
        
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
        parserPanel.add(instrumentsComboBox, c);
        
        
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
        parserPanel.add(peaklistSoftwaresComboBox, c);
        
        
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
        parserPanel.add(decoyComboBox, c);
        
        
        c.gridx = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        c.gridy++;
        decoyAccessionRegexLabel = new JLabel("Decoy Accesion Regex :");
        decoyAccessionRegexLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        parserPanel.add(decoyAccessionRegexLabel, c);
        
        c.gridx++;
        c.weightx = 1;
        parserPanel.add(decoyRegexTextField, c);
        
        c.gridx++;
        c.weightx = 0;
        regexButton = new JButton(IconManager.getIcon(IconManager.IconType.DOCUMENT_LIST));
        regexButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        parserPanel.add(regexButton, c);
        
        

            
        parserComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int parserIndex = parserComboBox.getSelectedIndex();
                if (parserIndex == previousParserIndex) {
                    return;
                }

                previousParserIndex = parserIndex;

                initParameters();

                // resize the dialog
                repack();
            }
        });
        
        
        decoyComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateDecoyRegexEnabled();
            }
        });
        
        regexButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                ArrayList<String> regexArrayList = readRegexArray(null);
                SelectRegexDialog regexDialog = SelectRegexDialog.getDialog(singletonDialog, regexArrayList);
                regexDialog.setLocationRelativeTo(regexButton);
                regexDialog.setVisible(true);
                if (regexDialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    
                    
                    
                    String selectedRegex = regexDialog.getSelectedRegex();
                    if (selectedRegex != null) {
                        decoyRegexTextField.setText(selectedRegex);
                    }
                    regexArrayList = regexDialog.getRegexArrayList();
                    writeRegexArray(regexArrayList);
                }
            }
        });
        
        return parserPanel;
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
        boolean enabled = (decoyComboBox.getSelectedIndex() == CONCATENATED_DECOY_INDEX);  
        decoyRegexParameter.setUsed(enabled); // done anyway to be sure there is not a problem at initialization
        decoyAccessionRegexLabel.setEnabled(enabled);
        decoyRegexTextField.setEnabled(enabled);
        regexButton.setEnabled(enabled);

    }

    
    
    private JPanel createParametersPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(" Parser Parameters "));
        return panel;
    }
    
    private void initParameters() {


        // remove all parameters
        parserParametersPanel.removeAll();
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        
        ParameterList parameterList = (ParameterList) parserComboBox.getSelectedItem();
        parserParametersPanel.add(parameterList.getPanel(), c);

      
    }
    


    private void reinitialize() {
        
        // reinit of files selection
        ((DefaultListModel) fileList.getModel()).removeAllElements();
        removeFileButton.setEnabled(false);
        
        // reinit of some parameters
        ParameterList parameterList = (ParameterList) parserComboBox.getSelectedItem();
        parameterList.clean();
        
        updateDecoyRegexEnabled();
    }

    @Override
    protected boolean okCalled() {


        ParameterList parameterList = (ParameterList) parserComboBox.getSelectedItem();
        
        // check global parameters
        if (!checkParameters()) {
            return false;
        }
        
        // check source parameters
        ParameterError error = sourceParameterList.checkParameters();
        
        
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
            
            OptionDialog dialog = new OptionDialog(this, "Server Identification File Path", "Please define the file path where the identifications (Mascot, Omssa files...) are saved.\nAsk to your IT Administrator if you don't know where it is.\nIf the data server is running on your computer, the file path can be empty.", "Identification File Path");
                    
            // on windows, try to predict the path for identifications
            OSType osType = OSInfo.getOSType();
            if ((osType == OSType.WINDOWS_AMD64) || (osType == OSType.WINDOWS_X86)) {
                File f = fileList.getModel().getElementAt(0);
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
        if (defaultDirectory != null) {
          preferences.put("UserIdentificationFilePath", defaultDirectory.getAbsolutePath());  
        }

        
        // Save Other Parameters    
        sourceParameterList.saveParameters();
        parameterList.saveParameters();
        
        if (decoyRegexTextField.isEnabled()) {
            ArrayList<String> regexArrayList = readRegexArray(decoyRegexTextField.getText());
            writeRegexArray(regexArrayList);
        }

        return true;

    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

    @Override
    protected boolean defaultCalled() {
        ParameterList parameterList = (ParameterList) parserComboBox.getSelectedItem();
        parameterList.initDefaults();

        return false;
    }

    private boolean checkParameters() {
        
        // check files selected
        int nbFiles = fileList.getModel().getSize();
        if (nbFiles == 0) {
            setStatus(true, "You must select a file to import.");
            highlight(fileList);
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
        parserComboBox.setSelectedIndex(parserIndex);
        
        
        // Prefered User File Path
        String filePath = preferences.get("UserIdentificationFilePath", null);
        if (filePath != null) {
            File f = new File(filePath);
            if (f.isDirectory()) {
                defaultDirectory = f;
            }
        } else {
            // User Server File Path for Prefered User File Path
            String serverFilePath = preferences.get("ServerIdentificationFilePath", null);
            if (serverFilePath != null) {
                File f = new File(serverFilePath);
                if (f.isDirectory()) {
                    defaultDirectory = f;
                }
            }
        }
    }


    
    public File[] getFilePaths() {
        
        DefaultListModel model = ((DefaultListModel) fileList.getModel());
        int nbFiles = model.getSize();
        File[] filePaths = new File[nbFiles];
        for (int i=0;i<nbFiles;i++) {
            filePaths[i] =  ((File)model.getElementAt(i));
        }

        return filePaths;
    }
    
    public HashMap<String, String> getParserArguments() {
        ParameterList parameterList = (ParameterList) parserComboBox.getSelectedItem();
        return parameterList.getValues();
    }

    public Integer getInstrumentId() {
        
        InstrumentConfiguration instrument = (InstrumentConfiguration) sourceParameterList.getParameter("instrument").getObjectValue(); 
        return instrument.getId();
    }
    
    public Integer getPeaklistSoftwareId() {
        PeaklistSoftware peaklistSoftware = (PeaklistSoftware) sourceParameterList.getParameter("peaklist_software").getObjectValue();  
        return peaklistSoftware.getId();
    }
    
    public String getParserId() {
        return PARSER_IDS[parserComboBox.getSelectedIndex()];
    }

    public String getDecoyRegex() {
        if (decoyRegexTextField.isEnabled()) {
            return decoyRegexTextField.getText();
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
        
        parameterList.add(new FileParameter("usermod.xml.file", "Usermods file path", JTextField.class, "", "Usermods XML File", "xml"));
        parameterList.add(new BooleanParameter("fasta.contains.target", "Fasta contains target entries", JCheckBox.class, Boolean.TRUE));
        parameterList.add(new BooleanParameter("fasta.contains.decoy", "Fasta contains decoy entries", JCheckBox.class, Boolean.TRUE));

        
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
        
        instrumentsComboBox = new JComboBox(UDSDataManager.getUDSDataManager().getInstrumentsWithNullArray());
        ObjectParameter<InstrumentConfiguration> instrumentParameter = new ObjectParameter<>("instrument", "Instrument", instrumentsComboBox, UDSDataManager.getUDSDataManager().getInstrumentsWithNullArray(), null, -1, instrumentToString);
        parameterList.add(instrumentParameter);
        
        peaklistSoftwaresComboBox = new JComboBox(UDSDataManager.getUDSDataManager().getPeaklistSoftwaresWithNullArray());
        ObjectParameter<PeaklistSoftware> peaklistParameter = new ObjectParameter("peaklist_software", "Peaklist Software", peaklistSoftwaresComboBox, UDSDataManager.getUDSDataManager().getPeaklistSoftwaresWithNullArray(), null, -1, softwareToString);
        parameterList.add(peaklistParameter);
        
        decoyComboBox = new JComboBox(DECOY_VALUES);
        ObjectParameter<String> decoyParameter = new ObjectParameter<>("decoy_accession", "Decoy", decoyComboBox, DECOY_VALUES, DECOY_VALUES_ASSOCIATED_KEYS, 0, null);
        parameterList.add(decoyParameter);

        decoyRegexTextField = new JTextField(20);
        decoyRegexParameter = new StringParameter("decoy_accession", "Decoy Accession", decoyRegexTextField, "", new Integer(2), null);
        decoyRegexParameter.setUsed(false);
        parameterList.add(decoyRegexParameter);
        
        
        return parameterList;
        
    }
    
    /**
     * Class used to select Regex previously used
     */
    public static class SelectRegexDialog extends DefaultDialog {
        
        private static SelectRegexDialog selectRegexSingletonDialog = null;
        
        private JList<String> regexList;
        private JScrollPane regexListScrollPane;
        private JButton removeRegexButton;
        
        
        private String selectedRegex = null;
        private ArrayList<String> regexArrayList = null;
        
        public static SelectRegexDialog getDialog(JDialog parent, ArrayList<String> regexArrayList) {
            if (selectRegexSingletonDialog == null) {
                selectRegexSingletonDialog = new SelectRegexDialog(parent);
            }
            selectRegexSingletonDialog.initData(regexArrayList);
            
            
            return selectRegexSingletonDialog;
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
            this.regexArrayList = regexArrayList;
            
            DefaultListModel<String> model = (DefaultListModel<String>) regexList.getModel();
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
            
            regexList = new JList<>(new DefaultListModel<String>());
            
            // double clicking on regex -> select it and click on ok button
            regexList.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        selectRegexSingletonDialog.doClick(DefaultDialog.BUTTON_OK);
                    }
                }
            });
            
            regexListScrollPane = new JScrollPane(regexList) {
                
                private Dimension preferredSize = new Dimension(280, 140);
                
                @Override
                public Dimension getPreferredSize() {
                    return preferredSize;
                }
            };
            
            
            removeRegexButton = new JButton(IconManager.getIcon(IconManager.IconType.ERASER));
            removeRegexButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

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
            regexSelectionPanel.add(regexListScrollPane, c);
            
            
            c.gridx++;
            c.gridheight = 1;
            c.weightx = 0;
            c.weighty = 0;
            regexSelectionPanel.add(removeRegexButton, c);
            
            c.gridy++;
            regexSelectionPanel.add(Box.createVerticalStrut(30), c);


            // Actions on objects

            regexList.addListSelectionListener(new ListSelectionListener() {
                
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    boolean sometingSelected = (regexList.getSelectedIndex() != -1);
                    removeRegexButton.setEnabled(sometingSelected);
                }
            });
            
            
            
            removeRegexButton.addActionListener(new ActionListener() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    List<String> selectedValues = regexList.getSelectedValuesList();
                    Iterator<String> it = selectedValues.iterator();
                    while (it.hasNext()) {
                        ((DefaultListModel) regexList.getModel()).removeElement(it.next());
                    }
                    removeRegexButton.setEnabled(false);
                }
            });
            
            return regexSelectionPanel;
            
        }
        
        @Override
        protected boolean okCalled() {
            selectedRegex = regexList.getSelectedValue();
            
            DefaultListModel<String> model = (DefaultListModel<String>) regexList.getModel();
            int size = model.getSize();
            if (regexArrayList == null) {
                regexArrayList = new ArrayList<>(size);
            } else {
                regexArrayList.clear();
            }
            for (int i=0;i<size;i++) {
                regexArrayList.add(model.elementAt(i));
            }

            return true;
        }
        
        @Override
        protected boolean cancelCalled() {
            selectedRegex = null;
            return true;
        }


        public String getSelectedRegex() {
            return selectedRegex;
        }
        
        public ArrayList<String> getRegexArrayList() {
            return regexArrayList;
        }
    }
}
