/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 28 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.pep;

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

    private static final Stroke DASHED = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, new float[]{2.0f, 2.0f}, 0.0f);

    ArrayList<PeptideView> _peptideList;

    @Override
    public void paint(Graphics2D g, int locationAjusted, int areaWidth) {
        x0 = m_x;
        y0 = m_y;
        
        for (PeptideView vp : _peptideList) {
            paintGrillX(g, areaWidth, x0, y0);
            vp.setBeginPoint(x0, y0);
            g.setColor(ViewSetting.PEPTIDE_COLOR);
            vp.paint(g, locationAjusted, areaWidth);
            paintGrillX(g, areaWidth, x0, y0 + ViewSetting.WIDTH_AA);
            y0 += (int) ViewSetting.WIDTH_AA * 1.5;
        }
    }

    private void paintGrillX(Graphics2D g, int areaWidth, int x, int y) {
        Stroke s = g.getStroke();//remain original stroke
        g.setColor(CyclicColorPalette.GRAY_GRID);
        g.setStroke(DASHED);
        g.drawLine(x, y, areaWidth, y);
        g.setStroke(s);
    }

    @Override
    public void setBeginPoint(int x, int y) {
        m_x = x;
        m_y = y;
    }

    void setViewPeptideList(ArrayList<PeptideView> peptideList) {
        this._peptideList = peptideList;
    }


}
