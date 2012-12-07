/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.actions;



import fr.proline.core.orm.msi.ResultSet;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMResultSetNode;
import fr.proline.studio.rsmexplorer.node.RSMResultSummaryNode;
import org.openide.util.NbBundle;

/**
 *
 * @author JM235353
 */
public class PeptidesAction extends AbstractRSMAction {

   //private static ProteinGroupsAction instance = null;

   public PeptidesAction() {
       super(NbBundle.getMessage(PeptidesAction.class, "CTL_PeptidesAction"));
   }

    @Override
    public void actionPerformed(RSMNode n) {
        
        ResultSet rset = null;
        if (n.getType() ==  RSMNode.NodeTypes.RESULT_SET) {
            rset = ((RSMResultSetNode) n).getResultSet();
        } else if (n.getType() ==  RSMNode.NodeTypes.RESULT_SUMMARY) {
            rset = ((RSMResultSummaryNode) n).getResultSummary().getResultSet();
        }
        
        // prepare window box
        WindowBox wbox = WindowBoxFactory.getPeptidesWindowBox();
        wbox.setEntryData(rset);
        
        
        // open a window to display the window box
        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
        win.open();
        win.requestActive(); 
        

    }
   

    
}