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
package fr.proline.studio.msfiles;

import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import fr.proline.studio.rsmexplorer.gui.LocalFileSystemTransferHandler;
import fr.proline.studio.rsmexplorer.gui.LocalFileSystemView;
import fr.proline.studio.rsmexplorer.gui.TreeFileChooserPanel;
import fr.proline.studio.rsmexplorer.gui.TreeFileChooserTransferHandler;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author AK249877
 */
public class MsFilesExplorer extends JPanel {

    private final TreeFileChooserPanel m_tree;
    private final TreeFileChooserTransferHandler m_transferHandler;
    private final LocalFileSystemView m_localFileSystemView;

    public MsFilesExplorer() {
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

    public TreeFileChooserPanel getTreeFileChooserPanel() {
        return m_tree;
    }

    public LocalFileSystemView getLocalFileSystemView() {
        return m_localFileSystemView;
    }

}
