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

import fr.proline.studio.graphics.ColorOrGradientChooserPanel;
import fr.proline.studio.graphics.ColorOrGradient;
import fr.proline.studio.graphics.ReferenceIdName;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JComponent;

/**
 * Parameter to select a color or a gradient of colors
 * @author JM235353
 */
public class ColorOrGradientParameter extends AbstractParameter {

    private ColorOrGradient m_defaultColor = null;
    private ArrayList<ReferenceIdName> m_gradientParam = null;
    
    private ColorOrGradient m_startValue;
    
    private boolean m_edited = false;
    
    public ColorOrGradientParameter(String key, String name, ColorOrGradient defaultColor, ArrayList<ReferenceIdName> gradientParam) {
        super(key, name, Color.class, ColorOrGradientChooserPanel.class);
        m_defaultColor = defaultColor;
        m_gradientParam = gradientParam;
    }

    public void setGradientParam(ArrayList<ReferenceIdName> gradientParam) {
        m_gradientParam = gradientParam;
    }
    
    @Override
    public JComponent getComponent(Object value) {

        m_startValue = (ColorOrGradient) value;

        if (m_startValue == null) {
            m_startValue = m_defaultColor;
        }

        if (m_parameterComponent !=null) {
            if (m_graphicalType.equals(ColorOrGradientChooserPanel.class)) {
                ((ColorOrGradientChooserPanel) m_parameterComponent).setColor(m_startValue);
                return m_parameterComponent;
            }
        }

        if (m_graphicalType.equals(ColorOrGradientChooserPanel.class)) {

            ColorOrGradientChooserPanel colorGradientChooser = new ColorOrGradientChooserPanel(m_startValue, m_gradientParam);

            m_parameterComponent = colorGradientChooser;
            return colorGradientChooser;
        }


        return null;
    }

    @Override
    public void initDefault() {
        if (m_defaultColor == null) {
            return; // should not happen
        }

        if (m_graphicalType.equals(ColorOrGradientChooserPanel.class)) {
            ColorOrGradientChooserPanel colorGradientChooser = (ColorOrGradientChooserPanel) m_parameterComponent;
            colorGradientChooser.setColor(m_defaultColor);
        }
    }

    @Override
    public ParameterError checkParameter() {
        return null;
    }

    @Override
    public String getStringValue() {
        return ((ColorOrGradient) getObjectValue()).toString();
    }

    @Override
    public Object getObjectValue() {
        if (m_graphicalType.equals(ColorOrGradientChooserPanel.class) && (m_parameterComponent != null)) {
           return ((ColorOrGradientChooserPanel) m_parameterComponent).getColor();
        }
        return null; // should not happen
    }

    public ColorOrGradient getColor() {
        ColorOrGradient c = (ColorOrGradient) getObjectValue();
        if (c == null) {
            return m_defaultColor;
        }
        return c;
    }
    
    public ReferenceIdName getSelectedReferenceIdName() {
        if (m_graphicalType.equals(ColorOrGradientChooserPanel.class) && (m_parameterComponent != null)) {
           return ((ColorOrGradientChooserPanel) m_parameterComponent).getSelectedReferenceIdName();
        }
        return null;
    }
    
    @Override
    public void setValue(String v) {
        
        if (m_parameterComponent == null) {
            return; // should not happen
        }
        if (m_graphicalType.equals(ColorOrGradientChooserPanel.class)) {
            ((ColorOrGradientChooserPanel) m_parameterComponent).setColor(ColorOrGradient.read(v));
        }
        
        if(v!=null && ColorOrGradient.read(v)!=null && m_startValue!=null){
            m_edited = ColorOrGradient.read(v) != m_startValue;
        }
        
    }

    @Override
    public boolean isEdited() {
        return m_edited;
    }

    
    
}
