/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.wizard;

import fr.profi.mzdb.io.writer.mgf.DefaultPrecursorComputer;
import fr.profi.mzdb.io.writer.mgf.IPrecursorComputation;
import fr.profi.mzdb.io.writer.mgf.IsolationWindowPrecursorExtractor;
import fr.profi.mzdb.io.writer.mgf.PrecursorMzComputationEnum;
import fr.proline.mzscope.ui.MgfExportParameters;
import fr.proline.mzscope.ui.ScanHeaderType;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.AbstractParameter;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.FileParameter;
import fr.proline.studio.parameter.FloatParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.rsmexplorer.gui.dialog.FileDialogInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.util.NbPreferences;

/**
 *
 * @author AK249877
 */
public class ExportMgfDialog extends DefaultDialog implements FileDialogInterface {

    private static ExportMgfDialog m_singleton = null;

    public static final String MZ_TOLERANCE_KEY = "MZ_TOLERANCE_KEY";
    public static final String MZ_TOLERANCE_NAME = "M/z tolerance (ppm)";

    public static final String INTENSITY_CUTOFF_KEY = "INTENSITY_CUTOFF_KEY";
    public static final String INTENSITY_CUTOFF_NAME = "Intensity cutoff";

    public static final String MZ_PRECURSOR_METHOD_KEY = "MZ_PRECURSOR_METHOD_KEY";
    public static final String MZ_PRECURSOR_METHOD_NAME = "Precursor m/z computation method";

    public static final String EXPORT_TITLE_KEY = "EXPORT_TITLE_KEY";
    public static final String EXPORT_TITLE_NAME = "Export proline title";

    public static final String EXPORT_OUTPUT_KEY = "EXPORT_OUTPUT_KEY";
    public static final String EXPORT_OUTPUT_NAME = "Select directory";

    public static final String EXPORT_IN_DIRECTORY_KEY = "EXPORT_IN_DIRECTORY_KEY";
    public static final String EXPORT_IN_DIRECTORY_NAME = "Export in specified directory";

    public static final String EXPORT_IN_PARENT_DIR_KEY = "EXPORT_IN_PARENT_DIR_KEY";
    public static final String EXPORT_IN_PARENT_DIR_NAME = "Export in .mzdb file's directory";

    private FloatParameter m_mzToleranceParam;
    private FloatParameter m_intensityCutoffParam;
    private BooleanParameter m_prolineTitleParam;
    private BooleanParameter m_exportInDirectoryParam;
    private BooleanParameter m_exportInParentParam;

    private FileParameter m_outputParam;

    private JComboBox m_precursorMethod;

    private JCheckBox m_exportProlineTitle;

    private JRadioButton m_exportInDirectoryButton;
    private JRadioButton m_exportInParentDirParam;

    private final String[] m_scanHeaderList;
    private final Map<Integer, ScanHeaderType> m_mapScanHeader;

    private final String[] m_precursorList;

    private static final String ISOLATION_WINDOW_PRECURSOR = "Proline refined precursor mz";

    private JButton m_addFileButton, m_removeFileButton;

    private JScrollPane m_fileListScrollPane;

    private static JList m_fileList;

    private String m_lastParentDirectory;

    private ParameterList m_exportParameterList;
    private ParameterList m_outputParameterList;

    public static final String MGF_EXPORT_NAME = "Mgf Export Parameters";

    public static final String MGF_OUTPUT_NAME = "Mgf Output Parameters";
    public static final String MGF_OUTPUT_KEY = "Mgf_Output_Parameters";

    private Preferences m_preferences;

    public static ExportMgfDialog getDialog(Window parent, String title) {
        if (m_singleton == null) {
            m_singleton = new ExportMgfDialog(parent, title);
        }
        return m_singleton;
    }

    private ExportMgfDialog(Window parent, String title) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle(title);

        setDocumentationSuffix(null);

        m_preferences = NbPreferences.root();

        EnumSet<PrecursorMzComputationEnum> precursorSet = EnumSet.allOf(PrecursorMzComputationEnum.class);
        m_precursorList = new String[precursorSet.size() + 1];
        int i = 0;
        for (PrecursorMzComputationEnum p : precursorSet) {
            m_precursorList[i] = p.getUserParamName();
            i++;
        }

        m_precursorList[i] = ISOLATION_WINDOW_PRECURSOR;
        EnumSet<ScanHeaderType> scanHeaderSet = EnumSet.allOf(ScanHeaderType.class);
        m_scanHeaderList = new String[scanHeaderSet.size()];
        m_mapScanHeader = new HashMap();
        i = 0;
        for (ScanHeaderType s : scanHeaderSet) {
            m_scanHeaderList[i] = s.getName();
            m_mapScanHeader.put(i, s);
            i++;
        }

        setInternalComponent(createInternalPanel());
        setButtonName(BUTTON_OK, "Export");

    }

    @Override
    public void setFiles(ArrayList<File> files) {
        ((DefaultListModel) m_fileList.getModel()).clear();
        if (files.size() > 0) {
            for (File f : files) {
                ((DefaultListModel) m_fileList.getModel()).addElement(f);
            }
            m_lastParentDirectory = files.get(0).getParentFile().getAbsolutePath();
        }
    }

    private JPanel createInternalPanel() {
        JPanel internalPanel = new JPanel();

        internalPanel.setLayout(new BorderLayout());
        internalPanel.add(createOutputParameterPanel(), BorderLayout.NORTH);
        internalPanel.add(createFileSelectionPanel(), BorderLayout.CENTER);
        internalPanel.add(createParametersPanel(), BorderLayout.SOUTH);

        return internalPanel;
    }

    private JPanel createOutputParameterPanel() {

        m_outputParameterList = new ParameterList(MGF_OUTPUT_NAME);

        ButtonGroup group = new ButtonGroup();

        m_exportInParentDirParam = new JRadioButton(EXPORT_IN_PARENT_DIR_NAME);
        m_exportInParentDirParam.setHorizontalTextPosition(JRadioButton.RIGHT);
        m_exportInParentParam = new BooleanParameter(EXPORT_IN_PARENT_DIR_KEY, "", m_exportInParentDirParam, Boolean.FALSE);
        m_outputParameterList.add(m_exportInParentParam);
        group.add(m_exportInParentDirParam);

        m_exportInDirectoryButton = new JRadioButton(EXPORT_IN_DIRECTORY_NAME);
        m_exportInDirectoryButton.setHorizontalTextPosition(JRadioButton.RIGHT);
        m_exportInDirectoryButton.setSelected(true);
        m_exportInDirectoryParam = new BooleanParameter(EXPORT_IN_DIRECTORY_KEY, EXPORT_IN_DIRECTORY_NAME, m_exportInDirectoryButton, Boolean.TRUE);
        m_outputParameterList.add(m_exportInDirectoryParam);
        group.add(m_exportInDirectoryButton);

        m_outputParam = new FileParameter(null, EXPORT_OUTPUT_KEY, EXPORT_OUTPUT_NAME, JTextField.class, "", null, null);
        m_outputParam.forceShowLabel(AbstractParameter.LabelVisibility.NO_VISIBLE);
        m_outputParam.setAllFiles(false);
        m_outputParam.setSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        m_outputParam.setDefaultDirectory(new File(NbPreferences.root().get(EXPORT_OUTPUT_KEY + "." + EXPORT_OUTPUT_KEY, System.getProperty("user.home"))));

        m_outputParameterList.add(m_outputParam);

        AbstractLinkedParameters linkedParameters = new AbstractLinkedParameters(m_outputParameterList) {

            @Override
            public void valueChanged(String value, Object associatedValue) {
                showParameter(m_outputParam, m_exportInDirectoryButton.isSelected(), m_preferences.get("mzDB_Settings.LAST_MZDB_PATH", System.getProperty("user.home")));
                updateParameterListPanel();
            }

        };

        m_exportInParentParam.addLinkedParameters(linkedParameters);
        m_exportInDirectoryParam.addLinkedParameters(linkedParameters);

        linkedParameters.valueChanged(m_exportInParentParam.getStringValue(), m_exportInParentParam.getObjectValue());

        m_outputParameterList.getPanel().setBorder(BorderFactory.createTitledBorder("Output directory"));

        return m_outputParameterList.getPanel();
    }

    private JPanel createParametersPanel() {
        m_exportParameterList = new ParameterList(MGF_EXPORT_NAME);

        m_mzToleranceParam = new FloatParameter(MZ_TOLERANCE_KEY, MZ_TOLERANCE_NAME, JTextField.class, 10.0f, 1.0f, 100.0f);
        m_exportParameterList.add(m_mzToleranceParam);

        m_precursorMethod = new JComboBox(m_precursorList);
        ObjectParameter precursorMethodParameter = new ObjectParameter(MZ_PRECURSOR_METHOD_KEY, MZ_PRECURSOR_METHOD_NAME, m_precursorMethod, m_precursorList, m_precursorList, 2, null);
        m_exportParameterList.add(precursorMethodParameter);

        m_intensityCutoffParam = new FloatParameter(INTENSITY_CUTOFF_KEY, INTENSITY_CUTOFF_NAME, JTextField.class, 0.0f, 0.0f, 1000000.0f);
        m_exportParameterList.add(m_intensityCutoffParam);

        m_exportProlineTitle = new JCheckBox("Export Proline Title");
        m_prolineTitleParam = new BooleanParameter(EXPORT_TITLE_KEY, EXPORT_TITLE_NAME, m_exportProlineTitle, Boolean.FALSE);
        m_exportParameterList.add(m_prolineTitleParam);

        m_exportParameterList.loadParameters(m_preferences);

        m_exportParameterList.getPanel().setBorder(BorderFactory.createTitledBorder("Export Parameters"));

        return m_exportParameterList.getPanel();
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

                String initializationDirectory = m_preferences.get("mzDB_Settings.LAST_MZDB_PATH", System.getProperty("user.home"));

                File f = new File(initializationDirectory);
                if (!(f.exists() && f.isDirectory())) {
                    initializationDirectory = System.getProperty("user.home");
                }

                JFileChooser fchooser = new JFileChooser(initializationDirectory);

                fchooser.setMultiSelectionEnabled(true);

                fchooser.addChoosableFileFilter(new FileNameExtensionFilter(".mzdb", "mzDB"));
                fchooser.setAcceptAllFileFilterUsed(false);

                //put the one and only filter here! (.mzdb)
                int result = fchooser.showOpenDialog(m_singleton);
                if (result == JFileChooser.APPROVE_OPTION) {

                    File[] files = fchooser.getSelectedFiles();
                    int nbFiles = files.length;
                    for (int i = 0; i < nbFiles; i++) {
                        ((DefaultListModel) m_fileList.getModel()).addElement(files[i]);
                    }

                    if (files.length > 0) {
                        m_lastParentDirectory = files[0].getParentFile().getAbsolutePath();
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

    @Override
    protected boolean okCalled() {

        if (!validateParameters()) {
            return false;
        }

        m_outputParameterList.saveParameters(m_preferences);
        m_exportParameterList.saveParameters(m_preferences);

        m_preferences.put("mzDB_Settings.LAST_MZDB_PATH", m_lastParentDirectory);

        HashMap<File, MgfExportSettings> mzdbFiles = new HashMap<File, MgfExportSettings>();
        for (int i = 0; i < m_fileList.getModel().getSize(); i++) {

            File f = (File) m_fileList.getModel().getElementAt(i);

            MgfExportParameters exportParameters = new MgfExportParameters(buildPrecursorComputer(), Float.parseFloat(m_mzToleranceParam.getStringValue()), Float.parseFloat(m_intensityCutoffParam.getStringValue()), m_exportProlineTitle.isSelected());

            String exportPath = ((boolean) m_exportInParentParam.getObjectValue()) ? f.getParentFile().getAbsolutePath() : m_outputParam.getStringValue();

            MgfExportSettings exportSettings = new MgfExportSettings(exportPath, exportParameters);
            mzdbFiles.put((File) m_fileList.getModel().getElementAt(i), exportSettings);
        }

        MgfExportBatch exportBatch = new MgfExportBatch(mzdbFiles);
        Thread thread = new Thread(exportBatch);
        thread.start();

        DefaultListModel listModel = (DefaultListModel) m_fileList.getModel();
        listModel.removeAllElements();

        return true;

    }

    private IPrecursorComputation buildPrecursorComputer() {
        IPrecursorComputation precComp = null;
        String item = (String) m_precursorMethod.getSelectedItem();
        if (item.equals(ISOLATION_WINDOW_PRECURSOR)) {
            precComp = new IsolationWindowPrecursorExtractor(Float.parseFloat(m_mzToleranceParam.getStringValue()));
        }
        EnumSet<PrecursorMzComputationEnum> precursorSet = EnumSet.allOf(PrecursorMzComputationEnum.class);
        for (PrecursorMzComputationEnum p : precursorSet) {
            if (item.equals(p.getUserParamName())) {
                precComp = new DefaultPrecursorComputer(p, Float.parseFloat(m_intensityCutoffParam.getStringValue()));
                break;
            }
        }
        return precComp;
    }

    private boolean validateParameters() {

        if (m_fileList.getModel().getSize() == 0) {
            setStatus(true, "No files are selected.");
            highlight(m_fileList);
            return false;
        }

        ParameterError error = m_exportParameterList.checkParameters();
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }

        return true;
    }

}
