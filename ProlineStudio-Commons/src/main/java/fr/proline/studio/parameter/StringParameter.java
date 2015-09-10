package fr.proline.studio.parameter;



import javax.swing.*;

/**
 *
 * @author jm235353
 */
public class StringParameter extends AbstractParameter {


    private String m_defaultValue;
    private Integer m_minChars;
    private Integer m_maxChars;

    public StringParameter(String key, String name, Class graphicalType, String defaultValue, Integer minChars, Integer maxChars) {
        super(key, name, String.class, graphicalType);
        m_defaultValue = defaultValue;
        m_minChars = minChars;
        m_maxChars = maxChars;
        
    }
    
    public StringParameter(String key, String name, JComponent component, String defaultValue, Integer minChars, Integer maxChars) {
        super(key, name, String.class, component.getClass());
        m_defaultValue = defaultValue;
        m_parameterComponent = component;
        m_minChars = minChars;
        m_maxChars = maxChars;

    }

    @Override
    public JComponent getComponent(Object value) {

        String startValue = null;
        if (value != null)  {
            startValue = value.toString();
        }
        if (startValue == null) {
            startValue = (m_defaultValue!= null) ? m_defaultValue : "";
        }
        

        if (m_parameterComponent !=null) {
            if (m_graphicalType.equals(JTextField.class)) {
                ((JTextField) m_parameterComponent).setText(startValue);
                return m_parameterComponent;
            }
        }
        
        
        
        if (m_graphicalType.equals(JTextField.class)) {

            // --- TextField ---

            JTextField textField = new JTextField(30);

            if (startValue != null) {
                textField.setText(startValue);
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
            textField.setText(m_defaultValue);
        }
    }
    
    @Override
    public ParameterError checkParameter() {

        if (!m_used && !m_compulsory) {
            return null;
        }
        
        String value = "";
        
        if (m_graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) m_parameterComponent;
            value = textField.getText();
        }
        
        
        int length = value.length();
        if (m_minChars != null) {
            if (length<m_minChars.intValue()) {
                if (length == 0) {
                    return new ParameterError(m_name+" field is not filled", m_parameterComponent);
                } else  {
                    return new ParameterError("Minimum length of "+m_name+" is "+m_minChars.intValue()+" characters", m_parameterComponent);
                }
                
            }
        }
        if (m_maxChars != null) {
            if (length>m_maxChars.intValue()) {
                return new ParameterError(m_name+" exceeds "+m_maxChars.intValue()+" characters", m_parameterComponent);
                
            }
        }
        
        
        return null;
    }

    @Override
    public void setValue(String v) {
        if ((m_graphicalType.equals(JTextField.class)) && (m_parameterComponent != null)) {
            ((JTextField) m_parameterComponent).setText(v);
        }
    }
    
    @Override
    public String getStringValue() {
        return getObjectValue().toString();
    }

    @Override
    public Object getObjectValue() {
        if (m_graphicalType.equals(JTextField.class)) {
           return ((JTextField) m_parameterComponent).getText();
        }
        return ""; // should not happen
    }
    
}
