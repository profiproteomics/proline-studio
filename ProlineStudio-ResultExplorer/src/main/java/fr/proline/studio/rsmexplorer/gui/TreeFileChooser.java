/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.rsmexplorer.gui.TreeFileChooserPanel.FileNode;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author JM235353
 */
public class TreeFileChooser extends JTree {
    
    public TreeFileChooser(TreeModel newModel) {
        super(newModel);
        getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    }
    
    public ArrayList<File> getSelectedFiles() {
        TreePath[] paths = getSelectionModel().getSelectionPaths();

        int nbPath = paths.length;

        ArrayList<File> m_selectedFiles = new ArrayList<>();
        
        for (int i = 0; i < nbPath; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i].getLastPathComponent();
            FileNode f = TreeFileChooserPanel.getFileNode(node);
            if (f != null) {
                m_selectedFiles.add(f.getFile());
            }
            
        }

        return m_selectedFiles;
    }
    
}
