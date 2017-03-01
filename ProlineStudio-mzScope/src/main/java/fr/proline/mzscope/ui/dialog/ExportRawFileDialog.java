/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui.dialog;

import fr.profi.mzdb.io.writer.mgf.DefaultPrecursorComputer;
import fr.profi.mzdb.io.writer.mgf.IPrecursorComputation;
import fr.profi.mzdb.io.writer.mgf.PrecursorMzComputationEnum;
import fr.profi.mzdb.io.writer.mgf.IsolationWindowPrecursorExtractor;
import fr.proline.mzscope.model.IExportParameters;
import fr.proline.mzscope.model.IExportParameters.ExportType;
import fr.proline.mzscope.ui.model.MzScopePreferences;
import fr.proline.mzscope.ui.MgfExportParameters;
import fr.proline.mzscope.ui.ScanHeaderExportParameters;
import fr.proline.mzscope.ui.ScanHeaderType;
import fr.proline.studio.export.ExportDialog;
import fr.proline.studio.export.ExporterFactory;
import fr.proline.studio.export.ExporterFactory.ExporterInfo;
import fr.proline.studio.export.ExporterFactory.ExporterType;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * export a rawFile as MGF file or tsv (ScanHeader)
 *
 * @author MB243701
 */
public class ExportRawFileDialog extends DefaultDialog {

    private static ExportRawFileDialog singletonExportDialog = null;

    private ExportType selectedExportType;

    private JFileChooser fchooser;

    private JTextField fileTextField;
    private List<FileNameExtensionFilter> filterList = new ArrayList<>();
    private JComboBox exportTypeCombobox;
    private JPanel panelExportParams;
    private JPanel panelMgfParam;
    private JPanel panelScanHeaderParam;

    private JTextField mzTolField;
    private JComboBox precursorCombobox;
    private JTextField intensityCutoffField;
    private JCheckBox cbExportProlineTitle;

    private String[] precursorList;

    private JComboBox scanHeaderCombobox;
    private String[] scanHeaderList;
    private Map<Integer, ScanHeaderType> mapScanHeader;

    private DefaultDialog.ProgressTask m_task;

    private float mzTolPPM = 10.0f;
    private IPrecursorComputation precComp = null;
    private float intensityCutoff = 0f;
    private boolean exportProlineTitle = false;
    private ScanHeaderType scanHeaderType = ScanHeaderType.MS2;

    private String outputFileName;

    public static ExportRawFileDialog getDialog(Window parent, String title) {
        if (singletonExportDialog == null) {
            singletonExportDialog = new ExportRawFileDialog(parent, title);
        }
        return singletonExportDialog;
    }

    private ExportRawFileDialog(Window parent, String title) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("Export " + title);
        try {
            setHelpURL(new File(".").getCanonicalPath() + File.separatorChar + "Documentation" + File.separatorChar + "Proline_UserGuide_1.4RC1.docx.html#id.nmf14n");
        } catch (IOException ex) {
            ;
        }
        EnumSet<PrecursorMzComputationEnum> precursorSet = EnumSet.allOf(PrecursorMzComputationEnum.class);
        precursorList = new String[precursorSet.size() + 1];
        int i = 0;
        for (PrecursorMzComputationEnum p : precursorSet) {
            precursorList[i] = p.getUserParamName();
            i++;
        }

        precursorList[i] = ISOLATION_WINDOW_PRECURSOR;
        EnumSet<ScanHeaderType> scanHeaderSet = EnumSet.allOf(ScanHeaderType.class);
        scanHeaderList = new String[scanHeaderSet.size()];
        mapScanHeader = new HashMap();
        i = 0;
        for (ScanHeaderType s : scanHeaderSet) {
            scanHeaderList[i] = s.getName();
            mapScanHeader.put(i, s);
            i++;
        }

        setInternalComponent(createExportPanel());
        setButtonName(BUTTON_OK, "Export");

        fchooser = new JFileChooser();
        fchooser.setMultiSelectionEnabled(false);

        setExportParamsPanel();

    }
    private static final String ISOLATION_WINDOW_PRECURSOR = "Proline refined precursor mz";

    private JPanel createExportPanel() {
        JPanel exportPanel = new JPanel();
        exportPanel.setLayout(new BorderLayout());

        JPanel panelType = new JPanel(new GridBagLayout());

        panelExportParams = new JPanel(new BorderLayout());
        panelMgfParam = createMgfParamPanel();
        panelScanHeaderParam = createScanHeaderParamPanel();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        fileTextField = new JTextField(30);
        panelType.add(fileTextField, c);

        final JButton addFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
        addFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        addFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                ExporterFactory.ExporterInfo exporterInfo = (ExporterFactory.ExporterInfo) exportTypeCombobox.getSelectedItem();

                if (exporterInfo != null) {
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(exporterInfo.getName(), exporterInfo.getFileExtension());
                    FileNameExtensionFilter existFilter = ExportDialog.getFilterWithSameExtensions(filter, filterList);

                    if (existFilter == null) {
                        fchooser.addChoosableFileFilter(filter);
                        filterList.add(filter);
                        fchooser.setFileFilter(filter);
                    } else {
                        fchooser.setFileFilter(existFilter);
                    }
                }

                String textFile = fileTextField.getText().trim();

                if (textFile.length() > 0) {
                    File currentFile = new File(textFile);
                    if (currentFile.isDirectory()) {
                        fchooser.setCurrentDirectory(currentFile);
                    } else {
                        fchooser.setSelectedFile(currentFile);
                    }
                }

                int result = fchooser.showOpenDialog(addFileButton);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fchooser.getSelectedFile();

                    String absolutePath = file.getAbsolutePath();
                    String fileName = file.getName();
                    if (fileName.indexOf('.') == -1) {
                        absolutePath += "." + exporterInfo.getFileExtension();
                    }
                    fileTextField.setText(absolutePath);
                }
            }
        });

        c.gridx += 2;
        c.gridwidth = 1;
        panelType.add(addFileButton, c);

        exportTypeCombobox = new JComboBox(ExporterFactory.getList(ExporterFactory.EXPORT_MGF).toArray());
        exportTypeCombobox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                ExporterFactory.ExporterInfo exporterInfo = (ExporterFactory.ExporterInfo) exportTypeCombobox.getSelectedItem();
                ExporterType type = exporterInfo.geType();
                if (type.equals(ExporterFactory.ExporterType.MGF)) {
                    selectedExportType = ExportType.MGF;
                } else if (type.equals(ExporterFactory.ExporterType.TSV)) {
                    selectedExportType = ExportType.SCAN_HEADER;
                }
                setExportParamsPanel();
            }
        });
        exportTypeCombobox.setSelectedIndex(0);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 1;
        panelType.add(new JLabel("Export Type:"), c);

        c.gridx++;
        c.gridwidth = 2;
        panelType.add(exportTypeCombobox, c);

        exportPanel.add(panelType, BorderLayout.NORTH);

        exportPanel.add(panelExportParams, BorderLayout.CENTER);

        return exportPanel;
    }

    private void setExportParamsPanel() {
        panelExportParams.removeAll();
        String extension = "";
        ArrayList<ExporterInfo> info = ExporterFactory.getList(ExporterFactory.EXPORT_MGF);
        ExporterInfo mgfInfo = null;
        ExporterInfo scanHeaderInfo = null;
        for (ExporterInfo i : info) {
            if (i.geType().equals(ExporterType.MGF)) {
                mgfInfo = i;
            } else if (i.geType().equals(ExporterType.TSV)) {
                scanHeaderInfo = i;
            }
        }
        switch (selectedExportType) {
            case MGF: {
                panelExportParams.add(panelMgfParam, BorderLayout.CENTER);
                if (mgfInfo != null) {
                    extension = mgfInfo.getFileExtension();
                }
                break;
            }
            case SCAN_HEADER: {
                panelExportParams.add(panelScanHeaderParam, BorderLayout.CENTER);
                if (scanHeaderInfo != null) {
                    extension = scanHeaderInfo.getFileExtension();
                }
                break;
            }
            default: {
                // should not happen
                break;
            }
        }
        if (!fileTextField.getText().trim().isEmpty() && !fileTextField.getText().trim().endsWith("." + extension)) {
            fileTextField.setText(fileTextField.getText().trim() + "." + extension);
        }
        panelExportParams.revalidate();
        panelExportParams.repaint();
    }

    private JPanel createMgfParamPanel() {
        JPanel panelMgf = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridy = 0;
        c.gridx = 0;
        c.gridwidth = 1;
        panelMgf.add(new JLabel("m/z tolerance (ppm):"), c);

        c.gridx++;
        c.gridwidth = 2;
        mzTolField = new JTextField();
        mzTolField.setText(Float.toString(MzScopePreferences.getInstance().getMzPPMTolerance()));
        panelMgf.add(mzTolField, c);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 1;
        panelMgf.add(new JLabel("Precursor m/z computation:"), c);
        c.gridx++;
        c.gridwidth = 2;

        precursorCombobox = new JComboBox(precursorList);
        precursorCombobox.setSelectedIndex(1); //MAIN_PRECURSOR_MZ by default
        panelMgf.add(precursorCombobox, c);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 1;
        panelMgf.add(new JLabel("Intensity Cutoff:"), c);

        c.gridx++;
        c.gridwidth = 2;
        intensityCutoffField = new JTextField();
        intensityCutoffField.setText(new Float(0.0).toString());
        panelMgf.add(intensityCutoffField, c);

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 3;
        cbExportProlineTitle = new JCheckBox("Export Proline Title");
        cbExportProlineTitle.setSelected(false);
        panelMgf.add(cbExportProlineTitle, c);

        return panelMgf;
    }

    private JPanel createScanHeaderParamPanel() {
        JPanel panelScanHeader = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridy = 0;
        c.gridx = 0;
        c.gridwidth = 1;
        panelScanHeader.add(new JLabel("Spectrum Header Level:"), c);
        c.gridx++;
        c.gridwidth = 2;

        scanHeaderCombobox = new JComboBox(scanHeaderList);
        scanHeaderCombobox.setSelectedIndex(1); //MS2 by default
        panelScanHeader.add(scanHeaderCombobox, c);

        return panelScanHeader;
    }

    public void setTask(DefaultDialog.ProgressTask task) {
        m_task = task;
    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

    @Override
    protected boolean okCalled() {
        String fileName = fileTextField.getText().trim();

        if (fileName.length() == 0) {
            setStatus(true, "You must fill the file name.");
            highlight(fileTextField);
            return false;
        }

        File mgfFile = new File(fileName);

        if (mgfFile.exists()) {
            String message = "The file already exists. Do you want to overwrite it ?";
            String title = "Overwrite ?";
            String[] options = {"Yes", "No"};
            int reply = JOptionPane.showOptionDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "Yes");
            if (reply != JOptionPane.YES_OPTION) {
                setStatus(true, "File already exists.");
                return false;
            }
        }

        FileWriter fw = null;
        try {
            fw = new FileWriter(mgfFile);
            fw.write("t");
        } catch (Exception e) {
            setStatus(true, fileName + " is not writable.");
            highlight(fileTextField);
            return false;
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }

                mgfFile.delete();
            } catch (Exception e2) {
            }
        }

        outputFileName = fileName;
        // checkParameters
        boolean isOk = true;
        switch (selectedExportType) {
            case MGF: {
                isOk = checkMgfParams();
                break;
            }
            case SCAN_HEADER: {
                isOk = checkScanHeaderParams();
                break;
            }
            default: {
                // should not happen
                break;
            }
        }
        if (!isOk) {
            return false;
        }

        startTask(m_task);
        return false;
    }

    private boolean checkMgfParams() {

        try {
            mzTolPPM = Float.parseFloat(mzTolField.getText());
        } catch (NumberFormatException e) {
            highlight(mzTolField);
            return false;
        }
        try {
            precComp = buildPrecursorComputer();
        } catch (Exception e) {
            highlight(precursorCombobox);
            return false;
        }
        try {
            intensityCutoff = Float.parseFloat(intensityCutoffField.getText());
        } catch (NumberFormatException e) {
            highlight(intensityCutoffField);
            return false;
        }
        exportProlineTitle = cbExportProlineTitle.isSelected();
        return true;
    }

    private IPrecursorComputation buildPrecursorComputer() {
        IPrecursorComputation precComp = null;
        String item = (String) precursorCombobox.getSelectedItem();
        if (item.equals(ISOLATION_WINDOW_PRECURSOR)) {
            precComp = new IsolationWindowPrecursorExtractor(mzTolPPM);
        }
        EnumSet<PrecursorMzComputationEnum> precursorSet = EnumSet.allOf(PrecursorMzComputationEnum.class);
        for (PrecursorMzComputationEnum p : precursorSet) {
            if (item.equals(p.getUserParamName())) {
                precComp = new DefaultPrecursorComputer(p, mzTolPPM);
                break;
            }
        }
        return precComp;
    }

    private boolean checkScanHeaderParams() {
        try {
            scanHeaderType = mapScanHeader.get(scanHeaderCombobox.getSelectedIndex());
        } catch (Exception e) {
            highlight(scanHeaderCombobox);
            return false;
        }
        return true;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public IExportParameters getExportParams() {
        switch (selectedExportType) {
            case MGF: {
                MgfExportParameters mgfExport = new MgfExportParameters(precComp, mzTolPPM, intensityCutoff, exportProlineTitle);
                return mgfExport;
            }
            case SCAN_HEADER: {
                ScanHeaderExportParameters scanHeaderExport = new ScanHeaderExportParameters(scanHeaderType);
                return scanHeaderExport;
            }
            default: {
                //should not happen
                return null;
            }
        }
    }

}
