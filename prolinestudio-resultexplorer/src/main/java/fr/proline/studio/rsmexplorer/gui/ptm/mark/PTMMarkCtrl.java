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
package fr.proline.studio.rsmexplorer.gui.ptm.mark;

import fr.proline.studio.rsmexplorer.gui.ptm.PTMMark;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewContext;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

/**
 * PTM control & data manager
 *
 * @author Karine XUE
 */
public class PTMMarkCtrl {

    PTMMarkSetView m_view;
    Map<Integer, PTMMarkView> m_ptmMarkViewMap;

    public PTMMarkCtrl() {
        m_view = new PTMMarkSetView();
        m_ptmMarkViewMap = new HashMap();

    }

    public void setData(Map<Integer, PTMMark> ptmMarks) {
        this.setPTM(ptmMarks);
        m_view.setPtmMarkMap(m_ptmMarkViewMap);
    }

    public void setBeginPoint(int x, int y) {
        this.m_view.setBeginPoint(x, y);
    }

    public void paint(Graphics2D g2, ViewContext viewContext) {
        this.m_view.paint(g2, viewContext);
    }

    public String getToolTipText(int x, int y) {
        return this.m_view.getToolTipText(x, y);

    }


    public void setPTM(Map<Integer, PTMMark> ptmMarks) {
        m_ptmMarkViewMap = new HashMap();
        for (int i : ptmMarks.keySet()) {
            PTMMark pa = ptmMarks.get(i);
            PTMMarkView p = new PTMMarkView(pa);
            m_ptmMarkViewMap.put(i, p);
        }
    }
}
