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
package fr.proline.studio.table;

import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer for LazyData : 
 * - paint a glasshour when the wrapped data is not ready
 * - call another renderer when the wrapped data is ready according to its
 * type
 *
 * @author JM235353
 */
public class LazyTableCellRenderer extends DefaultTableCellRenderer {

    private TableCellRenderer m_childRenderer = null;
    
    public LazyTableCellRenderer() {
        setIcon(IconManager.getIcon(IconManager.IconType.HOUR_GLASS));
    }
    
    public LazyTableCellRenderer(TableCellRenderer childRenderer) {
        setIcon(IconManager.getIcon(IconManager.IconType.HOUR_GLASS));
        m_childRenderer = childRenderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        if (value == null) {
            return TableDefaultRendererManager.getDefaultRenderer(String.class).getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        }
        
        Object data = ((LazyData) value).getData();
        
        if ((m_childRenderer != null) && (data != null)) {
             return m_childRenderer.getTableCellRendererComponent(table, data, isSelected, hasFocus, row, column);
        }
        
        if (data == null) {
            super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);

            return this;
        } else {
            return table.getDefaultRenderer(data.getClass()).getTableCellRendererComponent(table, data, isSelected, hasFocus, row, column);
        }


    }
}
