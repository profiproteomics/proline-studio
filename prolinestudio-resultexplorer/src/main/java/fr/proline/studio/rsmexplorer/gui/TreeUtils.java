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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;


/**
 *
 * @author AK249877
 */
public class TreeUtils {

    public enum TreeType {

        SERVER, LOCAL, XIC, WORKING_SET
    }

    public static Preferences m_preferences = NbPreferences.root();

    public static void saveExpansionState(JTree tree, TreeType type, String rootSuffix) {

        StringBuilder builder = new StringBuilder();

        HashSet<String> set = TreeUtils.getExpansionState(tree);

        Iterator iter = set.iterator();
        while (iter.hasNext()) {
            builder.append((String) iter.next());
            builder.append(";");
        }

        if (type == TreeType.SERVER) {
            m_preferences.put("TreeStateUtil.Server_tree", builder.toString());
        } else if (type == TreeType.LOCAL) {
            m_preferences.put("TreeStateUtil.Local_tree" + "." + rootSuffix, builder.toString());
        } else if (type == TreeType.XIC) {
            m_preferences.put("TreeStateUtil.XIC_tree", builder.toString());
        } else if (type == TreeType.WORKING_SET) {
            m_preferences.put("TreeStateUtil.Working_Set_tree", builder.toString());
        }

    }

    public static HashSet<String> loadExpansionState(TreeType type, String rootSuffix) {
        HashSet<String> retrievedSet = new HashSet<String>();

        String s = null;

        if (type == TreeType.SERVER) {
            s = m_preferences.get("TreeStateUtil.Server_tree", null);
        } else if (type == TreeType.LOCAL) {
            s = m_preferences.get("TreeStateUtil.Local_tree" + "." + rootSuffix, null);
        } else if (type == TreeType.XIC) {
            s = m_preferences.get("TreeStateUtil.XIC_tree", null);
        } else if (type == TreeType.WORKING_SET) {
            s = m_preferences.get("TreeStateUtil.Working_Set_tree", null);
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

    public static void setExpansionState(HashSet<String> previouslyExpandedPaths, JTree tree, DefaultMutableTreeNode root, TreeType type, String rootSuffix) {
        if (previouslyExpandedPaths == null || previouslyExpandedPaths.isEmpty()) {
            return;
        }

        if (type == TreeType.LOCAL || type == TreeType.WORKING_SET) {
            tree.addTreeExpansionListener(new TreeExpansionListener() {

                @Override
                public void treeExpanded(TreeExpansionEvent tee) {
                    tree.removeTreeExpansionListener(this);
                    setExpansionState(previouslyExpandedPaths, tree, root, type, rootSuffix);
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
                    tree.getModel().removeTreeModelListener(this);
                    TreePath triggerPath = new TreePath(tme.getPath());
                    if (!tree.isExpanded(triggerPath)) {
                        tree.expandPath(triggerPath);
                    }
                    setExpansionState(previouslyExpandedPaths, tree, root, type, rootSuffix);
                }

            });
        }

        Enumeration totalNodes = root.preorderEnumeration();

        while (totalNodes.hasMoreElements() && !previouslyExpandedPaths.isEmpty()) {

            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) totalNodes.nextElement();
            TreePath tp = new TreePath(currentNode.getPath());

            if (previouslyExpandedPaths.contains(tp.toString())) {
                previouslyExpandedPaths.remove(tp.toString());
                tree.expandPath(tp);
            }
        }

    }

    public static void expandTree(JTree tree, boolean expand) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        expandAll(tree, new TreePath(root), expand);
    }

    public static void expandAll(JTree tree, TreePath path, boolean expand) {
        TreeNode node = (TreeNode) path.getLastPathComponent();

        if (node.getChildCount() >= 0) {
            Enumeration enumeration = node.children();
            while (enumeration.hasMoreElements()) {
                TreeNode n = (TreeNode) enumeration.nextElement();
                TreePath p = path.pathByAddingChild(n);

                expandAll(tree, p, expand);
            }
        }

        if (expand) {
            tree.expandPath(path);
        } else {
            tree.collapsePath(path);
        }
    }
}
