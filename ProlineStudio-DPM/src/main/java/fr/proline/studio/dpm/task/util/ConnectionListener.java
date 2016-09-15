/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dpm.task.util;

import java.util.EventListener;

/**
 *
 * @author VD225637
 */
public interface ConnectionListener extends EventListener {
    
    //Use same value as ServerConnectionManager => To MERGE ! 
    public static int NOT_CONNECTED = 0;   
    public static final int CONNECTION_FAILED = 2;
    public static final int CONNECTION_DONE = 3;
    
    public void connectionStateChanged(int newStatus);
}
