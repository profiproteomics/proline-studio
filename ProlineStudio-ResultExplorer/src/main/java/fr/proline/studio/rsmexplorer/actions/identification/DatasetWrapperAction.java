package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.gui.DatasetAction;
import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;

/**
 *
 * @author CB205360
 */
public class DatasetWrapperAction extends AbstractRSMAction {

    private DatasetAction wrappedAction;
    
    public DatasetWrapperAction(DatasetAction action) {
        super((String)action.getValue(NAME), AbstractTree.TreeType.TREE_IDENTIFICATION);
        this.wrappedAction = action;
    }
    
    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) {
        final DDataset[] dataSet = new DDataset[selectedNodes.length];
        int count = 0;
        for (AbstractNode rsmNode : selectedNodes) {
               dataSet[count++] = ((DataSetData) ((DataSetNode)rsmNode).getData()).getDataset();
        }
        wrappedAction.actionPerformed(dataSet, x, y);
    }
    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {
        int nbSelectedNodes = selectedNodes.length;
        
        // we disallow to display multiple peptides window
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }
        
        AbstractNode node = selectedNodes[0];
        if (node.getType() != AbstractNode.NodeTypes.DATA_SET) {
            setEnabled(false);
            return;
        }
        
        final DDataset[] dataSet = new DDataset[selectedNodes.length];
        int count = 0;
        for (AbstractNode rsmNode : selectedNodes) {
               dataSet[count++] = ((DataSetData) ((DataSetNode)rsmNode).getData()).getDataset();
        }
        wrappedAction.updateEnabled(dataSet);
        setEnabled(wrappedAction.isEnabled());
    }
    
    
}
