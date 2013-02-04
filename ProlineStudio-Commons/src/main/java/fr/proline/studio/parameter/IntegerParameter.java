package fr.proline.studio.parameter;

import java.awt.FlowLayout;
import javax.swing.*;

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
    public JComponent getComponent() {




        if (graphicalType.equals(JTextField.class)) {

            // --- TextField ---

            JTextField textField = new JTextField(30);
            if (defaultValue != null) {
                textField.setText(defaultValue.toString());
            }
            parameterComponent = textField;
            return textField;
        } else if (graphicalType.equals(JSlider.class)) {

            // --- Slider ---

            JPanel sliderPanel = new JPanel(new FlowLayout());

            final JSlider slider = new JSlider(minValue, maxValue, defaultValue);
            slider.setPaintTicks(true);
            final JTextField textField = new JTextField(3);
            textField.setText(String.valueOf(defaultValue));

            slider.addChangeListener(new javax.swing.event.ChangeListener() {

                @Override
                public void stateChanged(javax.swing.event.ChangeEvent evt) {
                    int v = (int) slider.getValue();
                    textField.setText(String.valueOf(v));
                }
            });

            textField.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try {
                        int value = Integer.parseInt(textField.getText());
                        slider.setValue(value);
                    } catch (NumberFormatException e) {
                    }
                }
            });

            sliderPanel.add(slider);
            sliderPanel.add(textField);

            parameterComponent = slider;

            return sliderPanel;
        } else if (graphicalType.equals(JSpinner.class)) {

            // --- Spinner ---

            JSpinner spinner = new JSpinner();
            SpinnerNumberModel model = new SpinnerNumberModel(defaultValue, minValue, maxValue, new Integer(1));
            spinner.setModel(model);

            parameterComponent = spinner;

            return spinner;
        }


        return null;
    }
}
