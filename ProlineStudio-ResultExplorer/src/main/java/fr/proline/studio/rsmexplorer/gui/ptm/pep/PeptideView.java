/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 27 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm.pep;

import fr.proline.studio.rsmexplorer.gui.ptm.PtmSiteAA;
import fr.proline.studio.rsmexplorer.gui.ptm.PtmSitePeptide;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewPtmAbstract;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewSetting;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class PeptideView extends ViewPtmAbstract {

    private static Logger logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");
    private int _length;
    private int _beginIndex; //begin index at the protein level location 
    private ArrayList<PtmSiteAA> _ptmSiteAAList;
    private PtmSitePeptide _peptide;
    private float _score;

    public PeptideView(PtmSitePeptide pep) {
        this._peptide = pep;
        this._length = pep.getSequence().length();

        _ptmSiteAAList = pep.getPtmSiteAAList();
        _beginIndex = pep.getBeginInProtein();
    }

    @Override
    public void paint(Graphics2D g, int locationAjusted, int AreaWidth) {
        //public abstract void drawRoundRect(int x, int y, int width, int height,int arcWidth, int arcHeight);
        this.x0 = this.m_x + (this._beginIndex - locationAjusted) * ViewSetting.WIDTH_AA;
        this.y0 = this.m_y;
        int width = this._length * ViewSetting.WIDTH_AA;
        int height = ViewSetting.WIDTH_AA;
        g.drawRoundRect(x0, y0, width, height, ViewSetting.WIDTH_AA, ViewSetting.WIDTH_AA);
        g.fillRoundRect(x0, y0, width, height, ViewSetting.WIDTH_AA, ViewSetting.WIDTH_AA);
        for (PtmSiteAA modifyA : _ptmSiteAAList) {
            paintPtm(g, modifyA, y0);
        }

    }

    @Override
    public void setBeginPoint(int x, int y) {
        m_x = x;
        m_y = y;
    }

    /**
     * paint the PTM on a Amino Acide
     *
     * @param g
     * @param modifyA
     * @param y01
     */
    private void paintPtm(Graphics2D g, PtmSiteAA modifyA, int y01) {
        Color colorOld = g.getColor();
        g.setColor(modifyA.getColorWithProbability());
        int location = modifyA.getModifyLocPep();
        int x1 = (location - 1) * ViewSetting.WIDTH_AA;
        if (location == 0) {
            g.fillRoundRect(this.x0 + x1, y01, ViewSetting.WIDTH_AA, ViewSetting.WIDTH_AA, ViewSetting.WIDTH_AA / 2, ViewSetting.WIDTH_AA);
        } else if (location == 1) {
            g.fillRoundRect(this.x0 + x1, y01, ViewSetting.WIDTH_AA, ViewSetting.WIDTH_AA, ViewSetting.WIDTH_AA / 2, ViewSetting.WIDTH_AA);
            g.fillRect(this.x0 + x1 + ViewSetting.WIDTH_AA / 2, y01, ViewSetting.WIDTH_AA - ViewSetting.WIDTH_AA / 2, ViewSetting.WIDTH_AA);
        } else if (location == this._length) {
            g.fillRect(this.x0 + x1, y01, ViewSetting.WIDTH_AA - ViewSetting.WIDTH_AA / 2, ViewSetting.WIDTH_AA);
            g.fillRoundRect(this.x0 + x1, y01, ViewSetting.WIDTH_AA, ViewSetting.WIDTH_AA, ViewSetting.WIDTH_AA / 2, ViewSetting.WIDTH_AA);

        } else {
            g.fillRect(this.x0 + x1, y01, ViewSetting.WIDTH_AA, ViewSetting.WIDTH_AA);//draw one Letter wide
        }
        g.setColor(colorOld);
    }

    private String getToolTipText(double x, double y) {

        PtmSiteAA pa = getSelect(x, y);
        if (pa == null) {
            return "peptide info: ??";
        } else {
            return "" + pa.getPtmSite() + "  PTM site probability :" + pa.getProbability() * 100 + "%";
        }

    }

    //@todo
    private PtmSiteAA getSelect(double x, double y) {
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        int x = e.getX();
        int y = e.getY();
        logger.debug("!!!! Mouse clicked in ViewPeptide");
    }

}
