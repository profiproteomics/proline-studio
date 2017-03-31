/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author AK249877
 */
public class LocalFileSystemModel implements TreeModel {

    private String m_rootURL;

    private Vector listeners; // Declare the listeners vector

    public LocalFileSystemModel(String rootURL) {
        m_rootURL = rootURL;
        listeners = new Vector();
    }

    public void setRoot(String rootURL) {
        m_rootURL = rootURL;
    }

    @Override
    public Object getRoot() {
        return new File(m_rootURL);
    }

    @Override
    public Object getChild(Object parent, int index) {
        File directory = (File) parent;
        String[] directoryMembers = directory.list();
        return (new File(directory, directoryMembers[index]));
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent == null) {
            return 0;
        }
        File fileSystemMember = (File) parent;
        if (fileSystemMember.isDirectory()) {
            File[] directoryMembers = fileSystemMember.listFiles();
            if (directoryMembers == null) {
                return 0;
            }
            return directoryMembers.length;
        } else {
            return 0;
        }
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((File) node).isFile();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        ;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        File directory = (File) parent;
        File directoryMember = (File) child;
        String[] directoryMemberNames = directory.list();
        int result = -1;

        for (int i = 0; i < directoryMemberNames.length; ++i) {
            if (directoryMember.getName().equals(directoryMemberNames[i])) {
                result = i;
                break;
            }
        }

        return result;
    }

    @Override
    public void addTreeModelListener(TreeModelListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.addElement(listener);
        }
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        if (l != null) {
            listeners.removeElement(l);
        }
    }

    public void fireTreeNodesInserted(TreeModelEvent e) {
        Enumeration listenerCount = listeners.elements();
        while (listenerCount.hasMoreElements()) {
            TreeModelListener listener = (TreeModelListener) listenerCount.nextElement();
            listener.treeNodesInserted(e);
        }
    }

    //optional part
    public void fireTreeNodesRemoved(TreeModelEvent e) {
        Enumeration listenerCount = listeners.elements();
        while (listenerCount.hasMoreElements()) {
            TreeModelListener listener = (TreeModelListener) listenerCount.nextElement();
            listener.treeNodesRemoved(e);
        }

    }

    public void fireTreeNodesChanged(TreeModelEvent e) {
        Enumeration listenerCount = listeners.elements();
        while (listenerCount.hasMoreElements()) {
            TreeModelListener listener = (TreeModelListener) listenerCount.nextElement();
            listener.treeNodesChanged(e);
        }

    }

    
    public void fireTreeStructureChanged(TreeModelEvent e) {
        Enumeration listenerCount = listeners.elements();
        while (listenerCount.hasMoreElements()) {
            TreeModelListener listener = (TreeModelListener) listenerCount.nextElement();
            listener.treeStructureChanged(e);
        }

    }

}
