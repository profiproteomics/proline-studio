package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.uds.Project;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMProjectNode;
import org.openide.util.NbBundle;

/**
 *
 * @author JM235353
 */
public class DisplayAllRsetAction extends AbstractRSMAction {

    public DisplayAllRsetAction() {
        super(NbBundle.getMessage(ConnectAction.class, "CTL_DisplayAllRset"));
    }
    
    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {
        
        RSMNode node = selectedNodes[0];
        
        RSMProjectNode projectNode = (RSMProjectNode) node.getParent();
        
        Project p = projectNode.getProject();
        
        // prepare window box
        WindowBox wbox = WindowBoxFactory.getAllResultSetWindowBox("All Search Results");
        wbox.setEntryData(p.getId(), p);

        // open a window to display the window box
        DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
        win.open();
        win.requestActive();
    }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
       setEnabled(true); //JPM.TODO
    }
    
}
