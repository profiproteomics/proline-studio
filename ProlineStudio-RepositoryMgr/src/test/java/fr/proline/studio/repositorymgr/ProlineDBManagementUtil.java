/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.repositorymgr;

import fr.proline.repository.DatabaseConnector;
import org.openide.util.Exceptions;

/**
 *
 * @author VD225637
 */
public class ProlineDBManagementUtil {
    protected final static String DB_CONFIG_PROP="/dbs_test_connection.properties";
    
    public static ProlineDBManagement getOrInitDbManagment()  {        
        try {
             ProlineDBManagement.getProlineDBManagement();
        } catch(UnsupportedOperationException uoe){
            try {
                //Should be initialized                
                DatabaseConnector udsC = new DatabaseConnector(DB_CONFIG_PROP);
                ProlineDBManagement.initProlineDBManagment(udsC);

            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);                  
                return null;
            }        
        }
        return ProlineDBManagement.getProlineDBManagement();
    }
}
