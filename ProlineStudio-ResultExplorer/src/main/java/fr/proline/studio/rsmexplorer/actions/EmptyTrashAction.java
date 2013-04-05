/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.node.RSMDataSetNode;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import org.openide.util.NbBundle;

/**
 *
 * @author JM235353
 */
public class EmptyTrashAction extends AbstractRSMAction {

    public EmptyTrashAction() {
        super(NbBundle.getMessage(DeleteAction.class, "CTL_EmptyTrashAction"));
    }
    
    @Override
    public void actionPerformed(final RSMNode[] selectedNodes, int x, int y) {
        
    }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
        setEnabled(false); //JPM.TODO
        /*
        if (selectedNodes.length!=1) {
            setEnabled(false);
            return;
        }
        
        RSMDataSetNode trashNode = (RSMDataSetNode) selectedNodes[0];
        
        setEnabled(!trashNode.isLeaf());
        * */
    }
    
}
