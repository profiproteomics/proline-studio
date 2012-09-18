/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam;

import java.util.HashMap;

/**
 *
 * @author JM235353
 */
public abstract class DatabaseCallback {
    
    protected HashMap<Class,Object> results = null;
    
    public void addResult(Class key, Object result) {
        if (results == null) {
            results = new HashMap<Class, Object>();
        }
        results.put(key, result);
    }
    
    public abstract void run();
    
}
