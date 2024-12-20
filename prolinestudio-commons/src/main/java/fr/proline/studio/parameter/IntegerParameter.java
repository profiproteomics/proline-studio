/* 
 * Copyright (C) 2019
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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Parameter of type Integer, displayed as a Textfield or a Slider
 *
 * @author jm235353
 */
public class IntegerParameter extends AbstractParameter {

    private Integer m_minValue;
    private Integer m_maxValue;
    private Integer m_defaultValue;
    private Integer m_startValue;

    private boolean m_edited;

    public IntegerParameter(String key, String name, Class graphicalType, Integer defaultValue, Integer minValue, Integer maxValue) {
        super(key, name, Integer.class, graphicalType);
        m_defaultValue = defaultValue;
        m_minValue = minValue;
        m_maxValue = maxValue;

    }

    public IntegerParameter(String key, String name, JComponent component, Integer defaultValue, Integer minValue, Integer maxValue) {
        super(key, name, Integer.class, component.getClass());
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
                int valueParsed = Integer.parseInt(value.toString());
                m_startValue = Integer.valueOf(valueParsed);
            } catch (NumberFormatException nfe) {
            }
        }
        if (m_startValue == null) {
            m_startValue = m_defaultValue;
        }

        if (m_parameterComponent != null) {
            if (m_graphicalType.equals(JTextField.class)) {
                ((JTextField) m_parameterComponent).setText(m_startValue.toString());
                return m_parameterComponent;
            } else if (m_graphicalType.equals(JSlider.class)) {
                ((JSlider) m_parameterComponent).setValue(m_startValue);
                return m_parameterComponent;
            } else if (m_graphicalType.equals(JSpinner.class)) {
                ((JSpinner) m_parameterComponent).setValue(m_startValue);
                return m_parameterComponent;
            }
        }

        if (m_graphicalType.equals(JTextField.class)) {

            // --- TextField ---
            JTextField textField = new JTextField(30);

            if (m_startValue != null) {
                textField.setText(m_startValue.toString());
            }
            m_parameterComponent = textField;
            return textField;
        } else if (m_graphicalType.equals(JSlider.class)) {

            // --- Slider ---
            JPanel sliderPanel = new JPanel(new FlowLayout());

            final JSlider slider = new JSlider(m_minValue, m_maxValue, m_startValue);
            slider.setPaintTicks(true);
            final JTextField textField = new JTextField(3);
            textField.setText(String.valueOf(m_startValue));

            slider.addChangeListener(new javax.swing.event.ChangeListener() {

                @Override
                public void stateChanged(javax.swing.event.ChangeEvent evt) {
                    int v = (int) slider.getValue();
                    textField.setText(String.valueOf(v));
                    if (m_externalActionListener != null) {
                        ActionEvent e = new ActionEvent(slider, -1, null);
                        m_externalActionListener.actionPerformed(e);
                    }
                }
            });

            textField.getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void changedUpdate(DocumentEvent e) {
                    modifySlider();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    modifySlider();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    modifySlider();
                }

                private void modifySlider() {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                int value = Integer.parseInt(textField.getText().trim());
                                slider.setValue(value);
                            } catch (NumberFormatException e) {
                            }
                        }
                    });

                }
            });

            sliderPanel.add(slider);
            sliderPanel.add(textField);

            m_parameterComponent = slider;

            return sliderPanel;
        } else if (m_graphicalType.equals(JSpinner.class)) {

            // --- Spinner ---
            JSpinner spinner = new JSpinner();
            SpinnerNumberModel model = new SpinnerNumberModel(m_startValue, m_minValue, m_maxValue, Integer.valueOf(1));
            spinner.setModel(model);
            spinner.addChangeListener(new javax.swing.event.ChangeListener() {

                @Override
                public void stateChanged(javax.swing.event.ChangeEvent evt) {
                    if (m_externalActionListener != null) {
                        ActionEvent e = new ActionEvent(spinner, -1, null);
                        m_externalActionListener.actionPerformed(e);
                    }
                }
            });

            m_parameterComponent = spinner;

            return spinner;
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
        } else if (m_graphicalType.equals(JSlider.class)) {
            JSlider slider = (JSlider) m_parameterComponent;
            slider.setValue(m_defaultValue);
        } else if (m_graphicalType.equals(JSpinner.class)) {
            JSpinner spinner = (JSpinner) m_parameterComponent;
            spinner.setValue(m_defaultValue);
        }
    }

    public void setDefaultValue(Integer defaultValue) {
        m_defaultValue = defaultValue;
        initDefault();
    }

    @Override
    public ParameterError checkParameter() {

        if (!m_used && !m_compulsory) {
            return null;
        }

        Integer value = null;

        if (m_graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) m_parameterComponent;
            try {
                value = Integer.parseInt(textField.getText().trim());
            } catch (NumberFormatException nfe) {
                return new ParameterError(m_name + " is not a Integer", m_parameterComponent);
            }
        } else if (m_graphicalType.equals(JSlider.class)) {
            // with a slider, there can be no error
            return null;
        } else if (m_graphicalType.equals(JSpinner.class)) {
            // with a slider, there can be no error
            return null;
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
        if (m_parameterComponent == null) {
            return; // should not happen
        }

        if (v == null) {
            v = m_defaultValue.toString();
        }

        if (m_graphicalType.equals(JTextField.class)) {
            ((JTextField) m_parameterComponent).setText(v);
        } else if (m_graphicalType.equals(JSlider.class)) {
            ((JSlider) m_parameterComponent).setValue(Integer.valueOf(v));
        } else if (m_graphicalType.equals(JSpinner.class)) {
            ((JSpinner) m_parameterComponent).setValue(Integer.valueOf(v));
        }

        if (v != null && m_startValue != null) {
            m_edited = !(Integer.parseInt(v) == m_startValue);
        }

    }

    @Override
    public String getStringValue() {
        Object v = getObjectValue();
        return (v != null) ? v.toString() : null;
    }

    @Override
    public Object getObjectValue() {
        if (m_parameterComponent == null) {
            return null;
        }

        if (m_graphicalType.equals(JTextField.class)) {
            return Integer.parseInt(((JTextField) m_parameterComponent).getText().trim());
        }
        if (m_graphicalType.equals(JSlider.class)) {
            return ((JSlider) m_parameterComponent).getValue();
        }
        if (m_graphicalType.equals(JSpinner.class)) {
            return ((JSpinner) m_parameterComponent).getValue();
        }
        return ""; // should not happen
    }

    @Override
    public boolean isEdited() {
        return m_edited;
    }

}
