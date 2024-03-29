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
package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.NbPreferences;
import fr.proline.studio.msfiles.ExportMgfDialog;
import fr.proline.studio.msfiles.FileDeletionBatch;
import fr.proline.mzscope.utils.IPopupMenuDelegate;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.msfiles.FileToTransfer;
import fr.proline.studio.msfiles.MsFilesExplorer;
import fr.proline.studio.msfiles.MzdbEncodingVerificationBatch;
import fr.proline.studio.mzscope.MzdbInfo;
import fr.proline.studio.pattern.MzScopeWindowBoxManager;
import fr.proline.studio.rsmexplorer.gui.dialog.ConvertRawDialog;
import fr.proline.studio.rsmexplorer.gui.dialog.UploadMzdbDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import fr.proline.studio.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author AK249877
 */
public class LocalFileSystemView extends JPanel implements IPopupMenuDelegate {

    private LocalFileSystemModel m_fileSystemDataModel;
    private JTree m_tree;
    private JPopupMenu m_popupMenu;
    private JMenuItem m_detectPeakelsItem, m_viewRawFileItem, m_convertRawFileItem, m_uploadMzdbFileItem, m_exportMgfItem, m_deleteFileItem, m_verifyEncodingItem;
    private ActionListener viewRawFileAction;
    private ArrayList<File> m_selectedFiles;
    private final LocalFileSystemTransferHandler m_transferHandler;
    private boolean m_showUpdateButton;
    private JComboBox m_rootsComboBox;
    private Preferences m_preferences;

    private MsFilesExplorer.FileSelectionInterface m_fileSelectionInterface = null;
    
    private HashSet<String> m_paths;

    private boolean m_updating;

    protected static final Logger logger = LoggerFactory.getLogger("ProlineStudio.LocalFileSystemView");

    private static final String LOCAL_FILE_SYSTEM_LIST_KEY = "LOCAL_FILE_SYSTEM_VIEW";
    private static final String DRIVE_PARAM_KEY = "DRIVE_KEY";

    public LocalFileSystemView(LocalFileSystemTransferHandler transferHandler) {
        m_transferHandler = transferHandler;
        m_preferences = NbPreferences.root();
        m_paths = new HashSet<String>();
        initComponents();
    }

    public LocalFileSystemView(LocalFileSystemTransferHandler transferHandler, boolean showUpdateButton) {
        this(transferHandler);
        m_showUpdateButton = showUpdateButton;
        m_updating = false;
    }

    private void initComponents() {

        m_selectedFiles = new ArrayList<File>();

        setBorder(BorderFactory.createTitledBorder("Local File System"));

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 0, 0, 0);

        c.gridy = 0;
        c.gridx = 0;

        c.gridheight = 1;

        c.weighty = 0;
        c.weightx = 0;


        JButton updateButton = new JButton(IconManager.getIcon(IconManager.IconType.REFRESH));
        updateButton.setToolTipText("Refresh");
        updateButton.setFocusPainted(false);
        updateButton.setOpaque(true);
        updateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!m_updating) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateTree();
                        }
                    });
                }
            }
        });

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        toolbar.add(updateButton);

        add(toolbar, c);

        c.gridy++;

        add(Box.createVerticalBox(), c);

        c.gridy = 0;

        c.gridheight = 2;

        c.gridx++;


        JPanel treePanel = new JPanel();
        treePanel.setLayout(new BorderLayout(0, 5));

        File[] roots = File.listRoots();
        m_rootsComboBox = new JComboBox(roots);

        m_rootsComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ie) {

                reloadTree();
                resetTreeState();
                m_preferences.put(LOCAL_FILE_SYSTEM_LIST_KEY + "." + DRIVE_PARAM_KEY, m_rootsComboBox.getSelectedItem().toString());

            }

        });

        treePanel.add(m_rootsComboBox, BorderLayout.NORTH);

        m_popupMenu = new JPopupMenu();
        initPopupMenu(m_popupMenu);

        //m_fileSystemDataModel = new LocalFileSystemModel(roots[0].getAbsolutePath());
        m_fileSystemDataModel = new LocalFileSystemModel(m_rootsComboBox.getSelectedItem().toString());
        m_tree = new JTree(m_fileSystemDataModel);

        m_tree.setCellRenderer(new DefaultTreeCellRenderer() {

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean isLeaf, int row, boolean focused) {
                Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);

                if (((DefaultMutableTreeNode) value).isRoot()) {
                    setIcon(IconManager.getIcon(IconManager.IconType.DRIVE));
                } else {
                    File f = (File) ((DefaultMutableTreeNode) value).getUserObject();
                    if (f.isDirectory()) {
                        if (expanded) {
                            setIcon(IconManager.getIcon(IconManager.IconType.FOLDER_EXPANDED));
                        } else {
                            setIcon(IconManager.getIcon(IconManager.IconType.FOLDER));
                        }
                    } else {
                        if (f.getAbsolutePath().toLowerCase().endsWith(".raw")) {
                            setIcon(IconManager.getIcon(IconManager.IconType.SPECTRUM));
                        } else if (f.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
                            setIcon(IconManager.getIcon(IconManager.IconType.SPECTRUM_EMISSION));
                        } else {
                            setIcon(IconManager.getIcon(IconManager.IconType.FILE));
                        }
                    }
                }

                return c;
            }
        });

        HashSet<String> set = TreeUtils.loadExpansionState(TreeUtils.TreeType.LOCAL, m_rootsComboBox.getSelectedItem().toString());

        TreeUtils.setExpansionState(set, m_tree, (DefaultMutableTreeNode) m_tree.getModel().getRoot(), TreeUtils.TreeType.LOCAL, m_rootsComboBox.getSelectedItem().toString());

        m_tree.addTreeExpansionListener(new TreeExpansionListener() {

            @Override
            public void treeExpanded(TreeExpansionEvent tee) {
                TreeUtils.saveExpansionState(m_tree, TreeUtils.TreeType.LOCAL, m_rootsComboBox.getSelectedItem().toString());
                DefaultMutableTreeNode expandedNode = (DefaultMutableTreeNode) tee.getPath().getLastPathComponent();

                traverseAndExpand(expandedNode);
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent tee) {
                TreeUtils.saveExpansionState(m_tree, TreeUtils.TreeType.LOCAL, m_rootsComboBox.getSelectedItem().toString());
            }

        });

        m_tree.setTransferHandler(m_transferHandler);
        m_tree.setDragEnabled(true);

        m_tree.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    updatePopupMenu();
                    m_popupMenu.show((JComponent) e.getSource(), e.getX(), e.getY());
                }
            }
        });
        
        m_tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                
                if (m_fileSelectionInterface == null) {
                    return;
                }
                
                ArrayList<FileToTransfer> files = new ArrayList<>();
                ArrayList<FileToTransfer> directories = new ArrayList<>();
                ArrayList<FileToTransfer> parentDirectory = new ArrayList<>();
                getSelectedFilesAndDirectories(files, directories, parentDirectory);
                m_fileSelectionInterface.upSelectionChanged(files, directories, parentDirectory);
  
            }
        });

        JScrollPane scrollPane = new JScrollPane(m_tree);
        treePanel.add(scrollPane, BorderLayout.CENTER);

        c.weightx = 1.0;
        c.weighty = 1.0;

        add(treePanel, c);

        initRoot();
    }

    public void setFileSelectionListener(MsFilesExplorer.FileSelectionInterface fileSelectionInterface) {
        m_fileSelectionInterface = fileSelectionInterface;
    }
    
    private void initRoot() {
        String previousDrive = m_preferences.get(LOCAL_FILE_SYSTEM_LIST_KEY + "." + DRIVE_PARAM_KEY, null);
        if (previousDrive != null) {
            ComboBoxModel model = m_rootsComboBox.getModel();
            int size = model.getSize();
            for (int i = 0; i < size; i++) {
                File f = (File) model.getElementAt(i);
                if (f.getAbsolutePath().equalsIgnoreCase(previousDrive)) {
                    m_rootsComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private ArrayList<String> getSelectedURLs() {

        m_selectedFiles.clear();

        ArrayList<String> selectedURLs = new ArrayList<String>();
        TreePath[] paths = m_tree.getSelectionPaths();
        if (paths != null) {
            for (TreePath path : paths) {

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                File f = (File) node.getUserObject();

                if (f.isFile()) {
                    selectedURLs.add(f.getAbsolutePath());
                    m_selectedFiles.add(f);
                }

            }
        }
        return selectedURLs;
    }
    
    private void getSelectedFilesAndDirectories(ArrayList<FileToTransfer> files, ArrayList<FileToTransfer> directories, ArrayList<FileToTransfer> parentDirectory) {

        TreePath[] paths = m_tree.getSelectionPaths();
        if (paths != null) {
            for (TreePath path : paths) {

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                File f = (File) node.getUserObject();

                if (f.isFile()) {
                    
                    String url = f.getAbsolutePath().toLowerCase();
                    if (url.endsWith(".mzdb") || url.endsWith(".raw") || url.endsWith(".wiff") || url.endsWith(".dat")) {
                        files.add(new FileToTransfer(f,path));
                    }
                } else if (f.isDirectory()) {
                    directories.add(new FileToTransfer(f,path));
                }

            }

            // special case : if a file is selected and no directory
            // the parent directory is considerer as potential drop directory
            if (directories.isEmpty() && (files.size() == 1)) {
                for (TreePath path : paths) {

                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    File f = (File) node.getUserObject();

                    if (f.isFile()) {

                        String url = f.getAbsolutePath().toLowerCase();
                        if (url.endsWith(".mzdb") || url.endsWith(".raw") || url.endsWith(".wiff") || url.endsWith(".dat")) {
                            TreePath parentPath = path.getParentPath();
                            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
                            File parentFile = (File) parentNode.getUserObject();
                            if (parentFile.isDirectory()) {
                                parentDirectory.add(new FileToTransfer(parentFile, parentPath));
                                break;
                            }
                        }
                    }

                }
            }
        }
    }
    

    public void resetTreeState() {
        TreeUtils.setExpansionState(TreeUtils.loadExpansionState(TreeUtils.TreeType.LOCAL, m_rootsComboBox.getSelectedItem().toString()), m_tree, (DefaultMutableTreeNode) m_tree.getModel().getRoot(), TreeUtils.TreeType.LOCAL, m_rootsComboBox.getSelectedItem().toString());
    }

    public void expandTreePath(TreePath path) {
        m_tree.expandPath(path);
    }

    public void expandMultipleTreePath(HashSet<String> directories) {

        for (String s : directories) {
            m_paths.add(s);
        }

        traverseAndExpand((DefaultMutableTreeNode) m_fileSystemDataModel.getRoot());

    }

    private void traverseAndExpand(DefaultMutableTreeNode root) {

        /*if (m_paths.size() <= 0) {
            return;
        }*/

        boolean found = false;

        String rootAbsolutePath = ((File) root.getUserObject()).getAbsolutePath();

        for (String s : m_paths) {
            if (s.contains(rootAbsolutePath)) {
                found = true;
                break;
            }
        }

        if (!found) {
            return;
        }

        Enumeration totalNodes = root.depthFirstEnumeration();

        while (totalNodes.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) totalNodes.nextElement();

            File f = (File) node.getUserObject();

            String absolutePath = f.getAbsolutePath();

            if (m_paths.contains(absolutePath)) {
                m_paths.remove(node.toString());
                TreePath tp = new TreePath(node.getPath());

                if (!m_tree.isExpanded(tp)) {
                    m_tree.expandPath(new TreePath(node.getPath()));
                } else {
                    m_fileSystemDataModel.fireTreeNodesChanged(new TreeModelEvent(node, node.getPath()));
                }

            }

        }
    }

    public void updateTree() {
        if (!m_updating) {
            m_updating = true;
            reloadTree();
            resetTreeState();
            m_updating = false;
        }
    }

    public void reloadTree() {
        if (m_fileSystemDataModel != null) {
            m_tree.setModel(null);
            m_fileSystemDataModel = new LocalFileSystemModel(m_rootsComboBox.getSelectedItem().toString());
            m_tree.setModel(m_fileSystemDataModel);
        }
    }

    private boolean isSelectionHomogeneous(ArrayList<String> selectedURLs) {
        if (selectedURLs.size() > 0) {
            int lastIndexOfDot = selectedURLs.get(0).lastIndexOf(".");
            if (lastIndexOfDot == -1 ) {
                return false;
            }
            String firstSuffix = selectedURLs.get(0).substring(lastIndexOfDot);
            for (String url : selectedURLs) {
                if (!url.endsWith(firstSuffix)) {
                    return false;
                }
            }
        }
        return true;

    }

    private void displayRaw(ArrayList<File> rawfiles) {
        MzScope mzScope = new MzScope(MzdbInfo.MZSCOPE_VIEW, rawfiles);
        MzScopeWindowBoxManager.addMzdbScope(mzScope);
    }

    private void detectPeakels(ArrayList<File> rawfiles) {
        MzScope mzScope = new MzScope(MzdbInfo.MZSCOPE_DETECT_PEAKEL, rawfiles);
        MzScopeWindowBoxManager.addMzdbScope(mzScope);
    }

    @Override
    public void initPopupMenu(JPopupMenu popupMenu) {

        // view data
        viewRawFileAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                displayRaw(m_selectedFiles);
            }
        };
        m_viewRawFileItem = new JMenuItem("View");
        m_viewRawFileItem.addActionListener(viewRawFileAction);
        popupMenu.add(m_viewRawFileItem);

        // detect peakels
        m_detectPeakelsItem = new JMenuItem("Detect Peakels");
        m_detectPeakelsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                detectPeakels(m_selectedFiles);
            }
        });
        popupMenu.add(m_detectPeakelsItem);

        // convert raw file
        m_convertRawFileItem = new JMenuItem("Convert to mzDB");
        m_convertRawFileItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                ConvertRawDialog dialog = ConvertRawDialog.getDialog(null);
                dialog.setFiles(m_selectedFiles);
                dialog.setVisible(true);
            }
        });
        popupMenu.add(m_convertRawFileItem);

        // verify and repair encoding
        m_verifyEncodingItem = new JMenuItem("Verify Encoding");
        m_verifyEncodingItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                MzdbEncodingVerificationBatch batch = new MzdbEncodingVerificationBatch(m_selectedFiles);
                Thread thread = new Thread(batch);
                thread.start();
            }

        });
        popupMenu.add(m_verifyEncodingItem);

        // upload mzdb file
        m_uploadMzdbFileItem = new JMenuItem("Upload to Server");
        m_uploadMzdbFileItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                UploadMzdbDialog dialog = UploadMzdbDialog.getDialog(WindowManager.getDefault().getMainWindow());
                dialog.setFiles(m_selectedFiles);
                dialog.setVisible(true);
            }
        });
        popupMenu.add(m_uploadMzdbFileItem);

        m_exportMgfItem = new JMenuItem("Export .mgf");
        m_exportMgfItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                ExportMgfDialog dialog = ExportMgfDialog.getDialog(null, "Export .mgf file(s)");
                dialog.setLocationRelativeTo(null);
                dialog.setFiles(m_selectedFiles);
                dialog.setVisible(true);

            }
        ;
        });
        popupMenu.add(m_exportMgfItem);

        m_deleteFileItem = new JMenuItem("Delete file(s)");
        m_deleteFileItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                FileDeletionBatch batch = new FileDeletionBatch(m_selectedFiles);
                Thread thread = new Thread(batch);
                thread.start();
            }

        });

        popupMenu.add(m_deleteFileItem);

    }

    @Override
    public void updatePopupMenu() {
        ArrayList<String> selectedURLs = getSelectedURLs();
        if (!selectedURLs.isEmpty()) {
            if (isSelectionHomogeneous(selectedURLs)) {
                String firstURL = selectedURLs.get(0).toLowerCase();
                if (firstURL.endsWith(".mzdb")) {
                    setPopupEnabled(true);
                    m_convertRawFileItem.setEnabled(false);
                    m_uploadMzdbFileItem.setEnabled(DatabaseDataManager.getDatabaseDataManager().getLoggedUser()!=null);
                } else if (firstURL.endsWith(".raw") || firstURL.endsWith(".wiff")) {
                    setPopupEnabled(false);
                    m_convertRawFileItem.setEnabled(true);
                    m_deleteFileItem.setEnabled(true);
                } else if (firstURL.endsWith(".mgf")) {
                    setPopupEnabled(false);
                    m_deleteFileItem.setEnabled(true);
                } else {
                    setPopupEnabled(false);
                }
            } else {
                setPopupEnabled(false);
            }
        } else {
            setPopupEnabled(false);
        }
    }

    private void setPopupEnabled(boolean b) {
        m_viewRawFileItem.setEnabled(b);
        m_detectPeakelsItem.setEnabled(b);
        m_convertRawFileItem.setEnabled(b);
        m_verifyEncodingItem.setEnabled(b);
        m_uploadMzdbFileItem.setEnabled(b);
        m_exportMgfItem.setEnabled(b);
        m_deleteFileItem.setEnabled(b);
    }

    @Override
    public ActionListener getDefaultAction() {
        return null;
    }

    public String getSelectedRoot() {
        if (m_rootsComboBox != null) {
            return m_rootsComboBox.getSelectedItem().toString();
        } else {
            return "";
        }
    }

}
