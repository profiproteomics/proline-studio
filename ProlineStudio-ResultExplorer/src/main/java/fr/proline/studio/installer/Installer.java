package fr.proline.studio.installer;



import fr.proline.studio.dpm.ServerConnectionManager;
import org.openide.modules.ModuleInstall;

/**
 * Used to connect to web-core and UDS database as soon as possible
 * @author jm235353
 */
public class Installer extends ModuleInstall {

    @Override
    public void restored() {

        // set the proline version for the application title
        System.setProperty("netbeans.buildnumber", "Beta"); 
        
        // initialize the connection to the server as soon as possible
        ServerConnectionManager.getServerConnectionManager();

    }
}
