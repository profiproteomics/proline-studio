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
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.Serializable;

/**
 * This renderer encapsulates another renderer and force to display the text to the left
 * @author JM235353
 */
public class DefaultAlignRenderer implements TableCellRenderer, Serializable {

    private TableCellRenderer m_renderer;
    private int m_textalignment;

    public DefaultAlignRenderer(TableCellRenderer renderer, int textalignmentLabel) {
        m_renderer = renderer;
        if(textalignmentLabel != JLabel.RIGHT && textalignmentLabel != JLabel.LEFT && textalignmentLabel != JLabel.CENTER)
            throw new IllegalArgumentException("Invalid text alignment specified ");

        m_textalignment= textalignmentLabel;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = m_renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (c instanceof JLabel) {
            ((JLabel)c).setHorizontalAlignment(m_textalignment);
        }
        return c;
    }
    
}
