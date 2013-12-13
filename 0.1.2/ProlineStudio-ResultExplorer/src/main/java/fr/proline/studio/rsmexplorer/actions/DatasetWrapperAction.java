package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.data.DataSetData;
import fr.proline.studio.gui.DatasetAction;
import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;

/**
 *
 * @author CB205360
 */
public class DatasetWrapperAction extends AbstractRSMAction {

    private DatasetAction wrappedAction;
    
    public DatasetWrapperAction(DatasetAction action) {
        super((String)action.getValue(NAME));
        this.wrappedAction = action;
    }
    
    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {
        final DDataset[] dataSet = new DDataset[selectedNodes.length];
        int count = 0;
        for (RSMNode rsmNode : selectedNodes) {
               dataSet[count++] = ((DataSetData) ((RSMDataSetNode)rsmNode).getData()).getDataset();
        }
        wrappedAction.actionPerformed(dataSet, x, y);
    }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
        int nbSelectedNodes = selectedNodes.length;
        
        // we disallow to display multiple peptides window
        if (nbSelectedNodes != 1) {
            setEnabled(false);
            return;
        }
        
        RSMNode node = selectedNodes[0];
        if (node.getType() != RSMNode.NodeTypes.DATA_SET) {
            setEnabled(false);
            return;
        }
        
        final DDataset[] dataSet = new DDataset[selectedNodes.length];
        int count = 0;
        for (RSMNode rsmNode : selectedNodes) {
               dataSet[count++] = ((DataSetData) ((RSMDataSetNode)rsmNode).getData()).getDataset();
        }
        wrappedAction.updateEnabled(dataSet);
        setEnabled(wrappedAction.isEnabled());
    }
    
    
}
