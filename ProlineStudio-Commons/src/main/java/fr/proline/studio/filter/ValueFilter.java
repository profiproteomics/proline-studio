package fr.proline.studio.filter;

import java.awt.Component;
import java.awt.GridBagConstraints;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;


/**
 * Filter with a combobox to filter values
 * @author JM235353
 */
public class ValueFilter extends Filter {

    private static final Integer VALUE = 0;
    
    private Object[] m_values = null;
    private ImageIcon[] m_displayIcons = null;
    private int m_index = 0;

    private ValueFilterType m_type;
    
    public enum ValueFilterType {
        GREATER_EQUAL,
        EQUAL
    }
    
    public ValueFilter(String variableName, Object[] values, ImageIcon[] displayIcons, ValueFilterType type ) {
        super(variableName);
        
        m_values = values;
        m_displayIcons = displayIcons;
        m_type = type;
    }

    public boolean filter(int index) {

        if (m_type == ValueFilterType.GREATER_EQUAL) {
            return index >= m_index;
        } else {
            return m_index == index;
        }

    }

    @Override
    public FilterStatus checkValues() {
        return null;
    }

    @Override
    public void registerValues() {

        if (isDefined()) {
            
            m_index = ((JComboBox) getComponent(VALUE)).getSelectedIndex();
        }

        registerDefinedAsUsed();
    }



    @Override
    public void createComponents(JPanel p, GridBagConstraints c) {
        c.gridx++;
        c.gridwidth = 3;
        c.weightx = 1;
        JLabel nameLabel = new JLabel(getName());

        p.add(nameLabel, c);

        c.gridx += 3;
        c.gridwidth = 1;
        c.weightx = 0;
        if (m_type == ValueFilterType.GREATER_EQUAL) {
            p.add(new JLabel(">="), c);
        } else {
            p.add(new JLabel(":"), c);
        }
        c.gridx++;
        c.gridwidth = 3;
        c.weightx = 1;
        JComboBox vCombobox = ((JComboBox) getComponent(VALUE));
        if (vCombobox == null) {
            vCombobox = new JComboBox(m_values);
            vCombobox.setSelectedIndex(m_index);
            registerComponent(VALUE, vCombobox);
            if (m_displayIcons != null) {
                vCombobox.setRenderer(new IconValueRenderer()); 
            }
        }
        p.add(vCombobox, c);

        c.gridx += 2;

    }

    @Override
    public void reset() {
        m_index = -1;
    }
    
    
    private class IconValueRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if ((m_displayIcons != null) && (index>=0)) {
                l.setIcon(m_displayIcons[index]);
            } else {
                l.setIcon(null);
            }

            return l;
        }

    }
    
}

