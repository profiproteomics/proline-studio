/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import fr.proline.mzscope.utils.IPopupMenuDelegate;
import fr.proline.studio.mzscope.MzdbInfo;
import fr.proline.studio.pattern.MzScopeWindowBoxManager;
import fr.proline.studio.rsmexplorer.gui.MzScope;
import fr.proline.studio.rsmexplorer.gui.TreeUtils;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import org.json.simple.JSONArray;

/**
 *
 * @author AK249877
 */
public class WorkingSetView extends JPanel implements IPopupMenuDelegate {

    public enum ActionType {

        VIEW, DETECT_PEAKELS
    }

    private static WorkingSetView m_singleton = null;

    private WorkingSetModel m_workingSetModel;
    private JTree m_tree;
    private JMenuItem m_addWorkingSet, m_renameWorkingSet, m_changeDescriptionAtWorkingSet, m_removeWorkingSet, m_addWorkingSetEntry, m_removeWorkingSetEntry, m_viewMzdb, m_detectPeakels;
    private JPopupMenu m_popupMenu;
    private ActionListener m_viewMzdbAction;
    private WorkingSetRoot m_root;

    private ArrayList<WorkingSet> m_selectedWorkingSets;
    private ArrayList<WorkingSetEntry> m_selectedWorkingSetEntries;
    private ArrayList<WorkingSetEntry> m_existingSelectedWorkingSetEntries;
    private WorkingSetRoot m_selectedRoot;

    private HashSet<String> m_downloadIndex;

    public static WorkingSetView getWorkingSetView() {
        if (m_singleton == null) {
            m_singleton = new WorkingSetView();
        }
        return m_singleton;
    }

    private WorkingSetView() {
        initComponents();
    }

    private void initComponents() {

        m_selectedWorkingSets = new ArrayList<WorkingSet>();
        m_selectedWorkingSetEntries = new ArrayList<WorkingSetEntry>();
        m_existingSelectedWorkingSetEntries = new ArrayList<WorkingSetEntry>();
        m_selectedRoot = null;

        m_downloadIndex = new HashSet<String>();

        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Working Sets"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;

        m_root = new WorkingSetRoot((JSONArray) WorkingSetUtil.readJSON().get("working_sets"));

        m_workingSetModel = new WorkingSetModel(m_root);
        m_tree = new JTree(m_workingSetModel);

        m_tree.setTransferHandler(new WorkingSetEntriesTransferHandler());
        m_tree.setDragEnabled(true);

        m_tree.setCellRenderer(new DefaultTreeCellRenderer() {

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean isLeaf, int row, boolean focused) {
                Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);

                setForeground(Color.BLACK);

                if (((DefaultMutableTreeNode) value).isRoot()) {
                    //setIcon(IconManager.getIcon(IconManager.IconType.DRIVE));
                } else {

                    Object userObject = ((DefaultMutableTreeNode) value).getUserObject();

                    if (userObject instanceof WorkingSet) {
                        ;
                    } else if (userObject instanceof WorkingSetEntry) {
                        WorkingSetEntry entry = (WorkingSetEntry) userObject;
                        if (!entry.exists()) {
                            setForeground(Color.LIGHT_GRAY);
                            setIcon(IconManager.getIcon(IconManager.IconType.SPECTRUM_EMISSION));
                        } else {
                            if (m_downloadIndex.contains(entry.getFile().getAbsolutePath())) {
                                setIcon(IconManager.getIcon(IconManager.IconType.ARROW_DOWN));
                            } else {
                                setIcon(IconManager.getIcon(IconManager.IconType.SPECTRUM_EMISSION));
                            }
                        }

                    }
                }

                return c;
            }
        });

        m_tree.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    updatePopupMenu();
                    m_popupMenu.show((JComponent) e.getSource(), e.getX(), e.getY());
                }
            }
        });

        m_tree.addTreeExpansionListener(new TreeExpansionListener() {

            @Override
            public void treeExpanded(TreeExpansionEvent tee) {
                TreeUtils.saveExpansionState(m_tree, TreeUtils.TreeType.WORKING_SET, null);
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent tee) {
                TreeUtils.saveExpansionState(m_tree, TreeUtils.TreeType.WORKING_SET, null);
            }

        });

        JScrollPane scrollPane = new JScrollPane(m_tree);

        m_popupMenu = new JPopupMenu();
        initPopupMenu(m_popupMenu);

        add(scrollPane, c);

        resetTreeState();

    }

    @Override
    public void initPopupMenu(JPopupMenu popupMenu) {
        // view data
        m_viewMzdbAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {

                ArrayList<File> localFiles = getSelectedRawFiles(WorkingSetEntry.Location.LOCAL);
                ArrayList<WorkingSetEntry> remoteEntries = getSelectedWorkingSetEntries(WorkingSetEntry.Location.REMOTE);
                ArrayList<File> totalFiles = new ArrayList<File>();

                if (!remoteEntries.isEmpty()) {
                    MsListener msListener = new MsListener() {

                        @Override
                        public void conversionPerformed(ArrayList<MsListenerConverterParameter> list) {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }

                        @Override
                        public void uploadPerformed(ArrayList<MsListenerParameter> list) {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }

                        @Override
                        public void downloadPerformed(ArrayList<MsListenerDownloadParameter> list) {
                            //all the action will be here.

                            totalFiles.addAll(localFiles);
                            for (int i = 0; i < list.size(); i++) {
                                MsListenerDownloadParameter p = list.get(i);
                                if (p.wasSuccessful()) {
                                    File newFile = p.getDestinationFile();
                                    if (newFile.exists()) {
                                        replaceEntry(p.getRemoteFile(), newFile);
                                        totalFiles.add(newFile);
                                    }
                                }
                            }
                            verifyEncodingAndProcess(totalFiles, ActionType.VIEW);
                        }

                        @Override
                        public void exportPerformed(ArrayList<MsListenerParameter> list) {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }

                        @Override
                        public void verificationPerformed(ArrayList<MsListenerParameter> list) {
                            ;
                        }

                        @Override
                        public void entryStateUpdated(ArrayList<MsListenerEntryUpdateParameter> list) {
                            updateDownloadIndex(list);
                        }

                    };

                    String localDirPath = WorkingSetUtil.getTempDirectory().getAbsolutePath();

                    MzdbDownloadBatch downloadBatch = new MzdbDownloadBatch(localDirPath, "", remoteEntries);
                    downloadBatch.addMsListener(msListener);
                    Thread downloadThread = new Thread(downloadBatch);
                    downloadThread.start();
                } else {
                    verifyEncodingAndProcess(localFiles, ActionType.VIEW);
                }

            }
        };
        m_viewMzdb = new JMenuItem("View");
        m_viewMzdb.addActionListener(m_viewMzdbAction);
        popupMenu.add(m_viewMzdb);

        // detect peakels
        m_detectPeakels = new JMenuItem("Detect peakels");
        m_detectPeakels.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {

                ArrayList<File> localFiles = getSelectedRawFiles(WorkingSetEntry.Location.LOCAL);
                ArrayList<WorkingSetEntry> remoteEntries = getSelectedWorkingSetEntries(WorkingSetEntry.Location.REMOTE);
                ArrayList<File> totalFiles = new ArrayList<File>();

                if (!remoteEntries.isEmpty()) {
                    MsListener msListener = new MsListener() {

                        @Override
                        public void conversionPerformed(ArrayList<MsListenerConverterParameter> list) {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }

                        @Override
                        public void uploadPerformed(ArrayList<MsListenerParameter> list) {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }

                        @Override
                        public void downloadPerformed(ArrayList<MsListenerDownloadParameter> list) {

                            totalFiles.addAll(localFiles);
                            for (int i = 0; i < list.size(); i++) {
                                MsListenerDownloadParameter p = list.get(i);
                                if (p.wasSuccessful()) {
                                    File newFile = p.getDestinationFile();
                                    if (newFile.exists()) {
                                        replaceEntry(p.getRemoteFile(), newFile);
                                        totalFiles.add(newFile);
                                    }
                                }
                            }
                            verifyEncodingAndProcess(totalFiles, ActionType.DETECT_PEAKELS);
                        }

                        @Override
                        public void exportPerformed(ArrayList<MsListenerParameter> list) {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }

                        @Override
                        public void verificationPerformed(ArrayList<MsListenerParameter> list) {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }

                        @Override
                        public void entryStateUpdated(ArrayList<MsListenerEntryUpdateParameter> list) {
                            updateDownloadIndex(list);
                        }

                    };

                    String localDirPath = WorkingSetUtil.getTempDirectory().getAbsolutePath();

                    MzdbDownloadBatch downloadBatch = new MzdbDownloadBatch(localDirPath, "", remoteEntries);
                    downloadBatch.addMsListener(msListener);
                    Thread downloadThread = new Thread(downloadBatch);
                    downloadThread.start();

                } else {
                    verifyEncodingAndProcess(localFiles, ActionType.DETECT_PEAKELS);
                }
            }
        });
        popupMenu.add(m_detectPeakels);

        m_addWorkingSet = new JMenuItem("Add a working set");
        m_addWorkingSet.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                AddWorkingSetDialog dialog = AddWorkingSetDialog.getDialog(null, m_root);
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);

                reloadAndSave();
            }

        });
        popupMenu.add(m_addWorkingSet);

        m_renameWorkingSet = new JMenuItem("Rename working set");
        m_renameWorkingSet.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                String message = "Rename working set";
                String text = JOptionPane.showInputDialog(null, message);
                if (text != null) {
                    if (m_selectedWorkingSets.size() == 1) {
                        m_workingSetModel.updateJSONObject(WorkingSetModel.JSONObjectType.WORKING_SET, m_selectedWorkingSets.get(0).getName(), "name", text);
                        m_selectedWorkingSets.get(0).setName(text);

                        reloadAndSave();

                    }
                }
            }

        });
        popupMenu.add(m_renameWorkingSet);

        m_changeDescriptionAtWorkingSet = new JMenuItem("Change description");
        m_changeDescriptionAtWorkingSet.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                String message = "Change description";
                String text = JOptionPane.showInputDialog(null, message);
                if (text != null) {
                    if (m_selectedWorkingSets.size() == 1) {
                        m_workingSetModel.updateJSONObject(WorkingSetModel.JSONObjectType.WORKING_SET, m_selectedWorkingSets.get(0).getName(), "description", text);
                        m_selectedWorkingSets.get(0).setDescription(text);

                        reloadAndSave();

                    }
                }
            }
        });
        popupMenu.add(m_changeDescriptionAtWorkingSet);

        m_removeWorkingSet = new JMenuItem("Remove a working set");
        m_removeWorkingSet.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {

                DefaultMutableTreeNode root = (DefaultMutableTreeNode) m_workingSetModel.getRoot();
                WorkingSetRoot workingSetRoot = (WorkingSetRoot) root.getUserObject();

                boolean success = false;

                for (int i = 0; i < m_selectedWorkingSets.size(); i++) {
                    if (workingSetRoot.removeWorkingSet(m_selectedWorkingSets.get(i).getName())) {
                        success = true;
                        break;
                    }
                }

                if (success) {
                    reloadModel();
                    resetTreeState();
                    WorkingSetUtil.saveJSON(workingSetRoot.getWorkingSets());
                }

            }

        });
        popupMenu.add(m_removeWorkingSet);

        m_addWorkingSetEntry = new JMenuItem("Add an entry");
        m_addWorkingSetEntry.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {

                AddlWorkingSetEntryDialog dialog = AddlWorkingSetEntryDialog.getDialog(null, m_selectedWorkingSets.get(0));
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);

                TreePath pathToExpand = m_tree.getSelectionPaths()[0];

                m_tree.expandPath(pathToExpand);

                reloadAndSave();
            }

        });
        popupMenu.add(m_addWorkingSetEntry);

        m_removeWorkingSetEntry = new JMenuItem("Remove an entry");
        m_removeWorkingSetEntry.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                if (!m_selectedWorkingSetEntries.isEmpty() || !m_existingSelectedWorkingSetEntries.isEmpty()) {

                    boolean success = false;

                    for (int i = 0; i < m_selectedWorkingSetEntries.size(); i++) {
                        if (m_selectedWorkingSetEntries.get(i).getParent().removeEntry(m_selectedWorkingSetEntries.get(i).getPath())) {
                            success = true;
                        }
                    }

                    for (int i = 0; i < m_existingSelectedWorkingSetEntries.size(); i++) {
                        if (m_existingSelectedWorkingSetEntries.get(i).getParent().removeEntry(m_existingSelectedWorkingSetEntries.get(i).getPath())) {
                            success = true;
                        }
                    }

                    if (success) {
                        reloadAndSave();
                    }

                }
            }

        });
        popupMenu.add(m_removeWorkingSetEntry);
    }

    public void resetTreeState() {
        TreeUtils.setExpansionState(TreeUtils.loadExpansionState(TreeUtils.TreeType.WORKING_SET, null), m_tree, (DefaultMutableTreeNode) m_tree.getModel().getRoot(), TreeUtils.TreeType.WORKING_SET, null);
    }

    @Override
    public void updatePopupMenu() {
        updateSelectedObjects();

        setPopupEnabled(false);

        if (m_selectedRoot != null && m_selectedWorkingSets.isEmpty() && m_selectedWorkingSetEntries.isEmpty()) {
            m_addWorkingSet.setEnabled(true);
        }

        if (m_selectedRoot == null && m_selectedWorkingSetEntries.isEmpty() && m_selectedWorkingSets.size() == 1) {
            m_renameWorkingSet.setEnabled(true);
        }

        if (m_selectedRoot == null && m_selectedWorkingSetEntries.isEmpty() && m_selectedWorkingSets.size() == 1) {
            m_changeDescriptionAtWorkingSet.setEnabled(true);
        }

        if (m_selectedRoot == null && m_selectedWorkingSets.size() == 1 && m_selectedWorkingSetEntries.isEmpty()) {
            m_addWorkingSetEntry.setEnabled(true);
        }

        if (m_selectedRoot == null && m_selectedWorkingSets.isEmpty() && !m_selectedWorkingSetEntries.isEmpty()) {
            m_removeWorkingSetEntry.setEnabled(true);
        }

        if (m_selectedRoot == null && !m_selectedWorkingSets.isEmpty() && m_selectedWorkingSetEntries.isEmpty()) {
            m_removeWorkingSet.setEnabled(true);
        }

        if (!m_existingSelectedWorkingSetEntries.isEmpty()) {
            m_viewMzdb.setEnabled(true);
            m_detectPeakels.setEnabled(true);
        }

    }

    private void updateDownloadIndex(ArrayList<MsListenerEntryUpdateParameter> list) {
        for (int i = 0; i < list.size(); i++) {
            MsListenerEntryUpdateParameter parameter = list.get(i);
            if (parameter.getState() == MsListenerEntryUpdateParameter.State.DOWNLOAD_COMPLETE) {
                m_downloadIndex.remove(parameter.getFile().getAbsolutePath());
            } else {
                m_downloadIndex.add(parameter.getFile().getAbsolutePath());
            }
        }
    }

    private void setPopupEnabled(boolean b) {
        m_addWorkingSet.setEnabled(b);
        m_renameWorkingSet.setEnabled(b);
        m_changeDescriptionAtWorkingSet.setEnabled(b);
        m_removeWorkingSet.setEnabled(b);
        m_addWorkingSetEntry.setEnabled(b);
        m_removeWorkingSetEntry.setEnabled(b);
        m_viewMzdb.setEnabled(b);
        m_detectPeakels.setEnabled(b);
    }

    @Override
    public ActionListener getDefaultAction() {
        return null;
    }

    private void displayRaw(ArrayList<File> rawfiles) {
        MzScope mzScope = new MzScope(MzdbInfo.MZSCOPE_VIEW, rawfiles);
        MzScopeWindowBoxManager.addMzdbScope(mzScope);
    }

    private void detectPeakels(ArrayList<File> rawfiles) {
        MzScope mzScope = new MzScope(MzdbInfo.MZSCOPE_DETECT_PEAKEL, rawfiles);
        MzScopeWindowBoxManager.addMzdbScope(mzScope);
    }

    private void updateSelectedObjects() {

        m_selectedWorkingSets.clear();
        m_selectedWorkingSetEntries.clear();
        m_existingSelectedWorkingSetEntries.clear();
        m_selectedRoot = null;

        TreePath[] paths = m_tree.getSelectionPaths();
        if (paths != null) {
            for (TreePath path : paths) {

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

                Object userObject = node.getUserObject();

                if (userObject instanceof WorkingSetRoot) {
                    m_selectedRoot = (WorkingSetRoot) userObject;
                } else if (userObject instanceof WorkingSet) {
                    m_selectedWorkingSets.add((WorkingSet) userObject);
                } else if (userObject instanceof WorkingSetEntry) {
                    WorkingSetEntry entry = (WorkingSetEntry) userObject;
                    m_selectedWorkingSetEntries.add(entry);
                    if (entry.exists()) {
                        m_existingSelectedWorkingSetEntries.add(entry);
                    }
                }
            }
        }

    }

    private ArrayList<File> getSelectedRawFiles(WorkingSetEntry.Location location) {
        ArrayList<File> files = new ArrayList<File>();

        for (int i = 0; i < m_selectedWorkingSetEntries.size(); i++) {
            WorkingSetEntry entry = m_selectedWorkingSetEntries.get(i);
            if (entry.getLocation() == location) {
                files.add(entry.getFile());
            }
        }

        return files;
    }

    private ArrayList<WorkingSetEntry> getSelectedWorkingSetEntries(WorkingSetEntry.Location location) {
        ArrayList<WorkingSetEntry> entries = new ArrayList<WorkingSetEntry>();

        for (int i = 0; i < m_selectedWorkingSetEntries.size(); i++) {
            WorkingSetEntry entry = m_selectedWorkingSetEntries.get(i);
            if (entry.getLocation() == location) {
                entries.add(entry);
            }
        }

        return entries;
    }

    private void reloadModel() {
        if (m_workingSetModel != null) {
            m_tree.setModel(null);
            m_workingSetModel = new WorkingSetModel(m_root);
            m_tree.setModel(m_workingSetModel);
        }
    }

    private void replaceEntry(File oldFile, File newFile) {
        HashMap<String, ArrayList<WorkingSet>> index = m_workingSetModel.getAssociationsIndex();
        if (index.containsKey(oldFile.getAbsolutePath())) {
            ArrayList<WorkingSet> list = index.get(oldFile.getAbsolutePath());
            for (int i = 0; i < list.size(); i++) {
                WorkingSet set = list.get(i);
                set.removeEntry(oldFile.getAbsolutePath());
                set.addEntry(newFile.getAbsolutePath(), WorkingSetEntry.Location.LOCAL);
            }
        }
    }

    private void verifyEncodingAndProcess(ArrayList<File> totalFiles, ActionType actionType) {
        MsListener listener = new MsListener() {

            @Override
            public void conversionPerformed(ArrayList<MsListenerConverterParameter> list) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void uploadPerformed(ArrayList<MsListenerParameter> list) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void downloadPerformed(ArrayList<MsListenerDownloadParameter> list) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void exportPerformed(ArrayList<MsListenerParameter> list) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void verificationPerformed(ArrayList<MsListenerParameter> list) {

                reloadAndSave();

                ArrayList<File> verifiedFiles = new ArrayList<File>();

                for (int i = 0; i < list.size(); i++) {
                    MsListenerParameter p = list.get(i);
                    if (p.wasSuccessful()) {
                        verifiedFiles.add(p.getFile());
                    }
                }

                if (verifiedFiles.size() > 0) {

                    if (actionType == ActionType.DETECT_PEAKELS) {
                        detectPeakels(verifiedFiles);
                    } else if (actionType == ActionType.VIEW) {
                        displayRaw(verifiedFiles);
                    }
                }

            }

            @Override
            public void entryStateUpdated(ArrayList<MsListenerEntryUpdateParameter> list) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };

        MzdbEncodingVerificationBatch batch = new MzdbEncodingVerificationBatch(totalFiles);
        batch.addMsListener(listener);
        Thread thread = new Thread(batch);
        thread.start();
    }

    public void reloadAndSave() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) m_workingSetModel.getRoot();
        WorkingSetRoot workingSetRoot = (WorkingSetRoot) root.getUserObject();
        WorkingSetUtil.saveJSON(workingSetRoot.getWorkingSets());
        reloadModel();
        resetTreeState();
    }

    public WorkingSetModel getModel() {
        return m_workingSetModel;
    }

    public void expand(TreePath path) {
        m_tree.expandPath(path);
    }

}
