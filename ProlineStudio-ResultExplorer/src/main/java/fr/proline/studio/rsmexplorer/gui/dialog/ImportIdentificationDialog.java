package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.uds.Instrument;
import fr.proline.studio.dam.InstrumentList;
import fr.proline.studio.dam.UDSConnectionManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;

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
    private JList<File> fileList;
    private JScrollPane fileListScrollPane;
    private JPanel fileSelectionPanel;
    private JComboBox instrumentComboBox;
    private JComboBox parserComboBox;
    private JPanel parserPanel;
    private JButton addFileButton;
    private JButton removeFileButton;
    private int specificParametersGridy;
    private HashMap<String, JTextField> specificParametersMap = new HashMap<>();
    private ArrayList<JComponent> specificParametersComponentList = new ArrayList<>();
    private int previousParserIndex = -1;
    private final static String[] PARSER_NAMES = {"Mascot", "Test"};
    private final static String[] FILE_EXTENSIONS = {"dat", "test"};
    private final static String[] FILE_EXTENSIONS_DESCRIPTION = {"Mascot Identification Result", "Test Identification"};
    private final static String[][] SPECIFIC_PARAMETERS_KEY = {
        {"ion.score.cutoff", "subset.threshold", "protein.cutoff.pvalue"},
        {"test"}
    };
    private final static String[][] SPECIFIC_PARAMETERS_NAME = {
        {"Ion Score Cutoff", "Subset Threshold", "Protein Cutoff Pvalue"},
        {"Test"}
    };
    private final static String[][] SEPECIFIC_PARAMETERS_DEFAULT = {
        {"0", "1", "????"},
        {"4"}
    };

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


    }

    private void initInternalPanel() {

        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new java.awt.GridBagLayout());

        // create fileSelectionPanel
        createFileSelectionPanel();

        // create parserPanel
        createParserPanel();


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
        internalPanel.add(parserPanel, c);

        restoreParser();

        setInternalComponent(internalPanel);
    }

    private void createFileSelectionPanel() {

        // Creation of Objects for File Selection Panel
        fileSelectionPanel = new JPanel(new GridBagLayout());
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



    }

    private void createParserPanel() {
        // Creation of Objects for the Parser Panel
        parserPanel = new JPanel(new GridBagLayout());
        parserPanel.setBorder(BorderFactory.createTitledBorder(" Parser Parameters "));

        JLabel parserLabel = new JLabel("Parser :");
        parserLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        parserComboBox = new JComboBox(PARSER_NAMES);
        JLabel instrumentLabel = new JLabel("Instrument :");
        instrumentLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        instrumentComboBox = new JComboBox(InstrumentList.getInstrumentList().getArray());
        instrumentComboBox.setRenderer(new InstrumentComboboxRenderer());

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

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        parserPanel.add(instrumentLabel, c);

        c.gridx++;
        c.weightx = 1;
        parserPanel.add(instrumentComboBox, c);

        specificParametersGridy = c.gridy + 1;

        parserComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int parserIndex = parserComboBox.getSelectedIndex();
                if (parserIndex == previousParserIndex) {
                    return;
                }

                previousParserIndex = parserIndex;

                initParserParameters(parserIndex);

                // resize the dialog
                repack();
                //parserPanel.revalidate();
            }
        });
    }

    private void initParserParameters(int parserIndex) {

        // --- Add graphical components for the parameters of the selected parser
        String[] keys = SPECIFIC_PARAMETERS_KEY[parserIndex];
        String[] names = SPECIFIC_PARAMETERS_NAME[parserIndex];

        // remove all parameters
        removeParameters();


        int nbKeys = keys.length;

        // check if the parameters have been already restored
        boolean needToRestore = false;
        if (nbKeys > 0) {
            String paramInParserKey = parserIndex + keys[0];
            needToRestore = (specificParametersMap.get(paramInParserKey) == null);
        }


        // add new parameters
        for (int i = 0; i < nbKeys; i++) {
            String paramInParserKey = parserIndex + keys[i];
            addParameter(i, names[i], paramInParserKey);
        }

        if (needToRestore) {
            restoreParameters(parserIndex);
        }
    }

    private void addParameter(int paramIndex, String labelName, String paramInParserKey) {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = specificParametersGridy + paramIndex;


        JLabel label = new JLabel(labelName + " :");
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        parserPanel.add(label, c);
        specificParametersComponentList.add(label);

        JTextField textField = specificParametersMap.get(paramInParserKey);
        if (textField == null) {
            textField = new JTextField();
            specificParametersMap.put(paramInParserKey, textField);
        }
        c.gridx++;
        parserPanel.add(textField, c);
        specificParametersComponentList.add(textField);
    }

    private void removeParameters() {
        int nb = specificParametersComponentList.size();
        for (int i = 0; i < nb; i++) {
            parserPanel.remove(specificParametersComponentList.get(i));
        }
        specificParametersComponentList.clear();
    }

    private void reinitialize() {
        ((DefaultListModel) fileList.getModel()).removeAllElements();
        removeFileButton.setEnabled(false);
    }

    @Override
    protected boolean okCalled() {

        int parserIndex = parserComboBox.getSelectedIndex();

        // check parameters
        // JPM.TODO

        // save parameters
        saveParserAndParameters(parserIndex);

        // do action
        //JPM.TODO


        return true;
    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

    @Override
    protected boolean defaultCalled() {
        int parserIndex = parserComboBox.getSelectedIndex();

        restoreDefaults(parserIndex);

        return false;
    }

    private void restoreDefaults(int parserIndex) {
        String[] defaultParameters = SEPECIFIC_PARAMETERS_DEFAULT[parserIndex];
        String[] parameterKeys = SPECIFIC_PARAMETERS_KEY[parserIndex];
        int nbParameters = parameterKeys.length;
        for (int i = 0; i < nbParameters; i++) {
            String parameterInParserKey = parserIndex + parameterKeys[i];
            JTextField f = specificParametersMap.get(parameterInParserKey);
            f.setText(defaultParameters[i]);
        }
    }

    private void restoreParser() {
        Preferences preferences = NbPreferences.forModule(ImportIdentificationDialog.class);
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

        // restore its parameters
        restoreParameters(parserIndex);

    }

    private void restoreParameters(int parserIndex) {

        Preferences preferences = NbPreferences.forModule(ImportIdentificationDialog.class);

        String parserName = PARSER_NAMES[parserIndex];
        String[] parametersName = SPECIFIC_PARAMETERS_NAME[parserIndex];


        String parserKey = parserName.replaceAll(" ", "_");



        boolean aParameterFound = false;
        int nbParameters = parametersName.length;
        String[] parameterValues = new String[nbParameters];
        for (int i = 0; i < nbParameters; i++) {
            String parameter = parametersName[i];
            String parameterKey = parserKey + "." + parameter.replaceAll(" ", "_");

            parameterValues[i] = preferences.get(parameterKey, null);
            if (parameterValues[i] != null) {
                aParameterFound = true;
            } else {
                parameterValues[i] = "";
            }
        }

        if (!aParameterFound) {
            // we retrieve default values
            restoreDefaults(parserIndex);
        } else {

            String[] parameterKeys = SPECIFIC_PARAMETERS_KEY[parserIndex];
            for (int i = 0; i < nbParameters; i++) {
                String parameterKey = parameterKeys[i];
                String parameterInParserKey = parserIndex + parameterKey;
                JTextField f = specificParametersMap.get(parameterInParserKey);
                f.setText(parameterValues[i]);
            }
        }

    }

    private void saveParserAndParameters(int parserIndex) {
        Preferences preferences = NbPreferences.forModule(ImportIdentificationDialog.class);

        // save the last user parser
        String parserName = PARSER_NAMES[parserIndex];
        preferences.put("IdentificationParser", parserName);

        // save the parameters of this parser
        String[] parametersName = SPECIFIC_PARAMETERS_NAME[parserIndex];

        String[] parameterKeys = SPECIFIC_PARAMETERS_KEY[parserIndex];
        String parserKey = parserName.replaceAll(" ", "_");
        int nbParameters = parametersName.length;
        for (int i = 0; i < nbParameters; i++) {
            String parameter = parametersName[i];
            String parameterKey = parserKey + "." + parameter.replaceAll(" ", "_");


            String parameterInParserKey = parserIndex + parameterKeys[i];
            JTextField f = specificParametersMap.get(parameterInParserKey);

            String parameterValue = f.getText();
            preferences.put(parameterKey, parameterValue);

        }

        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            LoggerFactory.getLogger(UDSConnectionManager.class).error("Saving UDS Connection Parameters Failed", e);
        }
    }

    public class InstrumentComboboxRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            Instrument instrument = (Instrument) value;
            l.setText(instrument.getName());

            return l;
        }
    }
}
