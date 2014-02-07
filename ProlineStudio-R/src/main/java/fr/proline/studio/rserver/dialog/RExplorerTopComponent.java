package fr.proline.studio.rserver.dialog;


import fr.proline.studio.rserver.node.RTree;
import java.awt.GridLayout;
import javax.swing.JScrollPane;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top Component for R Server Tree
 * @author JM235353
 */

@ConvertAsProperties(dtd = "-//fr.proline.studio.rserver/dialog//RExplorer//EN",
autostore = false)
@TopComponent.Description(preferredID = "RExplorerTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Window", id = "fr.proline.studio.rsmexplorer.RExplorerTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 334
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_RExplorerAction",
preferredID = "RExplorerTopComponent")
@NbBundle.Messages({
    "CTL_RExplorerAction=Statistics Tool",
    "CTL_RExplorerTopComponent=Statistics Tool",
    "HINT_RExplorerTopComponent=Statistics Tool"
})
public class RExplorerTopComponent extends TopComponent {
    
    public RExplorerTopComponent() {
        
        initComponents();
        
        setName(Bundle.CTL_RExplorerTopComponent());
        setToolTipText(Bundle.HINT_RExplorerTopComponent());
    }
            
    private void initComponents() {

        JScrollPane treeScrollPane = new JScrollPane();
        RTree tree = RTree.getTree();
        treeScrollPane.setViewportView(tree);

        // Add panel
        setLayout(new GridLayout());
        add(treeScrollPane);

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
