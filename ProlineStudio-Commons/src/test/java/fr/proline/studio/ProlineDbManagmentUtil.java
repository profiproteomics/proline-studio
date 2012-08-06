/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio;

import fr.proline.repository.DatabaseConnector;
import fr.proline.studio.dbs.ProlineDbManagment;
import java.io.IOException;
import org.openide.util.Exceptions;

/**
 *
 * @author VD225637
 */
public class ProlineDbManagmentUtil {
    protected final static String DB_CONFIG_PROP="/dbs_test_connection.properties";
    
    public static ProlineDbManagment getOrInitDbManagment()  {        
        try {
             ProlineDbManagment.getProlineDbManagment();
        } catch(UnsupportedOperationException uoe){
            try {
                //Should be initialized                
                DatabaseConnector udsC = new DatabaseConnector(DB_CONFIG_PROP);
                ProlineDbManagment.initProlineDbManagment(udsC);

            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);                  
                return null;
            }        
        }
        return ProlineDbManagment.getProlineDbManagment();
    }
}
