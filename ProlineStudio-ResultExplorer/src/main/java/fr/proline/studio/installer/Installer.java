/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.installer;

import fr.proline.studio.dam.UDSConnectionManager;
import org.openide.modules.ModuleInstall;


public class Installer extends ModuleInstall {

    @Override
    public void restored() {

        // initialize the connection with the UDS if it is possible
        UDSConnectionManager.getUDSConnectionManager();
 

    }
}
