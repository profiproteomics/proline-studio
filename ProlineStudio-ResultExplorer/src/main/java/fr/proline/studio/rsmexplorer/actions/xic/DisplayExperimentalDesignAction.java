/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.actions.identification.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import org.openide.util.NbBundle;

/**
 * Action to display the experimental design
 * @author MB243701
 */
public class DisplayExperimentalDesignAction extends AbstractRSMAction {
    
    
   public DisplayExperimentalDesignAction() {
       super(NbBundle.getMessage(DisplayExperimentalDesignAction.class,"CTL_DisplayExperimentalActionAction"), AbstractTree.TreeType.TREE_QUANTITATION);
   }
   
   
   @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {

        int nbNodes = selectedNodes.length;
        for (int i = 0; i < nbNodes; i++) {
            DataSetNode dataSetNode = (DataSetNode) selectedNodes[i];

            actionImpl(dataSetNode);
        }

    }
    
    private void actionImpl(DataSetNode dataSetNode) {

        final DDataset dataset = ((DataSetData) dataSetNode.getData()).getDataset();

        WindowBox wbox = WindowBoxFactory.getExperimentalDesignWindowBox(dataset.getName(), dataset.getName() + " Exp. Design");
        wbox.setEntryData(dataset.getProject().getId(), dataset);

        // open a window to display the window box
        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
        win.open();
        win.requestActive();
    }

   
   @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        // only one node selected
        if (selectedNodes.length != 1) {
            setEnabled(false);
            return;
        }

        AbstractNode node = (AbstractNode) selectedNodes[0];

        // the node must not be in changing state
        if (node.isChanging()) {
            setEnabled(false);
            return;
        }

        // must be a dataset 
        if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
            setEnabled(false);
            return;
        }

        DataSetNode datasetNode = (DataSetNode) node;

        // must be a quantitation XIC
        if (! datasetNode.isQuantXIC()) {
            setEnabled(false);
            return;
        }

        setEnabled(true);
    }
}
