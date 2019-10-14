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
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer to display a float as a percentage
 * @author JM235353
 */
public class PercentageRenderer implements TableCellRenderer {
    
    private TableCellRenderer m_defaultRenderer;
    
    public PercentageRenderer(TableCellRenderer defaultRenderer) {
        m_defaultRenderer = defaultRenderer;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        ;
        String formatedValue;
        if (value == null) {
            formatedValue = "";
        } else if (value instanceof Float) {
            Float f = (Float) value;
            formatedValue = (f.isNaN()) ? "" : DataFormat.format(f.floatValue(), 2)+" %";
            
        } else {
            Double d = (Double) value;
            Float f = d.floatValue();
            formatedValue =  (f.isNaN()) ? "" : DataFormat.format(f.floatValue(), 2)+" %";
        }
        
        

        return m_defaultRenderer.getTableCellRendererComponent(table, formatedValue, isSelected, hasFocus, row, column);

    }
}

