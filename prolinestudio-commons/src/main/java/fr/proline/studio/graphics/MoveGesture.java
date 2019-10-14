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

/**
 *
 * @author JM235353
 */
public class MoveGesture {
    
    private MoveableInterface m_moveableObject = null;
    private int m_x;
    private int m_y;
    
    public MoveGesture() {
        
    }
    
    public void startMoving(int x, int y, MoveableInterface moveableObject) {
        m_moveableObject = moveableObject;
        m_x = x;
        m_y = y;
    }
    
    public boolean isMoving() {
        return (m_moveableObject != null);
    }
    
    public void move(int x, int y) {
        m_moveableObject.move(x-m_x, y-m_y);
        m_x = x;
        m_y = y;
    }
    
    public void stopMoving(int x, int y, boolean isCtrlOrShiftDown) {
        move(x, y);
        m_moveableObject.snapToData(isCtrlOrShiftDown);
        m_moveableObject = null;
    }
}
