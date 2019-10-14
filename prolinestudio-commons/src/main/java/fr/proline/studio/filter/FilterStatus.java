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
package fr.proline.studio.filter;

import java.awt.Component;

/**
 * Filter Status used to display to the user an error when
 * a filter is incorrectly set
 * @author JM235353
 */
public class FilterStatus {
    
    private String m_error;
    private Component m_c;
    
    
    public FilterStatus(String error, Component c) {
        m_error = error;
        m_c = c;
    }
    
    public String getError() {
        return m_error;
    }
    
    public Component getComponent() {
        return m_c;
    }
}
