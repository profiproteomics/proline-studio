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
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.ExtractionResult.Status;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.utils.IconManager;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer for extraction status: show a green tick for DONE, a hour glass for REQUESTED, nothing otherwise (NONE)
 * @author MB243701
 */
public class StatusRenderer  extends DefaultTableCellRenderer {

    public StatusRenderer() {
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setText("");
        label.setHorizontalAlignment(JLabel.CENTER);
        
        if ((value == null) || (! (value instanceof Status))) {
            label.setIcon(null);
            return label;
        }
        
        
        Status st = (Status) value;
        
        if (st.equals(Status.DONE)) {
            label.setIcon(IconManager.getIcon(IconManager.IconType.TICK_SMALL));
        } else if (st.equals(Status.REQUESTED)) {
            label.setIcon(IconManager.getIcon(IconManager.IconType.HOUR_GLASS_MINI16));
        } else {
            label.setIcon(null);
        }
        
        return label;
        
    }

}
