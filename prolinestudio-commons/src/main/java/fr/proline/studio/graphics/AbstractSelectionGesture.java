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

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Path2D;

/**
 *
 * @author JM235353
 */
public abstract class AbstractSelectionGesture  {
    public static final int ACTION_NONE = 0;
    public static final int ACTION_CLICK = 1;
    public static final int ACTION_SURROUND = 2;

    protected boolean m_isSelecting = false;
    protected int m_action = ACTION_NONE;

    protected static final int MIN_SURROUND_DELTA = 10;
    
    protected int m_minX;
    protected int m_maxX;
    protected int m_minY;
    protected int m_maxY;
    
    public AbstractSelectionGesture() {
    }

    public boolean isSelecting() {
        return m_isSelecting;
    }
    
    public abstract void startSelection(int x, int y);
    public abstract void continueSelection(int x, int y);
    
    public abstract void stopSelection(int x, int y);

    
    public abstract void paint(Graphics g);
    
    public int getAction() {
        return m_action;
    }

    
    public abstract Point getClickPoint();
    
    public abstract Path2D.Double getSelectionPath();
}
