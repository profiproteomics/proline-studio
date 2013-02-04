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

    public abstract JComponent getComponent(String value);
    public abstract void initDefault();
    public abstract ParameterError checkParameter();
    
    public String getValue() {
        if (parameterComponent instanceof JTextField) {
           return ((JTextField) parameterComponent).getText();
        }
        if (parameterComponent instanceof JSlider) {
           return String.valueOf(((JSlider) parameterComponent).getValue());
        }
        if (parameterComponent instanceof JSpinner) {
           return String.valueOf(((JSpinner) parameterComponent).getValue());
        }
        return ""; // should not happen
    }
}
