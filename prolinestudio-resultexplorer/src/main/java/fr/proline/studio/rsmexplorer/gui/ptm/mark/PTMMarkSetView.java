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

import fr.proline.studio.rsmexplorer.gui.ptm.ViewContext;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewPtmAbstract;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewSetting;
import java.awt.Graphics2D;
import java.util.Map;

/**
 * all PTM view
 *
 * @author Karine XUE
 */
public class PTMMarkSetView extends ViewPtmAbstract {

    Map<Integer, PTMMarkView> m_ptmMap;

    @Override
    public void paint(Graphics2D g, ViewContext viewContext) {
        if (m_ptmMap == null) {
            return;
        }
        for (PTMMarkView pm : m_ptmMap.values()) {
            pm.setBeginPoint(m_x, m_y);
            pm.paint(g, viewContext);
        }
    }

    void setPtmMarkMap(Map<Integer, PTMMarkView> ptmMap) {
        this.m_ptmMap = ptmMap;
    }

    @Override
    public void setBeginPoint(int x, int y) {
        this.m_x = x;
        this.m_y = y;
    }

    protected String getToolTipText(int x, int y) {
        int index = (x - this.m_x) / ViewSetting.WIDTH_AA;
        if (y >= this.m_y && y <= (this.m_y + ViewSetting.HEIGHT_AA * 3)) {
            if (m_ptmMap == null) {
                return null;
            }
            String prefix = "(" + x + "," + y + ") ";
            String tips = "";
            PTMMarkView pm = m_ptmMap.get(index);
            if (pm != null) {
                tips += pm.getPTMShortName() + "(Protein Loc. " + pm.getDisplayedLocationProtein() + ")";
            }
            if (tips.length() == 0) {
                tips = null;
            }
            return tips;
        } else if (y > (this.m_y + ViewSetting.HEIGHT_AA * 3)) {
            return "" + index;
        }

        return null;
    }
}
