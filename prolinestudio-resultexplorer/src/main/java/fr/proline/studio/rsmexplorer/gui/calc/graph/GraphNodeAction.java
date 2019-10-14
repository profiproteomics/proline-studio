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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JPopupMenu;

/**
 *
 * @author JM235353
 */
public class GraphNodeAction extends AbstractGraphObject {

    private static final Color WHITE_TRANSPARENT = new Color(255, 255, 255, 128);

    public boolean m_actionSet = false;

    public Rectangle m_bounds = new Rectangle();

    public GraphNodeAction() {
        super(TypeGraphObject.GRAPH_NODE_ACTION);
    }

    public void setAction(boolean v) {
        m_actionSet = v;
        if (!m_actionSet) {
            m_hightlighted = false;
        }
    }

    public void setBounds(int x, int y, int width, int height) {
        m_bounds.x = x;
        m_bounds.y = y;
        m_bounds.width = width;
        m_bounds.height = height;
    }

    @Override
    public void draw(Graphics g) {
        if (m_hightlighted) {
            g.setColor(WHITE_TRANSPARENT);
            g.fillRoundRect(m_bounds.x, m_bounds.y, m_bounds.width, m_bounds.height, 2, 2);
        }
    }

    @Override
    public AbstractGraphObject inside(int x, int y) {
        if (!m_actionSet) {
            return null;
        }
        if (m_bounds.contains(x, y)) {
            setHighlighted(true);
            return this;
        }
        return null;
    }

    @Override
    public void move(int dx, int dy) {
        // not used
    }

    @Override
    public void delete() {
        // not used
    }

    @Override
    public JPopupMenu createPopup(GraphPanel panel) {
        return null;
    }

    @Override
    public String getTooltip(int x, int y) {
        return null;
    }
}
