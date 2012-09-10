/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;


import fr.proline.studio.rsmexplorer.node.RSMNode;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author JM235353
 */
public class DetachAction extends NodeAction {

   private static DetachAction instance = null;

   private DetachAction() {
       putValue(Action.NAME, NbBundle.getMessage(DetachAction.class, "CTL_DetachAction"));
   }

   public static DetachAction getInstance() {
      if (instance == null) {
         instance = new DetachAction();
      }
      return instance;
   }


    @Override
    protected void performAction(Node[] nodes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean enable(Node[] nodes) {
        // a node must be selected
        boolean actionEnabled = ((nodes != null) && (nodes.length == 1));
        
        return actionEnabled;
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HelpCtx getHelpCtx() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}