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
package fr.proline.studio.graphics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

/**
 *
 * @author CB205360
 */
public class PlotPanel extends JPanel {
   
   private BasePlotPanel m_basePlotPanel;
   private JLayeredPane m_layeredPane;
   
   public PlotPanel(boolean isDoubleYAxis) {

      m_basePlotPanel = new BasePlotPanel();

      m_layeredPane = new JLayeredPane();
      setLayout(new BorderLayout());
      m_layeredPane.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();

                m_basePlotPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
                m_layeredPane.revalidate();
                m_layeredPane.repaint();

            }
        });
        add(m_layeredPane, BorderLayout.CENTER);

        m_layeredPane.add(m_basePlotPanel, JLayeredPane.DEFAULT_LAYER);
        m_layeredPane.add(m_basePlotPanel.getXAxis().getRangePanel(), JLayeredPane.PALETTE_LAYER);
        m_layeredPane.add(m_basePlotPanel.getYAxis().getRangePanel(), JLayeredPane.PALETTE_LAYER);
   }
   
   public BasePlotPanel getBasePlotPanel() {
      return m_basePlotPanel;
   }
 
}
