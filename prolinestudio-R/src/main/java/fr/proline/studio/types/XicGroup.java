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
package fr.proline.studio.types;

/**
 * Class used to add information to a Table Model in the Data Analyzer
 * This class corresponds to a reference to the Xic Group of a column
 * 
 * @author JM235353
 */
public class XicGroup {

    private Long m_id;
    private String m_name;
    
    public XicGroup(Long id, String name) {
        if (id == null) {
            m_id = -1L; // JPM.WART temporary wart for Spectral Count
        } else {
            m_id = id;
        }
        m_name = name;
    }
    
    public long getId() {
        return m_id;
    }
    
    public String getName() {
        return m_name;
    }

    
}
