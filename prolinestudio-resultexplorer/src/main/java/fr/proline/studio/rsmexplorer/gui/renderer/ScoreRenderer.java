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

import fr.proline.studio.utils.DataFormat;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.swingx.util.PaintUtils;

/**
 *
 * @author JM235353
 */
public class ScoreRenderer extends DefaultTableCellRenderer {

    
    private int m_digits = 2;
    private boolean m_scientific = false;
    private boolean m_showNaN = false;
    
    private Float m_value = 0f;
    
    private float m_maxValue = 100f;
    
    private static Color SCORE_COLOR = null;
    
    public ScoreRenderer(int digits, boolean scientific, boolean showNaN) {
        m_digits = digits;
        m_scientific = scientific;
        m_showNaN = showNaN;
    }
    
    public ScoreRenderer(int digits) {
        m_digits = digits;
    }
    
    public ScoreRenderer() {
    }
    
    public void setMaxValue(float maxValue) {
        m_maxValue = maxValue;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Float f = (Float) value;
        m_value = f;
        

        String formatedValue;
        
        if ((f == null) || (f.isNaN())) {
            m_value = 0f;
            if (m_showNaN) {
                formatedValue = "NaN";
            } else {
                formatedValue = "";
            }
        } else if (m_scientific) {
            double dAbs = Math.abs(f.floatValue());
            if ((dAbs!=0) && (dAbs*Math.pow(10, m_digits-1)>=1)) {
                formatedValue = DataFormat.format(f.floatValue(), m_digits);
            } else {
                int digits = m_digits - 2;
                if (digits < 2) {
                    digits = 2;
                }
                formatedValue = DataFormat.formatScientific(f.floatValue(), digits);
            }
        } else {
            formatedValue = DataFormat.format(f.floatValue(), m_digits);
        }

        
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, f, isSelected, hasFocus, row, column);
        l.setHorizontalAlignment(JLabel.RIGHT);
        l.setText(formatedValue);
        
        return l;

    }
    
    @Override
    public void paint(Graphics g) {
        setOpaque(true);
        super.paint(g);
        
        double length = m_value/m_maxValue;
        if (length>1) {
            length = 1;
        }
        int maxLength = getWidth()-2;
        int scoreWidth = (int) Math.round(length*maxLength);
        
        int start = 1+ maxLength-scoreWidth;
        
        if (SCORE_COLOR == null) {
            Color c = PaintUtils.setSaturation(Color.GREEN, .7f);
            SCORE_COLOR = PaintUtils.setAlpha(c, 125);
        }

        
        g.setColor(SCORE_COLOR);
        g.fillRect(start, 1, scoreWidth, getHeight()-2);
        
        
        setOpaque(false);
        super.paint(g); // repaint text only
    }
}
