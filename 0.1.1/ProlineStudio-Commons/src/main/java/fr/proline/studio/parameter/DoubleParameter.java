package fr.proline.studio.parameter;

import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 *
 * @author jm235353
 */
public class DoubleParameter extends AbstractParameter {

    private Double minValue;
    private Double maxValue;
    private Double defaultValue;

    public DoubleParameter(String key, String name, Class graphicalType, Double defaultValue, Double minValue, Double maxValue) {
        super(key, name, Double.class, graphicalType);
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;

    }
    
    public DoubleParameter(String key, String name, JComponent component, Double defaultValue, Double minValue, Double maxValue) {
        super(key, name, Double.class, component.getClass());
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.parameterComponent = component;
    }

    @Override
    public JComponent getComponent(Object value) {

        if (parameterComponent != null) {
            if (graphicalType.equals(JTextField.class)) {
                if (value != null) {
                    ((JTextField) parameterComponent).setText(value.toString());
                } else if (defaultValue != null) {
                    ((JTextField) parameterComponent).setText(defaultValue.toString());
                }

                return parameterComponent;
            }
        }
        
        
        if (graphicalType.equals(JTextField.class)) {
            JTextField textField = new JTextField(3);
            if (value != null) {
                textField.setText(value.toString());
            } else if (defaultValue != null) {
                textField.setText(defaultValue.toString());
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
            textField.setText(defaultValue.toString());
        }
    }

    @Override
    public ParameterError checkParameter() {
        
        if (!used) {
            return null;
        }
        
        Double value = null;
        
        if (graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) parameterComponent;
            try {
                value = Double.parseDouble(textField.getText());
            } catch (NumberFormatException nfe) {
                return new ParameterError(name+" is  not a Number", parameterComponent);
            }
        }
        
        if (minValue != null) {
            if (value < minValue) {
                return new ParameterError(name+" must be greater than "+minValue.toString(), parameterComponent);
            }
        }
        
        if (maxValue != null) {
            if (value > maxValue) {
                return new ParameterError(name+" must be lesser than "+maxValue.toString(), parameterComponent);
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
