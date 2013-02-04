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
    public JComponent getComponent() {

        if (graphicalType.equals(JTextField.class)) {
            JTextField textField = new JTextField(3);
            if (defaultValue != null) {
                textField.setText(defaultValue.toString());
            }
            parameterComponent = textField;
            return textField;
        }

        return null;
    }
}
