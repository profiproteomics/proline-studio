package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
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
    
    private RSMTree m_tree = null;
    private ArrayList<Dataset> m_selectedDatasetList = null;
    private ArrayList<RSMDataSetNode> m_selectedRSMDSNodeList = null;
    
    private boolean m_userSetSize = false;
    
    public TreeSelectionDialog(Window parent, RSMTree tree, String title) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
     
        // hide default button
        setButtonVisible(BUTTON_DEFAULT, false);
        
        setStatusVisible(false);
        
        setResizable(true);
        
        m_tree = tree;
        
        setTitle(title);
        

        JScrollPane scrollPane = new JScrollPane(tree);
        
        setInternalComponent(scrollPane);
 
    }
    
    public TreeSelectionDialog(Window parent, RSMTree tree, String title, int width, int height) {
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
    
    public ArrayList<Dataset> getSelectedDatasetList() {
        ArrayList<Dataset> returnedList = m_selectedDatasetList;
        m_selectedDatasetList = null; // avoid a potential memory leak
        m_selectedRSMDSNodeList = null;
        return returnedList;
    }
    
    public ArrayList<RSMDataSetNode> getSelectedRSMDSNodeList() {
        ArrayList<RSMDataSetNode> returnedList = m_selectedRSMDSNodeList;
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
            RSMNode node = (RSMNode) paths[i].getLastPathComponent();
            if (node.getType() == RSMNode.NodeTypes.DATA_SET) {
                RSMDataSetNode dataSetNode = (RSMDataSetNode) node;

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
