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
package fr.proline.studio.pattern;

/**
 * Definition of a parameter exhanged between databoxes
 * @author JM235353
 */
public class DataParameter {

    private Class m_c;
    private boolean m_isList;
    private boolean m_isCompulsory;

    
    public DataParameter(Class c, boolean isList) {
        this(c, isList, true);
    }
    public DataParameter(Class c, boolean isList, boolean isCompulsory) {
        m_c = c;
        m_isList = isList;
        m_isCompulsory = isCompulsory;
    }

    public Class getParameterClass() {
        return m_c;
    }
    
    public boolean getParameterIsList() {
        return m_isList;
    }
    
    @Override
    public boolean equals(Object p) {
        if (p instanceof DataParameter) {
            return m_c.equals(((DataParameter)p).m_c);
        }
        return false;
    }
    
    public boolean equalsData(Class dataC) {
        return m_c.equals(dataC);
    }

    public boolean isCompatibleWithOutParameter(DataParameter outParameter) {
        if (!m_c.equals(outParameter.m_c)) {
            return false;
        }
        if (m_isList && !outParameter.m_isList) {
            return false;
        }
        return true;

    }
    
    public boolean isCompulsory() {
        return m_isCompulsory;
    }
    
    @Override
    public int hashCode() {
        return m_c.hashCode();
    }
    
}