/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;


import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.*;
import fr.proline.studio.dam.tasks.DatabaseProteinSetsTask;
import fr.proline.studio.rsmexplorer.DataViewerTopComponent;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMResultSummaryNode;
import javax.swing.Action;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;
import org.openide.windows.WindowManager;

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
    protected void performAction(Node[] nodes) {
        if ((nodes == null) || (nodes.length != 1)) {
            // should never happen
            return;
        }
        
        RSMResultSummaryNode node = (RSMResultSummaryNode) nodes[0];
        final ResultSummary rsm = node.getResultSummary();
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                DataViewerTopComponent viewer = (DataViewerTopComponent) WindowManager.getDefault().findTopComponent("DataViewerTopComponent");
                viewer.open();
                viewer.requestActive();

                ProteinSet[] proteinSetArray = (ProteinSet[]) ORMDataManager.instance().get(ResultSummary.class, rsm.getId(), "ProteinSet[]");
                viewer.setSelectedResultSummary(proteinSetArray);
            }
        };
        
        //JPM.TODO : create DatabaseAction which fetch needed data for ResultSummary
        AccessDatabaseThread.getAccessDatabaseThread().addTask(new DatabaseProteinSetsTask(callback, rsm));

        
    }

    @Override
    protected boolean enable(Node[] nodes) {
        

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