package fr.proline.studio.rsmexplorer.actions;


import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;
import fr.proline.studio.rsmexplorer.PropertiesTopComponent;
import fr.proline.studio.rsmexplorer.TaskLogTopComponent;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.ServerConnectionDialog;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action to connect or disconnect to a UDS database
 * @author jm235353
 */

@ActionID(category = "File", id = "fr.proline.studio.rsmexplorer.actions.ConnectAction")
@ActionRegistration(displayName = "Connect...")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 100)
})
public class ConnectAction  extends AbstractAction implements ContextAwareAction, ServerConnectionManager.ConnectionListener {

    private static ConnectAction m_action = null;
    
    private boolean m_connectAction;
    
    public ConnectAction() {

        setConnectionType(true, true);

        ServerConnectionManager.getServerConnectionManager().setConnectionListener(this);
    }
    
    public static ConnectAction getConnectionAction() {
        if (m_action == null) {
            m_action = new ConnectAction();
        }
        return m_action;
    }
    
    public final void setConnectionType(boolean connectAction, boolean enabled) {
        m_connectAction = connectAction;
        if (connectAction) {
            putValue(Action.NAME, NbBundle.getMessage(ConnectAction.class, "CTL_ConnectAction"));
        } else {
            putValue(Action.NAME, NbBundle.getMessage(ConnectAction.class, "CTL_DisconnectAction"));
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return getConnectionAction();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (m_connectAction) {
            // Connect...
        
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
                            // RSMTree.getTree().startLoading(); //JPM.TODO.TREE
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

        } else {
            // Disconnect...
            
            // close all specific windows
            Set<TopComponent> tcs = TopComponent.getRegistry().getOpened();
            Iterator<TopComponent> itTop = tcs.iterator();
            while (itTop.hasNext()) {
                TopComponent topComponent = itTop.next();
                if (((topComponent instanceof DataBoxViewerTopComponent) || (topComponent instanceof PropertiesTopComponent)) && !(topComponent instanceof TaskLogTopComponent)) {
                    topComponent.close();
                }
            }

            // must remove all projects
            ProjectExplorerPanel.getProjectExplorerPanel().clearAll();
            
            // change menu to connect
            //connectionChanged(false);

            // open the dialog to connect to another user
            ServerConnectionDialog serverConnectionDialog = ServerConnectionDialog.getDialog(WindowManager.getDefault().getMainWindow());
            serverConnectionDialog.setChangeUser();
            serverConnectionDialog.centerToFrame(WindowManager.getDefault().getMainWindow());
            serverConnectionDialog.setVisible(true);
            
            
        }
    }

    @Override
    public void connectionDone() {
        //setConnection(!connected);
    }



    
}
