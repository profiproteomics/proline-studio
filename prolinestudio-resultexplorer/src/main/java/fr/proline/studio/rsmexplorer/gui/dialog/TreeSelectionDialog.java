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
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import java.awt.Dialog;
import java.awt.Window;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;

/**
 * Dialog used to display a subtree and select some of the nodes
 * @author JM235353
 */
public class TreeSelectionDialog extends DefaultDialog {
    
    private IdentificationTree m_tree = null;
    private ArrayList<DDataset> m_selectedDatasetList = null;
    private ArrayList<DataSetNode> m_selectedRSMDSNodeList = null;
    
    private boolean m_userSetSize = false;
    
    public TreeSelectionDialog(Window parent, IdentificationTree tree, String title) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
     
        // hide default and help buttons
        setButtonVisible(BUTTON_HELP, false);
        
        setStatusVisible(false);
        
        setResizable(true);
        
        m_tree = tree;
        
        setTitle(title);
        

        JScrollPane scrollPane = new JScrollPane(tree);
        
        setInternalComponent(scrollPane);
 
    }
    
    public TreeSelectionDialog(Window parent, IdentificationTree tree, String title, int width, int height) {
        this(parent, tree, title);
        
        setSize(width, height);
        m_userSetSize = true;
    }
    
    @Override
    public void pack() {
        if (m_userSetSize) {
            // forbid pack by overloading the method
            return;
        }
        super.pack();
    }
    
    
    public void setSelection(ArrayList<ResultSummary> rsmArray) {
        m_tree.setSelection(rsmArray);
    }
    
    public ArrayList<DDataset> getSelectedDatasetList() {
        ArrayList<DDataset> returnedList = m_selectedDatasetList;
        m_selectedDatasetList = null; // avoid a potential memory leak
        m_selectedRSMDSNodeList = null;
        return returnedList;
    }
    
    public ArrayList<DataSetNode> getSelectedRSMDSNodeList() {
        ArrayList<DataSetNode> returnedList = m_selectedRSMDSNodeList;
        m_selectedDatasetList = null; // avoid a potential memory leak
        m_selectedRSMDSNodeList = null;
        return returnedList;
    }
    
    @Override
    protected boolean okCalled() {
        
        TreePath[] paths = m_tree.getSelectionPaths();

        if (paths == null || paths.length == 0) {
            showSelectionError();
            return false;
        }

        m_selectedDatasetList = new ArrayList<>();
        m_selectedRSMDSNodeList = new ArrayList<>();
        
        int size = paths.length;
        for (int i=0;i<size;i++) {
            AbstractNode node = (AbstractNode) paths[i].getLastPathComponent();
            if (node.getType() == AbstractNode.NodeTypes.DATA_SET) {
                DataSetNode dataSetNode = (DataSetNode) node;

                m_selectedDatasetList.add(dataSetNode.getDataset());
                m_selectedRSMDSNodeList.add(dataSetNode);
            }
        }
        if (m_selectedDatasetList.isEmpty()) {
            showSelectionError();
            return false;
        }
        
        
        
        return true;
    }
    
    @Override
    protected boolean cancelCalled() {
        m_selectedDatasetList = null;
        m_selectedRSMDSNodeList = null;
        return true;
    }
    
    private void showSelectionError() {
        JOptionPane.showMessageDialog(m_tree, "You must at least select one Identification Summary.", "Warning", JOptionPane.ERROR_MESSAGE);
    }
    
    
}
