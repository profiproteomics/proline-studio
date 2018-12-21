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
public class PtmMarkCtrl {
    
    PtmMarkModel _mgr;
    PtmMarkSetView _view;
    
    public PtmMarkCtrl() {
        _mgr = new PtmMarkModel();
        _view = new PtmMarkSetView();
        
    }
    
    public void setData(Collection<PTMMark> ptmMarks){
        _mgr.setPTM(ptmMarks);
        _view.setPtmMarkList(_mgr.getPTMMarkList());
    }

    public void setBeginPoint(int x, int y) {
       this._view.setBeginPoint(x, y);
    }

    public void paint(Graphics2D g2, ViewContext viewContext) {
        this._view.paint(g2, viewContext);
    }

    public String getToolTipText(int x, int y, int ajustedLocation) {
       return this._view.getToolTipText(x,y, ajustedLocation);
              
    }
    
}
