package fr.proline.studio.parameter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

/**
 * Parameter of type Boolean, displayed as a Checkbox
 * @author JM235353
 */
public class BooleanParameter extends AbstractParameter {

    private final Boolean m_defaultValue;
    
    private ArrayList<AbstractLinkedParameters> m_linkedParametersList = null;

    public BooleanParameter(String key, String name, Class graphicalType, Boolean defaultValue) {
        super(key, name, Boolean.class, graphicalType);
        m_defaultValue = defaultValue;
    }
    
    public BooleanParameter(String key, String name, JComponent component, Boolean defaultValue) {
        super(key, name, Boolean.class,  component.getClass());
        m_defaultValue = defaultValue;
        m_parameterComponent = component;
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

        if (m_parameterComponent != null) {
            if (m_graphicalType.equals(JCheckBox.class)) {
                ((JCheckBox) m_parameterComponent).setSelected(startValue);
                return m_parameterComponent;
            }
        }
        
        
        if (m_graphicalType.equals(JCheckBox.class)) {

            // --- CheckBox ---

            JCheckBox checkBox = new JCheckBox(getName());
            checkBox.setSelected(startValue);

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
    public void setValue(String v) {
        if ((m_graphicalType.equals(JCheckBox.class)) && (m_parameterComponent!=null)) {
            ((JCheckBox)m_parameterComponent).setSelected(Boolean.parseBoolean(v));
        }
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
    public LabelVisibility showLabel() {
        return LabelVisibility.NO_VISIBLE;
    }
    
    public void addLinkedParameters(final AbstractLinkedParameters linkedParameters) {

        // create parameterComponent if needed
        getComponent(null);

        if (m_linkedParametersList == null) {
            m_linkedParametersList = new ArrayList<>(1);
        }
        m_linkedParametersList.add(linkedParameters);
 
        if (m_parameterComponent instanceof JCheckBox) {
            ((JCheckBox) m_parameterComponent).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    linkedParameters.valueChanged(getStringValue(), getObjectValue());
                }

            });
            initDefault();
        }
    }
    
    
}

