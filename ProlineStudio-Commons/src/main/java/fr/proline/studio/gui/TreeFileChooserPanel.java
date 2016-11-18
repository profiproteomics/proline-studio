package fr.proline.studio.gui;

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
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
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

    public TreeFileChooserPanel(FileSystemView fileSystemView, TreeFileChooserTransferHandler transferHandler) {

        m_fileSystemView = fileSystemView;

        setLayout(new GridBagLayout());

        m_top = new DefaultMutableTreeNode(new IconData(IconManager.getIcon(IconManager.IconType.COMPUTER_NETWORK), null, "Server"));

        DefaultMutableTreeNode node;

        File[] roots = fileSystemView.getRoots();
        for (int k = 0; k < roots.length; k++) {
            node = new DefaultMutableTreeNode(new IconData(IconManager.getIcon(IconManager.IconType.FOLDER), IconManager.getIcon(IconManager.IconType.FOLDER_EXPANDED), new FileNode(roots[k])));
            m_top.add(node);
            node.add(new DefaultMutableTreeNode(Boolean.TRUE));
        }

        m_model = new DefaultTreeModel(m_top);
        m_tree = new TreeFileChooser(m_model);

        if (transferHandler != null) {
            m_tree.setTransferHandler(transferHandler);
        } else {
            m_tree.setTransferHandler(new TreeFileChooserTransferHandler());
        }

        m_tree.setDragEnabled(true);

        m_tree.putClientProperty("JTree.lineStyle", "Angled");

        TreeCellRenderer renderer = new IconCellRenderer();
        m_tree.setCellRenderer(renderer);

        m_tree.addTreeExpansionListener(new DirExpansionListener());

        m_tree.addTreeSelectionListener(new DirSelectionListener());

        m_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        m_tree.setShowsRootHandles(true);
        m_tree.setEditable(false);

        JScrollPane s = new JScrollPane();
        s.getViewport().add(m_tree);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 3;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(s, c);

        c.gridx++;
        c.gridheight = 1;
        c.weightx = 0;
        c.weighty = 0;

        JButton updateButton = new JButton(IconManager.getIcon(IconManager.IconType.UPDATE));
        updateButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateTree();
            }
        });
        add(updateButton, c);

    }

    private void updateTree() {
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

    class DirSelectionListener
            implements TreeSelectionListener {

        @Override
        public void valueChanged(TreeSelectionEvent event) {
            DefaultMutableTreeNode node = getTreeNode(
                    event.getPath());
            FileNode fnode = getFileNode(node);

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
                    node.add(new DefaultMutableTreeNode(new Boolean(true)));
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

class IconCellRenderer extends JLabel implements TreeCellRenderer {

    protected Color m_textSelectionColor;
    protected Color m_textNonSelectionColor;
    protected Color m_bkSelectionColor;
    protected Color m_bkNonSelectionColor;
    protected Color m_borderSelectionColor;

    protected boolean m_selected;

    public IconCellRenderer() {
        super();
        m_textSelectionColor = UIManager.getColor("Tree.selectionForeground");
        m_textNonSelectionColor = UIManager.getColor("Tree.textForeground");
        m_bkSelectionColor = UIManager.getColor("Tree.selectionBackground");
        m_bkNonSelectionColor = UIManager.getColor("Tree.textBackground");
        m_borderSelectionColor = UIManager.getColor("Tree.selectionBorderColor");
        setOpaque(false);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
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
        setFont(tree.getFont());
        setForeground(sel ? m_textSelectionColor : m_textNonSelectionColor);
        setBackground(sel ? m_bkSelectionColor : m_bkNonSelectionColor);
        m_selected = sel;
        return this;
    }

    @Override
    public void paintComponent(Graphics g) {
        Color bColor = getBackground();
        Icon icon = getIcon();

        g.setColor(bColor);
        int offset = 0;
        if (icon != null && getText() != null) {
            offset = (icon.getIconWidth() + getIconTextGap());
        }
        g.fillRect(offset, 0, getWidth() - 1 - offset,
                getHeight() - 1);

        if (m_selected) {
            g.setColor(m_borderSelectionColor);
            g.drawRect(offset, 0, getWidth() - 1 - offset, getHeight() - 1);
        }
        super.paintComponent(g);
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
