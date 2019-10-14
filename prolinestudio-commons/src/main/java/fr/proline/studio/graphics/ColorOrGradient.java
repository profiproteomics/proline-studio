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
package fr.proline.studio.graphics;

import java.awt.Color;
import java.awt.LinearGradientPaint;

/**
 *
 * @author JM235353
 */
public class ColorOrGradient {

    private boolean m_colorSelected = true;
    private Color m_color = null;
    private LinearGradientPaint m_gradient = null;
    
    

    public ColorOrGradient() {

    }

    public void setColorSelected() {
        m_colorSelected = true;
    }

    public boolean isColorSelected() {
        return m_colorSelected;
    }

    public void setGradientSelected() {
        m_colorSelected = false;
    }

    public boolean isGradientSelected() {
        return m_colorSelected;
    }

    public void setColor(Color color) {
        m_color = color;
    }

    public Color getColor() {
        return m_color;
    }

    public void setGradient(LinearGradientPaint gradient) {
        m_gradient = gradient;
    }

    public LinearGradientPaint getGradient() {
        return m_gradient;
    }

    @Override
    public String toString() {
        /*StringBuilder sb = new StringBuilder();
         sb.append(m_colorSelected ? "0" : "1");
         if ()*/
        //JPM.TODO
        return "";
    }

    public static ColorOrGradient read(String s) {
        //JPM.TODO
        return new ColorOrGradient();
    }
}
