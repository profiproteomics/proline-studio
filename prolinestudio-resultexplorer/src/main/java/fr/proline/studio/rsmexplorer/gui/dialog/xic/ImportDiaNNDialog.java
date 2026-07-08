package fr.proline.studio.rsmexplorer.gui.dialog.xic;

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
import fr.proline.studio.parameter.AbstractParameterToString;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.utils.IconManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;

public class ImportDiaNNDialog extends DefaultStorableDialog {

  private final static String SETTINGS_KEY = "ImportDiaNNResult";
  private final static String PREFERENCE_PATH_KEY = "DefaultImportDiaNNPath";
  private static final String[] FILTER_MODES = {"No filtering", "DiaNN MBR filtering", "DiaNN noMBR filtering"};
  private static final String[] FILTER_MODES_KEYS = {"NONE", "MBR", "NOMBR"};
  private static ImportDiaNNDialog m_singletonDialog;

  private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

  //parameter for import
  private ParameterList m_importParameterList;
  private JComboBox<InstrumentConfiguration> m_instrumentsComboBox = null;
  private JComboBox<PeaklistSoftware> m_peaklistSoftwaresComboBox = null;
  private JComboBox<String> m_filterModeComboBox = null;
  private JTextField m_filepathTF = null;

  private boolean m_rootPathError = false;
  private ArrayList<String> m_rootPaths;
  private ServerFile m_defaultImportDiaNNPath;
  private File m_file2Import;


  private ImportDiaNNDialog(Window parent){
    super(parent, Dialog.ModalityType.APPLICATION_MODAL);

    setTitle("Import DiaNN Results");

//    setDocumentationSuffix("h.1tuee74");
    setResizable(true);
    setMinimumSize(new Dimension(200, 240));

    initInternalPanel();

    restoreInitialParameters(NbPreferences.root());
  }

  public static ImportDiaNNDialog getDialog(Window parent){
    if (m_singletonDialog == null) {
      m_singletonDialog = new ImportDiaNNDialog(parent);
    }

    m_singletonDialog.reinitParams();

    return m_singletonDialog;
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
    c.weightx = 0.8;
    c.weighty = 0.8;
    internalPanel.add(fileSelectionPanel, c);

    c.gridy++;
    c.weighty = 0;
    internalPanel.add(allParametersPanel, c);

    setInternalComponent(internalPanel);
  }

  private JPanel createFileSelectionPanel() {

    // Creation of Objects for File Selection Panel
    JPanel fileSelectionPanel = new JPanel(new GridBagLayout());
    fileSelectionPanel.setBorder(BorderFactory.createTitledBorder(" File Selection "));

    // Placement of Objects for File Selection Panel
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new java.awt.Insets(5, 5, 5, 5);

    JLabel fileChooselabel = new JLabel("Select DiaNN result directory to import");
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1.0;
    c.weighty = 1.0;
    fileSelectionPanel.add(fileChooselabel, c);

    m_filepathTF = new JTextField("<select path>", 20);
    m_filepathTF.setEditable(false);
    c.gridy++;
    fileSelectionPanel.add(m_filepathTF, c);
    JButton addFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
    addFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
    c.gridx++;
    c.gridheight = 1;
    c.weightx = 0;
    c.weighty = 0;
    fileSelectionPanel.add(addFileButton, c);

    c.gridy++;
    fileSelectionPanel.add(Box.createVerticalStrut(30), c);


    addFileButton.addActionListener(event -> {

      if (m_rootPathError) {
        LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Server has returned no Root Path for Result Files. There is a problem with the server installation, please contact your administrator.");

        InfoDialog errorDialog = new InfoDialog(m_singletonDialog, InfoDialog.InfoType.WARNING, "Root Path Error", "Server has returned no Root Path for Result Files.\nThere is a problem with the server installation, please contact your administrator.", true);
        errorDialog.setButtonVisible(DefaultDialog.BUTTON_CANCEL, false);
        errorDialog.setLocationRelativeTo(m_singletonDialog);
        errorDialog.setVisible(true);
        return;
      }

      JFileChooser fchooser;
      if ((m_defaultImportDiaNNPath != null) && (m_defaultImportDiaNNPath.isDirectory())) {
        fchooser = new JFileChooser(m_defaultImportDiaNNPath, ServerFileSystemView.getServerFileSystemView());
      } else {
        // should not happen in fact
        fchooser = new JFileChooser(ServerFileSystemView.getServerFileSystemView());
      }
      fchooser.setMultiSelectionEnabled(false);
      fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int result = fchooser.showOpenDialog(m_singletonDialog);
      if (result == JFileChooser.APPROVE_OPTION) {
        File file = fchooser.getSelectedFile();
        String path = file.getPath();
        for(String serverRoot : m_rootPaths){
          if(path.startsWith(serverRoot) && path.length() > (serverRoot.length()+1))
            path = path.substring(serverRoot.length()+1);
        }
        ServerFile sFile = new ServerFile(path, file.getName(), true,0,0);
        m_filepathTF.setText(sFile.getPath());
        m_file2Import = sFile;
      }
    });

    return fileSelectionPanel;
  }

  private JPanel createAllParametersPanel() {
    JPanel allParametersPanel = new JPanel(new GridBagLayout());
    allParametersPanel.setBorder(BorderFactory.createTitledBorder(" Parameters "));

    m_importParameterList = createSourceParameters();
    m_importParameterList.updateValues(NbPreferences.root());


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
    JLabel peaklistSoftwareLabel = new JLabel("Peaklist Software :");
    peaklistSoftwareLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    allParametersPanel.add(peaklistSoftwareLabel, c);

    c.gridx++;
    c.gridwidth = 2;
    c.weightx = 1;
    allParametersPanel.add(m_peaklistSoftwaresComboBox, c);

    c.gridx = 0;
    c.gridwidth = 1;
    c.weightx = 0;
    c.gridy++;
    JLabel filterModeLabel = new JLabel("Filtering Mode :");
    filterModeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    allParametersPanel.add(filterModeLabel, c);

    c.gridx++;
    c.gridwidth = 2;
    c.weightx = 1;
    allParametersPanel.add(m_filterModeComboBox, c);

    return allParametersPanel;
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

    m_instrumentsComboBox = new JComboBox<>(DatabaseDataManager.getDatabaseDataManager().getInstrumentsWithNullArray());
    final ObjectParameter<InstrumentConfiguration> instrumentParameter = new ObjectParameter<>("instrument", "Instrument", m_instrumentsComboBox, DatabaseDataManager.getDatabaseDataManager().getInstrumentsWithNullArray(), null, -1, instrumentToString);
    parameterList.add(instrumentParameter);
    m_instrumentsComboBox.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        instrumentParameter.setUsed(true);  //JPM.WART : found a better fix (parameters not saved if it has never been set)
      }
    });

    m_peaklistSoftwaresComboBox = new JComboBox<>(DatabaseDataManager.getDatabaseDataManager().getPeaklistSoftwaresWithNullArray());
    final ObjectParameter<PeaklistSoftware> peaklistParameter = new ObjectParameter("peaklist_software", "Peaklist Software", m_peaklistSoftwaresComboBox, DatabaseDataManager.getDatabaseDataManager().getPeaklistSoftwaresWithNullArray(), null, -1, softwareToString);
    parameterList.add(peaklistParameter);
    m_peaklistSoftwaresComboBox.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        peaklistParameter.setUsed(true);   //JPM.WART : found a better fix (parameters not saved if it has never been set)
      }
    });

    m_filterModeComboBox = new JComboBox<>(FILTER_MODES);
    final ObjectParameter<String> filterModeParameter = new ObjectParameter<>("filter_mode", "Filtering Mode", m_filterModeComboBox, FILTER_MODES, FILTER_MODES_KEYS, 0, null);
    parameterList.add(filterModeParameter);
    m_filterModeComboBox.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        filterModeParameter.setUsed(true);
      }
    });


    return parameterList;

  }


  private void reinitParams(){
    setStatus(false, "no file selected");

    //reinit PeaklistSoftware
    PeaklistSoftware[] allPS = DatabaseDataManager.getDatabaseDataManager().getPeaklistSoftwaresWithNullArray();
    String selectedPeaklistSoft = ((ObjectParameter<?>) m_importParameterList.getParameter("peaklist_software")).getStringValue();
    m_peaklistSoftwaresComboBox.removeAllItems();
    for (int i = 0; i < allPS.length; i++) {
      m_peaklistSoftwaresComboBox.addItem(allPS[i]);
    }
    ((ObjectParameter) m_importParameterList.getParameter("peaklist_software")).updateObjects(allPS);
    ((ObjectParameter<?>) m_importParameterList.getParameter("peaklist_software")).setValue(selectedPeaklistSoft);
    restoreInitialParameters(NbPreferences.root());
  }

  private void restoreInitialParameters(Preferences preferences){
    m_rootPaths = ServerFileSystemView.getServerFileSystemView().getLabels(RootInfo.TYPE_RESULT_FILES);
    if ((m_rootPaths == null) || (m_rootPaths.isEmpty())) {
      // check that the server has sent me at least one root path
      m_logger.error("Server has returned no Root Path for Result Files. There is a problem with the server installation, please contact your administrator.");
      m_rootPathError = true;
      return;
    } else {
      m_rootPathError = false;
    }

    String filePath = preferences.get(PREFERENCE_PATH_KEY, null);
    if (filePath == null) {
      if (m_rootPaths.size() >= 1) {
        filePath = m_rootPaths.get(0);
      }
    }
    if (filePath != null) {
      ServerFile f = new ServerFile(filePath, filePath, true, 0, 0);
      if (f.isDirectory()) {
        m_defaultImportDiaNNPath = f;
      }
    }
  }

  public long getInstrumentId() {

    InstrumentConfiguration instrument = (InstrumentConfiguration) m_importParameterList.getParameter("instrument").getObjectValue();
    return instrument.getId();
  }

  public long getPeaklistSoftwareId() {
    PeaklistSoftware peaklistSoftware = (PeaklistSoftware) m_importParameterList.getParameter("peaklist_software").getObjectValue();
    return peaklistSoftware.getId();
  }

  public File getFile2Import(){
    return m_file2Import;
  }

  public String getFilterMode() {
    return ((ObjectParameter<?>) m_importParameterList.getParameter("filter_mode")).getStringValue();
  }

  // -- DefaultStorableDialog implementation methods

  @Override
  protected String getSettingsKey() {
    return  SETTINGS_KEY;
  }

  @Override
  protected void saveParameters(Preferences preferences)  {
    // save file path
    if (m_defaultImportDiaNNPath != null) {
      preferences.put(PREFERENCE_PATH_KEY, m_defaultImportDiaNNPath.getPath());
    }

    // Save Other Parameters
    m_importParameterList.saveParameters(preferences);

  }

  @Override
  protected boolean checkParameters() {
    // check source parameters
    ParameterError error = m_importParameterList.checkParameters();

    if(error == null && m_file2Import == null)
      error = new ParameterError("File should be specified", m_filepathTF) ;

    // report error
    if (error != null) {
      setStatus(true, error.getErrorMessage());
      highlight(error.getParameterComponent());
      return false;
    }

    return true;
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

    m_importParameterList.loadParameters(filePreferences);
  }

  @Override
  protected void resetParameters() throws Exception {
    m_importParameterList.initDefaults();
  }

  @Override
  protected boolean okCalled() {

    // check parameters
    if (!checkParameters()) {
      return false;
    }

    saveParameters(NbPreferences.root());

    return true;

  }


}
