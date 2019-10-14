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

/**
 * LazyData can wrap any sort of data which will be loaded later.
 */
public class LazyData implements Comparable<LazyData>  {

    private Comparable m_data = null;

    public LazyData() {
    }

    public void setData(Comparable data) {
        m_data = data;
    }

    public Object getData() {
        return m_data;
    }

    @Override
    public int compareTo(LazyData o) {
        if (m_data == null) {
            if (o.m_data == null) {
                return 0;
            } else {
                return -1;
            }
        }
        if (o.m_data == null) {
            return 1;
        }

        if ((m_data instanceof String) ^ (o.m_data instanceof String)) {
            // can happen for a column with float but some data has no value
            // so it is returned ""
            if (o.m_data instanceof String) {
                return 1;
            } else {
                return -1;
            }
        }
        
        return m_data.compareTo(o.m_data);
    }
    
    @Override
    public String toString() {
        if (m_data == null) 
            return super.toString();
        return m_data.toString();
    }

}