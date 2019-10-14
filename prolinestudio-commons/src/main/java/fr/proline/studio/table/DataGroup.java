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

import fr.proline.studio.table.renderer.PropertiesRenderer;
import java.awt.Color;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Used by Properties Table Model. It breaks general table rendering convention.
 * Rendering does no longer depends of a type or of a column
 * @author JM235353
 */
public abstract class DataGroup {

    
    public static final String[] COLUMN_NAMES = {"Group", "Type"};
    
    public static final int COLTYPE_GROUP_NAME = 0;
    public static final int COLTYPE_PROPERTY_NAME = 1;
    
    

    private final String m_name;
    protected int m_rowStart;

    private PropertiesRenderer m_groupRenderer = null;
    private PropertiesRenderer m_groupSubRenderer = null;


    public DataGroup(String name, int rowStart) {
        m_name = name;
        m_rowStart = rowStart;
        
        m_groupRenderer = new PropertiesRenderer(false);
        m_groupSubRenderer = new PropertiesRenderer(false);
    }

    private GroupObject getName(int row) {
        if (row == m_rowStart) {
            GroupObject object = new GroupObject(m_name, this);
            object.setColoredRow();
            return object;
        }
        
        JTable table = m_groupRenderer.getTable(); //JPM.WART
        if ((table == null) || table.getRowSorter().getSortKeys().isEmpty()) {
            return new GroupObject("", m_name, this); // no sorting, for group name, we show no text
        }
        return new GroupObject(m_name, this);
    }

    public TableCellRenderer getRenderer(int row) {
        if (row == m_rowStart) {
            return m_groupRenderer;
        } else {
            return m_groupSubRenderer;
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == COLTYPE_GROUP_NAME) {
            return getName(rowIndex);
        }
        if (rowIndex == m_rowStart) {
            GroupObject object = new GroupObject("", this);
            object.setColoredRow();
            return object;
        }

        if (columnIndex == COLTYPE_PROPERTY_NAME) {
            return getGroupNameAt(rowIndex - m_rowStart - 1);
        }

        return getGroupValueAt(rowIndex - m_rowStart - 1, columnIndex - 2);
    }

    public boolean isFirstRow(int row) {
        return row == m_rowStart;
    }

    public abstract GroupObject getGroupValueAt(int rowIndex, int columnIndex);

    public abstract GroupObject getGroupNameAt(int rowIndex);

    public abstract Color getGroupColor(int row);

    public int getRowCount() {
        return getRowCountImpl() + 1;
    }

    public abstract int getRowCountImpl();
    
    
    public class GroupObject {
        
        public String m_valueRendering;
        public String m_valueFiltering;
        public DataGroup m_group;
        public boolean m_coloredRow = false;
        
        public GroupObject(String value, DataGroup group) {
            m_valueRendering = value;
            m_valueFiltering = value;
            m_group = group;
        }

        public void setColoredRow() {
            m_coloredRow = true;
        }
        
        public GroupObject(String valueRendering, String valueFiltering, DataGroup group) {
            m_valueRendering = valueRendering;
            m_valueFiltering = valueFiltering;
            m_group = group;
        }
        
        public String stringForFilter() {
            return m_valueFiltering;
        }
        public String stringForRendering() {
            return m_valueRendering;
        }
        
        @Override
        public String toString() {
            return m_valueRendering;
        }
    }
}
