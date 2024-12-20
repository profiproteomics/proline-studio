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
package fr.proline.studio.export;

import fr.proline.studio.NbPreferences;
import fr.proline.studio.WindowManager;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.utils.IconManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jdesktop.swingx.JXTable;
import org.slf4j.LoggerFactory;

import org.jfree.graphics2d.svg.*;

/**
 * Dialog used to export an image or a table
 *
 * @author JM235353
 */
public class ExportDialog extends DefaultDialog {

    private static ExportDialog m_singletonImageDialog = null; //Export directly an image from Studio
    private static ExportDialog m_singletonExcelDialog = null; //Export directly from Studio table
    private static ExportDialog m_singletonServerDialog = null; //Run Server side export

    private int m_exportType;

    private JTextField m_fileTextField;
    private JComboBox m_exporTypeCombobox;
    private JCheckBox m_exportAllPSMsChB;
    private Boolean m_showExportAllPSMsChB;

    private JXTable m_table = null;
    private JPanel m_panel = null;

    private JFileChooser m_fchooser;
    private final List<FileNameExtensionFilter> m_filterList = new ArrayList<>();

    private DefaultDialog.ProgressTask m_task = null;

    private String m_exportName = null;

    private JPanel m_decoratedPanel;
    private ParameterList m_parameterList;
    private static final String PARAMETER_LIST_NAME = "General Application Settings";
    private BooleanParameter m_exportParameter;

    private ExportManager m_exportManager = null;

    //This method is called when exporting directly a table from Studio GUI
    public static ExportDialog getDialog(Window parent, JXTable table, String exportName) {
        if (m_singletonExcelDialog == null) {
            m_singletonExcelDialog = new ExportDialog(parent, ExporterFactory.EXPORT_TABLE);
        }

        //Update data to export
        m_singletonExcelDialog.m_table = table;
        m_singletonExcelDialog.m_exportName = exportName;

        m_singletonExcelDialog.updateExportDecoration();

        return m_singletonExcelDialog;
    }

    //This method is called when exporting directly an image from Studio GUI
    public static ExportDialog getDialog(Window parent, JPanel panel, String exportName) {
        if (m_singletonImageDialog == null) {
            m_singletonImageDialog = new ExportDialog(parent, ExporterFactory.EXPORT_IMAGE);
        }
        
        //Update data to export
        m_singletonImageDialog.m_panel = panel;
        m_singletonImageDialog.m_exportName = exportName;

        return m_singletonImageDialog;
    }

    //This method is called when exporting an Identification RSM on server side
    public static ExportDialog getDialog(Window parent, Boolean showExportAllPSMsOption) {
        if (m_singletonServerDialog == null) {
            m_singletonServerDialog = new ExportDialog(parent, ExporterFactory.EXPORT_FROM_SERVER, showExportAllPSMsOption);
        } else if (!m_singletonServerDialog.m_showExportAllPSMsChB.equals(showExportAllPSMsOption)) { //option has changed, create new dialog
            m_singletonServerDialog = new ExportDialog(parent, ExporterFactory.EXPORT_FROM_SERVER, showExportAllPSMsOption);
        }

        return m_singletonServerDialog;
    }

    //This method is called when exportind from server side. Data to export is specified throhg export type
    public static ExportDialog getDialog(Window parent, Boolean showExportAllPSMsOption, int exportType) {
        if (m_singletonServerDialog == null) {
            m_singletonServerDialog = new ExportDialog(parent, exportType, showExportAllPSMsOption);
        } else if((m_singletonServerDialog.m_exportType != exportType)
                || (!m_singletonServerDialog.m_showExportAllPSMsChB.equals(showExportAllPSMsOption))) {
            m_singletonServerDialog = new ExportDialog(parent, exportType, showExportAllPSMsOption);
        }

        return m_singletonServerDialog;
    }

    public void setTask(DefaultDialog.ProgressTask task) {
        m_task = task;
    }

    private ExportDialog(Window parent, int type) {
        this(parent, type, null);
    }

    private ExportDialog(Window parent, int type, Boolean showExportAllPSMsOption) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        m_showExportAllPSMsChB = showExportAllPSMsOption;
        m_exportType = type;

        setTitle("Export");
        setDocumentationSuffix("id.37m2jsg");

        String defaultExportPath;
        Preferences preferences = NbPreferences.root();
        if ((m_exportType == ExporterFactory.EXPORT_TABLE) 
                || (m_exportType == ExporterFactory.EXPORT_FROM_SERVER) 
                || (m_exportType == ExporterFactory.EXPORT_XIC) 
                || (m_exportType == ExporterFactory.EXPORT_SPECTRA)
                || (m_exportType == ExporterFactory.EXPORT_MZIDENTML)
                ) {
            defaultExportPath = preferences.get("DefaultExcelExportPath", System.getProperty("user.home"));
        } else if(m_exportType == ExporterFactory.EXPORT_IMAGE) {
            defaultExportPath = preferences.get("DefaultImageExportPath", System.getProperty("user.home"));
        } else {
            throw new IllegalArgumentException("Unknow export type specified "+m_exportType);
        }

        setInternalComponent(createExportPanel(defaultExportPath));

        setButtonName(BUTTON_OK, (m_exportType == ExporterFactory.EXPORT_IMAGE) ? "Export Image" : "Export");

        m_fchooser = new JFileChooser(new File(defaultExportPath));
        m_fchooser.setMultiSelectionEnabled(false);

    }

    private JPanel createDecoratedRadioPanel() {
        m_decoratedPanel = new JPanel();
        m_decoratedPanel.setBorder(BorderFactory.createTitledBorder(" Settings "));
        m_decoratedPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        m_parameterList = new ParameterList(PARAMETER_LIST_NAME);

        m_exportParameter = new BooleanParameter("Export_Decorated", "Export Decorated", JCheckBox.class, true);
        m_parameterList.add(m_exportParameter);

        m_decoratedPanel.add(m_parameterList.getPanel());

        return m_decoratedPanel;
    }

    private void updateExportDecoration() {
        m_parameterList.loadParameters(NbPreferences.root());
    }

    public final JPanel createExportPanel(String defaultExportPath) {

        JPanel exportPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        m_fileTextField = new JTextField(30);
        m_fileTextField.setText(defaultExportPath);
        exportPanel.add(m_fileTextField, c);

        final JButton addFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
        addFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        addFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                ExporterFactory.ExporterInfo exporterInfo = (ExporterFactory.ExporterInfo) m_exporTypeCombobox.getSelectedItem();

                if (exporterInfo != null) {
                    FileNameExtensionFilter filter = new FileNameExtensionFilter(exporterInfo.getName(), exporterInfo.getFileExtension());
                    FileNameExtensionFilter existFilter = getFilterWithSameExtensions(filter);

                    if (existFilter == null) {
                        m_fchooser.addChoosableFileFilter(filter);
                        m_filterList.add(filter);
                        m_fchooser.setFileFilter(filter);
                    } else {
                        m_fchooser.setFileFilter(existFilter);
                    }
                }

                String textFile = m_fileTextField.getText().trim();

                if (textFile.length() > 0) {
                    File currentFile = new File(textFile);
                    if (currentFile.isDirectory()) {
                        m_fchooser.setCurrentDirectory(currentFile);
                    } else {
                        m_fchooser.setSelectedFile(currentFile);
                    }
                }

                int result = m_fchooser.showOpenDialog(addFileButton);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = m_fchooser.getSelectedFile();

                    String absolutePath = file.getAbsolutePath();
                    String fileName = file.getName();
                    if (fileName.indexOf('.') == -1 && exporterInfo!=null ) {
                        absolutePath += "." + exporterInfo.getFileExtension();
                    }
                    m_fileTextField.setText(absolutePath);
                }
            }
        });

        c.gridx += 2;
        c.gridwidth = 1;
        exportPanel.add(addFileButton, c);

        if ((m_exportType == ExporterFactory.EXPORT_FROM_SERVER 
                || m_exportType == ExporterFactory.EXPORT_XIC) && m_showExportAllPSMsChB) {
            //Allow specific parameter in this case
            c.gridy++;
            c.gridx = 0;
            c.gridwidth = 2;
            m_exportAllPSMsChB = new JCheckBox("Export all PSMs");
            exportPanel.add(m_exportAllPSMsChB, c);
        }


        m_exporTypeCombobox = new JComboBox(ExporterFactory.getList(m_exportType).toArray());
        m_exporTypeCombobox.setSelectedIndex(0);

        if (m_exportType == ExporterFactory.EXPORT_TABLE) {
            m_exporTypeCombobox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent ie) {
                    if (m_decoratedPanel == null) {
                        return;
                    }
                    if (ie.getItem().toString().contains("CSV")) {
                        m_decoratedPanel.setVisible(false);
                    } else {
                        m_decoratedPanel.setVisible(true);
                    }
                    repack();
                }
            });
        }

        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 1;
        exportPanel.add(new JLabel("Export Type:"), c);

        c.gridx++;
        c.gridwidth = 2;
        exportPanel.add(m_exporTypeCombobox, c);

        
        if (m_exportType == ExporterFactory.EXPORT_TABLE) {
            // Allow specific parameter in this case
            c.gridx = 0;
            c.gridy++;
            c.gridwidth = 3;
            exportPanel.add(createDecoratedRadioPanel(), c);
        }
        
        c.gridy++;
        c.weighty = 1;
        exportPanel.add(Box.createVerticalGlue());
        
        return exportPanel;
    }

    public String getFileName() {
        return m_fileTextField.getText().trim();
    }

    public Boolean exportAllPSMs() {
        if (m_exportType == ExporterFactory.EXPORT_FROM_SERVER || m_exportType == ExporterFactory.EXPORT_XIC) {
            return m_exportAllPSMsChB.isSelected();
        } else {
            return null;
        }
    }

    public Boolean exportDecorated() {
        if (m_exportType == ExporterFactory.EXPORT_TABLE) {
            return (boolean) m_exportParameter.getObjectValue();
        } else {
            return null;
        }
    }

    public ExporterFactory.ExporterInfo getExporterInfo() {
        return (ExporterFactory.ExporterInfo) m_exporTypeCombobox.getSelectedItem();
    }

    @Override
    protected boolean okCalled() {

        String fileName = m_fileTextField.getText().trim();

        if (fileName.length() == 0) {
            setStatus(true, "You must fill the file name.");
            highlight(m_fileTextField);
            return false;
        }


        final ExporterFactory.ExporterInfo exporterInfo = getExporterInfo();
        String extension = exporterInfo.getFileExtension();
        if (!fileName.endsWith("."+extension)) {
            fileName = fileName + "." + extension;
            m_fileTextField.setText(fileName);
        }

        
        File f = new File(fileName);

        if (f.exists()) {
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
            fw = new FileWriter(f);
            fw.write("t");
        } catch (Exception e) {
            setStatus(true, fileName + " is not writable.");
            highlight(m_fileTextField);
            return false;
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }

                f.delete();
            } catch (Exception e2) {
            }
        }

        if (m_exportType == ExporterFactory.EXPORT_TABLE) {

            m_exportManager = new ExportManager(m_table);
            ProgressTask exportTask = m_exportManager.getTask(exporterInfo.getExporter(), m_exportName, fileName, exportDecorated());
            startTask(exportTask);
            
            Preferences preferences = NbPreferences.root();
            preferences.put("DefaultExcelExportPath", f.getAbsoluteFile().getParentFile().getAbsolutePath());
            
        } else if (m_exportType == ExporterFactory.EXPORT_FROM_SERVER 
                || m_exportType == ExporterFactory.EXPORT_XIC 
                || m_exportType == ExporterFactory.EXPORT_SPECTRA
                || m_exportType == ExporterFactory.EXPORT_MZIDENTML) {

            startTask(m_singletonServerDialog.m_task);

            Preferences preferences = NbPreferences.root();
            preferences.put("DefaultExcelExportPath", f.getAbsoluteFile().getParentFile().getAbsolutePath());

        } else if (m_exportType == ExporterFactory.EXPORT_IMAGE) {

            ExporterFactory.ExporterType exporterType = exporterInfo.geType();

            String absolutePath = f.getAbsolutePath();
            if (absolutePath.endsWith(".png")) {
                // we force type to png
                exporterType = ExporterFactory.ExporterType.PNG;
                m_exporTypeCombobox.setSelectedItem(ExporterFactory.EXPORTER_INFO_PNG);
            } else if (absolutePath.endsWith(".svg")) {
                // we force type to svg
                exporterType = ExporterFactory.ExporterType.SVG;
                m_exporTypeCombobox.setSelectedItem(ExporterFactory.EXPORTER_INFO_SVG);
            } else if (exporterType == ExporterFactory.ExporterType.PNG) {
                // we add png to end of file
                f = new File(absolutePath + ".png");
            } else if (exporterType == ExporterFactory.ExporterType.SVG) {
                // we add png to end of file
                f = new File(absolutePath + ".svg");
            }

            m_fileTextField.setText(f.getAbsolutePath());

            if (exporterType == ExporterFactory.ExporterType.PNG) {

                BufferedImage bi = new BufferedImage(m_panel.getSize().width, m_panel.getSize().height, BufferedImage.TYPE_INT_ARGB);
                Graphics g = bi.createGraphics();
                m_panel.paint(g);
                g.dispose();
                try {
                    ImageIO.write(bi, "png", f);
                } catch (IOException e) {
                    LoggerFactory.getLogger("ProlineStudio.Commons").error("Error exporting png", e);
                }

                Preferences preferences = NbPreferences.root();
                preferences.put("DefaultExcelImagePath", f.getAbsoluteFile().getParentFile().getAbsolutePath());

            } else if (exporterType == ExporterFactory.ExporterType.SVG) {

                SVGGraphics2D g2 = new SVGGraphics2D(m_panel.getWidth(), m_panel.getHeight());
                m_panel.paint(g2);

                try {
                    SVGUtils.writeToSVG(f, g2.getSVGElement());
                } catch (Exception ex) {
                    LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Error exporting svg", ex);
                }

                g2.dispose();

                Preferences preferences = NbPreferences.root();
                preferences.put("DefaultExcelImagePath", f.getAbsoluteFile().getParentFile().getAbsolutePath());

            }
            return true;
        }

        return false;

    }

    @Override
    public void setVisible(boolean v) {

        if (!v) {

            if (m_exportManager != null) {
                Exception e = m_exportManager.getException();
                if (e != null) {

                    LoggerFactory.getLogger("ProlineStudio.Commons").error("Error during table export", e);
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String message = sw.toString();
                    InfoDialog errorDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "Error", message, true);
                    errorDialog.setButtonVisible(InfoDialog.BUTTON_CANCEL, false);
                    errorDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
                    errorDialog.setVisible(true);
                }
            }
            m_table = null;
            m_panel = null;
            m_exportName = null;
            m_exportManager = null;
        }
        super.setVisible(v);
    }

    @Override
    protected boolean cancelCalled() {

        return true;
    }

    private FileNameExtensionFilter getFilterWithSameExtensions(FileNameExtensionFilter filter) {
        return ExportDialog.getFilterWithSameExtensions(filter, m_filterList);
    }

    /* compare filter based on the extensions, returns null if the filter is not already in the list, otherwise returns the object filter in the list*/
    public static FileNameExtensionFilter getFilterWithSameExtensions(FileNameExtensionFilter filter, List<FileNameExtensionFilter> filterList) {
        FileNameExtensionFilter existFilter = null;

        String[] newExtension = filter.getExtensions();
        int nbNew = newExtension.length;
        for (FileNameExtensionFilter f : filterList) {
            String[] extensions = f.getExtensions();
            boolean sameExt = true;
            int nbE = extensions.length;
            if (nbE == nbNew) {
                for (int k = 0; k < nbE; k++) {
                    if (!extensions[k].equals(newExtension[k])) {
                        sameExt = false;
                        break;
                    }
                }
            }
            if (sameExt) {
                existFilter = f;
                break;
            }
        }
        return existFilter;

    }
}
