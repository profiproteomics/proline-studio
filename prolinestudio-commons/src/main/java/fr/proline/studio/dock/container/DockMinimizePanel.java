/*
 * Copyright (C) 2019
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

package fr.proline.studio.dock.container;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class DockMinimizePanel extends JPanel {

    HashMap<DockContainer, JLabel> m_componentMap = new HashMap<>();

    public DockMinimizePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setBackground(Color.white);

        setVisible(false);
    }

    public void add(DockContainer component) {

        JLabel l = new JLabel(component.getTitle());

        add(l);


        m_componentMap.put(component, l);

        if (!isVisible()) {
            setVisible(true);
        }

        revalidate();
        repaint();
    }

    public void remove(DockContainer component) {

        JLabel l = m_componentMap.remove(component);

        remove(l);

        if (m_componentMap.isEmpty()) {
            setVisible(false);
        }
    }


}
