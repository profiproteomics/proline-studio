package fr.proline.studio.rsmexplorer.actions.xic;

import fr.proline.studio.rsmexplorer.actions.AbstractRSMAction;
import fr.proline.studio.rsmexplorer.gui.dialog.xic.DesignTree;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import java.util.ArrayList;
import org.openide.util.NbBundle;

/**
 * Delete a Node in a Xic DesignTree
 * @author JM235353
 */
public class DeleteAction  extends AbstractRSMAction {
    
    public DeleteAction() {
        super(NbBundle.getMessage(DeleteAction.class, "CTL_DeleteAction"), RSMTree.TreeType.TREE_XIC_DESIGN);
    }

    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {
        
        int nbSelectedNode = selectedNodes.length;

        DesignTree tree = DesignTree.getDesignTree();
        RSMTree.RSMTreeModel model = (RSMTree.RSMTreeModel) tree.getModel();
        
        // we must keep only parent nodes
        // if a child and its parent are selected, we keep only the parent
        ArrayList<RSMNode> keptNodes = new ArrayList<>(nbSelectedNode);
        keptNodes.add(selectedNodes[0]);
        mainloop:
        for (int i = 1; i < nbSelectedNode; i++) {
            RSMNode curNode = selectedNodes[i];

            // look for an ancestor
            int nbKeptNodes = keptNodes.size();
            for (int j = 0; j < nbKeptNodes; j++) {

                RSMNode curKeptNode = keptNodes.get(j);
                if (curNode.isNodeAncestor(curKeptNode)) {
                    // ancestor is already in kept node
                    continue mainloop;
                }
            }
            // look for children and remove them
            for (int j = nbKeptNodes - 1; j >= 0; j--) {

                RSMNode curKeptNode = keptNodes.get(j);
                if (curKeptNode.isNodeAncestor(curNode)) {
                    // we have found a children
                    keptNodes.remove(j);
                }
            }
            keptNodes.add(curNode);

        }


        int nbKeptNodes = keptNodes.size();
        for (int i=0;i<nbKeptNodes;i++) {
            RSMNode nodeCur = keptNodes.get(i);
            model.removeNodeFromParent(nodeCur);
        }


    }
    
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {

        int nbSelectedNode = selectedNodes.length;
        for (int i = 0; i < nbSelectedNode; i++) {
            RSMNode node = selectedNodes[i];

            RSMNode.NodeTypes type = node.getType();

            if ((type != RSMNode.NodeTypes.BIOLOGICAL_GROUP) && (type != RSMNode.NodeTypes.BIOLOGICAL_SAMPLE_ANALYSIS) && (type != RSMNode.NodeTypes.BIOLOGICAL_SAMPLE)) {
                setEnabled(false);
                return;
            }

        }
        
        setEnabled(true);

    }
}
