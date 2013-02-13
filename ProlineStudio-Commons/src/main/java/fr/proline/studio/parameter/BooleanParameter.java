/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.parameter;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

/**
 *
 * @author JM235353
 */
public class BooleanParameter extends AbstractParameter {

    private Boolean defaultValue;

    public BooleanParameter(String key, String name, Class graphicalType, Boolean defaultValue) {
        super(key, name, Boolean.class, graphicalType);
        this.defaultValue = defaultValue;

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
            startValue = (defaultValue!=null) ? defaultValue : Boolean.TRUE;
        }

        
        if (graphicalType.equals(JCheckBox.class)) {

            // --- TextField ---

            JCheckBox checkBox = new JCheckBox(getName());

            if (startValue != null) {
                checkBox.setSelected(startValue);
            }
            parameterComponent = checkBox;
            return checkBox;
        }


        return null;
    }
    
    @Override
    public void initDefault() {
        if (defaultValue == null) {
            return; // should not happen
        }

        if (graphicalType.equals(JCheckBox.class)) {
            JCheckBox checkBox = (JCheckBox) parameterComponent;
            checkBox.setSelected(defaultValue);
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
        if (graphicalType.equals(JCheckBox.class)) {
           return Boolean.valueOf(((JCheckBox) parameterComponent).isSelected());
        }
        return ""; // should not happen
    }
 
    @Override
    public boolean showLabel() {
        return false;
    }
    
    
}

