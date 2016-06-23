/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.gui;

import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.utils.IconManager;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author AK249877
 */
public class AbstractParameterListTree extends JPanel {

    private String rootName;
    private TreeSelectionListener listener;
    private JTree tree;
    private DefaultMutableTreeNode rootNode;
    private Hashtable<String, ParameterList> list;

    public AbstractParameterListTree(String rootName, TreeSelectionListener listener) {
        this.rootName = rootName;
        this.listener = listener;
        list = new Hashtable<String, ParameterList>();
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
        list.put(parameterList.toString(), parameterList);
        rootNode.add(node);
    }

    public JTree getTree() {
        return this.tree;
    }

    public String getRootName() {
        return this.rootName;
    }

    public Hashtable<String, ParameterList> getList() {
        return list;
    }

    public void expandAllRows() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

}
