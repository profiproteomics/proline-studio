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

import fr.proline.studio.pattern.WindowBoxFactory;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top Component for logs of tasks and services
 * @author JM235353
 */

@ConvertAsProperties(dtd = "-//fr.proline.studio.rsmexplorer//TaskLog//EN",
autostore = false)
@TopComponent.Description(preferredID = "TaskLogTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "fr.proline.studio.rsmexplorer.TaskLogTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_TaskLogAction",
preferredID = "TaskLogTopComponent")
@NbBundle.Messages({
    "CTL_TaskLogAction=Logs",
    "CTL_TaskLogTopComponent=Logs",
    "HINT_TaskLogTopComponent=Logs"
})
public class TaskLogTopComponent extends MultiDataBoxViewerTopComponent {
    
    public TaskLogTopComponent() {
        super(WindowBoxFactory.getSystemMonitoringWindowBox(),"Logs");
        
        setName(Bundle.CTL_TaskLogTopComponent());
        setToolTipText(Bundle.HINT_TaskLogTopComponent());
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
    
    
    @Override
    protected void componentClosed() {
    }
    
}
