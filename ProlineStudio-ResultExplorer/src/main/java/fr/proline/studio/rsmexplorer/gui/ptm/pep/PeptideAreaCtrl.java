/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 28 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.pep;

import fr.proline.studio.rsmexplorer.gui.ptm.PtmSitePeptide;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewContext;

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

    public void paint(Graphics2D g2, ViewContext viewContext) {
        this._view.paint(g2, viewContext);
    }

    public PtmSitePeptide getSelectedItem(int x, int y) {
        PtmSitePeptide result = this._view.getSelectedItem(x, y);
        return result;

    }

    public int getSelectedItemIndex(int x, int y) {
        int index = this._view.getSelectedItemIndex(x, y);
        this._mgr.setSelctedItem(index);//change select 
        return this._view.getSelectedItemIndex(x, y);
    }

    public void setSelectedIndex(int i) {
        this._mgr.setSelctedItem(i);
    }


}
