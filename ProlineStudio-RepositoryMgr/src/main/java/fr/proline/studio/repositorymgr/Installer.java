/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.repositorymgr;

//import fr.proline.repository.DatabaseConnector;
//import fr.proline.repository.ProlineRepository;
import fr.proline.core.orm.util.DatabaseManager;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.swing.JOptionPane;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NbBundle.Messages({"initRepositity.error=Unable to initialize repository access ({0})",
    "initRepositity.title=Initialization of repository access",
    "default.UDS.configuration.file=/proline_uds_db_conn.properties"
})
public class Installer extends ModuleInstall {
   protected static Logger logger = LoggerFactory.getLogger(Installer.class);

   @Override
    public void restored() {        
        try {         
            
            logger.info("Read URL {}",Bundle.default_UDS_configuration_file());  

        } catch (Exception ex) {
            String msg = ex.getMessage();
            logger.warn(Bundle.initRepositity_error(msg));
            JOptionPane.showMessageDialog(null, Bundle.initRepositity_error(msg), Bundle.initRepositity_title(), JOptionPane.ERROR_MESSAGE);            
        }
          
    }

    @Override
    public boolean closing() {
        logger.debug("----------- repositorymgr  Installer closing");        
        return super.closing();
    }

}
