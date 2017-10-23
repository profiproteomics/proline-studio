package fr.proline.studio.parameter;



import fr.proline.studio.gui.StringChooserPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Parameter for Strings. Can be displayed JTextField or StringChooserPanel (preselected strings + custom textfield)
 * 
 * @author jm235353
 */
public class StringParameter extends AbstractParameter {

    private ArrayList<AbstractLinkedParameters> m_linkedParametersList = null;

    private String m_defaultValue;
    private Integer m_minChars;
    private Integer m_maxChars;
    
    private String[] m_possibilitiesName = null;
    private String[] m_possibilities = null;

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
    
    public StringParameter(String key, String name, String defaultValue, Integer minChars, Integer maxChars, String[] possibilitiesName, String[] possibilities) {
        super(key, name, String.class, StringChooserPanel.class);
        m_defaultValue = defaultValue;
        m_minChars = minChars;
        m_maxChars = maxChars;
        m_possibilitiesName = possibilitiesName;
        m_possibilities = possibilities;
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
            if (m_graphicalType.equals(StringChooserPanel.class)) {
                ((StringChooserPanel) m_parameterComponent).setText(startValue);
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
        } else if  (m_graphicalType.equals(StringChooserPanel.class)) {

            // --- TextField ---
            StringChooserPanel stringChooser = new StringChooserPanel(m_possibilitiesName, m_possibilities, 2, startValue, true);

            m_parameterComponent = stringChooser;
            return stringChooser;
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
        } else if  (m_graphicalType.equals(StringChooserPanel.class)) {
            StringChooserPanel stringChooser = (StringChooserPanel) m_parameterComponent;
            stringChooser.setText(m_defaultValue);
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
        }  else if  (m_graphicalType.equals(StringChooserPanel.class)) {
            StringChooserPanel stringChooser = (StringChooserPanel) m_parameterComponent;
            value = stringChooser.getText();
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
        } else if ((m_graphicalType.equals(StringChooserPanel.class)) && (m_parameterComponent != null)) {
            ((StringChooserPanel) m_parameterComponent).setText(v);
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
        } else if (m_graphicalType.equals(StringChooserPanel.class)) {
           return ((StringChooserPanel) m_parameterComponent).getText();
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
                @Override
                public void changedUpdate(DocumentEvent e) {
                    textChanged();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    textChanged();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    textChanged();
                }

                public void textChanged() {
                    linkedParameters.valueChanged(getStringValue(), getObjectValue());
                }
            });

        } else if (m_graphicalType.equals(StringChooserPanel.class)) {

            ((StringChooserPanel) m_parameterComponent).setActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    linkedParameters.valueChanged(getStringValue(), getObjectValue());
                }

            });


        }
    }
    
    
}
