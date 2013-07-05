package fr.proline.studio.parameter;


import java.awt.FlowLayout;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author jm235353
 */
public class StringParameter extends AbstractParameter {


    private String defaultValue;
    private Integer minChars;
    private Integer maxChars;

    public StringParameter(String key, String name, Class graphicalType, String defaultValue, Integer minChars, Integer maxChars) {
        super(key, name, String.class, graphicalType);
        this.defaultValue = defaultValue;
        this.minChars = minChars;
        this.maxChars = maxChars;
        
    }
    
    public StringParameter(String key, String name, JComponent component, String defaultValue, Integer minChars, Integer maxChars) {
        super(key, name, String.class, component.getClass());
        this.defaultValue = defaultValue;
        this.m_parameterComponent = component;
        this.minChars = minChars;
        this.maxChars = maxChars;

    }

    @Override
    public JComponent getComponent(Object value) {

        String startValue = null;
        if (value != null)  {
            startValue = value.toString();
        }
        if (startValue == null) {
            startValue = (defaultValue!= null) ? defaultValue : "";
        }
        

        if (m_parameterComponent !=null) {
            if (m_graphicalType.equals(JTextField.class)) {
                ((JTextField) m_parameterComponent).setText(startValue);
                return m_parameterComponent;
            }
        }
        
        
        
        if (m_graphicalType.equals(JTextField.class)) {

            // --- TextField ---

            JTextField textField = new JTextField(30);

            if (startValue != null) {
                textField.setText(startValue);
            }
            m_parameterComponent = textField;
            return textField;
        }


        return null;
    }
    
    @Override
    public void initDefault() {
        if (defaultValue == null) {
            return; // should not happen
        }

        if (m_graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) m_parameterComponent;
            textField.setText(defaultValue);
        }
    }
    
    @Override
    public ParameterError checkParameter() {

        if (!m_used) {
            return null;
        }
        
        String value = "";
        
        if (m_graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) m_parameterComponent;
            value = textField.getText();
        }
        
        
        int length = value.length();
        if (minChars != null) {
            if (length<minChars.intValue()) {
                if (length == 0) {
                    return new ParameterError(m_name+" field is not filled", m_parameterComponent);
                } else  {
                    return new ParameterError("Minimum length of "+m_name+" is "+minChars.intValue()+" characters", m_parameterComponent);
                }
                
            }
        }
        if (maxChars != null) {
            if (length>maxChars.intValue()) {
                return new ParameterError(m_name+" exceeds "+minChars.intValue()+" characters", m_parameterComponent);
                
            }
        }
        
        
        return null;
    }

    @Override
    public String getStringValue() {
        return getObjectValue().toString();
    }

    @Override
    public Object getObjectValue() {
        if (m_graphicalType.equals(JTextField.class)) {
           return ((JTextField) m_parameterComponent).getText();
        }
        return ""; // should not happen
    }
    
}
