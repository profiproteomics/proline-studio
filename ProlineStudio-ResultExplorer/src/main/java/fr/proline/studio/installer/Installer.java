package fr.proline.studio.installer;



import fr.proline.studio.dam.taskinfo.TaskInfoManager;
import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rserver.RServerManager;
import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;

/**
 * 1- Used to connect to web-core and UDS database as soon as possible
 * 
 * 2- Used to check when the user wants to close the application
 * 
 * @author jm235353
 */
public class Installer extends VersionInstaller {

    @Override
    public void restored() {

        String buildnumber = "1.1.2  alpha ("+moduleBuildDate+")"; //specify if alpha (or nothing = release)
//        String buildnumber = "1.1.1";
        
        // set the proline version for the application title
        System.setProperty("netbeans.buildnumber", buildnumber);  //"1.0.1 (alpha : build date @build.date@)"
        
        // initialize the connection to the server as soon as possible
        ServerConnectionManager.getServerConnectionManager();
        
    }
    
    @Override
    public boolean closing() {
        
        // check if there is tasks being done which ask not to close the application
        if (TaskInfoManager.getTaskInfoManager().askBeforeExitingApp()) {
            InfoDialog exitDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "Warning", "You should not exit. Important tasks are being done.\nAre you sure you want to exit ?");
            exitDialog.setButtonName(OptionDialog.BUTTON_OK, "Yes");
            exitDialog.setButtonName(OptionDialog.BUTTON_CANCEL, "No");
            exitDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
            exitDialog.setVisible(true);
            
            if (exitDialog.getButtonClicked() == OptionDialog.BUTTON_CANCEL) {
                // No clicked
                return false;
            }
        
        }
        
        // Close connection to R Server and kill it if needed
        RServerManager.getRServerManager().stopRProcess();
        
        //Close connection to JMS Server if needed
        JMSConnectionManager.getJMSConnectionManager().closeConnection();
        
        return true;
    }
}
