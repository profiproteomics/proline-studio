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
package fr.proline.studio.dam.taskinfo;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Error happening during the execution of a task
 * @author JM235353
 */
public class TaskError {
    
    private String m_errorTitle;
    private String m_errorText;
    
    public TaskError(String errorTitle) {
        m_errorTitle = errorTitle;
    }
    public TaskError(String errorTitle, String errorText) {
        m_errorTitle = errorTitle;
        m_errorText = errorText;
    }
    public TaskError(Exception e) {
        m_errorTitle = e.getMessage();
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        m_errorText = sw.toString();
    }
    
    public String getErrorTitle() {
        return m_errorTitle;
    }
    
    public String getErrorText() {
        return m_errorText;
    }
    
    public void setErrorText(String errorText) {
        m_errorText = errorText;
    }

    
    @Override
    public String toString() {
        if (m_errorText == null) {
            return m_errorTitle;
        }
        return m_errorTitle+'\n'+m_errorText;
    }
}
