/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 28 nov. 2018
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

    public String getToolTipText(int x, int y, int ajustedLocation) {
        return this.m_view.getToolTipText(x, y, ajustedLocation);

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
