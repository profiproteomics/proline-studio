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
package fr.proline.studio.graphics.core;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.ExtendableButtonPanel;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 *
 * @author JM235353
 */
public abstract class GraphicsToolbarPanel extends HourglassPanel implements PlotToolbarListenerInterface {

    
    private JLayeredPane m_layeredPane;
    
    protected BasePlotPanel m_plotPanel;

    
    // Toolbar
    private JToolBar m_toolbar = null;

    
    // Generic buttons of the toolbar
    protected JToggleButton m_normalModeButton = null;
    protected JToggleButton m_selectionModeButton = null;
    protected JButton m_zoomButton = null;
    protected JToggleButton m_gridButton = null;

    protected JButton m_importSelectionButton = null; // should be moved
    protected JButton m_exportSelectionButton = null;
    
    protected boolean m_dataLocked = false;
    protected boolean m_isDoubleYAxis;
    
    public GraphicsToolbarPanel(boolean dataLocked, boolean isDoubleYAxis) {
        setLayout(new BorderLayout());

        m_dataLocked = dataLocked;
        m_isDoubleYAxis = isDoubleYAxis;
        
        
        
        m_layeredPane = new JLayeredPane();
        add(m_layeredPane, BorderLayout.CENTER);

        
        JPanel mainPanel = createMainPanel();
        m_layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);
        
        
        m_layeredPane.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                mainPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
                m_layeredPane.revalidate();
                m_layeredPane.repaint();

            }
        });

    }
    
    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JPanel internalPanel = initInternalPanel();
        mainPanel.add(internalPanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        mainPanel.add(toolbar, BorderLayout.WEST);
        
        return mainPanel;
    }
    
    public void addToLayer(JPanel p) {
        m_layeredPane.add(p, JLayeredPane.PALETTE_LAYER);
    }


    private JPanel initInternalPanel() {
        JPanel p = createInternalPanel();
        return p;
    }
    private JToolBar initToolbar() {
        m_toolbar = new JToolBar(JToolBar.VERTICAL);
        m_toolbar.setFloatable(false);
        
        
        ButtonGroup buttonModeGroup = new ButtonGroup();
        
        m_normalModeButton = new JToggleButton(IconManager.getIcon(IconManager.IconType.MOUSE_POINTER));
        m_normalModeButton.setSelected(true);
        m_normalModeButton.setFocusPainted(false);
        m_normalModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_plotPanel.setMouseMode(BasePlotPanel.MOUSE_MODE.NORMAL_MODE);
            }
        });
        buttonModeGroup.add(m_normalModeButton);
        
        m_selectionModeButton = new JToggleButton(IconManager.getIcon(IconManager.IconType.MOUSE_SELECT));
        m_selectionModeButton.setSelected(false);
        m_selectionModeButton.setFocusPainted(false);
        m_selectionModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_plotPanel.setMouseMode(BasePlotPanel.MOUSE_MODE.SELECTION_MODE);
            }
        });
        buttonModeGroup.add(m_selectionModeButton);
        
        // Zoom+ / Zoom- / View all
        ActionListener zoomInAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_plotPanel.zoomIn();
            }
        };
        ActionListener zoomOutAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_plotPanel.zoomOut();
            }
        };
        ActionListener viewAllAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_plotPanel.viewAll();
            }
        };
        
        m_zoomButton = new JButton(IconManager.getIcon(IconManager.IconType.ZOOM_IN));
        m_zoomButton.addActionListener(zoomInAction);
        
        JButton zoomInButton = new JButton(IconManager.getIcon(IconManager.IconType.ZOOM_IN));
        zoomInButton.addActionListener(zoomInAction);
        zoomInButton.setToolTipText("Zoom In");
        
        JButton zoomOutButton = new JButton(IconManager.getIcon(IconManager.IconType.ZOOM_OUT));
        zoomOutButton.addActionListener(zoomOutAction);
        zoomOutButton.setToolTipText("Zoom Out");
        
        JButton viewAlltButton = new JButton(IconManager.getIcon(IconManager.IconType.ZOOM_FIT));
        viewAlltButton.addActionListener(viewAllAction);
        viewAlltButton.setToolTipText("View all");
        
        ExtendableButtonPanel extendableButtonPanel = new ExtendableButtonPanel(m_zoomButton);
        addToLayer(extendableButtonPanel);
        extendableButtonPanel.registerButton(zoomInButton);
        extendableButtonPanel.registerButton(zoomOutButton);
        extendableButtonPanel.registerButton(viewAlltButton);
        
        
        // -- Grid Button
        m_gridButton = new JToggleButton(IconManager.getIcon(IconManager.IconType.GRID));
        m_gridButton.setSelected(true);
        m_gridButton.setFocusPainted(false);
        m_gridButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_plotPanel.displayGrid(m_gridButton.isSelected());
            }
        });
        
        

        
        
        m_toolbar.add(m_normalModeButton);
        m_toolbar.add(m_selectionModeButton);
        m_toolbar.addSeparator(); // ----
        m_toolbar.add(m_zoomButton);
        m_toolbar.add(m_gridButton);
        
        fillToolbar(m_toolbar);
        return m_toolbar;
    }
    
    protected abstract JPanel createInternalPanel();
    protected abstract void fillToolbar(JToolBar toolbar);
    
    
        
    public boolean isNormalMode() {
        return m_normalModeButton.isSelected();
    }
    
    public boolean isSelectionMode() {
        return m_selectionModeButton.isSelected();
    }

    
    @Override
    public void stateModified(BUTTONS b) {
        switch (b) {
            case GRID:
                if (!m_plotPanel.displayGrid()) {
                    m_gridButton.setSelected(false);
                }
                break;
        }

    }

    @Override
    public void enable(BUTTONS b, boolean v) {
        switch (b) {
            case GRID:
                m_gridButton.setEnabled(v);
                break;
            case IMPORT_SELECTION:
                m_importSelectionButton.setEnabled(v);
                break;
            case EXPORT_SELECTION:
                m_exportSelectionButton.setEnabled(v);
                break;
        }
    }
    


    
}
