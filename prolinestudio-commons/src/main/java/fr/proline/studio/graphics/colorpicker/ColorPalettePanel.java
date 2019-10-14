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
package fr.proline.studio.graphics.colorpicker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;

/**
 * Panel to display a color palette
 * @author jm235353
 */
public class ColorPalettePanel extends JPanel implements MouseListener {

    private final BasicStroke STROKE_1 = new BasicStroke(1);
    private final BasicStroke STROKE_3 = new BasicStroke(3);
    
    private int m_squareSize;
    private int m_delta;
    private int m_paletteWidth;

    private ColorSquare[] m_squares = null;

    private Color[] m_palette  = null;
    
    private ColorDataInterface m_colorDataInterface = null;
    
    public ColorPalettePanel(ColorDataInterface colorDataInterface, Color[] palette, int squareSize, int delta, int paletteWidth) {
        
        m_colorDataInterface = colorDataInterface;
        
        if (palette == null) {
            setPreferredSize(new Dimension(0, 0));
            return;
        }
        
        int paletteSize = palette.length;
        
        m_palette = palette;
        m_squareSize = squareSize;
        m_delta = delta;
        m_paletteWidth = paletteWidth;

        int paletteHeight = ((paletteSize - 1) / 9) + 1;

        int heightPixel = paletteHeight * (m_squareSize + m_delta) + m_delta;
        int widthPixel = paletteWidth * (m_squareSize + m_delta) + m_delta;
        setPreferredSize(new Dimension(widthPixel, heightPixel));

        m_squares = new ColorSquare[paletteSize];
        for (int i = 0; i < paletteSize; i++) {
            m_squares[i] = new ColorSquare(i);
        }

        addMouseListener(this);
    }

    @Override
    public void paint(Graphics g) {

        if (m_palette == null) {
            return;
        }

        int paletteSize = m_palette.length;
        int paletteWidth = 9;
        int paletteHeight = ((paletteSize - 1) / 9) + 1;

        g.setColor(Color.white);
        g.fillRect(0, 0, m_delta + paletteWidth * (m_delta + m_squareSize) - 1, m_delta + paletteHeight * (m_delta + m_squareSize) - 1);

        for (int i = 0; i < paletteSize; i++) {
            m_squares[i].paint(g);
        }

        g.setColor(Color.black);
        g.drawRect(0, 0, m_delta + paletteWidth * (m_delta + m_squareSize) - 1, m_delta + paletteHeight * (m_delta + m_squareSize) - 1);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int paletteSize = m_palette.length;
        for (int i = 0; i < paletteSize; i++) {
            if (m_squares[i].inside(e.getX(), e.getY())) {
                Color c = m_palette[i];
                m_colorDataInterface.propagateColorChanged(c.getRed(), c.getGreen(), c.getBlue());
                break;
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public class ColorSquare {

        private int m_index;

        public ColorSquare(int index) {
            m_index = index;
        }

        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            g2d.setColor(m_palette[m_index]);
            g2d.fillRect(getX(), getY(), m_squareSize, m_squareSize);
            g2d.setColor(Color.black);
            if (isSelected()) {
                g2d.setStroke(STROKE_3);
            } else {
                g2d.setStroke(STROKE_1);
            }
            g2d.drawRect(getX(), getY(), m_squareSize, m_squareSize);
            g2d.setStroke(STROKE_1);
        }

        private int getX() {
            int x = m_index % m_paletteWidth;
            return m_delta + x * (m_delta + m_squareSize);
        }

        private int getY() {
            int y = m_index / m_paletteWidth;
            return m_delta + y * (m_delta + m_squareSize);
        }

        private boolean isSelected() {
            Color c = m_palette[m_index];
            return ((m_colorDataInterface.getRed() == c.getRed()) && (m_colorDataInterface.getBlue() == c.getBlue()) && (m_colorDataInterface.getGreen() == c.getGreen()));
        }

        public boolean inside(int x, int y) {
            int xSquare = getX();
            int ySquare = getY();
            return (x >= xSquare) && (y >= ySquare) && (x <= xSquare + m_squareSize) && (y <= ySquare + m_squareSize);
        }

    }
}
