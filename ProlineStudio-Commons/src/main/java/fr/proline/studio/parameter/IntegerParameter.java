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

    @Override
    public JComponent getComponent(Object value) {

        Integer startValue = null;
        if (value != null) {
            try {
                int valueParsed = Integer.parseInt(value.toString());
                startValue = new Integer(valueParsed);
            } catch (NumberFormatException nfe) {
            }
        }
        if (startValue == null) {
            startValue = defaultValue;
        }

        
        if (graphicalType.equals(JTextField.class)) {

            // --- TextField ---

            JTextField textField = new JTextField(30);

            if (startValue != null) {
                textField.setText(startValue.toString());
            }
            parameterComponent = textField;
            return textField;
        } else if (graphicalType.equals(JSlider.class)) {

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

            parameterComponent = slider;

            return sliderPanel;
        } else if (graphicalType.equals(JSpinner.class)) {

            // --- Spinner ---

            JSpinner spinner = new JSpinner();
            SpinnerNumberModel model = new SpinnerNumberModel(startValue, minValue, maxValue, new Integer(1));
            spinner.setModel(model);

            parameterComponent = spinner;

            return spinner;
        }


        return null;
    }
    
    @Override
    public void initDefault() {
        if (defaultValue == null) {
            return; // should not happen
        }

        if (graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) parameterComponent;
            textField.setText(defaultValue.toString());
        } else if (graphicalType.equals(JSlider.class)) {
            JSlider slider = (JSlider) parameterComponent;
            slider.setValue(defaultValue);
        }  else if (graphicalType.equals(JSpinner.class)) {
            JSpinner spinner = (JSpinner) parameterComponent;
            spinner.setValue(defaultValue);
        }
    }
    
    @Override
    public ParameterError checkParameter() {
        
        Integer value = null;
        
        if (graphicalType.equals(JTextField.class)) {
            JTextField textField = (JTextField) parameterComponent;
            try {
                value = Integer.parseInt(textField.getText());
            } catch (NumberFormatException nfe) {
                return new ParameterError(name+" is  not a Integer", parameterComponent);
            }
        } else if (graphicalType.equals(JSlider.class)) {
            // with a slider, there can be no error
            return null;
        } else if (graphicalType.equals(JSpinner.class)) {
            // with a slider, there can be no error
            return null;
        }
        
        if (minValue != null) {
            if (value < minValue) {
                return new ParameterError(name+" must be greater than "+minValue.toString(), parameterComponent);
            }
        }
        
        if (maxValue != null) {
            if (value > maxValue) {
                return new ParameterError(name+" must be lesser than "+maxValue.toString(), parameterComponent);
            }
        }
        
        return null;
    }
    
    
}
