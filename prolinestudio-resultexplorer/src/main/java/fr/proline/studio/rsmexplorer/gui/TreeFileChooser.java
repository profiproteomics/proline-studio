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
