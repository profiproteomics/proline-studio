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

    
    public String toString() {
        if (m_errorText == null) {
            return m_errorTitle;
        }
        return m_errorTitle+'\n'+m_errorText;
    }
}
