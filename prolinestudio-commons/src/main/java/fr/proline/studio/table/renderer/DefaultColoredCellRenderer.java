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
package fr.proline.studio.table.renderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * This renderer extends DefaultTableCellRenderer to color JLabel
 */
public class DefaultColoredCellRenderer extends DefaultTableCellRenderer {


    public static DefaultColoredCellRenderer disabledCellRendered = new DefaultColoredCellRenderer(UIManager.getColor ( "Label.background" ),  UIManager.getColor ( "Label.disabledForeground" ));
    private Color m_backColor;
    private Color m_fontColor;

    public DefaultColoredCellRenderer(Color backgroundColor, Color fontColor) {
        m_backColor = backgroundColor;
        m_fontColor = fontColor;
    }


    public DefaultColoredCellRenderer(Color backgroundColor) {
        m_backColor = backgroundColor;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String displayValue = value.toString();
        if(value instanceof Float && ((Float)value).isNaN()){
            displayValue="";
        }
        JLabel l = (JLabel) super.getTableCellRendererComponent(table, displayValue, isSelected, hasFocus, row, column);
        if(m_fontColor != null)
            l.setForeground(m_fontColor);
        l.setBackground(m_backColor);
        return l;
    }


}
