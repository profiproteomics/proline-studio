/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 28 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.mark;

import fr.proline.studio.rsmexplorer.gui.ptm.PtmSiteAA;
import java.awt.Graphics2D;
import java.util.ArrayList;

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
    
    public void setData(ArrayList<PtmSiteAA> ptmSiteAA2Mark){
        _mgr.setPTM(ptmSiteAA2Mark);
        _view.setPtmMarkList(_mgr.getPTMMarkList());
    }

    public void setBeginPoint(int x, int y) {
       this._view.setBeginPoint(x, y);
    }

    public void paint(Graphics2D g2, int ajustedLoaction, int areaWidth) {
        this._view.paint(g2, ajustedLoaction,areaWidth);
    }
    
}
