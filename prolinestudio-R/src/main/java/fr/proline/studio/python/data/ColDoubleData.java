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
import java.util.ArrayList;

/**
 * Python object corresponding to the column of type Double/Float of a Table Model
 * 
 * @author JM235353
 */
public class ColDoubleData extends Col {
    
    private final ArrayList<Double> m_data;
    
    public ColDoubleData(Table table, ArrayList<Double> data, String name) {
        super(table);
        m_data = data;
        m_columnName = name;
    }
    
    public ColDoubleData(Table table, double[] data, String name) {
        super(table);
        m_data = new ArrayList<>(data.length);
        for(double d : data) {
            m_data.add(d);
        }
        m_columnName = name;
    }

    private ColDoubleData() {
        super(null);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getValueAt(int row) {
        return m_data.get(row);
    }
    
    @Override
    public void setValuetAt(int row, Object o) {
        Number n = Conversion.convertToJavaNumber(o);
        if (n == null) {
            m_data.set(row, null);
        } else {
            m_data.set(row, Double.valueOf(n.doubleValue()));
        }
        
        
    }

    @Override
    public Col mutable() {
        return this;
    }
    
    @Override
    public int getRowCount() {
        return m_data.size();
    }
    
    @Override
    public Class getColumnClass() {
        return Double.class;
    }

    
}
