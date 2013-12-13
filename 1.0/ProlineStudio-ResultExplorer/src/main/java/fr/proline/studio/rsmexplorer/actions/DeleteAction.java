package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.node.RSMNode;
import org.openide.util.NbBundle;

/**
 *
 * @author JM235353
 */
public class DeleteAction extends AbstractRSMAction {

    public DeleteAction() {
        super(NbBundle.getMessage(DeleteAction.class, "CTL_DeleteAction"));
    }

    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {

        int nbSelectedNodes = selectedNodes.length;
        for (int i=0;i<nbSelectedNodes;i++) {
            RSMNode node = selectedNodes[i];
            
            if (! node.canBeDeleted()) {
                setEnabled(false);
                return;
            }
        }
        
        setEnabled(true);

    }
}