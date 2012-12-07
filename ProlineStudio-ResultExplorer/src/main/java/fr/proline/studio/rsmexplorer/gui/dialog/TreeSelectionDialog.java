/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.dialog;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.gui.AbstractDialog;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMResultSummaryNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;

/**
 *
 * @author JM235353
 */
public class TreeSelectionDialog extends AbstractDialog {
    
    private RSMTree tree = null;
    private ArrayList<ResultSummary> selectedRsmList = null;
    
    public TreeSelectionDialog(Window parent, RSMTree tree, String title) {
        super(parent, Dialog.ModalityType.APPLICATION_MODAL);
     
        this.tree = tree;
        
        setTitle(title);
        
        internalPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;

        JScrollPane scrollPane = new JScrollPane(tree);
        
        internalPanel.add(scrollPane, c);
 
    }
    
    public void setSelection(ArrayList<ResultSummary> rsmArray) {
        tree.setSelection(rsmArray);
    }
    
    public ArrayList<ResultSummary> getSelectedRsmList() {
        return selectedRsmList;
    }
    
    @Override
    protected boolean okCalled() {
        
        TreePath[] paths = tree.getSelectionPaths();

        if (paths == null || paths.length == 0) {
            showSelectionError();
            return false;
        }

        if (selectedRsmList == null) {
            selectedRsmList = new ArrayList<ResultSummary>();
        } else {
            selectedRsmList.clear();
        }
        
        int size = paths.length;
        for (int i=0;i<size;i++) {
            RSMNode node = (RSMNode) paths[i].getLastPathComponent();
            if (node.getType() == RSMNode.NodeTypes.RESULT_SUMMARY) {
                ResultSummary rsm = ((RSMResultSummaryNode) node).getResultSummary();
                selectedRsmList.add(rsm);
            }
        }
        if (selectedRsmList.isEmpty()) {
            showSelectionError();
            return false;
        }
        
        
        
        return true;
    }
    
    protected boolean cancelCalled() {
        if (selectedRsmList != null) {
            selectedRsmList.clear();
        }
        return true;
    }
    
    private void showSelectionError() {
        JOptionPane.showMessageDialog(tree, "You must at least select one Result Summary.", "Warning", JOptionPane.ERROR_MESSAGE);
    }
    
    
}
