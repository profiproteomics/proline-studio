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
package fr.proline.studio.graphics.parallelcoordinates;

/**
 * Encapsulation of values of type String to be displayed in ParallelCoordinates
 * 
 * @author JM235353
 */
public class StringValue extends AbstractValue {
    
    private String m_s;
    
    public StringValue(String s, int rowIndex) {
        super(ValueTypeEnum.STRING, rowIndex);
        
        m_s = s;
    }
    
    @Override
    public String toString() {
        return m_s;
    }

    @Override
    public int compareTo(AbstractValue o) {
        return m_s.compareTo(((StringValue) o).m_s);
    }
}
