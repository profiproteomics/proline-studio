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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * Reports an error during a calculation for the calculator
 * 
 * @author jm235353
 */
public class CalcError {
    
    private final Throwable m_t;
    private final String m_errorMessage;
    private final int m_lineError;

    public CalcError(Throwable t, String errorMessage, int lineError) {
        m_t = t;
        if ((errorMessage == null) && (t!=null)) {
            m_errorMessage = t.getMessage();
        } else {
            m_errorMessage = errorMessage;
        }
        m_lineError = lineError;
    }
    
    public String getErrorMessage() {
        return m_errorMessage;
    }
    
    public int getLineError() {
        return m_lineError;
    }
    
    public String getFullErrorMessage() {
        if (m_t != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            m_t.printStackTrace(pw);
            return sw.toString();
        } else {
            return m_errorMessage;
        }
    }
}
