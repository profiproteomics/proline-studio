/* 
 * Copyright (C) 2019 VD225637
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

import fr.proline.studio.msfiles.LocalFileSystemFile;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * Model containing the directories and files of the local file system
 * 
 * @author AK249877
 */
public class LocalFileSystemModel implements TreeModel {

    private DefaultMutableTreeNode m_root;

    private final HashSet<TreeModelListener> listeners; // Declare the listeners vector

    public LocalFileSystemModel(String rootURL) {
        m_root = new DefaultMutableTreeNode(new LocalFileSystemFile(rootURL));
        listeners = new HashSet<>();
    }

    public void setRoot(String rootURL) {
        m_root = new DefaultMutableTreeNode(new LocalFileSystemFile(rootURL));
    }

    @Override
    public Object getRoot() {
        return m_root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parent;

        File directory = (File) parentNode.getUserObject();

        String[] directoryMembers = getFilesName(directory);

        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new LocalFileSystemFile(directory, directoryMembers[index]));

        parentNode.add(childNode);

        return childNode;
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent == null) {
            return 0;
        }

        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parent;

        File fileSystemMember = (File) parentNode.getUserObject();
        if (fileSystemMember.isDirectory()) {
            return getFilesCount(fileSystemMember);
        } else {
            return 0;
        }
    }

    @Override
    public boolean isLeaf(Object node) {
        DefaultMutableTreeNode mutableNode = (DefaultMutableTreeNode) node;
        File fileNode = (File) mutableNode.getUserObject();
        return fileNode.isFile();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        ;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {

        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parent;
        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) child;

        File directory = (File) parentNode.getUserObject();
        File directoryMember = (File) childNode.getUserObject();

        String[] directoryMemberNames = getFilesName(directory);
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
            listeners.add(listener);
        }
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        if (l != null) {
            listeners.remove(l);
        }
    }

    public void fireTreeNodesInserted(TreeModelEvent e) {
        for (TreeModelListener listener : listeners) {
            listener.treeNodesInserted(e);
        }
    }

    public void fireTreeNodesRemoved(TreeModelEvent e) {
        for (TreeModelListener listener : listeners) {
            listener.treeNodesRemoved(e);
        }
    }

    public void fireTreeNodesChanged(TreeModelEvent e) {
        for (TreeModelListener listener : listeners) {
            listener.treeNodesChanged(e);
        }
    }

    public void fireTreeStructureChanged(TreeModelEvent e) {
        for (TreeModelListener listener : listeners) {
            listener.treeStructureChanged(e);
        }
    }

    /*public class Filter implements FilenameFilter {

        @Override

        public boolean accept(File directory, String fileName) {

            File f = new File(directory, fileName);

            return f.isDirectory() || fileName.toLowerCase().endsWith(".dat") || fileName.toLowerCase().endsWith(".raw") || fileName.toLowerCase().endsWith(".mzdb") || fileName.toLowerCase().endsWith(".wiff") || fileName.toLowerCase().endsWith(".mgf");

        }

    }*/

    
    private int getFilesCount(File directory) {
        String[] list = directory.list();
        return (list != null) ? list.length : 0;
    }
    private File[] getFiles(File directory) {
        File[] files = directory.listFiles();
        Arrays.sort(files, FILE_COMPARATOR);
        return files;
    }
    private String[] getFilesName(File directory) {
        File[] files = getFiles(directory);
        int nbFiles = files.length;
        String[] fileNames = new String[nbFiles];
        for (int i=0;i<nbFiles;i++) {
            fileNames[i] = files[i].getName();
        }
        return fileNames;
    }
    
    
    private static Comparator<File> FILE_COMPARATOR = new Comparator<File>() {         
    @Override         
    public int compare(File f1, File f2) {   
        if (f1.isDirectory()) {
            if (!f2.isDirectory()) {
                return -1;
            }
        } else if (f2.isDirectory()) {
            return 1;
        }
      return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
    }     
  };     

}
