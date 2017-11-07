/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import fr.proline.mzscope.utils.IPopupMenuDelegate;
import fr.proline.studio.msfiles.WorkingSetEntry.Location;
import fr.proline.studio.mzscope.MzdbInfo;
import fr.proline.studio.pattern.MzScopeWindowBoxManager;
import fr.proline.studio.rsmexplorer.gui.MzScope;
import fr.proline.studio.rsmexplorer.gui.TreeStateUtil;
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author AK249877
 */
public class WorkingSetView extends JPanel implements IPopupMenuDelegate {

    private WorkingSetModel m_workingSetModel;
    private JTree m_tree;
    private JMenuItem m_addWorkingSet, m_removeWorkingSet, m_addWorkingSetLocalEntry, m_removeWorkingSetEntry, m_viewMzdb, m_detectPeakels;
    private JPopupMenu m_popupMenu;
    private ActionListener m_viewMzdbAction;
    private WorkingSetRoot m_root;

    private ArrayList<WorkingSet> m_selectedWorkingSets;
    private ArrayList<WorkingSetEntry> m_selectedWorkingSetEntries;
    private ArrayList<WorkingSetEntry> m_existingSelectedWorkingSetEntries;
    private WorkingSetRoot m_selectedRoot;

    public WorkingSetView() {
        initComponents();
        resetTreeState();
    }

    public void initComponents() {

        m_selectedWorkingSets = new ArrayList<WorkingSet>();
        m_selectedWorkingSetEntries = new ArrayList<WorkingSetEntry>();
        m_existingSelectedWorkingSetEntries = new ArrayList<WorkingSetEntry>();
        m_selectedRoot = null;

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
                        }
                        if (entry.getLocation() == Location.LOCAL) {
                            setIcon(IconManager.getIcon(IconManager.IconType.DRIVE));
                        } else if (entry.getLocation() == Location.REMOTE) {
                            setIcon(IconManager.getIcon(IconManager.IconType.DRIVE_GLOBE));
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
                TreeStateUtil.saveExpansionState(m_tree, TreeStateUtil.TreeType.WORKING_SET, null);
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent tee) {
                TreeStateUtil.saveExpansionState(m_tree, TreeStateUtil.TreeType.WORKING_SET, null);
            }

        });

        JScrollPane scrollPane = new JScrollPane(m_tree);

        m_popupMenu = new JPopupMenu();
        initPopupMenu(m_popupMenu);

        add(scrollPane, c);

    }

    @Override
    public void initPopupMenu(JPopupMenu popupMenu) {
        // view data
        m_viewMzdbAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                displayRaw(extractSelectedRawFiles());
            }
        };
        m_viewMzdb = new JMenuItem("View");
        m_viewMzdb.addActionListener(m_viewMzdbAction);
        popupMenu.add(m_viewMzdb);

        // detect peakels
        m_detectPeakels = new JMenuItem("Detect Peakels");
        m_detectPeakels.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                detectPeakels(extractSelectedRawFiles());
            }
        });
        popupMenu.add(m_detectPeakels);

        m_addWorkingSet = new JMenuItem("Add Working Set");
        m_addWorkingSet.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                AddWorkingSetDialog dialog = AddWorkingSetDialog.getDialog(null, m_root);
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) m_workingSetModel.getRoot();
                m_workingSetModel.fireTreeStructureChanged(new TreeModelEvent(root, root.getPath()));
                resetTreeState();
            }

        });
        popupMenu.add(m_addWorkingSet);

        m_removeWorkingSet = new JMenuItem("Remove Working Set");
        m_removeWorkingSet.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {

                DefaultMutableTreeNode root = (DefaultMutableTreeNode) m_workingSetModel.getRoot();
                WorkingSetRoot workingSetRoot = (WorkingSetRoot) root.getUserObject();

                boolean success = false;

                for (int i = 0; i < m_selectedWorkingSets.size(); i++) {
                    if (workingSetRoot.removeWorkingSet(m_selectedWorkingSets.get(i).getName())) {
                        success = true;
                    }
                }

                if (success) {
                    m_workingSetModel.fireTreeStructureChanged(new TreeModelEvent(root, root.getPath()));
                    resetTreeState();
                    WorkingSetUtil.saveJSON(workingSetRoot.getWorkingSets());
                }

            }

        });
        popupMenu.add(m_removeWorkingSet);

        m_addWorkingSetLocalEntry = new JMenuItem("Add local entry");
        m_addWorkingSetLocalEntry.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                AddWorkingSetEntryDialog dialog = AddWorkingSetEntryDialog.getDialog(null, m_selectedWorkingSets.get(0));
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) m_workingSetModel.getRoot();
                m_workingSetModel.fireTreeStructureChanged(new TreeModelEvent(root, root.getPath()));
                resetTreeState();
                WorkingSetRoot workingSetRoot = (WorkingSetRoot) root.getUserObject();
                WorkingSetUtil.saveJSON(workingSetRoot.getWorkingSets());
            }

        });
        popupMenu.add(m_addWorkingSetLocalEntry);

        m_removeWorkingSetEntry = new JMenuItem("Remove Entry");
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
                        DefaultMutableTreeNode root = (DefaultMutableTreeNode) m_workingSetModel.getRoot();
                        WorkingSetRoot workingSetRoot = (WorkingSetRoot) root.getUserObject();
                        m_workingSetModel.fireTreeStructureChanged(new TreeModelEvent(root, root.getPath()));
                        resetTreeState();
                        WorkingSetUtil.saveJSON(workingSetRoot.getWorkingSets());
                    }

                }
            }

        });
        popupMenu.add(m_removeWorkingSetEntry);
    }

    public void resetTreeState() {
        TreeStateUtil.setExpansionState(TreeStateUtil.loadExpansionState(TreeStateUtil.TreeType.WORKING_SET, null), m_tree, (DefaultMutableTreeNode) m_tree.getModel().getRoot(), TreeStateUtil.TreeType.WORKING_SET, null);
    }

    @Override
    public void updatePopupMenu() {
        updateSelectedObjects();

        setPopupEnabled(false);

        if (m_selectedRoot != null && m_selectedWorkingSets.isEmpty() && m_selectedWorkingSetEntries.isEmpty()) {
            m_addWorkingSet.setEnabled(true);
        }

        if (m_selectedRoot == null && m_selectedWorkingSets.size() == 1 && m_selectedWorkingSetEntries.isEmpty()) {
            m_addWorkingSetLocalEntry.setEnabled(true);
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

    private void setPopupEnabled(boolean b) {
        m_addWorkingSet.setEnabled(b);
        m_removeWorkingSet.setEnabled(b);
        m_addWorkingSetLocalEntry.setEnabled(b);
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

    private ArrayList<File> extractSelectedRawFiles() {
        ArrayList<File> files = new ArrayList<File>();

        for (int i = 0; i < m_selectedWorkingSetEntries.size(); i++) {
            WorkingSetEntry entry = m_selectedWorkingSetEntries.get(i);
            files.add(entry.getFile());
        }

        return files;
    }


}
