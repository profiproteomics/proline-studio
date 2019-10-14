/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.installer;



import fr.proline.studio.dam.taskinfo.TaskInfoManager;
import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import fr.proline.studio.gui.InfoDialog;
import fr.proline.studio.gui.OptionDialog;
import fr.proline.studio.rserver.RServerManager;
import java.awt.Frame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.openide.windows.WindowManager;

/**
 * 1- Used to connect to web-core and UDS database as soon as possible
 * 
 * 2- Used to check when the user wants to close the application
 * 
 * @author jm235353
 */
public class Installer extends VersionInstaller {

    @Override
    public void restored() {

        // for Mac : we need to use Metal UI, otherwise the browse file on server does not work
        forceMetalUIForMac();
        
        String productVersion = moduleVersion;
        int firstIndex = moduleVersion.indexOf('.');
        int secIndex =  moduleVersion.length();
        if(firstIndex >0)
            secIndex = moduleVersion.indexOf('.', firstIndex+1);        
        if(secIndex>0)
            productVersion = moduleVersion.substring(0, secIndex);
        
        String buildnumber = productVersion+" Milestone ("+moduleBuildDate+")"; //specify if Milestone, Release Candidate or release (or nothing = release)
        
        // set the proline version for the application title
        System.setProperty("netbeans.buildnumber", buildnumber);  //"1.0.1 (alpha : build date @build.date@)"
        
        // initialize the connection to the server as soon as possible
        ServerConnectionManager.getServerConnectionManager();
        
    }
    
    private void forceMetalUIForMac() {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("mac")) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {

                    try {
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                        Frame f = WindowManager.getDefault().getMainWindow();
                        if (f == null) {
                            // should never be null
                            forceMetalUIForMac();
                        } else {
                            SwingUtilities.updateComponentTreeUI(f);
                        }
                    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
                        e.printStackTrace();
                    }

                }

            });
        }
    }
    
    @Override
    public boolean closing() {
        
        // check if there is tasks being done which ask not to close the application
        if (TaskInfoManager.getTaskInfoManager().askBeforeExitingApp()) {
            InfoDialog exitDialog = new InfoDialog(WindowManager.getDefault().getMainWindow(), InfoDialog.InfoType.WARNING, "Warning", "You should not exit. Important tasks are being done.\nAre you sure you want to exit ?");
            exitDialog.setButtonName(OptionDialog.BUTTON_OK, "Yes");
            exitDialog.setButtonName(OptionDialog.BUTTON_CANCEL, "No");
            exitDialog.centerToWindow(WindowManager.getDefault().getMainWindow());
            exitDialog.setVisible(true);
            
            if (exitDialog.getButtonClicked() == OptionDialog.BUTTON_CANCEL) {
                // No clicked
                return false;
            }
        
        }
        
        // Close connection to R Server and kill it if needed
        RServerManager.getRServerManager().stopRProcess();
        
        //Close connection to JMS Server if needed
        JMSConnectionManager.getJMSConnectionManager().closeConnection();
        
        return true;
    }
}
