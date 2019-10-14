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

import java.util.ArrayList;
import javax.swing.JComponent;

/**
 * Generic Parameter which can be used by any object which implements ComponentParameterInterface
 * which embeds a graphical component to return values as parameter.
 * Example : a scatter plot with cursors to choose values.
 * @author JM235353
 */
public class ValuesFromComponentParameter extends AbstractParameter {

    private ArrayList<AbstractLinkedParameters> m_linkedParametersList = null;
    
    private ComponentParameterInterface m_componentParameterInterface;
    
    private boolean m_edited = false;
    
    public ValuesFromComponentParameter(String key, String name, ComponentParameterInterface componentParameterInterface) {
        super(key, name, Object.class, ComponentParameterInterface.class);

        m_componentParameterInterface = componentParameterInterface;
    }

    @Override
    public JComponent getComponent(Object value) {

        if (m_parameterComponent == null) {
           
            m_parameterComponent = m_componentParameterInterface.getComponent();
                
        }
        
        // no default value to set
        
        return m_parameterComponent;

    }
    
    @Override
    public void initDefault() {
        // no default value to set
    }

    @Override
    public ParameterError checkParameter() {
        
        if (!m_used && !m_compulsory) {
            return null;
        }
        
        return m_componentParameterInterface.checkParameter();
    }
    
    @Override
    public void setValue(String v) {
        // not used
    }
    

    @Override
    public String getStringValue() {
        return m_componentParameterInterface.getStringValue();
    }

    @Override
    public Object getObjectValue() {
        return m_componentParameterInterface.getObjectValue();
    }
 
    
    public void addLinkedParameters(final AbstractLinkedParameters linkedParameters) {

        if (m_linkedParametersList == null) {
            m_linkedParametersList = new ArrayList<>(1);
        }
        m_linkedParametersList.add(linkedParameters);
    }
    
    public void valueChanged(Object o) {
        if (m_linkedParametersList == null) {
            return;
        }
        for (AbstractLinkedParameters linkedParameter : m_linkedParametersList) {
            linkedParameter.valueChanged(getStringValue(), getObjectValue());
        }
    }

    @Override
    public boolean isEdited() {
        return true;
    }
    
}
