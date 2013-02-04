package fr.proline.studio.parameter;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;

/**
 *
 * @author jm235353
 */
public abstract class AbstractParameter {

    protected String key;
    protected String name;
    protected Class type;
    protected Class graphicalType;
    protected JComponent parameterComponent = null;

    protected AbstractParameter(String key, String name, Class type, Class graphicalType) {
        this.key = key;
        this.name = name;
        this.type = type;
        this.graphicalType = graphicalType;
    }

    public String getName() {
        return name;
    }
    
    public String getKey() {
        return key;
    }

    public abstract JComponent getComponent(Object value);
    public abstract void initDefault();
    public abstract ParameterError checkParameter();
    
    public String getValue() {
        if (graphicalType.equals(JTextField.class)) {
           return ((JTextField) parameterComponent).getText();
        }
        if (graphicalType.equals(JSlider.class)) {
           return String.valueOf(((JSlider) parameterComponent).getValue());
        }
        if (graphicalType.equals(JSpinner.class)) {
           return String.valueOf(((JSpinner) parameterComponent).getValue());
        }
        return ""; // should not happen
    }
}
