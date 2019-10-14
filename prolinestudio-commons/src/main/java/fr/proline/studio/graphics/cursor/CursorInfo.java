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
package fr.proline.studio.graphics.cursor;

import java.awt.BasicStroke;
import java.awt.Color;

/**
 *
 * Information on a cursor : value, strokke, color, selectables...
 * 
 * @author JM235353
 */
public class CursorInfo {

    private final double m_value;
    
    private BasicStroke m_stroke = null;
    private Color m_color = null;
    private Boolean m_selectable = null;
    
    public CursorInfo(double v) {
        m_value = v;
    }
    
    public void applyParametersToCursor(AbstractCursor cursor) {
        if (m_color != null) {
            cursor.setColor(m_color);
        }
        if (m_stroke != null) {
            cursor.setStroke(m_stroke);
        }
        if (m_selectable != null) {
            cursor.setSelectable(m_selectable);
        }
    }
    
    public void setStroke(BasicStroke stroke) {
        m_stroke = stroke;
    }
    
    public void setColor(Color color) {
        m_color = color;
    }
    
    public void setSelectable(Boolean selectable) {
        m_selectable = selectable;
    }
    
    public double getValue() {
        return m_value;
    }
}
