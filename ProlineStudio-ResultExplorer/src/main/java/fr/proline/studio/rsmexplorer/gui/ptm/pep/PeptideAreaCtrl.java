/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 28 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.pep;

import fr.proline.studio.rsmexplorer.gui.ptm.PtmSitePeptide;
import java.awt.Graphics2D;
import java.util.ArrayList;

/**
 *
 * @author Karine XUE
 */
public class PeptideAreaCtrl {

    PeptideAreaModel _mgr;
    PeptideSetView _view;


    public PeptideAreaCtrl() {
        _mgr = new PeptideAreaModel();
        _view = new PeptideSetView();
    }
    

    public void setData(ArrayList<PtmSitePeptide> ptmSitePeptide) {
        _mgr.setPTM(ptmSitePeptide);
        _view.setViewPeptideList(_mgr.getViewPeptideList());
    }

    public void setBeginPoint(int x, int y) {
        this._view.setBeginPoint(x, y);
    }

    public void paint(Graphics2D g2, int ajustedLoaction, int areaWidth) {
        this._view.paint(g2, ajustedLoaction, areaWidth);
    }

}
