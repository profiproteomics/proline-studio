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

import fr.proline.studio.graphics.marker.AbstractMarker;
import java.awt.Color;

/**
 *
 * @author JM235353
 */
public class GraphicDataGroup {
    private final String m_groupName;
    private final Color m_color;
    private AbstractMarker m_associatedMarker = null;
    
    public GraphicDataGroup(String name, Color c) {
        m_groupName = name;
        m_color = c;
    }
    
    public String getName() {
        return m_groupName;
    }
    
    public Color getColor() {
        return m_color;
    }
    
    public void setAssociatedMarker(AbstractMarker associatedMarker) {
        m_associatedMarker = associatedMarker;
    }
    
    public AbstractMarker getAssociatedMarker() {
        return m_associatedMarker;
    }
    
}
