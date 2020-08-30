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

package fr.proline.studio.dock;

import fr.proline.studio.dock.container.DockComponent;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractTopPanel extends JPanel {

    private boolean m_openedOnce = false;


    public abstract String getTopPanelIdentifierKey();

    public abstract Image getIcon();

    public abstract String getTitle();


    public void componentAdded() {
        if (m_openedOnce == false) {
            m_openedOnce = true;
            componentOpened();
        }
    }

    public Action[] getActions(DockComponent component) {
        return null;
    }

    protected abstract void componentOpened();

    public abstract void componentClosed();


}
