package fr.proline.studio.parameter;


import java.awt.FlowLayout;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author jm235353
 */
public class IntegerParameter extends AbstractParameter {

    private Integer minValue;
    private Integer maxValue;
    private Integer defaultValue;

    public IntegerParameter(String key, String name, Class graphicalType, Integer defaultValue, Integer minValue, Integer maxValue) {
        super(key, name, Integer.class, graphicalType);
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;

    }
    
    public IntegerParameter(String key, String name, JComponent component, Integer defaultValue, Integer minValue, Integer maxValue) {
        super(key, name, Integer.class, component.getClass());
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.m_parameterComponent = component;

    }

    @Override
    public JComponent getComponent(Object value) {

        Integer startValue = null;
        if (value != null)  {
            try {
                int valueParsed = Integer.parseInt(value.toString());
                startValue = new Integer(valueParsed);
            } catch (NumberFormatException nfe) {
            }
        }
        if (startValue == null) {
            startValue = defaultValue;
        }

        if (m_parameterComponent !=null) {
            if (m_graphicalType.equals(JTextField.class)) {
                ((JTextField) m_parameterComponent).setText(startValue.toString());
                return m_parameterComponent;
            }
        }
        
        
        
        if (m_graphicalType.equals(JTextField.class)) {

            // --- TextField ---

            JTextField textField = new JTextField(30);

            if (startValue != null) {
                textField.setText(startValue.toString());
            }
            m_parameterComponent = textField;
            return textField;
        } else if (m_graphicalType.equals(JSlider.class)) {

            // --- Slider ---

            JPanel sliderPanel = new JPanel(new FlowLayout());


            
            
            final JSlider slider = new JSlider(minValue, maxValue, startValue);
            slider.setPaintTicks(true);
            final JTextField textField = new JTextField(3);
            textField.setText(String.valueOf(startValue));

            slider.addChangeListener(new javax.swing.event.ChangeListener() {

                @Override
                public void stateChanged(javax.swing.event.ChangeEvent evt) {
                    int v = (int) slider.getValue();
                    textField.setText(String.valueOf(v));
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
                                int value = Integer.parseInt(textField.getText());
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
            SpinnerNumberModel model = new SpinnerNumberModel(startValue, minValue, maxValue, new Integer(1));
            spinner.setModel(model);

            m_parameterComponent = spinner;

            return spinner;
        }


        return null;
    }
    
    @Override
    public void initDefault() {
        if (defaultValue == null) {
            return; // should not happen
        }

        if (m_graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) m_parameterComponent;
            textField.setText(defaultValue.toString());
        } else if (m_graphicalType.equals(JSlider.class)) {
            JSlider slider = (JSlider) m_parameterComponent;
            slider.setValue(defaultValue);
        }  else if (m_graphicalType.equals(JSpinner.class)) {
            JSpinner spinner = (JSpinner) m_parameterComponent;
            spinner.setValue(defaultValue);
        }
    }
    
    @Override
    public ParameterError checkParameter() {
        
        if (!m_used) {
            return null;
        }
        
        Integer value = null;
        
        if (m_graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) m_parameterComponent;
            try {
                value = Integer.parseInt(textField.getText());
            } catch (NumberFormatException nfe) {
                return new ParameterError(m_name+" is not a Integer", m_parameterComponent);
            }
        } else if (m_graphicalType.equals(JSlider.class)) {
            // with a slider, there can be no error
            return null;
        } else if (m_graphicalType.equals(JSpinner.class)) {
            // with a slider, there can be no error
            return null;
        }
        
        if (minValue != null) {
            if (value < minValue) {
                return new ParameterError(m_name+" must be greater than "+minValue.toString(), m_parameterComponent);
            }
        }
        
        if (maxValue != null) {
            if (value > maxValue) {
                return new ParameterError(m_name+" must be lesser than "+maxValue.toString(), m_parameterComponent);
            }
        }
        
        return null;
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
        if (m_graphicalType.equals(JSlider.class)) {
           return ((JSlider) m_parameterComponent).getValue();
        }
        if (m_graphicalType.equals(JSpinner.class)) {
           return ((JSpinner) m_parameterComponent).getValue();
        }
        return ""; // should not happen
    }
    
}
