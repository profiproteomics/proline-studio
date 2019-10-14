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
package fr.proline.studio.python.data;

import fr.proline.studio.python.util.Conversion;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import java.util.ArrayList;
import org.python.core.Py;

/**
 * Python object corresponding to the reference to a column of a Table Model
 * 
 * @author JM235353
 */
public class ColRef extends Col {
    
    private final int m_modelCol;
    private final GlobalTableModelInterface m_tableModel;
    
    public ColRef(Table table, int col, CompoundTableModel model) {
        super(table);
        m_modelCol = col;
        m_tableModel = model.getLastNonFilterModel();
    }
    
    public ColRef(Table table, int col, GlobalTableModelInterface model) {
        super(table);
        m_modelCol = col;
        m_tableModel = model;
    }
    
    public int getModelCol() {
        return m_modelCol;
    }
    
    @Override
    public Col mutable() {
        int nb = __len__();
        ArrayList<Double> resultArray = new ArrayList<>(nb);
        for (int i = 0; i < nb; i++) {
            Number v = Conversion.convertToJavaNumber(getValueAt(i));
            if (v ==  null) {
                 resultArray.add(null);
            } else {
                 resultArray.add(v.doubleValue());
            }
        }
        return new ColDoubleData(m_table, resultArray, getColumnName());
    }
    
    @Override
    public Object getValueAt(int row) {
        Object o =  m_tableModel.getValueAt(row, m_modelCol);
        if (o instanceof LazyData) {
            o = ((LazyData) o).getData();
        }
        return o;
    }

    @Override
    public int getRowCount() {
        return m_tableModel.getRowCount();
    }

    @Override
    public void setValuetAt(int row, Object o) {
        throw Py.TypeError("Tried to modify constant col");
    }

        
    @Override
    public String getColumnName() {
        if ((m_columnName == null) || (m_columnName.isEmpty())) {
           return m_tableModel.getColumnName(m_modelCol); 
        }
        return m_columnName;
    }
    
    @Override
    public String getExportColumnName() {
        if ((m_columnName == null) || (m_columnName.isEmpty())) {
           return m_tableModel.getExportColumnName(m_modelCol); 
        }
        return m_columnName;
    }

    @Override
    public Class getColumnClass() {
        return m_tableModel.getColumnClass(m_modelCol);
    }


}
