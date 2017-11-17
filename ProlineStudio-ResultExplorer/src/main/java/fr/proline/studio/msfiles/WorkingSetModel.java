/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import fr.proline.studio.msfiles.WorkingSetEntry.Location;
import java.util.HashSet;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author AK249877
 */
public class WorkingSetModel implements TreeModel {

    private DefaultMutableTreeNode m_root;

    private final HashSet<TreeModelListener> m_listeners; // Declare the listeners vector
    
    private HashSet<WorkingSet> m_workingSetIndex;

    public WorkingSetModel(WorkingSetRoot root) {
        m_root = new DefaultMutableTreeNode(root);
        m_listeners = new HashSet<TreeModelListener>();
        m_workingSetIndex = new HashSet<WorkingSet>();
    }

    @Override
    public Object getRoot() {
        return m_root;
    }

    @Override
    public Object getChild(Object parent, int index) {

        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parent;

        Object userObject = parentNode.getUserObject();

        if (userObject instanceof WorkingSetRoot) {
            WorkingSetRoot workingSetRoot = (WorkingSetRoot) userObject;

            JSONObject workingSetEntry = (JSONObject) workingSetRoot.getWorkingSets().get(index);

            String name = (String) workingSetEntry.get("name");
            String description = (String) workingSetEntry.get("description");
            JSONArray entries = (JSONArray) workingSetEntry.get("entries");

            WorkingSet newWorkingSet = new WorkingSet(name, description, entries);
            
            m_workingSetIndex.add(newWorkingSet);

            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(newWorkingSet);

            parentNode.add(childNode);

            return childNode;

        } else if (userObject instanceof WorkingSet) {

            WorkingSet workingSet = (WorkingSet) userObject;

            JSONObject entry = (JSONObject) workingSet.getEntries().get(index);

            String filename = (String) entry.get("filename");

            String stringLocation = (String) entry.get("location");
            Location location = null;
            if (stringLocation.equalsIgnoreCase("LOCAL")) {
                location = Location.LOCAL;
            } else if (stringLocation.equalsIgnoreCase("REMOTE")) {
                location = Location.REMOTE;
            }

            String path = (String) entry.get("path");

            WorkingSetEntry newEntry = new WorkingSetEntry(filename, path, location, workingSet);

            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(newEntry);

            parentNode.add(childNode);

            return childNode;
        } else {
            return null;
        }
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent == null) {
            return 0;
        } else {

            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parent;

            Object userObject = parentNode.getUserObject();

            if (userObject instanceof WorkingSet) {
                return ((WorkingSet) userObject).getEntries().size();
            } else if (userObject instanceof WorkingSetRoot) {
                return ((WorkingSetRoot) userObject).getWorkingSets().size();
            } else {
                return 0;
            }
        }
    }

    @Override
    public boolean isLeaf(Object node) {
        DefaultMutableTreeNode mutableNode = (DefaultMutableTreeNode) node;
        Object userObject = mutableNode.getUserObject();
        return (userObject instanceof WorkingSetEntry);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        ;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {

        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parent;
        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) child;

        Object parentObject = parentNode.getUserObject();
        Object childObject = childNode.getUserObject();

        if (parentObject instanceof WorkingSetRoot) {
            WorkingSetRoot root = (WorkingSetRoot) parentObject;
            WorkingSet childWorkingSet = (WorkingSet) childObject;

            JSONArray workingSets = root.getWorkingSets();
            for (int i = 0; i < workingSets.size(); i++) {
                JSONObject innerObj = (JSONObject) workingSets.get(i);
                String innerObjName = (String) innerObj.get("name");
                if (innerObjName.equalsIgnoreCase(childWorkingSet.getName())) {
                    return i;
                }
            }

        } else if (parentObject instanceof WorkingSet) {
            WorkingSet workingSet = (WorkingSet) parentObject;
            WorkingSetEntry workingSetEntry = (WorkingSetEntry) childObject;

            JSONArray entries = workingSet.getEntries();
            for (int i = 0; i < entries.size(); i++) {
                JSONObject innerObj = (JSONObject) entries.get(i);
                String innerObjPath = (String) innerObj.get("path");
                if (innerObjPath.equalsIgnoreCase(workingSetEntry.getPath())) {
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    public void addTreeModelListener(TreeModelListener listener) {
        if (listener != null && !m_listeners.contains(listener)) {
            m_listeners.add(listener);
        }
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        if (l != null) {
            m_listeners.remove(l);
        }
    }

    public void fireTreeNodesInserted(TreeModelEvent e) {
        for (TreeModelListener listener : m_listeners) {
            listener.treeNodesInserted(e);
        }
    }

    public void fireTreeNodesRemoved(TreeModelEvent e) {
        for (TreeModelListener listener : m_listeners) {
            listener.treeNodesRemoved(e);
        }
    }

    public void fireTreeNodesChanged(TreeModelEvent e) {
        for (TreeModelListener listener : m_listeners) {
            listener.treeNodesChanged(e);
        }
    }

    public void fireTreeStructureChanged(TreeModelEvent e) {
        for (TreeModelListener listener : m_listeners) {
            listener.treeStructureChanged(e);
        }
    }
    
    public HashSet<WorkingSet> getAllWorkingSets(){
        return m_workingSetIndex;
    }

}
