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
import fr.proline.studio.msfiles.MsFilesExplorer;
import fr.proline.studio.msfiles.WorkingSetView;
import fr.proline.studio.rsmexplorer.gui.TreeUtils;
import java.awt.BorderLayout;
import javax.swing.JTabbedPane;

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

    private static MsFilesExplorer m_explorer;
    private JTabbedPane m_tabbedPane;

    public MzdbFilesTopComponent() {
        initComponents();
        setName(Bundle.CTL_MzdbFilesTopComponent());
        setToolTipText(Bundle.HINT_MzdbFilesTopComponent());

    }

    private void initComponents() {

        setLayout(new BorderLayout());
        m_tabbedPane = new JTabbedPane();

        m_explorer = new MsFilesExplorer();
        m_tabbedPane.add("Explorer", m_explorer);

        m_tabbedPane.add("Working Sets", WorkingSetView.getWorkingSetView());
        add(m_tabbedPane, BorderLayout.CENTER);

        if (!ServerConnectionManager.getServerConnectionManager().isNotConnected()) {
                m_explorer.getTreeFileChooserPanel().initTree();
                m_explorer.getTreeFileChooserPanel().restoreTree(TreeUtils.TreeType.SERVER);
        }

    }

    public static MsFilesExplorer getExplorer() {
        return m_explorer;
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
