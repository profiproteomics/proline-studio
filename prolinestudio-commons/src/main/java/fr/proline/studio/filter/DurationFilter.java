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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Filter on Duration values Specific for ServerLog
 *
 */
public class DurationFilter extends LongFilter {

    private static final Integer VALUE_MIN = 0;
    private static final Integer VALUE_MAX = 1;
    private Long m_min;
    private Long m_max;

    public DurationFilter(String variableName, ConvertValueInterface convertValueInterface, int modelColumn) {
        super(variableName, convertValueInterface, modelColumn);
    }

    @Override
    public Filter cloneFilter4Search() {
        DurationFilter clone = new DurationFilter(m_variableName, m_convertValueInterface, m_modelColumn);
        clone.m_min = m_min;
        clone.m_max = m_max;
        setValuesForClone(clone);
        return clone;
    }

    @Override
    public boolean filter(Object v1, Object v2) {

        long value = (Long) v1;

        if (m_min != null) {
            if (value < m_min.longValue()) {
                return false;
            }
        }
        if (m_max != null) {
            if (value > m_max.longValue()) {
                return false;
            }
        }
        return true;

    }

    public static String formatDurationInHour(long duration) {
        int second = 1000, minute = 60000, hour = 3600000;

        String time = String.format("%d:%02d:%02d.%03d",
                duration / hour,
                (duration % hour) / minute,
                (duration % minute) / second,
                (duration % second));
        return time;
    }

    private Long getTextFieldValue(String content) {
        int h = 3600000;
        int m = 60000;
        int s = 1000;
        long duration = 0;
        final String regex = "(\\d+):([0-5][0-9]):([0-5][0-9])\\.(\\d\\d\\d)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                int v = Integer.parseInt(matcher.group(i));
                switch (i) {
                    case 1://hour
                        duration += v * 3600000;
                        break;
                    case 2:
                        duration += v * 60000;
                        break;
                    case 3:
                        duration += v * 1000;
                        break;
                    case 4:
                        duration += v;
                        break;
                }

            }
            return duration;
        } else {
            final String regexSimple = "(\\d+)([HhMmSs])?";
            pattern = Pattern.compile(regexSimple);
            matcher = pattern.matcher(content);
            if (matcher.find()) {
                int vs = Integer.parseInt(matcher.group(1));
                String type = matcher.group(2);
                if ((type) == null) {
                    duration = vs;
                } else if (type.equalsIgnoreCase("h")) {
                    duration = vs * h;
                } else if (type.equalsIgnoreCase("m")) {
                    duration = vs * m;
                } else if (type.equalsIgnoreCase("s")) {
                    duration = vs * s;
                }
                return duration;
            } else {
                return null;
            }
        }

    }

    @Override
    public FilterStatus checkValues() {
        Long min;
        String minValue = ((JTextField) getComponent(VALUE_MIN)).getText().trim();
        if ((minValue == null) || (minValue.length() == 0)) {
            min = null;
        } else {
            try {
                min = getTextFieldValue(minValue);
                if (min == null) {
                    return new FilterStatus("Min Value is  not a duration in (HHHH:mm:ss.SSS) format, or in (\\d+)h |(\\d+)m (\\d+)s ", m_components.get(VALUE_MIN));
                }
            } catch (Exception nfe) {
                return new FilterStatus("Min Value is  not a duration in (HHHH:mm:ss.SSS) format, or in (\\d+)h |(\\d+)m (\\d+)s ", m_components.get(VALUE_MIN));
            }
        }

        Long max;
        String maxValue = ((JTextField) getComponent(VALUE_MAX)).getText().trim();
        if ((maxValue == null) || (maxValue.length() == 0)) {
            max = null;
        } else {
            try {
                max = getTextFieldValue(maxValue);
                if (max == null) {
                    return new FilterStatus("Max Value is  not a duration in (HHHH:mm:ss.SSS) format, or in (\\d+)h |(\\d+)m (\\d+)s ", m_components.get(VALUE_MAX));
                }
            } catch (Exception nfe) {
                return new FilterStatus("Max Value is  not a duration in (HHHH:mm:ss.SSS) format, or in (\\d+)h |(\\d+)m (\\d+)s ", m_components.get(VALUE_MAX));
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

            Long lastMinValue = m_min;
            Long lastMaxValue = m_max;

            String minValue = ((JTextField) getComponent(VALUE_MIN)).getText().trim();
            if ((minValue == null) || (minValue.length() == 0)) {
                m_min = null;
            } else {
                try {
                    m_min = getTextFieldValue(minValue);
                } catch (Exception nfe) {

                }
            }

            String maxValue = ((JTextField) getComponent(VALUE_MAX)).getText().trim();
            if ((maxValue == null) || (maxValue.length() == 0)) {
                m_max = null;
            } else {
                try {
                    m_max = getTextFieldValue(maxValue);
                } catch (Exception nfe) {
                    // should never happen
                }
            }

            if (((lastMinValue == null) && (m_min != null)) || ((lastMinValue != null) && (m_min == null))) {
                hasChanged = true;
            } else if (((lastMaxValue == null) && (m_max != null)) || ((lastMaxValue != null) && (m_max == null))) {
                hasChanged = true;
            } else if ((lastMinValue != null) && (lastMinValue.longValue() != m_min.longValue())) {
                hasChanged = true;
            } else if ((lastMaxValue != null) && (lastMaxValue.longValue() != m_max.longValue())) {
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
            minTextField = new JTextField(10);
            if (m_min != null) {
                minTextField.setText(formatDurationInHour(m_min));
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
            maxTextField = new JTextField(10);
            if (m_max != null) {
                maxTextField.setText(formatDurationInHour(m_max));
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
