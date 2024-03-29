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

import fr.proline.studio.msfiles.FileToTransfer;
import fr.proline.studio.msfiles.MsFilesExplorer;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author Matthew Robinson, Pavel Vorobiev, (modifications) JM235353
 */
public class TreeFileChooserPanel extends JPanel {

    protected TreeFileChooser m_tree;
    protected DefaultTreeModel m_model;
    private DefaultMutableTreeNode m_top;
    private FileSystemView m_fileSystemView;
    private TreeFileChooserTransferHandler m_transferHandler;
    private boolean m_showUpdateButton;
    private TreeUtils.TreeType m_treeType;

    private MsFilesExplorer.FileSelectionInterface m_fileSelectionInterface = null;
    
    public TreeFileChooserPanel(FileSystemView fileSystemView, TreeFileChooserTransferHandler transferHandler) {
        m_fileSystemView = fileSystemView;
        m_transferHandler = transferHandler;
        m_showUpdateButton = true;
        m_treeType = TreeUtils.TreeType.XIC;
    }

    public TreeFileChooserPanel(FileSystemView fileSystemView, TreeFileChooserTransferHandler transferHandler, boolean showUpdateButton) {
        this(fileSystemView, transferHandler);
        m_showUpdateButton = showUpdateButton;
        m_treeType = TreeUtils.TreeType.SERVER;
    }

    public void restoreTree(TreeUtils.TreeType type) {
        if (m_tree != null) {
            TreeUtils.setExpansionState(TreeUtils.loadExpansionState(type, null), m_tree, (DefaultMutableTreeNode) m_tree.getModel().getRoot(), type, null);
        }
    }

    public void initTree() {
        setLayout(new GridBagLayout());

        m_top = new DefaultMutableTreeNode(new IconData(IconManager.getIcon(IconManager.IconType.COMPUTER_NETWORK), null, "Server"));

        DefaultMutableTreeNode node;

        File[] roots = m_fileSystemView.getRoots();

        for (int k = 0; k < roots.length; k++) {
            node = new DefaultMutableTreeNode(new IconData(IconManager.getIcon(IconManager.IconType.FOLDER), IconManager.getIcon(IconManager.IconType.FOLDER_EXPANDED), new FileNode(roots[k])));
            m_top.add(node);
            node.add(new DefaultMutableTreeNode(Boolean.TRUE));
        }

        m_model = new DefaultTreeModel(m_top);
        m_tree = new TreeFileChooser(m_model);

        m_tree.addTreeExpansionListener(new TreeExpansionListener() {

            @Override
            public void treeExpanded(TreeExpansionEvent tee) {
                TreeUtils.saveExpansionState(m_tree, m_treeType, null);
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent tee) {
                TreeUtils.saveExpansionState(m_tree, m_treeType, null);
            }

        });

        if (m_transferHandler != null) {
            m_tree.setTransferHandler(m_transferHandler);
        } else {
            m_tree.setTransferHandler(new TreeFileChooserTransferHandler());
        }

        m_tree.setDragEnabled(true);

        m_tree.putClientProperty("JTree.lineStyle", "Angled");

        TreeCellRenderer renderer = new IconCellRenderer();
        m_tree.setCellRenderer(renderer);

        m_tree.addTreeExpansionListener(new DirExpansionListener());

        
        m_tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {

                if (m_fileSelectionInterface == null) {
                    return;
                }
                
                ArrayList<FileToTransfer> files = new ArrayList<>();
                ArrayList<FileToTransfer> directories = new ArrayList<>();
                ArrayList<FileToTransfer> parentDirectory = new ArrayList<>();
                getSelectedFilesAndDirectories(files, directories, parentDirectory);
                m_fileSelectionInterface.downSelectionChanged(files, directories, parentDirectory);
  
            }
        });
        
        m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        m_tree.setShowsRootHandles(true);
        m_tree.setEditable(false);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().add(m_tree);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(0, 0, 0, 0);

        c.gridx = 0;
        c.gridy = 0;

        c.gridheight = 1;

        c.weightx = 0.0;
        c.weighty = 0.0;

        if (m_showUpdateButton) {
            JButton updateButton = new JButton(IconManager.getIcon(IconManager.IconType.REFRESH));
            updateButton.setFocusPainted(false);
            updateButton.setOpaque(true);
            updateButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    updateTree();
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
        }

        c.weightx = 1.0;
        c.weighty = 1.0;

        add(scrollPane, c);
    }

    public void setFileSelectionListener(MsFilesExplorer.FileSelectionInterface fileSelectionInterface) {
        m_fileSelectionInterface = fileSelectionInterface;
    }
    
    public void updateTree() {

        HashSet<String> expandedPaths = TreeUtils.getExpansionState(m_tree);

        m_top.removeAllChildren();
        m_model.reload(m_top);

        DefaultMutableTreeNode node;

        File[] roots = m_fileSystemView.getRoots();

        for (int k = 0; k < roots.length; k++) {
            node = new DefaultMutableTreeNode(new IconData(IconManager.getIcon(IconManager.IconType.FOLDER), IconManager.getIcon(IconManager.IconType.FOLDER_EXPANDED), new FileNode(roots[k])));
            m_top.add(node);
            node.add(new DefaultMutableTreeNode(Boolean.TRUE));
        }

        m_model.reload();

        TreeUtils.setExpansionState(expandedPaths, m_tree, (DefaultMutableTreeNode) m_tree.getModel().getRoot(), TreeUtils.TreeType.SERVER, null);
    }

    public static TreePath getPath(TreeNode treeNode) {
        ArrayList<Object> nodes = new ArrayList<Object>();
        if (treeNode != null) {
            nodes.add(treeNode);
            treeNode = treeNode.getParent();
            while (treeNode != null) {
                nodes.add(0, treeNode);
                treeNode = treeNode.getParent();
            }
        }

        return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
    }

    public void expandTreePath(TreePath path) {
        m_tree.expandPath(path);
    }

    public void expandMultipleTreePath(HashSet<String> directories, String pathLabel) {
        
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) m_model.getRoot();
        Enumeration totalNodes = root.depthFirstEnumeration();

        while (totalNodes.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) totalNodes.nextElement();

            TreePath nodePath = new TreePath(node.getPath());

            if (node.toString().toLowerCase().equalsIgnoreCase(pathLabel)) {

                m_tree.getModel().addTreeModelListener(new TreeModelListener() {

                    @Override
                    public void treeNodesChanged(TreeModelEvent tme) {
                        ;
                    }

                    @Override
                    public void treeNodesInserted(TreeModelEvent tme) {
                        ;
                    }

                    @Override
                    public void treeNodesRemoved(TreeModelEvent tme) {
                        ;
                    }

                    @Override
                    public void treeStructureChanged(TreeModelEvent tme) {
                        Enumeration mountingPointChildren = node.children();

                        while (mountingPointChildren.hasMoreElements()) {

                            DefaultMutableTreeNode child = (DefaultMutableTreeNode) mountingPointChildren.nextElement();

                            if (directories.contains(child.toString())) {
                                m_tree.expandPath(new TreePath(child.getPath()));
                            }
                        }

                    }

                });

                m_tree.expandPath(nodePath);

                break;
            }
        }
    }

    public JTree getTree() {
        return m_tree;
    }

    DefaultMutableTreeNode getTreeNode(TreePath path) {
        return (DefaultMutableTreeNode) (path.getLastPathComponent());
    }

    public static FileNode getFileNode(DefaultMutableTreeNode node) {
        if (node == null) {
            return null;
        }
        Object obj = node.getUserObject();
        if (obj instanceof IconData) {
            obj = ((IconData) obj).getObject();
        }
        if (obj instanceof FileNode) {
            return (FileNode) obj;
        } else {
            return null;
        }
    }

    // Make sure expansion is threaded and updating the tree model
    // only occurs within the event dispatching thread.
    class DirExpansionListener implements TreeExpansionListener {

        @Override
        public void treeExpanded(TreeExpansionEvent event) {
            final DefaultMutableTreeNode node = getTreeNode(
                    event.getPath());
            final FileNode fnode = getFileNode(node);

            Thread runner = new Thread() {
                @Override
                public void run() {
                    if (fnode != null && fnode.expand(node)) {
                        Runnable runnable = new Runnable() {
                            public void run() {
                                m_model.reload(node);
                            }
                        };
                        SwingUtilities.invokeLater(runnable);
                    }
                }
            };
            runner.start();
        }

        @Override
        public void treeCollapsed(TreeExpansionEvent event) {
        }
    }

    private void getSelectedFilesAndDirectories(ArrayList<FileToTransfer> files, ArrayList<FileToTransfer> directories, ArrayList<FileToTransfer> parentDirectory) {
        
        TreePath[] paths = m_tree.getSelectionPaths();
        if (paths != null) {
            for (TreePath path : paths) {

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object data = node.getUserObject();
                if (data instanceof IconData) {
                    Object extraData = ((IconData) data).getObject();
                    if (extraData instanceof FileNode) {
                        File f = ((FileNode) extraData).getFile();

                        if (f.isFile()) {
                            files.add(new FileToTransfer(f, path));
                        } else if (f.isDirectory()) {
                            directories.add(new FileToTransfer(f, path));
                        }
                    } else {
                        files.clear();
                        directories.clear();
                        return;
                    }
                }
            }
            
            // special case : if a file is selected and no directory
            // the parent directory is considerer as potential drop directory
            if (directories.isEmpty() && (files.size() == 1)) {
                for (TreePath path : paths) {

                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    Object data = node.getUserObject();
                    if (data instanceof IconData) {
                        Object extraData = ((IconData) data).getObject();
                        if (extraData instanceof FileNode) {
                            File f = ((FileNode) extraData).getFile();

                            if (f.isFile()) {
                                TreePath parentPath = path.getParentPath();
                                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
                                Object parentData = parentNode.getUserObject();
                                if (parentData instanceof IconData) {
                                    Object parentExtraData = ((IconData) parentData).getObject();
                                    if (parentExtraData instanceof FileNode) {
                                        File parentFile = ((FileNode) parentExtraData).getFile();
                                        parentDirectory.add(new FileToTransfer(parentFile, parentPath));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public class FileNode {

        protected File m_file;

        public FileNode(File file) {
            m_file = file;
        }

        public File getFile() {
            return m_file;
        }

        @Override
        public String toString() {
            return m_file.getName().length() > 0 ? m_file.getName()
                    : m_file.getPath();
        }

        public boolean expand(DefaultMutableTreeNode parent) {
            DefaultMutableTreeNode flag = (DefaultMutableTreeNode) parent.getFirstChild();
            if (flag == null) {
                // No flag
                return false;
            }

            Object obj = flag.getUserObject();
            if (!(obj instanceof Boolean)) {
                return false;      // Already expanded
            } else {

            }

            parent.removeAllChildren();  // Remove Flag

            File[] files = m_file.listFiles();
            if (files == null) {
                return true;
            }

            ArrayList v = new ArrayList();

            for (int k = 0; k < files.length; k++) {
                File f = files[k];

                FileNode newNode = new FileNode(f);

                boolean isAdded = false;
                for (int i = 0; i < v.size(); i++) {
                    FileNode nd = (FileNode) v.get(i);
                    if (newNode.compareTo(nd) < 0) {
                        v.add(i, newNode);
                        isAdded = true;
                        break;
                    }
                }
                if (!isAdded) {
                    v.add(newNode);
                }
            }

            for (int i = 0; i < v.size(); i++) {
                FileNode nd = (FileNode) v.get(i);
                boolean isDirectory = nd.getFile().isDirectory();
                IconData idata = new IconData(IconManager.getIcon(isDirectory ? IconManager.IconType.FOLDER : IconManager.IconType.FILE), isDirectory ? IconManager.getIcon(IconManager.IconType.FOLDER_EXPANDED) : null, nd);
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(idata);
                parent.add(node);

                if (nd.hasSubDirs()) {
                    node.add(new DefaultMutableTreeNode(Boolean.TRUE));
                }
            }

            return true;
        }

        public boolean hasSubDirs() {
            return (m_file.isDirectory());

            /*File[] files = m_file.listFiles();
             if (files == null) {
             return false;
             }
             return (files.length > 0);*/
        }

        public int compareTo(FileNode toCompare) {
            return m_file.getName().compareToIgnoreCase(
                    toCompare.m_file.getName());
        }

    }

}

class IconCellRenderer extends DefaultTreeCellRenderer {


    public IconCellRenderer() {
        super();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean isLeaf, int row, boolean hasFocus) {
        Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, hasFocus);
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object obj = node.getUserObject();
        setText(obj.toString());

        if (obj instanceof Boolean) {
            setText("Loading data...");
            setIcon(IconManager.getIcon(IconManager.IconType.HOUR_GLASS_MINI16));
        } else if (obj instanceof IconData) {
            IconData idata = (IconData) obj;
            if (expanded) {
                setIcon(idata.getExpandedIcon());
            } else {
                setIcon(idata.getIcon());
            }
        } else {
            setIcon(null);
        }
        return c;
    }

}

class IconData {

    protected Icon m_icon;
    protected Icon m_expandedIcon;
    protected Object m_data;

    public IconData(Icon icon, Object data) {
        m_icon = icon;
        m_expandedIcon = null;
        m_data = data;
    }

    public IconData(Icon icon, Icon expandedIcon, Object data) {
        m_icon = icon;
        m_expandedIcon = expandedIcon;
        m_data = data;
    }

    public Icon getIcon() {
        return m_icon;
    }

    public Icon getExpandedIcon() {
        return m_expandedIcon != null ? m_expandedIcon : m_icon;
    }

    public Object getObject() {
        return m_data;
    }

    @Override
    public String toString() {
        return m_data.toString();
    }

}
