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
package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JPopupMenu;

/**
 *
 * @author JM235353
 */
public abstract class AbstractGraphObject {
            
    protected static final BasicStroke STROKE_SELECTED = new BasicStroke(4);
    protected static final BasicStroke STROKE_NOT_SELECTED = new BasicStroke(2);
    
    protected static Font m_font = null;
    protected static Font m_fontBold = null;
    
    protected static int m_hgtBold;
    protected static int m_hgtPlain;
    protected static int m_ascentBold;
    
    protected boolean m_selected = false;
    protected boolean m_hightlighted = false;
    
    protected TypeGraphObject m_type;
    
    public enum TypeGraphObject {
        GRAPH_NODE,
        CONNECTOR,
        LINK,
        GRAPH_NODE_ACTION,
        GROUP
    };
    
    public AbstractGraphObject(TypeGraphObject type) {
        m_type = type;
    }
   
    public TypeGraphObject getType() {
        return m_type;
    }

    
    public abstract void draw(Graphics g);
    
    public abstract AbstractGraphObject inside(int x, int y);
    public abstract void move(int dx, int dy);
    public int correctMoveX(int dx) {
        return dx;
    }
    public int correctMoveY(int dy) {
        return dy;
    }
    
    public abstract void delete();

    public void setSelected(boolean s) {
        m_selected = s;
    }
    
    public boolean isSelected() {
        return m_selected;
    }
    
    public boolean setHighlighted(boolean h) {
        if (h ^ m_hightlighted) {
            m_hightlighted = h;
            return true;
        }
        return false;
    }
    
    public boolean isHighlighted() {
        return m_hightlighted;
    }
    
    
    
    public abstract JPopupMenu createPopup(final GraphPanel panel);

    
    public abstract String getTooltip(int x, int y);
}
