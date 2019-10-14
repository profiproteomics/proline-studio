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
import fr.proline.studio.export.ExportTextInterface;
import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer for booleans : show a green tick for TRUE, show nothing for FALSE
 * @author JM235353
 */
public class BooleanRenderer extends DefaultTableCellRenderer /*implements ExportTextInterface*/ {

    public BooleanRenderer() {
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setText("");
        label.setHorizontalAlignment(JLabel.CENTER);
        
        if ((value == null) || (! (value instanceof Boolean))) {
            label.setIcon(null);
            return label;
        }
        
        
        Boolean b = (Boolean) value;
        
        if (b.booleanValue()) {
            label.setIcon(IconManager.getIcon(IconManager.IconType.TICK_SMALL));
        } else {
            label.setIcon(null);
        }
        
        return label;
        
    }

    
}
