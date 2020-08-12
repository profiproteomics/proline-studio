package fr.proline.studio.rsmexplorer;

import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;

import javax.swing.*;
import java.awt.*;

public class RSMExplorerTopPanel extends JPanel {

    public RSMExplorerTopPanel() {
        initComponents();
        setName("Projects");
        setToolTipText("Projects");

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
}
