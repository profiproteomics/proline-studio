package fr.proline.studio.rsmexplorer.actions;

import fr.proline.core.orm.util.DatabaseManager;
import fr.proline.studio.dam.UDSConnectionManager;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import org.openide.util.NbBundle;

/**
 * Action to disconnect of a UDS database
 * @author jm235353
 */
public class DisconnectAction extends AbstractRSMAction {

    public DisconnectAction() {
        super(NbBundle.getMessage(ConnectAction.class, "CTL_DisconnectAction"));
    }
    
    @Override
    public void actionPerformed(RSMNode n, int x, int y) {
        
        //JPM.TODO
        // must remove all nodes
        // close all windows
        // mange actions being done
        
        //DatabaseManager.getInstance().closeAll();
    }
    
    
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
        int connectionState = UDSConnectionManager.getUDSConnectionManager().getConnectionState();
        boolean b = (connectionState == UDSConnectionManager.CONNECTION_DONE);
        setEnabled(b);
    }
    
}
