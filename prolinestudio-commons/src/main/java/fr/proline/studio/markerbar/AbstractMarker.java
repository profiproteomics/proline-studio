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
package fr.proline.studio.markerbar;

/**
 * Base Class for Markers
 * @author JM235353
 */
public abstract class AbstractMarker {

    private boolean m_visibleInMarkerBar;
    private boolean m_visibleInOverviewBar;
    private int m_row = -1;
    private int m_type = -1;

    public AbstractMarker(int row, boolean visibleInMarkerBar, boolean visibleInOverviewBar, int type) {
        m_row = row;
        m_visibleInMarkerBar = visibleInMarkerBar;
        m_visibleInOverviewBar = visibleInOverviewBar;
        m_type = type;
    }

    public boolean isVisibleInMarkerBar() {
        return m_visibleInMarkerBar;
    }

    public boolean isVisibleInOverviewBar() {
        return m_visibleInOverviewBar;
    }

    public int getRow() {
        return m_row;
    }

    
    public int getType() {
        return m_type;
    }
}
