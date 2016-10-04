/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.gui;

import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.utils.IconManager;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import static org.openide.util.NbPreferences.root;

/**
 *
 * @author AK249877
 */
public class AbstractParameterListTree extends JPanel {

    private String rootName;
    private TreeSelectionListener listener;
    private JTree tree;
    private DefaultMutableTreeNode rootNode;
    private Hashtable<String, ParameterList> m_parameters;
    private Hashtable<String, DefaultMutableTreeNode> m_nodes;

    public AbstractParameterListTree(String rootName, TreeSelectionListener listener) {
        this.rootName = rootName;
        this.listener = listener;
        m_parameters = new Hashtable<String, ParameterList>();
        m_nodes = new Hashtable<String, DefaultMutableTreeNode>();
        this.createTree();
    }

    private void createTree() {
        rootNode = new DefaultMutableTreeNode(rootName);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(IconManager.getIcon(IconManager.IconType.SETTINGS));
        renderer.setIcon(IconManager.getIcon(IconManager.IconType.SETTINGS));
        renderer.setClosedIcon(IconManager.getIcon(IconManager.IconType.TOOLBOX_PLUS));
        renderer.setOpenIcon(IconManager.getIcon(IconManager.IconType.TOOLBOX_MINUS));

        tree = new JTree(rootNode);
        tree.setCellRenderer(renderer);
        tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tree.addTreeSelectionListener(listener);
    }

    public void addNodes(ParameterList parameterList) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(parameterList.toString());
        m_parameters.put(parameterList.toString(), parameterList);
        m_nodes.put(parameterList.toString(), node);
        rootNode.add(node);
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
                TreePath path = new TreePath(node);
                try {
                    tree.setSelectionPath(path);
                    tree.setSelectionRow(node.getParent().getIndex(node) + 1);
                } catch (ArrayIndexOutOfBoundsException ex) {
                    return;
                } catch (NullPointerException ex) {
                    return;
                }
                return;
            }
        }
    }

}
