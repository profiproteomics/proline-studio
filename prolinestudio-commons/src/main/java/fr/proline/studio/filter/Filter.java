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

import fr.proline.studio.table.LazyData;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;

/**
 * Filter base class
 * @author JM235353
 */
public abstract class Filter {

    protected ArrayList<Integer> m_valueKeys = null;
    protected HashMap<Integer, Component> m_components = null;

    protected String m_variableName;
    protected boolean m_used;
    protected boolean m_defined;

    private int m_index = -1;

    protected final int m_modelColumn;

    protected final int m_extraModelColumn;

    protected final ConvertValueInterface m_convertValueInterface;

    
    public Filter(String variableName, ConvertValueInterface convertValueInterface, int modelColumn) {
        this(variableName, convertValueInterface, modelColumn, -1);
    }

    public Filter(String variableName, ConvertValueInterface convertValueInterface, int modelColumn, int extraModelColumn) {
        m_variableName = variableName;
        m_convertValueInterface = convertValueInterface;
        m_modelColumn = modelColumn;
        m_extraModelColumn = extraModelColumn;
    }

    public void setIndex(int index) {
        m_index = index;
    }

    public int getIndex() {
        return m_index;
    }

    public abstract boolean filter(Object v1, Object v2);

    /**
     * A copy of the Filter used for search : SearchToggleButton
     * @return 
     */
    public abstract Filter cloneFilter4Search();

    public void setValuesForClone(Filter clone) {
        clone.m_used = m_used;
        clone.m_defined = m_defined;
        clone.m_valueKeys = m_valueKeys;
    }

    public int getModelColumn() {
        return m_modelColumn;
    }

    public int getExtraModelColumn() {
        return m_extraModelColumn;
    }

    public Object convertValue(Object o) {
        if (o instanceof LazyData) {
            o = ((LazyData) o).getData();
        }

        if (m_convertValueInterface == null) {
            return o;
        }
        return m_convertValueInterface.convertValue(o);
    }

    
    public abstract FilterStatus checkValues();

    
    public void registerDefinedAsUsed() {
        m_used = m_defined;
    }

    /**
     * @return if the filter has been changed since the last time
     */
    public abstract boolean registerValues();


    public ArrayList<Integer> getValueKeys() {
        return m_valueKeys;
    }

    public String getName() {
        return m_variableName;
    }

    public boolean isDefined() {
        return m_defined;
    }

    public void setDefined(boolean defined) {
        m_defined = defined;
    }

    
    public boolean isUsed() {
        return m_used;
    }

    public void setUsed(boolean used) {
        m_used = used;
    }

    /**
     * will be used by ComboBox in AdvancedSearchFloatingPanel
     * @return the name of the colomn
     */
    @Override
    public String toString() {
        return m_variableName;
    }
    
    public String toStringFull() {
        return "["+this.getClass().getName()+"] "+m_variableName+ " (isDefined: " + m_defined + ") (isUsed: " + m_used + ")";
    }

    /**
     * register one component in HashMap
     *
     * @param key
     * @param c
     */
    public void registerComponent(Integer key, Component c) {
        if (m_components == null) {
            m_components = new HashMap<>();
        }
        m_components.put(key, c);
    }

    public Component getComponent(Integer key) {
        if (m_components == null) {
            return null;
        }
        return m_components.get(key);
    }

    public void clearComponents() {
        if (m_components == null) {
            return;
        }
        m_components.clear();
    }

    /**
     * @kx
     * each filtre mananger its self panel component, and put it's panel
     * according to GridBagConstraints c at the control panel p.
     * Will be called by FilterPanel and AdvancedSearchFloatingPanel
     * inside of a Filter, GridBagConstraints c take 7 column, 
     * the c.wightx = 1 for JLabel and TextField, 
     * the c.wightx = 0 for operator (JComboBox, "=" ">" etc),
     * try to respect it.
     * @param p JPanel, contrl mother panel
     * @param c GridBagConstraints, it's layout
     */
    public abstract void createComponents(JPanel p, GridBagConstraints c);

    public abstract void reset();
}
