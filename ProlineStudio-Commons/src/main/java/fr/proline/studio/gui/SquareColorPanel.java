package fr.proline.studio.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 * Panel with a square painted.
 * @author JM235353
 */
public class SquareColorPanel extends JPanel {

    private Dimension m_dim;
    private Color m_color = Color.red; // default color

    public SquareColorPanel(int size) {
        m_dim = new Dimension(size, size);
    }

    public void setColor(Color c) {
        this.m_color = c;
    }

    @Override
    public Dimension getPreferredSize() {
        return m_dim;
    }

    @Override
    public void paint(Graphics g) {
        if (m_color == null) {
            // blank panel
            return;
        }
        
        super.paint(g);

        Graphics2D g2 = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();

        // we want a square... (most of the time height is greater than width)
        int delta = 0;
        if (height > m_dim.height) {
            delta = (height - m_dim.height) / 2;
            if (delta < 0) {
                delta = 0;
            }
        }


        g2.setColor(m_color);
        g2.fillRect(0, delta, width - 1, height - 1 - delta);
        g2.setColor(Color.black);
        g2.drawRect(0, delta, width - 1, height - 1 - delta);

    }
}