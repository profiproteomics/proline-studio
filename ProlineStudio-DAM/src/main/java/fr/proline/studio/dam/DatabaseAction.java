/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class DatabaseAction { //JPM.TODO : set as abstract
    
    protected static final Logger logger = LoggerFactory.getLogger(DatabaseAction.class);
    
    protected DatabaseCallback callback;
    
    public DatabaseAction(DatabaseCallback callback) {
        this.callback = callback;
    }
    
    public boolean callbackInAWT() {
        return true;
    }
    
    public boolean fetchData() {
        return true;
    }
    
    public void callback() {
        if (callback != null) {
            callback.run();
        }
    }
    
}
