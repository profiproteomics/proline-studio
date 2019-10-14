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
package fr.proline.studio.pattern;

import fr.proline.studio.mzscope.MzScopeInterface;
import fr.proline.studio.rsmexplorer.DataBoxViewerTopComponent;

/**
 *
 * @author MB243701
 */
public class MzScopeWindowBoxManager {
    
    private static WindowBox m_windowBox = null;
    private static DataBoxViewerTopComponent m_win = null;
    
    public static void addMzdbScope(MzScopeInterface mzScopeInterface) {
        if (m_windowBox == null) {
            m_windowBox = WindowBoxFactory.getMzScopeWindowBox();
            m_windowBox.setEntryData(-1, mzScopeInterface);
            
            // open a window to display the window box
            m_win = new DataBoxViewerTopComponent(m_windowBox);
            m_win.open();
            m_win.requestActive();
        } else {
            m_windowBox.setEntryData(-1, mzScopeInterface);
            if (m_win.isOpened()) {
                m_win.requestActive();
            } else {
                m_win = new DataBoxViewerTopComponent(m_windowBox);
                m_win.open();
                m_win.requestActive();
            }
        }
    }
    
}
