package fr.proline.studio.parameter;

import javax.swing.JComponent;

/**
 * Class used to repert an Error on a parameter
 * @author JM235353
 */
public class ParameterError {
    
    private String m_errorMessage;
    private JComponent m_parameterComponent;
    
    public ParameterError(String errorMessage, JComponent parameterComponent) {
        m_errorMessage = errorMessage;
        m_parameterComponent = parameterComponent;
    }
    
    public String getErrorMessage() {
        return m_errorMessage;
    }
    
    public JComponent getParameterComponent() {
        return m_parameterComponent;
    }
    
}
