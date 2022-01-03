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

package fr.proline.studio.dock.gui;

import fr.proline.studio.dock.AbstractTopPanel;
import fr.proline.studio.dock.container.DockComponent;
import fr.proline.studio.dock.container.DockComponentListener;
import fr.proline.studio.dock.container.DockContainer;
import fr.proline.studio.dock.container.DockContainerTab;
import fr.proline.studio.dock.dragdrop.DockingExportTransferHandler;
import fr.proline.studio.utils.IconManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class TabbedPaneLabel extends JPanel implements DockComponentListener {

    final DockContainerTab m_dockContainerTab;
    final DockContainer m_dockComponent;

    private DockingExportTransferHandler m_transferHandler;
    private MouseEvent m_mouseBegin;
    private boolean m_exportAsDrag = false;

    private JLabel m_titleLabel;
    private JButton m_closeButton = null;
    private JButton m_minimizeButton = null;


    public TabbedPaneLabel(DockContainerTab dockContainerTab, DockComponent dockComponent) {
        m_dockContainerTab = dockContainerTab;
        m_dockComponent = dockComponent;
        dockComponent.setDockComponentListener(this);

        initComponents(dockComponent);


        m_transferHandler = new DockingExportTransferHandler(dockContainerTab, dockComponent);
        setTransferHandler(m_transferHandler);


    }

    private void initComponents(DockComponent content) {

        setLayout(new FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        setOpaque(false);


        m_titleLabel = new JLabel(content.getTitle());
        m_titleLabel.setIcon(content.getIcon());
        m_titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 4));

        m_titleLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
            }
            public void mousePressed(MouseEvent evt) {
                m_mouseBegin = evt;
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                m_mouseBegin = null;

                if (evt.isPopupTrigger()) {
                    popup(evt.getX(), evt.getY());
                } else if (!m_exportAsDrag) {
                    ((JTabbedPane) m_dockContainerTab.getComponent()).setSelectedComponent(m_dockComponent.getComponent());
                }

                m_exportAsDrag = false;
            }
        });

        m_titleLabel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                mouseDraggedImpl(evt);
            }
        });

        add(m_titleLabel);


        if (content.canClose()) {
            m_closeButton = new JButton();
            m_closeButton.setIcon(IconManager.getIcon(IconManager.IconType.CROSS_SMALL12));
            m_closeButton.setBorderPainted(false);
            m_closeButton.setContentAreaFilled(false);
            m_closeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
            m_closeButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    if (m_dockComponent.getComponent() instanceof AbstractTopPanel) {
                            if(((AbstractTopPanel) m_dockComponent.getComponent()).warnBeforeClosing()) {
                                int closeTab = JOptionPane.showConfirmDialog(null, "Are you sure tou want to close this Windows ? " + ((AbstractTopPanel) m_dockComponent.getComponent()).getWarnClosingMessage(), "Close Warning",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                                if (closeTab == JOptionPane.NO_OPTION)
                                    return;
                            }
                    }
                    m_dockContainerTab.remove(m_dockComponent);
                    if (m_dockComponent.getComponent() instanceof AbstractTopPanel) {
                        ((AbstractTopPanel) m_dockComponent.getComponent()).componentClosed();
                    }
                }
            });
            add(m_closeButton);
        }

        if (content.canMinimize()) {
            m_minimizeButton = new JButton();
            m_minimizeButton.setIcon(IconManager.getIcon(IconManager.IconType.MINIFY));
            m_minimizeButton.setBorderPainted(false);
            m_minimizeButton.setContentAreaFilled(false);
            m_minimizeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
            m_minimizeButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {

                    m_dockContainerTab.remove(m_dockComponent);
                    m_dockContainerTab.minimize(m_dockComponent);


                }
            });
            add(m_minimizeButton);
        }

    }



    private void mouseDraggedImpl(MouseEvent evt) {

        if (m_mouseBegin != null) {

            int dx = m_mouseBegin.getX() - evt.getX();
            int dy = m_mouseBegin.getY() - evt.getY();
            if ((dx * dx + dy * dy) > 16) {

                JComponent component = m_dockComponent.getComponent();
                JTabbedPane tabbedPane = (JTabbedPane) m_dockContainerTab.getComponent();
                int index = tabbedPane.indexOfComponent(component);

                tabbedPane.setSelectedIndex(index);

                m_transferHandler.exportAsDrag(this, m_mouseBegin, TransferHandler.MOVE);
                m_mouseBegin = null;
                m_exportAsDrag = true;
            }
        }

    }

    public void popup(int x, int y) {

        JComponent component = m_dockComponent.getComponent();
        if (component instanceof AbstractTopPanel) {
            Action[] actions = ((AbstractTopPanel) component).getActions((DockComponent) m_dockComponent);
            if (actions == null) {
                return;
            }

            JPopupMenu popup = new JPopupMenu();
            for (Action action : actions) {
                if (action == null) {
                    popup.addSeparator();
                } else {
                    popup.add(action);
                }
            }
            popup.show((JComponent) this, x, y);


        }


    }


    @Override
    public void titleChanged() {
        m_titleLabel.setText(m_dockComponent.getTitle());
        m_titleLabel.repaint();
    }
}
