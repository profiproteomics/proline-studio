/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.filter;

import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Filter on Boolean Values
 * @author MB243701
 */
public class BooleanFilter extends Filter{

    private static final String[] BOOLEAN_FILTER_VALUES = {"Yes", "No"};
    private final static Integer BOOLEAN_CB = 0;
    private Boolean m_bValue = null;
    
    public BooleanFilter(String variableName, ConvertValueInterface convertValueInterface) {
        super(FilterType.FILTER_BOOLEAN, variableName, convertValueInterface);
    }
    
    @Override
    public FilterStatus checkValues() {
        return null;
    }

    @Override
    public void registerValues() {
        if (isDefined()) {
            m_bValue =((JComboBox) getComponent(BOOLEAN_CB)).getSelectedIndex() == 0;
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
        p.add(new JLabel("="), c);
        c.gridx++;
        c.gridwidth = 3;
        c.weightx = 1;
        JComboBox cb = ((JComboBox) getComponent(BOOLEAN_CB));
        if (cb == null) {
            cb = new JComboBox(BOOLEAN_FILTER_VALUES);
            if (m_bValue != null) {
                cb.setSelectedIndex((m_bValue ? 0 : 1));
            }
            registerComponent(BOOLEAN_CB, cb);
        }
        p.add(cb, c);
        c.gridx += 2;
    }

    @Override
    public void reset() {
        m_bValue = null;
    }
    
    public boolean filter(Boolean value) {
        if (m_bValue != null &&  value != null){
            return m_bValue.equals(value);
        }
        return true;
    }
    
}
