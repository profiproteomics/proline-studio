/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.msfiles.WorkingSetEntry.Location;
import fr.proline.studio.utils.IconManager;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
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
public class AddlWorkingSetEntryDialog extends DefaultDialog {

    private static AddlWorkingSetEntryDialog m_singleton = null;
    private static WorkingSet m_workingSet;
    private JButton m_addLocalFileButton, m_addRemoteFileButton, m_removeFileButton;
    private JScrollPane m_fileListScrollPane;
    private static JList m_fileList;
    private String m_lastLocalParentDirectory, m_lastLabelUsed;
    private Preferences m_preferences;

    public static AddlWorkingSetEntryDialog getDialog(Window parent, WorkingSet workingSet) {
        m_workingSet = workingSet;
        if (m_singleton == null) {
            m_singleton = new AddlWorkingSetEntryDialog(parent);
        }
        m_singleton.updateRemoteButton();
        return m_singleton;
    }

    private AddlWorkingSetEntryDialog(Window parent) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
        setTitle("Add an entry");
        setSize(new Dimension(480, 320));
        m_preferences = NbPreferences.root();
        setInternalComponent(createFileSelectionPanel());
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

        m_addLocalFileButton = new JButton(IconManager.getIcon(IconManager.IconType.OPEN_FILE));
        m_addLocalFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        m_addRemoteFileButton = new JButton(IconManager.getIcon(IconManager.IconType.SERVER_ON));
        m_addRemoteFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        m_removeFileButton = new JButton(IconManager.getIcon(IconManager.IconType.ERASER));
        m_removeFileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));

        // Placement of Objects for File Selection Panel
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 4;
        c.weightx = 1.0;
        c.weighty = 1.0;
        fileSelectionPanel.add(m_fileListScrollPane, c);

        c.gridx++;
        c.gridheight = 1;
        c.weightx = 0;
        c.weighty = 0;
        fileSelectionPanel.add(m_addLocalFileButton, c);

        c.gridy++;
        fileSelectionPanel.add(m_addRemoteFileButton, c);

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

        m_addLocalFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                String localInitDirectory = m_preferences.get("New_Entry_Dialog.LAST_LOCAL_MZDB_PATH", System.getProperty("user.home"));

                File f = new File(localInitDirectory);
                if (!(f.exists() && f.isDirectory())) {
                    localInitDirectory = System.getProperty("user.home");
                }

                JFileChooser fchooser = new JFileChooser(localInitDirectory);

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
                        m_lastLocalParentDirectory = files[0].getParentFile().getAbsolutePath();
                    }

                }
            }
        });

        m_addRemoteFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                File[] roots = ServerFileSystemView.getServerFileSystemView().getRoots();

                String remoteInitDirectory = m_preferences.get("New_Entry_Dialog.LAST_REMOTE_MZDB_PATH", roots[0].getAbsolutePath());

                int index;

                for (index = 0; index < roots.length-1; index++) {
                    if (remoteInitDirectory.startsWith(roots[index].getAbsolutePath())) {
                        break;
                    }
                }

                JFileChooser fchooser = new JFileChooser(roots[index], ServerFileSystemView.getServerFileSystemView());

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
                        m_lastLabelUsed = files[0].getParentFile().getAbsolutePath();
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
        } else {

            if (m_lastLocalParentDirectory != null) {
                m_preferences.put("New_Entry_Dialog.LAST_LOCAL_MZDB_PATH", m_lastLocalParentDirectory);
            }

            if (m_lastLabelUsed != null) {
                m_preferences.put("New_Entry_Dialog.LAST_REMOTE_MZDB_PATH", m_lastLabelUsed);
            }

            for (int i = 0; i < m_fileList.getModel().getSize(); i++) {
                File f = (File) m_fileList.getModel().getElementAt(i);
                Location location = f.exists() ? WorkingSetEntry.Location.LOCAL : WorkingSetEntry.Location.REMOTE;
                boolean b = f.exists();
                m_workingSet.addEntry(f.getAbsolutePath(), location);
            }

            DefaultListModel listModel = (DefaultListModel) m_fileList.getModel();
            listModel.removeAllElements();

            return true;
        }
    }

    private void updateRemoteButton() {
        m_addRemoteFileButton.setEnabled(DatabaseDataManager.getDatabaseDataManager().getLoggedUser() != null);
    }

    private boolean validateParameters() {
        return true;
    }

}
