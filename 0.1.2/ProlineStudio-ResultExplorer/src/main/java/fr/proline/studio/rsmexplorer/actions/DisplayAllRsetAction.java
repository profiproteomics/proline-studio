package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.uds.Project;
import fr.proline.studio.pattern.WindowBox;
import fr.proline.studio.pattern.WindowBoxFactory;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMProjectNode;
import java.util.Set;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action to display a window with all Search Results (rset)
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
        
        String windowName = p.getName()+" : All Imported";
        

        TopComponent tc = findTopComponent(windowName);
        if (tc != null) {
            tc.requestActive();
        } else {
        
            // prepare window box
            WindowBox wbox = WindowBoxFactory.getAllResultSetWindowBox(windowName);
            wbox.setEntryData(p.getId(), p);

            // open a window to display the window box
            DataBoxViewerTopComponent win = new DataBoxViewerTopComponent(wbox);
            win.open();
            win.requestActive();
        }

    }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
       setEnabled(true); //JPM.TODO
    }
    
    
    private TopComponent findTopComponent(String name) {
        Set<TopComponent> openTopComponents = WindowManager.getDefault().getRegistry().getOpened();
        for (TopComponent tc : openTopComponents) {
            if (tc.getName().compareTo(name) == 0) {
                return tc;
            }
        }
        return null;
    }
    
}
