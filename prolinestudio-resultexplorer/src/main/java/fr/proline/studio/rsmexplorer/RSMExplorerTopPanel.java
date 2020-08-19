package fr.proline.studio.rsmexplorer;

import fr.proline.studio.dock.AbstractTopPanel;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;

import javax.swing.*;
import java.awt.*;

public class RSMExplorerTopPanel extends AbstractTopPanel {

    private static RSMExplorerTopPanel m_singleton = null;

    private RSMExplorerTopPanel() {
        initComponents();
        setName("Projects");
        setToolTipText("Projects");

    }

    public static RSMExplorerTopPanel getSingleton() {
        if (m_singleton == null) {
            m_singleton = new RSMExplorerTopPanel();
        }

        return m_singleton;
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
    public String getTopPanelIdentifierKey() {
        return "PROJECTS_TOP_PANEL";
    }

    @Override
    public Image getIcon() {
        return null;
    }

    @Override
    public String getTitle() {
        return getName();
    }
}
