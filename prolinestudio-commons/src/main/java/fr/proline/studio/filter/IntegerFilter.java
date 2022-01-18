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
package fr.proline.studio.filter;

import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Filter on int values
 *
 * @author JM235353
 */
public class IntegerFilter extends Filter {

    private static final Integer VALUE_MIN = 0;
    private static final Integer VALUE_MAX = 1;
    private Integer m_min;
    private Integer m_max;

    public IntegerFilter(String variableName, ConvertValueInterface convertValueInterface, int modelColumn) {
        super(variableName, convertValueInterface, modelColumn);
    }

    @Override
    public Filter cloneFilter4Search() {
        IntegerFilter clone = new IntegerFilter(m_variableName, m_convertValueInterface, m_modelColumn);
        clone.m_min = m_min;
        clone.m_max = m_max;
        setValuesForClone(clone);
        return clone;
    }
    
    @Override
    public boolean filter(Object v1, Object v2) {
        
        int value = (Integer) v1;
        
        if (m_min != null) {
            if (value < m_min.intValue()) {
                return false;
            }
        }
        if (m_max != null) {
            if (value > m_max.intValue()) {
                return false;
            }
        }
        return true;

    }

    @Override
    public FilterStatus checkValues() {
        Integer min;
        String minValue = ((JTextField) getComponent(VALUE_MIN)).getText().trim();
        if ((minValue == null) || (minValue.length() == 0)) {
            min = null;
        } else {
            try {
                min = Integer.parseInt(minValue);
            } catch (NumberFormatException nfe) {
                return new FilterStatus("Min Value is  not an Integer", m_components.get(VALUE_MIN));
            }
        }

        Integer max;
        String maxValue = ((JTextField) getComponent(VALUE_MAX)).getText().trim();
        if ((maxValue == null) || (maxValue.length() == 0)) {
            max = null;
        } else {
            try {
                max = Integer.parseInt(maxValue);
            } catch (NumberFormatException nfe) {
                return new FilterStatus("Max Value is  not an Integer", m_components.get(VALUE_MAX));
            }
        }

        if ((min != null) && (max != null) && (min > max)) {
            return new FilterStatus("Min Value is greater than Max Value", m_components.get(VALUE_MIN));
        }

        return null;
    }

    @Override
    public boolean registerValues() {

        boolean hasChanged = false;
        
        if (isDefined()) {
            
            Integer lastMinValue = m_min;
            Integer lastMaxValue = m_max;
            
            String minValue = ((JTextField) getComponent(VALUE_MIN)).getText().trim();
            if ((minValue == null) || (minValue.length() == 0)) {
                m_min = null;
            } else {
                try {
                    m_min = Integer.parseInt(minValue);
                } catch (NumberFormatException nfe) {
                    // should never happen
                }
            }

            String maxValue = ((JTextField) getComponent(VALUE_MAX)).getText().trim();
            if ((maxValue == null) || (maxValue.length() == 0)) {
                m_max = null;
            } else {
                try {
                    m_max = Integer.parseInt(maxValue);
                } catch (NumberFormatException nfe) {
                    // should never happen
                }
            }

            if (((lastMinValue == null) && (m_min != null)) || ((lastMinValue != null) && (m_min == null))) {
                hasChanged = true;
            } else if (((lastMaxValue == null) && (m_max != null)) || ((lastMaxValue != null) && (m_max == null))) {
                hasChanged = true;
            } else if ((lastMinValue != null) && (lastMinValue.intValue()!= m_min.intValue())) {
                hasChanged = true;
            } else if ((lastMaxValue != null) && (lastMaxValue.intValue() != m_max.intValue())) {
                hasChanged = true;
            }
        }

        registerDefinedAsUsed();
        
        return hasChanged;
    }

    @Override
    public void createComponents(JPanel p, GridBagConstraints c) {
        c.gridx++;
        c.gridwidth = 1;
        c.weightx = 1;
        JTextField minTextField = ((JTextField) getComponent(VALUE_MIN));
        if (minTextField == null) {
            minTextField = new JTextField(4);
            if (m_min != null) {
                minTextField.setText(m_min.toString());
            }
            registerComponent(VALUE_MIN, minTextField);
        }
        p.add(minTextField, c);


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
        JTextField maxTextField = ((JTextField) getComponent(VALUE_MAX));
        if (maxTextField == null) {
            maxTextField = new JTextField(4);
            if (m_max != null) {
                maxTextField.setText(m_max.toString());
            }
            registerComponent(VALUE_MAX, maxTextField);
        }
        p.add(maxTextField, c);
        c.gridx++;
    }

    @Override
    public void reset() {
        m_min = null;
        m_max = null;
    }

}
