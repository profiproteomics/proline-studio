/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.studio.dpm.serverfilesystem.RootInfo;
import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import fr.proline.studio.gui.DefaultDialog;
import static fr.proline.studio.gui.DefaultDialog.BUTTON_CANCEL;
import static fr.proline.studio.gui.DefaultDialog.BUTTON_OK;
import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.StringParameter;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.wizard.ConvertionUploadBatch;
import fr.proline.studio.wizard.ConversionSettings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import org.openide.util.NbPreferences;

/**
 *
 * @author AK249877
 */
public class ConvertRawDialog extends DefaultDialog {

    private static ConvertRawDialog m_singletonDialog = null;
    private JList m_fileList;
    private JScrollPane m_fileListScrollPane;
    private JButton m_addFileButton, m_removeFileButton;
    private static ParameterList m_parameterList;
    private BooleanParameter m_deleteMzdb, m_deleteRaw, m_uploadMzdb;
    private StringParameter m_converterPath, m_outputPath;
    private static ObjectParameter m_uploadLabelParameter;

    public static ConvertRawDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new ConvertRawDialog(parent);
        }else {
            ArrayList<String> labels = ServerFileSystemView.getServerFileSystemView().getLabels(RootInfo.TYPE_MZDB_FILES);
            Object[] associatedTable = labels.toArray(new String[labels.size()]);
            Object[] objectTable = labels.toArray(new String[labels.size()]);
            m_uploadLabelParameter.updateAssociatedObjects(associatedTable);
            m_uploadLabelParameter.updateObjects(objectTable);
            m_parameterList.loadParameters(NbPreferences.root());
        }

        return m_singletonDialog;
    }

    public ConvertRawDialog(Window parent) {
        super(parent, Dialog.ModalityType.MODELESS);

        setTitle("Convert raw file(s)");

        setSize(new Dimension(360, 480));
        setResizable(true);

        this.setHelpURL("");

        setButtonVisible(BUTTON_CANCEL, true);
        setButtonName(BUTTON_OK, "OK");
        setStatusVisible(true);

        setInternalComponent(this.createInternalComponent());

    }

    private Component createInternalComponent() {
        JPanel internalPanel = new JPanel();
        internalPanel.setLayout(new BorderLayout());
        internalPanel.add(createFileSelectionPanel(), BorderLayout.CENTER);
        internalPanel.add(createParameterPanel(), BorderLayout.SOUTH);
        return internalPanel;
    }

    private JPanel createParameterPanel() {
        m_parameterList = new ParameterList("mzDB Settings");

        JTextField converterTextField = new JTextField();
        converterTextField.setEditable(false);
        converterTextField.setBackground(Color.WHITE);
        converterTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jFileChooser.setMultiSelectionEnabled(false);
                jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(".exe", "exe"));
                jFileChooser.setAcceptAllFileFilterUsed(false);

                int result = jFileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    converterTextField.setText(jFileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        m_converterPath = new StringParameter("Converter_(.exe)", "Converter (.exe)", converterTextField, "", 5, null);
        m_parameterList.add(m_converterPath);

        JTextField outputTextField = new JTextField();
        outputTextField.setEditable(false);
        outputTextField.setBackground(Color.WHITE);
        outputTextField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jFileChooser.setMultiSelectionEnabled(false);
                int result = jFileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    outputTextField.setText(jFileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        m_outputPath = new StringParameter("Output_path", "Output path", outputTextField, "", 5, null);
        m_parameterList.add(m_outputPath);

        JCheckBox rawCheckbox = new JCheckBox("Delete raw file after a successful conversion");
        m_deleteRaw = new BooleanParameter("Delete_raw_file_after_a_successful_conversion", "Delete raw file after a successful conversion", rawCheckbox, false);
        m_parameterList.add(m_deleteRaw);

        JCheckBox uploadCheckbox = new JCheckBox("Upload .mzdb file successful conversion");
        m_uploadMzdb = new BooleanParameter("Upload_after_conversion", "Upload after conversion", uploadCheckbox, false);
        m_parameterList.add(m_uploadMzdb);
        
        ArrayList<String> labels = ServerFileSystemView.getServerFileSystemView().getLabels(RootInfo.TYPE_MZDB_FILES);

        Object[] associatedTable = labels.toArray(new String[labels.size()]);
        JComboBox namingComboBox = new JComboBox(associatedTable);
        Object[] objectTable = labels.toArray(new String[labels.size()]);
        m_uploadLabelParameter = new ObjectParameter("MZDB_MOUNT_LABEL", "Server's mounting point", namingComboBox, associatedTable, objectTable, 0, null);
        m_parameterList.add(m_uploadLabelParameter);

        JCheckBox mzdbCheckbox = new JCheckBox("Delete mzdb file after a successful upload");
        m_deleteMzdb = new BooleanParameter("Delete_mzdb_file_after_a_successful_upload", "Delete mzdb file after a successful upload", mzdbCheckbox, false);
        m_parameterList.add(m_deleteMzdb);

        m_parameterList.loadParameters(NbPreferences.root());

        AbstractLinkedParameters linkedParameters = new AbstractLinkedParameters(m_parameterList) {

            @Override
            public void valueChanged(String value, Object associatedValue) {
                showParameter(m_deleteMzdb, uploadCheckbox.isSelected(), (boolean) m_deleteMzdb.getObjectValue());
                showParameter(m_uploadLabelParameter, uploadCheckbox.isSelected(), b);
                updateParameterListPanel();
            }

        };

        m_uploadMzdb.addLinkedParameters(linkedParameters);

        linkedParameters.valueChanged("", "");

        JPanel parameterPanel = m_parameterList.getPanel();
        parameterPanel.setBorder(BorderFactory.createTitledBorder(" Conversion & Upload Options "));
        
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

                JFileChooser fchooser = new JFileChooser();

                fchooser.setMultiSelectionEnabled(true);

                fchooser.addChoosableFileFilter(new FileNameExtensionFilter(".raw", "RAW"));
                fchooser.setAcceptAllFileFilterUsed(false);

                //put the one and only filter here! (.mzdb)
                int result = fchooser.showOpenDialog(m_singletonDialog);
                if (result == JFileChooser.APPROVE_OPTION) {

                    File[] files = fchooser.getSelectedFiles();
                    int nbFiles = files.length;
                    for (int i = 0; i < nbFiles; i++) {
                        ((DefaultListModel) m_fileList.getModel()).addElement(files[i]);
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
        
        if(!new File(m_converterPath.getStringValue()).exists()){
            setStatus(true, "The selected executable no longer exists.");
            highlight(m_converterPath.getComponent(m_converterPath.getStringValue()));
            return false;
        }else if(!new File(m_outputPath.getStringValue()).exists()){
            setStatus(true, "The selected directory no longer exists.");
            highlight(m_outputPath.getComponent(m_outputPath.getStringValue()));
            return false;
        }
        
        ParameterError error = m_parameterList.checkParameters();
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }
        m_parameterList.saveParameters(NbPreferences.root());
        
        ArrayList<File> rawFiles = new ArrayList<File>();
        for(int i=0; i<m_fileList.getModel().getSize(); i++){
            rawFiles.add((File) m_fileList.getModel().getElementAt(i));
        }
        
        ConversionSettings settings = new ConversionSettings(m_converterPath.getStringValue(), m_outputPath.getStringValue(), (boolean)m_deleteRaw.getObjectValue(), (boolean)m_uploadMzdb.getObjectValue(), (boolean)m_deleteMzdb.getObjectValue());
        ConvertionUploadBatch conversionBatch = new ConvertionUploadBatch(rawFiles, settings, m_uploadLabelParameter.getStringValue());
        
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
