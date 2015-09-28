package fr.proline.studio.rsmexplorer.actions;


import fr.proline.studio.dam.taskinfo.TaskInfoManager;
import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.ServerConnectionDialog;
import java.awt.event.ActionEvent;
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
import org.openide.windows.WindowManager;

/**
 * Action to connect or disconnect to a UDS database
 * @author jm235353
 */

@ActionID(category = "File", id = "fr.proline.studio.rsmexplorer.actions.ConnectAction")
@ActionRegistration(displayName = "#CTL_ConnectAction")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 100)
})
@NbBundle.Messages("CTL_ConnectAction=Connect...")
public class ConnectAction  extends AbstractAction implements ContextAwareAction {

    private static ConnectAction m_action = null;
    
    private static boolean m_connectAction;
    
    private ConnectAction() {

        m_action = this;
        
        setConnectionType(true, true);
    }

    
    public final static void setConnectionType(boolean connectAction, boolean enabled) {
        m_connectAction = connectAction;
        
        if (m_action != null) {
            m_action.setEnabled(enabled);
            if (connectAction) {
                m_action.putValue(Action.NAME, NbBundle.getMessage(ConnectAction.class, "CTL_ConnectAction"));
            } else {
                m_action.putValue(Action.NAME, "Disconnect...");
            }
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new ConnectAction();
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
                            ProjectExplorerPanel.getProjectExplorerPanel().startLoadingProjects();
                        }
                    }
                });

            } else {

                // open the dialog to connect to another user
                ServerConnectionDialog serverConnectionDialog = ServerConnectionDialog.getDialog(WindowManager.getDefault().getMainWindow());
                serverConnectionDialog.setChangeUser();
                serverConnectionDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
                serverConnectionDialog.setVisible(true);
            }

        } else {
            // Disconnect...
            
            
            // check if there is tasks being done which ask not to disconnect/close the application
            if (TaskInfoManager.getTaskInfoManager().askBeforeExitingApp()) {
                InfoDialog exitDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "Warning", "You should not disconnect. Important tasks are being done.\nAre you sure you want to disconnect ?");
                exitDialog.setButtonName(OptionDialog.BUTTON_OK, "Yes");
                exitDialog.setButtonName(OptionDialog.BUTTON_CANCEL, "No");
                exitDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
                exitDialog.setVisible(true);

                if (exitDialog.getButtonClicked() == OptionDialog.BUTTON_CANCEL) {
                    // No clicked
                    return;
                }

            }
            



            // open the dialog to connect to another user
            ServerConnectionDialog serverConnectionDialog = ServerConnectionDialog.getDialog(WindowManager.getDefault().getMainWindow());
            serverConnectionDialog.setChangeUser();
            serverConnectionDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
            serverConnectionDialog.setVisible(true);
            
            
        }
    }


    
}
