package fr.proline.studio.rsmexplorer;

import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import fr.proline.studio.rsmexplorer.gui.TreeFileChooserPanel;
import fr.proline.studio.rsmexplorer.gui.TreeFileChooserTransferHandler;
import fr.proline.studio.rsmexplorer.gui.LocalFileSystemTransferHandler;
import fr.proline.studio.rsmexplorer.gui.LocalFileSystemView;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;

import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component to dispay the mzdb Files Panel
 */
@ConvertAsProperties(dtd = "-//fr.proline.studio.rsmexplorer//MzdbFiles//EN",
        autostore = false)
@TopComponent.Description(preferredID = "MzdbFilesTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = true, position = 2)
@ActionID(category = "Window", id = "fr.proline.studio.rsmexplorer.MzdbFilesTopComponent")
@ActionReference(path = "Menu/Window", position = 20
)
@TopComponent.OpenActionRegistration(displayName = "#CTL_MzdbFilesAction",
        preferredID = "MzdbFilesTopComponent")
@Messages({
    "CTL_MzdbFilesAction=Ms Files",
    "CTL_MzdbFilesTopComponent=Ms Files",
    "HINT_MzdbFilesTopComponent=Ms Files"
})
public final class MzdbFilesTopComponent extends TopComponent {

    private static TreeFileChooserPanel m_tree;
    private TreeFileChooserTransferHandler m_transferHandler;
    private static LocalFileSystemView m_localFileSystemView;
    
    public MzdbFilesTopComponent() {
        initComponents();
        setName(Bundle.CTL_MzdbFilesTopComponent());
        setToolTipText(Bundle.HINT_MzdbFilesTopComponent());

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

        m_localFileSystemView = new LocalFileSystemView(new LocalFileSystemTransferHandler(), true);       
        add(m_localFileSystemView, c);

        c.gridy++;

        m_transferHandler = new TreeFileChooserTransferHandler();
        m_transferHandler.addComponent(m_localFileSystemView);
        
        m_tree = new TreeFileChooserPanel(ServerFileSystemView.getServerFileSystemView(), m_transferHandler, true);
        JScrollPane treeScrollPane = new JScrollPane();
        treeScrollPane.setViewportView(m_tree);
        treeScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Proline Server File System"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        add(treeScrollPane, c);

    }
    
    public static TreeFileChooserPanel getTreeFileChooserPanel(){
        return m_tree;
    }
    
    public static LocalFileSystemView getLocalFileSystemView(){
        return m_localFileSystemView;
    }

    @Override
    public void componentOpened() {

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
