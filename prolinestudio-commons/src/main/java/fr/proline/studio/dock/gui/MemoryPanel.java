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

package fr.proline.studio.dock.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MemoryPanel extends JPanel implements MouseListener {

    private static int NB_VALUES = 30;
    private final static Color BLUE = new Color(116, 208, 241);
    private final static Color MAX_COLOR = new Color(160, 7 , 26);

    private static final Dimension m_dimension = new Dimension(NB_VALUES*3,20);
    private static final StringBuilder m_sb = new StringBuilder();

    private ActionListener m_actionListener = null;

    private Font m_font;

    private long m_maxMemory;
    private long m_totalMemory;
    private long m_usedMemory;

    private long[] m_values = new long[NB_VALUES];
    private int valueCurIndex = 0;

    private boolean m_firstPaint = true;

    public MemoryPanel() {
        setMinimumSize(m_dimension);
        setPreferredSize(m_dimension);

        addMouseListener(this);

    }

    public void setActionListener(ActionListener actionListener) {
        m_actionListener = actionListener;

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void startAnimation() {
        Timer t = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                peekMemoryValue();
                repaint();
            }
        });
        t.start();
    }

    private void peekMemoryValue() {

        m_maxMemory = Runtime.getRuntime().maxMemory();
        m_totalMemory = Runtime.getRuntime().totalMemory();
        m_usedMemory = m_totalMemory - Runtime.getRuntime().freeMemory();

        m_values[valueCurIndex] = m_usedMemory;
        valueCurIndex++;
        if (valueCurIndex>=NB_VALUES) {
            valueCurIndex = 0;
        }

    }

    public void paint(Graphics g) {

        if (m_firstPaint) {
            startAnimation();
            m_firstPaint = false;
        }

        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());

        int maxMemoryHeight = (int) Math.round(getHeight() * 1); // * 0.8



        g.setColor(BLUE);
        int index = 0;
        for (int i = valueCurIndex; i<NB_VALUES;i++) {
            int height = (int) Math.round(maxMemoryHeight * ((double) m_values[i]) / ((double) m_totalMemory));
            g.fillRect(index*3, getHeight() - height, 3, height);
            index++;
        }
        for (int i=0; i<valueCurIndex; i++) {
            int height = (int) Math.round(maxMemoryHeight * ((double) m_values[i]) / ((double) m_totalMemory));
            g.fillRect(index*3, getHeight() - height, 3, height);
            index++;
        }

        /*
        g.setColor(MAX_COLOR);
        g.drawLine(0,(int) getHeight()-maxMemoryHeight, getWidth(), getHeight()-maxMemoryHeight);
*/

        g.setColor(Color.black);
        g.drawRect(0, 0, getWidth()-1, getHeight()-1);

        int memAllocated = (int) m_totalMemory/1024000;
        int memUsed = (int) m_usedMemory/1024000;

        if (m_font == null) {
            m_font = g.getFont().deriveFont(Font.BOLD);
        }
        g.setFont(m_font);

        m_sb.setLength(0);
        m_sb.append(memUsed).append('/').append(memAllocated).append("Mo");
        String memString = m_sb.toString();
        int stringWidth = g.getFontMetrics().stringWidth(memString);
        g.drawString(memString, (getWidth()-stringWidth)/2, getHeight()/2+5);
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if (m_actionListener != null) {
            m_actionListener.actionPerformed(null);
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
}
