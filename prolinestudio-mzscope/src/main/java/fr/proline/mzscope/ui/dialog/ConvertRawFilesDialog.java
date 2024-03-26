package fr.proline.mzscope.ui.dialog;

import fr.proline.studio.NbPreferences;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class ConvertRawFilesDialog extends DefaultDialog {

  private JList<File> m_fileList;
  private JButton m_removeFileButton;
  private File m_converterExe;
  private String m_lastDirectory;
  private JTextField m_converterExeTF;
  private JTextField m_converterOptionTF;
  private final static String LAST_DIR = "mzscope.last.raw.directory";
  private final static String LAST_CONVERTER = "mzscope.converter.exe.path";
  private final static String LAST_CONVERTER_OPTION = "mzscope.converter.option";


  public ConvertRawFilesDialog(Window parent) {
    super(parent, Dialog.ModalityType.APPLICATION_MODAL);

    setTitle("Convert Raw Files");
//    setDocumentationSuffix("id.147n2zr");

    setResizable(true);
    setMinimumSize(new Dimension(200, 240));

    initInternalPanel();
  }

  public List<File> getFilePaths() {
    ListModel<File> model = m_fileList.getModel();
    int nbFiles = model.getSize();
    List<File> files = new ArrayList<>();
    for (int i = 0; i < nbFiles; i++) {
      files.add(model.getElementAt(i));
    }
    return files;
  }

  public File getConverterExeFile(){
    return m_converterExe;
  }

  public String getConverterOption(){
    return m_converterOptionTF.getText().trim();
  }

  private void initInternalPanel() {

    JPanel internalPanel = new JPanel();
    internalPanel.setLayout(new java.awt.GridBagLayout());

    // create fileSelectionPanel
    JPanel fileSelectionPanel = createFileSelectionPanel();
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.BOTH;
    c.insets = new java.awt.Insets(5, 5, 5, 5);

    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.gridwidth=3;
    internalPanel.add(fileSelectionPanel, c);

    JLabel converterLabel = new JLabel("Converter (.exe)");
    c.gridy++;
    c.weighty = 0;
    c.weightx = 0.2;
    c.gridwidth=1;
    internalPanel.add(converterLabel, c);

    c.gridx++;
    c.weightx = 0.4;
    m_converterExeTF = new JTextField();
    Preferences prefs = NbPreferences.root();
    String rawConverterPath = prefs.get(LAST_CONVERTER, "");
    if(!StringUtils.isEmpty(rawConverterPath)){
      m_converterExe = new File(rawConverterPath);
    }
    m_converterExeTF.setText(rawConverterPath);
    m_converterExeTF.setEditable(false);
    internalPanel.add(m_converterExeTF, c);

    c.gridx++;
    c.weightx = 0.4;
    final JButton addFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
    addFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
    addFileButton.addActionListener(e -> {

      JFileChooser fchooser = new JFileChooser();
      fchooser.setMultiSelectionEnabled(false);

      if (!StringUtils.isEmpty(rawConverterPath )) {
        fchooser.setCurrentDirectory(new File(rawConverterPath));
      }
      fchooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      FileNameExtensionFilter filter = new FileNameExtensionFilter("Converter (.exe)","exe");
      fchooser.setFileFilter(filter);
      fchooser.setAcceptAllFileFilterUsed(false);

      int result = fchooser.showOpenDialog(addFileButton);
      if (result == JFileChooser.APPROVE_OPTION) {
        File file = fchooser.getSelectedFile();
        m_converterExe = file;
        m_converterExeTF.setText(file.getPath());
      }
    });
    internalPanel.add(addFileButton, c);

    JLabel converterOptionLabel = new JLabel("Converter Options");
    c.gridx = 0;
    c.gridy++;
    c.weighty = 0;
    c.weightx = 0.3;
    c.gridwidth=1;
    internalPanel.add(converterOptionLabel, c);

    c.gridx++;
    c.weightx = 0.7;
    c.gridwidth=2;

    m_converterOptionTF = new JTextField();
    String rawConverterOption = prefs.get(LAST_CONVERTER_OPTION, "");
    m_converterOptionTF.setText(rawConverterOption);
    internalPanel.add(m_converterOptionTF, c);

    setInternalComponent(internalPanel);

  }


  private JPanel createFileSelectionPanel() {

    // Creation of Objects for File Selection Panel
    JPanel fileSelectionPanel = new JPanel(new GridBagLayout());
    fileSelectionPanel.setBorder(BorderFactory.createTitledBorder(" Files Selection "));

    m_fileList = new JList<>(new DefaultListModel<>());
    JScrollPane m_fileListScrollPane = new JScrollPane(m_fileList) {

      private Dimension preferredSize = new Dimension(360, 200);

      @Override
      public Dimension getPreferredSize() {
        return preferredSize;
      }
    };

    JButton m_addFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
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
    m_fileList.addListSelectionListener(e -> {
      boolean sometingSelected = (m_fileList.getSelectedIndex() != -1);
      m_removeFileButton.setEnabled(sometingSelected);
    });

    m_addFileButton.addActionListener(e -> {
      setStatus(false, String.format("%d file(s)", m_fileList.getModel().getSize()));

      JFileChooser fchooser  = new JFileChooser();
      fchooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      fchooser.setDialogTitle("Convert Raw file");
//      fchooser.setAcceptAllFileFilterUsed(false);
      fchooser.setFileFilter(new RawFilter());
      Preferences prefs = NbPreferences.root();
      m_lastDirectory = prefs.get(LAST_DIR, fchooser.getCurrentDirectory().getAbsolutePath());
      fchooser.setCurrentDirectory(new File(m_lastDirectory));

      fchooser.setMultiSelectionEnabled(true);

      int result = fchooser.showOpenDialog(null);
      if (result == JFileChooser.APPROVE_OPTION) {
        m_lastDirectory =  fchooser.getCurrentDirectory().getAbsolutePath();
        File[] files = fchooser.getSelectedFiles();
        for (File file : files) {
          ((DefaultListModel<File>) m_fileList.getModel()).addElement(file);
        }
        setStatus(false, String.format("%d file(s)", m_fileList.getModel().getSize()));
      }
    });

    m_removeFileButton.addActionListener(e -> {
      List<File> selectedValues = m_fileList.getSelectedValuesList();
      for (File selectedValue : selectedValues) {
        ((DefaultListModel<File>) m_fileList.getModel()).removeElement(selectedValue);
      }
      setStatus(false, String.format("%d file(s)", m_fileList.getModel().getSize()));
      m_removeFileButton.setEnabled(false);
    });

    return fileSelectionPanel;
  }

  private boolean checkParametersForOK() {
    // check files selected
    int nbFiles = m_fileList.getModel().getSize();
    if (nbFiles == 0) {
      setStatus(true, "You must select a file to convert.");
      highlight(m_fileList);
      return false;
    }
    if(StringUtils.isEmpty(m_converterExeTF.getText())){
      setStatus(true, "You must specify path to raw2mzdb converter .exe file.");
      highlight(m_converterExeTF);
      return false;
    }
    if(!m_converterExe.exists()){
      setStatus(true, "Specify to raw2mzdb converter .exe file doesn't exist.");
      highlight(m_converterExeTF);
      return false;
    }
    return  true;
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

  protected  void saveParameters(Preferences preferences) {
    preferences.put(LAST_CONVERTER, m_converterExe.getAbsolutePath());
    preferences.put(LAST_DIR, m_lastDirectory);
    preferences.put(LAST_CONVERTER_OPTION, getConverterOption());
  }

}
