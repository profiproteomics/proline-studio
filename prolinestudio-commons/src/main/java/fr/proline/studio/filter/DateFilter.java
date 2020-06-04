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
package fr.proline.studio.filter;

import java.awt.GridBagConstraints;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Filter on Date values, used by ServerLog
 *
 * @author kx257079
 */
public class DateFilter extends Filter {

    private static final Integer VALUE_MIN = 0;
    private static final Integer VALUE_MAX = 1;
    private Date m_min;
    private Date m_max;
    private SimpleDateFormat m_dateFormat;
    private SimpleDateFormat m_alterDateFormat = new SimpleDateFormat("dd MMM yyyy");

    public DateFilter(String variableName, ConvertValueInterface convertValueInterface, int modelColumn) {
        super(variableName, convertValueInterface, modelColumn);
        m_dateFormat = new SimpleDateFormat("HH:mm:ss.SSS - dd MMM yyyy");
    }

    public DateFilter(SimpleDateFormat dateFormat, String variableName, ConvertValueInterface convertValueInterface, int modelColumn) {
        super(variableName, convertValueInterface, modelColumn);
        m_dateFormat = dateFormat;

    }

    @Override
    public Filter cloneFilter4Search() {
        DateFilter clone = new DateFilter(m_dateFormat, m_variableName, m_convertValueInterface, m_modelColumn);
        clone.m_dateFormat = m_dateFormat;
        clone.m_min = m_min;
        clone.m_max = m_max;
        setValuesForClone(clone);
        return clone;
    }

    @Override
    public boolean filter(Object v1, Object v2) {
        Date value = (Date) v1;

        if (m_min != null) {
            if (value.getTime() < m_min.getTime()) {
                return false;
            }
        }
        if (m_max != null) {
            if (value.getTime() > m_max.getTime()) {
                return false;
            }
        }
        return true;

    }

    @Override
    public FilterStatus checkValues() {
        Date min;
        String minValue = ((JTextField) getComponent(VALUE_MIN)).getText().trim();
        if ((minValue == null) || (minValue.length() == 0)) {
            min = null;
        } else {
            try {
                min = parseTime(minValue);
            } catch (ParseException ex) {
                return new FilterStatus("Min Value is  not a Date", m_components.get(VALUE_MIN));
            }
        }

        Date max;
        String maxValue = ((JTextField) getComponent(VALUE_MAX)).getText().trim();
        if ((maxValue == null) || (maxValue.length() == 0)) {
            max = null;
        } else {
            try {
                max = parseTime(maxValue);
            } catch (ParseException ex) {
                return new FilterStatus("Max Value is  not a Date", m_components.get(VALUE_MAX));
            }
        }

        if ((min != null) && (max != null) && (min.getTime() > max.getTime())) {
            return new FilterStatus("Min Value is greater than Max Value", m_components.get(VALUE_MIN));
        }

        return null;
    }

    /**
     * reverse of formatTime
     *
     * @param time
     * @return
     */
    private Date parseTime(String time) throws ParseException {
        try {
            return m_dateFormat.parse(time.trim());
        } catch (ParseException ex) {
            try {
                return m_alterDateFormat.parse(time.trim());
            } catch (ParseException ex1) {
                throw ex1;
            }
        }
    }

    @Override
    public boolean registerValues() {

        boolean hasChanged = false;

        if (isDefined()) {

            Date lastMinValue = m_min;
            Date lastMaxValue = m_max;

            String minValue = ((JTextField) getComponent(VALUE_MIN)).getText().trim();
            if ((minValue == null) || (minValue.length() == 0)) {
                m_min = null;
            } else {
                try {
                    m_min = parseTime(minValue);
                } catch (ParseException ex) {
                    m_min = null;
                }
            }

            String maxValue = ((JTextField) getComponent(VALUE_MAX)).getText().trim();
            if ((maxValue == null) || (maxValue.length() == 0)) {
                m_max = null;
            } else {
                try {
                    m_max = parseTime(maxValue);
                } catch (ParseException ex) {
                    m_max = null;
                }
            }

            if (((lastMinValue == null) && (m_min != null)) || ((lastMinValue != null) && (m_min == null))) {
                hasChanged = true;
            } else if (((lastMaxValue == null) && (m_max != null)) || ((lastMaxValue != null) && (m_max == null))) {
                hasChanged = true;
            } else if ((lastMinValue != null) && (!lastMinValue.equals(m_min))) {
                hasChanged = true;
            } else if ((lastMaxValue != null) && (!lastMaxValue.equals(m_max))) {
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
            minTextField = new JTextField(18);
            if (m_min != null) {
                minTextField.setText(m_dateFormat.format(m_min));
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
            maxTextField = new JTextField(18);
            if (m_max != null) {
                maxTextField.setText(m_dateFormat.format(m_max));
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
