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
import fr.proline.studio.parameter.BooleanParameter;
import fr.proline.studio.parameter.ObjectParameter;
import fr.proline.studio.parameter.ParameterError;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.msfiles.MzdbUploadBatch;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.util.NbPreferences;

/**
 *
 * @author AK249877
 */
public class UploadMzdbDialog extends DefaultDialog implements FileDialogInterface {

    private static UploadMzdbDialog m_singletonDialog = null;
    private static JList m_fileList;
    private JScrollPane m_fileListScrollPane;
    private JButton m_addFileButton, m_removeFileButton;
    private static ParameterList m_parameterList;
    private BooleanParameter m_deleteMzdbParameter, m_createParentDirectoryParameter;
    private static ObjectParameter m_uploadLabelParameter;
    private String m_lastParentDirectory;

    public static UploadMzdbDialog getDialog(Window parent) {
        if (m_singletonDialog == null) {
            m_singletonDialog = new UploadMzdbDialog(parent);
        } else {
            ArrayList<String> labels = ServerFileSystemView.getServerFileSystemView().getLabels(RootInfo.TYPE_MZDB_FILES);
            Object[] associatedTable = labels.toArray(new String[labels.size()]);
            Object[] objectTable = labels.toArray(new String[labels.size()]);
            m_uploadLabelParameter.updateAssociatedObjects(associatedTable);
            m_uploadLabelParameter.updateObjects(objectTable);
            m_parameterList.loadParameters(NbPreferences.root());
            if (m_fileList != null) {
                DefaultListModel model = (DefaultListModel) m_fileList.getModel();
                model.clear();
            }
        }

        return m_singletonDialog;
    }

    public UploadMzdbDialog(Window parent) {
        super(parent, Dialog.ModalityType.MODELESS);

        setTitle("Upload mzDB file(s)");

        setSize(new Dimension(360, 480));
        setResizable(true);

        this.setDocumentationSuffix(null);

        setButtonVisible(BUTTON_CANCEL, true);
        setButtonName(BUTTON_OK, "OK");
        setStatusVisible(true);

        setInternalComponent(createInternalComponent());

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
        m_parameterList = new ParameterList("mzDB Settings");
        JCheckBox deleteCheckbox = new JCheckBox("Delete mzdb file after a successful upload");
        m_deleteMzdbParameter = new BooleanParameter("DELETE_MZDB", "Delete mzdb file after a successful upload", deleteCheckbox, false);
        m_parameterList.add(m_deleteMzdbParameter);

        JCheckBox parentDirectoryCheckbox = new JCheckBox("Create Parent Directory in Destination");
        m_createParentDirectoryParameter = new BooleanParameter("CREATE_PARENT_DIRECTORY", "Create Parent Directory in Destination", parentDirectoryCheckbox, false);
        m_parameterList.add(m_createParentDirectoryParameter);

        ArrayList<String> labels = ServerFileSystemView.getServerFileSystemView().getLabels(RootInfo.TYPE_MZDB_FILES);

        Object[] associatedTable = labels.toArray(new String[labels.size()]);
        JComboBox namingComboBox = new JComboBox(associatedTable);
        Object[] objectTable = labels.toArray(new String[labels.size()]);
        m_uploadLabelParameter = new ObjectParameter("MZDB_MOUNT_LABEL", "Server's mounting point", namingComboBox, associatedTable, objectTable, 0, null);
        m_parameterList.add(m_uploadLabelParameter);

        m_parameterList.loadParameters(NbPreferences.root());

        JPanel parameterPanel = m_parameterList.getPanel();
        parameterPanel.setBorder(BorderFactory.createTitledBorder(" Upload Options "));

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
                String initializationDirectory = preferences.get("mzDB_Settings.LAST_MZDB_PATH", System.getProperty("user.home"));

                File f = new File(initializationDirectory);
                if (!(f.exists() && f.isDirectory())) {
                    initializationDirectory = System.getProperty("user.home");
                }

                JFileChooser fchooser = new JFileChooser(initializationDirectory);

                fchooser.setMultiSelectionEnabled(true);

                fchooser.addChoosableFileFilter(new FileNameExtensionFilter(".mzdb", "mzDB"));
                fchooser.setAcceptAllFileFilterUsed(false);

                //put the one and only filter here! (.mzdb)
                int result = fchooser.showOpenDialog(m_singletonDialog);
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
        ParameterError error = m_parameterList.checkParameters();
        if (error != null) {
            setStatus(true, error.getErrorMessage());
            highlight(error.getParameterComponent());
            return false;
        }
        m_parameterList.saveParameters(NbPreferences.root());

        if (m_lastParentDirectory != null) {
            Preferences preferences = NbPreferences.root();
            preferences.put("mzDB_Settings.LAST_MZDB_PATH", m_lastParentDirectory);
        }

        HashMap<File, MzdbUploadSettings> mzdbFiles = new HashMap<File, MzdbUploadSettings>();
        for (int i = 0; i < m_fileList.getModel().getSize(); i++) {

            File file = (File) m_fileList.getModel().getElementAt(i);

            MzdbUploadSettings uploadSettings = new MzdbUploadSettings((boolean) m_deleteMzdbParameter.getObjectValue(), m_uploadLabelParameter.getStringValue(), (boolean) m_createParentDirectoryParameter.getObjectValue() ? File.separator + file.getParentFile().getName() : "");

            mzdbFiles.put((File) m_fileList.getModel().getElementAt(i), uploadSettings);
        }

        MzdbUploadBatch uploadBatch = new MzdbUploadBatch(mzdbFiles);
        Thread thread = new Thread(uploadBatch);
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
