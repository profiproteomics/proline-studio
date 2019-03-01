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
