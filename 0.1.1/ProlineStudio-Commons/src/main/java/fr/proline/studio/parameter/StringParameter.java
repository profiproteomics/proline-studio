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
        this.parameterComponent = component;
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
        

        if (parameterComponent !=null) {
            if (graphicalType.equals(JTextField.class)) {
                ((JTextField) parameterComponent).setText(startValue);
                return parameterComponent;
            }
        }
        
        
        
        if (graphicalType.equals(JTextField.class)) {

            // --- TextField ---

            JTextField textField = new JTextField(30);

            if (startValue != null) {
                textField.setText(startValue);
            }
            parameterComponent = textField;
            return textField;
        }


        return null;
    }
    
    @Override
    public void initDefault() {
        if (defaultValue == null) {
            return; // should not happen
        }

        if (graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) parameterComponent;
            textField.setText(defaultValue);
        }
    }
    
    @Override
    public ParameterError checkParameter() {

        if (!used) {
            return null;
        }
        
        String value = "";
        
        if (graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) parameterComponent;
            value = textField.getText();
        }
        
        
        int length = value.length();
        if (minChars != null) {
            if (length<minChars.intValue()) {
                if (length == 0) {
                    return new ParameterError(name+" field is not filled", parameterComponent);
                } else  {
                    return new ParameterError("Minimum length of "+name+" is "+minChars.intValue()+" characters", parameterComponent);
                }
                
            }
        }
        if (maxChars != null) {
            if (length>maxChars.intValue()) {
                return new ParameterError(name+" exceeds "+minChars.intValue()+" characters", parameterComponent);
                
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
        if (graphicalType.equals(JTextField.class)) {
           return ((JTextField) parameterComponent).getText();
        }
        return ""; // should not happen
    }
    
}
