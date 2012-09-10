/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer;

import fr.proline.studio.dam.ContextData;
import fr.proline.studio.gui.SearchPanel;
import fr.proline.studio.rsmexplorer.node.RSMChildFactory;
import fr.proline.studio.rsmexplorer.node.RSMResultSummaryNode;
import fr.proline.studio.rsmexplorer.node.RSMTreeParentNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.Lookups;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//fr.proline.studio.rsmexplorer//RSMExplorer//EN",
autostore = false)
@TopComponent.Description(preferredID = "RSMExplorerTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "fr.proline.studio.rsmexplorer.RSMExplorerTopComponent")
@ActionReference(path = "Menu/Window" /*
 * , position = 333
 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_RSMExplorerAction",
preferredID = "RSMExplorerTopComponent")
@Messages({
    "CTL_RSMExplorerAction=Identifications",
    "CTL_RSMExplorerTopComponent=Identifications",
    "HINT_RSMExplorerTopComponent=Identifications of your Project"
})
public final class RSMExplorerTopComponent extends TopComponent implements ExplorerManager.Provider {

    private final ExplorerManager mgr = new ExplorerManager();

    
    public RSMExplorerTopComponent() {
        initComponents();
        setName(Bundle.CTL_RSMExplorerTopComponent());
        setToolTipText(Bundle.HINT_RSMExplorerTopComponent());

        // create root node
        ContextData parent = new ContextData();
        mgr.setRootContext(new RSMTreeParentNode(Children.create(RSMChildFactory.getChildFactory(parent), true), Lookups.singleton(parent), null));
     
        associateLookup(ExplorerUtils.createLookup(mgr, getActionMap())); 
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        explorerScrollPane = new BeanTreeView();
        searchPanel = new fr.proline.studio.gui.SearchPanel();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(explorerScrollPane)
            .addComponent(searchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(searchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(explorerScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane explorerScrollPane;
    private fr.proline.studio.gui.SearchPanel searchPanel;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
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

    @Override
    public ExplorerManager getExplorerManager() {
        return mgr;
    }
    
}
