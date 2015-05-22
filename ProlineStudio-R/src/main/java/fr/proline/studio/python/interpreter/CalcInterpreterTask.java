package fr.proline.studio.python.interpreter;

/**
 *
 * @author JM235353
 */
public class CalcInterpreterTask {
    
    private CalcCallback m_callback = null;
    private String m_code = null;
    private ResultVariable[] m_parameters = null;
    
    public CalcInterpreterTask(String code, CalcCallback callback) {
        this(code, null, callback);
    }
    
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
