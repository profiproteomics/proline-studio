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
    "CTL_TaskLogAction=Tasks Log",
    "CTL_TaskLogTopComponent=Tasks Log",
    "HINT_TaskLogTopComponent=Tasks Log"
})
public class TaskLogTopComponent extends DataBoxViewerTopComponent {
    
    public TaskLogTopComponent() {
        super(WindowBoxFactory.getTaskListWindowBox());
        
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
