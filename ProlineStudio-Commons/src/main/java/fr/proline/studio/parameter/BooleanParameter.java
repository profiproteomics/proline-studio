package fr.proline.studio.parameter;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

/**
 * Parameter of type Boolean, displayed as a Checkbox
 * @author JM235353
 */
public class BooleanParameter extends AbstractParameter {

    private Boolean m_defaultValue;

    public BooleanParameter(String key, String name, Class graphicalType, Boolean defaultValue) {
        super(key, name, Boolean.class, graphicalType);
        this.m_defaultValue = defaultValue;

    }

    @Override
    public JComponent getComponent(Object value) {

        Boolean startValue = null;
        if (value != null) {
            try {
                boolean valueParsed = Boolean.parseBoolean(value.toString());
                startValue = Boolean.valueOf(valueParsed);
            } catch (NumberFormatException nfe) {
            }
        }
        if (startValue == null) {
            startValue = (m_defaultValue!=null) ? m_defaultValue : Boolean.TRUE;
        }

        
        if (m_graphicalType.equals(JCheckBox.class)) {

            // --- TextField ---

            JCheckBox checkBox = new JCheckBox(getName());

            if (startValue != null) {
                checkBox.setSelected(startValue);
            }
            m_parameterComponent = checkBox;
            return checkBox;
        }


        return null;
    }
    
    @Override
    public void initDefault() {
        if (m_defaultValue == null) {
            return; // should not happen
        }

        if (m_graphicalType.equals(JCheckBox.class)) {
            JCheckBox checkBox = (JCheckBox) m_parameterComponent;
            checkBox.setSelected(m_defaultValue);
        }
    }
    
    @Override
    public ParameterError checkParameter() {        
        return null;
    }

    @Override
    public String getStringValue() {
        return getObjectValue().toString();
    }

    @Override
    public Object getObjectValue() {
        if (m_graphicalType.equals(JCheckBox.class)) {
           return Boolean.valueOf(((JCheckBox) m_parameterComponent).isSelected());
        }
        return ""; // should not happen
    }
 
    @Override
    public boolean showLabel() {
        return false;
    }
    
    
}

