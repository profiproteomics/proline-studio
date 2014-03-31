package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.IdentificationTree;
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
        super(NbBundle.getMessage(DeleteAction.class, "CTL_DeleteAction"));
    }

    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) { 
        IdentificationTree.getCurrentTree().moveToTrash(selectedNodes);
    }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {

        
        // look if all the nodes can be separately deleted and 
        // are in the same project
        
        long commonProjectId = -1;
        int nbSelectedNodes = selectedNodes.length;
        HashSet<RSMDataSetNode> selectedNodesHashSet = new HashSet<>();
        for (int i=0;i<nbSelectedNodes;i++) {
            RSMNode node = selectedNodes[i];
            
            if (! node.canBeDeleted()) {
                setEnabled(false);
                return;
            }
            
            RSMDataSetNode datasetNode = (RSMDataSetNode) selectedNodes[i];
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
        Iterator<RSMDataSetNode> it = selectedNodesHashSet.iterator();
        while (it.hasNext()) {
            RSMDataSetNode node = it.next();
            RSMNode parent = (RSMNode) node.getParent();
            if (parent instanceof RSMDataSetNode) {
                
                RSMDataSetNode parentDataset = (RSMDataSetNode) parent;
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
    
    private void addChildren(RSMDataSetNode parentNode, HashSet<RSMDataSetNode> nodesHashSet) {
        Enumeration e = parentNode.children();
        while (e.hasMoreElements()) {
            RSMDataSetNode child = (RSMDataSetNode) e.nextElement();
            nodesHashSet.add(child);
            addChildren(child, nodesHashSet);
        }
    }
}