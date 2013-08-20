package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
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
        RSMTree.getTree().moveToTrash(selectedNodes);
    }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {

        long commonProjectId = -1;
        int nbSelectedNodes = selectedNodes.length;
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
        }
        
        setEnabled(true);

    }
}