package fr.proline.studio.rsmexplorer.actions;


import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.rsmexplorer.gui.dialog.ServerConnectionDialog;
import fr.proline.studio.rsmexplorer.node.RSMNode;
import fr.proline.studio.rsmexplorer.node.RSMTree;
import javax.swing.SwingUtilities;
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
        
        ServerConnectionManager serciceConnectionMgr = ServerConnectionManager.getServerConnectionManager();
        
        if ((serciceConnectionMgr.isNotConnected()) || (serciceConnectionMgr.isConnectionFailed())) {
            // the user need to enter connection parameters

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    ServerConnectionDialog serverConnectionDialog = ServerConnectionDialog.getDialog(WindowManager.getDefault().getMainWindow());
                    serverConnectionDialog.centerToScreen();
                    //databaseConnectionDialog.centerToFrame(WindowManager.getDefault().getMainWindow()); // does not work : main window has not its size most of the time at this point
                    serverConnectionDialog.setVisible(true);

                    ServerConnectionManager serciceConnectionMgr = ServerConnectionManager.getServerConnectionManager();
                    if (serciceConnectionMgr.isConnectionDone()) {
                        RSMTree.getTree().startLoading();
                    }
                }
            });

        } else {

            // open the dialog to connect to another user
            ServerConnectionDialog serverConnectionDialog = ServerConnectionDialog.getDialog(WindowManager.getDefault().getMainWindow());
            serverConnectionDialog.setChangeUser();
            serverConnectionDialog.centerToFrame(WindowManager.getDefault().getMainWindow());
            serverConnectionDialog.setVisible(true);
        }
    }
    
    @Override
    public void updateEnabled(RSMNode[] selectedNodes) {
       setEnabled((selectedNodes[0].getChildCount()==0));
    }
    
}
