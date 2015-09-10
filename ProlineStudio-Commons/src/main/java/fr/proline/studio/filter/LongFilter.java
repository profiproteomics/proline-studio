package fr.proline.studio.filter;

import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Filter on long values
 * @author JM235353
 */
public class LongFilter extends Filter {

    private static final Integer VALUE_MIN = 0;
    private static final Integer VALUE_MAX = 1;
    private Long m_min;
    private Long m_max;

    public LongFilter(String variableName, ConvertValueInterface convertValueInterface) {
        super(FilterType.FILTER_LONG, variableName, convertValueInterface);
    }

    public boolean filter(long value) {
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

    @Override
    public FilterStatus checkValues() {
        Long min;
        String minValue = ((JTextField) getComponent(VALUE_MIN)).getText().trim();
        if ((minValue == null) || (minValue.length() == 0)) {
            min = null;
        } else {
            try {
                min = Long.parseLong(minValue);
            } catch (NumberFormatException nfe) {
                return new FilterStatus("Min Value is  not a Long", m_components.get(VALUE_MIN));
            }
        }

        Long max;
        String maxValue = ((JTextField) getComponent(VALUE_MAX)).getText().trim();
        if ((maxValue == null) || (maxValue.length() == 0)) {
            max = null;
        } else {
            try {
                max = Long.parseLong(maxValue);
            } catch (NumberFormatException nfe) {
                return new FilterStatus("Max Value is  not a Long", m_components.get(VALUE_MAX));
            }
        }

        if ((min != null) && (max != null) && (min > max)) {
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
                    m_min = Long.parseLong(minValue);
                } catch (NumberFormatException nfe) {
                    // should never happen
                }
            }

            String maxValue = ((JTextField) getComponent(VALUE_MAX)).getText().trim();
            if ((maxValue == null) || (maxValue.length() == 0)) {
                m_max = null;
            } else {
                try {
                    m_max = Long.parseLong(maxValue);
                } catch (NumberFormatException nfe) {
                    // should never happen
                }
            }
        }

        registerDefinedAsUsed();
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
    }

    @Override
    public void reset() {
        m_min = null;
        m_max = null;
    }

}
