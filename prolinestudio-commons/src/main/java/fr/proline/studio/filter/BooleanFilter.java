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
    
    public BooleanFilter(String variableName, ConvertValueInterface convertValueInterface, int modelColumn) {
        super(variableName, convertValueInterface, modelColumn);
    }
    
    @Override
    public Filter cloneFilter4Search() {
        BooleanFilter clone = new BooleanFilter(m_variableName, m_convertValueInterface, m_modelColumn);
        clone.m_bValue = m_bValue;
        setValuesForClone(clone);
        return clone;
    }
    
    @Override
    public FilterStatus checkValues() {
        return null;
    }

    @Override
    public boolean registerValues() {
        
        boolean hasChanged = false;
        
        if (isDefined()) {
           
            Boolean lastValue = m_bValue;
           
            m_bValue =((JComboBox) getComponent(BOOLEAN_CB)).getSelectedIndex() == 0;
            
            hasChanged = (lastValue == null) || (lastValue ^ m_bValue);
        }

        registerDefinedAsUsed();
        
        return hasChanged;
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
        c.gridx += 3;
    }

    @Override
    public void reset() {
        m_bValue = null;
    }
    
    @Override
    public boolean filter(Object v1, Object v2) {
        
        Boolean value = (Boolean) v1;
        
        if (m_bValue != null &&  value != null){
            return m_bValue.equals(value);
        }
        return true;
    }

}
