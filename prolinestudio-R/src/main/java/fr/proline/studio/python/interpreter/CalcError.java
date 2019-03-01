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
