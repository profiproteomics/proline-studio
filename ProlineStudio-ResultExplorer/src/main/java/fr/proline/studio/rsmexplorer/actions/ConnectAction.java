package fr.proline.studio.rsmexplorer.actions;

import fr.proline.studio.dam.UDSConnectionManager;
import fr.proline.studio.rsmexplorer.gui.dialog.DatabaseConnectionDialog;
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
    public void actionPerformed(RSMNode n, int x, int y) {
            DatabaseConnectionDialog databaseConnectionDialog = DatabaseConnectionDialog.getDialog(WindowManager.getDefault().getMainWindow());
            databaseConnectionDialog.centerToFrame(WindowManager.getDefault().getMainWindow());
            databaseConnectionDialog.setVisible(true);

            UDSConnectionManager udsMgr = UDSConnectionManager.getUDSConnectionManager();
            if (udsMgr.getConnectionState() == UDSConnectionManager.CONNECTION_DONE) {
                RSMTree.getTree().startLoading();
            }
    }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
        int connectionState = UDSConnectionManager.getUDSConnectionManager().getConnectionState();
        boolean b = (connectionState == UDSConnectionManager.NOT_CONNECTED) || (connectionState == UDSConnectionManager.CONNECTION_FAILED);
        setEnabled(b);
    }
    
}
