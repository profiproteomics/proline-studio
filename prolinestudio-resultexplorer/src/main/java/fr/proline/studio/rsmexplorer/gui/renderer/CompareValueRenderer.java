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
package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.studio.export.ExportFontData;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import fr.proline.studio.table.renderer.GrayableTableCellRenderer;

/**
 * Class used to render a comparison between values in a JTable
 *
 * @author JM235353
 */
public class CompareValueRenderer implements GrayableTableCellRenderer {

    private CompareValuePanel m_valuePanel = null;

    private boolean m_grayed = false;
    
    
    private static Color STRIPPED_COLOR = UIManager.getColor("UIColorHighlighter.stripingBackground"); //JPM.WART

    public CompareValueRenderer(){
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Color selectionBackground = null;
        if (isSelected) {
            selectionBackground = table.getSelectionBackground();
        } else if (row % 2 == 1) {
            // JPMWART
            selectionBackground = STRIPPED_COLOR;
        }

        if (m_valuePanel == null) {
            m_valuePanel = new CompareValuePanel();
        }
        m_valuePanel.init((CompareValue) value, selectionBackground, m_grayed);
        return m_valuePanel;
    }

    @Override
    public void setGrayed(boolean v) {
        m_grayed = v;
    }


    public abstract static class CompareValue implements Comparable<CompareValue> {

        public abstract int getNumberColumns();

        public abstract Color getColor(int col);

        public abstract double getValue(int col);

        public abstract double getMaximumValue();

        public abstract double calculateComparableValue();
    }

    public static class CompareValuePanel extends JPanel {

        private static final int DELTA_X = 6;
        private static final int DELTA_Y = 2;
        private static final int DIM_Y = 20;

        private CompareValue m_v;
        private Color m_selectionBackground;
        private boolean m_grayed;
        

        public CompareValuePanel() {

        }

        public void init(CompareValue v, Color selectionBackground, boolean grayed) {
            m_v = v;
            m_selectionBackground = selectionBackground;
            m_grayed = grayed;
        }

        @Override
        public Dimension getPreferredSize() {
            int width = (m_v.getNumberColumns() + 2) * (DELTA_X + 2);
            return new Dimension(width, DIM_Y);
        }

        @Override
        public void paint(Graphics g) {

            if (m_selectionBackground != null) {
                g.setColor(m_selectionBackground);
                g.fillRect(0, 0, getWidth(), getHeight());
            }

            int nbCols = m_v.getNumberColumns();

            for (int i = 0; i < nbCols; i++) {
                
                if (m_grayed) {
                    Color c = m_v.getColor(i);
                    int gray = (int) (c.getRed() * 0.299 + c.getGreen() * 0.587 + c.getBlue() * 0.114);
                    Color newColor = new Color(gray, gray, gray).brighter();
                    g.setColor(newColor);
                } else {
                    g.setColor(m_v.getColor(i));
                }
                double maxValue = m_v.getMaximumValue();
                double value = m_v.getValue(i);
                int height = (int) Math.round((value / maxValue) * (DIM_Y - 2 * DELTA_Y));
                g.fillRect((i + 1) * (DELTA_X + 2), DIM_Y - height - DELTA_Y, DELTA_X, height);
                g.setColor(Color.gray);
                g.drawRect((i + 1) * (DELTA_X + 2), DIM_Y - height - DELTA_Y, DELTA_X, height);

            }

        }

    }

}
