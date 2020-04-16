/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.parameter;

import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Parameter of type Double, displayed as a Textfield
 *
 * @author jm235353
 */
public class DoubleParameter extends AbstractParameter {

    private Double m_minValue;
    private Double m_maxValue;
    private Double m_defaultValue;

    private Double m_startValue;

    private boolean m_edited = false;

    private ArrayList<AbstractLinkedParameters> m_linkedParametersList = null;

    public DoubleParameter(String key, String name, Class graphicalType, Double defaultValue, Double minValue, Double maxValue) {
        super(key, name, Double.class, graphicalType);
        m_defaultValue = defaultValue;
        m_minValue = minValue;
        m_maxValue = maxValue;

    }

    public DoubleParameter(String key, String name, JComponent component, Double defaultValue, Double minValue, Double maxValue) {
        super(key, name, Double.class, component.getClass());
        m_defaultValue = defaultValue;
        m_minValue = minValue;
        m_maxValue = maxValue;
        m_parameterComponent = component;
    }

    @Override
    public JComponent getComponent(Object value) {

        m_startValue = null;
        if (value != null) {
            try {
                double valueParsed = Double.parseDouble(value.toString());
                m_startValue = new Double(valueParsed);
            } catch (NumberFormatException nfe) {
            }
        }
        if (m_startValue == null) {
            m_startValue = m_defaultValue;
        }

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

        Double value = null;

        if (m_graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) m_parameterComponent;
            try {
                value = Double.parseDouble(textField.getText().trim());
            } catch (NumberFormatException nfe) {
                return new ParameterError(m_name + " is  not a Number", m_parameterComponent);
            }
        }

        if (m_minValue != null) {
            if (value < m_minValue) {
                return new ParameterError(m_name + " must be greater than " + m_minValue.toString(), m_parameterComponent);
            }
        }

        if (m_maxValue != null) {
            if (value > m_maxValue) {
                return new ParameterError(m_name + " must be lesser than " + m_maxValue.toString(), m_parameterComponent);
            }
        }

        return null;
    }

    @Override
    public void setValue(String v) {
        if ((m_graphicalType.equals(JTextField.class)) && (m_parameterComponent != null)) {
            ((JTextField) m_parameterComponent).setText(v);
        }
        if (v != null && m_startValue != null) {
            m_edited = !(Double.parseDouble(v) == m_startValue);
        }
    }

    @Override
    public String getStringValue() {
        String v = getObjectValue().toString();
        if (!v.contains(".") && !v.contains("e")) {
            v = v + ".0";  // JPM.WART : force to have a double (needed for python usage)
        }
        return v;
    }

    @Override
    public Object getObjectValue() {
        if (m_graphicalType.equals(JTextField.class)) {
            return ((JTextField) m_parameterComponent).getText().trim();
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
                    String valueString = (String) getObjectValue();
                    try {
                        Double d = Double.valueOf(valueString);
                        linkedParameters.valueChanged(valueString, d);
                    } catch (Exception e) {
                    }

                }
            });

        }
    }

    @Override
    public boolean isEdited() {
        return m_edited;
    }
}
