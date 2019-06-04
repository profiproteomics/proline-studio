package fr.proline.studio.parameter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JRadioButton;

/**
 * Parameter of type Boolean, displayed as a Checkbox
 *
 * @author JM235353
 */
public class BooleanParameter extends AbstractParameter {

    private final Boolean m_defaultValue;

    private boolean m_valueSet = false;

    boolean m_edited = false;

    Boolean m_startValue = null;

    private ArrayList<AbstractLinkedParameters> m_linkedParametersList = null;

    public BooleanParameter(String key, String name, Class graphicalType, Boolean defaultValue) {
        super(key, name, Boolean.class, graphicalType);
        m_defaultValue = defaultValue;

        m_labelVisibility = LabelVisibility.NO_VISIBLE;
    }

    public BooleanParameter(String key, String name, JComponent component, Boolean defaultValue) {
        super(key, name, Boolean.class, component.getClass());
        m_defaultValue = defaultValue;
        m_parameterComponent = component;

        m_labelVisibility = LabelVisibility.NO_VISIBLE;
    }

    @Override
    public JComponent getComponent(Object value) {

        m_startValue = null;
        if (value != null) {
            try {
                boolean valueParsed = Boolean.parseBoolean(value.toString());
                m_startValue = Boolean.valueOf(valueParsed);
            } catch (NumberFormatException nfe) {
            }
        }
        if ((m_startValue == null)) {
            m_startValue = (m_defaultValue != null) ? m_defaultValue : Boolean.TRUE;
        }

      if (m_parameterComponent != null) {
        if (m_graphicalType.equals(JCheckBox.class)) {
          ((JCheckBox) m_parameterComponent).setSelected(m_startValue);
          m_valueSet = true;
          return m_parameterComponent;
        } else if (m_graphicalType.equals(JRadioButton.class)) {
          ((JRadioButton) m_parameterComponent).setSelected(m_startValue);
          m_valueSet = true;
          return m_parameterComponent;
        }
      }

        if (m_graphicalType.equals(JCheckBox.class)) {

            // --- CheckBox ---
            JCheckBox checkBox = new JCheckBox(getName());
            checkBox.setSelected(m_startValue);
            m_valueSet = true;

            m_parameterComponent = checkBox;
            return checkBox;
        } else if (m_graphicalType.equals(JRadioButton.class)) {
            // --- JRadioButton ---
            JRadioButton radioButton = new JRadioButton(getName());
            radioButton.setSelected(m_startValue);
            m_valueSet = true;

            m_parameterComponent = radioButton;
            return radioButton;
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
            m_valueSet = true;
        } else if (m_graphicalType.equals(JRadioButton.class)) {
            JRadioButton radioButton = (JRadioButton) m_parameterComponent;
            radioButton.setSelected(m_defaultValue);
            m_valueSet = true;
        }

    }

    @Override
    public ParameterError checkParameter() {
        return null;
    }

    @Override
    public void setValue(String v) {
        if ((m_graphicalType.equals(JCheckBox.class)) && (m_parameterComponent != null)) {
            ((JCheckBox) m_parameterComponent).setSelected(Boolean.parseBoolean(v));
            m_valueSet = true;
        } else if ((m_graphicalType.equals(JRadioButton.class)) && (m_parameterComponent != null)) {
            ((JRadioButton) m_parameterComponent).setSelected(Boolean.parseBoolean(v));
            m_valueSet = true;
        }

        if (m_valueSet && v!=null && m_startValue!=null) {
            m_edited = !(Boolean.parseBoolean(v) == m_startValue);
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
        } else if (m_graphicalType.equals(JRadioButton.class)) {
            return Boolean.valueOf(((JRadioButton) m_parameterComponent).isSelected());
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

        if (m_parameterComponent instanceof JCheckBox) {
            ((JCheckBox) m_parameterComponent).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    linkedParameters.valueChanged(getStringValue(), getObjectValue());
                }

            });
        } else if (m_parameterComponent instanceof JRadioButton) {
            ((JRadioButton) m_parameterComponent).addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    linkedParameters.valueChanged(getStringValue(), getObjectValue());
                }
            });
        }
        if (!m_valueSet) {
            initDefault();
        } else {

            linkedParameters.valueChanged(getStringValue(), getObjectValue());
        }
    }

    public boolean isEdited() {
        return m_edited;
    }

}
