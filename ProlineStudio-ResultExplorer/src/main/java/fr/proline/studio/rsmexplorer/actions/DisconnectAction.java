package fr.proline.studio.rsmexplorer.actions;


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
    public void actionPerformed(RSMNode[] selectedNodes, int x, int y) {
        
        //JPM.TODO
        // must remove all nodes
        // close all windows
        // mange actions being done
        
        //DataStoreConnectorFactory.getInstance().closeAll();
    }
    
    
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
       /* int connectionState = UDSConnectionManagerOLD.getUDSConnectionManager().getConnectionState();
        boolean b = (connectionState == UDSConnectionManagerOLD.CONNECTION_DONE);
        setEnabled(b);*/
    }
    
}
