/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.action;

import static fr.proline.studio.action.Bundle.*;
import fr.proline.studio.dbs.ProlineDbManagment;
import fr.proline.repository.ConnectionPrototype;
import fr.proline.repository.ProlineRepository;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
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
       "dbPropFileName=/proline_dbs_connection.properties",
       "# {0} - cause of error ",
       "connPrototype.error=Error reading UDS db properties ({0})"        
})
public final class InitProlineRepositoryAction implements ActionListener {

    protected static Logger logger = LoggerFactory.getLogger(InitProlineRepositoryAction.class);
    
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            logger.debug(" START Read UDS "+dbPropFileName());           
            ConnectionPrototype conProto = new ConnectionPrototype( dbPropFileName()).namePattern("proline_default");
            ProlineRepository repo = ProlineRepository.getRepositoryManager(conProto);            
            ProlineDbManagment.intiProlineDbManagment(repo);
            logger.info(" Used UDS Database connection = "+ProlineDbManagment.getProlineDbManagment().getDatabaseConnector(ProlineRepository.Databases.UDS).getConnection().getMetaData().getURL());        
        } catch (SQLException sqle) {
            logger.warn(connPrototype_error(sqle));        
        }catch(IOException ioe){           
            logger.warn(connPrototype_error(ioe));
        } catch (Exception ex) {
            logger.warn(connPrototype_error(ex));        
        }                    
    }
}
