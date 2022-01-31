/*
 * Copyright (C) 2019
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

    @Override
    public void componentOpened() {
        // nothing to do
    }

    @Override
    public void componentClosed() {
        //  nothing to do
    }
}
