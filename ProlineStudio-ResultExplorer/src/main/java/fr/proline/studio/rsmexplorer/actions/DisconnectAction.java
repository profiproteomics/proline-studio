package fr.proline.studio.rsmexplorer.actions;


import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.PropertiesTopComponent;
import fr.proline.studio.rsmexplorer.TaskLogTopComponent;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.ServerConnectionDialog;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import java.util.Iterator;
import java.util.Set;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action to disconnect a user (unload projects)
 * @author jm235353
 */
public class DisconnectAction extends AbstractRSMAction {

    public DisconnectAction() {
        super(NbBundle.getMessage(ConnectAction.class, "CTL_DisconnectAction"));
    }
    
    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {

        // close all specific windows
        Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
        Iterator<TopComponent> itTop = tcs.iterator();
        while (itTop.hasNext()) {
            TopComponent topComponent = itTop.next();
            if (((topComponent instanceof DataBoxViewerTopComponent) || (topComponent instanceof PropertiesTopComponent)) && !(topComponent instanceof TaskLogTopComponent)) {
                topComponent.close();
            }
        }
        
        // must remove all nodes
        //RSMTree.getTree().removeRootChildren();
        ProjectExplorerPanel.getProjectExplorerPanel().selectProject(null);
        RSMTree.clearAll();

        
        // open the dialog to connect to another user
        ServerConnectionDialog serverConnectionDialog = ServerConnectionDialog.getDialog(WindowManager.getDefault().getMainWindow());
        serverConnectionDialog.setChangeUser();
        serverConnectionDialog.centerToFrame(WindowManager.getDefault().getMainWindow());
        serverConnectionDialog.setVisible(true);

    }
    
    
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
        setEnabled((selectedNodes[0].getChildCount()>0));
    }
    
}
