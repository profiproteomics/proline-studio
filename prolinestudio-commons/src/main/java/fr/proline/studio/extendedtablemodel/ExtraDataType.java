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
package fr.proline.studio.extendedtablemodel;

/**
 * Type for extra data which can be provided by a TableModel extending ExtraDataForTableModelInterface.
 * 
 * @author JM235353
 */
public class ExtraDataType {
    
    private final Class m_class;
    private final boolean m_isList;
    
    /**
     * 
     * @param c   class of the extra data type
     * @param list  boolean indicating if it is a value or a list of values
     */
    public ExtraDataType(Class c, boolean list) {
        m_class = c;
        m_isList = list;
    }
    
    public Class getTypeClass() {
        return m_class;
    }
    
    public boolean isList() {
        return m_isList;
    }
    
}
