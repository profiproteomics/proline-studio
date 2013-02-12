package fr.proline.studio.rsmexplorer.actions;


import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Action to connect to a UDS database
 * @author jm235353
 */
public class ConnectAction extends AbstractRSMAction {

    public ConnectAction() {
        super(NbBundle.getMessage(ConnectAction.class, "CTL_ConnectAction"));
    }
    
    @Override
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {
     /*       DatabaseConnectionDialog databaseConnectionDialog = DatabaseConnectionDialog.getDialog(WindowManager.getDefault().getMainWindow());
            databaseConnectionDialog.centerToFrame(WindowManager.getDefault().getMainWindow());
            databaseConnectionDialog.setVisible(true);

            UDSConnectionManagerOLD udsMgr = UDSConnectionManagerOLD.getUDSConnectionManager();
            if (udsMgr.getConnectionState() == UDSConnectionManagerOLD.CONNECTION_DONE) {
                RSMTree.getTree().startLoading();
            }*/
    }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
       /* int connectionState = UDSConnectionManagerOLD.getUDSConnectionManager().getConnectionState();
        boolean b = (connectionState == UDSConnectionManagerOLD.NOT_CONNECTED) || (connectionState == UDSConnectionManagerOLD.CONNECTION_FAILED);
        setEnabled(b);*/
    }
    
}
