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
