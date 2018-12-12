/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 28 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.pep;

import fr.proline.studio.rsmexplorer.gui.ptm.PtmSitePeptide;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewPtmAbstract;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewSetting;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;

/**
 *
 * @author Karine XUE
 */
public class PeptideSetView extends ViewPtmAbstract {

    

    ArrayList<PeptideView> _peptideList;

    public PeptideSetView() {
        _peptideList = null;
    }

    @Override
     public void paint(Graphics2D g, int locationAjusted, int areaWidth){
         
     }
    public void paint(Graphics2D g, int locationAjusted, int areaWidth, int fontWidth) {
        x0 = m_x;
        y0 = m_y;

        for (PeptideView vp : _peptideList) {
            paintGrillX(g, areaWidth, x0, y0);
            paintGrillX(g, areaWidth, x0, y0 + ViewSetting.HEIGHT_AA);
            vp.setBeginPoint(x0, y0);
            g.setColor(ViewSetting.PEPTIDE_COLOR);
            vp.paint(g, locationAjusted, fontWidth);
            y0 += (int) ViewSetting.HEIGHT_AA * 1.5;
        }
    }

    private void paintGrillX(Graphics2D g, int areaWidth, int x, int y) {
        Stroke s = g.getStroke();//remain original stroke
        g.setColor(CyclicColorPalette.GRAY_GRID);
        g.setStroke(ViewSetting.DASHED);
        g.drawLine(x, y, areaWidth, y);
        g.setStroke(s);
    }

    @Override
    public void setBeginPoint(int x, int y) {
        m_x = x;
        m_y = y;
    }

    protected void setViewPeptideList(ArrayList<PeptideView> peptideList) {
        this._peptideList = peptideList;
    }

    protected PtmSitePeptide getSelectedItem(int x, int y) {
        PtmSitePeptide selected = null;
        y0 = m_y;
        if (_peptideList != null) {
            for (PeptideView vp : _peptideList) {
                int yRangA = y0;
                int yRangZ = yRangA + ViewSetting.HEIGHT_AA;
                if (y > yRangA && y < yRangZ) {
                    selected = vp.getSelectedPeptide(x);
                    if (selected != null) {
                        return selected;
                    }
                }
                y0 += (int) ViewSetting.HEIGHT_AA * 1.5;
            }
        }
        return selected;
    }

    protected int getSelectedItemIndex(int x, int y) {
        PtmSitePeptide selected = null;
        y0 = m_y;
        PeptideView vp;
        if (_peptideList != null) {
            for (int i = 0; i < _peptideList.size(); i++) {
                vp = _peptideList.get(i);
                int yRangA = y0;
                int yRangZ = yRangA + ViewSetting.HEIGHT_AA;
                if (y > yRangA && y < yRangZ) {
                    selected = vp.getSelectedPeptide(x);
                    if (selected != null) {
                        return i;
                    }
                }
                y0 += (int) ViewSetting.HEIGHT_AA * 1.5;
            }
        }
        return -1;
    }

}
