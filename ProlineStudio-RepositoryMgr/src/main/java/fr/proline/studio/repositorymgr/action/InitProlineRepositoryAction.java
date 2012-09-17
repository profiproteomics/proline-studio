/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.repositorymgr.action;

import fr.proline.repository.DatabaseConnector;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionID(category = "File",
id = "fr.proline.studio.action.InitProlineRepositoryAction")
@ActionRegistration(displayName = "#CTL_InitProlineRepositoryAction")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1450, separatorAfter = 1475)
})
@Messages({"CTL_InitProlineRepositoryAction=Initialize Repository",
    "get.UDS.configuration.title=Get UDS database configuration file",
    "# {0} - cause of error ",
    "initRepositity.error=Unable to initialize repository access ({0})",
    "initRepositity.title=Initialization of repository access",
    "initRepositity.success=Initialization of repository access successfull"
})

public final class InitProlineRepositoryAction implements ActionListener {
//  "dbPropFileName=/proline_dbs_connection.properties",

    protected static Logger logger = LoggerFactory.getLogger(InitProlineRepositoryAction.class);

    @Override
    public void actionPerformed(ActionEvent e) {

        logger.debug(" START InitProlineRepositoryAction ");
        // Get UDS configuration file.
        JFileChooser fchooser = new JFileChooser();
        fchooser.setMultiSelectionEnabled(false);
        fchooser.setDialogTitle(Bundle.get_UDS_configuration_title());
        File udsDBPropFile;
        int result = fchooser.showOpenDialog(null);
        switch (result) {
            case JFileChooser.APPROVE_OPTION:
                udsDBPropFile = fchooser.getSelectedFile();
                break;
            case JFileChooser.ERROR_OPTION:
            case JFileChooser.CANCEL_OPTION:
            default:
                return;
        }
        logger.debug("Read File {}", udsDBPropFile.getAbsolutePath());
        
        try {                
            DatabaseConnector udsConn = new DatabaseConnector(getFilePropertiesMap(udsDBPropFile));
            ProlineDBManagement.initProlineDBManagment(udsConn);
            JOptionPane.showMessageDialog(null, Bundle.initRepositity_success(), Bundle.initRepositity_title(), JOptionPane.INFORMATION_MESSAGE);
        }catch (IOException ioe){
            String msg = ioe.getMessage();
            JOptionPane.showMessageDialog(null, Bundle.initRepositity_error(msg), Bundle.initRepositity_title(), JOptionPane.ERROR_MESSAGE);
            logger.warn(Bundle.initRepositity_error(msg));
        } catch (Exception ex) {
            String msg = ex.getMessage();
            JOptionPane.showMessageDialog(null, Bundle.initRepositity_error(msg), Bundle.initRepositity_title(), JOptionPane.ERROR_MESSAGE);
            logger.warn(Bundle.initRepositity_error(msg));
        }
        
        try {
            logger.debug(" Used UDS Database connection = {}", ProlineDBManagement.getProlineDBManagement().getDatabaseConnector(ProlineRepository.Databases.UDS).getConnection().getMetaData().getURL());
        } catch (Exception ex) {
            String msg = ex.getMessage();
            JOptionPane.showMessageDialog(null, Bundle.initRepositity_error(msg), Bundle.initRepositity_title(), JOptionPane.ERROR_MESSAGE);
            logger.warn(Bundle.initRepositity_error(msg));
        }
    }
    
    private Map getFilePropertiesMap(File propFile) throws IOException {
        Properties p = new Properties();
        p.load(new FileInputStream(propFile));

        Map<String, String> propMap = new HashMap<String, String>((Map) p);

        //
        // Get the entry set of the Map and print it out.
        //
        Set<Map.Entry<String, String>> propertySet = propMap.entrySet();
        for (Map.Entry entry : propertySet) {            
            logger.debug("{} = {}", entry.getKey(), entry.getValue());
        }
        return propMap;
    }
}
