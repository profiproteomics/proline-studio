package fr.proline.studio.parameter;

import javax.swing.JComponent;

/**
 * Error on a parameter
 * @author JM235353
 */
public class ParameterError {
    
    private String errorMessage;
    private JComponent parameterComponent;
    
    public ParameterError(String errorMessage, JComponent parameterComponent) {
        this.errorMessage = errorMessage;
        this.parameterComponent = parameterComponent;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public JComponent getParameterComponent() {
        return parameterComponent;
    }
    
}
