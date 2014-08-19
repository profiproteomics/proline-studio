package fr.proline.studio.rsmexplorer.actions.identification;

import fr.proline.studio.rsmexplorer.tree.DataSetNode;
import fr.proline.studio.rsmexplorer.tree.AbstractNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdentificationTree;
import fr.proline.studio.rsmexplorer.tree.AbstractTree;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.openide.util.NbBundle;

/**
 * Delete a dataset Action
 * @author JM235353
 */
public class DeleteAction extends AbstractRSMAction {

    public DeleteAction() {
        super(NbBundle.getMessage(DeleteAction.class, "CTL_DeleteAction"), AbstractTree.TreeType.TREE_IDENTIFICATION);
    }

    @Override
    public void actionPerformed(AbstractNode[] selectedNodes, int x, int y) { 
        IdentificationTree.getCurrentTree().moveToTrash(selectedNodes);
    }
    
    @Override
    public void updateEnabled(AbstractNode[] selectedNodes) {

        
        // look if all the nodes can be separately deleted and 
        // are in the same project
        
        long commonProjectId = -1;
        int nbSelectedNodes = selectedNodes.length;
        HashSet<DataSetNode> selectedNodesHashSet = new HashSet<>();
        for (int i=0;i<nbSelectedNodes;i++) {
            AbstractNode node = selectedNodes[i];
            
            if (! node.canBeDeleted()) {
                setEnabled(false);
                return;
            }
            
            DataSetNode datasetNode = (DataSetNode) selectedNodes[i];
            long projectId = datasetNode.getDataset().getProject().getId();
            if (commonProjectId == -1) {
                commonProjectId = projectId;
            } else if (commonProjectId != projectId) {
                setEnabled(false);
                return;
            }
            
            selectedNodesHashSet.add(datasetNode);
            addChildren(datasetNode, selectedNodesHashSet);
        }
        
        // Check if one selected node is a child of a non selected node with merged results
        // in this case, it can not be suppressed alone
        Iterator<DataSetNode> it = selectedNodesHashSet.iterator();
        while (it.hasNext()) {
            DataSetNode node = it.next();
            AbstractNode parent = (AbstractNode) node.getParent();
            if (parent instanceof DataSetNode) {
                
                DataSetNode parentDataset = (DataSetNode) parent;
                if (!selectedNodesHashSet.contains(parentDataset)) {
                    if (parentDataset.hasResultSet() || parentDataset.hasResultSummary()) {
                        // we have a merged parent dataset which is not selected to be deleted, so its son can not be deleted
                        setEnabled(false);
                        return;
                    }
                }
            }
        }
        
        
        setEnabled(true);

    }
    
    private void addChildren(DataSetNode parentNode, HashSet<DataSetNode> nodesHashSet) {
        Enumeration e = parentNode.children();
        while (e.hasMoreElements()) {
            DataSetNode child = (DataSetNode) e.nextElement();
            nodesHashSet.add(child);
            addChildren(child, nodesHashSet);
        }
    }
}