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

import java.awt.Component;
import java.awt.Font;
import java.io.Serializable;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * This renderer encapsulates another renderer so it can be grayed. In fact to
 * gray it is not possible due to optimizations in JTable. So the text font is
 * modified to italic
 *
 * @author JM235353
 */
public class GrayedRenderer implements TableCellRenderer, Serializable {

    private final TableCellRenderer m_renderer;
    private boolean innerRenderIsGrayable = false;

    public GrayedRenderer(TableCellRenderer renderer) {
        m_renderer = renderer;
        innerRenderIsGrayable = (m_renderer instanceof GrayableTableCellRenderer);
        if (innerRenderIsGrayable) {
            ((GrayableTableCellRenderer) m_renderer).setGrayed(true);
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = m_renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (!innerRenderIsGrayable) {
            c.setFont(c.getFont().deriveFont(Font.ITALIC));
            // does not work c.setForeground(Color.lightGray);
        }
        return c;
    }

    public TableCellRenderer getBaseRenderer() {
        return m_renderer;
    }

}
