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
    
    private RSMTree tree = null;
    private ArrayList<Dataset> selectedDatasetList = null;
    
    public TreeSelectionDialog(Window parent, RSMTree tree, String title) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
     
        // hide default button
        setButtonVisible(BUTTON_DEFAULT, false);
        
        setStatusVisible(false);
        
        this.tree = tree;
        
        setTitle(title);
        

        JScrollPane scrollPane = new JScrollPane(tree);
        
        setInternalComponent(scrollPane);
 
    }
    
    public void setSelection(ArrayList<ResultSummary> rsmArray) {
        tree.setSelection(rsmArray);
    }
    
    public ArrayList<Dataset> getSelectedDatasetList() {
        ArrayList<Dataset> returnedList = selectedDatasetList;
        selectedDatasetList = null; // avoid a potential memory leak
        
        return returnedList;
    }
    
    @Override
    protected boolean okCalled() {
        
        TreePath[] paths = tree.getSelectionPaths();

        if (paths == null || paths.length == 0) {
            showSelectionError();
            return false;
        }

        selectedDatasetList = new ArrayList<Dataset>();

        
        int size = paths.length;
        for (int i=0;i<size;i++) {
            RSMNode node = (RSMNode) paths[i].getLastPathComponent();
            if (node.getType() == RSMNode.NodeTypes.DATA_SET) {
                RSMDataSetNode dataSetNode = (RSMDataSetNode) node;

                selectedDatasetList.add(dataSetNode.getDataset());
            }
        }
        if (selectedDatasetList.isEmpty()) {
            showSelectionError();
            return false;
        }
        
        
        
        return true;
    }
    
    @Override
    protected boolean cancelCalled() {
        selectedDatasetList = null;

        return true;
    }
    
    private void showSelectionError() {
        JOptionPane.showMessageDialog(tree, "You must at least select one Result Summary.", "Warning", JOptionPane.ERROR_MESSAGE);
    }
    
    
}
