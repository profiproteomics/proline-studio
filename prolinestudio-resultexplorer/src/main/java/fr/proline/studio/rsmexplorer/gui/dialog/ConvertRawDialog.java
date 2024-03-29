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
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dpm.serverfilesystem.RootInfo;
import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import fr.proline.studio.gui.DefaultDialog;
import static fr.proline.studio.gui.DefaultDialog.BUTTON_CANCEL;
import static fr.proline.studio.gui.DefaultDialog.BUTTON_OK;
import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.FileParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.msfiles.ConvertionUploadBatch;
import fr.proline.studio.msfiles.ConversionSettings;
import fr.proline.studio.msfiles.MzdbUploadSettings;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import fr.proline.studio.NbPreferences;

/**
 *
 * @author AK249877
 */
public class ConvertRawDialog extends DefaultDialog implements FileDialogInterface {

    private static ConvertRawDialog m_singletonNonConnectedDialog = null;
    private static ConvertRawDialog m_singletonConnectedDialog = null;

    private boolean m_serverConnected;
    private JList m_fileList;
    private JScrollPane m_fileListScrollPane;
    private JButton m_addFileButton, m_removeFileButton;
    private ParameterList m_parameterList;
    private BooleanParameter m_deleteMzdb, m_deleteRaw, m_uploadMzdb, m_createParentDirectoryParameter, m_isConverter_v1;

    private FileParameter m_converterFilePath, m_outputFilePath;

    private static ObjectParameter m_uploadLabelParameter = null;
    private String m_lastParentDirectory;

    public static ConvertRawDialog getDialog(Window parent) {

        ArrayList<String> labels = ServerFileSystemView.getServerFileSystemView().getLabels(RootInfo.TYPE_MZDB_FILES);
        boolean connectedToServer = (labels != null);

        if (connectedToServer) {
            if (m_singletonConnectedDialog == null) {
                m_singletonConnectedDialog = new ConvertRawDialog(parent, true);
            } else {
                Object[] associatedTable = labels.toArray(new String[labels.size()]);
                Object[] objectTable = labels.toArray(new String[labels.size()]);
                m_uploadLabelParameter.updateAssociatedObjects(associatedTable);
                m_uploadLabelParameter.updateObjects(objectTable);
                m_singletonConnectedDialog.m_parameterList.loadParameters(NbPreferences.root());
                if (m_singletonConnectedDialog.m_fileList != null) {
                    DefaultListModel model = (DefaultListModel) m_singletonConnectedDialog.m_fileList.getModel();
                    model.clear();
                }
            }

            return m_singletonConnectedDialog;
        } else {
            if (m_singletonNonConnectedDialog == null) {
                m_singletonNonConnectedDialog = new ConvertRawDialog(parent, false);
            } else {

                m_singletonNonConnectedDialog.m_parameterList.loadParameters(NbPreferences.root());
                if (m_singletonNonConnectedDialog.m_fileList != null) {
                    DefaultListModel model = (DefaultListModel) m_singletonNonConnectedDialog.m_fileList.getModel();
                    model.clear();
                }
            }

            return m_singletonNonConnectedDialog;
        }



    }

    private ConvertRawDialog(Window parent, boolean serverConnected) {
        super(parent, Dialog.ModalityType.MODELESS);

        m_serverConnected = serverConnected;

        setTitle("Convert raw file(s)");

        setSize(new Dimension(360, 480));
        setResizable(true);

        this.setDocumentationSuffix("h.j8sehv");

        setButtonVisible(BUTTON_CANCEL, true);
        setButtonName(BUTTON_OK, "OK");
        setStatusVisible(true);

        setInternalComponent(this.createInternalComponent());

    }

    @Override
    public void setFiles(ArrayList<File> files) {
        ((DefaultListModel) m_fileList.getModel()).clear();
        if (files.size() > 0) {
            for (File f : files) {
                ((DefaultListModel) m_fileList.getModel()).addElement(f);
            }

            if (files.get(0).getParentFile() != null) {
                m_lastParentDirectory = files.get(0).getParentFile().getAbsolutePath();
            }
        }
    }

    private Component createInternalComponent() {
        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new BorderLayout());
        internalPanel.add(createFileSelectionPanel(), BorderLayout.CENTER);
        internalPanel.add(createParameterPanel(), BorderLayout.SOUTH);
        return internalPanel;
    }

    private JPanel createParameterPanel() {
        Preferences preferences = NbPreferences.root();

        m_parameterList = new ParameterList(ApplicationSettingsDialog.MS_FILES_SETTINGS);
        m_parameterList.addBackwardCompatiblePrefix(ApplicationSettingsDialog.OTHER_PREVIOUS_MS_FILES_SETTINGS);

        String[] converterExtentions = {"exe"};
        String[] converterFilterNames = {"raw2mzDB.exe"};
        m_converterFilePath = new FileParameter(null, "Converter_(.exe)", "Converter (.exe)", JTextField.class, "", converterFilterNames, converterExtentions);
        m_converterFilePath.setAllFiles(false);
        m_converterFilePath.setSelectionMode(JFileChooser.FILES_ONLY);
        m_converterFilePath.setDefaultDirectory(new File(preferences.get("mzDB_Settings.Converter_(.exe)", System.getProperty("user.home"))));
        m_parameterList.add(m_converterFilePath);

        m_outputFilePath = new FileParameter(null, "Output_Path", "Output Path", JTextField.class, "", null, null);
        m_outputFilePath.setAllFiles(false);
        m_outputFilePath.setSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        m_outputFilePath.setDefaultDirectory(new File(preferences.get("mzDB_Settings.Output_Path", System.getProperty("user.home"))));
        m_parameterList.add(m_outputFilePath);

        JCheckBox isConverterV1Checkbox = new JCheckBox("Converter version 1.1 or above");
        m_isConverter_v1 = new BooleanParameter("CONVERTER_V1", "Converter version is greater or equal to 1.1", isConverterV1Checkbox, false);
        m_parameterList.add(m_isConverter_v1);

        JCheckBox rawCheckbox = new JCheckBox("Delete raw file after a successful conversion");
        m_deleteRaw = new BooleanParameter("DELETE_RAW", "Delete raw file after a successful conversion", rawCheckbox, false);
        m_parameterList.add(m_deleteRaw);




        if (m_serverConnected) {

            JCheckBox uploadCheckbox = new JCheckBox("Upload .mzdb file successful conversion");
            m_uploadMzdb = new BooleanParameter("UPLOAD_CONVERTED", "Upload after conversion", uploadCheckbox, false);
            m_parameterList.add(m_uploadMzdb);

            ArrayList<String> labels = ServerFileSystemView.getServerFileSystemView().getLabels(RootInfo.TYPE_MZDB_FILES);

            Object[] associatedTable = labels.toArray(new String[labels.size()]);
            Object[] objectTable = labels.toArray(new String[labels.size()]);
            JComboBox namingComboBox = new JComboBox(associatedTable);
            m_uploadLabelParameter = new ObjectParameter("MZDB_MOUNT_LABEL", "Server's mounting point", namingComboBox, associatedTable, objectTable, 0, null);
            m_parameterList.add(m_uploadLabelParameter);

            JCheckBox mzdbCheckbox = new JCheckBox("Delete .mzdb file after a successful upload");
            m_deleteMzdb = new BooleanParameter("DELETE_MZDB", "Delete mzdb file after a successful upload", mzdbCheckbox, false);
            m_parameterList.add(m_deleteMzdb);

            JCheckBox parentDirectoryCheckbox = new JCheckBox("Create Parent Directory in Destination");
            m_createParentDirectoryParameter = new BooleanParameter("CREATE_PARENT_DIRECTORY", "Create Parent Directory in Destination", parentDirectoryCheckbox, false);
            m_parameterList.add(m_createParentDirectoryParameter);
        }

        m_parameterList.loadParameters(NbPreferences.root());

        AbstractLinkedParameters linkedParameters = null;
        if (m_serverConnected) {
            linkedParameters = new AbstractLinkedParameters(m_parameterList) {

                @Override
                public void valueChanged(String value, Object associatedValue) {

                    showParameter(m_deleteMzdb, (boolean) m_uploadMzdb.getObjectValue(), (boolean) m_deleteMzdb.getObjectValue());
                    showParameter(m_uploadLabelParameter, (boolean) m_uploadMzdb.getObjectValue(), m_uploadLabelParameter.getObjectValue());
                    showParameter(m_createParentDirectoryParameter, (boolean) m_uploadMzdb.getObjectValue(), (boolean) m_createParentDirectoryParameter.getObjectValue());
                    updateParameterListPanel();
                }

            };
        }
        //linkedParameters.valueChanged("", "");
        JPanel parameterPanel = m_parameterList.getPanel();
        parameterPanel.setBorder(BorderFactory.createTitledBorder(" Conversion & Upload Options "));

        if (m_serverConnected) {
            m_uploadMzdb.addLinkedParameters(linkedParameters);
        }

        return parameterPanel;
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

                Preferences preferences = NbPreferences.root();
                String initializationDirectory = preferences.get("mzDB_Settings.LAST_RAW_PATH", System.getProperty("user.home"));

                File f = new File(initializationDirectory);
                if (!(f.exists() && f.isDirectory())) {
                    initializationDirectory = System.getProperty("user.home");
                }

                JFileChooser fchooser = new JFileChooser(initializationDirectory);

                fchooser.setMultiSelectionEnabled(true);

                fchooser.addChoosableFileFilter(new FileNameExtensionFilter(".raw", "RAW"));
                fchooser.setAcceptAllFileFilterUsed(false);

                //put the one and only filter here! (.mzdb)
                int result = fchooser.showOpenDialog(m_serverConnected ? m_singletonConnectedDialog : m_singletonNonConnectedDialog);
                if (result == JFileChooser.APPROVE_OPTION) {

                    File[] files = fchooser.getSelectedFiles();
                    int nbFiles = files.length;
                    for (int i = 0; i < nbFiles; i++) {
                        ((DefaultListModel) m_fileList.getModel()).addElement(files[i]);
                    }

                    if (files.length > 0) {
                        if (files[0].getParentFile() != null) {
                            m_lastParentDirectory = files[0].getParentFile().getAbsolutePath();
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

    @Override
    protected boolean okCalled() {
        if (m_fileList.getModel().getSize() == 0) {
            setStatus(true, "No files are selected.");
            highlight(m_fileList);
            return false;
        }

        if (m_converterFilePath.getStringValue() == null || m_converterFilePath.getStringValue().length() == 0) {
            setStatus(true, "An appropriate executable must be selected.");
            highlight(m_converterFilePath.getComponent(m_converterFilePath.getStringValue()));
            return false;
        } else if (!new File(m_converterFilePath.getStringValue()).exists()) {
            setStatus(true, "The selected executable no longer exists.");
            highlight(m_converterFilePath.getComponent(m_converterFilePath.getStringValue()));
            return false;
        } else if (m_outputFilePath.getStringValue() == null || m_outputFilePath.getStringValue().length() == 0) {
            setStatus(true, "An appropriate executable must be selected.");
            highlight(m_outputFilePath.getComponent(m_outputFilePath.getStringValue()));
            return false;
        } else if (!new File(m_outputFilePath.getStringValue()).exists()) {
            setStatus(true, "The selected directory no longer exists.");
            highlight(m_outputFilePath.getComponent(m_outputFilePath.getStringValue()));
            return false;
        }

        ParameterError error = m_parameterList.checkParameters();
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }
        m_parameterList.saveParameters(NbPreferences.root());

        HashMap<File, ConversionSettings> conversions = new HashMap<File, ConversionSettings>();
        for (int i = 0; i < m_fileList.getModel().getSize(); i++) {

            File file = (File) m_fileList.getModel().getElementAt(i);

            ConversionSettings conversionSettings = new ConversionSettings(m_converterFilePath.getStringValue(), m_outputFilePath.getStringValue(), (boolean) m_deleteRaw.getObjectValue(), (m_serverConnected) ? (boolean) m_uploadMzdb.getObjectValue() : false,  (boolean) m_isConverter_v1.getObjectValue());
            
            if(conversionSettings.getUploadAfterConversion()){
                MzdbUploadSettings uploadSettings = new MzdbUploadSettings((boolean) m_deleteMzdb.getObjectValue(), m_uploadLabelParameter.getStringValue(), (boolean) m_createParentDirectoryParameter.getObjectValue() ? File.separator + file.getParentFile().getName() : "");
                conversionSettings.setUploadSettings(uploadSettings);
            }else{
                conversionSettings.setUploadSettings(null);
            }

            conversions.put((File) m_fileList.getModel().getElementAt(i), conversionSettings);
        }

        ConvertionUploadBatch conversionBatch = new ConvertionUploadBatch(conversions);

        if (m_lastParentDirectory != null) {
            Preferences preferences = NbPreferences.root();
            preferences.put("mzDB_Settings.LAST_RAW_PATH", m_lastParentDirectory);
        }

        Thread thread = new Thread(conversionBatch);
        thread.start();

        DefaultListModel listModel = (DefaultListModel) m_fileList.getModel();
        listModel.removeAllElements();

        return true;
    }

    @Override
    protected boolean cancelCalled() {
        return true;
    }

}
