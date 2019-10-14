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
package fr.proline.studio.parameter;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Base class for All Parameters
 * @author jm235353
 */
public abstract class AbstractParameter {

    protected String m_key;
    protected String m_name;
    protected Class m_type;
    protected Class m_graphicalType;
    protected JComponent m_parameterComponent = null;
    protected boolean m_used = true;
    protected boolean m_compulsory = true;
    protected Object m_associatedData = null;
    protected LabelVisibility m_labelVisibility = LabelVisibility.VISIBLE;
    protected ActionListener m_externalActionListener = null;
   
    //JP.WART : use for backward compatibility to change the name of a parameter
    protected List<String> m_backwardCompatibleKeys = new ArrayList<>(1);
    
    public enum LabelVisibility {
        NO_VISIBLE,
        VISIBLE,
        AS_BORDER_TITLE
        
        
    }
    
    protected AbstractParameter(String key, String name, Class type, Class graphicalType) {
        m_key = key;
        m_name = name;
        m_type = type;
        m_graphicalType = graphicalType;
    }

    public void setCompulsory(boolean v) {
        m_compulsory = v;
    }
    
    public String getName() {
        return m_name;
    }
    
    public String getKey() {
        return m_key;
    }

    public void addBackwardCompatibleKey(String backwardCompatibleKey) {
        m_backwardCompatibleKeys.add(backwardCompatibleKey);
    }
    
    public List<String> getBackwardCompatibleKeys() {
        return m_backwardCompatibleKeys;
    }
    
    public JComponent getComponent() {
        return m_parameterComponent;
    }
    public boolean componentNeedsScrollPane() {
        return false;
    }
    public abstract JComponent getComponent(Object value);
    public abstract void initDefault();
    
    public abstract ParameterError checkParameter();
    
    public abstract boolean isEdited();
    
    public abstract String getStringValue();
    public abstract Object getObjectValue();
    
    public abstract void setValue(String v);
    /**
     * Specify if a component should be associated to the label for this parameter. 
     * Default is true;
     * @return 
     */
    public Boolean hasComponent(){
        return true;
    }
    
    /**
     * Returns if the parameter wants its name to be displayed in the panel
     * @return 
     */
    public LabelVisibility showLabel() {
        return m_labelVisibility;
    }
    
    public void forceShowLabel(LabelVisibility labelVisibility) {
        m_labelVisibility = labelVisibility;
    }
    
    /**
     * Called when a window is reopened to be able to clean
     * some parameters (like FileParameter)
     */
    public void clean() {
        
    }
    
    public boolean isUsed() {
        return m_used;
    }
    
    public void setUsed(boolean used) {
        m_used = used;
    }
    
    public boolean isCompulsory() {
        return m_compulsory;
    }

    
    @Override
    public String toString() {
        return getName();
    }

    public void setAssociatedData(Object associatedData) {
        m_associatedData = associatedData;
    }
    
    public Object getAssociatedData() {
        return m_associatedData;
    }
    
    public void setExternalActionListener(ActionListener a) {
        m_externalActionListener = a;
    }
    
}
