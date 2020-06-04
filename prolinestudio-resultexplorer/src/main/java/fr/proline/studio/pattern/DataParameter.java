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
 * 
 * a DataParameter is defined by its type and subType.
 * subType can be null
 * 
 * Two DataParameters are compatible when type are subtypes are equals 
 * 
 * @author JM235353
 */
public class DataParameter {

    private Class m_type;
    private ParameterSubtypeEnum m_subtype;
    private boolean m_isCompulsory;

    
    public DataParameter(Class c, ParameterSubtypeEnum subtype) {
        this(c, subtype, true);
    }
    public DataParameter(Class c, ParameterSubtypeEnum subtype, boolean isCompulsory) {
        m_type = c;
        m_subtype = subtype;
        m_isCompulsory = isCompulsory;
    }

    public Class getParameterClass() {
        return m_type;
    }
    
    public ParameterSubtypeEnum getSubtype() {
        return m_subtype;
    }
    
    @Override
    public boolean equals(Object p) {
        if (p instanceof DataParameter) {
            return m_type.equals(((DataParameter)p).m_type);
        }
        return false;
    }
   
    public boolean equalsData(Class dataC, ParameterSubtypeEnum subtype) {
        return m_type.equals(dataC) && ((subtype==null) || (m_subtype==null) || (m_subtype.equals(subtype))); // subtype null corresponds to subtype not specified
    }

    public boolean isCompatibleWithOutParameter(DataParameter outParameter) {
        if (!m_type.equals(outParameter.m_type)) {
            return false;
        }
        if (m_subtype == null && outParameter.m_subtype == null) {
            return true;
        }
        if ((m_subtype == null) || (outParameter.m_subtype == null)) {
            return false;
        }

        return m_subtype.equals(outParameter.m_subtype);

    }
    
    public boolean isCompulsory() {
        return m_isCompulsory;
    }
    
    @Override
    public int hashCode() {
        return m_type.hashCode();
    }
    
}