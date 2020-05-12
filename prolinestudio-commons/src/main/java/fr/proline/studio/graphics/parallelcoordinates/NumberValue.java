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
 * Encapsulation of values of type Number to be displayed in ParallelCoordinates
 * 
 * @author JM235353
 */
public class NumberValue extends AbstractValue {
    
    private boolean m_error = false;
    private Number m_n;
    
    public NumberValue(Number n, int rowIndex) {
        super(ValueTypeEnum.NUMBER, rowIndex);
        
        m_n = n;
    }
    
    public void setValue(Number n) {
        m_n = n;
    }
    
    @Override
    public String toString() {
        return m_n.toString();
    }
    
    public double doubleValue() {
        return m_n.doubleValue();
    }
    
    @Override
    public int compareTo(AbstractValue o) {
        
        double v1 = m_n.doubleValue();
        double v2 = ((NumberValue) o).m_n.doubleValue();
        if (Double.isNaN(v1)) {
            if (Double.isNaN(v2)) {
                return 0;
            } else {
                return -1;
            }
        } else if (Double.isNaN(v2)) {
            return 1;
        }
        
        double delta =  v1 - v2;
        if (delta >0) {
            return 1;
        } else if (delta < 0) {
            return -1;
        }
        return 0;
    }
    
    @Override
    public boolean isNan() {
        return Double.isNaN(doubleValue());
    }
    
    public void log() {
        m_n = Math.log10(m_n.doubleValue());
    }
    
    public boolean error() {
        if (isNan()) {
            return true;
        }
        return m_error;
    }
    
    public void setError(boolean b) {
        m_error = b;
    }
    
}
