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

import javax.swing.JComponent;

/**
 * Parameter used only to display extra info in a Parameter Panel
 * @author JM235353
 */
public class DisplayStubParameter extends AbstractParameter {

    private JComponent m_displayComponent = null;
    
    public DisplayStubParameter(String name, JComponent displayComponent) {
        super(null, name, null, displayComponent.getClass());
        m_displayComponent = displayComponent;
    }
    
    
    @Override
    public JComponent getComponent(Object value) {
        return m_displayComponent;
    }

    @Override
    public void initDefault() {
        // nothing to do
    }

    @Override
    public ParameterError checkParameter() {
        // nothing to do
        return null;
    }

    @Override
    public String getStringValue() {
        // nothing to do
        return null;
    }

    @Override
    public Object getObjectValue() {
        // nothing to do
        return null;
    }

    @Override
    public void setValue(String v) {
        // nothing to do
    }
    
    @Override
    public LabelVisibility showLabel() {
        return LabelVisibility.AS_BORDER_TITLE;
    }

    @Override
    public boolean isEdited() {
        return true;
    }
}
