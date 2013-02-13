package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.uds.Instrument;
import fr.proline.core.orm.uds.PeaklistSoftware;
import fr.proline.studio.dam.UDSDataManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.*;
import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
    
    
    private JList<File> fileList;
    private JScrollPane fileListScrollPane;

    private JButton addFileButton;
    private JButton removeFileButton;
    

    private JComboBox parserComboBox;
    private int previousParserIndex = -1;
    private ParameterList sourceParameterList;

    
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


        setTitle("Import Identifications");
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

        fileList = new JList<File>(new DefaultListModel());
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

        JLabel parserLabel = new JLabel("Parser :");
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
        c.weightx = 1;
        parserPanel.add(parserComboBox, c);

        sourceParameterList = createSourceParameters();
        sourceParameterList.completePanel(parserPanel, c);

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
        
        return parserPanel;
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
    }

    @Override
    protected boolean okCalled() {


        ParameterList parameterList = (ParameterList) parserComboBox.getSelectedItem();
        
        // check global parameters
        if (!checkParameters()) {
            return false;
        }
        
        // check specific parameters
        ParameterError error = parameterList.checkParameters();
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }
               
        // retrieve values
        //HashMap<String, String> values = parameterList.getValues();

        
        // save parser
        String parserSelected = parameterList.toString();
        Preferences preferences = NbPreferences.root();
        preferences.put("IdentificationParser", parserSelected);
        
        // save file path
        if (defaultDirectory != null) {
          preferences.put("IdentificationFilePath", defaultDirectory.getAbsolutePath());  
        }

        
        // Save Other Parameters        
        parameterList.saveParameters();

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
        
        
        // Prefered File Path
        String filePath = preferences.get("IdentificationFilePath", null);
        if (filePath != null) {
            File f = new File(filePath);
            if (f.isDirectory()) {
                defaultDirectory = f;
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
        
        Instrument instrument = (Instrument) sourceParameterList.getParameter("instrument").getObjectValue(); 
        return instrument.getId();
    }
    
    public Integer getPeaklistSoftwareId() {
        PeaklistSoftware peaklistSoftware = (PeaklistSoftware) sourceParameterList.getParameter("peaklist_software").getObjectValue();  
        return peaklistSoftware.getId();
    }
    
    public String getParserId() {
        return PARSER_IDS[parserComboBox.getSelectedIndex()];
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
        
        AbstractParameterToString<Instrument> instrumentToString = new AbstractParameterToString<Instrument>() {
            @Override
            public String toString(Instrument o) {
                return o.getName();
            }  
        };
        
        AbstractParameterToString<PeaklistSoftware> softwareToString = new AbstractParameterToString<PeaklistSoftware>() {
            @Override
            public String toString(PeaklistSoftware o) {
                return o.getName();
            }  
        };
        
        parameterList.add(new ObjectParameter("instrument", "Instrument", UDSDataManager.getUDSDataManager().getInstrumentsArray(), -1, instrumentToString));
        parameterList.add(new ObjectParameter("peaklist_software", "Peaklist Software", UDSDataManager.getUDSDataManager().getPeaklistSoftwaresArray(), -1, softwareToString));

        return parameterList;
        
    }
    
    
}
