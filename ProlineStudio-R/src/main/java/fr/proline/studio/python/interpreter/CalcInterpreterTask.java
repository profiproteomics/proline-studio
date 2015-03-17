package fr.proline.studio.python.interpreter;

/**
 *
 * @author JM235353
 */
public class CalcInterpreterTask {
    
    private CalcCallback m_callback = null;
    private String m_code = null;
    
    public CalcInterpreterTask(String code, CalcCallback callback) {
        m_code = code;
        m_callback = callback;
    }
    
    public String getCode() {
        return m_code;
    }
    
    public CalcCallback getCallback() {
        return m_callback;
    }
}
