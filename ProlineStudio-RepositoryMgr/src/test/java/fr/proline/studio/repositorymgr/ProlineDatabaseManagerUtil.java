/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.repositorymgr;

import fr.proline.core.orm.util.DataStoreConnectorFactory;
import org.openide.util.Exceptions;

/**
 *
 * @author VD225637
 */
public class ProlineDatabaseManagerUtil  {
    protected final static String DB_CONFIG_PROP="/dbs_test_connection.properties";
    
    public static DataStoreConnectorFactory getOrInitDbManagment()  {     
        try {
            if(!DataStoreConnectorFactory.getInstance().isInitialized())
             DataStoreConnectorFactory.getInstance().initialize(DB_CONFIG_PROP);
        } catch(Exception e){
            Exceptions.printStackTrace(e);    
            return null;
        }        
    
        return DataStoreConnectorFactory.getInstance();
    }
}
