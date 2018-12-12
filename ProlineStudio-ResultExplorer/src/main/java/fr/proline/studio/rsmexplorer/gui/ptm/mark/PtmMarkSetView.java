/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 28 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.mark;

import fr.proline.studio.rsmexplorer.gui.ptm.ViewContext;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewPtmAbstract;
import java.awt.Graphics2D;
import java.util.ArrayList;

/**
 *
 * @author Karine XUE
 */
public class PtmMarkSetView extends ViewPtmAbstract{

    private ArrayList<PtmMarkView> _PtmMarkList;
    
    
    @Override
    public void paint(Graphics2D g, ViewContext viewContext) {
        for (PtmMarkView pm : _PtmMarkList) {
            pm.setBeginPoint(m_x, m_y);
            pm.paint(g, viewContext);
        }
    }

    void setPtmMarkList(ArrayList<PtmMarkView> ptmMarkList) {
        this._PtmMarkList = ptmMarkList;
    }
    
    

    @Override
    public void setBeginPoint(int x, int y) {
        this.m_x = x;
        this.m_y = y;
    }
}
