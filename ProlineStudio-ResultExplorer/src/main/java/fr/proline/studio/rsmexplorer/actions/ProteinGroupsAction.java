/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMResultSummaryNode;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author JM235353
 */
public class ProteinGroupsAction extends NodeAction {

   private static ProteinGroupsAction instance = null;

   private ProteinGroupsAction() {
       putValue(Action.NAME, NbBundle.getMessage(ProteinGroupsAction.class, "CTL_ProteinGroupsAction"));
   }

   public static ProteinGroupsAction getInstance() {
      if (instance == null) {
         instance = new ProteinGroupsAction();
      }
      return instance;
   }

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
  


    @Override
    protected void performAction(Node[] nodes) {
        if ((nodes == null) || (nodes.length != 1)) {
            // should never happen
            return;
        }
        
        RSMResultSummaryNode node = (RSMResultSummaryNode) nodes[0];
        //node. //JPM.TODO
    }

    @Override
    protected boolean enable(Node[] nodes) {
        
        System.out.println("ProteinGroupsAction.enable");
        
        // a node must be selected
        boolean actionEnabled = ((nodes != null) && (nodes.length == 1));
        
        if (actionEnabled) {
            RSMNode node = (RSMNode) nodes[0];
            actionEnabled =  node.searchChildNodeOfAType(RSMNode.NodeTypes.RESULT_SUMMARY);
        }
        
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