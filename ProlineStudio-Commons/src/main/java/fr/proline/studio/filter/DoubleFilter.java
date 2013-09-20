package fr.proline.studio.filter;

import java.awt.GridBagConstraints;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Filter on float or double values
 * @author JM235353
 */
public class DoubleFilter extends Filter {
    
    private static final Integer VALUE_MIN = 0;
    private static final Integer VALUE_MAX = 1;
    
    private Double m_min;
    private Double m_max;
    
    public DoubleFilter(String variableName) {
        super(variableName);
        
        m_valueKeys = new ArrayList<>(2);
        m_valueKeys.add(VALUE_MIN);
        m_valueKeys.add(VALUE_MAX);
    }
    
    @Override
    public FilterStatus checkValues() {

        Double min;
        String minValue = ((JTextField) getComponent(VALUE_MIN)).getText().trim();
        if ((minValue == null) || (minValue.length() == 0)) {
            min = null;
        } else {
            try {
                min = Double.parseDouble(minValue);
            } catch (NumberFormatException nfe) {
                return new FilterStatus("Min Value is  not a Number", m_components.get(VALUE_MIN));
            }
        }
        
        Double max;
        String maxValue = ((JTextField) getComponent(VALUE_MAX)).getText().trim();
        if ((maxValue == null) || (maxValue.length() == 0)) {
            max = null;
        } else {
            try {
                max = Double.parseDouble(maxValue);
            } catch (NumberFormatException nfe) {
                return new FilterStatus("Max Value is  not a Number", m_components.get(VALUE_MAX));
            }
        }
        
        if ((min != null) && (max != null) && (min>max)) {
            return new FilterStatus("Min Value is greater than Max Value", m_components.get(VALUE_MIN));
        }
        
        return null;
    }
    
    @Override
    public void registerValues() {
        
        if (isDefined()) {
            String minValue = ((JTextField) getComponent(VALUE_MIN)).getText().trim();
            if ((minValue == null) || (minValue.length() == 0)) {
                m_min = null;
            } else {
                try {
                    m_min = Double.parseDouble(minValue);
                } catch (NumberFormatException nfe) {
                    // should never happen
                }

                String maxValue = ((JTextField) getComponent(VALUE_MAX)).getText().trim();
                if ((maxValue == null) || (maxValue.length() == 0)) {
                    m_max = null;
                } else {
                    try {
                        m_max = Double.parseDouble(maxValue);
                    } catch (NumberFormatException nfe) {
                        // should never happen
                    }
                }

            }
        }
        
        registerDefinedAsUsed();
    }
    
    /*public void setMin(Double min) {
        m_min = min;
    }
    
    public void setMax(Double max) {
        m_max = max;
    }*/
    
    public boolean filter(double value) {
        if (m_min != null) {
            if (value<m_min.doubleValue()) {
                return false;
            }
        }
        if (m_max != null) {
            if (value>m_max.doubleValue()) {
                return false;
            }
        }
        return true;
        
    }

    
    
    @Override
    public void createComponents(JPanel p, GridBagConstraints c) {
        c.gridx++;
        c.gridwidth = 1;
        c.weightx = 1;
        JTextField minTextField = new JTextField(4);
        p.add(minTextField, c);
        if (m_min != null) {
            minTextField.setText(m_min.toString());
        }
        registerComponent(VALUE_MIN, minTextField);

        c.gridx++;
        c.gridwidth = 1;
        c.weightx = 0;
        p.add(new JLabel("<="), c);

        c.gridx++;
        c.gridwidth = 3;
        c.weightx = 1;
        JLabel nameLabel = new JLabel(getName());
        nameLabel.setHorizontalAlignment(JLabel.CENTER);
        p.add(nameLabel, c);

        c.gridx += 3;
        c.gridwidth = 1;
        c.weightx = 0;
        p.add(new JLabel("<="), c);

        c.gridx++;
        c.gridwidth = 1;
        c.weightx = 1;
        JTextField maxTextField = new JTextField(4);
        p.add(maxTextField, c);
        if (m_max != null) {
            maxTextField.setText(m_max.toString());
        }
        registerComponent(VALUE_MAX, maxTextField);
    }
    
    @Override
    public void reset() {
        m_min = null;
        m_max = null;
    }
    
}
