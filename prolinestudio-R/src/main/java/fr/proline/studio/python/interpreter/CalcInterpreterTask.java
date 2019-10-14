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
package fr.proline.studio.python.interpreter;

/**
 *
 * Task to do a calculation in Python
 * 
 * @author JM235353
 */
public class CalcInterpreterTask {
    
    private CalcCallback m_callback = null;
    private String m_code = null;
    private ResultVariable[] m_parameters = null;
    
    /**
     * 
     * @param code      python expression
     * @param callback 
     */
    public CalcInterpreterTask(String code, CalcCallback callback) {
        this(code, null, callback);
    }
    
    /**
     * 
     * @param code  python expression
     * @param parameters  variables to be added to the python interpreter environment
     * @param callback 
     */
    public CalcInterpreterTask(String code, ResultVariable[] parameters, CalcCallback callback) {
        m_code = code;
        m_callback = callback;
        m_parameters = parameters;
    } 
    
    public String getCode() {
        return m_code;
    }
    
    public CalcCallback getCallback() {
        return m_callback;
    }
    
    public ResultVariable[] getParameters() {
        return m_parameters;
    }
    
    
}
