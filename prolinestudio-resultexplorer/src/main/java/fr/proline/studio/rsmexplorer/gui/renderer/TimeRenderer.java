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
import fr.proline.studio.utils.StringUtils;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer to correctly format time as min sec
 * @author JM235353
 */
public class TimeRenderer implements TableCellRenderer  {
    
    private TableCellRenderer m_defaultRenderer;
    private int m_nbDigits = 2;
    
    public TimeRenderer(TableCellRenderer defaultRenderer) {
        m_defaultRenderer = defaultRenderer;
        m_nbDigits = 2;
    }
    
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null) {
            return m_defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        //long seconds = ((Float)value).longValue();
        if (((Float)value).isNaN()){
            return m_defaultRenderer.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        }
        float seconds = ((Float)value);
        return m_defaultRenderer.getTableCellRendererComponent(table, StringUtils.getTimeInMinutes(seconds, m_nbDigits), isSelected, hasFocus, row, column);

    }
}
