package fr.proline.studio.dock.gui;

import fr.proline.studio.dock.container.DockComponent;
import fr.proline.studio.dock.container.DockContainer;
import fr.proline.studio.dock.container.DockContainerTab;
import fr.proline.studio.dock.dragdrop.DockingExportTransferHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class TabbedPaneLabel extends JPanel
{
    final DockContainerTab m_dockContainerTab;
    final DockContainer m_dockComponent;

    private DockingExportTransferHandler m_transferHandler;
    private MouseEvent m_mouseBegin;

    private JButton m_closeButton = null;
    private JButton m_minimizeButton = null;

    public TabbedPaneLabel(DockContainerTab dockContainerTab, DockComponent dockComponent) {
        m_dockContainerTab = dockContainerTab;
        m_dockComponent = dockComponent;

        initComponents(dockComponent);


        m_transferHandler = new DockingExportTransferHandler(dockContainerTab, dockComponent);
        setTransferHandler(m_transferHandler);


    }

    private void initComponents(DockComponent content) {

        setLayout(new FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        setOpaque(false);


        JLabel titleLabel = new JLabel(content.getTitle());
        titleLabel.setIcon(content.getIcon());
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 4));

        titleLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ((JTabbedPane) m_dockContainerTab.getComponent()).setSelectedComponent(m_dockComponent.getComponent());
            }
            public void mousePressed(MouseEvent evt) {
                m_mouseBegin = evt;
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                m_mouseBegin = null;
            }
        });

        titleLabel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                mouseDraggedImpl(evt);
            }
        });

        add(titleLabel);


        if (content.canClose()) {
            m_closeButton = new JButton("x");

            //bn_close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/close.png"))); // NOI18N
            m_closeButton.setBorderPainted(false);
            m_closeButton.setContentAreaFilled(false);
            m_closeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
            m_closeButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    m_dockContainerTab.remove(m_dockComponent);
                }
            });
            add(m_closeButton);
        }

        if (content.canMinimize()) {
            m_minimizeButton = new JButton("_");

            //bn_close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/close.png"))); // NOI18N
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

/*
        if (content.getFloatting()) {
            bn_float = new javax.swing.JButton();
            bn_float.setText("f");
            //bn_float.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/float.png"))); // NOI18N
            bn_float.setBorderPainted(false);
            bn_float.setContentAreaFilled(false);
            bn_float.setMargin(new java.awt.Insets(0, 0, 0, 0));
            bn_float.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    bn_floatActionPerformed(evt);
                }
            });
            add(bn_float);
        }

        if (content.getMaximize()) {
            bn_maximize = new javax.swing.JButton();
            bn_maximize.setText("M");
            //bn_maximize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/maximize.png"))); // NOI18N
            bn_maximize.setBorderPainted(false);
            bn_maximize.setContentAreaFilled(false);
            bn_maximize.setMargin(new java.awt.Insets(0, 0, 0, 0));
            bn_maximize.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    bn_maximizeActionPerformed(evt);
                }
            });
            add(bn_maximize);
        }

        if (content.getMinimize()) {
            bn_minimize = new javax.swing.JButton();
            bn_minimize.setText("m");
            //bn_minimize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/minimize.png"))); // NOI18N
            bn_minimize.setBorderPainted(false);
            bn_minimize.setContentAreaFilled(false);
            bn_minimize.setMargin(new java.awt.Insets(0, 0, 0, 0));
            bn_minimize.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    bn_minimizeActionPerformed(evt);
                }
            });
            add(bn_minimize);
        }

        if (content.getClose()) {
            bn_close = new javax.swing.JButton();
            bn_close.setText("x");
            //bn_close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/close.png"))); // NOI18N
            bn_close.setBorderPainted(false);
            bn_close.setContentAreaFilled(false);
            bn_close.setMargin(new java.awt.Insets(0, 0, 0, 0));
            bn_close.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    bn_closeActionPerformed(evt);
                }
            });
            add(bn_close);
        }*/
    }

    /*
    private void bn_floatActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_bn_floatActionPerformed
    {//GEN-HEADEREND:event_bn_floatActionPerformed
        tabPanel.floatTab(content);
    }//GEN-LAST:event_bn_floatActionPerformed

    private void bn_maximizeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_bn_maximizeActionPerformed
    {//GEN-HEADEREND:event_bn_maximizeActionPerformed
        tabPanel.maximizeTab(content);
    }//GEN-LAST:event_bn_maximizeActionPerformed

    private void bn_minimizeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_bn_minimizeActionPerformed
    {//GEN-HEADEREND:event_bn_minimizeActionPerformed
        tabPanel.minimizeTab(content);
    }//GEN-LAST:event_bn_minimizeActionPerformed

*/


    private void mouseDraggedImpl(MouseEvent evt) {

        if (m_mouseBegin != null) {

            int dx = m_mouseBegin.getX() - evt.getX();
            int dy = m_mouseBegin.getY() - evt.getY();
            if ((dx * dx + dy * dy) > 16) {
                m_transferHandler.exportAsDrag(this, m_mouseBegin, TransferHandler.MOVE);
                m_mouseBegin = null;
            }
        }

    }



   /* private javax.swing.JButton bn_close;
    private javax.swing.JButton bn_float;
    private javax.swing.JButton bn_maximize;
    private javax.swing.JButton bn_minimize;*/



}
