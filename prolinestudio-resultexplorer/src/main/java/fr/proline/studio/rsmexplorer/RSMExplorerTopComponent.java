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
package fr.proline.studio.rsmexplorer;



import fr.proline.studio.dpm.ServerConnectionManager;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.ServerConnectionDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.SwingUtilities;

import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;

import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;


/**
 * Top component to dispay the RSM Tree.
 */
@ConvertAsProperties(dtd = "-//fr.proline.studio.rsmexplorer//RSMExplorer//EN",
autostore = false)
@TopComponent.Description(preferredID = "RSMExplorerTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = true, position=1)
@ActionID(category = "Window", id = "fr.proline.studio.rsmexplorer.RSMExplorerTopComponent")
@ActionReference(path = "Menu/Window"
 , position = 10
 )
@TopComponent.OpenActionRegistration(displayName = "#CTL_RSMExplorerAction",
preferredID = "RSMExplorerTopComponent")
@Messages({
    "CTL_RSMExplorerAction=Projects",
    "CTL_RSMExplorerTopComponent=Projects",
    "HINT_RSMExplorerTopComponent=Projects"
})
public final class RSMExplorerTopComponent extends TopComponent  {

    
    public RSMExplorerTopComponent() {
        initComponents();
        setName(Bundle.CTL_RSMExplorerTopComponent());
        setToolTipText(Bundle.HINT_RSMExplorerTopComponent());

    }
    
 
    
    private void initComponents() {
        
         // Add panel
         setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        add(ProjectExplorerPanel.getProjectExplorerPanel(), c);
    }


    @Override
    public void componentOpened() {
        
        Thread t = new Thread() {
            @Override
            public void run() {

                ServerConnectionManager serverConnectionMgr = ServerConnectionManager.getServerConnectionManager();
                while (serverConnectionMgr.isConnectionAsked()) {
                    // wait for the connection to have succedeed or failed
                    try {
                        Thread.sleep(100); // JPM.TODO : one day remove the polling and write blocking code instead
                    } catch (InterruptedException ex) {
                    }
                }

                if ((serverConnectionMgr.isNotConnected()) || (serverConnectionMgr.isConnectionFailed())) {
                    // the user need to enter connection parameters

                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            ServerConnectionDialog serverConnectionDialog = ServerConnectionDialog.getDialog(WindowManager.getDefault().getMainWindow());
                            serverConnectionDialog.centerToScreen();
                            //databaseConnectionDialog.centerToFrame(WindowManager.getDefault().getMainWindow()); // does not work : main window has not its size most of the time at this point
                            serverConnectionDialog.setVisible(true);
                            
                            ServerConnectionManager serciceConnectionMgr = ServerConnectionManager.getServerConnectionManager();
                            if (serciceConnectionMgr.isConnectionDone()) {
                                ProjectExplorerPanel.getProjectExplorerPanel().startLoadingProjects();
                                //RSMTree.getTree().startLoading();
                            }
                        }
                    });

                } else if (serverConnectionMgr.isConnectionDone()) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            ProjectExplorerPanel.getProjectExplorerPanel().startLoadingProjects();
                            //RSMTree.getTree().startLoading();
                        }
                    }); 
                }
   
            }
        };
        t.start();
        
        
        // check if the connection to the UDS is done
        
        
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    
        
    
    
    
    
}
