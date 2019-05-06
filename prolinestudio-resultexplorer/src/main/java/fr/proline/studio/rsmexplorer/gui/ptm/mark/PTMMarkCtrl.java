/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 28 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.mark;

import fr.proline.studio.rsmexplorer.gui.ptm.PTMMark;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewContext;

import java.awt.Graphics2D;
import java.util.Collection;

/**
 *
 * @author Karine XUE
 */
public class PTMMarkCtrl {
    
    PTMMarkModel m_mgr;
    PTMMarkSetView m_view;
    
    public PTMMarkCtrl() {
        m_mgr = new PTMMarkModel();
        m_view = new PTMMarkSetView();
        
    }
    
    public void setData(Collection<PTMMark> ptmMarks){
        m_mgr.setPTM(ptmMarks);
        m_view.setPtmMarkList(m_mgr.getPTMMarkList());
    }

    public void setBeginPoint(int x, int y) {
       this.m_view.setBeginPoint(x, y);
    }

    public void paint(Graphics2D g2, ViewContext viewContext) {
        this.m_view.paint(g2, viewContext);
    }

    public String getToolTipText(int x, int y, int ajustedLocation) {
       return this.m_view.getToolTipText(x,y, ajustedLocation);
              
    }
    
}
