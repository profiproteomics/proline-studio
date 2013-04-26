/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.parameter;

import javax.swing.JComponent;

/**
 *
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
