/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 28 nov. 2018
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

    protected String getToolTipText(int x, int y, int ajustedLocation) {
        int index = (x - this.m_x) / ViewSetting.WIDTH_AA + ajustedLocation;
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
