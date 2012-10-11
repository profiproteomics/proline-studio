/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;

import org.openide.util.NbBundle;

/**
 *
 * @author JM235353
 */
public class DeleteAction extends AbstractRSMAction {

   // private static DeleteAction instance = null;

    public DeleteAction() {
        super(NbBundle.getMessage(DeleteAction.class, "CTL_DeleteAction"));
    }
/*
    public static DeleteAction getInstance() {
        if (instance == null) {
            instance = new DeleteAction();
        }
        return instance;
    }

    @Override
    protected void performAction(Node[] nodes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean enable(Node[] nodes) {
       /* // a node must be selected
        boolean actionEnabled = ((nodes != null) && (nodes.length >= 1));

        // the parent node of all other nodes can not be deleted
        for (Node curNode : nodes) {
            if (((RSMNode) curNode).getType() == RSMNode.NodeTypes.TREE_PARENT) {
                actionEnabled = false;
                break;
            }
        }

        return actionEnabled;*/ //JPM.TODO
     /*   return true;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HelpCtx getHelpCtx() {
        throw new UnsupportedOperationException("Not supported yet.");
    }*/
}