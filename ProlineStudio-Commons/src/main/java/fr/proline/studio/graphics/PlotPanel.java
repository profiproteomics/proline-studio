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
   
   public PlotPanel() {
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
