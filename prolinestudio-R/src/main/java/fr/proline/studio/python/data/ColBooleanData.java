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

import java.util.ArrayList;

/**
 * Python object corresponding to the column of type Boolean of a Table Model
 * 
 * @author JM235353
 */
public class ColBooleanData extends Col {
    
    private final ArrayList<Boolean> m_data;
    
    public ColBooleanData(Table table, ArrayList<Boolean> data, String name) {
        super(table);
        m_data = data;
        m_columnName = name;
    }

    private ColBooleanData() {
        super(null);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getValueAt(int row) {
        return m_data.get(row);
    }
    
    @Override
    public void setValuetAt(int row, Object o) {
        Boolean b = (Boolean) o;
        m_data.set(row, b);

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
        return Boolean.class;
    }
    



    
    
}
