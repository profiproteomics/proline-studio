package fr.proline.studio.gui;

import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.utils.IconManager;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author AK249877
 */
public class AbstractParameterListTree extends JPanel {

    private String rootName;
    private TreeSelectionListener m_selectionListener;
    private TreeWillExpandListener m_expandListener;
    private JTree tree;
    private DefaultMutableTreeNode m_root;
    private Hashtable<String, ParameterList> m_parameters;
    private Hashtable<String, DefaultMutableTreeNode> m_nodes;

    public AbstractParameterListTree(String rootName, TreeSelectionListener listener, TreeWillExpandListener expandListener) {
        this.rootName = rootName;
        this.m_selectionListener = listener;
        this.m_expandListener = expandListener;
        m_parameters = new Hashtable<String, ParameterList>();
        m_nodes = new Hashtable<String, DefaultMutableTreeNode>();
        this.createTree();
    }

    private void createTree() {
        m_root = new DefaultMutableTreeNode(rootName);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(IconManager.getIcon(IconManager.IconType.FILE));
        renderer.setIcon(IconManager.getIcon(IconManager.IconType.FILE));
        renderer.setClosedIcon(IconManager.getIcon(IconManager.IconType.TOOLBOX_PLUS));
        renderer.setOpenIcon(IconManager.getIcon(IconManager.IconType.TOOLBOX_MINUS));

        tree = new JTree(m_root);
        tree.setCellRenderer(renderer);
        tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tree.addTreeSelectionListener(m_selectionListener);
        tree.addTreeWillExpandListener(m_expandListener);
    }

    public void addNodes(ParameterList parameterList) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(parameterList.toString());
        m_parameters.put(parameterList.toString(), parameterList);
        m_nodes.put(parameterList.toString(), node);
        m_root.add(node);
    }

    public JTree getTree() {
        return this.tree;
    }

    public String getRootName() {
        return this.rootName;
    }

    public Hashtable<String, ParameterList> getList() {
        return m_parameters;
    }

    public void expandAllRows() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    public void selectNode(String key) {

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();

        Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            if (node.toString().equalsIgnoreCase(key)) {
                if (node.getParent() != null) {
                    tree.setSelectionRow(node.getParent().getIndex(node) + 1);
                }
                return;
            }
        }
    }

}
