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
package fr.proline.studio.table.renderer;

import fr.proline.studio.table.DataGroup;
import fr.proline.studio.table.DataGroup.GroupObject;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer used for the Properties Table with GroupObject and DataGroup
 * @author JM235353
 */
public class PropertiesRenderer extends DefaultTableCellRenderer {

    private final boolean m_nonOkRenderer;
    private static final Color COLOR_DIFFERENT = new Color(253, 241, 184);

    private JTable m_table = null;
    
    public PropertiesRenderer(boolean nonOkRenderer) {
        m_nonOkRenderer = nonOkRenderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        GroupObject groupObject = (GroupObject) value;
        DataGroup dataGroup = (groupObject==null) ? null : groupObject.m_group;
        String valueString = (groupObject == null) ? "" : groupObject.m_valueRendering;
        
        super.getTableCellRendererComponent(table, valueString, isSelected, hasFocus, row, column);
        setHorizontalAlignment(JLabel.RIGHT);

        m_table = table;
        
        if (!isSelected) {
            if (m_nonOkRenderer) {
                setBackground(COLOR_DIFFERENT);
                setForeground(Color.black);
            } else if (column == DataGroup.COLTYPE_GROUP_NAME) {

                if (groupObject.m_coloredRow) {
                    setBackground(dataGroup.getGroupColor(row));
                    setForeground(Color.white);
                } else {
                    setBackground(javax.swing.UIManager.getColor("Table.background"));
                    setForeground(javax.swing.UIManager.getColor("Table.foreground"));
                }
            } else {
                setBackground(javax.swing.UIManager.getColor("Table.background"));
                setForeground(javax.swing.UIManager.getColor("Table.foreground"));
            }
        } else {
            setBackground(javax.swing.UIManager.getColor("Table.selectionBackground"));
            setForeground(javax.swing.UIManager.getColor("Table.selectionForeground"));
        }

        return this;

    }
    
    public JTable getTable() {
        //JPM.WART
        return m_table;
    }

}
