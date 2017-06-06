/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.openide.util.NbPreferences;

/**
 *
 * @author AK249877
 */
public class TreeStateUtil {

    public enum TreeType {

        SERVER, LOCAL, XIC
    }

    public static void saveExpansionState(JTree tree, TreeType type, String rootSuffix) {

        StringBuilder builder = new StringBuilder();

        HashSet<String> set = TreeStateUtil.getExpansionState(tree);

        Iterator iter = set.iterator();
        while (iter.hasNext()) {
            builder.append((String) iter.next());
            builder.append(";");
        }

        if (type == TreeType.SERVER) {
            NbPreferences.root().put("TreeStateUtil.Server_tree", builder.toString());
        } else if (type == TreeType.LOCAL) {
            NbPreferences.root().put("TreeStateUtil.Local_tree" + "." + rootSuffix, builder.toString());
        } else if (type == TreeType.XIC) {
            NbPreferences.root().put("TreeStateUtil.XIC_tree", builder.toString());
        }

    }

    public static HashSet<String> loadExpansionState(TreeType type, String rootSuffix) {
        HashSet<String> retrievedSet = new HashSet<String>();

        String s;

        if (type == TreeType.SERVER) {
            s = NbPreferences.root().get("TreeStateUtil.Server_tree", null);
        } else if (type == TreeType.LOCAL) {
            s = NbPreferences.root().get("TreeStateUtil.Local_tree" + "." + rootSuffix, null);
        } else if (type == TreeType.XIC) {
            s = NbPreferences.root().get("TreeStateUtil.XIC_tree", null);
        } else {
            s = null;
        }

        if (s == null) {
            return retrievedSet;
        }

        StringTokenizer tokenizer = new StringTokenizer(s, ";");
        while (tokenizer.hasMoreTokens()) {
            retrievedSet.add(tokenizer.nextToken());
        }

        return retrievedSet;
    }

    public static HashSet<String> getExpansionState(JTree tree) {
        HashSet<String> expandedPaths = new HashSet<String>();
        for (int i = 0; i < tree.getRowCount(); i++) {
            TreePath tp = tree.getPathForRow(i);
            if (tree.isExpanded(i)) {
                expandedPaths.add(tp.toString());
            }
        }
        return expandedPaths;
    }

    public static void setExpansionState(HashSet<String> previouslyExpanded, JTree tree, DefaultMutableTreeNode root, TreeType type, String rootSuffix) {
        if (previouslyExpanded == null || previouslyExpanded.isEmpty()) {
            return;
        }

        if (type == TreeType.LOCAL) {
            tree.addTreeExpansionListener(new TreeExpansionListener() {

                @Override
                public void treeExpanded(TreeExpansionEvent tee) {
                    setExpansionState(previouslyExpanded, tree, root, type, rootSuffix);
                }

                @Override
                public void treeCollapsed(TreeExpansionEvent tee) {
                }
            }
            );
        } else {

            tree.getModel().addTreeModelListener(new TreeModelListener() {

                @Override
                public void treeNodesChanged(TreeModelEvent tme) {
                }

                @Override
                public void treeNodesInserted(TreeModelEvent tme) {
                }

                @Override
                public void treeNodesRemoved(TreeModelEvent tme) {
                    ;
                }

                @Override
                public void treeStructureChanged(TreeModelEvent tme) {
                    TreePath triggerPath = new TreePath(tme.getPath());
                    if (!tree.isExpanded(triggerPath)) {
                        tree.expandPath(triggerPath);
                    }
                    setExpansionState(previouslyExpanded, tree, root, type, rootSuffix);
                }

            });
        }

        Enumeration totalNodes = root.preorderEnumeration();

        while (totalNodes.hasMoreElements()) {

            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) totalNodes.nextElement();
            TreePath tp = new TreePath(currentNode.getPath());

            if (previouslyExpanded.contains(tp.toString())) {
                previouslyExpanded.remove(tp.toString());
                tree.expandPath(tp);
            }
        }

    }

}
