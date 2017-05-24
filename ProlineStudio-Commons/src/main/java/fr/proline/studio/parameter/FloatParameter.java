package fr.proline.studio.parameter;

import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Parameter of type Double, displayed as a Textfield
 * @author jm235353
 */
public class FloatParameter extends AbstractParameter {

    private Float m_minValue;
    private Float m_maxValue;
    private Float m_defaultValue;

    private ArrayList<AbstractLinkedParameters> m_linkedParametersList = null;
    
    public FloatParameter(String key, String name, Class graphicalType, Float defaultValue, Float minValue, Float maxValue) {
        super(key, name, Float.class, graphicalType);
        m_defaultValue = defaultValue;
        m_minValue = minValue;
        m_maxValue = maxValue;

    }
    
    public FloatParameter(String key, String name, JComponent component, Float defaultValue, Float minValue, Float maxValue) {
        super(key, name, Float.class, component.getClass());
        m_defaultValue = defaultValue;
        m_minValue = minValue;
        m_maxValue = maxValue;
        m_parameterComponent = component;
    }

    @Override
    public JComponent getComponent(Object value) {

        if (m_parameterComponent != null) {
            if (m_graphicalType.equals(JTextField.class)) {
                if (value != null) {
                    ((JTextField) m_parameterComponent).setText(value.toString());
                } else if (m_defaultValue != null) {
                    ((JTextField) m_parameterComponent).setText(m_defaultValue.toString());
                }

                return m_parameterComponent;
            }
        }
        
        
        if (m_graphicalType.equals(JTextField.class)) {
            JTextField textField = new JTextField(3);
            if (value != null) {
                textField.setText(value.toString());
            } else if (m_defaultValue != null) {
                textField.setText(m_defaultValue.toString());
            }
            m_parameterComponent = textField;
            return textField;
        }

        return null;
    }
    
    @Override
    public void initDefault() {
        if (m_defaultValue == null) {
            return; // should not happen
        }
        
        if (m_graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) m_parameterComponent;
            textField.setText(m_defaultValue.toString());
        }
    }

    @Override
    public ParameterError checkParameter() {
        
        if (!m_used && !m_compulsory) {
            return null;
        }
        
        Float value = null;
        
        if (m_graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) m_parameterComponent;
            try {
                value = Float.parseFloat(textField.getText());
            } catch (NumberFormatException nfe) {
                return new ParameterError(m_name+" is  not a Number", m_parameterComponent);
            }
        }
        
        if (m_minValue != null) {
            if (value < m_minValue) {
                return new ParameterError(m_name+" must be greater than "+m_minValue.toString(), m_parameterComponent);
            }
        }
        
        if (m_maxValue != null) {
            if (value > m_maxValue) {
                return new ParameterError(m_name+" must be lesser than "+m_maxValue.toString(), m_parameterComponent);
            }
        }
        
        return null;
    }
    
    @Override
    public void setValue(String v) {
        if ((m_graphicalType.equals(JTextField.class)) && (m_parameterComponent!=null)) {
            ((JTextField)m_parameterComponent).setText(v);
        }
    }
    

    @Override
    public String getStringValue() {
        String v = getObjectValue().toString();
        if (!v.contains(".")) {
            v = v+".0";  // JPM.WART : force to have a float (needed for python usage)
        }
        return v;
    }

    @Override
    public Object getObjectValue() {
        if (m_graphicalType.equals(JTextField.class)) {
           return ((JTextField) m_parameterComponent).getText();
        }
        return ""; // should not happen
    }
    
    
    public void addLinkedParameters(final AbstractLinkedParameters linkedParameters) {

        // create parameterComponent if needed
        getComponent(null);

        if (m_linkedParametersList == null) {
            m_linkedParametersList = new ArrayList<>(1);
        }
        m_linkedParametersList.add(linkedParameters);

        if (m_graphicalType.equals(JTextField.class)) {

            ((JTextField) m_parameterComponent).getDocument().addDocumentListener(new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                    textChanged();
                }

                public void removeUpdate(DocumentEvent e) {
                    textChanged();
                }

                public void insertUpdate(DocumentEvent e) {
                    textChanged();
                }

                public void textChanged() {
                    String valueString = (String) getObjectValue();
                    try {
                        float d = Float.valueOf(valueString);
                        linkedParameters.valueChanged(valueString, d);
                    } catch (Exception e) {
                    }
                    
                }
            });

        }
    }
}