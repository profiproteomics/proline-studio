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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Karine XUE
 */
public class PTMMarkSetView extends ViewPtmAbstract {

    private List<PTMMarkView> m_ptmMarkList = new ArrayList<>();

    @Override
    public void paint(Graphics2D g, ViewContext viewContext) {
        for (PTMMarkView pm : m_ptmMarkList) {
            pm.setBeginPoint(m_x, m_y);
            pm.paint(g, viewContext);
        }
    }

    void setPtmMarkList(List<PTMMarkView> ptmMarkList) {
        if(ptmMarkList == null)
            ptmMarkList = new ArrayList<>();
        this.m_ptmMarkList = ptmMarkList;
    }

    @Override
    public void setBeginPoint(int x, int y) {
        this.m_x = x;
        this.m_y = y;
    }

    protected String getToolTipText(int x, int y, int ajustedLocation) {
        if (y >= this.m_y && y <= this.m_y + ViewSetting.HEIGHT_AA * 2 && m_ptmMarkList != null) {
            int index = (x - this.m_x) / ViewSetting.WIDTH_AA + ajustedLocation;
            for (PTMMarkView pm : m_ptmMarkList) {
                if (pm.getDisplayedLocationProtein() == index) {
                    return "Location in Protein: " + index;
                } 
            }
        }
        return null;
    }
}
