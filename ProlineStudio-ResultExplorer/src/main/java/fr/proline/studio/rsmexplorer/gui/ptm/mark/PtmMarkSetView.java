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
import java.util.List;

/**
 *
 * @author Karine XUE
 */
public class PtmMarkSetView extends ViewPtmAbstract {

    private List<PtmMarkView> _PtmMarkList;

    @Override
    public void paint(Graphics2D g, ViewContext viewContext) {
        for (PtmMarkView pm : _PtmMarkList) {
            pm.setBeginPoint(m_x, m_y);
            pm.paint(g, viewContext);
        }
    }

    void setPtmMarkList(List<PtmMarkView> ptmMarkList) {
        this._PtmMarkList = ptmMarkList;
    }

    @Override
    public void setBeginPoint(int x, int y) {
        this.m_x = x;
        this.m_y = y;
    }

    protected String getToolTipText(int x, int y, int ajustedLocation) {
        if (y >= this.m_y && y <= this.m_y + ViewSetting.HEIGHT_AA * 2 && _PtmMarkList != null) {
            int index = (x - this.m_x) / ViewSetting.WIDTH_AA + ajustedLocation;
            for (PtmMarkView pm : _PtmMarkList) {
                if (pm.getLocationProtein() == index) {
                    return "Location in Protein: " + index;
                } 
            }
        }
        return null;
    }
}
