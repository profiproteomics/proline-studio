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
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import org.openide.util.NbPreferences;

/**
 *
 * @author AK249877
 */
public class TreeStateUtil {

    public enum TreeType {

        SERVER, LOCAL
    }

    public static void saveExpansionState(JTree tree, TreeType type) {

        StringBuilder builder = new StringBuilder();

        HashSet<String> set = TreeStateUtil.getExpansionState(tree);

        Iterator iter = set.iterator();
        while (iter.hasNext()) {
            builder.append((String) iter.next());
            builder.append(";");
        }

        if (type == TreeType.SERVER) {
            NbPreferences.root().put("TreeStateUtil.Server_tree", builder.toString());
        } else {
            NbPreferences.root().put("TreeStateUtil.Local_tree", builder.toString());
        }

    }

    public static HashSet<String> retrieveExpansionState(TreeType type) {
        HashSet<String> retrievedSet = new HashSet<String>();

        String s;

        if (type == TreeType.SERVER) {
            s = NbPreferences.root().get("TreeStateUtil.Server_tree", null);
        } else {
            s = NbPreferences.root().get("TreeStateUtil.Local_tree", null);
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

    public static void resetExpansionState(HashSet<String> previouslyExpanded, JTree tree, DefaultMutableTreeNode root, TreeType type) {
        if (previouslyExpanded == null || previouslyExpanded.isEmpty()) {
            return;
        }

        if (type == TreeType.LOCAL) {

            tree.addTreeWillExpandListener(new TreeWillExpandListener() {

                @Override
                public void treeWillExpand(TreeExpansionEvent tee) throws ExpandVetoException {
                    resetExpansionState(previouslyExpanded, tree, root, type);
                }

                @Override
                public void treeWillCollapse(TreeExpansionEvent tee) throws ExpandVetoException {
                    ;
                }

            });

            tree.addTreeExpansionListener(new TreeExpansionListener() {

                @Override
                public void treeExpanded(TreeExpansionEvent tee) {
                    resetExpansionState(previouslyExpanded, tree, root, type);
                }

                @Override
                public void treeCollapsed(TreeExpansionEvent tee) {
                    ;
                }

            });

        } else {

            tree.getModel().addTreeModelListener(new TreeModelListener() {

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
                    TreePath triggerPath = new TreePath(tme.getPath());
                    if(!tree.isExpanded(triggerPath)){
                        tree.expandPath(triggerPath);
                    }
                    resetExpansionState(previouslyExpanded, tree, root, type);
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

    public static void setExpansionState(HashSet<String> previouslyExpanded, JTree tree) {

        if (previouslyExpanded == null) {
            return;
        }

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();

        while (!previouslyExpanded.isEmpty()) {
            Enumeration totalNodes = root.preorderEnumeration();
            while (totalNodes.hasMoreElements()) {
                DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) totalNodes.nextElement();
                TreePath tp = new TreePath(currentNode.getPath());
                /*
                 if (tree.isExpanded(tp)) {
                 previouslyExpanded.remove(tp.toString());
                 continue;
                 }
                 */
                if (previouslyExpanded.contains(tp.toString())) {
                    tree.expandPath(tp);
                    previouslyExpanded.remove(tp.toString());
                }
            }
        }
    }

}
