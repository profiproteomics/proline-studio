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
package fr.proline.studio.graphics.marker;

import fr.proline.studio.graphics.BasePlotPanel;
import java.awt.Graphics2D;

/**
 * Base class for all markers which can be plot in a BasePlotPanel
 *
 * @author JM235353
 */
public abstract class AbstractMarker implements Cloneable {

    protected BasePlotPanel m_plotPanel;
    protected boolean m_isVisible;

    public AbstractMarker(BasePlotPanel plotPanel) {
        m_plotPanel = plotPanel;
    }

    public boolean isVisible() {
        return m_isVisible;
    }

    public void setVisible(boolean m_isVisible) {
        this.m_isVisible = m_isVisible;
    }

    public abstract void paint(Graphics2D g);

    protected AbstractMarker clone() throws CloneNotSupportedException {
        return (AbstractMarker) super.clone();
    }

    public AbstractMarker clone(BasePlotPanel plotPanel) throws CloneNotSupportedException {
        AbstractMarker clone = clone();
        clone.m_plotPanel = plotPanel;
        return clone;
    }

}
