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

    @Override
    public JComponent getComponent(String value) {

        if (graphicalType.equals(JTextField.class)) {
            JTextField textField = new JTextField(3);
            if (value != null) {
                textField.setText(value);
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
        
        Double value = null;
        
        if (graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) parameterComponent;
            try {
                value = Double.parseDouble(textField.getText());
            } catch (NumberFormatException nfe) {
                return new ParameterError(name+" is  not a Number.", parameterComponent);
            }
        }
        
        if (minValue != null) {
            if (value < minValue) {
                return new ParameterError(name+" must be greater than "+minValue.toString()+".", parameterComponent);
            }
        }
        
        if (maxValue != null) {
            if (value > maxValue) {
                return new ParameterError(name+" must be lesser than "+maxValue.toString()+".", parameterComponent);
            }
        }
        
        return null;
    }
    
}
