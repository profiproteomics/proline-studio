/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import fr.proline.studio.msfiles.WorkingSetEntry.Location;
import fr.proline.studio.rsmexplorer.gui.dialog.ApplicationSettingsDialog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openide.util.NbPreferences;

/**
 *
 * @author AK249877
 */
public class WorkingSetModel implements TreeModel {
    
    public enum JSONObjectType {
        WORKING_SET, ENTRY
    }

    private final DefaultMutableTreeNode m_root;

    private final HashSet<TreeModelListener> m_listeners; // Declare the listeners vector
    
    private final HashMap<String, ArrayList<WorkingSet>> m_index;
    
    private final HashMap<String, JSONObject> m_workingSets, m_workingSetEntries;
    
    private boolean m_displayFilename;

    public WorkingSetModel(WorkingSetRoot root) {
        m_root = new DefaultMutableTreeNode(root);
        m_listeners = new HashSet<TreeModelListener>();
        
        m_displayFilename = NbPreferences.root().get(ApplicationSettingsDialog.MS_FILES_SETTINGS+"."+ApplicationSettingsDialog.WORKING_SET_ENTRY_NAMING_KEY, ApplicationSettingsDialog.FILENAME).equalsIgnoreCase(ApplicationSettingsDialog.FILENAME);
        
        m_index = new HashMap<String, ArrayList<WorkingSet>>();
        
        m_workingSets = new HashMap<String, JSONObject>();
        m_workingSetEntries = new HashMap<String, JSONObject>();
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
            
            m_workingSets.put(name, workingSetEntry);

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

            WorkingSetEntry newEntry = new WorkingSetEntry(filename, path, location, workingSet, (m_displayFilename) ? WorkingSetEntry.Labeling.DISPLAY_FILENAME : WorkingSetEntry.Labeling.DISPLAY_PATH);
            
            m_workingSetEntries.put(path, entry);
            
            if(m_index.containsKey(path)){
                m_index.get(path).add(workingSet);       
            }else{
                ArrayList<WorkingSet> list = new ArrayList<WorkingSet>();
                list.add(workingSet);
                m_index.put(path, list);
            }

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
    
    
    public HashMap<String, ArrayList<WorkingSet>> getEntriesIndex(){
        return m_index;
    }
    
    public void updateJSONObject(JSONObjectType type, String key, String objectKey, String objectNewValue){
        if(type == JSONObjectType.WORKING_SET){
            JSONObject workingSet = m_workingSets.get(key);
            if(workingSet!=null){
                workingSet.put(objectKey, objectNewValue);
            }
        }else if(type == JSONObjectType.ENTRY){
            JSONObject entry = m_workingSetEntries.get(key);
            if(entry!=null){
                entry.put(objectKey, objectNewValue);
            }
        }
    }

}
